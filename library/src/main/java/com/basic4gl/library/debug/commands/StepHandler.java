package com.basic4gl.library.debug.commands;

import com.basic4gl.lib.util.DebuggerCallbackMessage;
import com.basic4gl.runtime.TomVM;

public class StepHandler extends ContinueHandler {

    private final TomVM mVM;

    public StepHandler(DebuggerCallbackMessage message, TomVM vm) {
        super(vm, message);
        mVM = vm;
    }

    // Debugging
    public void DoStep(int type) {
        //TODO handle this in the editor
//        if (mHost.isApplicationRunning())
//            return;
//
        //TODO handle this in the editor
//        // Recompile program if necessary
//        if (mHost.isApplicationStopped() && !mHost.Compile())
//            return;

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
        Continue();
//        mHost.resumeApplication();
    }
}
