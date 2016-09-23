package com.mdetect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * Overview:
 * 
 * TimedTaskQueue is a timeout-aware task queue with timeouts
 * for each individual task.
 * 
 * There are two threads running in the background:
 * - one picks up items from the work queue and submits them for execution
 * - one gathers the completed tasks and cancels the ones that have timed out
 * 
 * There's also an executor service (backed by a thread pool) which runs the
 * tasks that are submitted to it.
 * 
 * An external thread (bgThreadGather) acts as a watchdog and takes care of
 * cancelling tasks that take too much time.
 * 
 */



/*
 * Receives the work unit type and the task worker type as a parameter types
 */
public abstract class AbstractTimedTaskQueue<W, TW extends AbstractTimedTaskWorker<W>> {
	ExecutorCompletionService service = null;
	ExecutorService pool = null;
	BlockingQueue workQueue = null;
	/*
	 * the items in this array should contain: - future object (that is used
	 * to cancel the task if it times out) - the start time of the task -
	 * expected completion time (which is exceeded, will lead to cancelling
	 * the task)
	 */
	List futures = null;

	Integer queueCapacity = null;
	Integer numConcurrentWorkers = null;

	Boolean stoppedProcessing = false;
	Thread bgThreadProcess = null;
	Thread bgThreadGather = null;
	CountDownLatch cdl = null;

	public AbstractTimedTaskQueue(Integer capacity) {
		queueCapacity = capacity;
		numConcurrentWorkers = capacity;

		pool = Executors.newFixedThreadPool(numConcurrentWorkers);
		workQueue = new LinkedBlockingQueue<W>(queueCapacity);
		service = new ExecutorCompletionService<>(pool);
		futures = Collections.synchronizedList(new ArrayList<Triple<Future<?>, Long, Long>>());
		cdl = new CountDownLatch(2);
	}

	abstract Long estimateTaskTimeout(W workUnit);

	public Long getEpochMilli() {
		return Instant.now().toEpochMilli();
	}

	public void produce(W workUnit) {
		try {
			workQueue.put(workUnit);
			System.out.println("generating " + workUnit);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * wait for all in-progress tasks to be finished and clean up task
	 * queue.
	 */
	public void close() {
		System.out.println("waiting for in-progress tasks");
		while (true) {
			/*
			 * break out of loop if the futures set is empty (all pending
			 * tasks have finished)
			 */
			synchronized (futures) {
				if (futures.isEmpty())
					break;
			}
			System.out.println("len(futures)=" + Integer.toString(futures.size()));
			synchronized (futures) {
				try {
					futures.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		System.out.println("closing");
		stoppedProcessing = true;

		try {
			/* wait for process and gather threads to stop */
			cdl.await();
		} catch (InterruptedException e) {
		}
		System.out.println("shutting down pool");

		pool.shutdownNow();
	}

	abstract TW createTaskWorker(W workUnit);

	/*
	 * this method polls the work queue for new items and submits them for
	 * processing to the executor service.
	 */
	public void process() {
		Runnable bgRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					while (!stoppedProcessing) {
						W newWorkItem = (W) workQueue.poll(100, TimeUnit.MILLISECONDS);
						if (newWorkItem != null) {
							System.out.println("picked up and submitted for execution");

							/* submit task to executor */

							TW newWorker = createTaskWorker(newWorkItem);

							Future future = service.submit(newWorker);

							/* estimate task and store in futures */
							synchronized (futures) {
								Long timeout = estimateTaskTimeout(newWorkItem);
								Long start = getEpochMilli();
								Long end = start + timeout;
								Triple<Future<?>, Long, Long> taskTriple = new Triple(future, start, end);
								futures.add(taskTriple);
								futures.notifyAll();
							}
						}
					}
				} catch (InterruptedException e) {

				} catch (Exception e) {
					e.printStackTrace();
				}
				cdl.countDown();
			}
		};
		bgThreadProcess = new Thread(bgRunnable);
		bgThreadProcess.start();
	}

	/*
	 * this thread gradually checks the executor service for completed
	 * tasks.
	 * 
	 * it also observes the tasks that have exceeded their expected
	 * completion time and cancels them.
	 */
	public void gatherAndClean() {
		Runnable bgRunnable = new Runnable() {

			/*
			 * cancels timed out tasks and keeps the futures list up-to-date
			 */
			public void cleanup() {
				Long now = getEpochMilli();
				synchronized (futures) {
					Iterator<Object> iter = futures.iterator();
					while (iter.hasNext()) {
						Triple<Future<?>, Long, Long> e = (Triple<Future<?>, Long, Long>) iter.next();
						Long expectedCompletionTime = e.get3();
						Future<?> taskFuture = e.get1();
						if (!taskFuture.isDone() && now > expectedCompletionTime) {
							/*
							 * the task is in progress but has timed out so
							 * we cancel it, collect the result, and then
							 * remove it from futures
							 */
							System.out.println(
									"cancelled task due to timeout len=" + Integer.toString(futures.size()));
							taskFuture.cancel(true);
							try {
								taskFuture.get();
							} catch (Exception e1) {
							}
							iter.remove();
							futures.notifyAll();
						} else if (taskFuture.isDone()) {
							/*
							 * the task is done, so we remove it from
							 * futures
							 */
							iter.remove();
							futures.notifyAll();
						}
					}
				}
			}

			@Override
			public void run() {
				try {
					while (!stoppedProcessing) {
						Future completedTask = service.poll(300, TimeUnit.MILLISECONDS);
						if (completedTask != null) {
							System.out.println("completed and result gathered");
							try {
								completedTask.get();
							} catch (CancellationException e) {

							} catch (Exception e) {

							}
							// completedTask.cancel(true);
						} else {
							System.out.println("didn't find any completed task");
						}
						cleanup();
					}
				} catch (InterruptedException e) {

				} catch (Exception e) {
					e.printStackTrace();
				}
				cdl.countDown();
			}
		};
		bgThreadGather = new Thread(bgRunnable);
		bgThreadGather.start();
	}
}
