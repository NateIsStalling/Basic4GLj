package com.basic4gl.library.netlib4games.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class ThreadUtils {
	private ThreadUtils() {}

	public static final long INFINITE = 0L;

	public static boolean waitForEvents(ThreadEvent[] events, int count) {
		return waitForEvents(events, count, false, INFINITE);
	}

	public static boolean waitForEvents(ThreadEvent[] events, int count, boolean all, long timeoutMillis) {

		BlockingQueue<Boolean> queue = new LinkedBlockingQueue<>(count);
		for (int i = 0; i < count; i++) {
			events[i].addBlockingQueue(queue);
		}

		// Perform wait
		try {
			Boolean result = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
			return result != null ? result : false;
		} catch (InterruptedException e) {
			return false;
		}
	}
}
