package com.basic4gl.runtime;

import com.basic4gl.runtime.types.OpCode;

public class PatchedBreakPt {

    private int offset;

    private short replacedOpCode;

    public PatchedBreakPt() {
        offset = 0xffff;
        replacedOpCode = OpCode.OP_NOP;
    }

    /**
     * Op-Code offset in program
     */
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * For active breakpoints:
     * The op-code that has been replaced with the OP_BREAKPT.
     */
    public short getReplacedOpCode() {
        return replacedOpCode;
    }

    public void setReplacedOpCode(short replacedOpCode) {
        this.replacedOpCode = replacedOpCode;
    }
}
