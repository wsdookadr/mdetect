
(:~ 
  search for base64_decode in the RHS of an assignment statement's strings
  
  :)

let $nodes := db:open("xtrees","unknown/home/user/work/mdetect/samples/sample.php")//assignmentOperator/following-sibling::*
for $exp_node in $nodes
  let $exp_node_raw_text := string-join($exp_node//text())
  let $exp_text := replace($exp_node_raw_text,"[\.'" || '"]+',"")
  return $exp_text
   