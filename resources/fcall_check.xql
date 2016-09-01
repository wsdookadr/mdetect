(:~

  This query runs metrics on the parse trees
  that were previously computed.

  Iterates over all documents(parse trees)
  in the database. Counts the function calls in each
  file and reports back a list of per-file counts.


  TODO: Also need to account for variable function calls
  //functionCall//functionCallName//chainBase//keyedVariable
:)

let $partial := 
    element root {
    for $doc in db:list("xtrees")
    where matches($doc,"^.*\.php$")
    (:~ all function call nodes in the document :)
    let $anodes := db:open("xtrees",$doc)//functionCall//identifier
    (:~ names of the function calls :)
    let $tnodes := $anodes//text()
    (:~ distinct values thereof :)
    let $dnodes := distinct-values($tnodes)
    for $func in $dnodes
    let $cnt := count($anodes//[text()=$func])
    group by $doc
    let $elem := element file {
      attribute path { $doc },
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
        for $func in $doc//function
          let $prob    := round-half-to-even($func//@count//number() div $sum, 3)
          return $func update insert node attribute prob {$prob} into .
        }
(: filtering documents returned, based on specific function usage patterns :)
let $filtered :=
    for $doc in $files
    return
    if(
      ($doc//function[@name="chr"]//@prob//number()  > 0.9) or
      ($doc//function[@name="eval"]//@prob//number() > 0.9)
     )
     then element file {attribute path {$doc//@path}}
     else ()
return element root { $filtered }

