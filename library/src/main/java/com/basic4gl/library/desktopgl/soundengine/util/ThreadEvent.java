package com.basic4gl.library.desktopgl.soundengine.util;

/**
 * Created by Nate on 1/21/2016.
 */
public class ThreadEvent {
  private final Thread event;

  public ThreadEvent() {
    event = new Thread();
  }

  public void dispose() {
    synchronized (event) {
      event.notify();
    }
  }

  // Member access
  public Thread getEventHandle() {
    return event;
  }

  // Methods
  public void set() {
    synchronized (event) {
      event.notify();
    }
  }

  public void reset() {
    synchronized (event) {
      event.notify();
    }
  }

  public void waitFor(long timeout) {
    try {
      synchronized (event) {
        event.wait(timeout);
      }
    } catch (InterruptedException consumed) {
      // Do nothing
    }
  }

  public void waitFor() {
    try {
      synchronized (event) {
        event.wait();
      }
    } catch (InterruptedException e) {
      // Do nothing
    }
  }

  public void pulse() {
    event.notify();
  }
}
