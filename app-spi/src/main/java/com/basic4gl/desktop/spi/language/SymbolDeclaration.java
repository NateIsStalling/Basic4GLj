package com.basic4gl.desktop.spi.language;

/**
 * A concrete declaration site discovered by a language support implementation.
 *
 * @param kind Declaration kind, e.g. {@code "label"}, {@code "variable"}, {@code "userfunc"}.
 * @param name Bare declared identifier.
 * @param signature Human-readable declaration signature.
 * @param scope Logical scope label, e.g. {@code "global"} or a routine name.
 * @param declarationIndex 1-based ordinal for repeated declarations of the same scoped symbol.
 * @param fileId Source file identifier supplied by caller (typically absolute file path).
 * @param line 0-based line number in file.
 * @param column 0-based column in line.
 */
public record SymbolDeclaration(
        String kind,
        String name,
        String signature,
        String scope,
        int declarationIndex,
        String fileId,
        int line,
        int column) {}
