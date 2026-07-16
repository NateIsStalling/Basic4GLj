package com.basic4gl.desktop.debugger;

import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.desktop.editor.ApMode;

public interface IDebugPresenter {

    void updateCallStack(StackTraceCallback message);

    void refreshWatchList();

    void updateEvaluateWatch(String evaluatedWatch, String result);

    void clearCallStack();

    void refreshDebugControls(ApMode mode);
}
