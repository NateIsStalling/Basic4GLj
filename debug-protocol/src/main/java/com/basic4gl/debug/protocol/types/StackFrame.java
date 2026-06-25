package com.basic4gl.debug.protocol.types;

public class StackFrame {

    // either a user function name or GOSUB label
    public String name;

    // the source code filename
    public String source;

    public int line;

    public int column;

    public String instructionPointer;

    public StackFrame() {}
}
