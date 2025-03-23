package com.basic4gl.debug.protocol.commands;

import com.google.gson.Gson;
import java.util.Objects;

public class DebugCommandFactory {
  private final Gson gson;

  public DebugCommandFactory(Gson gson) {
    this.gson = gson;
  }

  public DebugCommand fromJson(String commandJson) {
    DebugCommand command = null;

    try {
      command = gson.fromJson(commandJson, DebugCommand.class);
      if (!Objects.equals(command.getType(), DebugCommand.TYPE)) {
        return null;
      }

      switch (command.command) {
        case ConfigurationDoneCommand.COMMAND:
          return gson.fromJson(commandJson, ConfigurationDoneCommand.class);
        case ContinueCommand.COMMAND:
          return gson.fromJson(commandJson, DebugCommand.class);
        case EvaluateWatchCommand.COMMAND:
          return gson.fromJson(commandJson, EvaluateWatchCommand.class);
        case InitializeCommand.COMMAND:
          return gson.fromJson(commandJson, InitializeCommand.class);
        case PauseCommand.COMMAND:
          return gson.fromJson(commandJson, PauseCommand.class);
        case ResumeCommand.COMMAND:
          return gson.fromJson(commandJson, ResumeCommand.class);
        case StackTraceCommand.COMMAND:
          return gson.fromJson(commandJson, StackTraceCommand.class);
        case StartCommand.COMMAND:
          return gson.fromJson(commandJson, StartCommand.class);
        case StepCommand.COMMAND:
          return gson.fromJson(commandJson, StepCommand.class);
        case StopCommand.COMMAND:
          return gson.fromJson(commandJson, StopCommand.class);
        case DisconnectCommand.COMMAND:
          return gson.fromJson(commandJson, DisconnectCommand.class);
        case SetBreakpointsCommand.COMMAND:
          return gson.fromJson(commandJson, SetBreakpointsCommand.class);
        case ToggleBreakpointCommand.COMMAND:
          return gson.fromJson(commandJson, ToggleBreakpointCommand.class);
        case TerminateCommand.COMMAND:
          return gson.fromJson(commandJson, TerminateCommand.class);
        default:
          return gson.fromJson(commandJson, UnsupportedCommand.class);
      }
    } catch (Exception e) {
      return new InvalidCommand(commandJson);
    }
  }
}
