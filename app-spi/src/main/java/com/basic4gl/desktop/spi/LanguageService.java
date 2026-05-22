package com.basic4gl.desktop.spi;

import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.desktop.spi.language.FunctionDefinition;
import com.basic4gl.desktop.spi.language.LabelDefinition;
import com.basic4gl.desktop.spi.language.VariableDefinition;

import java.util.ArrayList;
import java.util.List;

public interface LanguageService {

    public void onLoad(PluginContext context);

    public void onUnload();

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
}
