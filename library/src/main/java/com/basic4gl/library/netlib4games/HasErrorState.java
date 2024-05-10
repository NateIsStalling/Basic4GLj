package com.basic4gl.library.netlib4games;

/**
 * Base class for very simple error handling mechanism.
 */
public class HasErrorState extends AbstractHasErrorState {
    // Error status
    boolean                        m_error;
    String                 m_errorString;

    @Override
    protected void setError (String text) {
        m_error = true;
        m_errorString = text;
    }

    public HasErrorState ()  {
        m_error = false;
     }

     @Override
    public boolean error () {
        return m_error;
    }
    @Override
    public String getError () {
        // TODO suspicious assert
        assert(error());

        return m_errorString;
    }
    @Override
    public void clearError () {
        m_error = false;
    }
}
