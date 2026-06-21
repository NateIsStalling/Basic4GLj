package com.basic4gl.library.debug.commands;

import com.basic4gl.debug.protocol.commands.SetBreakpointsCommand;
import com.basic4gl.debug.protocol.types.Breakpoint;
import com.basic4gl.debug.protocol.types.SourceBreakpoint;
import com.basic4gl.language.core.runtime.IVMDebugger;
import com.basic4gl.language.core.runtime.VM;

public class SetBreakpointsHandler {
    private final IVMDebugger debugger;
    private final VM vm;

    public SetBreakpointsHandler(IVMDebugger debugger, VM vm) {
        this.debugger = debugger;
        this.vm = vm;
    }

    public void handle(SetBreakpointsCommand command) {
        if (command.isSourceModified()) {
            // TODO determine how to handle isSourceModified;
            //  unable to verify breakpoints of modified code without recompiling
            return;
        }

        String filename = command.getSource().path;

        debugger.clearUserBreakPoints(filename);

        for (SourceBreakpoint breakpoint : command.getBreakpoints()) {
            int line = breakpoint.line;
            Breakpoint verifiedBreakpoint = new Breakpoint();
            debugger.addUserBreakPoint(filename, line);

            verifiedBreakpoint.source = command.getSource();
            verifiedBreakpoint.line = breakpoint.line;

            // TODO decide how to handle breakpoint column;
            // currently can only handle breakpoints on the first instruction of a line
            // verifiedBreakpoint.column = breakpoint.column;

            verifiedBreakpoint.verified = debugger.isUserBreakPoint(filename, line);

            // TODO send verifiedBreakpoint in response
        }

        vm.repatchBreakpoints();
    }
}
