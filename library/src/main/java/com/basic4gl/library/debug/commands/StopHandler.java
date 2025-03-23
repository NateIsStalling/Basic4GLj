package com.basic4gl.library.debug.commands;

import com.basic4gl.compiler.util.IVMDriver;

public class StopHandler {

    private final IVMDriver mVMDriver;

    public StopHandler(IVMDriver vmDriver) {
        mVMDriver = vmDriver;
    }

    public void stop() {
        mVMDriver.stop();
    }
}
