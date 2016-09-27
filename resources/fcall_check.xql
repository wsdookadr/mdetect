(:~
  Iterates over all documents(parse trees)
  in the database. Counts the function calls in each
  file.
  
  Timing: 5.3 seconds on 7313 documents
:)

module namespace m = 'http://com.mdetect/modules/fcall_check';
declare function m:fcalls() {
    let $partial := 
        element root {
        for $dbkey in db:list("xtrees")  (: [position() = (40 to 50)] :)
        where matches($dbkey,"^unknown/.*\.php$")
        group by $dbkey
        let $root := db:open("xtrees",$dbkey)
        (: all functionCall nodes :)
        let $all_fcalls := $root//functionCall
        return element file {
            attribute path  { $dbkey } ,
            attribute total { count($all_fcalls)},
            (:~ regular function calls :)
            let $anodes := $all_fcalls//identifier//text()
            (:~ variable function calls :)
            let $fvar   := $all_fcalls//functionCallName//chainBase//keyedVariable//text()
            (:~ union of the two :)
            let $all    := ($anodes | $fvar)
            for $func in distinct-values($all)
            let $cnt := count($all[.=$func])
            where $cnt > 0
            order by $func descending
            group by $func
            return element func {
              attribute cnt {$cnt},
              $func
            }
        }
    }
    return $partial
};



