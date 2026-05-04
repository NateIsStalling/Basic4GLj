package com.basic4gl.debug.protocol.callbacks;

import com.basic4gl.debug.protocol.types.DisassembledInstruction;

public class DisassembleCallback extends Callback {
    public static final String COMMAND = "disassemble";

    public DisassembleCallback() {
        super(COMMAND);
    }

    private DisassembledInstruction[] instructions;

    /**
     * The list of disassembled instructions.
     */
    public DisassembledInstruction[] getInstructions() {
        return instructions;
    }

    public void setInstructions(DisassembledInstruction[] instructions) {
        this.instructions = instructions;
    }
}
