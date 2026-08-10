package com.basic4gl.desktop.spi.language;

import java.util.Set;

/**
 * Describes which kinds of completion are relevant at a particular caret position.
 *
 * <p>Produced by {@link LanguageSupport#completionContext(String)} so the IDE can filter the merged
 * completion set to only the proposals that make sense in context — for example, offering only
 * labels immediately after {@code gosub}/{@code goto}, or only type names after {@code as}.
 *
 * <p>The {@code kind} strings correspond to {@link CompletionProposal#kind()} and
 * {@link IndexedSymbol#kind()} values (e.g. {@code "keyword"}, {@code "type"}, {@code "label"},
 * {@code "userfunc"}, {@code "variable"}, {@code "struc"}).
 *
 * @param allowedKinds The set of permitted completion kinds, or {@code null} to permit every kind.
 */
public record CompletionContext(Set<String> allowedKinds) {

    /** Permits completions of every kind (no contextual restriction). */
    public static final CompletionContext ANY = new CompletionContext(null);

    /**
     * Creates a context that permits only the given kinds.
     *
     * @param kinds the permitted completion kinds
     * @return a restricting context
     */
    public static CompletionContext of(String... kinds) {
        return new CompletionContext(Set.of(kinds));
    }

    /**
     * @param kind a completion kind
     * @return {@code true} if a completion of this kind should be offered in this context
     */
    public boolean allows(String kind) {
        return allowedKinds == null || allowedKinds.contains(kind);
    }

    /**
     * @return {@code true} if no contextual restriction applies (every kind is allowed)
     */
    public boolean allowsAll() {
        return allowedKinds == null;
    }
}
