package com.basic4gl.compiler.util;

import java.io.Serializable;

/**
 * Source file and line number before pre-processing
 */
public class SourcePosition implements Serializable {
    private int fileIndex;
    private int fileLineNumber;

    public SourcePosition(int fileIndex, int fileLineNo) {
        this.fileIndex = fileIndex;
        fileLineNumber = fileLineNo;
    }

    public SourcePosition(SourcePosition c) {
        fileIndex = c.fileIndex;
        fileLineNumber = c.fileLineNumber;
    }

    public int getFileIndex() {
        return fileIndex;
    }

    public int getFileLineNumber() {
        return fileLineNumber;
    }

    public void setFileIndex(int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public void setFileLineNumber(int fileLineNo) {
        fileLineNumber = fileLineNo;
    }
}
