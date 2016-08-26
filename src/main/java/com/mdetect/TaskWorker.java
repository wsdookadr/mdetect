package com.mdetect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskWorker<I, O> implements Runnable {
    private final BlockingQueue<I> workQueue;
    private final ConcurrentLinkedQueue<O> resultQueue;
    protected int workerId = 0;
    public TaskWorker(BlockingQueue<I> workQueue, ConcurrentLinkedQueue<O> resultQueue, int i) {
        this.workQueue = workQueue;
        this.resultQueue = resultQueue;
        this.workerId = i;
    }
    
    
    
    /*
     * The workUnit is meant to be overriden by derived classes and 
     * does the computations required to process the work item.
     * 
     */
    public O workUnit(I item) {
		return null;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
            	/*
            	 * Process the work item and push result onto
            	 * the result queue.
            	 */
                I item = workQueue.take();
                O result = workUnit(item);
                resultQueue.add(result);
                System.out.println("worker " + Integer.toString(workerId) + " finished [" + item.toString() + "]");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
    }
}