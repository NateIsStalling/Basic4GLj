package com.basic4gl.runtime.stackframe;

/**
 * Indicates a range of data on the stack protected from destruction.
 * (Used when collapsing temporary data, to destroy everything except the data
 * to be returned).
 */
public class ProtectedStackRange {
	int startAddr, endAddr;
	public ProtectedStackRange() {
		startAddr = 0;
		endAddr = 0;
	}
	public ProtectedStackRange(int startAddr, int endAddr) {
		this.startAddr = startAddr;
		this.endAddr = endAddr;
	}

	public boolean containsAddress(int addr) { return addr >= startAddr && addr < endAddr; }
	public boolean containsRange(int start, int end) { return start >= startAddr && end <= endAddr; }
}
