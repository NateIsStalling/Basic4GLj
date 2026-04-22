package com.basic4gl.desktop.vmview;

public interface DebugControlsListener {
    void onPlayPauseRequested();

    void onStepRequested();

    void onStepOverRequested();

    void onStepOutRequested();
}
