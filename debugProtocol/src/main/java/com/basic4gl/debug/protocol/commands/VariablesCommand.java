package com.basic4gl.debug.protocol.commands;

import com.basic4gl.debug.protocol.types.VariablesArguments;

public class VariablesCommand extends DebugCommand {
    public static final String COMMAND = "variables";

    // VM-specific references used by the adapter to expose debugger views via DAP variables.
    public static final int REF_GLOBALS = 1;
    public static final int REF_REGISTERS = 2;
    public static final int REF_HEAP = 3;
    public static final int REF_STACK = 4;
    public static final int REF_TEMP = 5;
    public static final int REF_ALLOCATED_STRINGS = 6;

    public VariablesArguments arguments;

    public VariablesCommand() {
        super(COMMAND);
        arguments = new VariablesArguments();
        arguments.variablesReference = REF_GLOBALS;
    }

    public VariablesCommand(int variablesReference, Integer start, Integer count) {
        super(COMMAND);
        arguments = new VariablesArguments();
        arguments.variablesReference = variablesReference;
        arguments.start = start;
        arguments.count = count;
    }
}
