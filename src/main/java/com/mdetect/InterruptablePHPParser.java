package com.mdetect;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * subclass PHPParser and allow the thread it will run in to be interrupted
 * (because for some files, it will take a longer time, and we need to be able
 * to timeout these if necessary).
 * the most frequent call in PHPParser is enterRule, and if we override that,
 * then we allow for thread interruptions.
 * 
 */
public class InterruptablePHPParser extends PHPParser {
	private static final Logger logger = LoggerFactory.getLogger(InterruptablePHPParser.class);
	private String filePath = null;
	
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