package com.basic4gl.debug.protocol.commands;

import com.basic4gl.debug.protocol.callbacks.ProtocolMessage;

// TODO make this an interface
public class DebugCommand extends ProtocolMessage {

  public static final String TYPE = "command";

  protected String command;

  public DebugCommand() {
    super(TYPE);
  }

  public DebugCommand(String command) {
    super(TYPE);
    this.command = command;
  }

  public boolean isValid() {
    return true;
  }

  public String getCommand() {
    return command;
  }
}
