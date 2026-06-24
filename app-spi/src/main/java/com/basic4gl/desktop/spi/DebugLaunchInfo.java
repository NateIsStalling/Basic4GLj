package com.basic4gl.desktop.spi;

public class DebugLaunchInfo {
    private final Integer jvmDebugPort;
    private final boolean suspendedUntilDebuggerAttach;

    public DebugLaunchInfo(Integer jvmDebugPort, boolean suspendedUntilDebuggerAttach) {
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
