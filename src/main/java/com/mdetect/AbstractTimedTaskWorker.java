package com.mdetect;

import java.util.concurrent.Callable;

public abstract class AbstractTimedTaskWorker<W> implements Callable {
	W workUnit = null;

	public AbstractTimedTaskWorker(W workUnit) {
		this.workUnit = workUnit;
	}
}