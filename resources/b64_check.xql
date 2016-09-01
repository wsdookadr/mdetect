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
    where matches($doc,"^.*\.php$")
    let $anodes := db:open("xtrees",$doc)//string
    for $s in $anodes
    group by $doc
    let $elem :=
    element file {
      attribute path {$doc},
      (: map operator :)
      $s ! (
          (: $ss is an iterator over all the strings in document $doc :)
          let $ss := .
          let $stx  := string-join($ss//text(),'') (: collect text from the node and all its descendants :)
          let $swq  := replace($stx,$re_q,"")     (: without quotes :)
          let $lwq  := string-length($swq)
          let $shx  := replace($swq,$re_hex,"")   (: remove hex literals :)
          let $lwhx := string-length($shx) (: in chars, length of string without hex literals :)
          (: how much of the string was a hex literal string:)
          let $phx  := 
              if($lwhx=0)
              then 0
              else round-half-to-even(((($lwq - $lwhx)) div number($lwq)) * 100, 3)
    
          (: removed b64 :)
          let $swb64 := replace($swq,$re_b64,'')
          (: length without b64 :)
          let $lwb64 := string-length($swb64)
          (: length of b64 :)
          let $lb64 := $lwq - $lwb64
          let $pb64 := 
              if($lwq=0)
              then 0
              else round-half-to-even(($lb64 div number($lwq)) * 100, 3)
          return
            element string {
              attribute hex {$phx} ,
              attribute pb64 {$pb64},
              $swb64
            }
      )
    }
    return $elem
  )
return $partial
  
  