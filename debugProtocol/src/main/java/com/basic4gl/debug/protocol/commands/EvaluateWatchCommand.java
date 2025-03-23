package com.basic4gl.debug.protocol.commands;

public class EvaluateWatchCommand extends DebugCommand {
  public static final String COMMAND = "evaluate-watch";

  public static final String EVALUATE_CONTEXT_WATCH = "watch";
  public static final String EVALUATE_CONTEXT_REPL = "repl";
  public static final String EVALUATE_CONTEXT_HOVER = "hover";
  public static final String EVALUATE_CONTEXT_CLIPBOARD = "clipboard";
  public static final String EVALUATE_CONTEXT_VARIABLES = "variables";

  public String watch;

  public String context;

  public EvaluateWatchCommand() {
    super(COMMAND);
  }

  public EvaluateWatchCommand(String watch, String context) {
    super(COMMAND);

    this.watch = watch;
    this.context = context;
  }
}
