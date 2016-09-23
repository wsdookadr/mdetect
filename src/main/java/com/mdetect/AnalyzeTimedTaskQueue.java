package com.mdetect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.concurrent.ConcurrentLinkedQueue;


public class AnalyzeTimedTaskQueue extends AbstractTimedTaskQueue<String, AnalyzeTimedTaskWorker> {
	private static final Logger logger = LoggerFactory.getLogger(AnalyzeTimedTaskQueue.class);
    private final XmlStore xstore;
	private String storePrefix;

    public AnalyzeTimedTaskQueue(Integer capacity, XmlStore xstore, String storePrefix) {
    	super(capacity);
    	this.xstore = xstore;
    	this.storePrefix = storePrefix;
        logger.info("Starting analyze queue");
    }

	public AnalyzeTimedTaskWorker createTaskWorker(String workUnit) {
		return new AnalyzeTimedTaskWorker(workUnit, xstore);
	}

	/*
	 * Computes the task timeout
	 */
	public Long estimateTaskTimeout(String workUnit) {
		/* hardcoded at the moment */
		return new Long(5000);
	}

}