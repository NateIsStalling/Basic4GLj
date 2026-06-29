package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.GLTextGridWindow;
import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.desktop.spi.FileLineNumber;
import com.basic4gl.desktop.spi.LanguageService;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.language.adapter.util.NumberUtil;
import com.basic4gl.language.core.extensions.FunctionLibrary;
import com.basic4gl.language.core.extensions.Library;
import com.basic4gl.language.core.internal.Mutable;
import com.basic4gl.language.core.runtime.IServiceCollection;
import com.basic4gl.language.spi.PluginManager;
import com.basic4gl.language.spi.PluginLibrary;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

public class Basic4GLLanguageService implements LanguageService {

    private final TomBasicCompiler compiler;
    private final Preprocessor preprocessor;
    private final PluginManager pluginManager;

    Basic4GLLanguageService(TomBasicCompiler compiler, Preprocessor preprocessor, PluginManager pluginManager) {
        this.compiler = compiler;
        this.preprocessor = preprocessor;
        this.pluginManager = pluginManager;
    }

    @Override
    public void onLoad(PluginContext context) {

    }

    @Override
    public void onUnload() {

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
        LinkedHashSet<String> functions = new LinkedHashSet<>(compiler.getFunctionIndex().keySet());
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
        return Stream.concat(
                compiler.getBinaryOperators().stream(),
                compiler.getUnaryOperators().stream())
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
}
