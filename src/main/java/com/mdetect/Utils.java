package com.mdetect;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMObjectModel;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

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
			System.exit(-1);
		}
		return document;
	}
	/*
	 * Convert a 
	 * 	 org.w3c.dom.Document =>
	 *   net.sf.saxon.dom.DocumentWrapper =>
	 *   net.sf.saxon.s9api.XdmNode
	 * 
	 */
	public static XdmNode convertDOMToXDM(Document doc) {
		 Document dom = Utils.buildTestDOM();
		 if(dom == null) {
			 System.out.println("document is null");
			 return null;
		 }
		 System.out.println(Utils.serializeDOMDocument(dom));
		 Processor proc = new Processor(false);
		 Configuration config = proc.getUnderlyingConfiguration(); 
		 config.registerExternalObjectModel(new DOMObjectModel());
		 DocumentWrapper dw = new DocumentWrapper(dom, dom.getBaseURI(), config);
		 XdmNode xdmRoot = new XdmNode(dw.getRootNode());
		 return xdmRoot;
	}
	 
}
