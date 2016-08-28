# if the document is not there, create it
XQUERY if(count(fn:collection('/xtrees/checksums.xml')) = 0) then ( db:add('xtrees', 'checksums.xml', <doc></doc>) ) else ()

# insert a new entry
XQUERY insert node <entry><path>%s</path><gtag>%s</gtag><checksum>%s</checksum><size>%s</size></entry> into collection(checksums.xml)

