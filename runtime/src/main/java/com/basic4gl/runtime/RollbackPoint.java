package com.basic4gl.runtime;

public class RollbackPoint {
    // Registered code blocks
	public int codeBlockCount;
    public int boundCodeBlock;

    // User function prototypes
    public int functionPrototypeCount;

    // User functions
    public int functionCount;

    // Data statements
    public int dataCount;

    // Program statements
    public int instructionCount;
}
