(:~

  This transformation will build a MAST(Merkelized abstract syntax tree)
  We'll use this later on in order to identify matching portions of
  logic in different programs.
  
  This is a summary of the bottom-up construction of a merkle tree:
  start from leafs, assign the leafs a hash based on their node name.
  Then, progressively, for each higher level, take a node, concatenate
  its name with the previously computed hashes for all of its children,
  compute a hash of that, and then assign the hash to the node. Continue
  the entire process up the root node.
  
  Use-case: we use the MAST to identify subtrees that carry the same form.
  
  [1] https://hal.archives-ouvertes.fr/hal-00627811/document
  [2] http://www.mit.edu/~jlrubin/public/pdfs/858report.pdf
    
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

(:~ get all documents, build
    their MAST, and store it back into the
    datastore
  :)
for $doc in db:list("xtrees")
where matches($doc,"^.*\.php")
let $tree := db:open("xtrees", $doc)
let $mast_name := "/mast/" || $doc
let $mast_tree := local:mast($tree/node(),1)
return 
  if($mast_tree/*)
  then db:add("xtrees",$mast_tree,$mast_name)
  else ()
  



