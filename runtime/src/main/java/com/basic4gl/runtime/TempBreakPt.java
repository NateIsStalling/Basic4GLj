package com.basic4gl.runtime;

public class TempBreakPt {
	private int offset;

	public TempBreakPt() {
		offset = 0xffff;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
