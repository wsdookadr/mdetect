package com.mdetect;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * This class serializes an ANTLR4 parse tree into an
 * org.w3c.dom.Document using a stack to store the current path
 * from the root element. 
 * 
 */

/*
 * TODO: add offset-based start/end for each node.
 * 		 http://www.antlr.org/api/JavaTool/org/antlr/v4/runtime/ParserRuleContext.html
 */

public class ParseTreeDOMSerializer implements ParseTreeListener {
	private Map<Integer, String> invTokenMap = null;
	private Document domDoc = null;
	private Stack<Element> nodeStack = null;
	private boolean debugMode = false;
	private TokenStream tokenStream = null;
    private final List<String> ruleNames;
    public ParseTreeDOMSerializer(List<String> ruleNames, Map<Integer, String> invTokenMap, TokenStream tokenStream) {
    	this.tokenStream = tokenStream;
        this.ruleNames = ruleNames;
        this.invTokenMap = invTokenMap;
        nodeStack = new Stack<Element>();
        
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
			Element root = (Element) document.createElement("ast");
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
    
    public Pair<Integer, Integer> getLineRange(ParserRuleContext ctx) {
    	Pair<Integer, Integer> result = null; 
    	Interval sourceInterval = ctx.getSourceInterval();
    	Token firstToken = tokenStream.get(sourceInterval.a);
    	Token lastToken = tokenStream.get(sourceInterval.b);
    	result = new Pair<Integer, Integer>(firstToken.getLine(), lastToken.getLine());
    	return result;
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
        Pair<Integer, Integer> interval = getLineRange(ctx);
        Element newNode = (Element) domDoc.createElement(ruleName);
		if (interval != null) {
			newNode.setAttribute("start", Integer.toString(interval.a));
			newNode.setAttribute("end", Integer.toString(interval.b));
		}
		
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