(:~ 
  Here, we check the nature of the strings in programs.
  We're looking for base64 strings or hexliterals
  
  reference for the b64 regex: http://stackoverflow.com/a/8571649/827519
  
  TODO: add support for detecting gzipped strings
    reference for gzinflate detection:
    - https://tools.ietf.org/html/rfc1950#page-4
    - http://stackoverflow.com/a/29268776/827519

  Timing: takes 4.6 seconds when BaseX has 7338 documents
:)

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
