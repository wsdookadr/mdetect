package com.mdetect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Pair;

public class WriteWorkerSqliteStore implements Runnable {
	public int writeSize;
	public BlockingQueue<Pair<GitFileDTO, String>> q;
	public SqliteStore sq;
	
	public WriteWorkerSqliteStore(SqliteStore sq, BlockingQueue<Pair<GitFileDTO, String>> q, int writeSize) {
		this.writeSize = writeSize;
		this.q = q;
		this.sq = sq;
	}
	
	@Override
	public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
            	if(q.size() >= writeSize) {
            		int retrieved = writeSize;
            		while(retrieved > 0) {
            			retrieved--;
            			Pair<GitFileDTO, String> item = q.take();
            			sq.addChecksum(item.getLeft(), item.getRight());
            		}
            		sq.commit();
            	}
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
	}

}
