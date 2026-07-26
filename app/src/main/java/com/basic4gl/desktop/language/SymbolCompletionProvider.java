package com.basic4gl.desktop.language;

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
 * <p>Each debounce cycle the indexer delivers a fresh {@link IndexedSymbol} list; feeding that list
 * to {@link #setSymbols(List)} rebuilds the completion set so code-completion always reflects the
 * user-defined functions, variables, structs and labels currently declared in the open source.
 *
 * <p>The provider contains no language-specific parsing logic. It merely translates the portable
 * {@link IndexedSymbol} records produced by a {@code LanguageSupport} into autocomplete
 * {@link Completion} instances, so swapping languages requires no changes here.
 */
public class SymbolCompletionProvider extends DefaultCompletionProvider {

    public SymbolCompletionProvider() {
        // Auto-activation is gated by the provider: without this the popup never appears while
        // typing letters, regardless of AutoCompletion.setAutoActivationEnabled(true).
        setAutoActivationRules(true, null);
    }

    /**
     * Replaces the current completion set with completions derived from {@code symbols}.
     *
     * <p>Safe to call from any thread; the rebuild is marshalled onto the Swing EDT because the
     * underlying provider is read by the autocomplete popup on the EDT. When invoked from the EDT
     * the update is applied synchronously.
     *
     * @param symbols the latest indexed symbols; {@code null} is treated as an empty list
     */
    public void setSymbols(List<IndexedSymbol> symbols) {
        if (SwingUtilities.isEventDispatchThread()) {
            rebuild(symbols);
        } else {
            SwingUtilities.invokeLater(() -> rebuild(symbols));
        }
    }

    private void rebuild(List<IndexedSymbol> symbols) {
        clear();
        if (symbols == null) {
            return;
        }
        for (IndexedSymbol symbol : symbols) {
            Completion completion = toCompletion(symbol);
            if (completion != null) {
                addCompletion(completion);
            }
        }
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
