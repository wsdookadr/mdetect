package com.mdetect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue<T> {

    private final BlockingQueue<T> workQueue;
    private final ExecutorService service;
    
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
    public TaskQueue(int numActiveParallelWorkers, int queueCapacity) {
        workQueue = new LinkedBlockingQueue<T>(queueCapacity);
        service = Executors.newFixedThreadPool(numActiveParallelWorkers);
        
        for (int i=0; i < numActiveParallelWorkers; i++) {
            service.submit(new TaskWorker<T>(workQueue, i));
        }
        System.out.println("ctor");
    }

    public void produce(T item) {
        try {
            workQueue.put(item);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void waitToComplete() {
    	while(!workQueue.isEmpty()){
    		try {
    			System.out.println("waiting...");
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	System.out.println("queue is empty, shutting down");
    	service.shutdownNow();
    }

}