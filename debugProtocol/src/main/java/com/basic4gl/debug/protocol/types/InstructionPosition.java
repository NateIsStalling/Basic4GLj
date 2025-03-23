package com.basic4gl.debug.protocol.types;

public class InstructionPosition {
public int line;
public int column;

public InstructionPosition(int line, int column) {
	this.line = line;
	this.column = column;
}

public InstructionPosition(InstructionPosition other) {
	this.line = other.line;
	this.column = other.column;
}
}
