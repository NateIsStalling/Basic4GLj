package com.basic4gl.library.netlib4games;

/**
 * Threadsafe version of HasErrorState
 */
public class HasErrorStateThreadSafe extends HasErrorState {
    ThreadLock m_lock;			// Lock object.
    boolean		m_ownLock;		// True if we own it (and will free it on destruction)

    public HasErrorStateThreadSafe () {

        // Create own threadlock
        m_lock		= new ThreadLock ();
        m_ownLock	= true;
    }

    public void dispose() {
        if (m_ownLock) {
            m_lock.dispose();
        }
    }

    // Note: Locking is automatic around the standard HasErrorState methods, however
    // calling code may want to explicitly lock around a set of calls to make them
    // transactional, e.g:
    //
    //		o.LockError ();
    //			if (o.Error ()) {
    //				DoSomethingWith (o.GetError ());
    //				o.ClearError ();
    //			}
    //		o.UnlockError ();
    //
    // This would ensure that another thread didn't clear the error between the test
    // and reading the string with ::GetError.
    //
    void LockError ()	{ m_lock.Lock (); }
    void UnlockError () { m_lock.Unlock (); }

    // HasErrorState methods
    public void setError (String text) {
        LockError();
        super.setError(text);
        UnlockError();
    }
    public boolean error () {
        boolean result = false;
        LockError();
        result = super.error();
        UnlockError();
        return result;
    }
    public String getError () {
        String result = null;
        LockError();
        result = super.getError();
        UnlockError();
        return result;
    }
    public void clearError () {
        LockError();
        super.clearError();
        UnlockError();
    }
}
