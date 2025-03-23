// TODO replace with synchronized blocks
package com.basic4gl.library.netlib4games.internal;

import static com.basic4gl.library.netlib4games.internal.ThreadUtils.INFINITE;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadLock {
private int m_lockCount;
private final ReentrantLock m_lock;

public ThreadLock() {
	m_lockCount = (0);
	m_lock = new ReentrantLock();
}

public void dispose() {
	m_lock.notifyAll();
	m_lock.unlock();
}

// Member access
public ReentrantLock getLockHandle() {
	return m_lock;
}

// Methods
public boolean lock(long timeout) {
	boolean result = false;
	try {
	result = m_lock.tryLock(timeout, TimeUnit.MILLISECONDS);
	} catch (InterruptedException e) {
	return false;
	}
	if (result) {
	m_lockCount++;
	}
	return result;
}

public boolean lock() {
	return lock(INFINITE);
}

public void unlock() {
	m_lockCount--;
	m_lock.unlock();
}
}
