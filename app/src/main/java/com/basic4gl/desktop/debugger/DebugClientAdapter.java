package com.basic4gl.desktop.debugger;

import com.basic4gl.debug.protocol.commands.DebugCommand;
import com.basic4gl.debug.websocket.DebugClientSocket;
import com.basic4gl.debug.websocket.IDebugCallbackListener;
import com.basic4gl.debug.websocket.IDebugCommandListener;
import com.google.gson.Gson;
import java.net.URI;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.eclipse.jetty.util.component.LifeCycle;

public class DebugClientAdapter implements IDebugCommandListener {
private WebSocketContainer container;
private Session session;
private final IDebugCallbackListener callbackListener;
private final String debugServerPort;

private int lastRequestId;

public DebugClientAdapter(IDebugCallbackListener callbackListener, String debugServerPort) {
	this.callbackListener = callbackListener;
	this.debugServerPort = debugServerPort;
}

public void connect() {

	URI uri = URI.create("ws://localhost:" + debugServerPort + "/debug/");

	try {
	container = ContainerProvider.getWebSocketContainer();

	// Create client side endpoint
	DebugClientSocket clientEndpoint = new DebugClientSocket(this, callbackListener);

	// Attempt Connect
	session = container.connectToServer(clientEndpoint, uri);
	} catch (Throwable t) {
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

public void onDebugCommandReceived(DebugCommand command) {}

public void onDisconnected() {
	callbackListener.onDisconnected();
}

public int message(DebugCommand command) {
	Gson gson = new Gson();
	if (session != null && session.isOpen()) {
	try {
		int requestId = newRequestId();

		command.setId(requestId);
		String json = gson.toJson(command);
		session.getBasicRemote().sendText(json);

		return requestId;
	} catch (Exception e) {
		e.printStackTrace();
	}
	}

	return 0;
}

private int newRequestId() {
	// TODO consider synchronized block
	int requestId = lastRequestId + 1;
	lastRequestId = requestId;
	return requestId;
}
}
