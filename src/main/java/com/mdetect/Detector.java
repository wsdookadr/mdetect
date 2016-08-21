package com.mdetect;

import java.util.ArrayList;
import java.util.Arrays;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.mdetect.*;
import com.mdetect.PHPParser.HtmlDocumentContext;


public class Detector {
	/*
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
	 * - use of the base64_decode PHP function (high)
	 * - presence of eval() (high)
	 */
	
	public float ruleChr() {
		
		return 0.0f;
	}
	
	public float ruleOrd() {
		
		return 0.0f;
	}
	
	public float ruleHex() {
		
		return 0.0f;
	}
	
	public float ruleBase64() {
		
		return 0.0f;
	}

    public static ArrayList<String> getAnnotationsFromSyncRequest(String reqBody) {
        ANTLRInputStream input = new ANTLRInputStream(reqBody);
        PHPLexer lexer = new PHPLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PHPParser parser = new PHPParser(tokens);

        
        PHPParser.HtmlDocumentContext ctx = (HtmlDocumentContext) parser.getContext();
        /*List<String> ruleNames = Arrays.asList(parser.getRuleNames());
        OrgParseTreeConvertor listener = new OrgParseTreeConvertor(ruleNames);
        ParseTree tree = ctx;
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        */
        
        ArrayList<String> retval = new ArrayList<String>();
        return retval;
        
        
    }
    
	public Detector() {
	}
}
