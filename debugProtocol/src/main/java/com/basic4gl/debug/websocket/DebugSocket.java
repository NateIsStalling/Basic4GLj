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
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

//TODO https://stackoverflow.com/questions/17080216/how-to-send-message-to-particular-websocket-connection-using-java-server

//@ClientEndpoint
@ServerEndpoint(value = "/debug/")
public class DebugSocket
{
    private CountDownLatch closureLatch = new CountDownLatch(1);
    private Session session;
    private DebugCommandAdapter adapter;

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
        System.out.println("Server Received TEXT message: " + message);

        CallbackMessage callback = CallbackMessage.FromJson(message);
        if (callback != null) {
            sendClient(callback.text);
        } else {
            DebugCommand command = adapter.FromJson(message);

            if (command != null && command.isValid()) {
                sendClient(command.getClass().getName());
            } else {
                sendClient(message);
            }
        }


        if (message.toLowerCase(Locale.US).contains("bye"))
        {
            sess.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Thanks"));
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

    private void sendError(String err) {
        this.sendClient(String.format("{\"msg\": \"error\", \"error\": \"%s\"}", err));
    }
}
