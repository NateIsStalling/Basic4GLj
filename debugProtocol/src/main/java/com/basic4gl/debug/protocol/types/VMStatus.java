package com.basic4gl.debug.protocol.types;

public class VMStatus {
    //TODO replace with exited event
    public boolean done;

    public boolean hasError;

    public String error;

    public VMStatus() {

    }

    public VMStatus(boolean done, boolean hasError, String error) {
        this.done = done;
        this.hasError = hasError;
        this.error = error;
    }

    public static VMStatus copy(VMStatus vmStatus) {
        if (vmStatus == null) {
            return null;
        }

        return new VMStatus(
            vmStatus.done,
            vmStatus.hasError,
            vmStatus.error);
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
