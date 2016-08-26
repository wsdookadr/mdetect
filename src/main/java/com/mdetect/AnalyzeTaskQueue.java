package com.mdetect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;

import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * 
 * 
 */

public class AnalyzeTaskQueue {
    private final BlockingQueue<String> workQueue;
    private final ExecutorService service;
    private final ConcurrentLinkedQueue<Document> resultQueue;
    /*
     * The task queue uses a blocking work queue with a maximum size
     * (if the queue has reached maximum capacity, it will block
     *  on enqueue until there is more room).
     *  
     * This class uses a pool of worker threads, the pool is of fixed
     * size, and a maximum of numActiveParallelWorkers will work on tasks
     * from the queue at any given time.
     * 
     */
    public AnalyzeTaskQueue(int numActiveParallelWorkers, int queueCapacity) {
        workQueue = new LinkedBlockingQueue<String>(queueCapacity);
        resultQueue = new ConcurrentLinkedQueue<Document>();
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
    			//System.out.println("waiting on work queue , size = " + workQueue.size());
				Thread.sleep(1000);
				System.out.println("is service term ? " + service.isTerminated());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
    	
    	System.out.println("shutdown..");
    	service.shutdown();
    	
    	System.out.println("queue size = " + workQueue.size());
    	
    	/*
    	 * TODO: improve logic by which workers notify
    	 * of their available status, so we can ensure
    	 * that we end execution and don't idle waiting
    	 * for them without cause.
    	 * 
    	 */
    	
    	try {
			service.awaitTermination(6, TimeUnit.SECONDS);
    		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	System.out.println("after await");
    	
    	
    }

}