(:~

  This query runs metrics on the parse trees
  that were previously computed.

  It works by iterating over all documents(parse trees)
  in the database. First, we extract only the function
  calls and group them by files.

:)


for $doc in db:list("xtrees")
where matches($doc,"^.*\.php$")
let $anodes := db:open("xtrees",$doc)//functionCall//identifier
let $tnodes := $anodes//text()
let $dnodes := distinct-values($tnodes)
for $func in $dnodes
let $cnt := count($anodes//[text()=$func])
group by $doc
let $elem := element file {
  attribute path { $doc },
  $func ! (
      let $x := .
      return
        element function {
          attribute name { $x } ,
          count(filter($anodes//[text()=$x], function($y) {$y = true()} ))
        }
  )
}
return $elem
