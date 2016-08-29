(:~
This query runs metrics on the parse trees
that were previously computed.

It works by iterating over all documents in the database.
:)

for $d in db:list("xtrees") 
where matches($d,"^.*\.php$")
for $t in db:open("xtrees",$d)//identifier
return $t