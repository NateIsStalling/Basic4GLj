package com.basic4gl.desktop.spi.language;

import java.util.List;

/**
 * A user-defined symbol discovered by {@link com.basic4gl.desktop.spi.language.LanguageSupport#extractSymbols}.
 *
 * @param kind One of {@code "userfunc"}, {@code "label"}, or {@code "variable"}.
 * @param name The bare symbol name (no punctuation).
 * @param signature Human-readable signature shown in the reference panel.
 * @param parameters For {@code "userfunc"} symbols, each parameter formatted as {@code "type
 *     name"} (e.g. {@code "string x$"}), with the type already resolved by the language (including
 *     any type-suffix inference, e.g. {@code $}/{@code %}/{@code #}); empty for a zero-argument
 *     function or any other kind. Lets the editor populate real parameter-assistance data instead
 *     of re-parsing {@link #signature()}.
 */
public record IndexedSymbol(String kind, String name, String signature, List<String> parameters) {

    public IndexedSymbol(String kind, String name, String signature) {
        this(kind, name, signature, List.of());
    }
}
