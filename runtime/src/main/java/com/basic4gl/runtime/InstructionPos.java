package com.basic4gl.runtime;

// compInstructionPos
// Marks a position within a source file
public class InstructionPos {
	int m_sourceLine, m_sourceCol;

	public InstructionPos() {
		m_sourceLine = 0;
		m_sourceCol = 0;
	}

	public InstructionPos(int line, int col) {
		m_sourceLine = line;
		m_sourceCol = col;
	}

	public int getSourceLine() {
		return m_sourceLine;
	}

	public int getSourceColumn() {
		return m_sourceCol;
	}

	public void setSourcePosition(int line, int col) {
		m_sourceLine = line;
		m_sourceCol = col;
	}
}