package com.basic4gl.desktop.spi.language;

/**
 * A user-defined symbol discovered by {@link com.basic4gl.desktop.spi.language.LanguageSupport#extractSymbols}.
 *
 * @param kind One of {@code "userfunc"}, {@code "label"}, or {@code "variable"}.
 * @param name The bare symbol name (no punctuation).
 * @param signature Human-readable signature shown in the reference panel.
 */
public record IndexedSymbol(String kind, String name, String signature) {}
