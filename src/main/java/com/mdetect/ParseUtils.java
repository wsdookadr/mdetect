package com.mdetect;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


public class ParseUtils {
	private static final Logger logger = LoggerFactory.getLogger(ParseUtils.class);
	
	/*
	 * Returns a Parser object (that contains the AST)
	 */
    public static Pair<Parser, Lexer> parsePHP(String filePath) {
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
    public static Map<Integer, String> getInvTokenMap(Parser p) {
    	Map<String, Integer> tokenMap = p.getTokenTypeMap();
    	Map<Integer, String> invMap = new HashMap<Integer, String>();
    	for (String i: tokenMap.keySet()) {
    		invMap.put(tokenMap.get(i), i);
    	};
    	return invMap;
    }
    

    public static Document processFile(String filePath) {
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
    
	public ParseUtils() {
	}
	
}
