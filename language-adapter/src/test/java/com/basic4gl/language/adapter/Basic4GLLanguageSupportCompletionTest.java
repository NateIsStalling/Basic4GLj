package com.basic4gl.language.adapter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.basic4gl.desktop.spi.language.CompletionContext;
import com.basic4gl.desktop.spi.language.CompletionProposal;
import java.util.List;
import org.junit.jupiter.api.Test;

public class Basic4GLLanguageSupportCompletionTest {

    private final Basic4GLLanguageSupport support = new Basic4GLLanguageSupport();

    @Test
    public void keywordCompletions_includeReservedWordsAndTypes() {
        List<CompletionProposal> proposals = support.keywordCompletions();

        assertTrue(containsText(proposals, "gosub"));
        assertTrue(containsText(proposals, "goto"));
        assertTrue(containsText(proposals, "function"));
        assertTrue(containsText(proposals, "integer"));
        assertTrue(containsText(proposals, "string"));
    }

    @Test
    public void completionContext_default_isUnrestricted() {
        assertTrue(support.completionContext("").allowsAll());
        assertTrue(support.completionContext("print ").allowsAll());
        assertTrue(support.completionContext("dim x").allowsAll());
    }

    @Test
    public void completionContext_afterGosub_allowsOnlyLabels() {
        CompletionContext context = support.completionContext("gosub ");

        assertFalse(context.allowsAll());
        assertTrue(context.allows("label"));
        assertFalse(context.allows("keyword"));
        assertFalse(context.allows("variable"));
    }

    @Test
    public void completionContext_whileTypingLabel_afterGoto_allowsOnlyLabels() {
        CompletionContext context = support.completionContext("goto Che");

        assertTrue(context.allows("label"));
        assertFalse(context.allows("keyword"));
    }

    @Test
    public void completionContext_afterAs_allowsTypesAndStructs() {
        CompletionContext context = support.completionContext("dim x as ");

        assertFalse(context.allowsAll());
        assertTrue(context.allows("type"));
        assertTrue(context.allows("struc"));
        assertFalse(context.allows("keyword"));
        assertFalse(context.allows("label"));
    }

    @Test
    public void completionContext_isPerLine() {
        // The gosub is on a previous line; the current (blank) line imposes no restriction.
        assertTrue(support.completionContext("gosub Foo\n").allowsAll());
    }

    private static boolean containsText(List<CompletionProposal> proposals, String text) {
        return proposals.stream().anyMatch(p -> text.equals(p.text()));
    }
}
