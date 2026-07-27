package com.basic4gl.desktop.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.basic4gl.desktop.spi.language.CompletionContext;
import com.basic4gl.desktop.spi.language.CompletionProposal;
import com.basic4gl.desktop.spi.language.IndexedSymbol;
import java.awt.Component;
import java.awt.Graphics;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.junit.Test;

/**
 * Exercises the Swing-integration piece of contextual completion: that {@link
 * SymbolCompletionProvider} actually narrows its offered completions using a caret-position context
 * resolver, on top of the framework's own prefix matching.
 */
public class SymbolCompletionProviderTest {

    @Test
    public void gosub_offersOnlyIndexedLabels() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setBaseCompletions(
                List.of(new CompletionProposal("keyword", "gosub"), new CompletionProposal("keyword", "goto")));
        provider.setSymbols(List.of(
                new IndexedSymbol("label", "Foo", "Foo:"), new IndexedSymbol("variable", "Bar", "Bar as integer")));
        provider.setContextResolver(
                text -> text.endsWith("gosub ") ? CompletionContext.of("label") : CompletionContext.ANY);

        List<Completion> completions = provider.getCompletions(textAreaWithCaretAtEnd("gosub "));

        assertEquals(1, completions.size());
        assertEquals("Foo", completions.get(0).getInputText());
    }

    @Test
    public void unrestrictedContext_stillOffersKeywordsAndSymbols() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setBaseCompletions(List.of(new CompletionProposal("keyword", "gosub")));
        provider.setSymbols(List.of(new IndexedSymbol("label", "Foo", "Foo:")));
        provider.setContextResolver(text -> CompletionContext.ANY);

        List<Completion> completions = provider.getCompletions(textAreaWithCaretAtEnd(""));

        assertTrue(completions.stream().anyMatch(c -> "gosub".equals(c.getInputText())));
        assertTrue(completions.stream().anyMatch(c -> "Foo".equals(c.getInputText())));
    }

    @Test
    public void restrictedContext_withNoMatchingSymbols_offersNothing() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setBaseCompletions(List.of(new CompletionProposal("keyword", "gosub")));
        provider.setSymbols(List.of(new IndexedSymbol("variable", "Bar", "Bar as integer")));
        provider.setContextResolver(text -> CompletionContext.of("label"));

        List<Completion> completions = provider.getCompletions(textAreaWithCaretAtEnd(""));

        assertFalse(completions.stream().anyMatch(c -> "gosub".equals(c.getInputText())));
        assertFalse(completions.stream().anyMatch(c -> "Bar".equals(c.getInputText())));
    }

    @Test
    public void kindIcons_areAppliedToMatchingCompletions() {
        Icon labelIcon = noopIcon();
        Icon variableIcon = noopIcon();

        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setKindIcons(Map.of("label", labelIcon, "variable", variableIcon));
        provider.setSymbols(List.of(
                new IndexedSymbol("label", "Foo", "Foo:"),
                new IndexedSymbol("variable", "Bar", "Bar as integer"),
                new IndexedSymbol("struc", "Baz", "struc Baz")));

        List<Completion> completions = provider.getCompletions(textAreaWithCaretAtEnd(""));

        assertEquals(labelIcon, completionNamed(completions, "Foo").getIcon());
        assertEquals(variableIcon, completionNamed(completions, "Bar").getIcon());
        assertEquals(null, completionNamed(completions, "Baz").getIcon());
    }

    /**
     * Regression test: builtin library functions used to be built as bare {@code BasicCompletion}s
     * with no structured parameters, so the editor's parameter-assistance popup had nothing to show
     * even when the "Show function signatures" setting was enabled.
     */
    @Test
    public void builtinFunctionProposal_populatesRealParameters() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setBaseCompletions(
                List.of(new CompletionProposal("userfunc", "Sin", "real Sin(real arg1)", List.of("real arg1"))));

        Completion completion = completionNamed(provider.getCompletions(textAreaWithCaretAtEnd("")), "Sin");

        assertTrue(completion instanceof FunctionCompletion);
        FunctionCompletion function = (FunctionCompletion) completion;
        assertEquals(1, function.getParamCount());
        assertEquals("real", function.getParam(0).getType());
        assertEquals("arg1", function.getParam(0).getName());
    }

    @Test
    public void builtinFunctionProposal_withNoParameters_hasEmptyParamList() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setBaseCompletions(List.of(new CompletionProposal("userfunc", "Beep", "Beep", List.of())));

        Completion completion = completionNamed(provider.getCompletions(textAreaWithCaretAtEnd("")), "Beep");

        assertEquals(0, ((FunctionCompletion) completion).getParamCount());
    }

    /**
     * Regression test: {@code getParameterListStart()} defaults to {@code 0} (unconfigured), which
     * makes the library's {@code getParameterizedCompletions} bail out immediately - the
     * parameter-assistance popup never triggers off a typed '(' no matter what parameter data a
     * completion carries, unless the provider explicitly configures its parameter-list delimiters.
     */
    @Test
    public void parameterListDelimiters_areConfigured() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();

        assertEquals('(', provider.getParameterListStart());
        assertEquals(')', provider.getParameterListEnd());
        assertEquals(", ", provider.getParameterListSeparator());
    }

    /**
     * When the language populates {@link IndexedSymbol#parameters()} directly (the real path, via
     * Basic4GLLanguageSupport), that structured data must be used as-is rather than falling back to
     * re-parsing the display signature - this is what lets type-suffix parameters (x$/x%/x#) come
     * through correctly, since the flattened signature has no "as Type" clause to parse for them.
     */
    @Test
    public void userDefinedFunction_prefersStructuredParametersOverParsingSignature() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setSymbols(
                List.of(new IndexedSymbol("userfunc", "Foo", "Foo(x$, y%)", List.of("string x$", "integer y%"))));

        Completion completion = completionNamed(provider.getCompletions(textAreaWithCaretAtEnd("")), "Foo");

        FunctionCompletion function = (FunctionCompletion) completion;
        assertEquals(2, function.getParamCount());
        assertEquals("string", function.getParam(0).getType());
        assertEquals("x$", function.getParam(0).getName());
        assertEquals("integer", function.getParam(1).getType());
        assertEquals("y%", function.getParam(1).getName());
    }

    /**
     * Regression test: user-defined functions' {@code FunctionCompletion} used to have no
     * parameters set at all (only the flattened signature string as a description), so parameter
     * assistance had nothing to show for them either. Covers a {@code LanguageSupport} that hasn't
     * been updated to populate {@link IndexedSymbol#parameters()} - the provider falls back to
     * parsing the flattened signature string instead of offering no parameters at all.
     */
    @Test
    public void userDefinedFunction_parsesParametersFromFlattenedSignature() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setSymbols(List.of(new IndexedSymbol("userfunc", "MyFunc", "MyFunc(x as integer, y as string)")));

        Completion completion = completionNamed(provider.getCompletions(textAreaWithCaretAtEnd("")), "MyFunc");

        FunctionCompletion function = (FunctionCompletion) completion;
        assertEquals(2, function.getParamCount());
        assertEquals("integer", function.getParam(0).getType());
        assertEquals("x", function.getParam(0).getName());
        assertEquals("string", function.getParam(1).getType());
        assertEquals("y", function.getParam(1).getName());
    }

    @Test
    public void userDefinedFunction_withNoParameters_hasEmptyParamList() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setSymbols(List.of(new IndexedSymbol("userfunc", "DoThing", "DoThing()")));

        Completion completion = completionNamed(provider.getCompletions(textAreaWithCaretAtEnd("")), "DoThing");

        assertEquals(0, ((FunctionCompletion) completion).getParamCount());
    }

    @Test
    public void userDefinedFunction_withMalformedSignature_doesNotThrowAndHasNoParams() {
        SymbolCompletionProvider provider = new SymbolCompletionProvider();
        provider.setSymbols(List.of(new IndexedSymbol("userfunc", "Weird", "not a real signature")));

        Completion completion = completionNamed(provider.getCompletions(textAreaWithCaretAtEnd("")), "Weird");

        assertEquals(0, ((FunctionCompletion) completion).getParamCount());
    }

    private static Completion completionNamed(List<Completion> completions, String inputText) {
        return completions.stream()
                .filter(c -> inputText.equals(c.getInputText()))
                .findFirst()
                .orElseThrow();
    }

    private static Icon noopIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {}

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    private static JTextComponent textAreaWithCaretAtEnd(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setCaretPosition(text.length());
        return textArea;
    }
}
