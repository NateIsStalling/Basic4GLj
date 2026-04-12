package com.basic4gl.desktop.debugger;

import com.basic4gl.lib.util.Library;
import java.util.List;

public interface IDebugger {
    void beginSessionConfiguration();

    void commitSessionConfiguration();

    void continueApplication();

    void pauseApplication();

    void resumeApplication();

    void runApplication(Library builder, String currentDirectory, String libraryPath);

    void stopApplication();

    void step(int type);

    void terminateApplication();

    boolean setBreakpoints(String filename, List<Integer> breakpoints);

    boolean toggleBreakpoint(String filename, int line);

    int evaluateWatch(String watch, boolean canCallFunc);

    void refreshCallStack();

    void refreshDisassembly();

    int requestDisassembly(Integer instructionOffset, int instructionCount);

    void refreshVariables();

    int requestVariables(int variablesReference, Integer start, Integer count);
}
