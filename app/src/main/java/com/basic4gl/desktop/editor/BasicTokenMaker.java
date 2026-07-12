package com.basic4gl.desktop.editor;

import com.basic4gl.language.adapter.Basic4GLLanguageSupport;
import java.util.ArrayList;
import java.util.List;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

/**
 * RSyntaxTextArea {@code TokenMaker} for the Basic4GL language.
 *
 * <p>All tokenisation logic now lives in {@link Basic4GLLanguageSupport} (backed by the
 * ANTLR-generated {@code Basic4GL} lexer). This class is retained so that:
 *
 * <ul>
 *   <li>The existing {@code "text/basic4gl"} MIME-type registration in {@code MainWindow} keeps
 *       working without change (RSyntaxTextArea instantiates this class by name).
 *   <li>The static keyword lists populated by {@code BasicEditor} at startup continue to produce
 *       correct highlighting for runtime-registered library functions and constants.
 * </ul>
 */
public class BasicTokenMaker extends LanguageSupportTokenMaker {

    private static final String INCLUDE = "include ";
    private static final String PLUGIN = "#plugin ";
    private static final char CHAR_COMMENT = '\'';

    // Static lists populated by BasicEditor after the compiler loads its libraries.
    // Grammar-level keywords are now handled by the ANTLR lexer; these lists are only
    // needed for dynamically registered names (library functions, constants, operators).
    public static final List<String> reservedWords = new ArrayList<>();
    public static final List<String> functions = new ArrayList<>();
    public static final List<String> constants = new ArrayList<>();
    public static final List<String> operators = new ArrayList<>();

    /** No-arg constructor used by RSyntaxTextArea's {@code TokenMakerFactory} via reflection. */
    public BasicTokenMaker() {
        super(new Basic4GLLanguageSupport());
    }

    /**
     * Merges the runtime-registered keyword lists into the {@code wordsToHighlight} map that
     * {@link LanguageSupportTokenMaker} uses to re-classify {@code IDENTIFIER} tokens.
     *
     * <p>Called once by the superclass constructor; the static lists must be populated by
     * {@code BasicEditor} before the first editor tab is opened.
     */
    @Override
    public TokenMap getWordsToHighlight() {
        TokenMap tokenMap = new TokenMap(true);
        for (String token : reservedWords) {
            tokenMap.put(token, Token.RESERVED_WORD);
        }
        for (String token : functions) {
            tokenMap.put(token, Token.FUNCTION);
        }
        for (String token : constants) {
            tokenMap.put(token, Token.RESERVED_WORD_2);
        }
        for (String token : operators) {
            tokenMap.put(token, Token.OPERATOR);
        }
        return tokenMap;
    }
}
