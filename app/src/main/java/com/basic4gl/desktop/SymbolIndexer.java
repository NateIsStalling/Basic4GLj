package com.basic4gl.desktop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

/**
 * Lightweight debounced symbol indexer.
 *
 * <p>Listens for source-text changes and, after a short debounce delay, scans the text for
 * user-defined symbols (functions/subs, gosub labels, and dim-declared variables). Results are
 * delivered to the supplied {@link Callback} on the Swing EDT, making it safe to update UI
 * components directly from the callback.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * SymbolIndexer indexer = new SymbolIndexer(symbols -> updateReferencePanel(symbols));
 * // On every document change:
 * indexer.schedule(getAllEditorText());
 * // On window close:
 * indexer.shutdown();
 * }</pre>
 */
public class SymbolIndexer {

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** A single symbol discovered in the source. */
    public static final class IndexedSymbol {
        /** Kind tag: {@code "userfunc"}, {@code "label"}, or {@code "variable"}. */
        public final String kind;
        /** The bare symbol name (no punctuation). */
        public final String name;
        /** Human-readable signature shown in the reference panel. */
        public final String signature;

        public IndexedSymbol(String kind, String name, String signature) {
            this.kind = kind;
            this.name = name;
            this.signature = signature;
        }
    }

    /** Receives indexed symbols on the Swing EDT after each debounce cycle. */
    public interface Callback {
        void onIndexed(List<IndexedSymbol> symbols);
    }

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    /** Milliseconds to wait after the last change before running the indexer. */
    private static final long DEBOUNCE_MILLIS = 400;

    // -------------------------------------------------------------------------
    // Patterns (case-insensitive, multiline)
    // -------------------------------------------------------------------------

    // function/sub header: "function Foo(int x, string y)" or "sub Bar()"
    private static final Pattern FUNC_PATTERN =
            Pattern.compile(
                    "^[ \\t]*(?:function|sub)[ \\t]+(\\w+)[ \\t]*\\(([^)]*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // label declaration: "myLabel:" (at start of a non-blank line, optional leading whitespace)
    // Excludes lines that look like "keyword:" to avoid false positives with type annotations.
    private static final Pattern LABEL_PATTERN =
            Pattern.compile(
                    "^[ \\t]*(\\w+)[ \\t]*:[ \\t]*(?:$|'|rem[ \\t])",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // dim declaration: "dim x", "dim x as integer", "dim x(10)"
    // Also handles "dim x as integer()" array types.
    private static final Pattern DIM_PATTERN =
            Pattern.compile(
                    "^[ \\t]*dim[ \\t]+(\\w+)(?:[ \\t]*\\([^)]*\\))?(?:[ \\t]+as[ \\t]+(\\w+(?:[ \\t]*\\([ \\t]*\\))?))?",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r, "symbol-indexer");
                        t.setDaemon(true);
                        return t;
                    });

    private ScheduledFuture<?> pending;
    private final Callback callback;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public SymbolIndexer(Callback callback) {
        this.callback = callback;
    }

    // -------------------------------------------------------------------------
    // Public methods
    // -------------------------------------------------------------------------

    /**
     * Schedules an indexing pass for the given source text.
     *
     * <p>Calling this method again before the debounce window expires cancels the previous
     * scheduled pass and restarts the timer — only one indexing pass runs per idle period.
     *
     * <p>This method is thread-safe and may be called from any thread.
     *
     * @param source the full source text to index (may span multiple concatenated files)
     */
    public synchronized void schedule(String source) {
        if (pending != null && !pending.isDone()) {
            pending.cancel(false);
        }
        pending =
                scheduler.schedule(
                        () -> {
                            List<IndexedSymbol> result = scan(source);
                            SwingUtilities.invokeLater(() -> callback.onIndexed(result));
                        },
                        DEBOUNCE_MILLIS,
                        TimeUnit.MILLISECONDS);
    }

    /**
     * Triggers an immediate (non-debounced) indexing pass.
     *
     * <p>Useful after a successful full compile when up-to-date symbols are already available but
     * the indexer should also refresh its last-known symbol set for subsequent incremental updates.
     *
     * @param source the full source text to index
     */
    public synchronized void indexNow(String source) {
        if (pending != null && !pending.isDone()) {
            pending.cancel(false);
        }
        pending =
                scheduler.schedule(
                        () -> {
                            List<IndexedSymbol> result = scan(source);
                            SwingUtilities.invokeLater(() -> callback.onIndexed(result));
                        },
                        0,
                        TimeUnit.MILLISECONDS);
    }

    /**
     * Shuts down the background scheduler.
     *
     * <p>Call this when the owning window is disposed to release the daemon thread.
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    // -------------------------------------------------------------------------
    // Scanning
    // -------------------------------------------------------------------------

    /** Scans {@code source} and returns every discovered symbol. */
    private List<IndexedSymbol> scan(String source) {
        List<IndexedSymbol> symbols = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return symbols;
        }

        // --- User functions and subs ---
        Matcher m = FUNC_PATTERN.matcher(source);
        while (m.find()) {
            String name = m.group(1);
            String params = m.group(2).trim();
            // Normalise whitespace inside parameter list
            params = params.replaceAll("[ \\t]+", " ");
            String sig = name + "(" + params + ")";
            symbols.add(new IndexedSymbol("userfunc", name, sig));
        }

        // --- Gosub / goto labels ---
        m = LABEL_PATTERN.matcher(source);
        while (m.find()) {
            String name = m.group(1).trim();
            if (!isReservedWord(name)) {
                symbols.add(new IndexedSymbol("label", name, name + ":"));
            }
        }

        // --- Dim-declared variables ---
        m = DIM_PATTERN.matcher(source);
        while (m.find()) {
            String name = m.group(1);
            String type = m.group(2); // may be null
            String sig = (type != null && !type.isBlank()) ? type.trim() + " " + name : name;
            symbols.add(new IndexedSymbol("variable", name, sig));
        }

        return symbols;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code word} is a Basic4GL reserved keyword.
     *
     * <p>This is a fast approximation; only common keywords that would otherwise produce false
     * label matches are listed here.
     */
    private static boolean isReservedWord(String word) {
        if (word == null) return false;
        return switch (word.toLowerCase()) {
            case "dim",
                    "goto",
                    "if",
                    "then",
                    "else",
                    "elseif",
                    "endif",
                    "end",
                    "gosub",
                    "return",
                    "for",
                    "to",
                    "step",
                    "next",
                    "while",
                    "wend",
                    "run",
                    "struc",
                    "endstruc",
                    "const",
                    "alloc",
                    "null",
                    "data",
                    "read",
                    "reset",
                    "type",
                    "function",
                    "sub",
                    "true",
                    "false",
                    "and",
                    "or",
                    "not",
                    "xor",
                    "mod",
                    "rem",
                    "integer",
                    "single",
                    "double",
                    "string" -> true;
            default -> false;
        };
    }
}

