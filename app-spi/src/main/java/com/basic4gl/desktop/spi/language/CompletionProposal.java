package com.basic4gl.desktop.spi.language;

import java.util.List;

/**
 * A portable code-completion proposal produced by a {@link LanguageSupport}.
 *
 * <p>This record is intentionally free of any UI-toolkit types. The IDE adapter translates each
 * proposal into whatever the completion framework requires (e.g. an autocomplete
 * {@code Completion}), so a language plugin never depends on RSyntaxTextArea or the autocomplete
 * library.
 *
 * @param kind Proposal category, e.g. {@code "keyword"}, {@code "type"}, {@code "userfunc"},
 *     {@code "variable"}, {@code "label"}. The adapter uses this to pick an appropriate completion
 *     style; unknown kinds fall back to a plain text completion.
 * @param text The text inserted when the proposal is accepted.
 * @param summary Human-readable description shown alongside the proposal; may be {@code null} or
 *     empty, in which case the adapter substitutes {@link #text()}.
 * @param parameters For {@code "userfunc"} proposals, each parameter formatted as {@code "type
 *     name"} (e.g. {@code "integer arg1"}); empty for a zero-argument function or any other kind.
 *     Lets the adapter populate a real parameter list so the editor's function-signature/parameter
 *     assistance has something to show, instead of only a flattened description string.
 */
public record CompletionProposal(String kind, String text, String summary, List<String> parameters) {

    public CompletionProposal(String kind, String text, String summary) {
        this(kind, text, summary, List.of());
    }

    public CompletionProposal(String kind, String text) {
        this(kind, text, null, List.of());
    }
}
