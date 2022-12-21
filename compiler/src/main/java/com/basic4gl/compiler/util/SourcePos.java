package com.basic4gl.compiler.util;

import java.io.Serializable;

public class SourcePos implements Serializable {
	// Source file and line number before pre-processing
	private int mFileIndex;
	private int mFileLineNo;

	public SourcePos(int fileIndex, int fileLineNo) {
		mFileIndex = fileIndex;
		mFileLineNo = fileLineNo;
	}

	public SourcePos(SourcePos c) {
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