package com.basic4gl.desktop.spi;

import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.desktop.spi.language.*;

import java.util.ArrayList;
import java.util.List;

public interface LanguageService {

    public void onLoad(PluginContext context);

    public void onUnload();

    public List<String> extractStringLiterals(String text);
    public List<String> getReservedWords();

    public List<String> getConstants();

    public List<String> getFunctions();

    public List<String> getOperators();

    ArrayList<String> buildFriendlyCallStackLabels(StackTraceCallback stackTraceCallback);

    String toFriendlyStackFrameLabel(StackFrame frame);

    StackTraceCallback toVmViewFriendlyCallStack(StackTraceCallback stackTraceCallback);

    int getSourceFromMain(String filename, int sourceLine);

    FileLineNumber getFileLineNumberFromMain(int sourceLine);

    Iterable<VariableDefinition> getVariableDefinitions();

    Iterable<VariableDefinition> getConstantDefinitions();

    Iterable<LabelDefinition> getLabelDefinitions();

    Iterable<FunctionDefinition> getFunctionDefinitions();


    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------

    /**
     * The MIME-type style string used to register this language with RSyntaxTextArea's
     * {@code TokenMakerFactory} (e.g. {@code "text/basic4gl"}).
     *
     * <p>The value is opaque to the core indexer but consumed by the RSyntaxTextArea adapter.
     */
    String syntaxStyle();

    // -------------------------------------------------------------------------
    // Tokenisation
    // -------------------------------------------------------------------------

    /**
     * Tokenizes a single line of source text.
     *
     * <p>The returned list contains all tokens in left-to-right order. {@link LangToken#start()}
     * and {@link LangToken#end()} are 0-based character offsets within {@code line}.
     *
     * <p>Implementations must not return {@code null}; an empty line may return an empty list.
     *
     * @param line a single line of source (no {@code \n})
     * @return ordered, non-null token list
     */
    List<LangToken> tokenizeLine(String line);

    /**
     * Maps an implementation-specific {@link LangToken#type()} to a portable
     * {@link HighlightKind}.
     *
     * <p>This is the only place where the internal token type integers are interpreted.
     * All other code works with {@link HighlightKind} values.
     *
     * @param token a token previously produced by {@link #tokenizeLine}
     * @return the semantic highlight category; never {@code null}
     */
    HighlightKind classify(LangToken token);

    // -------------------------------------------------------------------------
    // Symbol extraction
    // -------------------------------------------------------------------------

    /**
     * Scans the full source text (which may span multiple concatenated files) and returns every
     * user-defined symbol it can discover.
     *
     * <p>This method is called from a background thread by the {@code SymbolIndexer} after each
     * debounce cycle; it must not touch Swing components.
     *
     * @param source full program source text
     * @return discovered symbols; never {@code null}
     */
    List<IndexedSymbol> extractSymbols(String source);

    /**
     * Extracts declaration sites from source for navigation features (e.g. Go To Declaration).
     *
     * <p>Default implementation returns an empty list so existing language plugins remain binary
     * compatible until they opt into declaration-aware navigation.
     *
     * @param source full source text
     * @param fileId caller-provided source identifier (typically absolute file path)
     * @return declaration list; never {@code null}
     */
    default List<SymbolDeclaration> extractDeclarations(String source, String fileId) {
        return List.of();
    }
}
