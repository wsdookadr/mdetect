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
import org.apache.commons.io.IOUtils;
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
		context = new Context();
		String xq = Utils.getResource("/create_db.xql");
		xq = xq.replaceAll("\n", "");
		try {
			session.execute(xq);
			new CreateIndex("fulltext").execute(context);
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.close();
		
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
			//session.query("CHECK " + dbName + "; SET DTD false;");
			InputStream is = new ByteArrayInputStream(xmlString.getBytes());
			if (replaceExisting) {
				session.execute("DELETE /" + xmlKey);
			}
			session.add("/"+xmlKey, is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addChecksum(GitFileDTO f, String gTag){
		/*
		 * XQuery template
		 */
		String xqlTmplt = Utils.getResource("/add_checksum.xql");
		if(xqlTmplt == null)
			return;
		String xql = String.format(xqlTmplt, f.getPath(), gTag, f.getSha1(), Integer.toString(f.getFileSize()));
		try {
			session.execute(xql);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	protected String eval(final String query) throws QueryException, IOException {
		final ArrayOutput ao = new ArrayOutput();
		try (final QueryProcessor qp = new QueryProcessor(query, context)) {
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
