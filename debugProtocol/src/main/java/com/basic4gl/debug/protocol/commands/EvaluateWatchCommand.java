package com.basic4gl.debug.protocol.commands;

public class EvaluateWatchCommand extends DebugCommand {
    public static final String COMMAND = "evaluate-watch";

    public String watch;

    public boolean canCallFunc;

    public EvaluateWatchCommand() {

    }

    public EvaluateWatchCommand(
        String watch,
        boolean canCallFunc) {

        this.watch = watch;
        this.canCallFunc = canCallFunc;
    }
}
