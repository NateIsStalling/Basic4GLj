package com.basic4gl.library.netlib4games.internal;

import static com.basic4gl.library.netlib4games.internal.ThreadUtils.INFINITE;

public class Thread {
    private final String name;
    private java.lang.Thread thread;
    private final ThreadEvent terminateEvent;

    public Thread(String name) {
        this.name = name;
        thread = null;
        terminateEvent = new ThreadEvent("Thread.terminateEvent");
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
        Assert.assertTrue(thread == null);
        Assert.assertTrue(threaded != null);
        terminateEvent.reset();

        thread = new java.lang.Thread(threaded);

        thread.start();
    }

    public boolean isRunning() {
        synchronized (thread) {
            return thread != null;
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
        terminateEvent.set();
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
        return terminateEvent;
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
        if (thread != null) {
            synchronized (thread) {
                boolean signalled = false;
                try {
                    thread.join(timeout);
                    thread = null;
                } catch (InterruptedException e) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean waitFor() {
        return waitFor(INFINITE);
    }

    public void raisePriority() {
        synchronized (thread) {
            Assert.assertTrue(thread != null);
            // set thread priority above normal
            thread.setPriority(java.lang.Thread.NORM_PRIORITY + 1);
        }
    }
}
