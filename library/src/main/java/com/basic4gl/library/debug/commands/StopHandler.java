package com.basic4gl.library.debug.commands;

import com.basic4gl.compiler.util.IVMDriver;

public class StopHandler {

    private final IVMDriver vmDriver;

    public StopHandler(IVMDriver vmDriver) {
        this.vmDriver = vmDriver;
    }

    public void stop() {
        vmDriver.stop();
    }
}
