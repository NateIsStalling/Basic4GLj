package com.basic4gl.library.desktopgl.soundengine.util;

/**
 * Created by Nate on 1/21/2016.
 */
public class ThreadEvent {
    Thread m_event;

    public ThreadEvent() {
        this(false, false);
    }

    public ThreadEvent(boolean manualReset) {
        this(manualReset, false);
    }

    public ThreadEvent(boolean manualReset, boolean signalled) {
        m_event = new Thread();//
        // (null, manualReset, signalled, null); }
    }

    public void dispose() {
        m_event.notify();
    }

    // Member access
    public Thread getEventHandle() {
        return m_event;
    }

    // Methods
    public void set() {
        m_event.notify();
    }

    public void reset() {
        m_event.notify();
    }

    public void waitFor(long timeout) {
        try {
            m_event.wait(timeout);
        } catch (InterruptedException consumed) {
            //Do nothing
        }
    }

    public void waitFor() {
        try {
            m_event.wait();
        } catch (InterruptedException e) {
            //Do nothing
        }
    }

    public void pulse() {
        m_event.notify();
    }
}
