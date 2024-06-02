//TODO replace with synchronized blocks
package com.basic4gl.library.netlib4games;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.basic4gl.library.netlib4games.ThreadUtils.INFINITE;

public class ThreadLock {
    int		m_lockCount;
    ReentrantLock m_lock;

    public ThreadLock () {
        m_lockCount = (0);
        m_lock = new ReentrantLock ();
    }
    public void dispose ()					{
        m_lock.notifyAll();
        m_lock.unlock();
    }

    // Member access
    public ReentrantLock LockHandle ()			{ return m_lock; }

    // Methods
    public boolean Lock (long timeout)		{
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
    public boolean Lock ()					{
        return Lock (INFINITE);
    }
    public void Unlock ()					{
        m_lockCount--;
        m_lock.unlock();
    }
}
