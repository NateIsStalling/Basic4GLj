package com.basic4gl.lib.util;

public interface DebuggerTaskCallback {
	public void onDebuggerConnected();
	public abstract void message(DebuggerCallbackMessage message);
	public abstract void message(CallbackMessage message);
	public abstract void messageObject(Object message);

    void onDebuggerDisconnected();
}
