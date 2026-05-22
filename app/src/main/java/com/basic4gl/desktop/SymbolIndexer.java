package com.basic4gl.desktop;

import com.basic4gl.desktop.language.IndexedSymbol;
import com.basic4gl.desktop.language.LanguageSupport;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Lightweight debounced symbol indexer.
 *
 * <p>Listens for source-text changes and, after a short debounce delay, delegates symbol
 * extraction to a {@link LanguageSupport} instance. Results are delivered via a {@link Callback}
 * on the Swing EDT.
 *
 * <p>The indexer itself contains <strong>no language-specific logic</strong>; all parsing is
 * performed by the supplied {@code LanguageSupport}. Swapping languages is a constructor change.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * LanguageSupport lang = new Basic4GLLanguageSupport();
 * SymbolIndexer indexer = new SymbolIndexer(lang, symbols -> updateReferencePanel(symbols));
 * // On every document change:
 * indexer.schedule(getAllEditorText());
 * // On window close:
 * indexer.shutdown();
 * }</pre>
 */
public class SymbolIndexer {

    /** Receives indexed symbols on the Swing EDT after each debounce cycle. */
    public interface Callback {
        void onIndexed(List<IndexedSymbol> symbols);
    }

    /** Milliseconds to wait after the last change before running extraction. */
    private static final long DEBOUNCE_MILLIS = 400;

    private final LanguageSupport languageSupport;
    private final Callback callback;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "symbol-indexer");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> pending;

    public SymbolIndexer(LanguageSupport languageSupport, Callback callback) {
        this.languageSupport = languageSupport;
        this.callback = callback;
    }

    /**
     * Schedules an indexing pass after the debounce delay.
     *
     * <p>Calling this again before the window expires cancels the previous pass and restarts the
     * timer — only one extraction runs per idle period. Thread-safe.
     *
     * @param source full source text (may span multiple concatenated editor files)
     */
    public synchronized void schedule(String source) {
        cancelPending();
        pending = scheduler.schedule(() -> runAndDeliver(source), DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * Triggers an immediate (zero-delay) indexing pass.
     *
     * <p>Useful after a successful full compile to sync symbols without waiting for the debounce
     * window.
     *
     * @param source full source text
     */
    public synchronized void indexNow(String source) {
        cancelPending();
        pending = scheduler.schedule(() -> runAndDeliver(source), 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Shuts down the background scheduler. Call when the owning window is disposed to release
     * the daemon thread cleanly.
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    // -------------------------------------------------------------------------
    // Private
    // -------------------------------------------------------------------------

    private void cancelPending() {
        if (pending != null && !pending.isDone()) {
            pending.cancel(false);
        }
    }

    private void runAndDeliver(String source) {
        List<IndexedSymbol> result = languageSupport.extractSymbols(source);
        SwingUtilities.invokeLater(() -> callback.onIndexed(result));
    }
}
