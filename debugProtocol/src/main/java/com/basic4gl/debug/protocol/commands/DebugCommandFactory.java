package com.basic4gl.debug.protocol.commands;

import com.google.gson.Gson;

import java.util.Objects;

public class DebugCommandFactory {
    private final Gson gson;

    public DebugCommandFactory(Gson gson){
        this.gson = gson;
    }

    public DebugCommand FromJson(String commandJson) {
        DebugCommand command = null;

        try {
            command = gson.fromJson(commandJson, DebugCommand.class);
            if (!Objects.equals(command.type, DebugCommand.TYPE)) {
                return null;
            }

            switch (command.command) {
                case ContinueCommand.COMMAND:
                    return gson.fromJson(commandJson, DebugCommand.class);
                case EvaluateWatchCommand.COMMAND:
                    return gson.fromJson(commandJson, EvaluateWatchCommand.class);
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
                case TerminateCommand.COMMAND:
                    return gson.fromJson(commandJson, TerminateCommand.class);
                case ToggleBreakpointCommand.COMMAND:
                    return gson.fromJson(commandJson, ToggleBreakpointCommand.class);
                default:
                    return gson.fromJson(commandJson, UnsupportedCommand.class);
            }
        } catch (Exception e) {
            return new InvalidCommand(commandJson);
        }
    }
}
