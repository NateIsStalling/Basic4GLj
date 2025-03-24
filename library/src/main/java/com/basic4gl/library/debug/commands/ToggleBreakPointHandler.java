package com.basic4gl.library.debug.commands;

import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;

public class ToggleBreakPointHandler {

    private final Debugger debugger;
    private final TomVM vm;

    public ToggleBreakPointHandler(Debugger debugger, TomVM vm) {
        this.debugger = debugger;
        this.vm = vm;
    }

    public boolean toggleBreakPoint(String filename, int line) {
        boolean isBreakpoint = debugger.toggleUserBreakPoint(filename, line);
        // If program is not running, breakpoints will be patched as soon as it
        // resumes or restarts.
        // If it IS running, however we must explicitly force a re-patch to ensure
        // the change is registered.
        // TODO Address potential concurrency issue

        //        if (mVM.Running()) {
        vm.repatchBreakpoints();
        //        } else {
        //            System.out.println("mVM.");
        //        }

        return isBreakpoint;
    }
}
