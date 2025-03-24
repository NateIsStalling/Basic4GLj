package com.basic4gl.desktop;

import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import java.io.File;
import java.util.List;

interface IEditorPresenter {
    void onModeChanged(ApMode mode, String statusMsg);

    void refreshDebugDisplays(ApMode mode);

    void placeCursorAtProcessed(final int line, int col);

    void refreshActions(ApMode mode);

    void onPause();

    void onApplicationClosing();

    void setCompilerStatus(String error);

    void updateCallStack(StackTraceCallback message);

    void updateEvaluateWatch(String evaluatedWatch, String result);

    void refreshWatchList();

    void setRecentItems(List<File> files);
}
