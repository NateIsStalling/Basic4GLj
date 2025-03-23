package com.basic4gl.runtime;

/**
 * Used to save virtual machine state.
 * (Debugger uses this to run small sections of code, without interrupting the
 * main program.)
 */
public class VMState {

  // Instruction pointer
  int ip;

  // Registers
  Value reg, reg2;
  String regString, reg2String;

  // Stacks
  int stackTop, userFuncStackTop;
  int currentUserFrame;

  // Top of program
  int codeSize;
  int codeBlockCount;

  // Var data
  int stackDataTop, tempDataLock;

  // Error state
  boolean error;
  String errorString;

  // Other state
  boolean paused;
}
