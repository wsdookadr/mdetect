XQUERY if(count(fn:collection('%s')) = 0) then (db:add('%s', <doc></doc>, '%s') ) else ()
