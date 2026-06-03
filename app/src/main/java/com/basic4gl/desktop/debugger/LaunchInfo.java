package com.basic4gl.desktop.debugger;

public class LaunchInfo {
    private final Integer jvmDebugPort;
    private final boolean suspendedUntilDebuggerAttach;

    public LaunchInfo(Integer jvmDebugPort, boolean suspendedUntilDebuggerAttach) {
        this.jvmDebugPort = jvmDebugPort;
        this.suspendedUntilDebuggerAttach = suspendedUntilDebuggerAttach;
    }

    public Integer getJvmDebugPort() {
        return jvmDebugPort;
    }

    public boolean isSuspendedUntilDebuggerAttach() {
        return suspendedUntilDebuggerAttach;
    }
}
