package com.basic4gl.library.netlib4games;

/**
 * Any network object that needs to be refreshed periodically.
 */
public interface NetRefreshed {
    /**
     * Refresh internal state
     */
    void refresh();
}
