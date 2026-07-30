package com.basic4gl.desktop.language;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.junit.Test;

/**
 * Regression test for a stuck-completion-popup feedback loop: {@code AutoCompletion}'s
 * auto-activation restarts its popup timer on any single-character document insert, including the
 * single-character insert produced by accepting a one-character completion (e.g. a variable named
 * {@code i}). Verifies {@link NonRetriggeringAutoCompletion} suppresses auto-activation for the
 * duration of that insert, then restores it on the next EDT cycle.
 */
public class NonRetriggeringAutoCompletionTest {

    @Test
    public void insertCompletion_disablesAutoActivationDuringInsertThenRestoresItAfterward() throws Exception {
        CompletionProvider provider = new DefaultCompletionProvider();
        ObservingAutoCompletion autoCompletion = new ObservingAutoCompletion(provider);
        autoCompletion.setAutoActivationEnabled(true);

        JTextArea textArea = new JTextArea("i");
        textArea.setCaretPosition(1);
        autoCompletion.install(textArea);

        BasicCompletion completion = new BasicCompletion(provider, "i");

        SwingUtilities.invokeAndWait(() -> autoCompletion.insertCompletion(completion, false));

        assertTrue(autoCompletion.observedSuppressedInsertion());

        // Pump the EDT so the scheduled invokeLater restore runs.
        SwingUtilities.invokeAndWait(() -> {});

        assertTrue(autoCompletion.isAutoActivationEnabled());
    }

    @Test
    public void insertCompletion_leavesAutoActivationOffIfItWasAlreadyOff() throws Exception {
        CompletionProvider provider = new DefaultCompletionProvider();
        NonRetriggeringAutoCompletion autoCompletion = new NonRetriggeringAutoCompletion(provider);
        autoCompletion.setAutoActivationEnabled(false);

        JTextArea textArea = new JTextArea("i");
        textArea.setCaretPosition(1);
        autoCompletion.install(textArea);

        BasicCompletion completion = new BasicCompletion(provider, "i");

        SwingUtilities.invokeAndWait(() -> autoCompletion.insertCompletion(completion, false));
        SwingUtilities.invokeAndWait(() -> {});

        assertFalse(autoCompletion.isAutoActivationEnabled());
    }

    private static class ObservingAutoCompletion extends NonRetriggeringAutoCompletion {
        private final AtomicBoolean observedSuppressedInsertion = new AtomicBoolean(false);

        ObservingAutoCompletion(CompletionProvider provider) {
            super(provider);
        }

        @Override
        protected String getReplacementText(Completion c, Document doc, int start, int len) {
            observedSuppressedInsertion.set(!isAutoActivationEnabled());
            return super.getReplacementText(c, doc, start, len);
        }

        private boolean observedSuppressedInsertion() {
            return observedSuppressedInsertion.get();
        }
    }
}
