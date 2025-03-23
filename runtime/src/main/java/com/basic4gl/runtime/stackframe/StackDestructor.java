package com.basic4gl.runtime.stackframe;

/**
 * Indicates data in the function or temp stack area that needs to be destroyed
 * when the stack unwinds.
 * Currently, our only "destruction" logic is for deallocating strings
 * referenced by the data.
 */
public class StackDestructor {

  /**
   * Address of data on stack or in temp space
   */
  public int addr;

  /**
   * Index of data type
   */
  public int dataTypeIndex;

  public StackDestructor(int addr, int dataTypeIndex) {
    this.addr = addr;
    this.dataTypeIndex = dataTypeIndex;
  }
}
