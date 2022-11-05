package com.basic4gl.debug.server;

public class DebuggerServerCommandBuilder {

    public static String connectDebugger(String name) {
        return "connect-debugger " + name;
    }

    public static String attachProcess(String name) {
        return "attach-process " + name;
    }

    public static String detachProcess(String name) {
        return "detach-process " + name;
    }

    public static String killProcess() {
        return "kill-process";
    }

    public static String addBreakpoint(int index) {
        return "add-breakpoint " + index;
    }

    public static String removeBreakpoint(int index) {
        return "remove-breakpoint " + index;
    }

    public static String getBreakpointCount() {
        return "breakpoint-count";
    }

    public static String getBreakpointLine(int index) {
        return "breakpoint-line " + index;
    }

    public static String getProcessStatus() {
        return "process-status";
    }

    public static String debugPause() {
        return "debug-pause";
    }

    public static String debugResume() {
        return "debug-resume";
    }

    public static String debugStepOver() {
        return "debug-step-over";
    }

    public static String debugStepInto() {
        return "debug-step-into";
    }

    public static String debugStepOut() {
        return "debug-step-out";
    }
}

