package com.basic4gl.debug.websocket;

import com.basic4gl.debug.ConsoleLogger;
import com.basic4gl.debug.ILogger;
import com.basic4gl.debug.protocol.callbacks.CallbackMessage;
import com.basic4gl.debug.protocol.commands.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

// TODO
// https://stackoverflow.com/questions/17080216/how-to-send-message-to-particular-websocket-connection-using-java-server

@ServerEndpoint(value = "/debug/")
public class DebugSocket {
  private static ILogger logger = new ConsoleLogger();

  private static Gson gson = new Gson();

  private static Map<UUID, Session> sessionRepository = new HashMap<>();

  private static ArrayList<DebugCommand> initializeCommandsQueue = new ArrayList<>();

  private static boolean configurationDone = false;

  private CountDownLatch closureLatch = new CountDownLatch(1);

  private Session session;

  private UUID sessionId;

  private DebugCommandFactory adapter;

  @OnOpen
  public void onWebSocketConnect(Session sess) {
    UUID sessionId = UUID.randomUUID();
    sessionRepository.put(sessionId, sess);

    this.session = sess;
    this.sessionId = sessionId;

    this.adapter = new DebugCommandFactory(new Gson());

    // send any initialization events to client if configuration is done
    if (configurationDone) {
      for (DebugCommand command : initializeCommandsQueue) {
        sendClient(session, gson.toJson(command));
      }
    }

    logger.log("Socket Connected: " + sess);
  }

  @OnMessage
  public void onWebSocketText(Session sess, String message) throws IOException {
    logger.log("Server Received TEXT message: " + message);

    DebugCommand command = adapter.fromJson(message);

    // reset pending configuration when initialize command is received
    if (command != null && Objects.equals(command.getCommand(), InitializeCommand.COMMAND)) {
      configurationDone = false;
      initializeCommandsQueue.clear();
    }

    // configuration complete
    if (command != null && Objects.equals(command.getCommand(), ConfigurationDoneCommand.COMMAND)) {
      configurationDone = true;

      // notify others of pending events; may be processed one or more times
      for (DebugCommand initializeCommand : initializeCommandsQueue) {
        replyAll(gson.toJson(initializeCommand));
      }
    }

    // stash breakpoints command; debugee should be initialized with them
    if (command != null && Objects.equals(command.getCommand(), SetBreakpointsCommand.COMMAND)) {
      if (!configurationDone) {
        initializeCommandsQueue.add(command);
      }
    }

    replyAll(message);

    // handle terminated command
    if (command != null && Objects.equals(command.getCommand(), DisconnectCommand.COMMAND)) {
      sess.close(
          new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Debug Session Terminated"));
    }
  }

  @OnClose
  public void onWebSocketClose(CloseReason reason) {
    sessionRepository.remove(sessionId);

    logger.log("Socket Closed: " + reason);

    // Notify other processes debug session has disconnected
    CallbackMessage callbackMessage = new CallbackMessage(CallbackMessage.STOPPED, "closed");
    Gson gson = new Gson();
    String message = gson.toJson(callbackMessage);

    Set<Map.Entry<UUID, Session>> sessions = sessionRepository.entrySet();
    for (Map.Entry<UUID, Session> entry : sessions) {
      if (!entry.getKey().equals(sessionId)) {
        sendClient(entry.getValue(), message);
      }
    }

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

  private void sendClient(Session session, String str) {
    try {
      session.getBasicRemote().sendText(str);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private void replyAll(String message) {
    Set<Map.Entry<UUID, Session>> sessions = sessionRepository.entrySet();
    for (Map.Entry<UUID, Session> entry : sessions) {
      if (!entry.getKey().equals(sessionId)) {
        sendClient(entry.getValue(), message);
      }
    }
  }
}
