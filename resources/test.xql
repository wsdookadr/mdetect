
(:~ 
  search for base64_decode in the RHS of an assignment statement's strings
  
  :)

let $nodes := db:open("xtrees","unknown/home/user/work/mdetect/samples/sample.php")//assignmentOperator/following-sibling::*
let $instrb64_seq :=
for $exp_node in $nodes
  let $exp_node_raw_text := string-join($exp_node//text())
  let $exp_text := replace($exp_node_raw_text,"[\.'" || '"]+',"")
  return 
    if(matches($exp_text,"base64_decode"))
    then 1
    else 0
let $any_instrb64 := sum($instrb64_seq)
return $any_instrb64