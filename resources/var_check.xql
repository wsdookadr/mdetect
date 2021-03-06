(:~ 
   We're computing variable name usage.
:)

let $partial :=
    for $doc in db:list("xtrees")
    where matches($doc,"^unknown/.*\.php$")
    (:~ all variable nodes in the document :)
    let $anodes1 := db:open("xtrees",$doc)//variableInitializer//*[matches(text(),"^\$")]
    let $anodes2 := db:open("xtrees",$doc)//keyedVariable//*[matches(text(),"^\$")]
    let $anodes := ($anodes1 | $anodes2)
    (:~ names of the variables :)
    let $tnodes := $anodes//text()
    (:~ distinct values thereof :)
    let $dnodes := distinct-values($tnodes)
    for $var in $dnodes
    let $cnt := count($anodes//[text()=$var])
    group by $doc
    let $elem := element file {
      attribute path { $doc },
      $var ! (
          let $x := .
          return
            element variable {
              attribute name  { $x } ,
              attribute count {
                count(filter($anodes//[text()=$x], function($y) {$y = true()} )) 
              }
            }
      )
    }
    (:? return json:serialize($elem, map { 'format': 'jsonml' }) :)
    return $elem
(: aggregate :)
let $files:=
    for $doc in $partial
      (:~
        sum of variable occurences at document level.
        computation of probabilities of occurence for each variable name.
      :)
      let $sum     := sum($doc//variable//@count//number())
      return element      {$doc/node-name()} {
        attribute path    {$doc/@path},
        for $var in $doc//variable
          let $prob    := round-half-to-even($var//@count//number() div $sum, 3)
          return $var update insert node attribute prob {$prob} into .
        }
return element root  {$files}

