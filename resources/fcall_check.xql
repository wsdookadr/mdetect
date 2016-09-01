(:~

  Iterates over all documents(parse trees)
  in the database. Counts the function calls in each
  file.

:)

let $partial := 
    element root {
    for $doc in db:list("xtrees")
    where matches($doc,"^.*\.php$")
    (:~ all function call nodes in the document :)
    let $anodes := db:open("xtrees",$doc)//functionCall//identifier
    (:~  variable function calls :)
    let $fvar   := db:open("xtrees",$doc)//functionCall//functionCallName//chainBase//keyedVariable
    (:~ names of the function calls :)
    let $tnodes := $anodes//text()
    (:~ distinct values thereof :)
    let $dnodes := distinct-values($tnodes)
    for $func in $dnodes
    let $cnt := count($anodes//[text()=$func])
    group by $doc
    
    (: variable syntax function call score :)
    let $fvarscore := 
      if(count($anodes) = 0)
      then 0
      else (count($fvar) div count($anodes))
      
    let $elem := element file {
      attribute path { $doc },
      attribute fvarscore { $fvarscore },
      $func ! (
          (:~ 
             retain implicit iterator of simple map operator in $x
             in order to avoid ambiguity
           :)
          let $x := .
          return
            element function {
              attribute name  { $x } ,
              attribute count {
                count(filter($anodes//[text()=$x], function($y) {$y = true()} )) 
              }
            }
      )
    }
    return $elem
}
(: aggregate and compute probabilities of occurence for each function name :)
let $files := 
    for $doc in $partial/node()
      let $sum  := sum($doc//function//@count//number())
      return element {$doc/node-name()} {
        attribute path {$doc/@path},
        attribute fvarscore {$doc/@fvarscore},
        for $func in $doc//function
          let $prob    := round-half-to-even($func//@count//number() div $sum, 3)
          return $func update insert node attribute prob {$prob} into .
        }

(: filtering documents returned, based on specific function usage patterns :)
let $filtered :=
    for $doc in $files
    let $score_total := 0
    let $score_chr  := $doc//function[@name="chr"]//@prob//number()
    let $score_eval := $doc//function[@name="eval"]//@prob//number()
    let $score_fvar := $doc/@fvarscore//number()
    return
     element file {
       attribute chr  {$score_chr},
       attribute eval {$score_eval},
       attribute fvar {$score_fvar},
       attribute path {$doc//@path}
     }
return element root { $filtered }
