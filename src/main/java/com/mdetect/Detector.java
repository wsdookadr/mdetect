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
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamSource;

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
	 *   names that appear to be random (high) 
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
	 * Optional: Maybe also add logic to detect commonly used obfuscators:
	 * 
	 * - http://www.pipsomania.com/best_php_obfuscator.do
	 * - https://github.com/prakharprasad/carbylamine-php-encoder
	 * - http://sysadmin.cyklodev.com/
	 * - http://www.joeswebtools.com/security/php-obfuscator/
	 *  
	 *  Note: Git-compatible file hash with JGit
	 *  	  http://stackoverflow.com/a/19789797/827519
	 *  
	 *  TODO: Some of the larger files in wordpress contain & and other symbols.	
	 *  	  Because the serialization is incomplete, Saxon will throw
	 *  	  exceptions on account of invalid XML.
	 *  	  Need to fix that either by serializing valid XML, or directly building
	 *  	  the XML tree. 
	 */
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
     * Inverts a map (K->V to V->K)
     */
    public Map<Integer, String> getInvTokenMap(Parser p) {
    	Map<String, Integer> tokenMap = p.getTokenTypeMap();
    	Map<Integer, String> invMap = new HashMap<Integer, String>();
    	for (String i: tokenMap.keySet()) {
    		invMap.put(tokenMap.get(i), i);
    	};
    	return invMap;
    }

    public Document processFile(String filePath) {
    	Pair<Parser, Lexer> pl = parsePHP(filePath);
    	PHPParser parser = (PHPParser) pl.a;
    	parser.setBuildParseTree(true);
        /* 
         * htmlDocument is the start rule (the top-level rule)
         * for the PHP grammar
         */
    	ParserRuleContext tree =   parser.htmlDocument();
    	List<String> ruleNames = Arrays.asList(parser.getRuleNames());
    	Map<Integer, String> invTokenMap = getInvTokenMap(parser);
    	TokenStream tokenStream = parser.getTokenStream();
    	ParseTreeDOMSerializer ptSerializer = new ParseTreeDOMSerializer(ruleNames, invTokenMap, tokenStream);
    	ParseTreeWalker.DEFAULT.walk(ptSerializer, tree);
    	Document result= ptSerializer.getDOMDocument();
    	return result;
    }
    
	public Detector() {
	}
	
	
}
