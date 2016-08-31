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
	
	public AnalyzeWorker(BlockingQueue<String> workQueue, ConcurrentLinkedQueue<ParseTreeDTO> resultQueue, int i) {
		super(workQueue, resultQueue, i);
	}

	@Override
	public ParseTreeDTO workUnit(String filePath) {
		d = new Detector();
		Document processedDoc = d.processFile(filePath);
		System.out.println("worker " + this.workerId + " finished task " + filePath);
		ParseTreeDTO result = new ParseTreeDTO(processedDoc, filePath, "");
		return result;
	}

}
