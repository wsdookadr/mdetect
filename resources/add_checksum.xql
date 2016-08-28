# if the document is not there, create it
OPEN xtrees; XQUERY if(count(fn:collection('/xtrees/checksums.xml')) = 0) then (db:add('xtrees', <doc></doc>, 'checksums.xml') ) else ()

# insert a new entry
OPEN xtrees; XQUERY insert node <entry><path>%s</path><gtag>%s</gtag><checksum>%s</checksum><size>%s</size></entry> into collection('/xtrees/checksums.xml')/doc
