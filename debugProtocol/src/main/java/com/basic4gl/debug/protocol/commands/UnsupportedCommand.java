package com.basic4gl.debug.protocol.commands;

public class UnsupportedCommand extends DebugCommand {
	public UnsupportedCommand() {
		super(null);
	}

	public UnsupportedCommand(String command) {
		super(command);
	}

	@Override
	public boolean isValid() {
		return false;
	}
}
