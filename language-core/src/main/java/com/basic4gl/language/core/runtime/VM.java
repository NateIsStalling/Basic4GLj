package com.basic4gl.language.core.runtime;

import com.basic4gl.language.core.extensions.Basic4GLLongRunningFunction;
import com.basic4gl.language.core.streaming.ProgramStreamable;
import com.basic4gl.language.core.types.TypeLibrary;

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
}
