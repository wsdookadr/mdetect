package com.mdetect;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

	/*
	 * serialize Document
	 */
	public static String serializeDOMDocument(org.w3c.dom.Document doc) {
		/*
		 * DOMImplementationLS domImplementation = (DOMImplementationLS)
		 * doc.getImplementation(); LSSerializer lsSerializer =
		 * domImplementation.createLSSerializer(); String bodyXML =
		 * lsSerializer.writeToString(doc); return bodyXML;
		 */

		// DocumentBuilderFactory domFact =
		// DocumentBuilderFactory.newInstance();
		// DocumentBuilder builder = domFact.newDocumentBuilder();
		// Document doc = builder.parse(st);

		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		try {
			transformer.transform(domSource, result);
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}
		String xmlString = writer.toString();
		return xmlString;

		/*
		 * DOMImplementationRegistry registry; try { registry =
		 * DOMImplementationRegistry.newInstance(); } catch (Exception e) {
		 * e.printStackTrace(); return null; } DOMImplementationLS domImplLS =
		 * (DOMImplementationLS) registry.getDOMImplementation("LS");
		 * 
		 * LSSerializer lsSerializer = domImplLS.createLSSerializer();
		 * DOMConfiguration domConfig = lsSerializer.getDomConfig();
		 * domConfig.setParameter("format-pretty-print", true);
		 * 
		 * LSOutput lsOutput = domImplLS.createLSOutput();
		 * lsOutput.setEncoding("UTF-8"); return
		 * lsSerializer.writeToString(doc);
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
	 * 
	 * Convert a org.w3c.dom.Document => net.sf.saxon.dom.DocumentWrapper =>
	 * net.sf.saxon.s9api.XdmNode
	 * 
	 */
	public static XdmNode convertDOMToXDM(Document dom) {
		if (dom == null) {
			System.out.println("document is null");
			return null;
		}
		Processor proc = new Processor(false);
		Configuration config = proc.getUnderlyingConfiguration();
		config.registerExternalObjectModel(new DOMObjectModel());
		DocumentWrapper dw = new DocumentWrapper(dom, dom.getBaseURI(), config);
		XdmNode xdmRoot = new XdmNode(dw.getRootNode());
		return xdmRoot;
	}

	public static void processAndStore(String filePath, Detector d, XmlStore xstore) {
		d.processFile(filePath);
		Document w = d.domDoc;
		String contentsToInsert = "";
		try {
			contentsToInsert = Utils.serializeDOMDocument(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		xstore.createDB();
		xstore.add(filePath, contentsToInsert,true);
		System.out.println("finished processing "+filePath);
	}

}
