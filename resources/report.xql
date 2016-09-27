declare function local:fcalls() {
    let $partial := 
        element root {
        for $dbkey in db:list("xtrees")
        where matches($dbkey,"^unknown/.*\.php$")
        group by $dbkey
        let $root := db:open("xtrees",$dbkey)
        (: all functionCall nodes :)
        let $all_fcalls := $root//functionCall
        return element file {
            attribute path  { $dbkey } ,
            attribute total_fcalls { count($all_fcalls)},
            (:~ regular function calls :)
            let $anodes := $all_fcalls//identifier//text()
            (:~ variable function calls :)
            let $fvar   := $all_fcalls//functionCallName//chainBase//keyedVariable//text()
            (:~ union of the two :)
            let $all    := ($anodes | $fvar)
            for $func in distinct-values($all)
            let $cnt := count($all[.=$func])
            where $cnt > 0
            order by $func descending
            group by $func
            return element func {
              attribute cnt {$cnt},
              $func
            }
        }
    }
    return $partial
};

declare function local:strings() {
    let $re_b64   := "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$"
    let $re_hex   := "((?:\\x[a-zA-Z0-9]{2})+)"
    let $re_q     := string-join(("['",'"]+'),'') (: single and double quotes :)
    let $re_endl  := "[\n]"
    let $partial  := (
        for $doc in db:list("xtrees")
          where matches($doc,"^unknown/.*\.php$")
        (: top-level document node :)
        
        let $common_doc := db:open("xtrees",$doc)
        let $strings :=
        for $e in $common_doc//assignmentOperator//following-sibling::*
          (:
          $e is an array of all the siblings following the assignmentOperator node(the RHS).
          we're interested in getting the string descendants of those siblings.
          :)

          (:
          if the RHS is a PHP key/value Array, then just take its values (not the keys, so the
          3rd child of the arrayItem node).
          otherwise just get all the strings in the RHS.
          :)
          let $r1 := 
            if($e//arrayItemList)
            then $e//arrayItem/*[3]/text()
            else $e//string//text()
          
          let $nnames := string-join($e//arrayItemList//arrayItem/*[3], ';')
          
          let $r2 := $r1 ! replace(.,$re_q,"")
          let $r3 := string-join($r2,'')
          (: swq is the entire string in the RHS, without quotes :)
          let $swq  := $r3
          let $instrb64 := if(matches($swq,"base64_decode")) then 1 else 0
          let $lwq  := string-length($swq)
          let $shx  := replace($swq,$re_hex,"")    (: remove hex literals :)
          let $lwhx := string-length($shx)         (: in chars, length of string without hex literals :)
          let $lhx  := $lwq - $lwhx                (: length of hex literal :)

          (: per-string percentage hex literal :)
          let $phx  := 
              if($lwhx=0)
              then 0
              else round-half-to-even(($lhx div number($lwq)), 3)

          let $swb64 := replace($swq,$re_b64,'')   (: removed b64 :)
          let $lwb64 := string-length($swb64)      (: length without b64 :)
          let $lb64 := $lwq - $lwb64               (: length of b64 :)
          

          (: per-string percentage base64:)
          let $pb64 := 
              if($lwq=0)
              then 0
              else round-half-to-even(($lb64 div number($lwq)), 3)

          return
              element string {
                attribute lhex {$lhx} ,
                attribute lb64 {$lb64},
                attribute lwq  {$lwq} ,
                attribute instrb64 {$instrb64}
                (:attribute nnames {$nnames}, :)
                (: $swb64 :)
              }
          return 
          element file {
            attribute path {$doc},
            $strings
          }
      )
    let $files :=
    for $doc in $partial
      let $total_length_strings  := sum($doc//string//@lwq//number())
      let $total_b64 := sum($doc//string//@lb64//number())
      let $total_hex := sum($doc//string//@lhex//number())
      let $phex      := 
        if($total_length_strings > 0)
        then round-half-to-even($total_hex div $total_length_strings, 3)
        else 0
      let $phex      := if(fn:exists($phex)) then $phex else 0
      let $pb64      := 
        if($total_length_strings > 0)
        then round-half-to-even($total_b64 div $total_length_strings, 3)
        else 0
      let $pb64      := if(fn:exists($phex)) then $pb64 else 0
      let $hasinstrb64 := ($doc//@instrb64//number() > 0)
      (:
      return file metadata such as percentage (out of the total length of strings found)
      that are hex literals, base64
      :)
      return element {$doc/node-name()} {
        attribute path {$doc/@path},
        attribute phex {$phex},
        attribute pb64 {$pb64},
        attribute hasinstrb64 {$hasinstrb64},
        attribute total_strings {$total_length_strings}
      }
    return element root { $files }
};

declare function local:merge() {
  (:
    merging the data gathered so far
  :)
  let $A := local:strings()/file
  let $B := local:fcalls()/file
  (: full outer join using for sets $A and $B on file path :)
  for $f in (
    for $a in $A
    for $b in $B
    where $a/@path = $b/@path
      return element file {
      $a/@*,
      $b/@*[name()!="path"],
      $b/*
      }
    ,
    for $a in $A
    where fn:empty($B[@path=$a/@path])
      return element file {
      $a/@*
      }
  )
  return $f
  (: merge the attributes but avoid the duplicate attribute path :)

};


declare function local:detect() {
  (:  
  use custom detection rules 
  :)
  let $files :=
    for $f in local:merge()
      (: percentage usage of chr :)
      let $pchr  := 
        if($f/@total_fcalls > 0)
        then $f[text()="chr"]/@cnt div $f/@total_fcalls
        else 0
      
      (: eval calls :)
      let $ev    := $f/func[text()="eval"]/@cnt
      (: decoding b64 either via direct fcall or intention to use b64 via its presence in file strings :)
      let $rule1 := ($f/func[text()="base64_decode"] or $f[@hasinstrb64="true"])
      (: percentage of overall b64 in file :)
      let $rule2 := ($f/@pb64 > 0.5)
      (: percentage of overall hex in file :)
      let $rule3 := ($f/@phex > 0.5)
      (: does not include external modules :)
      let $rule4 := $f/func[text()="include"]/@cnt = 0
      (: excessive usage of chr :)
      let $rule5 := $pchr > 0.5
      
      return
      if(
        ($rule1 and $rule2 and $ev > 0) or
        ($rule3 and $rule4)
      )
      then element file {attribute path {$f/@path}}
      else ()
  return element report {
    $files
  }
};

local:detect()
