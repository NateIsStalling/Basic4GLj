package com.basic4gl.debug.protocol.commands;

public class TerminateCommand extends DebugCommand {
  public static final String COMMAND = "terminate";

  public TerminateCommand() {
    super(COMMAND);
  }
}
