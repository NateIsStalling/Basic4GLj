package com.basic4gl.desktop.debugger.commands;

import com.basic4gl.desktop.MainWindow;
import com.basic4gl.desktop.debugger.IApplicationHost;
import com.basic4gl.runtime.TomVM;

public class StepHandler {

    private final IApplicationHost mHost;
    private final TomVM mVM;

    public StepHandler(IApplicationHost host, TomVM vm) {
        mHost = host;
        mVM = vm;
    }

    // Debugging
    public void DoStep(int type) {
        if (mHost.isApplicationRunning())
            return;

        // Recompile program if necessary
        if (mHost.isApplicationStopped() && !mHost.Compile())
            return;

        // Patch in temp breakpoints
        switch (type) {
            case 1:
                mVM.AddStepBreakPts(false);
                break;        // Step over
            case 2:
                mVM.AddStepBreakPts(true);
                break;        // Step into
            case 3:
                if (!mVM.AddStepOutBreakPt())                 // Step out
                    return;                                     // (No gosub to step out of)
                break;
        }

        // Resume running program
        mHost.resumeApplication();
    }
}
