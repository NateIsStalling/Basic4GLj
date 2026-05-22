package com.basic4gl.desktop.language;

/**
 * An immutable, language-neutral token produced by {@link LanguageSupport#tokenizeLine}.
 *
 * <p>Positions are 0-based character offsets within the line string passed to
 * {@code tokenizeLine}:
 *
 * <ul>
 *   <li>{@link #start()} – inclusive start offset
 *   <li>{@link #end()} – exclusive end offset (i.e. {@code line.substring(start, end)} == text)
 * </ul>
 *
 * <p>The {@link #type()} field carries the implementation-specific integer token type (e.g. an
 * ANTLR token type constant). It is opaque to callers; use
 * {@link LanguageSupport#classify(LangToken)} to obtain the portable {@link HighlightKind}.
 */
public record LangToken(int type, String text, int start, int end) {

    /** Convenience: returns {@code true} when this is the synthetic EOF sentinel. */
    public boolean isEof() {
        return type == -1; // matches org.antlr.v4.runtime.Token.EOF
    }

    /** Length in characters. */
    public int length() {
        return end - start;
    }
}
