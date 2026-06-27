package com.basic4gl.desktop.debugger;

import com.basic4gl.desktop.spi.DebugService;
import com.basic4gl.desktop.spi.IProcessExitListener;
import java.io.*;

public class RunHandler {

    private final IApplicationHost host;
    private final DebugService debugger;

    public RunHandler(IApplicationHost host, DebugService debugger) {
        this.host = host;
        this.debugger = debugger;
    }

    public com.basic4gl.desktop.spi.DebugLaunchInfo launchRemote() {

        // TODO 12/2020 replacing Continue();

        // Compile and run program from start
        if (!host.compile()) {
            return new com.basic4gl.desktop.spi.DebugLaunchInfo(null, false);
        }

        return debugger.start(this);
    }

    public void terminateLaunchedProcess() {
        debugger.terminateLaunchedProcess();
    }

    public void setProcessExitListener(IProcessExitListener listener) {
        debugger.setProcessExitListener(listener);
    }
}
