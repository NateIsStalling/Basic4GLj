package com.basic4gl.debug.protocol.commands;

// TODO make this an interface
public class DebugCommand {

    protected String command;

    public DebugCommand(String command) {
        this.command = command;
    }

    public boolean isValid() { return true; }

    public String getCommand() {
        return command;
    }
}
