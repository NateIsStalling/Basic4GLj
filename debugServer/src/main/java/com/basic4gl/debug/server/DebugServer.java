package com.basic4gl.debug.server;

import com.basic4gl.debug.websocket.DebugSocket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

public class DebugServer {
private static final int DEFAULT_DEBUG_SERVER_PORT = 8080;

public static void main(String[] args) {
	try {

	int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_DEBUG_SERVER_PORT;

	System.out.println("Starting debug server on port " + port);

	Server server = new Server();
	ServerConnector connector = new ServerConnector(server);
	connector.setPort(port);
	server.addConnector(connector);

	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	context.setContextPath("/");
	server.setHandler(context);

	// Initialize javax.websocket layer
	WebSocketServerContainerInitializer.configure(
		context,
		(servletContext, wsContainer) -> {
			// Configure defaults for container
			wsContainer.setDefaultMaxTextMessageBufferSize(65535);

			// Add WebSocket endpoint to javax.websocket layer
			wsContainer.addEndpoint(DebugSocket.class);
		});

	server.start();
	server.join();

	System.out.println("Debug server started on port " + port);
	} catch (Throwable t) {
	t.printStackTrace(System.err);
	}
}
}
