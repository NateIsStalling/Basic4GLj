package com.basic4gl.compiler;

// compInstructionPos
// Marks a position within a source file
class InstructionPos {
	int m_sourceLine, m_sourceCol;

	InstructionPos() {
		m_sourceLine = 0;
		m_sourceCol = 0;
	}

	InstructionPos(int line, int col) {
		m_sourceLine = line;
		m_sourceCol = col;
	}
}