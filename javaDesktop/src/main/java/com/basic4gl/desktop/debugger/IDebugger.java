package com.basic4gl.desktop.debugger;

import com.basic4gl.lib.util.Library;

public interface IDebugger {
    void continueApplication();
    void pauseApplication();
    void resumeApplication();
    void runApplication(Library builder, String currentDirectory, String libraryPath);
    void stopApplication();

    void step(int type);
    boolean toggleBreakpoint(String filename, int line);
    int evaluateWatch(String watch, boolean canCallFunc);

    void refreshCallStack();
}
