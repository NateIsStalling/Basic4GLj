package com.basic4gl.desktop;

import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;

public interface IEditorPresenter {
    public void onModeChanged(ApMode mode, String statusMsg);
    void RefreshDebugDisplays(ApMode mode);

    void PlaceCursorAtProcessed(final int line, int col);

    void RefreshActions(ApMode mode);

    void onPause();

    void onApplicationClosing();

    void setCompilerStatus(String error);

    void updateCallStack(StackTraceCallback message);

    void updateEvaluateWatch(String evaluatedWatch, String result);
}
