XQUERY if(not(contains-token(db:list(),"xtrees"))) then ( db:create("xtrees") ) else ()
