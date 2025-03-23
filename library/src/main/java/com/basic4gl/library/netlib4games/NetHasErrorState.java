package com.basic4gl.library.netlib4games;

/**
 * Implementation of HasErrorState with automatic logging
 */
public class NetHasErrorState extends HasErrorState {

    /**
     * Set network error state.
     * Also automatically logs the error (if logging is enabled.)
     */
    @Override
    protected void setError(String text) {
        super.setError(text);
    }
}
