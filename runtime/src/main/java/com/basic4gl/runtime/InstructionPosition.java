package com.basic4gl.runtime;

import java.io.Serializable;

/**
 * Marks a position within a source file
 * TODO:
 */
public class InstructionPosition implements Serializable {
	private int sourceLine = 0;
	private int sourceColumn = 0;
	// TODO: replace this with the name of the file instead
	private int fileIndex = 0;

	public InstructionPosition() {
	}

	public InstructionPosition(int fileIndex, int line, int col) {
		this.fileIndex = fileIndex;
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

	public int getFileIndex() {
		return fileIndex;
	}

	public int getFileLineNumber() {
		return sourceLine;
	}

	public void setFileIndex(int fileIndex) {
		this.fileIndex = fileIndex;
	}

	public void setFileLineNumber(int fileLineNo) {
		sourceLine = fileLineNo;
	}
}
