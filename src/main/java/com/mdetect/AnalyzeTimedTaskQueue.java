package com.mdetect;


import java.io.File;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzeTimedTaskQueue
		extends AbstractTimedTaskQueue<String, Pair<String, String>, AnalyzeTimedTaskWorker> {
	private static final Logger logger = LoggerFactory.getLogger(AnalyzeTimedTaskQueue.class);
	private final XmlStore xstore;
	private String storePrefix;
	private Long totalProcessed = null;
	private Long tsFirst = null;
	private Long tsLast = null;
	private Long peakMem = null;
	/* whether to collect performance data */
	public boolean bPerf = true;

	public AnalyzeTimedTaskQueue(Integer capacity, XmlStore xstore, String storePrefix) {
		super(capacity);
		this.xstore = xstore;
		this.storePrefix = storePrefix;
		logger.info("Starting analyze queue");
		totalProcessed = new Long(0);
		tsFirst = new Long(this.getEpochMilli());
		tsLast = new Long(0);
		peakMem = new Long(0);
	}

	public AnalyzeTimedTaskWorker createTaskWorker(String workUnit) {
		return new AnalyzeTimedTaskWorker(workUnit);
	}

	/*
	 * Computes the task timeout. We're expecting larger files to take a longer
	 * time.
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
		/*
		 * serializedAST is the AST serialized in XML format and stored as a
		 * string. And we're writing this to BaseX
		 */
		String filePath = w.getLeft();
		String serializedAST = w.getRight();
		if (bPerf) {
			/* update performance data */
			Long now = this.getEpochMilli();
			if (now > tsLast)
				tsLast = now;
			totalProcessed += (new File(filePath)).length();
			Long totalMemNow = Runtime.getRuntime().totalMemory();
			if (peakMem < totalMemNow) {
				peakMem = totalMemNow;
			}
		}

		xstore.add(storePrefix + filePath, serializedAST, true);
	}

	/*
	 * this method computes speed = data_size/processing_time ;
	 * 
	 * the unit of measurement for the return value is bytes/second
	 */
	private double computeSpeed() {
		/* timespan in milliseconds */
		Long timeSpan = new Long(tsLast - tsFirst);
		double speed;
		if (timeSpan > 0) {
			speed = (totalProcessed / timeSpan) * 1000;
		} else {
			speed = 0;
		}
		return speed;
	}

	private Long getPeakMem() {
		return peakMem;
	}

	private Long getTotalProcessed() {
		return totalProcessed;
	}

	public void printPerfReport() {
		logger.info("[PERF] processing speed    " + (computeSpeed() / 1024) + " kb/s");
		logger.info("[PERF] peak memory usage   " + (getPeakMem() / (1024L * 1024L)) + " MB");
		logger.info("[PERF] time spent " + ((tsLast - tsFirst) / 1000) + " seconds");
		logger.info("[PERF] processed data size " + (getTotalProcessed() / (1024L * 1024L)) + " MB");
	}

}