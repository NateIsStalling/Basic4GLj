package com.basic4gl.language.adapter;

import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.desktop.spi.FileLineNumber;
import com.basic4gl.desktop.spi.LanguageService;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.language.*;
import com.basic4gl.language.adapter.antlr.Basic4GL;
import com.basic4gl.language.adapter.util.LanguageUtil;
import com.basic4gl.language.adapter.util.NumberUtil;
import com.basic4gl.language.core.extensions.FunctionLibrary;
import com.basic4gl.language.core.extensions.Library;
import com.basic4gl.language.core.internal.Mutable;
import com.basic4gl.language.core.types.Constant;
import com.basic4gl.language.core.types.FunctionSpecification;
import com.basic4gl.language.core.types.ValType;
import com.basic4gl.language.spi.PluginLibrary;
import com.basic4gl.language.spi.PluginManager;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.stream.Stream;

import static com.basic4gl.language.adapter.util.LanguageUtil.*;

public class Basic4GLLanguageService implements LanguageService {

    private static final String SYNTAX_STYLE = "text/basic4gl";

    private final TomBasicCompiler compiler;
    private final Preprocessor preprocessor;
    private final PluginManager pluginManager;

    Basic4GLLanguageService(TomBasicCompiler compiler, Preprocessor preprocessor, PluginManager pluginManager) {
        this.compiler = compiler;
        this.preprocessor = preprocessor;
        this.pluginManager = pluginManager;
    }

    @Override
    public void onLoad(PluginContext context) {}

    @Override
    public void onUnload() {}

    @Override
    public List<String> extractStringLiterals(String text) {
        java.util.List<String> literals = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return literals;
        }

        int length = text.length();
        int index = 0;
        while (index < length) {
            char ch = text.charAt(index);
            if (ch != '"') {
                index++;
                continue;
            }

            StringBuilder literal = new StringBuilder();
            index++;
            boolean escaped = false;
            boolean terminated = false;
            while (index < length) {
                char current = text.charAt(index++);
                if (escaped) {
                    if (current == '"' || current == '\\') {
                        literal.append(current);
                    } else {
                        // Preserve non-quote escape sequences exactly as typed.
                        literal.append('\\').append(current);
                    }
                    escaped = false;
                    continue;
                }

                if (current == '\\') {
                    escaped = true;
                    continue;
                }

                if (current == '"') {
                    literals.add(literal.toString());
                    terminated = true;
                    break;
                }

                literal.append(current);
            }

            // Unterminated string literal: discard and continue scanning.
            if (!terminated) {
                continue;
            }
        }

        return literals;
    }

    @Override
    public List<String> getReservedWords() {
        return new ArrayList<>(compiler.getReservedWords());
    }

    @Override
    public List<String> getConstants() {
        return new ArrayList<>(compiler.getConstants().keySet());
    }

    @Override
    public List<String> getFunctions() {
        LinkedHashSet<String> functions =
                new LinkedHashSet<>(compiler.getFunctionIndex().keySet());
        for (PluginLibrary library : pluginManager.getLoadedLibraries()) {
            for (int i = 0; i < library.count(); i++) {
                String functionName = library.getFunctionName(i);
                if (functionName != null && !functionName.isBlank()) {
                    functions.add(functionName.toLowerCase());
                }
            }
        }
        return new ArrayList<>(functions);
    }

    @Override
    public List<String> getOperators() {
        return Stream.concat(compiler.getBinaryOperators().stream(), compiler.getUnaryOperators().stream())
                .toList();
    }

    @Override
    public ArrayList<String> buildFriendlyCallStackLabels(StackTraceCallback stackTraceCallback) {
        ArrayList<String> labels = new ArrayList<>();
        labels.add("IP");

        if (stackTraceCallback == null || stackTraceCallback.stackFrames == null) {
            return labels;
        }

        int totalFrames = stackTraceCallback.stackFrames.size();
        for (int i2 = 0; i2 < totalFrames; i2++) {
            StackFrame frame = stackTraceCallback.stackFrames.get(totalFrames - i2 - 1);
            labels.add(toFriendlyStackFrameLabel(frame));
        }
        return labels;
    }

    @Override
    public String toFriendlyStackFrameLabel(StackFrame frame) {
        // User functions have positive indices.
        Integer userFuncIndex = NumberUtil.parseIntOrNull(frame.name);
        if (userFuncIndex == null) {
            return frame.name;
        }

        if (userFuncIndex >= 0) {
            return compiler.getUserFunctionName(userFuncIndex) + "()";
        }

        Integer returnAddr = NumberUtil.parseIntOrNull(frame.instructionPointer);
        String gosubLabel = returnAddr != null ? compiler.describeStackCall(returnAddr) : "???";
        return "gosub " + gosubLabel;
    }

    @Override
    public StackTraceCallback toVmViewFriendlyCallStack(StackTraceCallback stackTraceCallback) {
        StackTraceCallback friendly = new StackTraceCallback();
        for (String label : buildFriendlyCallStackLabels(stackTraceCallback)) {
            StackFrame frame = new StackFrame();
            frame.name = label;
            frame.source = "";
            frame.line = 0;
            friendly.stackFrames.add(frame);
        }
        friendly.totalFrames = friendly.stackFrames.size();
        return friendly;
    }

    @Override
    public int getSourceFromMain(String filename, int sourceLine) {
        return preprocessor.getLineNumberMap().getSourceFromMain(filename, sourceLine);
    }

    @Override
    public FileLineNumber getFileLineNumberFromMain(int sourceLine) {
        // Find corresponding source position
        Mutable<String> filename = new Mutable<>("");
        Mutable<Integer> fileRow = new Mutable<>(0);

        preprocessor.getLineNumberMap().getSourceFromMain(filename, fileRow, sourceLine);

        return new FileLineNumber(filename.get(), fileRow.get());
    }

    @Override
    public Iterable<VariableDefinition> getVariableDefinitions() {
        ArrayList<VariableDefinition> variableDefinitions = new ArrayList<>();
        for (com.basic4gl.language.core.types.VariableCollection.Variable variable :
                compiler.getProgram().getVariables().getVariables()) {
            if (variable.name == null || variable.name.isEmpty()) continue;
            String typeStr = LanguageUtil.getTypeString(variable.type);
            String signature = typeStr + " " + variable.name;
            TypeDefinition typeDefinition = LanguageUtil.toTypeDefinition(variable.type);

            VariableDefinition definition = null; // TODO new VariableDefinition(variable.name,)
            variableDefinitions.add(definition);
        }
        return variableDefinitions;
    }

    public Iterable<LabelDefinition> getLabelDefinitions() {
        ArrayList<LabelDefinition> labelDefinitions = new ArrayList<>();
        return compiler.getLabelNames().stream()
                .map(labelName -> {
                    String usage = "gosub " + labelName + ": goto" + labelName;
                    return new LabelDefinition(labelName, labelName + ":", usage);
                })
                .toList();
    }

    public Iterable<FunctionDefinition> getFunctionDefinitions() {
        Map<Integer, String> functionLibraryBySpecIndex = buildFunctionLibraryBySpecIndex();
        ArrayList<FunctionDefinition> items = new ArrayList<>();
        for (String key : compiler.getFunctionIndex().keySet()) {
            for (Integer index : compiler.getFunctionIndex().get(key)) {
                String name = key;
                FunctionSpecification spec = compiler.getFunctions().get(index);
                String libraryName = functionLibraryBySpecIndex.getOrDefault(index, "Builtin");
                String library = libraryName != null ? libraryName : "Builtin";
                StringBuilder signature = new StringBuilder();
                if (spec.isFunction()) {
                    signature
                            .append(LanguageUtil.getTypeString(spec.getReturnType()))
                            .append(' ');
                }
                signature.append(name);
                signature.append(spec.hasBrackets() ? "(" : " ");
                boolean needComma = false;
                Vector<ValType> params = spec.getParamTypes().getParams();
                StringBuilder argsOnly = new StringBuilder();
                if (params != null) {
                    for (ValType type : params) {
                        if (needComma) {
                            signature.append(", ");
                            argsOnly.append(", ");
                        }
                        String typeName = LanguageUtil.getTypeString(type);
                        signature.append(typeName);
                        argsOnly.append(typeName);
                        needComma = true;
                    }
                }
                if (spec.hasBrackets()) {
                    signature.append(')');
                }

                //                TypeDefinition returnType = spec.isFunction() ? new
                // TypeDefinition(LanguageUtil.getTypeString(spec.getReturnType())) : new TypeDefinition("void");
                //                FunctionDefinition definition = new FunctionDefinition(
                //                        name,
                //                        signature.toString(),
                //                        returnType,
                //                        params != null ? params.stream()
                //                                .map(this::getTypeString)
                //                                .map((typeName, i) -> new VariableDefinition(typeName))
                //                                .toArray(VariableDefinition[]::new) : new VariableDefinition[0],
                //                        spec.getDescription(),
                //                        library
                //                );
                // TODO
                FunctionDefinition definition = null;
                items.add(definition);
            }
        }

        items.addAll(buildUserFunctionReferenceItems());

        return items;
    }

    private ArrayList<FunctionDefinition> buildUserFunctionReferenceItems() {
        ArrayList<FunctionDefinition> items = new ArrayList<>();
        Map<String, Integer> funcIndex = compiler.getGlobalUserFunctionIndex();
        java.util.Vector<com.basic4gl.language.core.stackframe.UserFunc> functions =
                compiler.getProgram().getUserFunctions();
        java.util.Vector<com.basic4gl.language.core.stackframe.UserFuncPrototype> prototypes =
                compiler.getProgram().getUserFunctionPrototypes();
        for (Map.Entry<String, Integer> entry : funcIndex.entrySet()) {
            String name = entry.getKey();
            int funcIdx = entry.getValue();
            com.basic4gl.language.core.stackframe.UserFuncPrototype prototype = null;
            if (funcIdx >= 0 && funcIdx < functions.size()) {
                int protoIdx = functions.get(funcIdx).prototypeIndex;
                if (protoIdx >= 0 && protoIdx < prototypes.size()) {
                    prototype = prototypes.get(protoIdx);
                }
            }
            StringBuilder signature = new StringBuilder();
            if (prototype != null && prototype.hasReturnVal) {
                signature
                        .append(LanguageUtil.getTypeString(prototype.returnValType))
                        .append(' ');
            }
            signature.append(name).append('(');
            if (prototype != null && prototype.paramCount > 0) {
                String[] params = new String[prototype.paramCount];
                for (Map.Entry<String, Integer> v : prototype.localVarIndex.entrySet()) {
                    int idx = v.getValue();
                    if (idx < prototype.paramCount && idx < prototype.localVarTypes.size()) {
                        params[idx] = LanguageUtil.getTypeString(prototype.localVarTypes.get(idx)) + " " + v.getKey();
                    }
                }
                boolean needComma = false;
                for (String param : params) {
                    if (needComma) signature.append(", ");
                    signature.append(param != null ? param : "?");
                    needComma = true;
                }
            }
            signature.append(')');

            FunctionDefinition definition = null; // TODO new FunctionDefinition(name, ...)
            items.add(definition);
        }

        return items;
    }

    private Map<Integer, String> buildFunctionLibraryBySpecIndex() {
        Map<Integer, String> functionLibraryBySpecIndex = new HashMap<>();

        int specCursor = 0;
        for (Library library : compiler.getLibraries()) {
            if (!(library instanceof FunctionLibrary functionLibrary)) {
                continue;
            }
            Map<String, FunctionSpecification[]> specs = functionLibrary.specs();
            if (specs == null) {
                continue;
            }
            int count = 0;
            for (FunctionSpecification[] overloads : specs.values()) {
                if (overloads != null) {
                    count += overloads.length;
                }
            }
            for (int i = 0; i < count; i++) {
                String libName = library.name();
                if (libName != null) {
                    functionLibraryBySpecIndex.put(specCursor + i, library.name());
                }
            }
            specCursor += count;
        }
        return functionLibraryBySpecIndex;
    }

    public Iterable<VariableDefinition> getConstantDefinitions() {

        Map<String, String> constantLibraryByName = buildConstantLibraryByName();
        java.util.List<VariableDefinition> items = new ArrayList<>();
        for (String key : compiler.getConstants().keySet()) {
            String library = constantLibraryByName.getOrDefault(key.toLowerCase(Locale.ROOT), "Builtin");
            if (library == null) {
                library = "Builtin";
            }
            Constant constant = compiler.getConstants().get(key);
            String signature = key + " = (" + LanguageUtil.getTypeString(constant.getType()) + ") " + constant;

            items.add(new VariableDefinition(
                    key,
                    signature,
                    LanguageUtil.toTypeDefinition(constant.getType()),
                    constant.toString(),
                    "",
                    library,
                    true,
                    "",
                    "Builtin"));
        }
        return items;
    }

    private Map<String, String> buildConstantLibraryByName() {
        Map<String, String> constantLibraryByName = new HashMap<>();
        for (Library library : compiler.getLibraries()) {
            if (!(library instanceof FunctionLibrary functionLibrary)) {
                continue;
            }
            Map<String, com.basic4gl.language.core.types.Constant> constants = functionLibrary.constants();
            if (constants == null) {
                continue;
            }
            for (String name : constants.keySet()) {
                String libName = library.name();
                if (libName != null) {
                    constantLibraryByName.put(name.toLowerCase(Locale.ROOT), library.name());
                }
            }
        }
        return constantLibraryByName;
    }

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

}
