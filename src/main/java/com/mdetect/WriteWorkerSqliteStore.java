package com.mdetect;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.tuple.Pair;

public class WriteWorkerSqliteStore implements Runnable {
	public int writeSize;
	public LinkedBlockingDeque<Pair<GitFileDTO, String>> q;
	public SqliteStore sq;
	
	public WriteWorkerSqliteStore(SqliteStore sq, LinkedBlockingDeque<Pair<GitFileDTO, String>> q, int writeSize) {
		this.writeSize = writeSize;
		this.q = q;
		this.sq = sq;
	}
	
	@Override
	public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
				if (q.size() > writeSize) {
					int remaining = writeSize;
					while (remaining-- > 0) {
						Pair<GitFileDTO, String> item = q.take();
						sq.addChecksum(item.getLeft(), item.getRight());
					}
					sq.commit();
				}
				Thread.sleep(10);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
	}

}
