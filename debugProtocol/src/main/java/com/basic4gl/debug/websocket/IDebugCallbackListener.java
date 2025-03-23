package com.basic4gl.debug.websocket;

import com.basic4gl.debug.protocol.callbacks.Callback;
import com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage;

public interface IDebugCallbackListener {
	void onDebugCallbackReceived(DebuggerCallbackMessage callback);

	void onCallbackReceived(Callback callback);

	void onDisconnected();
}
