package com.basic4gl.debug.protocol.commands;

public class ToggleBreakpointCommand extends DebugCommand {
    public static final String COMMAND = "toggle-breakpoint";

    public String filename;
    public int line;

    public ToggleBreakpointCommand(String filename, int line) {
        super(COMMAND);
        this.filename = filename;
        this.line = line;
    }
}
