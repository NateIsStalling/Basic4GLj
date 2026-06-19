package com.basic4gl.language.core.runtime;

/**
 * Debugger interface presented to the virtual machine itself.
 */
public abstract class IVMDebugger {

    /**
     * Breakpoint access
     */
    public abstract int getUserBreakPointCount();

    /**
     * Return the line number of a user breakpoint.
     * Note that this is the line number of the breakpoint in the main file.
     */
    public abstract int getUserBreakPointLine(int index);
}
