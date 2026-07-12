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
}
