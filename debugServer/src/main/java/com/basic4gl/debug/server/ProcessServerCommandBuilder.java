package com.basic4gl.debug.server;

public class ProcessServerCommandBuilder {

    public static String connectProcess(String name) {
        return "connect-process " + name;
    }

    public static String eventBreakPointReached(int index) {
        return "breakpoint-reached " + index;
    }
}
