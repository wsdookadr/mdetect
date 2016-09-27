(:

get documents that have the same top-level fingerprint
as the one we're searching for.

the matches will have a very similar structure (since
their parse tree structure will be the same).

:)
declare function local:find-similar($h) {
  for $doc in collection("xtrees")
    where
      matches(document-uri($doc),'^/xtrees/mast/') and
      $doc/ast/@h = $h
    return document-uri($doc)
};

local:find-similar("EF5AEDF475A424316E366A786CFDE487")

