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
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamSource;

import com.mdetect.*;
import com.mdetect.PHPParser.HtmlDocumentContext;


/*
 * Ideally, the code in parsePHP should be in such a way
 * that it checks for thread interruptions.
 * 
 * We want to allow the thread that will run this code
 * to be interrupted.
 * 
 * 
 */

public class Detector {
	private static final Logger logger = LoggerFactory.getLogger(Detector.class);
	
    public class InterruptablePHPParser extends PHPParser {
    	
    	private String filePath;
    	
    	public InterruptablePHPParser(CommonTokenStream tokens, String filePath) {
    		super(tokens);
    		this.filePath = filePath;
    	}
    	/*
    	 * Overriding the enterRule method in order to allow thread-interruption
    	 */
		@Override
		public void enterRule(ParserRuleContext ctx, int i, int j) {
			super.enterRule(ctx, i, j);
			if(Thread.currentThread().isInterrupted()) {
				logger.info("[DBG] thread interrupted while parsing " + filePath);
				Thread.yield();
				String exceptionMessage = "thread interrupted " + filePath;
				throw new ParseCancellationException(exceptionMessage);
			}
		}	
    }
	
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
        PHPParser parser = new InterruptablePHPParser(tokens, filePath);
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
