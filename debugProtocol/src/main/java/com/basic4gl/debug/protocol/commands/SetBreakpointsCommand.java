package com.basic4gl.debug.protocol.commands;

import com.basic4gl.debug.protocol.types.Source;
import com.basic4gl.debug.protocol.types.SourceBreakpoint;
import java.util.List;

public class SetBreakpointsCommand extends DebugCommand {
  public static final String COMMAND = "set-breakpoints";

  protected Source source;

  protected List<SourceBreakpoint> breakpoints;

  protected boolean sourceModified;

  public SetBreakpointsCommand() {
    super(COMMAND);
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public Source getSource() {
    return this.source;
  }

  public void setBreakpoints(List<SourceBreakpoint> breakpoints) {
    this.breakpoints = breakpoints;
  }

  public List<SourceBreakpoint> getBreakpoints() {
    return this.breakpoints;
  }

  public void setSourceModified(boolean sourceModified) {
    this.sourceModified = sourceModified;
  }

  public boolean isSourceModified() {
    return sourceModified;
  }
}
