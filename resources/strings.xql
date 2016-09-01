(:~ 
  Here, we check the nature of the strings in programs.
  We're looking for base64 strings or for hexliterals
  
  reference for the b64 regex: http://stackoverflow.com/a/8571649/827519
:)

(: threshold as percentage of the string :)
let $threshold_hex := 50.0
let $re_b64   := "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$"
let $re_hex   := "((?:\\x[a-zA-Z0-9]{2})+)"
let $re_q     := string-join(("['",'"]+'),'') (: single and double quotes :)
let $partial  := (
    for $doc in db:list("xtrees")
    where matches($doc,"^unknown/.*\.php$")
    (: top-level document node :)
    let $common_doc := db:open("xtrees",$doc)
    (: all string nodes :)
    let $anodes := $common_doc//string

    (: search for base64_decode in the RHS of an assignment statement's strings :)
    let $instrb64_nodes := $common_doc//assignmentOperator/following-sibling::*
    let $instrb64_seq :=
      for $exp_node in $instrb64_nodes
        let $exp_node_raw_text := string-join($exp_node//text())
        let $exp_text := replace($exp_node_raw_text,"[\.'" || '"]+',"")
        return 
          if(matches($exp_text,"base64_decode"))
            then 1
            else 0
            
            
    for $s in $anodes
    group by $doc
    let $instrb64 := sum($instrb64_seq)
    let $elem :=
    element file {
      attribute path {$doc},
      (: map operator :)
      $s ! (
          (: $ss is an iterator over all the strings in document $doc :)
          let $ss := .
          let $stx  := string-join($ss//text(),'') (: collect text from the node and all its descendants :)
          let $swq  := replace($stx,$re_q,"")      (: without quotes :)
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
              attribute instrb64 {$instrb64},
              $swb64
            }
      )
    }
    return $elem
  )
(: we now aggregate to find 
   the percentage of base64 and hex literals in each file.
   handle undefined values.
   :)
let $files :=
for $doc in $partial
  let $total_length_strings  := sum($doc//string//@lwq//number())
  let $total_b64 := sum($doc//string//@lb64//number())
  let $total_hex := sum($doc//string//@lhex//number())
  let $phex      := round-half-to-even($total_hex div $total_length_strings, 3)
  let $phex      := if(fn:exists($phex)) then $phex else 0
  let $pb64      := round-half-to-even($total_b64 div $total_length_strings, 3)
  let $pb64      := if(fn:exists($phex)) then $pb64 else 0
  let $hasinstrb64 := ($doc//@instrb64//number() > 0)
  return element {$doc/node-name()} {
    attribute path {$doc/@path},
    attribute phex {$phex},
    attribute pb64 {$pb64},
    attribute hasinstrb64 {$hasinstrb64}
  }
return element root { $files }
