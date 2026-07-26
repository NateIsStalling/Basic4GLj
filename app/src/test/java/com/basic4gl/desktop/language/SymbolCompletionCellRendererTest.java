package com.basic4gl.desktop.language;

import static org.junit.Assert.assertTrue;

import javax.swing.JList;
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
}
