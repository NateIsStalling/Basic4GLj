package com.basic4gl.desktop.debugger;

import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.basic4gl.lib.util.Library;
import org.eclipse.jetty.client.HttpClient;

public class RemoteDebugger implements IDebugger {
    public void debugStart() {
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void continueApplication() {

    }

    @Override
    public void pauseApplication() {
        WebSocketClient client = new WebSocketClient();
        URI uri = URI.create("ws://localhost:8080/events/");
        try
        {
            client.start();
            // The socket that receives events
            EventSocket socket = new EventSocket();
            // Attempt Connect

            Future<Session> fut = client.connect(socket, uri);
            // Wait for Connect
            Session session = fut.get();

            // Send a message
            session.getRemote().sendString("Hello");

            // Send another message
            session.getRemote().sendString("Goodbye");

            // Wait for other side to close
            socket.awaitClosure();

            // Close session
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void resumeApplication() {

    }

    @Override
    public void runApplication(Library builder, String currentDirectory, String libraryPath) {

    }

    @Override
    public void stopApplication() {

    }

    @Override
    public void step(int type) {

    }

    @Override
    public void toggleBreakpoint(String filename, int line) {

    }

    @Override
    public String evaluateWatch(String watch, boolean canCallFunc) {
        return null;
    }
}
