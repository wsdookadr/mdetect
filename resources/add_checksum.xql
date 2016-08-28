# if the document is not there, create it
XQUERY if(count(fn:collection('/xtrees/x2.xml')) == 0) then ( db:add('x1.xml', <doc></doc>) ) else ()

# insert a new entry
XQUERY insert node 
<entry>
	<path>%s</path>
	<gtag>%s</gtag>
	<checksum>%s</checksum>
	<size>%s</size>
</entry>
into doc('x1.xml')

