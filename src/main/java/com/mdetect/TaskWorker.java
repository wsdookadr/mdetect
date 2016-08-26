package com.mdetect;

import java.util.concurrent.BlockingQueue;

public class TaskWorker<T> implements Runnable {
    private final BlockingQueue<T> workQueue;
    private int workerId = 0;

    public TaskWorker(BlockingQueue<T> workQueue, int workerId) {
        this.workQueue = workQueue;
        this.workerId = workerId;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
            	/*
            	 * Process the work item
            	 */
                T item = workQueue.take();
                System.out.println("Worker " + Integer.toString(workerId) + " finished [" + item.toString() + "]");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}