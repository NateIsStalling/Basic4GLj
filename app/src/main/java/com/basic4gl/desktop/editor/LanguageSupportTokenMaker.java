package com.basic4gl.desktop.editor;

import com.basic4gl.desktop.spi.language.HighlightKind;
import com.basic4gl.desktop.spi.language.LangToken;
import com.basic4gl.desktop.spi.language.LanguageSupport;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

/**
 * Generic RSyntaxTextArea {@link AbstractTokenMaker} that delegates all lexical analysis to a
 * pluggable {@link LanguageSupport} instance.
 *
 * <p>This is the <strong>only</strong> class in the IDE that imports RSyntaxTextArea types and
 * bridges them to the language-neutral {@code language} package. Swapping the language is a
 * one-line constructor change; no RSyntaxTextArea knowledge leaks into the language definition.
 *
 * <p>The {@link #getWordsToHighlight()} method still honours the runtime keyword maps populated
 * by {@code BasicEditor} (library function names, constant names, etc.) so that names registered
 * after startup are highlighted correctly without a grammar recompile.
 */
public class LanguageSupportTokenMaker extends AbstractTokenMaker {

    private static final int LINE_TOKEN_CACHE_SIZE = 512;

    private final LanguageSupport languageSupport;
    private final Map<String, List<LangToken>> lineTokenCache = new LinkedHashMap<>(128, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<LangToken>> eldest) {
            return size() > LINE_TOKEN_CACHE_SIZE;
        }
    };

    public LanguageSupportTokenMaker(LanguageSupport languageSupport) {
        this.languageSupport = languageSupport;
    }

    // -------------------------------------------------------------------------
    // AbstractTokenMaker contract
    // -------------------------------------------------------------------------

    /**
     * Returns an empty map by default; subclasses (e.g. {@link BasicTokenMaker}) override this to
     * register runtime-discovered names (library functions, constants) that should be highlighted
     * even though they are not in the grammar.
     */
    @Override
    public TokenMap getWordsToHighlight() {
        return new TokenMap(true);
    }

    @Override
    public Token getTokenList(Segment text, int startTokenType, int startOffset) {
        resetTokenList();

        // Handle a string literal that started on the previous line (Basic4GL strings are
        // technically single-line, but the editor may carry the state across line repaints).
        if (startTokenType == Token.LITERAL_STRING_DOUBLE_QUOTE) {
            int eol = text.offset + text.count;
            int closeQuote = -1;
            for (int i = text.offset; i < eol; i++) {
                if (text.array[i] == '"') {
                    closeQuote = i;
                    break;
                }
            }
            if (closeQuote >= 0) {
                // Emit the closing fragment of the string …
                addToken(text, text.offset, closeQuote, Token.LITERAL_STRING_DOUBLE_QUOTE, startOffset);
                // … then tokenize whatever follows normally
                int remaining = eol - closeQuote - 1;
                if (remaining > 0) {
                    String rest = new String(text.array, closeQuote + 1, remaining);
                    appendTokens(text, rest, closeQuote + 1, startOffset + closeQuote + 1 - text.offset);
                }
            } else {
                // The entire line is inside an unclosed string
                if (text.count > 0) {
                    addToken(
                            text,
                            text.offset,
                            text.offset + text.count - 1,
                            Token.LITERAL_STRING_DOUBLE_QUOTE,
                            startOffset);
                }
            }
            addNullToken();
            return firstToken;
        }

        // Normal case: tokenize the line via LanguageSupport
        if (text.count == 0) {
            addNullToken();
            return firstToken;
        }

        String lineText = new String(text.array, text.offset, text.count);
        appendTokens(text, lineText, text.offset, startOffset);
        addNullToken();
        return firstToken;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Tokenizes {@code lineText} and appends the resulting RSyntaxTextArea tokens to the linked
     * list.
     *
     * @param seg         the full line {@link Segment} (needed by {@link #addToken})
     * @param lineText    the string to tokenize (may be a sub-range of {@code seg})
     * @param arrayOffset offset into {@code seg.array} where {@code lineText} starts
     * @param docOffset   document-level character offset of the first character
     */
    private void appendTokens(Segment seg, String lineText, int arrayOffset, int docOffset) {
        List<LangToken> tokens;
        synchronized (lineTokenCache) {
            tokens = lineTokenCache.get(lineText);
        }
        if (tokens == null) {
            tokens = List.copyOf(languageSupport.tokenizeLine(lineText));
            synchronized (lineTokenCache) {
                lineTokenCache.put(lineText, tokens);
            }
        }

        for (LangToken lt : tokens) {
            HighlightKind kind = languageSupport.classify(lt);
            int rstaType = kindToRstaType(kind);

            int arrayStart = arrayOffset + lt.start();
            int arrayEnd = arrayOffset + lt.end() - 1; // inclusive

            // Re-classify IDENTIFIER tokens that appear in the runtime wordsToHighlight map
            // (e.g. library function names, constant names loaded from the compiler at startup).
            if (kind == HighlightKind.IDENTIFIER && wordsToHighlight != null) {
                int override = wordsToHighlight.get(seg, arrayStart, arrayEnd);
                if (override != -1) {
                    rstaType = override;
                }
            }

            if (arrayStart <= arrayEnd) {
                addToken(seg, arrayStart, arrayEnd, rstaType, docOffset + lt.start());
            }
        }
    }

    /**
     * Maps a {@link HighlightKind} to the corresponding RSyntaxTextArea {@link Token} type
     * constant. All RSyntaxTextArea coupling is isolated to this single switch statement.
     */
    private static int kindToRstaType(HighlightKind kind) {
        return switch (kind) {
            case KEYWORD -> Token.RESERVED_WORD;
            case KEYWORD_2 -> Token.RESERVED_WORD_2;
            case FUNCTION -> Token.FUNCTION;
            case CONSTANT -> Token.RESERVED_WORD_2;
            case IDENTIFIER -> Token.IDENTIFIER;
            case STRING -> Token.LITERAL_STRING_DOUBLE_QUOTE;
            case NUMBER -> Token.LITERAL_NUMBER_DECIMAL_INT;
            case COMMENT -> Token.COMMENT_EOL;
            case PREPROCESSOR -> Token.PREPROCESSOR;
            case OPERATOR -> Token.OPERATOR;
            case WHITESPACE, NEWLINE -> Token.WHITESPACE;
            default -> Token.IDENTIFIER;
        };
    }
}
