package com.mdetect;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.basex.BaseXServer;
import org.basex.api.client.ClientSession;
import org.basex.core.Context;
import org.basex.core.StaticOptions;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.XQuery;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.iter.Iter;
import org.basex.query.value.item.Item;
import org.basex.util.Prop;
import org.basex.util.list.StringList;

public class XmlStore {
	public String dbName = "xtrees";
	ClientSession session = null;
	static BaseXServer server = null;
	protected Context context;

	/*
	 * create database and schema
	 */
	public void createDB() {
		context = new Context();
		try {
			/* check if the database is present, otherwise throw exception */
			session.execute("LIST " + dbName);
			session.execute("OPEN " + dbName);
		} catch (IOException e) {
			String dbNotFoundErrorMessage = String.format("Database '%s' was not found.", dbName);
			String exceptionMessage = e.getMessage();
			if(exceptionMessage != null && exceptionMessage.equals(dbNotFoundErrorMessage)) {
				try {
					/* 
					 * create database and indexes 
					 * then select the database to be used
					 */
					new CreateDB(dbName,"").execute(context);
					new org.basex.core.cmd.Open(dbName).execute(context);
					session.execute("OPEN " + dbName);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else {
				e.printStackTrace();
			}
		}
		context.close();
	}
	
	public void createIndexes() {
		try {
			session.execute("OPEN " + dbName);
			session.execute("CREATE INDEX text");
			session.execute("CREATE INDEX attribute");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void makeSession() {
		try {
			session = new ClientSession("localhost", 1984, "admin", "admin");
		} catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/*
	 * Adds an XML document to the XML store.
	 * The replaceExisting parameter indicates if it's
	 * desirable to only have one such document per key
	 * in the store
	 * (for more details
	 * 	see https://mailman.uni-konstanz.de/pipermail/basex-talk/2011-July/001823.html) 
	 * 
	 */
	public void add(String xmlKey, String xmlString, boolean replaceExisting) {
		try {
			session.execute("OPEN " + dbName);
			InputStream is = new ByteArrayInputStream(xmlString.getBytes());
			if (replaceExisting) {
				session.execute("DELETE /" + xmlKey);
			}
			session.add("/"+xmlKey, is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	public void addChecksumDoc() {
		executeStoredQuery("/create_doc.xql","/xtrees/checksums.xml","xtrees","checksums.xml");
	}
	
	public void addChecksum(GitFileDTO f, String gTag){
		executeStoredQuery("/add_checksum.xql",f.getPath(), gTag, f.getSha1(), Integer.toString(f.getFileSize()));
	}
	*/
	
	public void executeStoredQuery(String q, String ...args) {
		/* 
		 * retrieve stored query
		 * render it using the arguments passed
		 * and execute it
		 */
		String storedXql = Utils.getResource(q);
		if(storedXql == null)
			return;
		String xql = String.format(storedXql, args);
		try {
			session.execute(xql);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	ArrayList<String> eval(final String query) throws QueryException, IOException {
		ArrayList<String> result = new ArrayList<String>();
		// Create a query processor
		try (QueryProcessor proc = new QueryProcessor(query, context)) {
			Iter iter = proc.iter();
			for (Item item; (item = iter.next()) != null;) {
				result.add(item.serialize().toString());
			}
		}
		return result;
	}


	public String query(String query) {
		try {
			session.execute("OPEN " + this.dbName);
			//ClientQuery cq = session.query(query);
			return session.execute(new XQuery(query));
			//return cq;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void startServer() {
		try {
			int serverPort = 1984;
		    final String path = System.getenv("HOME") + "/BaseXData";
		    final StringList sl = new StringList("-z", "-p " + Integer.toString(serverPort), "-q");
			context = new Context();
		    server = new BaseXServer(sl.finish());
		   
		    Prop.put(StaticOptions.DBPATH, path + "/");
		    Prop.put(StaticOptions.WEBPATH, path + "/webapp");
		    Prop.put(StaticOptions.RESTXQPATH, path + "/webapp");
		    Prop.put(StaticOptions.REPOPATH, path + "/repo");
		    Prop.put(StaticOptions.SERVERPORT, Integer.toString(serverPort));
		    //Prop.put(StaticOptions.PARALLEL, Integer.toString(20));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServer()  {
		if (server != null) {
			try {
				server.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public XmlStore() {
		startServer();
		makeSession();
		createDB();
		createIndexes();
		//addChecksumDoc();
	}
}
