package com.basic4gl.library.netlib4games.internal;

import static com.basic4gl.library.netlib4games.internal.ThreadUtils.INFINITE;

public class Thread {
    private final Object lock = new Object();

    private String name;
    private java.lang.Thread m_thread;
    private final ThreadEvent m_terminateEvent;

    public Thread(String name) {
        this.name = name;
        m_thread = null;
        m_terminateEvent = new ThreadEvent("Thread.m_terminateEvent");
    }

    public void dispose() {
        terminate();
    }


    /**
     * Start the thread. Thread does not start until this method is called.
     *
     * @param threaded runnable
     */
    public void start(Runnable threaded) {
        Assert.assertTrue(m_thread == null);
        Assert.assertTrue(threaded != null);
        m_terminateEvent.reset();

        m_thread = new java.lang.Thread(threaded);

        m_thread.start();
    }

    public boolean isRunning() {
        synchronized (lock) {
            return m_thread != null;
        }
    }

    public void terminate() {
        terminate(true);
    }

    /**
     * Signal to the thread to terminate.
     * Threaded::ThreadExecute should call Terminating() to check if the thread wants to
     * terminate
     *
     * @param waitFor true if thread should wait until completed signal is set
     */
    void terminate(boolean waitFor) {
        m_terminateEvent.set();
        if (waitFor) {
            waitFor();
        }
    }

    /**
     * Handle of the "terminating" event.
     * (Incase calling code wants to add it as a waitobject.)
     *
     * @return
     */
    public ThreadEvent getTerminateEvent() {
        return m_terminateEvent;
    }

    /**
     * @return Returns true if terminate() has been called.
     */
    public boolean isTerminating() {
        return getTerminateEvent().waitFor(1);
    }

    /**
     * Wait for the thread to terminate
     *
     * @param timeout
     * @return true if no InterruptedException occurred
     */
    public boolean waitFor(long timeout) {
        if (m_thread != null) {
            synchronized (lock) {
                boolean signalled = false;
                try {
                    m_thread.wait(timeout);
                } catch (InterruptedException e) {
                    return false;
                }
                m_thread = null;
            }
        }
        return true;
    }

    public boolean waitFor() {
        return waitFor(INFINITE);
    }

    public void raisePriority() {
        Assert.assertTrue(m_thread != null);
        // set thread priority above normal
        m_thread.setPriority(java.lang.Thread.NORM_PRIORITY + 1);
    }
}
