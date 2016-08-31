package com.mdetect;

import java.io.IOException;
import java.util.ArrayList;

import org.basex.BaseXClient;
import org.basex.query.QueryException;
import org.basex.query.QueryModule.Lock;
import org.w3c.dom.Document;

public class ASTCheckRunner {
	private XmlStore xstore = null;
	public ASTCheckRunner(XmlStore xstore) {
		this.xstore = xstore;
	}
	/*
	 * Uses the QueryProcessor, which doesn't need
	 * the server to be open.
	 * 
	 */
	public void check1() {
		try {
			String query = Utils.getResource("/fcall_check.xql");
			ArrayList<String> r = xstore.eval(query);
			Document d = Utils.parseToDOM(r.get(0));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
