package com.basic4gl.lib.util;

public interface DebuggerTaskCallback {
    void onDebuggerConnected();

    void message(DebuggerCallbackMessage message);

    void message(CallbackMessage message);

    void messageObject(Object message);

    void onDebuggerDisconnected();
}
