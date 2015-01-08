package com.basic4gl.vm.util;

/// Debugger interface presented to the virtual machine itself.
public abstract class IVMDebugger {

	// Breakpoint access
	public abstract int UserBreakPtCount();

	// / Return the line number of a user breakpoint.
	// / Note that this is the line number of the breakpoint in the main file.
	public abstract int UserBreakPtLine(int index);
}