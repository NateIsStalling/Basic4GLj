package com.basic4gl.vm.util;

public class SourceUserBreakPt {
	public String sourceFile;
	public int lineNo;

	public SourceUserBreakPt(String _sourceFile, int _lineNo) {
		sourceFile = _sourceFile;
		lineNo = _lineNo;
	}

	public SourceUserBreakPt(SourceUserBreakPt b) {
		sourceFile = b.sourceFile;
		lineNo = b.lineNo;
	}
}