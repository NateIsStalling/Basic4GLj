package com.basic4gl.language;

import com.basic4gl.language.core.runtime.ILineNumberMapping;
import com.basic4gl.language.core.runtime.IVMDebugger;

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

    @Override
    public void clearUserBreakPoints(String filename) {}

    @Override
    public void addUserBreakPoint(String filename, int line) {}

    @Override
    public boolean isUserBreakPoint(String filename, int line) {
        return false;
    }

    @Override
    public boolean toggleUserBreakPoint(String filename, int line) {
        return false;
    }

    @Override
    public ILineNumberMapping getLineNumberMapping() {
        return null;
    }
}
