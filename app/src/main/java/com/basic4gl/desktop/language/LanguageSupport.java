package com.basic4gl.desktop.language;

import java.util.List;

/**
 * Plugin contract for a language definition.
 *
 * <p>A single implementation encapsulates everything the IDE needs to know about one language:
 *
 * <ul>
 *   <li>How to tokenize source text (for syntax highlighting)
 *   <li>How to classify each token into a portable {@link HighlightKind}
 *   <li>How to extract user-defined symbols from source (for the reference panel / indexer)
 * </ul>
 *
 * <p><strong>No RSyntaxTextArea or other UI framework types appear in this interface.</strong>
 * Adapters that bridge to a specific UI toolkit ({@code LanguageSupportTokenMaker} for
 * RSyntaxTextArea, a future LSP adapter, etc.) hold a reference to a {@code LanguageSupport}
 * and translate its output into whatever the toolkit requires.
 *
 * <p>Implementations are expected to be thread-safe: {@link #tokenizeLine} and
 * {@link #extractSymbols} may be called concurrently from both the EDT and background threads.
 */
public interface LanguageSupport {

    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------

    /**
     * The MIME-type style string used to register this language with RSyntaxTextArea's
     * {@code TokenMakerFactory} (e.g. {@code "text/basic4gl"}).
     *
     * <p>The value is opaque to the core indexer but consumed by the RSyntaxTextArea adapter.
     */
    String syntaxStyle();

    // -------------------------------------------------------------------------
    // Tokenisation
    // -------------------------------------------------------------------------

    /**
     * Tokenizes a single line of source text.
     *
     * <p>The returned list contains all tokens in left-to-right order. {@link LangToken#start}
     * and {@link LangToken#end} are 0-based character offsets within {@code line}.
     *
     * <p>Implementations must not return {@code null}; an empty line may return an empty list.
     *
     * @param line a single line of source (no {@code \n})
     * @return ordered, non-null token list
     */
    List<LangToken> tokenizeLine(String line);

    /**
     * Maps an implementation-specific {@link LangToken#type()} to a portable
     * {@link HighlightKind}.
     *
     * <p>This is the only place where the internal token type integers are interpreted.
     * All other code works with {@link HighlightKind} values.
     *
     * @param token a token previously produced by {@link #tokenizeLine}
     * @return the semantic highlight category; never {@code null}
     */
    HighlightKind classify(LangToken token);

    // -------------------------------------------------------------------------
    // Symbol extraction
    // -------------------------------------------------------------------------

    /**
     * Scans the full source text (which may span multiple concatenated files) and returns every
     * user-defined symbol it can discover.
     *
     * <p>This method is called from a background thread by the {@code SymbolIndexer} after each
     * debounce cycle; it must not touch Swing components.
     *
     * @param source full program source text
     * @return discovered symbols; never {@code null}
     */
    List<IndexedSymbol> extractSymbols(String source);
}
