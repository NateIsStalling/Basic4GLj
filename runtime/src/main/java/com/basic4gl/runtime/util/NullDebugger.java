package com.basic4gl.runtime.util;

/**
 * A null object implementation for IVMDebugger, used when debugging is disabled
 * or not available.
 */
public class NullDebugger extends IVMDebugger {

    @Override
    public int getUserBreakPointCount() {
        return 0;
    }

    @Override
    public int getUserBreakPointLine(int index) {
        return 0;
    }
}
