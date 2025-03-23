package com.basic4gl.compiler.util;

import java.io.Serializable;

/**
 * Source file and line number before pre-processing
 */
public class SourcePosition implements Serializable {
    private int mFileIndex;
    private int mFileLineNo;

    public SourcePosition(int fileIndex, int fileLineNo) {
        mFileIndex = fileIndex;
        mFileLineNo = fileLineNo;
    }

    public SourcePosition(SourcePosition c) {
        mFileIndex = c.mFileIndex;
        mFileLineNo = c.mFileLineNo;
    }

    public int getFileIndex() {
        return mFileIndex;
    }

    public int getFileLineNo() {
        return mFileLineNo;
    }

    public void setFileIndex(int fileIndex) {
        mFileIndex = fileIndex;
    }

    public void setFileLineNo(int fileLineNo) {
        mFileLineNo = fileLineNo;
    }
}
