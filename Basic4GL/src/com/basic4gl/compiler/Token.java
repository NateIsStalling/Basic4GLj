package com.basic4gl.compiler;

import com.basic4gl.vm.types.ValType;

class Token {
	public static enum TokenType {
		CTT_CONSTANT, 
		CTT_TEXT, // Could be a variable, function name, keyword, e.t.c...
		CTT_KEYWORD, // Note: Parser will never return CTT_KEYWORD,
						// as it doesnt know what the reserved keywords are.
						// It is up to the calling code to detect CTT_KEYWORD,
						// compare it against its known keywords and update
						// the token type.
		CTT_FUNCTION, 			// A builtin function (Same deal as CTT_KEYWORD)
		CTT_USER_FUNCTION, 		// A user defined function (Same deal as CTT_KEYWORD)
        CTT_RUNTIME_FUNCTION,   // A special function that will be implemented by runtime compiled code
		CTT_SYMBOL, 
		CTT_EOL, 
		CTT_EOF
	}
	
	String m_text;
	TokenType m_type;
	int m_valType; // For constants. Defines the value type.
	boolean m_newLine; // True if immediately preceeded by newline
	int m_line;
	int m_col;
}