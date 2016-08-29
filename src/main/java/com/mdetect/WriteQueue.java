package com.mdetect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;

/*
 * This class implements a consumer-producer demux mechanism
 * that facilitates writing to multiple data stores at the 
 * same time (with one thread for each).
 */
public class WriteQueue  {
	public LinkedBlockingDeque<Pair<GitFileDTO, String>> qSqlStore = null, qXmlStore = null;
    private ExecutorService sqliteStoreService=null,xmlStoreService=null;
    
    
    public final XmlStore 	  xstore;
    public final SqliteStore sq;
    
	public int maxQueueSize = 1000;
	public int writeSize = 500;
	
	public WriteQueue(XmlStore xstore, SqliteStore sq) {
		qSqlStore = new LinkedBlockingDeque<Pair<GitFileDTO, String>>(maxQueueSize);
		qXmlStore = new LinkedBlockingDeque<Pair<GitFileDTO, String>>(maxQueueSize);
		
		this.xmlStoreService = Executors.newSingleThreadExecutor();
		this.sqliteStoreService = Executors.newSingleThreadExecutor();
		
		this.xmlStoreService.submit(new WriteWorkerXmlStore(xstore, qXmlStore));
		this.sqliteStoreService.submit(new WriteWorkerSqliteStore(sq, qSqlStore, writeSize));
		
		this.xstore = xstore;
		this.sq = sq;
		
		
	}
	
    public void produce(Pair<GitFileDTO, String> item) {
        try {
        	qSqlStore.put(item);
			//qXmlStore.put(item);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void shutdown() {
    	xmlStoreService.shutdown();
    	sqliteStoreService.shutdown();
    	
    	try {
			xmlStoreService.awaitTermination(5, TimeUnit.SECONDS);
			sqliteStoreService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	/* 
    	 * drain the remaining elements 
    	 * (since the worker only writes in complete batches)
    	 *
    	 */
    	if(qSqlStore.size() > 0) {
    		while(qSqlStore.size() > 0) {
    			try {
					Pair<GitFileDTO, String> item = qSqlStore.take();
					sq.addChecksum(item.getLeft(), item.getRight());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    	
    }
}