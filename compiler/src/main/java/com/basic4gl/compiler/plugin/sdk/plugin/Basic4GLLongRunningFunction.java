package com.basic4gl.compiler.plugin.sdk.plugin;

/**
 * Interface to a BASIC function that suspends Basic4GL execution until it completes.
 * See Basic4GLRuntime.beginLongRunningFn() for more information.
 */
public interface Basic4GLLongRunningFunction
{
    void dispose();

    /**
     * Return true to have Basic4GL periodically call "poll()"
     * @return
     */
    boolean isPolled();

    /**
     * Return true to have Basic4GL automatically delete this object after it resumes (or is cancelled).
     * IMPORTANT: If this returns true, the object should consider itself invalid as soon as it calls
     * Basic4GLRuntime.resume().
     * @return
     */
    boolean deleteWhenDone();

    /**
     * Periodically called by Basic4GL if "IsPolled()" returns true
     */
    void poll();

    /**
     * Called by Basic4GL if execution is cancelled,  e.g. BASIC program has been stopped/restarted
     */
    void cancel();
}