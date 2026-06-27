package com.basic4gl.language.core.runtime;

public interface DebuggerTaskCallback {
    void onDebuggerConnected();

    void message(com.basic4gl.language.core.runtime.DebuggerCallbackMessage message);

    void message(CallbackMessage message);

    void messageObject(Object message);

    void onDebuggerDisconnected();
}
