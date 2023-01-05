package com.basic4gl.debug.websocket;

import com.basic4gl.debug.protocol.callbacks.CallbackMessage;
import com.basic4gl.debug.protocol.commands.DebugCommand;
import com.basic4gl.debug.protocol.commands.DebugCommandFactory;
import com.basic4gl.debug.protocol.commands.DisconnectCommand;
import com.google.gson.Gson;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

//TODO https://stackoverflow.com/questions/17080216/how-to-send-message-to-particular-websocket-connection-using-java-server

@ServerEndpoint(value = "/debug/")
public class DebugSocket
{
    private static Map<UUID, Session> sessionRepository = new HashMap<UUID, Session>();

    private CountDownLatch closureLatch = new CountDownLatch(1);

    private Session session;

    private UUID sessionId;

    private DebugCommandFactory adapter;

    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
        UUID sessionId = UUID.randomUUID();
        sessionRepository.put(sessionId, sess);

        this.session = sess;
        this.sessionId = sessionId;

        this.adapter = new DebugCommandFactory(new Gson());

        System.out.println("Socket Connected: " + sess);
    }

    @OnMessage
    public void onWebSocketText(Session sess, String message) throws IOException
    {
        System.out.println("Server Received TEXT message: " + message);

        Set<Map.Entry<UUID, Session>> sessions = sessionRepository.entrySet();
        for (Map.Entry<UUID, Session> entry: sessions) {
            if (!entry.getKey().equals(sessionId)) {
                sendClient(entry.getValue(), message);
            }
        }

        // handle terminated command
        DebugCommand command = adapter.FromJson(message);
        if (command != null && Objects.equals(command.getCommand(), DisconnectCommand.COMMAND)) {
            sess.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Debug Session Terminated"));
        }
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        sessionRepository.remove(sessionId);

        System.out.println("Socket Closed: " + reason);

        // Notify other processes debug session has disconnected
        CallbackMessage callbackMessage = new CallbackMessage(CallbackMessage.STOPPED, "closed");
        Gson gson = new Gson();
        String message = gson.toJson(callbackMessage);

        Set<Map.Entry<UUID, Session>> sessions = sessionRepository.entrySet();
        for (Map.Entry<UUID, Session> entry: sessions) {
            if (!entry.getKey().equals(sessionId)) {
                sendClient(entry.getValue(), message);
            }
        }

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

    private void sendClient(Session session, String str) {
        try {
            session.getBasicRemote().sendText(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
