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
let $dnodes := distinct-values($anodes//text())
for $func in $dnodes
group by $doc,$func
let $cnt := $anodes//*[text()=$func]
let $elem := element file {
  attribute path { $doc },
  $func ! element call {
    attribute cnt { count($cnt) },
    data() 
  }
}
return $elem

