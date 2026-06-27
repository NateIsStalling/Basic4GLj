package com.basic4gl.language.core.extensions;

import com.basic4gl.language.core.runtime.CodeBlock;
import com.basic4gl.language.core.runtime.IFunctionIndex;
import com.basic4gl.language.core.runtime.RuntimeFunctionRollbackPoint;
import com.basic4gl.language.core.streaming.ProgramStreamable;
import com.basic4gl.language.core.types.Constant;
import com.basic4gl.language.core.types.FunctionSpecification;
import com.basic4gl.language.core.types.ValType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Map;

public interface Basic4GLCompiler extends IFunctionIndex {
    //    Basic4GLFunctionRegistry getPlugins();

    ProgramStreamable getProgram();

    boolean tempCompileExpression(String watch, ValType valType, boolean inFunction, int currentFunction);

    String getError();

    void addConstants(Map<String, Constant> constants);

    void addFunctions(Library lib, Map<String, FunctionSpecification[]> specs);

    long getTokenLine();

    long getTokenColumn();

    Basic4GLParser getParser();

    String getSymbolPrefix();

    void setSymbolPrefix(String s);

    Map<String, Integer> getGlobalUserFunctionIndex();

    boolean compileOntoEnd();

    void clearError();

    void rollback(RuntimeFunctionRollbackPoint rollbackPoint);

    CodeBlock getCurrentCodeBlock();

    RuntimeFunctionRollbackPoint getRollbackPoint();

    void streamOut(DataOutputStream output);

    boolean streamIn(DataInputStream input);

    ArrayList<Library> getLibraries();

    //    com.basic4gl.compiler.Parser getParser();
}
