package com.basic4gl.desktop.vmview;

import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.callbacks.VariablesCallback;

public interface IVirtualMachineView {
    void updateCallStack(StackTraceCallback callback);

    void updateDisassembly(DisassembleCallback callback);

    void updateVariables(VariablesCallback callback);

    void setVmRunning(boolean running);
}

