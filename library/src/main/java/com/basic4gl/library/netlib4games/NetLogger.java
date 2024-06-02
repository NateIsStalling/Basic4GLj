package com.basic4gl.library.netlib4games;

import static com.basic4gl.library.netlib4games.NetLayer1.getTickCount;

public class NetLogger {
    public static interface NetLogCallback {
        public void NetLog(String text);
    }
    public static class DebugLogger implements NetLogCallback {

        @Override
        public void NetLog(String text) {
            System.out.println(("Net event (" + getTickCount() + "): " + text + "\r\n"));
        }
    }

    static NetLogCallback callback;

    private NetLogger() {
        DebugNetLogger();
    }
    public static void NetLog(String text) {
        callback.NetLog(text);
    }

   public static void SetNetLogger(NetLogCallback callback) {
        NetLogger.callback = callback;
   }

   public static void DebugNetLogger() {
       NetLogger.callback = new DebugLogger();
   }
}
