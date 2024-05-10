package com.basic4gl.library.netlib4games;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Simple event wrapper for thread synchronsiation event
 */
public class ThreadEvent {
    final java.lang.Thread event;
    private final List<BlockingQueue<Boolean>> queue;

    private boolean isSignalled;

    public ThreadEvent() {
        event = new java.lang.Thread();
        queue = new ArrayList<>();
        isSignalled = false;
    }

    public ThreadEvent(boolean initialState) {
        this();
        isSignalled = initialState;
    }

    public void dispose() {
        synchronized (event) {
            event.notify();
        }

        notifyQueue(false);
    }

    private void notifyQueue(boolean result) {
        synchronized (queue) {
            Iterator<BlockingQueue<Boolean>> it = queue.iterator();
            while (it.hasNext()) {
                BlockingQueue<Boolean> blockingQueue = it.next();
                synchronized (blockingQueue) {
                    blockingQueue.add(result);
                }
                it.remove();
            }
        }
    }


    // Member access
    public java.lang.Thread getEventHandle() {
        return event;
    }

    // Methods
    public void set() {
        synchronized (event) {
            isSignalled = true;
            event.notify();
        }
        notifyQueue(true);
    }

    public void reset() {
        synchronized (event) {
            isSignalled = false;
            event.notify();
        }
        notifyQueue(false);
    }

    public boolean waitFor(long timeout) {
        try {
            synchronized (event) {
                if (isSignalled) {
                    return true;
                }
                event.wait(timeout);
            }
        } catch (InterruptedException consumed) {
            return false;
        }
        synchronized (event) {
            return isSignalled;
        }
    }

    public boolean waitFor() {
        try {
            synchronized (event) {
                if (isSignalled) {
                    return true;
                }
                event.wait();
            }
        } catch (InterruptedException e) {
            return false;
        }
        synchronized (event) {
            return isSignalled;
        }
    }

    public void pulse() {
        event.notify();
        notifyQueue(true);
    }

    public void addBlockingQueue(BlockingQueue<Boolean> queue) {
        synchronized (event) {
            if (isSignalled) {
                queue.add(true);
                return;
            }
        }
        synchronized (this.queue) {
            this.queue.add(queue);
        }
    }
}
