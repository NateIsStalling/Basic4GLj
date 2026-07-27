package com.basic4gl.desktop.language;

import javax.swing.JList;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;

/**
 * Renders completion rows with their signature inline, so the popup carries the same detail that
 * would otherwise only appear in a separate description window.
 *
 * <p>{@link SymbolCompletionProvider} never populates {@link FunctionCompletion}'s structured
 * parameter list or {@link VariableCompletion}'s type (the signature is a single opaque string
 * supplied by the language's {@code LanguageSupport}), so the base class's parameter/type-based
 * rendering for those two completion kinds would otherwise show a bare name. This renderer falls
 * back to the short description text instead, matching how the base class already renders every
 * other completion kind (see {@code CompletionCellRenderer.prepareForOtherCompletion}).
 *
 * <p>Per-kind icons are not handled here: {@link SymbolCompletionProvider} sets each {@code
 * Completion}'s icon directly at construction time (see {@link SymbolCompletionProvider#setKindIcons}),
 * and the base class already renders whatever icon is present.
 *
 * <p>There is no toggle to hide this detail text: {@link SymbolCompletionProvider} only ever
 * attaches a description when it carries real information (a function's parameters, a variable's
 * type) and omits it entirely otherwise (keywords, types, labels, structs), so there is nothing to
 * hide - the name plus the row's icon already says everything for those kinds.
 */
public class SymbolCompletionCellRenderer extends CompletionCellRenderer {

    private static final String HTML_PREFIX = "<html><nobr>";

    @Override
    protected void prepareForFunctionCompletion(
            JList<?> list, FunctionCompletion fc, int index, boolean selected, boolean hasFocus) {
        setText(nameAndDescription(fc.getName(), fc.getShortDescription()));
    }

    @Override
    protected void prepareForVariableCompletion(
            JList<?> list, VariableCompletion vc, int index, boolean selected, boolean hasFocus) {
        setText(nameAndDescription(vc.getName(), vc.getShortDescription()));
    }

    private String nameAndDescription(String name, String shortDescription) {
        StringBuilder sb = new StringBuilder(HTML_PREFIX).append(name);
        if (shortDescription != null && !shortDescription.isEmpty()) {
            appendShortDescription(sb, shortDescription);
        }
        return sb.toString();
    }
}
