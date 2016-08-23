package com.mdetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.xpath.XPathEvaluator;

import com.mdetect.*;
import com.mdetect.PHPParser.HtmlDocumentContext;


public class Detector {
	/*
	 * 
	 * Note: The grammar works for PHP up to 5.6
	 * 
	 * So far, we want to cover these:
	 * - does not contain common names from 
	 *   Joomla/Wordpress/Drupal ; instead contains
	 * - names that appear to be random (high) 
	 * - excessive use of chr() (medium)
	 * - excessive use of ord() (medium)
	 * - makes use of hex-escaped characters (low)
	 * - excessive use of string concatenation (high)
	 * - long base64 encoded strings (high)
	 *   whether uninterrupted strings or concatenated
	 * - use of the base64_decode PHP function (high)
	 * - presence of eval() (high) relative to other functions
	 * - one way of checking "unusual" names is to split them
	 *   up and see if parts of them are in from a dictionary
	 * - strange variable names can also be considered mixed 
	 * - case letters followed by at least two numbers
	 * - excessive use of indexed string values to concatenate another string
	 * 	 (normally this is quite useless)
	 * - excessive usage of variable function syntax usage
	 * 	 (calling a function by using its name stored 
	 * 	  in a different variable)
	 *   http://php.net/manual/en/functions.variable-functions.php
	 *   or its other form
	 *   http://php.net/manual/en/function.call-user-func.php
	 * - no imports whatsoever, IOW self-contained
	 *   (normally, if a PHP program is a customization or a
	 *    an extension/plugin of an existing framework, it would have
	 *    to reuse some of the API, and if it doesn't, we mark that
	 *    as a red-flag)
	 * 
	 * Note: if there is complex tree matching involved, that should
	 * 		 be done as described in the ANTLR docs
	 * 		 https://github.com/antlr/antlr4/blob/master/doc/tree-matching.md
	 * 
	 * Note: HashSet should be used if we want to check for string belonging to
	 * 		 sets.
	 * 
	 * Will figure out how much of the logic will be in SQL and how much will
	 * be kept in Java. 
	 * 
	 * Reference PHP grammar (for the 5.6.25 release)
	 * https://github.com/php/php-src/blob/e37064dae4a80c70405899bb591969bbe6aad9a8/Zend/zend_language_parser.y
	 * 
	 * Note: To speed things up, threads will be used to parse the 
	 * 		 codebase in parallel
	 * 
	 * Optional: Maybe also add logic to detect commonly used obfuscators:
	 * 
	 * - http://www.pipsomania.com/best_php_obfuscator.do
	 * - https://github.com/prakharprasad/carbylamine-php-encoder
	 * - http://sysadmin.cyklodev.com/
	 * - http://www.joeswebtools.com/security/php-obfuscator/
	 *  
	 *  
	 *  Note: Git-compatible file hash with JGit
	 *  	  http://stackoverflow.com/a/19789797/827519
	 */
	

	Processor xmlProcessor = null;
	/*
	 * Returns a Parser object (that contains the AST)
	 */
    public  Pair<Parser, Lexer> parsePHP(String filePath) {
    	AntlrCaseInsensitiveFileStream input;
		try {
			input = new AntlrCaseInsensitiveFileStream(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
        PHPLexer lexer = new PHPLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PHPParser parser = new PHPParser(tokens);
        /* turn on prediction mode to speed up parsing */
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        Pair<Parser, Lexer> retval = new Pair<Parser, Lexer>(parser, lexer);
        return retval;
    }
    
    /*
     * this function will use the start rule name. it will
     * then attempt to use xpath in order to find motifs inside
     * the AST.
     * 
     * https://github.com/antlr/antlr4/blob/master/doc/tree-matching.md
     * https://github.com/antlr/antlr4/blob/master/tool-testsuite/test/org/antlr/v4/test/tool/TestXPath.java
     * https://github.com/antlr/antlr4/blob/master/runtime-testsuite/test/org/antlr/v4/test/runtime/java/BaseTest.java#L573
     */
    public static void findMotif() {
    	
    }
    
    public  Map<Integer, String> getInvTokenMap(Parser p) {
    	Map<String, Integer> tokenMap = p.getTokenTypeMap();
    	Map<Integer, String> invMap = new HashMap<Integer, String>();
    	
    	for (String i: tokenMap.keySet()) {
    		invMap.put(tokenMap.get(i), i);
    	};

    	return invMap;
    }
    
    /*
     * Receives XML as a string and returns an XdmNode
     * (we can operate with XPath and XQuery on the XdmNode)
     */
    public  XdmNode getXDM(String xmlString) {
    	XdmNode input;
    	DocumentBuilder newDocumentBuilder = xmlProcessor.newDocumentBuilder();
		try {
			StringReader stringReader = new StringReader(xmlString);
			// streamSource = new StreamSource(new ByteArrayInputStream(xmlString.getBytes()))
			StreamSource streamSource = new javax.xml.transform.stream.StreamSource(stringReader);
			//new StreamSource(new FileInputStream(in))
			input = newDocumentBuilder.build(streamSource);
			
			return input;
		} catch (SaxonApiException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		};
		
		return null;
 	}
    
    
    
    public  List<XdmItem> runXPath(XdmNode xdmDoc, String stringXPath) {
    	ArrayList<XdmItem> matched = new ArrayList<XdmItem>();
    	XPathExecutable exec = null;
    	XPathCompiler xpath = xmlProcessor.newXPathCompiler();
    	try {
			exec = xpath.compile(stringXPath);
			XPathSelector eval = exec.load();
			eval.setContextItem(xdmDoc);
			eval.evaluate();
			Iterator<XdmItem> it = eval.iterator();
			XdmItem current = null;
			if (it.hasNext()) {
				current = it.next();
			}
			while (current != null) {
				matched.add(current);
				try {
					current = it.next();
				} catch (NoSuchElementException ex) {
					break;
				}
			}
		} catch (SaxonApiException e) {
			e.printStackTrace();
		}
    	return matched;
    }
    
    public  List<String> testTreeMatch() {
    	//PHPParser p = parsePHP("/home/user/work/mdetect/samples/mod_system/adodb.class.php.txt");
    	//PHPParser p = parsePHP("/home/user/work/mdetect/samples/sample.php.txt");
    	//PHPParser p = parsePHP("/tmp/a.php.txt");
    	List<String> results = new ArrayList<String>();
    	Pair<Parser, Lexer> pl = parsePHP("/home/user/work/mdetect/samples/mod_system/pdo.inc.php.suspected");
    	PHPParser parser = (PHPParser) pl.a;
    	parser.setBuildParseTree(true);
    	
        /* 
         * htmlDocument is the start rule for the PHP grammar
         * (the top-level rule)
         */
    	ParserRuleContext tree =   parser.htmlDocument();
    	List<String> ruleNames = Arrays.asList(parser.getRuleNames());
    	
    	Map<Integer, String> invTokenMap = getInvTokenMap(parser);
    	ParseTreeSerializer ptSerializer = new ParseTreeSerializer(ruleNames, invTokenMap);
    	
    	ParseTreeWalker.DEFAULT.walk(ptSerializer, tree);
    	String strXML = ptSerializer.getXML();
    	XdmNode xdmNode = getXDM(strXML);
    	if(xdmNode == null) {
    		return results;
    	}
    	
    	List<XdmItem> matched = runXPath(xdmNode, "//functionCall");
    	for(XdmItem x: matched) {
    		System.out.println("matched");
    		//System.out.println(x.toString());
    	}
    	return results;
    }
    
	public Detector() {
		xmlProcessor = new Processor(false);
	}
	
	
}
