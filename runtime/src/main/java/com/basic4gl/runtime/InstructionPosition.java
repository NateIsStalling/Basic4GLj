package com.basic4gl.runtime;

/**
 * Marks a position within a source file
 */
public class InstructionPosition {
	private int sourceLine;
	private int sourceColumn;

	public InstructionPosition() {
		sourceLine = 0;
		sourceColumn = 0;
	}

	public InstructionPosition(int line, int col) {
		sourceLine = line;
		sourceColumn = col;
	}

	public int getSourceLine() {
		return sourceLine;
	}

	public int getSourceColumn() {
		return sourceColumn;
	}

	public void setSourcePosition(int line, int col) {
		sourceLine = line;
		sourceColumn = col;
	}
}