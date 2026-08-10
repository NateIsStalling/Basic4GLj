package com.basic4gl.language.core.runtime;

public interface ILongRunningFunctionListener {
    void onLongRunningFunctionDone(boolean cancelled);
}
