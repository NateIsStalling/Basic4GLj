package com.basic4gl.debug.protocol.commands;

public class StepCommand extends DebugCommand {
    public static final String COMMAND = "step";

    public static final int STEP_TYPE_OVER = 1;
    public static final int STEP_TYPE_INTO = 2;
    public static final int STEP_TYPE_OUT = 3;

    public int stepType;

    public StepCommand() {
        super(COMMAND);
    }

    public StepCommand(int stepType) {
        super(COMMAND);
        this.stepType = stepType;
    }
}
