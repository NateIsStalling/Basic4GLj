package com.basic4gl.desktop;

public interface IEditorPresenter {
    public void onModeChanged(ApMode mode, String statusMsg);
    void RefreshDebugDisplays(ApMode mode);

    void PlaceCursorAtProcessed(final int line, int col);

    void RefreshActions(ApMode mode);

    void onPause();

    void onApplicationClosing();

    void setCompilerStatus(String error);
}
