package com.basic4gl.desktop.language;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.antlr.v4.runtime.CharStreams;
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
                // Preprocessor
            case Basic4GL.INCLUDE_DIR -> HighlightKind.PREPROCESSOR;

                // Comments
            case Basic4GL.COMMENT, Basic4GL.REM_COMMENT -> HighlightKind.COMMENT;

                // Primary keywords
            case Basic4GL.FUNCTION_KW,
                    Basic4GL.SUB_KW,
                    Basic4GL.DIM_KW,
                    Basic4GL.AS_KW,
                    Basic4GL.GOTO_KW,
                    Basic4GL.GOSUB_KW,
                    Basic4GL.IF_KW,
                    Basic4GL.THEN_KW,
                    Basic4GL.ELSE_KW,
                    Basic4GL.ELSEIF_KW,
                    Basic4GL.ENDIF_KW,
                    Basic4GL.END_KW,
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
                    Basic4GL.AND_KW,
                    Basic4GL.OR_KW,
                    Basic4GL.NOT_KW,
                    Basic4GL.XOR_KW,
                    Basic4GL.MOD_KW -> HighlightKind.KEYWORD;

                // Secondary keywords – type names and boolean literals
            case Basic4GL.INTEGER_T,
                    Basic4GL.INT_T,
                    Basic4GL.SINGLE_T,
                    Basic4GL.DOUBLE_T,
                    Basic4GL.STRING_T,
                    Basic4GL.TRUE_KW,
                    Basic4GL.FALSE_KW -> HighlightKind.KEYWORD_2;

                // Literals
            case Basic4GL.STRING_LIT -> HighlightKind.STRING;
            case Basic4GL.INT_LIT, Basic4GL.FLOAT_LIT, Basic4GL.HEX_LIT -> HighlightKind.NUMBER;

                // Identifiers – the IDE adapter re-classifies these via wordsToHighlight
            case Basic4GL.IDENTIFIER -> HighlightKind.IDENTIFIER;

                // Whitespace
            case Basic4GL.WS -> HighlightKind.WHITESPACE;
            case Basic4GL.NEWLINE -> HighlightKind.NEWLINE;

                // Operators and punctuation
            case Basic4GL.COLON,
                    Basic4GL.LPAREN,
                    Basic4GL.RPAREN,
                    Basic4GL.LBRACKET,
                    Basic4GL.RBRACKET,
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
                    Basic4GL.BACKSLASH,
                    Basic4GL.CARET,
                    Basic4GL.AT,
                    Basic4GL.BANG,
                    Basic4GL.TILDE,
                    Basic4GL.PERCENT,
                    Basic4GL.PIPE,
                    Basic4GL.HASH,
                    Basic4GL.AMPERSAND -> HighlightKind.OPERATOR;

                // Unknown / unrecognised
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
                        // Infer type from identifier suffix (#, !, $, %)
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
                    } else if (type == Basic4GL.LPAREN || type == Basic4GL.LBRACKET) {
                        dimArrayDepth++;
                    } else if (type == Basic4GL.RPAREN || type == Basic4GL.RBRACKET) {
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
                            || type == Basic4GL.INT_T
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
                        // Infer type from identifier suffix (#, !, $, %)
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
                    } else if (type == Basic4GL.LPAREN || type == Basic4GL.LBRACKET) {
                        dimArrayDepth++;
                    } else if (type == Basic4GL.RPAREN || type == Basic4GL.RBRACKET) {
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
                            || type == Basic4GL.INT_T
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Basic4GL createLexer(String input) {
        Basic4GL lexer = new Basic4GL(CharStreams.fromString(input));
        lexer.removeErrorListeners(); // suppress console noise on partial / invalid source
        return lexer;
    }

    private static LangToken toLangToken(Token t) {
        int start = t.getStartIndex();
        // getStopIndex() is inclusive; LangToken.end is exclusive
        int end = t.getStopIndex() + 1;
        return new LangToken(t.getType(), t.getText(), start, end);
    }

    /** Returns the first non-whitespace token at or after position {@code from}, or null. */
    private static Token peekNonWs(List<Token> tokens, int from) {
        for (int i = from; i < tokens.size(); i++) {
            int type = tokens.get(i).getType();
            if (type != Basic4GL.WS && type != Token.EOF) {
                return tokens.get(i);
            }
        }
        return null;
    }

    private static void addFirstLabel(Map<String, IndexedSymbol> out, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        String key = symbolKey("label", name, null);
        out.putIfAbsent(key, new IndexedSymbol("label", name, name + ":"));
    }

    private static void addFirstFunction(Map<String, IndexedSymbol> out, String name, String signature) {
        if (name == null || name.isBlank()) {
            return;
        }
        String key = symbolKey("userfunc", name, null);
        out.putIfAbsent(key, new IndexedSymbol("userfunc", name, signature));
    }

    private static void addFirstStruct(Map<String, IndexedSymbol> out, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        String key = symbolKey("struc", name, null);
        out.putIfAbsent(key, new IndexedSymbol("struc", name, "struc " + name));
    }

    private static void flushVariable(
            Map<String, IndexedSymbol> out,
            Map<String, Integer> variableDeclCounts,
            String name,
            String type,
            String currentRoutine) {
        if (name == null || name.isBlank()) {
            return;
        }

        String scope = currentRoutine == null ? "global" : currentRoutine;
        String key = symbolKey("variable", name, scope);
        int declCount = variableDeclCounts.merge(key, 1, Integer::sum);

        String baseSig = (type != null && !type.isBlank()) ? type + " " + name : name;
        String scopedSig = baseSig + " [scope: " + scope + "]";
        String sig = declCount > 1 ? scopedSig + " [re-dim x" + declCount + "]" : scopedSig;
        out.put(key, new IndexedSymbol("variable", name, sig));
    }

    private static void emitVariableDeclaration(
            List<SymbolDeclaration> declarations,
            Map<String, Integer> variableDeclCounts,
            Token nameToken,
            String type,
            String currentRoutine,
            String fileId) {
        String name = nameToken.getText();
        if (name == null || name.isBlank()) {
            return;
        }
        String scope = currentRoutine == null ? "global" : currentRoutine;
        String key = symbolKey("variable", name, scope);
        int declCount = variableDeclCounts.merge(key, 1, Integer::sum);
        String baseSig = (type != null && !type.isBlank()) ? type + " " + name : name;
        String scopedSig = baseSig + " [scope: " + scope + "]";
        String sig = declCount > 1 ? scopedSig + " [re-dim x" + declCount + "]" : scopedSig;

        declarations.add(new SymbolDeclaration(
                "variable",
                name,
                sig,
                scope,
                declCount,
                fileId,
                Math.max(0, nameToken.getLine() - 1),
                Math.max(0, nameToken.getCharPositionInLine())));
    }

    private static String symbolKey(String kind, String name, String scope) {
        String normalizedName = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (scope == null || scope.isBlank()) {
            return kind + "|" + normalizedName;
        }
        return kind + "|" + scope.toLowerCase(Locale.ROOT) + "|" + normalizedName;
    }

    /**
     * Infer the type of a variable from its identifier suffix.
     * Returns the inferred type, or null if no suffix.
     *
     * <ul>
     *   <li>{@code #} or {@code !} → "real"</li>
     *   <li>{@code $} → "string"</li>
     *   <li>{@code %} → "integer"</li>
     *   <li>no suffix → null (undefined type)</li>
     * </ul>
     */
    private static String inferTypeFromIdentifierSuffix(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }
        char last = identifier.charAt(identifier.length() - 1);
        return switch (last) {
            case '#', '!' -> "real";
            case '$' -> "string";
            case '%' -> "integer";
            default -> null;
        };
    }
}
