ADD x1.xml;
XQUERY insert node 
<entry>
	<path>%s</path>
	<gtag>%s</gtag>
	<checksum>%s</checksum>
	<size>%s</size>
</entry>
into doc('x1.xml');

