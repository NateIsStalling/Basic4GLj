package com.basic4gl.compiler;

import java.util.*;

import com.basic4gl.compiler.Token.TokenType;
import com.basic4gl.vm.HasErrorState;
import com.basic4gl.vm.types.ValType.BasicValType;

public class compParser extends HasErrorState {


	// Text
	Vector<String> m_sourceCode;

	// Special mode
	boolean m_special;
	String m_specialText;
	int m_specialCol;
	int m_specialSourceLine, m_specialSourceCol;

	// State
	int m_line, m_col;

	// Reading
	char GetChar(boolean inString) {
		char c = PeekChar(inString); // Read character
		if (c == 13) { // Advance position
			m_col = 0;
			m_line++;
		} else {
			if (m_special)
				m_specialCol++;
			else
				m_col++;
		}
		return c;
	}

	char PeekChar(boolean inString) {
		if (Eof()) // End of file
			return 0;
		else if (m_special)
			return m_specialText.charAt(m_specialCol);
		else if (m_col >= Text().length()
				|| (Text().charAt(m_col) == '\'' && !inString)) // End of line,
																// or start of
																// comment
			return 13;
		else
			return Text().charAt(m_col); // Regular text
	}

	String Text() {
		assert (!Eof());
		return m_sourceCode.get(m_line);
	}

	boolean IsNumber(char c) {
		return ((c >= '0' && c <= '9') || c == '.');
	}

	boolean IsComparison(char c) {
		return c == '<' || c == '=' || c == '>';
	}

	public compParser() {
		m_sourceCode = new Vector<String>();
		m_specialText = "";
		Reset();
	}

	public Vector<String> SourceCode() {
		return m_sourceCode;
	}

	public void SetPos(int line, int col) {
		SetNormal();
		m_line = line;
		m_col = col;
		ClearError();
	}

	public void Reset() {
		SetPos(0, 0);
	}

	public int Line() {
		return m_line;
	}

	public int Col() {
		return m_col;
	}

	public boolean Eof() {
		if (m_special)
			return m_specialCol >= m_specialText.length();
		else
			return m_line >= m_sourceCode.size();
	}

	public Token NextToken() {
		return NextToken(false, false);
	}

	public Token NextToken(boolean skipEOL, boolean dataMode) {

		ClearError();

		// Create token, with some defaults
		Token t = new Token();
		t.m_text = "";
		t.m_valType = BasicValType.VTP_INT;
		t.m_newLine = (m_col == 0);

		// Skip leading whitespace.
		// Detect newlines
		char c = ' ';
		if (m_special) { // Special text being parsed.
			t.m_line = m_specialSourceLine; // Substitute the position in the
											// source that the special text has
											// been
			t.m_col = m_specialSourceCol; // associated with.
		} else {
			t.m_line = m_line;
			t.m_col = m_col;
		}
		while (!Eof() && c <= ' ' && (c != 13 || skipEOL)) {
			if (m_special) { // Special text being parsed.
				t.m_line = m_specialSourceLine; // Substitute the position in
												// the source that the special
												// text has been
				t.m_col = m_specialSourceCol; // associated with.
			} else {
				t.m_line = m_line;
				t.m_col = m_col;
			}
			c = GetChar(false);
			t.m_newLine = t.m_newLine || m_col == 0;
		}

		// Determine token type
		if (c == 0)
			t.m_type = TokenType.CTT_EOF;
		else if (c == 13)
			t.m_type = TokenType.CTT_EOL;
		else if (c == '"') {
			t.m_type = TokenType.CTT_CONSTANT;
			t.m_valType = BasicValType.VTP_STRING;
		} else if (!dataMode) {
			if (IsNumber((char) c)) {
				t.m_type = TokenType.CTT_CONSTANT;
				if (c == '.')
					t.m_valType = BasicValType.VTP_REAL;
			} else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| c == '_')
				t.m_type = TokenType.CTT_TEXT;
			else
				t.m_type = TokenType.CTT_SYMBOL;
		} else {

			// Data mode. (And not a quoted string or EOF/EOL).
			// Comma and colon are recognised separators (colon also means end
			// of statement).
			// Anything else is considered part of the data element.
			if (c == ',' || c == ':')
				t.m_type = TokenType.CTT_SYMBOL;
			else
				t.m_type = TokenType.CTT_TEXT;
		}

		// Empty token types
		if (t.m_type == TokenType.CTT_EOF
				|| t.m_type == TokenType.CTT_EOL)
			return t;

		// Extract token text
		// Regular case, not dataMode text
		if (!dataMode || t.m_type != TokenType.CTT_TEXT) {
			// Don't include leading quote (if string constant)
			if (!(t.m_type == TokenType.CTT_CONSTANT && t.m_valType == BasicValType.VTP_STRING)) 
				t.m_text = t.m_text + (char) c;
			c = PeekChar(t.m_valType == BasicValType.VTP_STRING);
			byte lcase = (byte) Character.toLowerCase(c);
			boolean done = false;
			boolean hex = false;

			while (!done && !Error()) {

				// Determine whether found end of token
				switch (t.m_type) {
				case CTT_CONSTANT:
					if (t.m_valType == BasicValType.VTP_STRING) {
						if (c == '"') {
							done = true;
							GetChar(false); // Skip terminating quote
						} else if (c == 0 || c == 13) {
							SetError("Unterminated string");
							done = true;
						}
					} else {
						boolean validDecimalPt = (c == '.' && t.m_valType == BasicValType.VTP_INT);
						if (validDecimalPt) // Floating point number
							t.m_valType = BasicValType.VTP_REAL;

	                    boolean hexSpecifier = lcase == 'x' && (t.m_text.equals("0") || t.m_text.equals("-0"));
	                    if (hexSpecifier)
	                        hex = true;
	                    done = !(   (c >= '0' && c <= '9')
	                                || validDecimalPt
	                                || hexSpecifier                             // Hex specifier
	                                || (hex && lcase >= 'a' && lcase <= 'f')); // Or hex
																	// digit
					}
					break;
				case CTT_TEXT:
					done = !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
							|| c == '_' || (c >= '0' && c <= '9'));
					if (c == '$' || c == '%' || c == '#') // Variables can be
															// trailed with a $,
															// # or %
						t.m_text = t.m_text + GetChar(false);
					break;
				case CTT_SYMBOL:
					done = !(IsComparison(t.m_text.charAt(0)) && IsComparison(c));
					break;
				default:
					SetError("Bad token");
				}

				// Store character
				if (!done) {
					t.m_text = t.m_text
							+ GetChar(t.m_valType == BasicValType.VTP_STRING);
					c = PeekChar(t.m_valType == BasicValType.VTP_STRING);
					lcase = (byte) Character.toLowerCase(c);
				}
	            else {
	                // Check token is well formed
	                if (t.m_type == TokenType.CTT_CONSTANT && t.m_valType == BasicValType.VTP_INT) {
	                    // Check integer number is valid
	                    char last = t.m_text.charAt(t.m_text.length() - 1);
	                    if (last == 'x' || last == 'X')
	                        SetError("'" + t.m_text + "' is not a valid number");
	                }
	            }

			}
		} else {

			// Special case: dataMode text
			// Token is actually a text constant
			t.m_type = TokenType.CTT_CONSTANT;
			boolean done = false;
			boolean hex = false;
			boolean firstIteration = true;
			boolean whiteSpaceFound = false;
			byte lcase = (byte) Character.toLowerCase(c);

			while (!done && !Error()) {

				// Comma, colon, return, newline and eof are separators
				// Double quotes are not allowed (syntactically), so we truncate
				// the
				// token at that point
				done = c == 13 || c == 10 || c == 0 || c == ',' || c == ':'
						|| c == '"';
				if (!done) {

					// Value type rules.
					// Assume numeric until non-numeric character found.
					// (Also, numeric types can't have spaces).
					// Decimal point means floating point (real) type.
					if (t.m_valType != BasicValType.VTP_STRING) {

						boolean validDecimalPt = (c == '.' && t.m_valType == BasicValType.VTP_INT);
	                    if (validDecimalPt)                                   // Floating point number
							t.m_valType = BasicValType.VTP_REAL;
	                    boolean hexSpecifier = (lcase == 'x' && (t.m_text.equals("0") || t.m_text.equals("-0")));
	                    if (hexSpecifier)
	                        hex = true;
	                    if (!(  (c >= '0' && c <= '9')
	                            ||  validDecimalPt                              // Regular decimal number
	                            ||  hexSpecifier

								|| (hex && lcase >= 'a' && lcase <= 'f') // Or
																			// hex
																			// digit
								|| (firstIteration && c == '-') // Negative sign
						|| (c <= ' ')) // Trailing whitespace
								|| (c > ' ' && whiteSpaceFound)) // Contained
																	// whitespace
							t.m_valType = BasicValType.VTP_STRING;
					}
					if (c <= ' ')
						whiteSpaceFound = true;
				}
				if (!done) {
					if (firstIteration)
						t.m_text = String.valueOf((char) c);
					else
						t.m_text = t.m_text + GetChar(false);
					c = PeekChar(false);
					lcase = (byte) Character.toLowerCase(c);
				}
	            else {
	                // Check token is well formed
	                if (t.m_type == TokenType.CTT_CONSTANT && t.m_valType == BasicValType.VTP_INT) {
	                    // Check integer number is valid
	                    char last = t.m_text.charAt(t.m_text.length() - 1);
	                    if (last == 'x' || last == 'X')
	                        SetError("'" + t.m_text + "' is not a valid number");
	                }
	            }

				firstIteration = false;
			}

			// Trim trailing whitespace
			int end = t.m_text.length() - 1;
			while (end >= 0 && t.m_text.charAt(end) <= ' ')
				end--;
			t.m_text = t.m_text.substring(0, end + 1);
		}

		return t;
	}

	public Token PeekToken() {
		return PeekToken(false, false);
	}

	public Token PeekToken(boolean skipEOL, boolean dataMode) {

		// Save position
		int line = m_line, col = m_col;

		// Read token
		Token t = NextToken(skipEOL, dataMode);

		// Restore position. (Except on error, when we leave the cursor pointing
		// to
		// the error position.)
		if (!Error()) {
			m_line = line;
			m_col = col;
		}

		return t;
	}

	// "dataMode" is set to true when reading elements of a "DATA" statement.
	// (Slightly different parsing rules apply.)

	// Special mode
	public boolean Special() {
		return m_special;
	}

	public void SetSpecial(String text) {
		SetSpecial(text, -1, -1);
	}

	public void SetSpecial(String text, int line, int col) {
		m_special = true;
		m_specialText = text;
		m_specialCol = 0;
		m_specialSourceCol = col;
		if (line >= 0)
			m_specialSourceLine = line;
		else
			m_specialSourceLine = m_line;
		if (col >= 0)
			m_specialSourceCol = col;
		else
			m_specialSourceCol = m_col;
	}

	public void SetNormal() {
		m_special = false;
	}

}