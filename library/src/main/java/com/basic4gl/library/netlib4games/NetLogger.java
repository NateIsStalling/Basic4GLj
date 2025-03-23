package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.NetLayer1.getTickCount;

public class NetLogger {
  public interface NetLogHandler {
    void netLog(String text);
  }

  public static class DebugLogger implements NetLogHandler {
    @Override
    public void netLog(String text) {
      System.out.println(("Net event (" + getTickCount() + "): " + text + "\r\n"));
    }
  }

  private static NetLogHandler handler;

  private NetLogger() {
    initDebugNetLogger();
  }

  public static void netLog(String text) {
    handler.netLog(text);
  }

  public static void setNetLogger(NetLogHandler handler) {
    NetLogger.handler = handler;
  }

  public static void initDebugNetLogger() {
    NetLogger.handler = new DebugLogger();
  }
}
