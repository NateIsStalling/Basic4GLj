package com.basic4gl.compiler;

class Token {
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

  String text;
  TokenType tokenType;

  /**
   * For constants. Defines the value type.
   */
  int valType;

  /**
   * True if immediately preceeded by newline
   */
  boolean newLine;

  int line;
  int col;
}
