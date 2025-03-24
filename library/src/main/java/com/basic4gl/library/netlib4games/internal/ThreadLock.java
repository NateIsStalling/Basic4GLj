// TODO replace with synchronized blocks
package com.basic4gl.library.netlib4games.internal;

import static com.basic4gl.library.netlib4games.internal.ThreadUtils.INFINITE;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadLock {
    private int lockCount;
    private final ReentrantLock lock;

    public ThreadLock() {
        lockCount = (0);
        lock = new ReentrantLock();
    }

    public void dispose() {
        lock.notifyAll();
        lock.unlock();
    }

    // Member access
    public ReentrantLock getLockHandle() {
        return lock;
    }

    // Methods
    public boolean lock(long timeout) {
        boolean result = false;
        try {
            result = lock.tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
        if (result) {
            lockCount++;
        }
        return result;
    }

    public boolean lock() {
        return lock(INFINITE);
    }

    public void unlock() {
        lockCount--;
        lock.unlock();
    }
}
