package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.internal.Assert;

/**
 * Base class for very simple error handling mechanism.
 */
public class HasErrorState extends AbstractHasErrorState {
    // Error status
    private boolean hasError;
    private String message;

    @Override
    protected void setError(String text) {
        hasError = true;
        message = text;
    }

    public HasErrorState() {
        hasError = false;
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public String getError() {
        // TODO review suspicious assert
        Assert.assertTrue(hasError());

        return message;
    }

    @Override
    public void clearError() {
        hasError = false;
    }
}
