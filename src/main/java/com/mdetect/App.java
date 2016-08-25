package com.mdetect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;

import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMObjectModel;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;


public class App {
	/*
	 * Overview:
	 * 
	 * Files with unknown checksums will be determined, and  
	 * a series of metrics will be computed on them. After that, a 
	 * set of rules would mark some of them as being suspicious.
	 * 
	 */

	 public static void main(String[] args) {
		 Detector d = new Detector();
	     //d.loadFile("/home/user/work/mdetect/samples/mod_system/adodb.class.php.txt");
	     //d.loadFile("/home/user/work/mdetect/samples/sample.php.txt");
		 d.loadFile("/home/user/work/mdetect/samples/mod_system/pdo.inc.php.suspected");
		 //d.loadFile("/home/user/work/mdetect/data/wordpress/wp-includes/class-phpmailer.php");
		 //d.loadFile("/home/user/work/mdetect/data/drupal/core/modules/datetime/src/Tests/DateTimeFieldTest.php");
		 d.runChecks();
	 }
	 
	 
}


