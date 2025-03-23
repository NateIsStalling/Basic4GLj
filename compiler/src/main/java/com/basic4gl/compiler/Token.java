package com.basic4gl.compiler;

class Token {

    private String text;
    private TokenType tokenType;

    private int valType;

    private boolean newLine;

    private int line;
    private int col;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * For constants. Defines the value type.
     */
    public int getValType() {
        return valType;
    }

    public void setValType(int valType) {
        this.valType = valType;
    }

    /**
     * True if immediately preceeded by newline
     */
    public boolean isNewLine() {
        return newLine;
    }

    public void setNewLine(boolean newLine) {
        this.newLine = newLine;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public static enum TokenType {
        CTT_CONSTANT,
        /**
         * Could be a variable, function name, keyword, e.t.c...
         */
        CTT_TEXT,
        /**
         * Note: Parser will never return CTT_KEYWORD,
         * as it doesnt know what the reserved keywords are.
         * It is up to the calling code to detect CTT_KEYWORD,
         * compare it against its known keywords and update
         * the token type.
         */
        CTT_KEYWORD,
        /**
         * A builtin function (Same deal as CTT_KEYWORD)
         */
        CTT_FUNCTION,
        /**
         * A user defined function (Same deal as CTT_KEYWORD)
         */
        CTT_USER_FUNCTION,
        /**
         * A special function that will be implemented by runtime compiled code
         */
        CTT_RUNTIME_FUNCTION,
        CTT_SYMBOL,
        CTT_EOL,
        CTT_EOF
    }
}
