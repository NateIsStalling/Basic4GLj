package com.basic4gl.runtime;

public class UserBreakPt {
	private int offset;

	public UserBreakPt() {
		offset = 0xffff;
	}

	/**
	 * Note: Set to 0xffff if line is invalid.
	 */
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
