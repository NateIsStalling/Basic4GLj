package com.basic4gl.language.core.runtime;

public class SourceUserBreakPt {
    public String sourceFile;
    public int lineNo;

    public SourceUserBreakPt(String sourceFile, int lineNo) {
        this.sourceFile = sourceFile;
        this.lineNo = lineNo;
    }

    public SourceUserBreakPt(SourceUserBreakPt b) {
        sourceFile = b.sourceFile;
        lineNo = b.lineNo;
    }
}
