package com.mdetect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

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
	        }
	        else {
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
	    }
	    
	    @Override
	    public void visitTerminal(TerminalNode node) {
	    }
	}

	
    public static ArrayList<String> getAnnotationsFromSyncRequest(String reqBody) {
        ANTLRInputStream input = new ANTLRInputStream(reqBody);
        PHPLexer lexer = new PHPLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PHPParser parser = new PHPParser(tokens);

        /* 
         * htmlDocument is the root term in the PHP grammar
         * (the grammar in use for this project)
         * 
         */
        PHPParser.HtmlDocumentContext ctx = (HtmlDocumentContext) parser.getContext();
        List<String> ruleNames = Arrays.asList(parser.getRuleNames());
        /*
        SampleTreeListener listener = new SampleTreeListener(ruleNames);
        ParseTree tree = ctx;
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        */
        
        ArrayList<String> retval = new ArrayList<String>();
        return retval;
    }
    
	public Detector() {
		
	}
}
