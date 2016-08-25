package com.mdetect;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;




public class ParseTreeDOMSerializer implements ParseTreeListener {
	private Map<Integer, String> invTokenMap = null;
	private Document domDoc = null;
	private Stack<Element> nodeStack = null;
	private boolean debugMode = false;
    private final List<String> ruleNames;
    public ParseTreeDOMSerializer(List<String> ruleNames, Map<Integer, String> invTokenMap) {
        this.ruleNames = ruleNames;
        this.invTokenMap = invTokenMap;
        nodeStack = new Stack<Element>();
        
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
			Element root = (Element) document.createElement("root");
			document.appendChild(root);
			nodeStack.push(root);
			domDoc = document;
		} catch (Exception pce) {
			pce.printStackTrace();
		}
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
		if (debugMode) {
			System.out.println("exit->" + ruleName);
		}
        nodeStack.pop();
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) { 
        String ruleName = extractRuleName(ctx);
        Element newNode = (Element) domDoc.createElement(ruleName);
		if (debugMode) {
			System.out.println("enter->" + ruleName);
		}
		if (ctx.getText() != null && ctx.getChildCount() == 0) {
			//newNode.setTextContent(ctx.getText());
			newNode.appendChild(domDoc.createTextNode(ctx.getText()));
		}
		nodeStack.peek().appendChild(newNode);
		nodeStack.push(newNode);
    }
    
    @Override
    public void visitTerminal(TerminalNode node) {
    	//int nodeType = node.getSymbol().getType();
    	//String nodeValue = "TERM_" + invTokenMap.get(nodeType);
    	String termValue = node.getText();
    	if(termValue != null) {
    		/*
    		 * Avoid special case for end-of-file
    		 * where terminal value will interefere
    		 * with XMl formatting.
    		 * 
    		 */
    		if(termValue.equals("<EOF>")) {
    			termValue = "EOF";
    		}
    		
    		Element newNode = (Element) domDoc.createElement("term");
			if (debugMode) {
				System.out.println("enter/exit term");
			}
			
			newNode.appendChild(domDoc.createTextNode(termValue));
    		nodeStack.peek().appendChild(newNode);
    		/* 
    		 * we're not pushing it onto the stack because there's no entry/exit callback pair
    		 * like there is for rules
    		 * 
    		 */
    	}
    }
    
    public Document getDOMDocument() {
	    return domDoc;
    }
}