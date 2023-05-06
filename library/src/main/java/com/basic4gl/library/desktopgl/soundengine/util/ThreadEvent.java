package com.basic4gl.library.desktopgl.soundengine.util;

/**
 * Created by Nate on 1/21/2016.
 */
public class ThreadEvent {
        Thread	m_event;

    public ThreadEvent(){ this(false, false);}
    public ThreadEvent(boolean manualReset){ this(manualReset, false);}
    public ThreadEvent (boolean manualReset, boolean signalled) {
        m_event = new Thread();//
        // (null, manualReset, signalled, null); }
    }
    public void dispose ()					{ m_event.notify(); }

        // Member access
        public Thread EventHandle ()			{ return m_event; }

        // Methods
        public void Set ()						{ m_event.notify(); }
        public void Reset ()					{ m_event.notify(); }
    public void WaitFor (long timeout)	{
        try {
            m_event.wait(timeout);
        } catch (InterruptedException consumed) {
            //Do nothing
        }
    }
    public void WaitFor ()					{
        try {
            m_event.wait();
        } catch (InterruptedException e) {
            //Do nothing
        }
    }
    public void Pulse ()					{ m_event.notify(); }

}
