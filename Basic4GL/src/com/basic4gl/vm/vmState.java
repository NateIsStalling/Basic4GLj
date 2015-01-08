package com.basic4gl.vm;

////////////////////////////////////////////////////////////////////////////////
// VM state
//
// Used to save virtual machine state.
// (Debugger uses this to run small sections of code, without interrupting the
// main program.)

class vmState {

	// Instruction pointer
	int ip;

	// Registers
	VMValue reg, reg2;
	String regString, reg2String;

	// Stacks
	int stackTop, userFuncStackTop;
	int currentUserFrame;

	// Top of program
	int codeSize;
	int codeBlockCount;
	
	// Variable data
	int stackDataTop, tempDataLock;

	// Error state
	boolean error;
	String errorString;

	// Other state
	boolean paused;
}
