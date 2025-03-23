package com.basic4gl.debug.protocol.commands;

public class ConfigurationDoneCommand extends DebugCommand {
	public static final String COMMAND = "configuration-done";

	public ConfigurationDoneCommand() {
		super(COMMAND);
	}
}
