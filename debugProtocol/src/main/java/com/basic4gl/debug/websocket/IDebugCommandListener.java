package com.basic4gl.debug.websocket;

import com.basic4gl.debug.protocol.commands.DebugCommand;

public interface IDebugCommandListener {
    void OnDebugCommandReceived(DebugCommand command);
}
