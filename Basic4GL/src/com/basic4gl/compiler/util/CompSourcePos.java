package com.basic4gl.compiler.util;

public class CompSourcePos {
	// Source file and line number before pre-processing
	private int mFileIndex;
	private int mFileLineNo;

	public CompSourcePos(int fileIndex, int fileLineNo) {
		mFileIndex = fileIndex;
		mFileLineNo = fileLineNo;
	}

	public CompSourcePos(CompSourcePos c) {
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