package com.basic4gl.runtime;

/**
 * Error handling
 */
public class HasErrorState {
  /**
   * Error status
   */
  protected boolean hasErrorOccurred;

  protected String errorMessage;

  public HasErrorState() {
    hasErrorOccurred = false;
  }

  public String getError() {
    return hasErrorOccurred ? errorMessage : "";
  }

  public void setError(String message) {
    System.err.println(message);

    hasErrorOccurred = true;
    errorMessage = message;
  }

  public boolean hasError() {
    return hasErrorOccurred;
  }

  public void clearError() {
    hasErrorOccurred = false;
  }
}
