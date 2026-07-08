package com.basic4gl.language.core.streaming;

import com.basic4gl.language.core.internal.Mutable;
import com.basic4gl.language.core.runtime.*;
import com.basic4gl.language.core.stackframe.UserFunc;
import com.basic4gl.language.core.stackframe.UserFuncPrototype;
import com.basic4gl.language.core.types.TypeLibrary;
import com.basic4gl.language.core.types.ValType;
import com.basic4gl.language.core.types.VariableCollection;
import java.util.Vector;

public interface ProgramStreamable extends Streamable {
    void clearProgram();

    TypeLibrary getDataTypes();

    Data getData();

    int newCodeBlock();

    int getInstructionCount();

    Instruction getInstruction(int jumpInstruction);

    Vector<UserFunc> getUserFunctions();

    Vector<UserFuncPrototype> getUserFunctionPrototypes();

    Vector<ProgramDataElement> getProgramData();

    int getStoreTypeIndex(ValType type);

    VariableCollection getVariables();

    int addFunction(Function instance);

    void storeProgramData(int type, Value v);

    int storeStringConstant(String text);

    boolean isCodeBlockValid(int currentCodeBlockIndex);

    int getCodeBlockOffset(int index);

    CodeBlock getCodeBlock(int currentCodeBlockIndex);

    RollbackPoint getRollbackPoint();

    void rollback(RollbackPoint vmRollback);

    void addInstruction(Instruction instruction);

    void rollbackProgram(int size);

    void removeLastInstruction();

    CodeBlock getCurrentCodeBlock();

    Register evaluateExpression(int expressionStart);

    boolean hasError();

    String getError();

    void addResources(Resources resources);

    void addInitFunction(Function function);

    void setInstruction(int i, Instruction instruction);

    int getCurrentCodeBlockIndex();

    void getIPInSourceCode(Mutable<Integer> line, Mutable<Integer> col);

    void clearError();
}
