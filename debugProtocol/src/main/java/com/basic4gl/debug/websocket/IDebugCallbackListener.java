package com.basic4gl.debug.websocket;

import com.basic4gl.debug.protocol.callbacks.Callback;
import com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage;

public interface IDebugCallbackListener {
    void OnDebugCallbackReceived(DebuggerCallbackMessage callback);
    void OnCallbackReceived(Callback callback);
    void OnDisconnected();
}
