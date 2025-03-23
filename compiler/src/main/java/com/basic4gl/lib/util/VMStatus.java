package com.basic4gl.lib.util;

import java.util.Objects;

public class VMStatus {
  protected boolean done;
  protected boolean hasError;
  protected String error;

  public VMStatus() {}

  public VMStatus(boolean done, boolean hasError, String error) {
    this.done = done;
    this.hasError = hasError;
    this.error = error;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VMStatus vmStatus = (VMStatus) o;
    return done == vmStatus.done
        && hasError == vmStatus.hasError
        && Objects.equals(error, vmStatus.error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(done, hasError, error);
  }

  public boolean isDone() {
    return done;
  }

  public boolean hasError() {
    return hasError;
  }

  public String getError() {
    return error;
  }
}
