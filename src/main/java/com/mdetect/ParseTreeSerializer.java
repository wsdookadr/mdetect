package com.mdetect;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;


public class ParseTreeSerializer implements ParseTreeListener {
	private String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private String xmlBuffer = "";
	private Map<Integer, String> invTokenMap = null;
	
    private final List<String> ruleNames;
    public ParseTreeSerializer(List<String> ruleNames, Map<Integer, String> invTokenMap) {
        this.ruleNames = ruleNames;
        this.invTokenMap = invTokenMap;
        xmlBuffer += xmlHeader + "\n";
        
        
    }
    
    /*
     * Receives a parser rule as parameter.
     * Uses the parser rule to find out the rule index in 
     * the ruleNames list and retrieves the rule name
     * 
     * Returns the rule name as a string.
     */
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
        xmlBuffer += "</" + ruleName + ">\n";
        
        
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) { 
        String ruleName = extractRuleName(ctx);
        xmlBuffer += "<" + ruleName + ">\n";
        
		if (ctx.getText() != null && ctx.getChildCount() == 0) {
			//xmlBuffer += ctx.getText() + "\n";
		}
    }
    
    @Override
    public void visitTerminal(TerminalNode node) {
    	int nodeType = node.getSymbol().getType();
    	String nodeValue = "TERM_" + invTokenMap.get(nodeType);
    	String termValue = node.getText();
    	
    	if(node.getText() != null) {
    		/*
    		 * Avoid special case for end-of-file
    		 * where terminal value will interefere
    		 * with XMl formatting.
    		 * 
    		 */
    		
    		if(termValue.equals("<EOF>")) {
    			termValue = "EOF";
    		};
    		
    		xmlBuffer += "<term>";
    		xmlBuffer += termValue;
    		xmlBuffer += "</term>\n";
    	}
    }
    
    public String getXML() {
	    return xmlBuffer;
    }
}