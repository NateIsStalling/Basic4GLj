package com.basic4gl.language.adapter;

import static com.basic4gl.language.adapter.util.LanguageUtil.*;

import com.basic4gl.desktop.spi.language.HighlightKind;
import com.basic4gl.desktop.spi.language.IndexedSymbol;
import com.basic4gl.desktop.spi.language.LangToken;
import com.basic4gl.desktop.spi.language.LanguageSupport;
import com.basic4gl.desktop.spi.language.SymbolDeclaration;
import com.basic4gl.language.adapter.antlr.Basic4GL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

/**
 * {@link LanguageSupport} implementation for the Basic4GL language.
 *
 * <p>Backed by the ANTLR4-generated {@link Basic4GL} lexer produced from {@code Basic4GL.g4}.
 * That single grammar file is the source of truth for:
 *
 * <ul>
 *   <li>Which character sequences are keywords, operators, literals, comments, etc.
 *   <li>Which identifiers are reserved and must be excluded from label / symbol heuristics.
 * </ul>
 *
 * <p>This class contains <strong>no RSyntaxTextArea imports</strong>. The IDE adapter
 * ({@code LanguageSupportTokenMaker}) is the only class that knows about RSyntaxTextArea.
 *
 * <p>Thread-safe: each call to {@link #tokenizeLine} and {@link #extractSymbols} creates a fresh
 * {@link Basic4GL} lexer instance, so concurrent calls from EDT and background threads are safe.
 */
public class Basic4GLLanguageSupport implements LanguageSupport {

    private static final String SYNTAX_STYLE = "text/basic4gl";

    // -------------------------------------------------------------------------
    // LanguageSupport – identity
    // -------------------------------------------------------------------------

    @Override
    public String syntaxStyle() {
        return SYNTAX_STYLE;
    }

    // -------------------------------------------------------------------------
    // LanguageSupport – tokenisation
    // -------------------------------------------------------------------------

    @Override
    public List<LangToken> tokenizeLine(String line) {
        if (line == null || line.isEmpty()) {
            return List.of();
        }
        Basic4GL lexer = createLexer(line);
        List<LangToken> result = new ArrayList<>();
        Token token;
        while ((token = lexer.nextToken()).getType() != Token.EOF) {
            // Skip NEWLINE tokens – the caller provides one line at a time
            if (token.getType() == Basic4GL.NEWLINE) {
                continue;
            }
            result.add(toLangToken(token));
        }
        return result;
    }

    @Override
    public HighlightKind classify(LangToken token) {
        return switch (token.type()) {
            // Preprocessor directives.
            // 'include' has no leading '#'; '#plugin' does. They are separate
            // token types because they have separate syntaxes.
            case Basic4GL.INCLUDE_DIR, Basic4GL.PLUGIN_DIR -> HighlightKind.PREPROCESSOR;

            // Directive arguments: an unquoted include path, and a plugin name
            // that may or may not be quoted.
            case Basic4GL.INCLUDE_PATH, Basic4GL.PLUGIN_VALUE, Basic4GL.PLUGIN_VALUE_STRING ->
                    HighlightKind.STRING;

            // Comments. There is no 'rem' form.
            case Basic4GL.COMMENT -> HighlightKind.COMMENT;

            // Reserved words - TomBasicCompiler.reservedWords, less the four
            // type names, which are KEYWORD_2 below.
            case Basic4GL.DIM_KW,
                 Basic4GL.GOTO_KW,
                 Basic4GL.IF_KW,
                 Basic4GL.THEN_KW,
                 Basic4GL.ELSEIF_KW,
                 Basic4GL.ELSE_KW,
                 Basic4GL.ENDIF_KW,
                 Basic4GL.END_KW,
                 Basic4GL.GOSUB_KW,
                 Basic4GL.RETURN_KW,
                 Basic4GL.FOR_KW,
                 Basic4GL.TO_KW,
                 Basic4GL.STEP_KW,
                 Basic4GL.NEXT_KW,
                 Basic4GL.WHILE_KW,
                 Basic4GL.WEND_KW,
                 Basic4GL.RUN_KW,
                 Basic4GL.STRUC_KW,
                 Basic4GL.ENDSTRUC_KW,
                 Basic4GL.CONST_KW,
                 Basic4GL.ALLOC_KW,
                 Basic4GL.NULL_KW,
                 Basic4GL.DATA_KW,
                 Basic4GL.READ_KW,
                 Basic4GL.RESET_KW,
                 Basic4GL.TYPE_KW,
                 Basic4GL.AS_KW,
                 Basic4GL.LANGUAGE_KW,
                 Basic4GL.TRADITIONAL_KW,
                 Basic4GL.BASIC4GL_KW,
                 Basic4GL.TRADITIONAL_PRINT_KW,
                 Basic4GL.TRADITIONAL_SUFFIX_KW,
                 Basic4GL.INPUT_KW,
                 Basic4GL.DO_KW,
                 Basic4GL.LOOP_KW,
                 Basic4GL.UNTIL_KW,
                 Basic4GL.FUNCTION_KW,
                 Basic4GL.SUB_KW,
                 Basic4GL.ENDFUNCTION_KW,
                 Basic4GL.ENDSUB_KW,
                 Basic4GL.DECLARE_KW,
                 Basic4GL.RUNTIME_KW,
                 Basic4GL.BINDCODE_KW,
                 Basic4GL.EXEC_KW,
                 Basic4GL.INCLUDE_KW,
                 Basic4GL.ARRAYMAX_KW,
                 Basic4GL.BEGINCODEBLOCK_KW,
                 Basic4GL.ENDCODEBLOCK_KW,

                 // Statement-position built-ins. Not reserved words - they resolve
                 // to library functions - but they read as statements.
                 Basic4GL.PRINT_KW,
                 Basic4GL.PRINTR_KW,

                 // Word operators. Styled as keywords to match the usual BASIC
                 // convention and the previous behaviour of this adapter; move
                 // them to OPERATOR if you would rather they matched '+' and '='.
                 Basic4GL.AND_OP,
                 Basic4GL.OR_OP,
                 Basic4GL.NOT_OP,
                 Basic4GL.XOR_OP,
                 Basic4GL.LOR_OP,
                 Basic4GL.LAND_OP -> HighlightKind.KEYWORD;

            // Type names. The complete set - there is no 'int'.
            // 'true' and 'false' are library constants, not tokens; the IDE
            // adapter re-classifies them via wordsToHighlight.
            case Basic4GL.INTEGER_T, Basic4GL.SINGLE_T, Basic4GL.DOUBLE_T, Basic4GL.STRING_T ->
                    HighlightKind.KEYWORD_2;

            // Literals. An unquoted element inside a DATA statement is a string
            // constant, not an identifier.
            case Basic4GL.STRING_LIT, Basic4GL.DATA_ELEMENT -> HighlightKind.STRING;
            case Basic4GL.INT_LIT, Basic4GL.FLOAT_LIT, Basic4GL.HEX_LIT -> HighlightKind.NUMBER;

            // Identifiers - the IDE adapter re-classifies these via wordsToHighlight
            case Basic4GL.IDENTIFIER -> HighlightKind.IDENTIFIER;

            // Whitespace
            case Basic4GL.WS -> HighlightKind.WHITESPACE;
            case Basic4GL.NEWLINE -> HighlightKind.NEWLINE;

            // Operators and punctuation. Basic4GL has no '[' ']', no '\\', no '^'.
            case Basic4GL.COLON,
                 Basic4GL.LPAREN,
                 Basic4GL.RPAREN,
                 Basic4GL.COMMA,
                 Basic4GL.DOT,
                 Basic4GL.SEMICOLON,
                 Basic4GL.EQ,
                 Basic4GL.NEQ,
                 Basic4GL.LT,
                 Basic4GL.GT,
                 Basic4GL.LTE,
                 Basic4GL.GTE,
                 Basic4GL.PLUS,
                 Basic4GL.MINUS,
                 Basic4GL.STAR,
                 Basic4GL.SLASH,
                 Basic4GL.PERCENT,
                 Basic4GL.AMPERSAND -> HighlightKind.OPERATOR;

            // Lexically well-formed but always rejected by the compiler.
            // These are the three token types to move to an error style if
            // HighlightKind gains one; for now they take the nearest visual
            // match so the line still reads sensibly.
            case Basic4GL.UNTERMINATED_STRING -> HighlightKind.STRING;
            case Basic4GL.INVALID_COMPARISON -> HighlightKind.OPERATOR;
            case Basic4GL.INVALID_SYMBOL -> HighlightKind.OTHER;

            default -> HighlightKind.OTHER;
        };
    }

    // -------------------------------------------------------------------------
    // LanguageSupport – symbol extraction
    // -------------------------------------------------------------------------

    /**
     * Scans the full source text and extracts user-defined symbols by walking the ANTLR token
     * stream with a lightweight state machine.
     *
     * <p>Recognised patterns:
     *
     * <ul>
     *   <li>{@code function Name(params)} / {@code sub Name(params)} → {@code "userfunc"}
     *   <li>{@code Name:} (identifier immediately followed by {@code COLON}) → {@code "label"}
     *   <li>{@code dim Name [as Type]} → {@code "variable"}
     * </ul>
     */
    @Override
    public List<IndexedSymbol> extractSymbols(String source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        Basic4GL lexer = createLexer(source);
        CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();
        List<Token> tokens = stream.getTokens();

        Map<String, IndexedSymbol> symbolsByKey = new LinkedHashMap<>();
        Map<String, Integer> variableDeclCounts = new LinkedHashMap<>();

        // State machine
        final int NONE = 0;
        final int AFTER_FUNC_KW = 1; // saw function/sub – next identifier is the name
        final int COLLECT_PARAMS = 2; // collecting signature text inside ( … )
        final int AFTER_DIM_KW = 3; // saw dim – next identifier is the variable name
        final int AFTER_DIM_NAME = 4; // saw dim name – look for 'as <Type>'
        final int AFTER_AS_KW = 5; // saw 'as' after dim name – next identifier is type

        int state = NONE;
        String pendingFuncName = null;
        StringBuilder paramBuf = null;
        int parenDepth = 0;
        String pendingVarName = null;
        String pendingVarType = null;
        // Depth of ( or [ seen while in AFTER_DIM_NAME – used to suppress the
        // type-prefix identifier swap when inside an array-size expression.
        int dimArrayDepth = 0;
        String currentRoutine = null;
        // Struc-scope tracking: dims inside a struc block use "struc:<Name>" as scope
        // so they never collide with same-named program variables in re-dim counting.
        boolean inStruc = false;
        String currentStrucName = null;

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            int type = t.getType();

            // Skip whitespace, newlines, and EOF in the state machine
            if (type == Token.EOF || type == Basic4GL.WS || type == Basic4GL.NEWLINE) {
                // A newline resets after-dim state (one dim per line)
                if (type == Basic4GL.NEWLINE && state == AFTER_DIM_NAME) {
                    String effectiveRoutine =
                            inStruc ? "struc:" + (currentStrucName != null ? currentStrucName : "") : currentRoutine;
                    flushVariable(symbolsByKey, variableDeclCounts, pendingVarName, pendingVarType, effectiveRoutine);
                    state = NONE;
                    pendingVarName = null;
                    pendingVarType = null;
                    dimArrayDepth = 0;
                }
                continue;
            }

            switch (state) {
                case NONE -> {
                    if (type == Basic4GL.FUNCTION_KW || type == Basic4GL.SUB_KW) {
                        state = AFTER_FUNC_KW;
                    } else if (type == Basic4GL.END_KW) {
                        Token next = peekNonWs(tokens, i + 1);
                        if (next != null
                                && (next.getType() == Basic4GL.FUNCTION_KW || next.getType() == Basic4GL.SUB_KW)) {
                            currentRoutine = null;
                        } else if (next != null && next.getType() == Basic4GL.TYPE_KW) {
                            // "end type" – same as endstruc
                            inStruc = false;
                            currentStrucName = null;
                        }
                    } else if (type == Basic4GL.STRUC_KW || type == Basic4GL.TYPE_KW) {
                        // Entering a struc/type block – capture the struct name from the next identifier
                        Token nameToken = peekNonWs(tokens, i + 1);
                        currentStrucName = (nameToken != null && nameToken.getType() == Basic4GL.IDENTIFIER)
                                ? nameToken.getText()
                                : null;
                        inStruc = true;
                        // Emit the struct type itself as a symbol
                        if (currentStrucName != null) {
                            addFirstStruct(symbolsByKey, currentStrucName);
                        }
                    } else if (type == Basic4GL.ENDSTRUC_KW) {
                        inStruc = false;
                        currentStrucName = null;
                    } else if (type == Basic4GL.DIM_KW) {
                        state = AFTER_DIM_KW;
                    } else if (type == Basic4GL.IDENTIFIER) {
                        // Look ahead (skip WS) for a COLON → label declaration
                        Token next = peekNonWs(tokens, i + 1);
                        if (next != null && next.getType() == Basic4GL.COLON) {
                            addFirstLabel(symbolsByKey, t.getText());
                        }
                    }
                }
                case AFTER_FUNC_KW -> {
                    if (type == Basic4GL.IDENTIFIER) {
                        pendingFuncName = t.getText();
                        paramBuf = new StringBuilder(t.getText()).append('(');
                        parenDepth = 0;
                        state = COLLECT_PARAMS;
                    } else {
                        state = NONE; // unexpected token – reset
                    }
                }
                case COLLECT_PARAMS -> {
                    if (type == Basic4GL.LPAREN) {
                        parenDepth++;
                        // don't append – we already opened the sig paren
                    } else if (type == Basic4GL.RPAREN) {
                        if (parenDepth == 0) {
                            // Closing paren of the function signature
                            String sig = paramBuf.toString().trim();
                            // Remove trailing comma if any
                            if (sig.endsWith(","))
                                sig = sig.substring(0, sig.length() - 1).trim();
                            addFirstFunction(symbolsByKey, pendingFuncName, sig + ")");
                            currentRoutine = pendingFuncName;
                            state = NONE;
                            pendingFuncName = null;
                            paramBuf = null;
                        } else {
                            parenDepth--;
                            paramBuf.append(t.getText());
                        }
                    } else if (type != Basic4GL.WS && type != Basic4GL.NEWLINE) {
                        if (paramBuf.length() > 0
                                && !paramBuf.toString().endsWith("(")
                                && !paramBuf.toString().endsWith(",")
                                && !paramBuf.toString().endsWith(" ")) {
                            paramBuf.append(' ');
                        }
                        paramBuf.append(t.getText());
                    }
                }
                case AFTER_DIM_KW -> {
                    if (type == Basic4GL.IDENTIFIER) {
                        pendingVarName = t.getText();
                        // Infer type from identifier suffix ($, %, #)
                        pendingVarType = inferTypeFromIdentifierSuffix(t.getText());
                        state = AFTER_DIM_NAME;
                    } else {
                        state = NONE;
                    }
                }
                case AFTER_DIM_NAME -> {
                    String effectiveRoutine =
                            inStruc ? "struc:" + (currentStrucName != null ? currentStrucName : "") : currentRoutine;
                    if (type == Basic4GL.AS_KW && dimArrayDepth == 0) {
                        dimArrayDepth = 0;
                        state = AFTER_AS_KW;
                    } else if (type == Basic4GL.LPAREN) {
                        dimArrayDepth++;
                    } else if (type == Basic4GL.RPAREN) {
                        if (dimArrayDepth > 0) dimArrayDepth--;
                    } else if (type == Basic4GL.IDENTIFIER && dimArrayDepth == 0) {
                        // "dim Type VarName" – the first IDENTIFIER was the type name,
                        // this IDENTIFIER is the actual variable name.
                        // Check if we already have a pendingVarType: if it's the inferred
                        // type from pendingVarName (the first ID), we're in type-prefix mode.
                        String inferredFromFirstId = inferTypeFromIdentifierSuffix(pendingVarName);
                        if (pendingVarType == null || pendingVarType.equals(inferredFromFirstId)) {
                            // Type-prefix case: pendingVarName is the explicit type, new ID is the var name
                            String newVarUserType = t.getText();
                            String newVarInferredType = inferTypeFromIdentifierSuffix(newVarUserType);
                            pendingVarType = newVarInferredType != null ? newVarInferredType : pendingVarName;
                            pendingVarName = newVarUserType;
                        }
                    } else if ((type == Basic4GL.COLON || type == Basic4GL.COMMA) && dimArrayDepth == 0) {
                        // 'dim x, y' or 'dim x :' – flush current, continue
                        flushVariable(
                                symbolsByKey, variableDeclCounts, pendingVarName, pendingVarType, effectiveRoutine);
                        pendingVarName = null;
                        pendingVarType = null;
                        dimArrayDepth = 0;
                        state = (type == Basic4GL.COMMA) ? AFTER_DIM_KW : NONE;
                    }
                    // else: other tokens (array size expression contents, &, etc.) – stay
                }
                case AFTER_AS_KW -> {
                    String effectiveRoutine =
                            inStruc ? "struc:" + (currentStrucName != null ? currentStrucName : "") : currentRoutine;
                    if (type == Basic4GL.IDENTIFIER
                            || type == Basic4GL.INTEGER_T
                            || type == Basic4GL.SINGLE_T
                            || type == Basic4GL.DOUBLE_T
                            || type == Basic4GL.STRING_T) {
                        pendingVarType = t.getText();
                        flushVariable(
                                symbolsByKey, variableDeclCounts, pendingVarName, pendingVarType, effectiveRoutine);
                        state = NONE;
                    } else {
                        flushVariable(symbolsByKey, variableDeclCounts, pendingVarName, null, effectiveRoutine);
                        state = NONE;
                    }
                }
            }
        }

        // Flush any dangling state at EOF
        if (state == AFTER_DIM_NAME || state == AFTER_AS_KW) {
            String effectiveRoutine =
                    inStruc ? "struc:" + (currentStrucName != null ? currentStrucName : "") : currentRoutine;
            flushVariable(symbolsByKey, variableDeclCounts, pendingVarName, pendingVarType, effectiveRoutine);
        }

        return new ArrayList<>(symbolsByKey.values());
    }

    @Override
    public List<SymbolDeclaration> extractDeclarations(String source, String fileId) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }

        Basic4GL lexer = createLexer(source);
        CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();
        List<Token> tokens = stream.getTokens();

        List<SymbolDeclaration> declarations = new ArrayList<>();
        Map<String, Integer> variableDeclCounts = new LinkedHashMap<>();

        final int NONE = 0;
        final int AFTER_FUNC_KW = 1;
        final int COLLECT_PARAMS = 2;
        final int AFTER_DIM_KW = 3;
        final int AFTER_DIM_NAME = 4;
        final int AFTER_AS_KW = 5;

        int state = NONE;
        Token pendingFuncNameToken = null;
        StringBuilder paramBuf = null;
        int parenDepth = 0;
        Token pendingVarNameToken = null;
        String pendingVarType = null;
        // Depth of ( or [ seen while in AFTER_DIM_NAME – used to suppress the
        // type-prefix identifier swap when inside an array-size expression.
        int dimArrayDepth = 0;
        String currentRoutine = null;
        // Struc-scope tracking: dims inside a struc block use "struc:<Name>" as scope
        // so they never collide with same-named program variables in re-dim counting.
        boolean inStruc = false;
        String currentStrucName = null;

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            int type = t.getType();

            if (type == Token.EOF || type == Basic4GL.WS || type == Basic4GL.NEWLINE) {
                if (type == Basic4GL.NEWLINE && state == AFTER_DIM_NAME && pendingVarNameToken != null) {
                    String effectiveRoutine =
                            inStruc ? "struc:" + (currentStrucName != null ? currentStrucName : "") : currentRoutine;
                    emitVariableDeclaration(
                            declarations,
                            variableDeclCounts,
                            pendingVarNameToken,
                            pendingVarType,
                            effectiveRoutine,
                            fileId);
                    pendingVarNameToken = null;
                    pendingVarType = null;
                    dimArrayDepth = 0;
                    state = NONE;
                }
                continue;
            }

            switch (state) {
                case NONE -> {
                    if (type == Basic4GL.FUNCTION_KW || type == Basic4GL.SUB_KW) {
                        state = AFTER_FUNC_KW;
                    } else if (type == Basic4GL.END_KW) {
                        Token next = peekNonWs(tokens, i + 1);
                        if (next != null
                                && (next.getType() == Basic4GL.FUNCTION_KW || next.getType() == Basic4GL.SUB_KW)) {
                            currentRoutine = null;
                        } else if (next != null && next.getType() == Basic4GL.TYPE_KW) {
                            // "end type" – same as endstruc
                            inStruc = false;
                            currentStrucName = null;
                        }
                    } else if (type == Basic4GL.STRUC_KW || type == Basic4GL.TYPE_KW) {
                        // Entering a struc/type block – capture the struct name from the next identifier
                        Token nameToken = peekNonWs(tokens, i + 1);
                        currentStrucName = (nameToken != null && nameToken.getType() == Basic4GL.IDENTIFIER)
                                ? nameToken.getText()
                                : null;
                        inStruc = true;
                        // Emit the struct type definition itself as a declaration
                        if (currentStrucName != null && nameToken != null) {
                            declarations.add(new SymbolDeclaration(
                                    "struc",
                                    currentStrucName,
                                    "struc " + currentStrucName,
                                    "global",
                                    1,
                                    fileId,
                                    Math.max(0, nameToken.getLine() - 1),
                                    Math.max(0, nameToken.getCharPositionInLine())));
                        }
                    } else if (type == Basic4GL.ENDSTRUC_KW) {
                        inStruc = false;
                        currentStrucName = null;
                    } else if (type == Basic4GL.DIM_KW) {
                        state = AFTER_DIM_KW;
                    } else if (type == Basic4GL.IDENTIFIER) {
                        Token next = peekNonWs(tokens, i + 1);
                        if (next != null && next.getType() == Basic4GL.COLON) {
                            declarations.add(new SymbolDeclaration(
                                    "label",
                                    t.getText(),
                                    t.getText() + ":",
                                    currentRoutine == null ? "global" : currentRoutine,
                                    1,
                                    fileId,
                                    Math.max(0, t.getLine() - 1),
                                    Math.max(0, t.getCharPositionInLine())));
                        }
                    }
                }
                case AFTER_FUNC_KW -> {
                    if (type == Basic4GL.IDENTIFIER) {
                        pendingFuncNameToken = t;
                        paramBuf = new StringBuilder(t.getText()).append('(');
                        parenDepth = 0;
                        state = COLLECT_PARAMS;
                    } else {
                        state = NONE;
                    }
                }
                case COLLECT_PARAMS -> {
                    if (type == Basic4GL.LPAREN) {
                        parenDepth++;
                    } else if (type == Basic4GL.RPAREN) {
                        if (parenDepth == 0 && pendingFuncNameToken != null) {
                            String sig = paramBuf.toString().trim();
                            if (sig.endsWith(",")) {
                                sig = sig.substring(0, sig.length() - 1).trim();
                            }
                            declarations.add(new SymbolDeclaration(
                                    "userfunc",
                                    pendingFuncNameToken.getText(),
                                    sig + ")",
                                    "global",
                                    1,
                                    fileId,
                                    Math.max(0, pendingFuncNameToken.getLine() - 1),
                                    Math.max(0, pendingFuncNameToken.getCharPositionInLine())));
                            currentRoutine = pendingFuncNameToken.getText();
                            pendingFuncNameToken = null;
                            paramBuf = null;
                            state = NONE;
                        } else {
                            parenDepth--;
                            if (paramBuf != null) {
                                paramBuf.append(t.getText());
                            }
                        }
                    } else {
                        if (paramBuf != null
                                && !paramBuf.toString().endsWith("(")
                                && !paramBuf.toString().endsWith(",")
                                && !paramBuf.toString().endsWith(" ")) {
                            paramBuf.append(' ');
                        }
                        if (paramBuf != null) {
                            paramBuf.append(t.getText());
                        }
                    }
                }
                case AFTER_DIM_KW -> {
                    if (type == Basic4GL.IDENTIFIER) {
                        pendingVarNameToken = t;
                        // Infer type from identifier suffix ($, %, #)
                        pendingVarType = inferTypeFromIdentifierSuffix(t.getText());
                        state = AFTER_DIM_NAME;
                    } else {
                        state = NONE;
                    }
                }
                case AFTER_DIM_NAME -> {
                    String effectiveRoutine =
                            inStruc ? "struc:" + (currentStrucName != null ? currentStrucName : "") : currentRoutine;
                    if (type == Basic4GL.AS_KW && dimArrayDepth == 0) {
                        dimArrayDepth = 0;
                        state = AFTER_AS_KW;
                    } else if (type == Basic4GL.LPAREN) {
                        dimArrayDepth++;
                    } else if (type == Basic4GL.RPAREN) {
                        if (dimArrayDepth > 0) dimArrayDepth--;
                    } else if (type == Basic4GL.IDENTIFIER && dimArrayDepth == 0) {
                        // "dim Type VarName" – the first IDENTIFIER was the type name,
                        // this IDENTIFIER is the actual variable name.
                        String firstIdText = pendingVarNameToken != null ? pendingVarNameToken.getText() : null;
                        String inferredFromFirstId = inferTypeFromIdentifierSuffix(firstIdText);
                        if (pendingVarType == null || pendingVarType.equals(inferredFromFirstId)) {
                            // Type-prefix case: first token is the explicit type, new token is the var name
                            String newVarUserType = t.getText();
                            String newVarInferredType = inferTypeFromIdentifierSuffix(newVarUserType);
                            pendingVarType = newVarInferredType != null ? newVarInferredType : firstIdText;
                            pendingVarNameToken = t;
                        }
                    } else if ((type == Basic4GL.COLON || type == Basic4GL.COMMA) && dimArrayDepth == 0) {
                        if (pendingVarNameToken != null) {
                            emitVariableDeclaration(
                                    declarations,
                                    variableDeclCounts,
                                    pendingVarNameToken,
                                    pendingVarType,
                                    effectiveRoutine,
                                    fileId);
                        }
                        pendingVarNameToken = null;
                        pendingVarType = null;
                        dimArrayDepth = 0;
                        state = (type == Basic4GL.COMMA) ? AFTER_DIM_KW : NONE;
                    }
                    // else: other tokens (array size expression, &, etc.) – stay in AFTER_DIM_NAME
                }
                case AFTER_AS_KW -> {
                    String effectiveRoutine =
                            inStruc ? "struc:" + (currentStrucName != null ? currentStrucName : "") : currentRoutine;
                    if (type == Basic4GL.IDENTIFIER
                            || type == Basic4GL.INTEGER_T
                            || type == Basic4GL.SINGLE_T
                            || type == Basic4GL.DOUBLE_T
                            || type == Basic4GL.STRING_T) {
                        pendingVarType = t.getText();
                    }
                    if (pendingVarNameToken != null) {
                        emitVariableDeclaration(
                                declarations,
                                variableDeclCounts,
                                pendingVarNameToken,
                                pendingVarType,
                                effectiveRoutine,
                                fileId);
                    }
                    pendingVarNameToken = null;
                    pendingVarType = null;
                    state = NONE;
                }
            }
        }

        if ((state == AFTER_DIM_NAME || state == AFTER_AS_KW) && pendingVarNameToken != null) {
            String effectiveRoutine =
                    inStruc ? "struc:" + (currentStrucName != null ? currentStrucName : "") : currentRoutine;
            emitVariableDeclaration(
                    declarations, variableDeclCounts, pendingVarNameToken, pendingVarType, effectiveRoutine, fileId);
        }

        return declarations;
    }
}