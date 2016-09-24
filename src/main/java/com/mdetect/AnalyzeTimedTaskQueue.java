package com.mdetect;

import java.io.File;
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
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AnalyzeTimedTaskQueue extends AbstractTimedTaskQueue<String, Pair<String, String>, AnalyzeTimedTaskWorker> {
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
		return new AnalyzeTimedTaskWorker(workUnit);
	}

	/*
	 * Computes the task timeout.
	 * We're expecting larger files to take a longer time.
	 */
	public Long estimateTaskTimeout(String workUnit) {
		File f = new File(workUnit);
		Long kb = f.length() / 1024;

		if (kb < 5) {
			return new Long(1400);
		} else if (kb < 10) {
			return new Long(2000);
		} else if (kb < 100) {
			return new Long(5000);
		} else if (kb < 200) {
			return new Long(8000);
		}

		return new Long(10000);
	}
	
	public void onTaskCompletion(Pair<String, String> w) {
		String filePath = w.getLeft();
		/* 
		 * This is the AST serialized in XML format and stored as a string.
		 * And we're writing this to BaseX 
		 */
		String serializedAST = w.getRight();
		xstore.add(storePrefix + filePath, serializedAST, true);
	}

}