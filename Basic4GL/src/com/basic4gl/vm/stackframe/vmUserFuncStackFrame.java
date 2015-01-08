package com.basic4gl.vm.stackframe;

import java.util.Vector;

////////////////////////////////////////////////////////////////////////////////
//vmUserFuncStackFrame
//
//Stack frame created when a user function is called.

public class vmUserFuncStackFrame {

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

	public vmUserFuncStackFrame() {
		localVarDataOffsets = new Vector<Integer>();
	}

	public void InitForGosub(int _returnAddr) {
		userFuncIndex = -1;
		returnAddr = _returnAddr;
		localVarDataOffsets.clear();
	}

	public void InitForUserFunction(vmUserFuncPrototype prototype, int _userFuncIndex) {
		userFuncIndex = _userFuncIndex;
		returnAddr = -1;

		// Allocate local variable data offsets
		int oldSize = localVarDataOffsets.size();
		int newSize = prototype.localVarTypes.size();
		localVarDataOffsets.setSize(newSize);
		for (int i = oldSize; i < newSize; i++)
			localVarDataOffsets.set(i, 0);
	}
}