package com.mdetect;


import org.basex.core.*;
//import org.basex.api.client.ClientSession;
import org.basex.core.cmd.*;
import org.basex.util.Prop;
import org.basex.util.list.StringList;
import org.basex.BaseXServer;
import org.basex.api.client.ClientSession;
import java.io.*;

public class XmlStore {
	public String dbName = "xtrees";
	ClientSession session = null;
	static BaseXServer server = null;
	protected static Context context;
	/*
	 * create database and schema
	 */
	public void createDB() {
		try {
			context = new Context();
			new CreateDB(dbName, "").execute(context);
			new CreateIndex("fulltext").execute(context);
		} catch (Exception e) {
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

	public void add(String xmlKey, String xmlString) {
		try {
			session.execute("OPEN " + this.dbName);
			InputStream is = new ByteArrayInputStream(xmlString.getBytes());
			session.add("/"+xmlKey, is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void startServer() {
		try {
			int serverPort = 1984;
			context = new Context();
		    final StringList sl = new StringList("-z", "-p " + Integer.toString(serverPort), "-q");
		    server = new BaseXServer(sl.finish());
		    final String path = System.getenv("HOME");
		    Prop.put(StaticOptions.DBPATH, path + "/data");
		    Prop.put(StaticOptions.WEBPATH, path + "/webapp");
		    Prop.put(StaticOptions.RESTXQPATH, path + "/webapp");
		    Prop.put(StaticOptions.REPOPATH, path + "/repo");
		    Prop.put(StaticOptions.SERVERPORT, Integer.toString(serverPort));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void stopServer()  {
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
		createDB();
		makeSession();
	}
}
