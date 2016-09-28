(:~
  MAST1 (recursive bottom-up)

  This transformation will build a MAST(Merkleized abstract syntax tree)
  We'll use this later on in order to identify matching portions of
  logic in different programs.
  
  This is a summary of the bottom-up construction of a Merkle tree:
  start from leafs, assign the leafs a hash based on their node name.
  Then, progressively, for each higher level, take a node, concatenate
  its name with the previously computed hashes for all of its children,
  compute a hash of that, and then assign the hash to the node. Continue
  the entire process up the root node.
  
  Use-case: we use the MAST to identify subtrees that carry the same form.
  
  [1] https://hal.archives-ouvertes.fr/hal-00627811/document
  [2] http://www.mit.edu/~jlrubin/public/pdfs/858report.pdf
    
    
  MAST2 implementation (second version, non-recursive bottom-up, addressing memory issues)
  
  For a large parse tree of max depth 1957 and 13269 nodes
  the previous implementation will exit with "Out of main memory".
  Because of that, a non-recursive bottom-up approach is needed.
  Starting from the leaf nodes, at each stage, we maintain an array
  of items. We maintain a list of counts too for the next parent, and we
  increment it each time. We only advance to it if the count is previously c-1
  where c is the number of children for that parent (meaning, all the other
  children are ready, and with the current one, that means all children are ready
  to advance).
  To keep track of the nodes, BaseX provides the function db:node-id.
  On the other hand, we'll need some arrays and maps(dictionaries), and the XQuery
  standard provides those too [2], and BaseX implements those [3].
  This updating will be done until the root is reached. Along the way, the list of
  nodes that we need to keep up-to-date will shrink in size, reaching size 1
  when it reaches the root element.
  
  [1] https://mailman.uni-konstanz.de/pipermail/basex-talk/2011-January/001072.html
  [2] https://www.w3.org/TR/xpath-functions-31/#maps-and-arrays
  [3] http://docs.basex.org/wiki/Map_Module
  
:)

declare function local:mast($node, $d) {
  typeswitch($node)
    case element()
    return 
      (:~ 
          prevent a stack overflow
          (the trees we operate on usually have 
           max depth 500)
      :)
      if($d le 3000)
      then (
        (:~ compute all the children recursively :)
        let $newc := (
          for $c in $node/node()
          return local:mast($c, $d + 1)
        )
        (:~ get the children's hashes :)
        let $nc := string-join($newc ! @h,'')
        (:~ get the current node name :)
        let $nn := node-name($node)
        (:~ concatenate current node name with the children hashes :)
        let $msg := $nn || $nc
        (:~ 
            compute a hash for the entire subtree rooted 
            in the current node
        :)
        let $subtree_hash := crypto:hmac($msg,'hardcodedkey','md5','hex') 
        return element {$nn} {
          attribute h {$subtree_hash},
          $node/@start,
          $node/@end,
          $newc
        }
      )
      else
        ()
    case text() return ()
    default return ()
};

(:~ 
    get all documents, build
    their MAST, and store it back into the datastore

  :)
for $doc in db:list("xtrees")
where matches($doc,"^unknown/.*\.php")
let $tree := db:open("xtrees", $doc)
let $mast_name := "/mast/" || $doc
let $mast_tree := local:mast($tree/node(),1)
return 
  if($mast_tree/*)
  then db:add("xtrees",$mast_tree,$mast_name)
  else ()
  



