package com.basic4gl.desktop.language;

/**
 * Language-neutral semantic categories used to classify tokens for syntax highlighting.
 *
 * <p>A {@link LanguageSupport} implementation maps its internal token types to these categories.
 * An IDE adapter (e.g. {@code LanguageSupportTokenMaker} for RSyntaxTextArea) then maps these
 * categories to the syntax-highlighting primitives of whichever UI toolkit it targets.
 *
 * <p>No RSyntaxTextArea or other UI dependency lives in this enum.
 */
public enum HighlightKind {
    /** Language reserved words (if, while, for, function, dim, …). */
    KEYWORD,

    /**
     * Secondary reserved words – typically type names (integer, string, single, …) or boolean
     * literals (true, false).
     */
    KEYWORD_2,

    /** Built-in library function names resolved at runtime (DrawLine, PrintString, …). */
    FUNCTION,

    /** Named constant (resolved via {@code wordsToHighlight} by the IDE adapter). */
    CONSTANT,

    /** User-written identifier not otherwise classified. */
    IDENTIFIER,

    /** String literal. */
    STRING,

    /** Numeric literal (integer, float, hex). */
    NUMBER,

    /** Comment token (single-line comment or rem). */
    COMMENT,

    /** Preprocessor directive (e.g. {@code #include}). */
    PREPROCESSOR,

    /** Operator or punctuation symbol. */
    OPERATOR,

    /** Horizontal / inline whitespace. */
    WHITESPACE,

    /** Newline character(s). */
    NEWLINE,

    /** Anything else – used as a safe fallback so no token is silently dropped. */
    OTHER,
}
