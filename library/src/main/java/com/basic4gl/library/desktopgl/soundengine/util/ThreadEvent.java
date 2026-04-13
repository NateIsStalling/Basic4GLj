package com.basic4gl.library.desktopgl.soundengine.util;

/**
 * Created by Nate on 1/21/2016.
 */
public class ThreadEvent {
    private final Thread event;
    private boolean signaled;

    public ThreadEvent() {
        event = new Thread();
        signaled = false;
    }

    public void dispose() {
        synchronized (event) {
            signaled = true;
            event.notifyAll();
        }
    }

    // Member access
    public Thread getEventHandle() {
        return event;
    }

    // Methods
    public void set() {
        synchronized (event) {
            signaled = true;
            event.notifyAll();
        }
    }

    public void reset() {
        synchronized (event) {
            signaled = false;
        }
    }

    public void waitFor(long timeout) {
        long endTime = timeout > 0 ? System.currentTimeMillis() + timeout : 0;
        try {
            synchronized (event) {
                while (!signaled) {
                    if (timeout <= 0) {
                        break;
                    }
                    long remaining = endTime - System.currentTimeMillis();
                    if (remaining <= 0) {
                        break;
                    }
                    event.wait(remaining);
                }
            }
        } catch (InterruptedException consumed) {
            // Do nothing
        }
    }

    public void waitFor() {
        try {
            synchronized (event) {
                while (!signaled) {
                    event.wait();
                }
            }
        } catch (InterruptedException e) {
            // Do nothing
        }
    }

    public void pulse() {
        set();
    }
}
