package com.basic4gl.desktop.debugger;

import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;

public class ToggleBreakPointHandler {

    private final IApplicationHost mHost;
    private final Debugger mDebugger;
    private final TomVM mVM;

    public ToggleBreakPointHandler(IApplicationHost host, Debugger debugger, TomVM vm) {
        mHost = host;
        mDebugger = debugger;
        mVM = vm;
    }
    public boolean toggleBreakPoint(String filename, int line) {
        boolean isBreakpoint = mDebugger.ToggleUserBreakPt(filename, line);
        // If program is not running, breakpoints will be patched as soon as it
        // resumes or restarts.
        // If it IS running, however we must explicitly force a re-patch to ensure
        // the change is registered.
        //TODO Address potential concurrency issue
        if (mHost.isApplicationRunning()) {
            mVM.RepatchBreakpts();
        }

        return isBreakpoint;
    }
}
