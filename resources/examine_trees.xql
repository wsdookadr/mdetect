(:~
  This query runs metrics on the parse trees
  that were previously computed.

  It works by iterating over all documents 
  in the database. First, we extract only the function
  calls and group them by files.
  
  
:)

for $d in db:list("xtrees") 
where matches($d,"^.*\.php$")
for $t in db:open("xtrees",$d)//functionCall//identifier//text()
group by $d
order by $d
return element file {
  attribute path { $d },
  $t ! element call { data() }
}