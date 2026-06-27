package com.basic4gl.library.debug.commands;

import com.basic4gl.language.core.runtime.IVMDebugger;
import com.basic4gl.language.core.runtime.VM;

public class ToggleBreakPointHandler {

    private final IVMDebugger debugger;
    private final VM vm;

    public ToggleBreakPointHandler(IVMDebugger debugger, VM vm) {
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
