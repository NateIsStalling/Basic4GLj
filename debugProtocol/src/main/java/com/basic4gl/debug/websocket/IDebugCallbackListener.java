package com.basic4gl.debug.websocket;

import com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage;

public interface IDebugCallbackListener {
    public void OnDebugCallbackReceived(DebuggerCallbackMessage callback);
}
