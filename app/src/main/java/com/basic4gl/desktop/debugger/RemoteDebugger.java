package com.basic4gl.desktop.debugger;

import com.basic4gl.debug.protocol.commands.*;
import com.basic4gl.debug.protocol.types.Source;
import com.basic4gl.debug.protocol.types.SourceBreakpoint;
import com.basic4gl.lib.util.Library;
import java.util.ArrayList;
import java.util.List;

public class RemoteDebugger implements IDebugger {
  private DebugClientAdapter adapter;

  public RemoteDebugger(DebugClientAdapter adapter) {
    this.adapter = adapter;
  }

  @Override
  public void beginSessionConfiguration() {
    DebugCommand command = new InitializeCommand();
    adapter.message(command);
  }

  @Override
  public void commitSessionConfiguration() {
    DebugCommand command = new ConfigurationDoneCommand();
    adapter.message(command);
  }

  @Override
  public void continueApplication() {
    DebugCommand command = new ContinueCommand();
    adapter.message(command);
  }

  @Override
  public void pauseApplication() {
    DebugCommand command = new PauseCommand();
    adapter.message(command);
  }

  @Override
  public void resumeApplication() {
    DebugCommand command = new ResumeCommand();
    adapter.message(command);
  }

  @Override
  public void runApplication(Library builder, String currentDirectory, String libraryPath) {}

  @Override
  public void stopApplication() {
    DebugCommand command = new StopCommand();
    adapter.message(command);
  }

  @Override
  public void step(int type) {
    DebugCommand command = new StepCommand(type);
    adapter.message(command);
  }

  @Override
  public void terminateApplication() {
    DebugCommand command = new TerminateCommand();
    adapter.message(command);
  }

  @Override
  public boolean setBreakpoints(String filename, List<Integer> breakpoints) {
    SetBreakpointsCommand command = new SetBreakpointsCommand();
    Source source = new Source();
    ArrayList<SourceBreakpoint> sourceBreakpoints = new ArrayList<>();

    source.path = filename;
    source.name = filename;

    for (int line : breakpoints) {
      SourceBreakpoint s = new SourceBreakpoint();

      s.line = line;
      s.column = 0;

      sourceBreakpoints.add(s);
    }

    command.setSource(source);
    command.setBreakpoints(sourceBreakpoints);

    adapter.message(command);
    return false;
  }

  @Override
  public boolean toggleBreakpoint(String filename, int line) {
    DebugCommand command = new ToggleBreakpointCommand(filename, line);
    adapter.message(command);
    return false;
  }

  @Override
  public int evaluateWatch(String watch, boolean canCallFunc) {
    String context =
        canCallFunc
            ? EvaluateWatchCommand.EVALUATE_CONTEXT_WATCH
            : EvaluateWatchCommand.EVALUATE_CONTEXT_VARIABLES;
    DebugCommand command = new EvaluateWatchCommand(watch, context);

    int requestId = adapter.message(command);
    return requestId;
  }

  @Override
  public void refreshCallStack() {
    DebugCommand command = new StackTraceCommand();
    adapter.message(command);
  }
}
