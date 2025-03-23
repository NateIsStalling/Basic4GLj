package com.basic4gl.library.debug.commands;

import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;

public class ToggleBreakPointHandler {

  private final Debugger mDebugger;
  private final TomVM mVM;

  public ToggleBreakPointHandler(Debugger debugger, TomVM vm) {
    mDebugger = debugger;
    mVM = vm;
  }

  public boolean toggleBreakPoint(String filename, int line) {
    boolean isBreakpoint = mDebugger.ToggleUserBreakPt(filename, line);
    // If program is not running, breakpoints will be patched as soon as it
    // resumes or restarts.
    // If it IS running, however we must explicitly force a re-patch to ensure
    // the change is registered.
    // TODO Address potential concurrency issue

    //        if (mVM.Running()) {
    mVM.repatchBreakpoints();
    //        } else {
    //            System.out.println("mVM.");
    //        }

    return isBreakpoint;
  }
}
