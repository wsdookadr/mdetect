package com.mdetect;

import java.io.IOException;

import org.basex.BaseXClient;
import org.basex.query.QueryException;
import org.basex.query.QueryModule.Lock;

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
	@Lock(read = { "HEAVYIO" })
	public void check1() {
		try {
			String query = Utils.getResource("/fcall_check.xql");
			xstore.serialize(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
