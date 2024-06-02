package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.ThreadUtils.INFINITE;

public class Thread {
    java.lang.Thread		m_thread;
    ThreadEvent	m_terminateEvent;


    public Thread () {
       m_thread = null;
       m_terminateEvent = new ThreadEvent();
    }

    public void dispose() {
    Terminate ();
    };


    /// Start the thread. Thread does not start until this method is called.
    public void Start (Runnable threaded) {
        assert (m_thread == null);
        assert (threaded != null);
        m_terminateEvent.reset ();

        m_thread = new java.lang.Thread(threaded);

        m_thread.start();
    }

    public boolean Running () {
        synchronized (m_thread) {
            return m_thread != null;
        }
    }

    public void Terminate () {
        Terminate(true);
    }
    /// Signal to the thread to terminate.
    /// Threaded::ThreadExecute should call Terminating() to check if the thread wants to
    /// terminate
    void Terminate (boolean waitFor) {
        m_terminateEvent.set ();
        if (waitFor) {
            WaitFor ();
        }
    }

    /// Handle of the "terminating" event.
    /// (Incase calling code wants to add it as a waitobject.)
    public ThreadEvent TerminateEvent () { return m_terminateEvent; }

    /// Returns true if Terminate() has been called.
    public boolean Terminating () { return TerminateEvent ().waitFor (0); }

    /// Wait for the thread to terminate
    public boolean WaitFor (long timeout) {

        if (m_thread != null) {
            synchronized (m_thread) {
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
    public boolean WaitFor ()	{
        return WaitFor (INFINITE);
    }

    public void RaisePriority () {
        assert (m_thread != null);
        // set thread priority above normal
        m_thread.setPriority(java.lang.Thread.NORM_PRIORITY + 1);
    }
}
