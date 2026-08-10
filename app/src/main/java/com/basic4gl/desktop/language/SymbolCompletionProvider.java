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
import org.fife.ui.autocomplete.ParameterizedCompletion;
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
        // Without this, getParameterListStart() stays 0 (unconfigured) and
        // AbstractCompletionProvider#getParameterizedCompletions bails out immediately - the
        // parameter-assistance popup never triggers off a typed '(', no matter how it's populated.
        setParameterizedCompletionParams('(', ", ", ')');
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
        String kind = normalizeKind(proposal.kind());
        Icon icon = kindIcons.get(kind);

        if ("userfunc".equals(kind)) {
            // Populated (rather than left empty) so the editor's parameter-assistance popup has
            // real parameters to show when the user types the function's opening paren.
            FunctionCompletion completion = new FunctionCompletion(this, text, "");
            completion.setParams(toParameters(proposal.parameters()));
            completion.setShortDescription(proposal.summary());
            completion.setSummary(proposal.summary());
            completion.setIcon(icon);
            return completion;
        }
        // No fallback to text() as a shortDescription here: a row with nothing distinguishing it
        // beyond its own name (e.g. a bare keyword or type) should show just the name, not the
        // name a second time as a "description".
        BasicCompletion completion = isBlank(proposal.summary())
                ? new BasicCompletion(this, text)
                : new BasicCompletion(this, text, proposal.summary());
        completion.setIcon(icon);
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
        String signature = symbol.signature();
        Icon icon = kindIcons.get(kind);

        switch (kind) {
            case "userfunc" -> {
                FunctionCompletion completion = new FunctionCompletion(this, name, "");
                // Prefer the language's own structured parameters (correct even for type-suffix
                // params like x$/x%/x# with no explicit "as Type"). Fall back to best-effort
                // parsing of the flattened "Name(param as Type, ...)" display string only for a
                // LanguageSupport that hasn't been updated to populate IndexedSymbol#parameters.
                List<String> parameterSignatures = symbol.parameters();
                completion.setParams(
                        parameterSignatures.isEmpty()
                                ? parseUserFunctionParameters(signature == null ? "" : signature)
                                : toParameters(parameterSignatures));
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
                // struc, label, and any future kinds: the name plus the kind's icon already
                // conveys everything useful here (signature() is typically just the name itself,
                // e.g. "Foo:" or "struc Foo"), so no inline description is shown.
                BasicCompletion completion = new BasicCompletion(this, name);
                completion.setIcon(icon);
                return completion;
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Converts {@code "type name"}-formatted parameter strings (see {@link
     * CompletionProposal#parameters()}) into real {@link ParameterizedCompletion.Parameter}s.
     */
    private static List<ParameterizedCompletion.Parameter> toParameters(List<String> parameterSignatures) {
        List<ParameterizedCompletion.Parameter> params = new ArrayList<>(parameterSignatures.size());
        for (String parameterSignature : parameterSignatures) {
            params.add(toParameter(parameterSignature));
        }
        return params;
    }

    private static ParameterizedCompletion.Parameter toParameter(String parameterSignature) {
        String trimmed = parameterSignature.trim();
        int splitAt = trimmed.lastIndexOf(' ');
        if (splitAt < 0) {
            return new ParameterizedCompletion.Parameter(null, trimmed);
        }
        String type = trimmed.substring(0, splitAt).trim();
        String name = trimmed.substring(splitAt + 1).trim();
        return new ParameterizedCompletion.Parameter(type, name);
    }

    /**
     * Best-effort parse of a flattened {@code "Name(param1 as Type1, param2 as Type2)"} display
     * string back into real parameters. Returns an empty list (rather than throwing) for any
     * signature that doesn't match the expected shape, so a malformed/unusual signature just means
     * no parameter assistance rather than a broken completion.
     */
    private static List<ParameterizedCompletion.Parameter> parseUserFunctionParameters(String signature) {
        int open = signature.indexOf('(');
        int close = signature.lastIndexOf(')');
        if (open < 0 || close < open) {
            return List.of();
        }
        String inner = signature.substring(open + 1, close).trim();
        if (inner.isEmpty()) {
            return List.of();
        }
        List<ParameterizedCompletion.Parameter> params = new ArrayList<>();
        for (String part : inner.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int asIndex = trimmed.toLowerCase().indexOf(" as ");
            if (asIndex < 0) {
                params.add(new ParameterizedCompletion.Parameter(null, trimmed));
            } else {
                String name = trimmed.substring(0, asIndex).trim();
                String type = trimmed.substring(asIndex + 4).trim();
                params.add(new ParameterizedCompletion.Parameter(type, name));
            }
        }
        return params;
    }
}
