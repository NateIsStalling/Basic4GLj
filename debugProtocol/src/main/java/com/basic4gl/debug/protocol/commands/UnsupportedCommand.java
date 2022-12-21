package com.basic4gl.debug.protocol.commands;

public class UnsupportedCommand extends DebugCommand{
    public UnsupportedCommand() {

    }

    public UnsupportedCommand(String command) {
        this.command = command;
    }

    @Override
    public boolean isValid() { return false; }
}
