package com.mdetect;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.tuple.Pair;

public class WriteWorkerXmlStore implements Runnable {
	public BlockingQueue<Pair<GitFileDTO, String>> q;
	public XmlStore xstore;
	
	public WriteWorkerXmlStore(XmlStore xstore,BlockingQueue<Pair<GitFileDTO, String>> q) {
		this.xstore = xstore;
		this.q = q;
	}

	@Override
	public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
            	if(q.size() >= 0) {
            		Pair<GitFileDTO, String> item = q.take();
            		xstore.addChecksum(item.getLeft(), item.getRight());
            	}
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
	}

}
