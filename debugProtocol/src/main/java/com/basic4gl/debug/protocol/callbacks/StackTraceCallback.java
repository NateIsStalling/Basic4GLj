package com.basic4gl.debug.protocol.callbacks;

import com.basic4gl.debug.protocol.types.StackFrame;
import java.util.ArrayList;

public class StackTraceCallback extends Callback {
  public static final String COMMAND = "stackTrace";

  public StackTraceCallback() {
    super(COMMAND);
  }

  public ArrayList<StackFrame> stackFrames = new ArrayList<>();
  public int totalFrames;
}
