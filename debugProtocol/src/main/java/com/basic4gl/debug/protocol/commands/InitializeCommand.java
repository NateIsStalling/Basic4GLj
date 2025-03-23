package com.basic4gl.debug.protocol.commands;

public class InitializeCommand extends DebugCommand {
	public static final String COMMAND = "initialize";

	public InitializeCommand() {
		super(COMMAND);
	}
}
