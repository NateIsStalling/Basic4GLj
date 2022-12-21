package com.basic4gl.debug.websocket;

import com.basic4gl.debug.protocol.callbacks.CallbackMessage;
import com.basic4gl.debug.protocol.callbacks.DebugCallback;

public interface IDebugCallbackListener {
    public void OnDebugCallbackReceived(CallbackMessage callback);
}
