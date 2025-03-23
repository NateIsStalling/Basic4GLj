package com.basic4gl.compiler;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.compiler.Token.TokenType;
import com.basic4gl.runtime.HasErrorState;
import com.basic4gl.runtime.types.BasicValType;
import java.util.*;

public class Parser extends HasErrorState {

// Text
private final Vector<String> sourceCode;

// Special mode
private boolean isSpecialMode;
private String specialText;
private int specialColumn;
private int specialSourceLine;
private int specialSourceColumn;

// State
private int line;
private int column;

public Parser() {
	sourceCode = new Vector<>();
	specialText = "";
	reset();
}

// Reading
char getChar(boolean inString) {
	char c = peekChar(inString); // Read character
	if (c == 13) { // Advance position
	column = 0;
	line++;
	} else {
	if (isSpecialMode) {
		specialColumn++;
	} else {
		column++;
	}
	}
	return c;
}

char peekChar(boolean inString) {
	if (isEof()) // End of file
	{
	return 0;
	} else if (isSpecialMode) {
	return specialText.charAt(specialColumn);
	} else if (column >= getText().length()
		|| (getText().charAt(column) == '\'' && !inString)) // End of line,
	// or start of
	// comment
	{
	return 13;
	} else {
	return getText().charAt(column); // Regular text
	}
}

String getText() {
	assertTrue(!isEof());
	return sourceCode.get(line);
}

boolean isNumber(char c) {
	return ((c >= '0' && c <= '9') || c == '.');
}

boolean isComparison(char c) {
	return c == '<' || c == '=' || c == '>';
}

public Vector<String> getSourceCode() {
	return sourceCode;
}

public void setPos(int line, int col) {
	setNormal();
	this.line = line;
	column = col;
	clearError();
}

public void reset() {
	setPos(0, 0);
}

public int getLine() {
	return line;
}

public int getColumn() {
	return column;
}

public boolean isEof() {
	if (isSpecialMode) {
	return specialColumn >= specialText.length();
	} else {
	return line >= sourceCode.size();
	}
}

public Token nextToken() {
	return nextToken(false, false);
}

public Token nextToken(boolean skipEOL, boolean dataMode) {

	clearError();

	// Create token, with some defaults
	Token t = new Token();
	t.setText("");
	t.setValType(BasicValType.VTP_INT);
	t.setNewLine((column == 0));

	// Skip leading whitespace.
	// Detect newlines
	char c = ' ';
	if (isSpecialMode) {
	// Special text being parsed.

	// Substitute the position in the source that
	// the special text has been associated with.
	t.setLine(specialSourceLine);
	t.setCol(specialSourceColumn);
	} else {
	t.setLine(line);
	t.setCol(column);
	}
	while (!isEof() && c <= ' ' && (c != 13 || skipEOL)) {
	if (isSpecialMode) {
		// Special text being parsed.

		// Substitute the position in the source that
		// the special text has been associated with.
		t.setLine(specialSourceLine);
		t.setCol(specialSourceColumn);
	} else {
		t.setLine(line);
		t.setCol(column);
	}
	c = getChar(false);
	t.setNewLine(t.isNewLine() || column == 0);
	}

	// Determine token type
	if (c == 0) {
	t.setTokenType(TokenType.CTT_EOF);
	} else if (c == 13) {
	t.setTokenType(TokenType.CTT_EOL);
	} else if (c == '"') {
	t.setTokenType(TokenType.CTT_CONSTANT);
	t.setValType(BasicValType.VTP_STRING);
	} else if (!dataMode) {
	if (isNumber((char) c)) {
		t.setTokenType(TokenType.CTT_CONSTANT);
		if (c == '.') {
		t.setValType(BasicValType.VTP_REAL);
		}
	} else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
		t.setTokenType(TokenType.CTT_TEXT);
	} else {
		t.setTokenType(TokenType.CTT_SYMBOL);
	}
	} else {

	// Data mode. (And not a quoted string or EOF/EOL).
	// Comma and colon are recognised separators (colon also means end
	// of statement).
	// Anything else is considered part of the data element.
	if (c == ',' || c == ':') {
		t.setTokenType(TokenType.CTT_SYMBOL);
	} else {
		t.setTokenType(TokenType.CTT_TEXT);
	}
	}

	// Empty token types
	if (t.getTokenType() == TokenType.CTT_EOF || t.getTokenType() == TokenType.CTT_EOL) {
	return t;
	}

	// Extract token text
	// Regular case, not dataMode text
	if (!dataMode || t.getTokenType() != TokenType.CTT_TEXT) {
	// Don't include leading quote (if string constant)
	if (!(t.getTokenType() == TokenType.CTT_CONSTANT
		&& t.getValType() == BasicValType.VTP_STRING)) {
		t.setText(t.getText() + (char) c);
	}
	c = peekChar(t.getValType() == BasicValType.VTP_STRING);
	byte lcase = (byte) Character.toLowerCase(c);
	boolean done = false;
	boolean hex = false;

	while (!done && !hasError()) {

		// Determine whether found end of token
		switch (t.getTokenType()) {
		case CTT_CONSTANT:
			if (t.getValType() == BasicValType.VTP_STRING) {
			if (c == '"') {
				done = true;
				getChar(false); // Skip terminating quote
			} else if (c == 0 || c == 13) {
				setError("Unterminated string");
				done = true;
			}
			} else {
			boolean validDecimalPt = (c == '.' && t.getValType() == BasicValType.VTP_INT);
			if (validDecimalPt) // Floating point number
			{
				t.setValType(BasicValType.VTP_REAL);
			}

			boolean hexSpecifier =
				lcase == 'x' && (t.getText().equals("0") || t.getText().equals("-0"));
			if (hexSpecifier) {
				hex = true;
			}
			done =
				!((c >= '0' && c <= '9')
					|| validDecimalPt
					|| hexSpecifier // Hex specifier
					|| (hex && lcase >= 'a' && lcase <= 'f')); // Or hex
			// digit
			}
			break;
		case CTT_TEXT:
			done =
				!((c >= 'a' && c <= 'z')
					|| (c >= 'A' && c <= 'Z')
					|| c == '_'
					|| (c >= '0' && c <= '9'));
			if (c == '$' || c == '%' || c == '#') // Variables can be
			// trailed with a $,
			// # or %
			{
			t.setText(t.getText() + getChar(false));
			}
			break;
		case CTT_SYMBOL:
			done = !(isComparison(t.getText().charAt(0)) && isComparison(c));
			break;
		default:
			setError("Bad token");
		}

		// Store character
		if (!done) {
		t.setText(t.getText() + getChar(t.getValType() == BasicValType.VTP_STRING));
		c = peekChar(t.getValType() == BasicValType.VTP_STRING);
		lcase = (byte) Character.toLowerCase(c);
		} else {
		// Check token is well formed
		if (t.getTokenType() == TokenType.CTT_CONSTANT
			&& t.getValType() == BasicValType.VTP_INT) {
			// Check integer number is valid
			char last = t.getText().charAt(t.getText().length() - 1);
			if (last == 'x' || last == 'X') {
			setError("'" + t.getText() + "' is not a valid number");
			}
		}
		}
	}
	} else {

	// Special case: dataMode text
	// Token is actually a text constant
	t.setTokenType(TokenType.CTT_CONSTANT);
	boolean done = false;
	boolean hex = false;
	boolean firstIteration = true;
	boolean whiteSpaceFound = false;
	byte lcase = (byte) Character.toLowerCase(c);

	while (!done && !hasError()) {

		// Comma, colon, return, newline and eof are separators
		// Double quotes are not allowed (syntactically), so we truncate
		// the token at that point
		done = c == 13 || c == 10 || c == 0 || c == ',' || c == ':' || c == '"';
		if (!done) {

		// Value type rules.
		// Assume numeric until non-numeric character found.
		// (Also, numeric types can't have spaces).
		// Decimal point means floating point (real) type.
		if (t.getValType() != BasicValType.VTP_STRING) {

			boolean validDecimalPt = (c == '.' && t.getValType() == BasicValType.VTP_INT);
			if (validDecimalPt) // Floating point number
			{
			t.setValType(BasicValType.VTP_REAL);
			}
			boolean hexSpecifier =
				(lcase == 'x' && (t.getText().equals("0") || t.getText().equals("-0")));
			if (hexSpecifier) {
			hex = true;
			}
			if (!((c >= '0' && c <= '9')
					|| validDecimalPt // Regular decimal number
					|| hexSpecifier
					|| (hex && lcase >= 'a' && lcase <= 'f') // Or
					// hex
					// digit
					|| (firstIteration && c == '-') // Negative sign
					|| (c <= ' ')) // Trailing whitespace
				|| (c > ' ' && whiteSpaceFound)) // Contained
			// whitespace
			{
			t.setValType(BasicValType.VTP_STRING);
			}
		}
		if (c <= ' ') {
			whiteSpaceFound = true;
		}
		}
		if (!done) {
		if (firstIteration) {
			t.setText(String.valueOf((char) c));
		} else {
			t.setText(t.getText() + getChar(false));
		}
		c = peekChar(false);
		lcase = (byte) Character.toLowerCase(c);
		} else {
		// Check token is well formed
		if (t.getTokenType() == TokenType.CTT_CONSTANT
			&& t.getValType() == BasicValType.VTP_INT) {
			// Check integer number is valid
			char last = t.getText().charAt(t.getText().length() - 1);
			if (last == 'x' || last == 'X') {
			setError("'" + t.getText() + "' is not a valid number");
			}
		}
		}

		firstIteration = false;
	}

	// Trim trailing whitespace
	int end = t.getText().length() - 1;
	while (end >= 0 && t.getText().charAt(end) <= ' ') {
		end--;
	}
	t.setText(t.getText().substring(0, end + 1));
	}

	return t;
}

public Token peekToken() {
	return peekToken(false, false);
}

public Token peekToken(boolean skipEOL, boolean dataMode) {

	// Save position
	int line = this.line, col = column;

	// Read token
	Token t = nextToken(skipEOL, dataMode);

	// Restore position.
	// (Except on error, when we leave the cursor pointing
	// to the error position.)
	if (!hasError()) {
	this.line = line;
	column = col;
	}

	return t;
}

// "dataMode" is set to true when reading elements of a "DATA" statement.
// (Slightly different parsing rules apply.)

// Special mode
public boolean isSpecialMode() {
	return isSpecialMode;
}

public void setSpecial(String text) {
	setSpecial(text, -1, -1);
}

public void setSpecial(String text, int line, int col) {
	isSpecialMode = true;
	specialText = text;
	specialColumn = 0;
	specialSourceColumn = col;
	if (line >= 0) {
	specialSourceLine = line;
	} else {
	specialSourceLine = this.line;
	}
	if (col >= 0) {
	specialSourceColumn = col;
	} else {
	specialSourceColumn = column;
	}
}

public void setNormal() {
	isSpecialMode = false;
}
}
