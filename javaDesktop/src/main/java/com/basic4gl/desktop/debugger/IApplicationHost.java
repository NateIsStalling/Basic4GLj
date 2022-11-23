package com.basic4gl.desktop.debugger;

public interface IApplicationHost {
    boolean isApplicationRunning();
    void continueApplication();
    void pushApplicationState();
    void restoreHostState();

    void resumeApplication();

    boolean isApplicationStopped();

    boolean Compile();
}
