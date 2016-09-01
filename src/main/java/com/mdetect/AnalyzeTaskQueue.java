package com.mdetect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;

import java.util.concurrent.ConcurrentLinkedQueue;


public class AnalyzeTaskQueue {
    private final BlockingQueue<String> workQueue;
    private final ExecutorService service;
    private final XmlStore xstore;
    private String storePrefix;
    private int HARD_TIMEOUT = 120;
    public final ConcurrentLinkedQueue<ParseTreeDTO> resultQueue;
    /*
     * The task queue uses a blocking work queue with a maximum size
     * (if the queue has reached maximum capacity, it will block
     *  on enqueue until there is more room).
     *  
     * This class uses a pool of worker threads, the pool is of fixed
     * size, and a maximum of numActiveParallelWorkers will work on tasks
     * from the queue at any given time.
     * 
     * The analysis (parsing) is done in parallel, and the writing
     * to the datastore happens sequentially.
     */
    public AnalyzeTaskQueue(int numActiveParallelWorkers, int queueCapacity, XmlStore xstore, String storePrefix) {
    	this.xstore = xstore;
    	this.storePrefix = storePrefix;
        workQueue = new LinkedBlockingQueue<String>(queueCapacity);
        resultQueue = new ConcurrentLinkedQueue<ParseTreeDTO>();
        service = Executors.newFixedThreadPool(numActiveParallelWorkers);
        //service = Executors.newCachedThreadPool();
        
        for (int i=0; i < numActiveParallelWorkers; i++) {
        	service.submit(new AnalyzeWorker(workQueue, resultQueue,i));
        }
        System.out.println("ctor");
    }

    public void produce(String item) {
        try {
            workQueue.put(item);
            //System.out.println("produced, now queue size = " + workQueue.size());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /*
     * 
     * Clean up work queue and await for executor to finish up.
     * 
     */
    public void shutdown() {
    	while(!workQueue.isEmpty()) {
    		try {
				Thread.sleep(1000);
				System.out.println("is service term ? " + service.isTerminated());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	System.out.println("shutdown..");
    	service.shutdown();

    	try {
			service.awaitTermination(HARD_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	System.out.println("[DBG] after worker shutdown");
    	
		/*
		 * store the resutls that were computed after all the work units were
		 * sent out, and the executor was shut down (those were not drained in
		 * the loop above because they were still processing after that loop
		 * finished, so we collect the remaining ones below)
		 */
		storePartialResultsInXMLStore();
    }
    
    /*
     * Drain all processed results into a list and return it.
     */
    private ArrayList<ParseTreeDTO > retrieveResults() {
    	ArrayList<ParseTreeDTO > retrieved = new ArrayList<ParseTreeDTO >();
    	while(!resultQueue.isEmpty()) {
    		retrieved.add(resultQueue.remove());
    	}
    	return retrieved;
    }
    
    /*
     * Retrieve partial results and store them in xml database.
     */
    public void storePartialResultsInXMLStore() {
		ArrayList<ParseTreeDTO > retrieved = retrieveResults();
		if(retrieved.size() > 0 ){
			for(ParseTreeDTO p: retrieved) {
				String contentsToInsert = "";
				try {
					contentsToInsert = Utils.serializeDOMDocument(p.getD());
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					xstore.add(storePrefix + p.getFilePath(), contentsToInsert, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
    }
    

}