package com.mdetect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


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
	
	public static String getResource(String path) {
		String result = null;
		try {
			InputStream is =  Class.class.getClass().getResourceAsStream(path);
			InputStreamReader isr = new InputStreamReader(is);
			result = IOUtils.toString(isr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/*
	 * Git-compatible SHA-1 checksum
	 * 
	 * blob <length>\0content
	 * reference https://git-scm.com/book/en/v2/Git-Internals-Git-Objects#Object-Storage
	 */
	public static String gitHash(String path) {
		File file = new File(path);
		long length = file.length();
		byte[] content = null;
		String sha1 = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			content = IOUtils.toByteArray(fis);
			byte header[] = String.format("blob %d\0", length).getBytes();
			byte[] toHash = new byte[header.length + content.length];
			System.arraycopy(header ,0,toHash,			  0,header.length);
			System.arraycopy(content,0,toHash,header.length,content.length);
			sha1 = DigestUtils.sha1Hex(toHash);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return sha1;
	}

	public static void processAndStore(String filePath, Detector d, XmlStore xstore) {
		Document w = d.processFile(filePath); 
		String contentsToInsert = "";
		try {
			contentsToInsert = Utils.serializeDOMDocument(w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		xstore.add(filePath, contentsToInsert,true);
		System.out.println("finished processing "+filePath);
	}
	
	

}
