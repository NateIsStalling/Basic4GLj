package com.basic4gl.debug.protocol.commands;

import com.google.gson.Gson;

public class DebugCommandAdapter {
    private final Gson gson;

    public DebugCommandAdapter(Gson gson){
        this.gson = gson;
    }

    public DebugCommand FromJson(String commandJson) {
        DebugCommand command = null;

        try {
            command = gson.fromJson(commandJson, DebugCommand.class);

            switch (command.command) {
                case ContinueCommand.COMMAND:
                    return gson.fromJson(commandJson, DebugCommand.class);
                case EvaluateWatchCommand.COMMAND:
                    return gson.fromJson(commandJson, EvaluateWatchCommand.class);
                case PauseCommand.COMMAND:
                    return gson.fromJson(commandJson, PauseCommand.class);
                case ResumeCommand.COMMAND:
                    return gson.fromJson(commandJson, ResumeCommand.class);
                case StartCommand.COMMAND:
                    return gson.fromJson(commandJson, StartCommand.class);
                case StepCommand.COMMAND:
                    return gson.fromJson(commandJson, StepCommand.class);
                case StopCommand.COMMAND:
                    return gson.fromJson(commandJson, StopCommand.class);
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
