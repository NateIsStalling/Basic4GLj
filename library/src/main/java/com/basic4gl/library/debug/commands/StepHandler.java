package com.basic4gl.library.debug.commands;

import com.basic4gl.lib.util.DebuggerCallbackMessage;
import com.basic4gl.runtime.TomVM;

public class StepHandler extends ContinueHandler {

    public StepHandler(DebuggerCallbackMessage message, TomVM vm) {
        super(vm, message);
    }

    // Debugging
    public void doStep(int type) {
        // TODO handle this in the editor
        //        if (mHost.isApplicationRunning())
        //            return;
        //
        // TODO handle this in the editor
        //        // Recompile program if necessary
        //        if (mHost.isApplicationStopped() && !mHost.Compile())
        //            return;

        // Patch in temp breakpoints
        switch (type) {
            case 1:
                vm.addStepBreakPoints(false);
                break; // Step over
            case 2:
                vm.addStepBreakPoints(true);
                break; // Step into
            case 3:
                if (!vm.addStepOutBreakPoint()) // Step out
                {
                    return; // (No gosub to step out of)
                }
                break;
        }

        // Resume running program
        doContinue();
        //        mHost.resumeApplication();
    }
}
