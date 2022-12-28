package com.basic4gl.debug.websocket;

import com.basic4gl.debug.protocol.callbacks.CallbackMessage;
import com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage;
import com.basic4gl.debug.protocol.commands.DebugCommand;
import com.basic4gl.debug.protocol.commands.DebugCommandAdapter;
import com.basic4gl.debug.protocol.commands.TerminateCommand;
import com.google.gson.Gson;

import javax.websocket.*;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class DebugClientSocket {
    private CountDownLatch closureLatch = new CountDownLatch(1);
    private Session session;
    private DebugCommandAdapter adapter;

    private IDebugCommandListener commandListener;
    private IDebugCallbackListener callbackListener;

    public DebugClientSocket(
        IDebugCommandListener commandListener,
        IDebugCallbackListener callbackListener) {
        this.commandListener = commandListener;
        this.callbackListener = callbackListener;
    }

    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
        this.session = sess;
        this.adapter = new DebugCommandAdapter(new Gson());

        System.out.println("Socket Connected: " + sess);
    }

    @OnMessage
    public void onWebSocketText(Session sess, String message) throws IOException
    {
        System.out.println("Client Received TEXT message: " + message);

        DebugCommand command = adapter.FromJson(message);

        // handle terminated command
        if (command != null && Objects.equals(command.getCommand(), TerminateCommand.COMMAND)) {
            sess.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Debug Session Terminated"));
        }

        if (command != null && command.isValid()) {
            System.out.println("Client processing command");
            //sendClient(command.getClass().getName());
            commandListener.OnDebugCommandReceived(command);
        } else {
            DebuggerCallbackMessage callback = DebuggerCallbackMessage.FromJson(message);
            if (callback != null) {
                System.out.println("Client processing callback");
                //sendClient(callback.text);
                callbackListener.OnDebugCallbackReceived(callback);
            } else {
                System.out.println("Client ignoring message");
                //sendClient(message);
            }
        }
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        System.out.println("Socket Closed: " + reason);
        closureLatch.countDown();
    }

    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }

    public void awaitClosure() throws InterruptedException
    {
        System.out.println("Awaiting closure from remote");
        closureLatch.await();
    }

    private void sendClient(String str) {
        try {
            this.session.getBasicRemote().sendText(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
