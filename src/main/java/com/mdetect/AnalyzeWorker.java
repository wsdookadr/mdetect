package com.mdetect;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/*
 * TaskWorker derived class that takes care of
 * processing files: parsing the file.
 * 
 */
public class AnalyzeWorker extends TaskWorker<String, ParseTreeDTO> {
	public Detector d = null;
	private static final Logger logger = LoggerFactory.getLogger(AnalyzeTaskQueue.class);
	
	/*
	 * since this worker is run in its separate thread, there will be
	 * one sqlite connection per thread.
	 * https://www.sqlite.org/threadsafe.html
	 */
	private SqliteStore sq = null;
	
	public AnalyzeWorker(BlockingQueue<String> workQueue, ConcurrentLinkedQueue<ParseTreeDTO> resultQueue, int i) {
		super(workQueue, resultQueue, i);
		this.sq = new SqliteStore();
	}
	
	/*
	 * The file's checksum is looked up in the db.
	 * If it's not found it's a valid work unit, and we analyze it
	 * and return back the result, otherwise we return null since it's
	 * a known file (if it's stored in sqlite).
	 * 
	 */
	@Override
	public ParseTreeDTO workUnit(String filePath) {
		d = new Detector();
		String sha1 = Utils.gitHash(filePath);
		ParseTreeDTO result = null;
		logger.info("sha1=" + sha1 + " file="+filePath);
		if(!sq.hasChecksum(sha1)) {
			logger.info("worker " + this.workerId + " started  task " + filePath);
			Document processedDoc = d.processFile(filePath);
			logger.info("worker " + this.workerId + " finished task " + filePath);
			result = new ParseTreeDTO(processedDoc, filePath, "");
		} else {
			logger.info("known file "+filePath);
		}
		return result;
	}

}
