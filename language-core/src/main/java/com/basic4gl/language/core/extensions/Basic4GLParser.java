package com.basic4gl.language.core.extensions;

import com.basic4gl.language.core.types.Token;

import java.util.Vector;

public interface Basic4GLParser {
    // Reading
    char getChar(boolean inString);

    char peekChar(boolean inString);

    String getText();

    boolean isNumber(char c);

    boolean isComparison(char c);

    Vector<String> getSourceCode();

    void setPos(int line, int col);

    void reset();

    int getLine();

    int getColumn();

    boolean isEof();

    Token nextToken();

    Token nextToken(boolean skipEOL, boolean dataMode);

    Token peekToken();

    Token peekToken(boolean skipEOL, boolean dataMode);

    // Special mode
    boolean isSpecialMode();

    void setSpecial(String text);

    void setSpecial(String text, int line, int col);
}
