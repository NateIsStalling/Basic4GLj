package com.basic4gl.desktop;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nate on 1/10/2015.
 */
public class BasicTokenMaker extends AbstractTokenMaker {
    static final String INCLUDE = "include ";
    static final char CHAR_COMMENT = '\'';
    public static List<String> mReservedWords = new ArrayList<String>();
    public static List<String> mFunctions = new ArrayList<String>();
    public static List<String> mConstants = new ArrayList<String>();
    public static List<String> mOperators = new ArrayList<String>();

    @Override
    public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {
        // This assumes all keywords, etc. were parsed as "identifiers."
        if (tokenType == Token.IDENTIFIER) {
            int value = wordsToHighlight.get(segment, start, end);
            if (value != -1) {
                tokenType = value;
            }
        }
        super.addToken(segment, start, end, tokenType, startOffset);
    }

    @Override
    public TokenMap getWordsToHighlight() {
        TokenMap tokenMap = new TokenMap(true);

        for (String token : mReservedWords)
            tokenMap.put(token, Token.RESERVED_WORD);

        for (String token : mFunctions)
            tokenMap.put(token, Token.FUNCTION);

        for (String token : mConstants)
            tokenMap.put(token, Token.RESERVED_WORD_2);

        for (String token : mOperators)
            tokenMap.put(token, Token.OPERATOR);


        return tokenMap;
    }

    @Override
    public Token getTokenList(Segment text, int startTokenType, int startOffset) {
        resetTokenList();

        char[] array = text.array;
        int offset = text.offset;
        int count = text.count;
        int end = offset + count;

        // Token starting offsets are always of the form:
        // 'startOffset + (currentTokenStart-offset)', but since startOffset and
        // offset are constant, tokens' starting positions become:
        // 'newStartOffset+currentTokenStart'.
        int newStartOffset = startOffset - offset;

        int currentTokenStart = offset;
        int currentTokenType = startTokenType;
        if (offset + INCLUDE.length() < end &&
                String.valueOf(array).toLowerCase().substring(offset, offset + INCLUDE.length()).startsWith(INCLUDE)) {
            currentTokenType = Token.PREPROCESSOR;
        }
        for (int i = offset; i < end; i++) {

            char c = array[i];

            switch (currentTokenType) {
                case Token.NULL:

                    currentTokenStart = i;   // Starting a new token here.

                    switch (c) {
                        case ':':
                        case ' ':
                        case '\t':
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        case CHAR_COMMENT:
                            currentTokenType = Token.COMMENT_EOL;
                            break;
                        case '.':
                        case ',':
                        case ';':
                        case '/':
                        case '\\':
                        case '|':
                        case '{':
                        case '}':
                        case '[':
                        case ']':
                        case '(':
                        case ')':
                        case '<':
                        case '>':
                        case '-':
                        case '+':
                        case '*':
                        case '%':
                        case '@':
                        case '!':
                        case '~':
                        case '^':
                        case '?':
                            currentTokenType = Token.OPERATOR;
                            break;
                        default:
                            if (RSyntaxUtilities.isDigit(c)) {
                                currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                                break;
                            } else if (RSyntaxUtilities.isLetter(c) || c == '_') {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }

                            // Anything not currently handled - mark as an identifier
                            currentTokenType = Token.IDENTIFIER;
                            break;

                    } // End of switch (c).

                    break;

                case Token.WHITESPACE:

                    switch (c) {
                        case ':':
                        case ' ':
                        case '\t':
                            break;   // Still whitespace.

                        case '"':
                            addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        case CHAR_COMMENT:
                            addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.COMMENT_EOL;
                            break;

                        case '.':
                        case ',':
                        case ';':
                        case '/':
                        case '\\':
                        case '|':
                        case '{':
                        case '}':
                        case '[':
                        case ']':
                        case '(':
                        case ')':
                        case '<':
                        case '>':
                        case '-':
                        case '+':
                        case '*':
                        case '%':
                        case '@':
                        case '!':
                        case '~':
                        case '^':
                        case '?':
                            addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.OPERATOR;
                            break;
                        default:   // Add the whitespace token and start anew.

                            addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
                            currentTokenStart = i;

                            if (RSyntaxUtilities.isDigit(c)) {
                                currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                                break;
                            } else if (RSyntaxUtilities.isLetter(c) || c == '_') {
                                currentTokenType = Token.IDENTIFIER;
                                break;
                            }

                            // Anything not currently handled - mark as identifier
                            currentTokenType = Token.IDENTIFIER;

                    } // End of switch (c).

                    break;

                default: // Should never happen
                case Token.IDENTIFIER:

                    switch (c) {
                        case ':':
                        case ' ':
                        case '\t':
                            addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;
                        case '\'':
                            addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.COMMENT_EOL;
                            break;
                        case '.':
                        case ',':
                        case ';':
                        case '/':
                        case '\\':
                        case '|':
                        case '{':
                        case '}':
                        case '[':
                        case ']':
                        case '(':
                        case ')':
                        case '<':
                        case '>':
                        case '-':
                        case '+':
                        case '*':
                        case '%':
                        case '@':
                        case '!':
                        case '~':
                        case '^':
                        case '?':
                            addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.OPERATOR;
                            break;
                        default:
                            if (RSyntaxUtilities.isLetterOrDigit(c) || c == '_') {
                                break;   // Still an identifier of some type.
                            }
                            // Otherwise, we're still an identifier (?).

                    } // End of switch (c).

                    break;

                case Token.LITERAL_NUMBER_DECIMAL_INT:

                    switch (c) {

                        case ':':
                        case ' ':
                        case '\t':
                            addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;
                        case '.':
                        case ',':
                        case ';':
                        case '/':
                        case '\\':
                        case '|':
                        case '{':
                        case '}':
                        case '[':
                        case ']':
                        case '(':
                        case ')':
                        case '<':
                        case '>':
                        case '-':
                        case '+':
                        case '*':
                        case '%':
                        case '@':
                        case '!':
                        case '~':
                        case '^':
                        case '?':
                            addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.OPERATOR;
                            break;
                        default:

                            if (RSyntaxUtilities.isDigit(c)) {
                                break;   // Still a literal number.
                            }

                            // Otherwise, remember this was a number and start over.
                            addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
                            i--;
                            currentTokenType = Token.NULL;

                    } // End of switch (c).

                    break;
                case Token.OPERATOR:
                    switch (c) {
                        case ':':
                        case ' ':
                        case '\t':
                            addToken(text, currentTokenStart, i - 1, Token.OPERATOR, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.WHITESPACE;
                            break;

                        case '"':
                            addToken(text, currentTokenStart, i - 1, Token.OPERATOR, newStartOffset + currentTokenStart);
                            currentTokenStart = i;
                            currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
                            break;

                        case '.':
                        case ',':
                        case ';':
                        case '/':
                        case '\\':
                        case '|':
                        case '{':
                        case '}':
                        case '[':
                        case ']':
                        case '(':
                        case ')':
                        case '<':
                        case '>':
                        case '-':
                        case '+':
                        case '*':
                        case '%':
                        case '@':
                        case '!':
                        case '~':
                        case '^':
                        case '?':
                            //Still an operator
                            break;
                        default:
                            if (RSyntaxUtilities.isDigit(c)) {
                                addToken(text, currentTokenStart, i - 1, Token.OPERATOR, newStartOffset + currentTokenStart);
                                currentTokenStart = i;
                                currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                                break;   // A literal number.
                            }
                            if (RSyntaxUtilities.isLetter(c) || c == '_') {
                                addToken(text, currentTokenStart, i - 1, Token.OPERATOR, newStartOffset + currentTokenStart);
                                currentTokenStart = i;
                                currentTokenType = Token.IDENTIFIER;
                                break;   // An identifier of some type.
                            }
                            break;

                    }

                    break;
                case Token.PREPROCESSOR:
                    //Preprocessor goes till EOL
                    break;
                case Token.COMMENT_EOL:
                    i = end - 1;
                    addToken(text, currentTokenStart, i, currentTokenType, newStartOffset + currentTokenStart);
                    // We need to set token type to null so at the bottom we don't add one more token.
                    currentTokenType = Token.NULL;
                    break;

                case Token.LITERAL_STRING_DOUBLE_QUOTE:
                    if (c == '"') {
                        addToken(text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset + currentTokenStart);
                        currentTokenType = Token.NULL;
                    }
                    break;

            } // End of switch (currentTokenType).

        } // End of for (int i=offset; i<end; i++).

        switch (currentTokenType) {

            // Remember what token type to begin the next line with.
            case Token.LITERAL_STRING_DOUBLE_QUOTE:
                addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
                break;

            // Do nothing if everything was okay.
            case Token.NULL:
                addNullToken();
                break;

            // All other token types don't continue to the next line...
            default:
                addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
                addNullToken();

        }

        // Return the first token in our linked list.
        return firstToken;
    }
}
