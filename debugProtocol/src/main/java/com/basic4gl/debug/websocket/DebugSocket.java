package com.basic4gl.debug.websocket;
//
//  ========================================================================
//  Copyright (c) Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

import com.basic4gl.debug.protocol.callbacks.CallbackMessage;
import com.basic4gl.debug.protocol.callbacks.DebugCallback;
import com.basic4gl.debug.protocol.commands.DebugCommand;
import com.basic4gl.debug.protocol.commands.DebugCommandAdapter;
import com.google.gson.Gson;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

//TODO https://stackoverflow.com/questions/17080216/how-to-send-message-to-particular-websocket-connection-using-java-server

//@ClientEndpoint
@ServerEndpoint(value = "/debug/")
public class DebugSocket
{
    private static Map<UUID, Session> sessionRepository = new HashMap<UUID, Session>();

    private CountDownLatch closureLatch = new CountDownLatch(1);

    private Session session;

    private UUID sessionId;

    private DebugCommandAdapter adapter;

    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
        UUID sessionId = UUID.randomUUID();
        sessionRepository.put(sessionId, sess);

        this.session = sess;
        this.sessionId = sessionId;

        this.adapter = new DebugCommandAdapter(new Gson());

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
//        CallbackMessage callback = CallbackMessage.FromJson(message);
//        if (callback != null) {
////            sendClient(callback.text);
//            Set<Map.Entry<UUID, Session>> sessions = sessionRepository.entrySet();
//            for (Map.Entry<UUID, Session> entry: sessions) {
//                if (!entry.getKey().equals(sessionId)) {
//                    sendClient(entry.getValue(), message);
//                }
//            }
//
//        } else {
//
//            DebugCommand command = adapter.FromJson(message);
//
//            if (command != null && command.isValid()) {
////                sendClient(command.getClass().getName());
//                sendClient(message);
//            } else {
//                sendClient(message);
//            }
//        }


        if (message.toLowerCase(Locale.US).contains("bye"))
        {
            sess.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Thanks"));
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

    private void sendError(Session session, String err) {
        this.sendClient(session, String.format("{\"msg\": \"error\", \"error\": \"%s\"}", err));
    }
}
