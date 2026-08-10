package com.basic4gl.desktop.language;

import javax.swing.SwingUtilities;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * {@link AutoCompletion} that suppresses the library's own auto-activation retrigger immediately
 * after inserting a completion.
 *
 * <p>{@code AutoCompletion}'s auto-activation listens for any single-character document insert and
 * restarts its popup timer - including the single-character insert produced by accepting a
 * one-character completion (e.g. a variable named {@code i}). Left unchecked, accepting such a
 * completion re-triggers the popup for the exact same, still-matching text; accepting again
 * re-triggers it again, which reads as a stuck completion menu.
 *
 * <p>Auto-activation is turned off for the duration of the synchronous document mutation that
 * {@code insertCompletion} performs, then restored on the next EDT cycle so normal typing keeps
 * auto-activating as usual.
 */
public class NonRetriggeringAutoCompletion extends AutoCompletion {

    public NonRetriggeringAutoCompletion(CompletionProvider provider) {
        super(provider);
    }

    @Override
    protected void insertCompletion(Completion c, boolean typedParamListStartChar) {
        boolean wasAutoActivationEnabled = isAutoActivationEnabled();
        setAutoActivationEnabled(false);
        try {
            super.insertCompletion(c, typedParamListStartChar);
        } finally {
            if (wasAutoActivationEnabled) {
                SwingUtilities.invokeLater(() -> setAutoActivationEnabled(true));
            }
        }
    }
}
