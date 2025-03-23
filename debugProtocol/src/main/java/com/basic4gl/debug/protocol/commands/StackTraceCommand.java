package com.basic4gl.debug.protocol.commands;

public class StackTraceCommand extends DebugCommand {
  public static final String COMMAND = "stackTrace";

  public StackTraceCommand() {
    super(COMMAND);
  }
}
