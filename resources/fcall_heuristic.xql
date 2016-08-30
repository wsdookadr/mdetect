(:~

  This query runs metrics on the parse trees
  that were previously computed.

  It works by iterating over all documents(parse trees)
  in the database. Counts the function calls in each
  file and reports back a list of per-file counts.


  TODO: Also need to account for variable function calls
  //functionCall//functionCallName//chainBase//keyedVariable
:)

let $z := 1
return element root {
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
    (:? return json:serialize($elem, map { 'format': 'jsonml' }) :)
    return $elem
}
