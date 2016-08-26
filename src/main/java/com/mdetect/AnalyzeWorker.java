package com.mdetect;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.w3c.dom.Document;

public class AnalyzeWorker extends TaskWorker<String, Document> {
	public Detector d = null;
	
	public AnalyzeWorker(BlockingQueue<String> workQueue, ConcurrentLinkedQueue<Document> resultQueue, int i) {
		super(workQueue, resultQueue, i);
	}

	@Override
	public Document workUnit(String filePath) {
		d = new Detector();
		d.processFile(filePath);
		Document w = d.domDoc;
		System.out.println("worker " + this.workerId + " finished task " + filePath);
		return w;
	}
	
	

}
