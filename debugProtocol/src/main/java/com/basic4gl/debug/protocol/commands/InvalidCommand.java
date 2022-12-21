package com.basic4gl.debug.protocol.commands;

public class InvalidCommand extends DebugCommand {
    public InvalidCommand(String command) {
        this.command = command;
    }

    @Override
    public boolean isValid() { return false; }
}
