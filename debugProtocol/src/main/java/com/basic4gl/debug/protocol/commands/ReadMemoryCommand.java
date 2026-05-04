package com.basic4gl.debug.protocol.commands;

import com.basic4gl.debug.protocol.types.ReadMemoryArguments;

public class ReadMemoryCommand extends DebugCommand {
    public static final String COMMAND = "readMemory";

    public ReadMemoryArguments arguments;

    public ReadMemoryCommand() {
        super(COMMAND);
        arguments = new ReadMemoryArguments();
    }

    public ReadMemoryCommand(String memoryReference, Integer offset, int count) {
        super(COMMAND);
        arguments = new ReadMemoryArguments();
        arguments.memoryReference = memoryReference;
        arguments.offset = offset;
        arguments.count = count;
    }
}

