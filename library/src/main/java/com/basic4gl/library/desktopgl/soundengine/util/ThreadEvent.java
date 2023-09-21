package com.basic4gl.library.desktopgl.soundengine.util;

/**
 * Created by Nate on 1/21/2016.
 */
public class ThreadEvent {
    Thread event;
    public ThreadEvent() {
        event = new Thread();
    }

    public void dispose() {
        event.notify();
    }

    // Member access
    public Thread getEventHandle() {
        return event;
    }

    // Methods
    public void set() {
        event.notify();
    }

    public void reset() {
        event.notify();
    }

    public void waitFor(long timeout) {
        try {
            event.wait(timeout);
        } catch (InterruptedException consumed) {
            //Do nothing
        }
    }

    public void waitFor() {
        try {
            event.wait();
        } catch (InterruptedException e) {
            //Do nothing
        }
    }

    public void pulse() {
        event.notify();
    }
}
