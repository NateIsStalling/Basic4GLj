package com.basic4gl.desktop.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.basic4gl.desktop.spi.language.CompletionContext;
import com.basic4gl.desktop.spi.language.CompletionProposal;
import com.basic4gl.desktop.spi.language.IndexedSymbol;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import org.fife.ui.autocomplete.Completion;
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

    private static JTextComponent textAreaWithCaretAtEnd(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setCaretPosition(text.length());
        return textArea;
    }
}
