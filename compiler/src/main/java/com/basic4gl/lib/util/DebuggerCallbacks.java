package com.basic4gl.lib.util;

import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.runtime.InstructionPosition;
import com.basic4gl.runtime.TomVM;

/**
 * Created by Nate on 11/23/2015.
 */
public abstract class DebuggerCallbacks {
    private final DebuggerTaskCallback taskCallback;
    private final DebuggerCallbackMessage callbackMessage;
    private final TomVM vm;
    private final IVMDriver vmDriver;

    protected DebuggerCallbacks(
            DebuggerTaskCallback callback,
            DebuggerCallbackMessage message,
            TomVM vm,
            // TODO circular dependency with start???
            IVMDriver driver) {

        taskCallback = callback;
        callbackMessage = message;
        this.vm = vm;
        vmDriver = driver;
    }

    /**
     * Occurs before IVMDriver onPreExecute
     */
    public abstract void onPreLoad();

    /**
     * Occurs after IVMDriver onPreExecute
     */
    public abstract void onPostLoad();

    public DebuggerCallbackMessage getMessage() {
        return callbackMessage;
    }

    public void setMessage(DebuggerCallbackMessage mMessage) {
        this.callbackMessage.setMessage(mMessage);
    }

    public void pause(String message) {
        InstructionPosition instructionPosition = null;
        if (vm.isIPValid()) {
            instructionPosition = vm.getIPInSourceCode();
        }
        VMStatus vmStatus = new VMStatus(vm.isDone(), vm.hasError(), vm.getError());
        callbackMessage.setMessage(CallbackMessage.PAUSED, message, vmStatus);
        callbackMessage.setInstructionPosition(instructionPosition);

        taskCallback.message(callbackMessage);
        try {
            // Wait for IDE to unpause the application
            while (callbackMessage.status == CallbackMessage.PAUSED) {
                // Go easy on the processor
                Thread.sleep(10);

                // Keep driver responsive while paused
                vmDriver.handleEvents();
                //                mMessage.wait(100);

                // Check if program was stopped while paused
                if (Thread.currentThread().isInterrupted() || vm.hasError() || vm.isDone() || vmDriver.isClosing()) {
                    break;
                }
            }
        } catch (InterruptedException e) { // Do nothing
        }
    }

    public void message() {
        taskCallback.message(callbackMessage);
    }

    public void message(DebuggerCallbackMessage message) {
        callbackMessage.setMessage(message);
        taskCallback.message(message);
    }

    public void message(CallbackMessage message) {
        DebuggerCallbackMessage debuggerCallbackMessage = null;

        if (message != null) {
            VMStatus vmStatus = new VMStatus(vm.isDone(), vm.hasError(), vm.getError());
            debuggerCallbackMessage = new DebuggerCallbackMessage(message.getStatus(), message.getText(), vmStatus);
        }
        callbackMessage.setMessage(debuggerCallbackMessage);
        taskCallback.message(debuggerCallbackMessage);
    }
}
