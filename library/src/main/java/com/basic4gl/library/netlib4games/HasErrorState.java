package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.internal.Assert;

/**
 * Base class for very simple error handling mechanism.
 */
public class HasErrorState extends AbstractHasErrorState {
  // Error status
  private boolean m_error;
  private String m_errorString;

  @Override
  protected void setError(String text) {
    m_error = true;
    m_errorString = text;
  }

  public HasErrorState() {
    m_error = false;
  }

  @Override
  public boolean hasError() {
    return m_error;
  }

  @Override
  public String getError() {
    // TODO review suspicious assert
    Assert.assertTrue(hasError());

    return m_errorString;
  }

  @Override
  public void clearError() {
    m_error = false;
  }
}
