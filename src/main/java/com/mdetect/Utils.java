package com.mdetect;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class Utils {
	
	public static String serializeDOMDocument(org.w3c.dom.Document doc) {
		DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
		LSSerializer lsSerializer = domImplementation.createLSSerializer();
		return lsSerializer.writeToString(doc);

		/*
		DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		DOMImplementationLS domImplLS = (DOMImplementationLS) registry.getDOMImplementation("LS");

		LSSerializer lsSerializer = domImplLS.createLSSerializer();
		DOMConfiguration domConfig = lsSerializer.getDomConfig();
		domConfig.setParameter("format-pretty-print", true);

		LSOutput lsOutput = domImplLS.createLSOutput();
		lsOutput.setEncoding("UTF-8");
		return lsSerializer.writeToString(doc);
		*/
	}
	
	public static Document buildTestDOM() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
			Element root = (Element) document.createElement("rootElement");
			document.appendChild(root);
			root.appendChild(document.createTextNode("Some"));
			root.appendChild(document.createTextNode(" "));
			root.appendChild(document.createTextNode("text"));
		} catch (Exception pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();
		}
		return document;
	}
	 
}
