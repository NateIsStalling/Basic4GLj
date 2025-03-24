package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.internal.ThreadLock;

/**
 * Threadsafe version of HasErrorState
 */
public class HasErrorStateThreadSafe extends HasErrorState {
    /**
     * Lock object
     */
    private final ThreadLock threadLock;

    /**
     * True if we own it (and will free it on destruction)
     */
    private final boolean ownLock;

    public HasErrorStateThreadSafe() {

        // Create own threadlock
        threadLock = new ThreadLock();
        ownLock = true;
    }

    public void dispose() {
        if (ownLock) {
            threadLock.dispose();
        }
    }

    /**
     * Note: Locking is automatic around the standard HasErrorState methods, however
     * calling code may want to explicitly lock around a set of calls to make them
     * transactional, e.g:
     * <p>
     * o.lockError ();
     * if (o.hasError ()) {
     * doSomethingWith (o.getError());
     * o.clearError ();
     * }
     * o.unlockError ();
     * <p>
     * This would ensure that another thread didn't clear the error between the test
     * and reading the string with getError.
     */
    void lockError() {
        threadLock.lock();
    }

    void unlockError() {
        threadLock.unlock();
    }

    // HasErrorState methods
    public void setError(String text) {
        lockError();
        super.setError(text);
        unlockError();
    }

    public boolean hasError() {
        boolean result = false;
        lockError();
        result = super.hasError();
        unlockError();
        return result;
    }

    public String getError() {
        String result = null;
        lockError();
        result = super.getError();
        unlockError();
        return result;
    }

    public void clearError() {
        lockError();
        super.clearError();
        unlockError();
    }
}
