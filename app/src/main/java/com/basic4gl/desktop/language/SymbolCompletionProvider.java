package com.basic4gl.desktop.language;

import com.basic4gl.desktop.spi.language.CompletionProposal;
import com.basic4gl.desktop.spi.language.IndexedSymbol;
import java.util.List;
import javax.swing.SwingUtilities;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;

/**
 * {@link DefaultCompletionProvider} kept in sync with the output of a {@link SymbolIndexer}.
 *
 * <p>The provider merges two completion sources:
 *
 * <ul>
 *   <li><strong>Base completions</strong> — the language's source-independent keywords and built-in
 *       types, supplied once via {@link #setBaseCompletions(List)}. These make code-completion work
 *       even in an empty program.
 *   <li><strong>Symbol completions</strong> — the user-defined functions, variables, structs and
 *       labels discovered in the current source, refreshed each debounce cycle via
 *       {@link #setSymbols(List)}.
 * </ul>
 *
 * <p>Because rebuilding clears every completion, the base set is retained and re-added on each
 * refresh so keyword completions survive re-indexing.
 *
 * <p>The provider contains no language-specific parsing logic. It merely translates the portable
 * {@link IndexedSymbol} and {@link CompletionProposal} records produced by a {@code LanguageSupport}
 * into autocomplete {@link Completion} instances, so swapping languages requires no changes here.
 */
public class SymbolCompletionProvider extends DefaultCompletionProvider {

    private List<CompletionProposal> baseProposals = List.of();
    private List<IndexedSymbol> symbols = List.of();

    public SymbolCompletionProvider() {
        // Auto-activation is gated by the provider: without this the popup never appears while
        // typing letters, regardless of AutoCompletion.setAutoActivationEnabled(true).
        setAutoActivationRules(true, null);
    }

    /**
     * Installs the language's fixed keyword/built-in completions (typically
     * {@code LanguageSupport.keywordCompletions()}). Retained across symbol refreshes.
     *
     * <p>Safe to call from any thread.
     *
     * @param proposals base proposals; {@code null} is treated as an empty list
     */
    public void setBaseCompletions(List<CompletionProposal> proposals) {
        List<CompletionProposal> copy = proposals == null ? List.of() : List.copyOf(proposals);
        runOnEdt(() -> {
            this.baseProposals = copy;
            rebuild();
        });
    }

    /**
     * Replaces the current symbol completions with completions derived from {@code symbols}.
     *
     * <p>Safe to call from any thread; the rebuild is marshalled onto the Swing EDT because the
     * underlying provider is read by the autocomplete popup on the EDT. When invoked from the EDT
     * the update is applied synchronously.
     *
     * @param symbols the latest indexed symbols; {@code null} is treated as an empty list
     */
    public void setSymbols(List<IndexedSymbol> symbols) {
        List<IndexedSymbol> copy = symbols == null ? List.of() : List.copyOf(symbols);
        runOnEdt(() -> {
            this.symbols = copy;
            rebuild();
        });
    }

    private void runOnEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    private void rebuild() {
        clear();
        for (CompletionProposal proposal : baseProposals) {
            Completion completion = toCompletion(proposal);
            if (completion != null) {
                addCompletion(completion);
            }
        }
        for (IndexedSymbol symbol : symbols) {
            Completion completion = toCompletion(symbol);
            if (completion != null) {
                addCompletion(completion);
            }
        }
    }

    private Completion toCompletion(CompletionProposal proposal) {
        if (proposal == null) {
            return null;
        }
        String text = proposal.text();
        if (text == null || text.isEmpty()) {
            return null;
        }
        String summary = proposal.summary() == null || proposal.summary().isEmpty() ? text : proposal.summary();
        return new BasicCompletion(this, text, summary);
    }

    private Completion toCompletion(IndexedSymbol symbol) {
        if (symbol == null) {
            return null;
        }
        String name = symbol.name();
        if (name == null || name.isEmpty()) {
            return null;
        }
        String kind = symbol.kind() == null ? "" : symbol.kind();
        String signature = symbol.signature() == null || symbol.signature().isEmpty() ? name : symbol.signature();

        switch (kind) {
            case "userfunc" -> {
                FunctionCompletion completion = new FunctionCompletion(this, name, "");
                completion.setShortDescription(signature);
                completion.setSummary(signature);
                return completion;
            }
            case "variable" -> {
                VariableCompletion completion = new VariableCompletion(this, name, "");
                completion.setShortDescription(signature);
                completion.setSummary(signature);
                return completion;
            }
            default -> {
                // struc, label, and any future kinds insert the bare name and surface the
                // signature as the description.
                return new BasicCompletion(this, name, signature);
            }
        }
    }
}
