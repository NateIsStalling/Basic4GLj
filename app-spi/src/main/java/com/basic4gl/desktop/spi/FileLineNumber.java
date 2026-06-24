package com.basic4gl.desktop.spi;

public class FileLineNumber {
    private final String filename;
    private final int lineNumber;

    public FileLineNumber(String filename, int lineNumber) {
        this.filename = filename;
        this.lineNumber = lineNumber;
    }

    public String getFilename() {
        return filename;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
