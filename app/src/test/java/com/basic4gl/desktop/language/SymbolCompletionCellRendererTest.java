package com.basic4gl.desktop.language;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.swing.JList;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.VariableCompletion;
import org.junit.Test;

/**
 * {@link SymbolCompletionProvider} never populates {@link FunctionCompletion}'s parameter list or
 * {@link VariableCompletion}'s type (the signature is one opaque string), so the base {@code
 * CompletionCellRenderer} would render a bare name for these two kinds. Verifies the override
 * instead surfaces the short description (the full signature) inline in the row text.
 */
public class SymbolCompletionCellRendererTest {

    private final CompletionProvider provider = new DefaultCompletionProvider();
    private final SymbolCompletionCellRenderer renderer = new SymbolCompletionCellRenderer();
    private final JList<Object> list = new JList<>();

    @Test
    public void functionCompletion_rendersFullSignature() {
        FunctionCompletion completion = new FunctionCompletion(provider, "MyFunc", "");
        completion.setShortDescription("MyFunc(x As Integer, y As String) As Integer");

        renderer.getListCellRendererComponent(list, completion, 0, false, false);

        assertTrue(renderer.getText().contains("MyFunc"));
        assertTrue(renderer.getText().contains("MyFunc(x As Integer, y As String) As Integer"));
    }

    @Test
    public void variableCompletion_rendersFullSignature() {
        VariableCompletion completion = new VariableCompletion(provider, "Bar", "");
        completion.setShortDescription("Bar as integer");

        renderer.getListCellRendererComponent(list, completion, 0, false, false);

        assertTrue(renderer.getText().contains("Bar"));
        assertTrue(renderer.getText().contains("Bar as integer"));
    }

    /**
     * A completion with no short description at all (e.g. a keyword/type/label/struc, which {@link
     * SymbolCompletionProvider} deliberately never attaches a redundant description to - see its
     * {@code toCompletion} methods) should render as just its bare name, not "name - name" or
     * "name -" with an empty tail. This goes through the base class's unmodified {@code
     * prepareForOtherCompletion}, so it's really confirming the data layer's contract holds up
     * rather than any special rendering logic of our own.
     */
    @Test
    public void completionWithNoDescription_rendersBareName() {
        BasicCompletion keyword = new BasicCompletion(provider, "goto");

        renderer.getListCellRendererComponent(list, keyword, 0, false, false);

        assertTrue(renderer.getText().contains("goto"));
        assertFalse(renderer.getText().contains(" - "));
    }
}
