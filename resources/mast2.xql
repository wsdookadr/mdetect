(:
MAST2 implementation (second version, non-recursive bottom-up, addressing memory issues)

For very large parse trees of max depth 1957 and 13269 nodes the previous
implementation will exit with the BaseX exception "Out of main memory".

Because of that, a non-recursive bottom-up approach is needed.

Starting from the lowest level, all leaf nodes will compute their
own checksums.  When the entire level is done, we can get to the next
one. At all times, we only have keep two levels in memory, the current
one, and the immediately next level.  This way we minimize memory usage.
About getting the set of parents, running distinct-nodes() or
distinct-nodes-stable on the parent array is quite costly. We want the
distinct set of parents because we compute their values only once.

We might need some arrays and maps(dictionaries) [2] [3], to keep track of the
nodes, BaseX provides the function db:node-id [1].

There's also a need to keep the generated tree (the MAST) somewhere, so
we might need additional memory for that too, and the generated sub-trees
will be merged gradually towards the root. The memory consumption should
be considerably less than the recursive version.

Could either be written in XQuery or in Java as a BaseX module [4].

There's another option, which is copying the tree, and then using
the updating functions [5] to operate on it. This would cause more disk I/O
but should not cause memory problems anymore.

[1] https://mailman.uni-konstanz.de/pipermail/basex-talk/2011-January/001072.html
[2] https://www.w3.org/TR/xpath-functions-31/#maps-and-arrays
[3] http://docs.basex.org/wiki/Map_Module
[4] https://github.com/BaseXdb/basex/blob/master/basex-examples/src/main/java/org/basex/examples/module/ModuleDemo.java
[5] http://docs.basex.org/wiki/XQuery_Update

:)

""


