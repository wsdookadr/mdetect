package com.mdetect;


import org.basex.core.*;
//import org.basex.api.client.ClientSession;
import org.basex.core.cmd.*;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Prop;
import org.basex.util.list.StringList;
import org.basex.BaseXServer;
import org.basex.api.client.ClientQuery;
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
			context.close();
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
			session.execute("OPEN " + dbName);
			//session.query("CHECK " + dbName + "; SET DTD false;");
			InputStream is = new ByteArrayInputStream(xmlString.getBytes());
			session.add("/"+xmlKey, is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	protected String eval(final String query) throws QueryException, IOException {
		final ArrayOutput ao = new ArrayOutput();
		try (final QueryProcessor qp = new QueryProcessor(query, context)) {
			//qp.sc.baseURI(BASEURI);
			qp.register(context);
			try (final Serializer ser = qp.getSerializer(ao)) {
				qp.value().serialize(ser);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				qp.unregister(context);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ao.toString();
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

	public static void startServer() {
		try {
			int serverPort = 1984;
		    final String path = System.getenv("HOME") + "/";
		    final StringList sl = new StringList("-z", "-p " + Integer.toString(serverPort), "-q");
			context = new Context();
		    server = new BaseXServer(sl.finish());
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
		makeSession();
	}
}
