package com.basic4gl.desktop.debugger;

import com.basic4gl.desktop.spi.DebugService;
import com.basic4gl.desktop.spi.IProcessExitListener;
import com.basic4gl.language.core.extensions.IAppSettings;
import com.basic4gl.library.desktopgl.util.ITargetCommandLineOptions;
import com.basic4gl.language.core.extensions.Library;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.SystemUtils;

public class RunHandler {


    private final IApplicationHost host;
    private final DebugService debugger;

    public RunHandler(
            IApplicationHost host, DebugService debugger) {
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
