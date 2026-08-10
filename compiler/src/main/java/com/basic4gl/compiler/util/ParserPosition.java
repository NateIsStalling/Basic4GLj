package com.basic4gl.compiler.util;

// Misc
public class ParserPosition {
    private int line;
    private int column;
    private com.basic4gl.language.core.types.Token token;

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public com.basic4gl.language.core.types.Token getToken() {
        return token;
    }

    public void setToken(com.basic4gl.language.core.types.Token token) {
        this.token = token;
    }
}
