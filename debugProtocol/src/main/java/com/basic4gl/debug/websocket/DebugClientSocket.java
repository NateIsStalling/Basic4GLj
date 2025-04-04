package com.basic4gl.debug.websocket;

import com.basic4gl.debug.ConsoleLogger;
import com.basic4gl.debug.ILogger;
import com.basic4gl.debug.protocol.callbacks.Callback;
import com.basic4gl.debug.protocol.callbacks.CallbackFactory;
import com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage;
import com.basic4gl.debug.protocol.commands.DebugCommand;
import com.basic4gl.debug.protocol.commands.DebugCommandFactory;
import com.basic4gl.debug.protocol.commands.DisconnectCommand;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import javax.websocket.*;

@ClientEndpoint
public class DebugClientSocket {
    private static final ILogger logger = new ConsoleLogger();

    private final CountDownLatch closureLatch = new CountDownLatch(1);
    private Session session;
    private DebugCommandFactory commandFactory;
    private CallbackFactory callbackFactory;

    private final IDebugCommandListener commandListener;
    private final IDebugCallbackListener callbackListener;

    public DebugClientSocket(IDebugCommandListener commandListener, IDebugCallbackListener callbackListener) {
        this.commandListener = commandListener;
        this.callbackListener = callbackListener;
    }

    @OnOpen
    public void onWebSocketConnect(Session sess) {
        Gson gson = new Gson();

        this.session = sess;
        this.commandFactory = new DebugCommandFactory(gson);
        this.callbackFactory = new CallbackFactory(gson);

        logger.log("Socket Connected: " + sess);
    }

    @OnMessage
    public void onWebSocketText(Session sess, String message) throws IOException {
        logger.log("Client Received TEXT message: " + message);

        DebugCommand command = commandFactory.fromJson(message);
        Callback callback = callbackFactory.fromJson(message);

        // handle terminated command
        if (command != null && Objects.equals(command.getCommand(), DisconnectCommand.COMMAND)) {
            sess.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Debug Session Terminated"));
        }

        if (command != null && command.isValid()) {
            logger.log("Client processing command");
            commandListener.onDebugCommandReceived(command);
        } else if (callback != null) {
            logger.log("Client processing callback");
            callbackListener.onCallbackReceived(callback);
        } else {
            // TODO 12/2022 migrate to separate Callback subtypes to align with DAP spec
            DebuggerCallbackMessage debuggerCallbackMessage = DebuggerCallbackMessage.fromJson(message);
            if (debuggerCallbackMessage != null) {
                logger.log("Client processing callback");
                callbackListener.onDebugCallbackReceived(debuggerCallbackMessage);
            } else {
                logger.log("Client ignoring message");
            }
        }
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
        callbackListener.onDisconnected();
        commandListener.onDisconnected();

        logger.log("Socket Closed: " + reason);
        closureLatch.countDown();
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        logger.error(cause);
    }

    public void awaitClosure() throws InterruptedException {
        logger.log("Awaiting closure from remote");
        closureLatch.await();
    }

    private void sendClient(String str) {
        try {
            this.session.getBasicRemote().sendText(str);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
