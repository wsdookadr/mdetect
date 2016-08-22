package com.mdetect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

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
	 *   http://php.net/manual/ro/functions.variable-functions.php
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

	public class SampleTreeListener implements ParseTreeListener {
	    private final List<String> ruleNames;
	    public SampleTreeListener(List<String> ruleNames) {
	        this.ruleNames = ruleNames;
	    }

	    public String extractRuleName(ParserRuleContext ctx) {
	        int ruleIndex = ctx.getRuleIndex();
	        String ruleName;
	        if (ruleIndex >= 0 && ruleIndex < ruleNames.size()) {
	            ruleName = ruleNames.get(ruleIndex);
	        } else {
	            ruleName = Integer.toString(ruleIndex);
	        };
	        return ruleName;
	    }

	    @Override
	    public void visitErrorNode(ErrorNode node) {
	    	
	    }

	    @Override
	    public void exitEveryRule(ParserRuleContext ctx) { 
	        String ruleName = extractRuleName(ctx);
	    }

	    @Override
	    public void enterEveryRule(ParserRuleContext ctx) { 
	        String ruleName = extractRuleName(ctx);
	        // ctx.getText()
			if (ctx.getText() != null) {
				System.out.println(ctx.getText());
			}
	    }
	    
	    @Override
	    public void visitTerminal(TerminalNode node) {
	    }
	}

	/*
	 * Returns a Parser object (that contains the AST)
	 */
    public static PHPParser parsePHP(String filePath) {
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
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        /* 
         * 
         * htmlDocument is the root term in the PHP grammar
         * (the grammar in use for this project)
         * 
         */
        
        /*
          //this is listener-based matching, we're moving to tree matching
        PHPParser.HtmlDocumentContext ctx = (HtmlDocumentContext) parser.getContext();
        List<String> ruleNames = Arrays.asList(parser.getRuleNames());
        SampleTreeListener listener = new SampleTreeListener(ruleNames);
        ParseTree tree = ctx;
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        ArrayList<String> retval = new ArrayList<String>();
        */
        return parser;
    }
    
    public static void testTreeMatch() {
    	//PHPParser p = parsePHP("/home/user/work/mdetect/samples/mod_system/adodb.class.php.txt");
    	//PHPParser p = parsePHP("/home/user/work/mdetect/samples/sample.php.txt");
    	PHPParser p = parsePHP("/tmp/a.php.txt");
    	ParserRuleContext prc = p.htmlDocument();
    	System.out.println("parsed..");
    	List<String> ruleNames = Arrays.asList(p.getRuleNames());
    	//String a1 = prc.getText();
    	System.out.println(prc.toStringTree(ruleNames));
    }
    
	public Detector() {
	}
}
