package com.basic4gl.runtime;

import com.basic4gl.runtime.types.OpCode;

public class PatchedBreakPt {

  /**
   * Op-Code offset in program
   */
  int offset;

  /**
   * For active breakpoints:
   * The op-code that has been replaced with the OP_BREAKPT.
   */
  short replacedOpCode;

  public PatchedBreakPt() {
    offset = 0xffff;
    replacedOpCode = OpCode.OP_NOP;
  }
}
