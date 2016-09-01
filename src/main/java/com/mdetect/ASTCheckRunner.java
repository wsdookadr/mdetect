package com.mdetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.basex.BaseXClient;
import org.basex.query.QueryException;
import org.basex.query.QueryModule.Lock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/*
 * Uses the QueryProcessor, which doesn't need
 * the server to be open.
 * 
 */
public class ASTCheckRunner {
	private XmlStore xstore = null;
	
	String fcallQuery = null;
	String varQuery = null;
	String stringsQuery = null;
	String mastQuery = null;
	
	public ASTCheckRunner(XmlStore xstore) {
		this.xstore = xstore;
		this.fcallQuery = Utils.getResource("/fcall_check.xql");
		this.stringsQuery = Utils.getResource("/strings.xql");
		this.varQuery = Utils.getResource("/var_check.xql");
		this.mastQuery = Utils.getResource("/mast.xql");
	}

	public List<String>checkFCalls() {
		ArrayList<String> result = new ArrayList<String>();
		try {
			String query = Utils.getResource("/fcall_check.xql");
			ArrayList<String> r = xstore.eval(query);
			Document doc = Utils.parseToDOM(r.get(0));
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList n1 = (NodeList) xPath.evaluate(
					"/root/file",
					doc.getDocumentElement(),
					XPathConstants.NODESET
					);
			for (int i1 = 0; i1 < n1.getLength(); ++i1) {
				Element e1 = (Element) n1.item(i1);
				String path = e1.getAttribute("path");
				int functions=0;
				NodeList n2 = (NodeList) xPath.evaluate(
						"//function",
						e1,
						XPathConstants.NODESET
						);
				for (int i2 = 0; i2 < n2.getLength(); ++i2) {
					Element e2 = (Element) n2.item(i1);
					String name = e2.getAttribute("name");
					int count = Integer.parseInt(e2.getAttribute("count"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}
