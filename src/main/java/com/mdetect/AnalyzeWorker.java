package com.mdetect;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.w3c.dom.Document;

/*
 * TaskWorker derived class that takes care of
 * processing files: parsing the file.
 * 
 */
public class AnalyzeWorker extends TaskWorker<String, ParseTreeDTO> {
	public Detector d = null;
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
		System.out.println("sha1=" + sha1 + " file="+filePath);
		if(!sq.hasChecksum(sha1)) {
			Document processedDoc = d.processFile(filePath);
			System.out.println("worker " + this.workerId + " finished task " + filePath);
			result = new ParseTreeDTO(processedDoc, filePath, "");
		} else {
			System.out.println("[DBG] known file "+filePath);
		}
		return result;
	}

}
