package com.basic4gl.desktop.language;

import com.basic4gl.desktop.spi.language.CompletionContext;
import com.basic4gl.desktop.spi.language.CompletionProposal;
import com.basic4gl.desktop.spi.language.IndexedSymbol;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
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
 * <p>An optional {@linkplain #setContextResolver(Function) context resolver} lets the language
 * restrict which completion kinds are offered at the caret (e.g. only labels after
 * {@code gosub}/{@code goto}). The resolver is consulted on each completion request and its verdict
 * filters the merged set by kind.
 *
 * <p>An optional {@linkplain #setKindIcons(Map) kind-to-icon map} is applied to each completion as
 * it's built, so the popup list can show a distinct icon per kind (e.g. function vs. variable vs.
 * label) via {@link SymbolCompletionCellRenderer}.
 *
 * <p>The provider contains no language-specific parsing logic. It merely translates the portable
 * {@link IndexedSymbol} and {@link CompletionProposal} records produced by a {@code LanguageSupport}
 * into autocomplete {@link Completion} instances, so swapping languages requires no changes here.
 */
public class SymbolCompletionProvider extends DefaultCompletionProvider {

    private List<CompletionProposal> baseProposals = List.of();
    private List<IndexedSymbol> symbols = List.of();
    private Map<String, Icon> kindIcons = Map.of();

    // Kind ("keyword", "label", "variable", …) of each live completion, used for context filtering.
    // Accessed only on the EDT (rebuild and completion requests both run there).
    private final Map<Completion, String> kindByCompletion = new IdentityHashMap<>();

    // Maps the source text before the caret to the kinds allowed there; defaults to unrestricted.
    private Function<String, CompletionContext> contextResolver = text -> CompletionContext.ANY;

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

    /**
     * Installs a resolver that maps the source text preceding the caret to the set of completion
     * kinds permitted there (typically {@code LanguageSupport::completionContext}). When set, the
     * offered completions are filtered by kind on every request.
     *
     * @param resolver context resolver; {@code null} disables contextual filtering
     */
    public void setContextResolver(Function<String, CompletionContext> resolver) {
        this.contextResolver = resolver == null ? text -> CompletionContext.ANY : resolver;
    }

    /**
     * Installs the icon to show for each completion kind (e.g. {@code "variable"}, {@code
     * "userfunc"}). Applied when completions are (re)built; kinds absent from the map render with
     * no icon.
     *
     * <p>Safe to call from any thread.
     *
     * @param icons icon per kind; {@code null} is treated as an empty map
     */
    public void setKindIcons(Map<String, Icon> icons) {
        Map<String, Icon> copy = icons == null ? Map.of() : Map.copyOf(icons);
        runOnEdt(() -> {
            this.kindIcons = copy;
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
        kindByCompletion.clear();
        for (CompletionProposal proposal : baseProposals) {
            Completion completion = toCompletion(proposal);
            if (completion != null) {
                addCompletion(completion);
                kindByCompletion.put(completion, normalizeKind(proposal.kind()));
            }
        }
        for (IndexedSymbol symbol : symbols) {
            Completion completion = toCompletion(symbol);
            if (completion != null) {
                addCompletion(completion);
                kindByCompletion.put(completion, normalizeKind(symbol.kind()));
            }
        }
    }

    private static String normalizeKind(String kind) {
        return kind == null ? "" : kind;
    }

    /**
     * Filters the framework's prefix-matched completions by the current caret context so, for
     * example, only labels survive after {@code gosub}/{@code goto}.
     */
    @Override
    protected List<Completion> getCompletionsImpl(JTextComponent comp) {
        List<Completion> matches = super.getCompletionsImpl(comp);
        CompletionContext context = resolveContext(comp);
        if (context == null || context.allowsAll() || matches.isEmpty()) {
            return matches;
        }
        List<Completion> filtered = new ArrayList<>(matches.size());
        for (Completion completion : matches) {
            String kind = kindByCompletion.get(completion);
            if (kind != null && context.allows(kind)) {
                filtered.add(completion);
            }
        }
        return filtered;
    }

    private CompletionContext resolveContext(JTextComponent comp) {
        try {
            int caret = comp.getCaretPosition();
            String textBeforeCaret = comp.getDocument().getText(0, caret);
            return contextResolver.apply(textBeforeCaret);
        } catch (BadLocationException e) {
            return CompletionContext.ANY;
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
        BasicCompletion completion = new BasicCompletion(this, text, summary);
        completion.setIcon(kindIcons.get(normalizeKind(proposal.kind())));
        return completion;
    }

    private Completion toCompletion(IndexedSymbol symbol) {
        if (symbol == null) {
            return null;
        }
        String name = symbol.name();
        if (name == null || name.isEmpty()) {
            return null;
        }
        String kind = normalizeKind(symbol.kind());
        String signature = symbol.signature() == null || symbol.signature().isEmpty() ? name : symbol.signature();
        Icon icon = kindIcons.get(kind);

        switch (kind) {
            case "userfunc" -> {
                FunctionCompletion completion = new FunctionCompletion(this, name, "");
                completion.setShortDescription(signature);
                completion.setSummary(signature);
                completion.setIcon(icon);
                return completion;
            }
            case "variable" -> {
                VariableCompletion completion = new VariableCompletion(this, name, "");
                completion.setShortDescription(signature);
                completion.setSummary(signature);
                completion.setIcon(icon);
                return completion;
            }
            default -> {
                // struc, label, and any future kinds insert the bare name and surface the
                // signature as the description.
                BasicCompletion completion = new BasicCompletion(this, name, signature);
                completion.setIcon(icon);
                return completion;
            }
        }
    }
}
