package com.basic4gl.runtime.stackframe;

import java.util.Vector;

/**
 * Stack frame created when a user function is called.
 */
public class UserFuncStackFrame {

  // Corresponding function definition. -1 if is a simple GOSUB
  public int userFuncIndex;

  // Return address
  public int returnAddr;

  // Previous stack frame info
  // (ignored for GOSUBs)
  public int prevStackTop;
  public int prevTempDataLock;
  public int prevCurrentFrame;

  // Local variables and parameters
  // Stores offset of each variable in data array (0 = unallocated).
  // Elements 0..paramCount-1 are parameters, paramCount..size()-1 are local
  // variables.
  // (also ignored for GOSUBs)
  public Vector<Integer> localVarDataOffsets;

  public UserFuncStackFrame() {
    localVarDataOffsets = new Vector<Integer>();
  }

  public void InitForGosub(int returnAddr) {
    userFuncIndex = -1;
    this.returnAddr = returnAddr;
    localVarDataOffsets.clear();
  }

  public void InitForUserFunction(UserFuncPrototype prototype, int userFuncIndex) {
    this.userFuncIndex = userFuncIndex;
    returnAddr = -1;

    // Allocate local variable data offsets
    int oldSize = localVarDataOffsets.size();
    int newSize = prototype.localVarTypes.size();
    localVarDataOffsets.setSize(newSize);
    for (int i = oldSize; i < newSize; i++) {
      localVarDataOffsets.set(i, 0);
    }
  }
}
