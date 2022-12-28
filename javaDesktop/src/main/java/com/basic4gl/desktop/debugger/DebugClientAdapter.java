package com.basic4gl.desktop.debugger;

import com.basic4gl.debug.protocol.callbacks.DebuggerCallbackMessage;
import com.basic4gl.debug.protocol.commands.DebugCommand;
import com.basic4gl.debug.websocket.DebugClientSocket;
import com.basic4gl.debug.websocket.IDebugCallbackListener;
import com.basic4gl.debug.websocket.IDebugCommandListener;
import com.google.gson.Gson;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;

public class DebugClientAdapter implements IDebugCommandListener {
    WebSocketContainer container;
    Session session;
    IDebugCallbackListener callbackListener;
    public DebugClientAdapter(IDebugCallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    public void connect() {

        URI uri = URI.create("ws://localhost:6796/debug/");

        try
        {
            container = ContainerProvider.getWebSocketContainer();

            try
            {
                // Create client side endpoint
                DebugClientSocket clientEndpoint = new DebugClientSocket(this, callbackListener);

                // Attempt Connect
                session = container.connectToServer(clientEndpoint,uri);

//                callMeMaybe.session = session;
//
//                // Send a message
//                session.getBasicRemote().sendText("Hello");
//
//                // Send another message
//                session.getBasicRemote().sendText("Goodbye");
//
//                // Wait for remote to close
//                clientEndpoint.awaitClosure();
//
//                // Close session
//                session.close();
            }
            finally
            {
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
//                LifeCycle.stop(container);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }

    public void stop() {
        try {
            System.out.println("stopping debugger");
            if (session != null) {
                session.close();
            }
            System.out.println("session closed");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            // Force lifecycle stop when done with container.
            // This is to free up threads and resources that the
            // JSR-356 container allocates. But unfortunately
            // the JSR-356 spec does not handle lifecycles (yet)
            LifeCycle.stop(container);
        }
    }

    @Override
    public void OnDebugCommandReceived(DebugCommand command) {

    }

    public void message(DebugCommand command) {
        Gson gson = new Gson();
        if (session != null && session.isOpen()) {
            try {

                String json = gson.toJson(command);
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
