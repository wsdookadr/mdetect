(:~
  This is a scratchpad for analyzing tree depth, redundant node chains
  and other parts of the tree structure.
  
  
  Here's an example result of this program:
  
  <result>
    <depth>1956</depth>
    <nodes>13268</nodes>
    <maxlscc>10</maxlscc>
    <tpfr>7002</tpfr>
  </result>
  
  This means that out of a tree with depth 1956 and 13268 nodes,
  there's a longest one-child chain of length 10. And if we were
  to remove all redundant chains(chains that have only nodes with
  one-child), we would be removing 7002 nodes from the tree.

:)

(:
  computes depth of tree rooted in $root
:)
declare function local:max-depth($root as node()?) as xs:integer? {
   if($root/*)
   then max($root/*/local:max-depth(.)) + 1
   else 1
};

(:
  computes longest chain of one-child nodes in
  the entire tree rooted in $root.
  
  the base case are leafs which are one-child
  chains of length 1.
  
  if one node along the way has multiple children, the
  count is "reset" and re-computed for that node's children.
:)
declare function local:max-len-chain($root as node()?) as xs:integer? {
  let $nc := count($root/*)
  return
    if($nc > 1)      then max($root/*/local:max-len-chain(.))
    else if($nc = 1) then max($root/*/local:max-len-chain(.)) + 1
    else 1
};

(: 
  get the longest single-child chain starting at this node
  and going up to the root (stopping somewhere along the way
  if a multi-child node is found).
  
  http://docs.basex.org/wiki/Higher-Order_Functions_Module#hof:take-while
:)
declare function local:lscc($n as node()?) {
  (: take ancestor list in reverse order (bottom-up)  :)
  let $ancestors := reverse($n/ancestor::node())
  let $has-one-child := function($x){count($x/*) = 1}
  (:
    take from the ancestor list as long 
    as the parent has one child
  :)
  let $chain := hof:take-while($ancestors, $has-one-child)
  return $chain
};


(: 
   tpfr(stands for "total planned for removal")

   this works like local:lscc and produces all chains.
   some of the chains in $ids might be sub-chains of other ones.
   so it gets distinct values at the end to avoid double-counting
   later on.
:)
declare function local:tpfr($r as node()?) {
  let $ids := 
  for $n in $r//*
    return
      let $ancestors := reverse($n/ancestor::node())
      (: has one child and is unmarked :)
      let $cond := function($x) {count($x/*) = 1}
      let $chain := hof:take-while($ancestors, $cond)
      (: create new visited entries for the map :)
      let $ma := $chain ! db:node-id(.)
      (: return them in separate arrays (keeping all the chains separate) :)
      return array {$ma}
  (:
    the same chains but with node-names instead of node ids
  :)
  let $nids := $ids ! (
    let $i := .
    let $unpacked := fn:data($i)
    return
      (: open nodes in chains again to get their names :)
      if(count($unpacked) > 1)
      then [ $unpacked ! db:open-id("xtrees",.)/node-name() ]
      else ()
  )
  (: ids planned for removal (avoiding double-usage) :)
  let $pids := distinct-values($ids ! fn:data(.))
  return $pids
};

(: root of the parse tree :)
let $r := db:open("xtrees","unknown/home/user/work/mdetect/samples/sample.php")/node()
(: maximum depth of tree :)
let $md  := local:max-depth($r) 
(: count all nodes in the tree :)
let $an  := count($r//*)
(: apply lscc to every node in the tree :)
let $cl  := $r//* ! count(local:lscc(.))
(: get maximum lscc length :)
let $mcl := max($cl)
(: total nodes that would be planned for removal :)


let $tpr := count(local:tpfr($r))

return
  <result>
  <depth>{$md}</depth>
  <nodes>{$an}</nodes>
  <maxlscc>{$mcl}</maxlscc>
  <tpfr>{$tpr}</tpfr>
  </result>
