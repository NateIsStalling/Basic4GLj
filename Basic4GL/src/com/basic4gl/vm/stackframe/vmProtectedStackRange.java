package com.basic4gl.vm.stackframe;

////////////////////////////////////////////////////////////////////////////////
//vmProtectedStackRange
//
/// Indicates a range of data on the stack protected from destruction.
/// (Used when collapsing temporary data, to destroy everything except the data
/// to be returned).
public class vmProtectedStackRange {
	int startAddr, endAddr;
	public vmProtectedStackRange() {
		startAddr = 0;
		endAddr = 0;
	}
	public vmProtectedStackRange(int _startAddr, int _endAddr) {
		startAddr = _startAddr;
		endAddr = _endAddr;
	}

	public boolean ContainsAddr(int addr) { return addr >= startAddr && addr < endAddr; }
	public boolean ContainsRange(int start, int end) { return start >= startAddr && end <= endAddr; }
}
