package com.basic4gl.debug.protocol.commands;

public class DisconnectCommand extends DebugCommand {
    public static final String COMMAND = "disconnect";

    public DisconnectCommand() {
        super(COMMAND);
    }
}
