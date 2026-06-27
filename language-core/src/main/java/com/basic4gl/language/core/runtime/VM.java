package com.basic4gl.language.core.runtime;

import com.basic4gl.language.core.extensions.Basic4GLLongRunningFunction;
import com.basic4gl.language.core.internal.Mutable;
import com.basic4gl.language.core.stackframe.UserFuncStackFrame;
import com.basic4gl.language.core.streaming.ProgramStreamable;
import com.basic4gl.language.core.types.TypeLibrary;
import com.basic4gl.language.core.types.ValType;
import com.basic4gl.language.core.types.VariableCollection;
import java.util.Vector;

public interface VM {

    String getStringParam(int i);

    Value getReg();

    Data getData();

    TypeLibrary getDataTypes();

    Integer getIntParam(int i);

    Float getRealParam(int i);

    void functionError(String error);

    void setRegString(String value);

    boolean checkNullRefParam(int i);

    Value getRefParam(int i);

    void beginLongRunningFunction(Basic4GLLongRunningFunction handler);

    void endLongRunningFunction();

    int getIP();

    ProgramStreamable stream();

    void gotoInstruction(int saveIP);

    String getString(int stringIndex);

    String getRegString();

    Value getReg2();

    boolean hasError();

    boolean isDone();

    boolean isPaused();

    boolean isEndCallback();

    void miscError(String message);

    void continueVM(int steps);

    void pause();

    void resetVM();

    void addStepBreakPoints(boolean stepInto);

    boolean addStepOutBreakPoint();

    void clearTempBreakPoints();

    Instruction[] getInstructions();

    VariableCollection getVariables();

    String getOpCodeData(Instruction data, IFunctionIndex functions);

    Vector<UserFuncStackFrame> getUserCallStack();

    void repatchBreakpoints();

    String getReg2String();

    Store<String> getStringStore();

    // TODO would prefer to not expose Mutable and try to keep it within the internal package
    String valToString(Value value, ValType valueType, Mutable<Integer> maxChars);

    VMState getState();

    int getCurrentUserFrame();

    int getInstructionCount();

    Instruction getInstruction(int index);

    String getError();

    void setState(VMState state);

    boolean isIPValid();

    InstructionPosition getIPInSourceCode();

    void patchIn();

    void stop();

    void clearResources();

    String getValueString(VariableCollection.Variable vmVariable);

    String getDisplayVariable(ValType valType);
}
