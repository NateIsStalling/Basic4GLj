package com.basic4gl.debug.protocol.commands;

import com.basic4gl.debug.protocol.types.DisassembleArguments;

public class DisassembleCommand extends DebugCommand {
    public static final String COMMAND = "disassemble";

    public DisassembleArguments arguments;

    public DisassembleCommand() {
        super(COMMAND);
        arguments = new DisassembleArguments();
    }

    public DisassembleCommand(
            String memoryReference,
            Integer offset,
            Integer instructionOffset,
            int instructionCount,
            Boolean resolveSymbols) {
        super(COMMAND);
        arguments = new DisassembleArguments();
        arguments.memoryReference = memoryReference;
        arguments.offset = offset;
        arguments.instructionOffset = instructionOffset;
        arguments.instructionCount = instructionCount;
        arguments.resolveSymbols = resolveSymbols;
    }
}
