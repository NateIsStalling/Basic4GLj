package com.basic4gl.language.core.runtime;

/**
 * Created by Nate on 11/23/2015.
 */
public abstract class DebuggerCallbacks {
    private static final int MAX_STATUS_ERROR_CHARS = 16 * 1024;

    private final DebuggerTaskCallback taskCallback;
    private final com.basic4gl.language.core.runtime.DebuggerCallbackMessage callbackMessage;
    private final VM vm;
    private final IVMDriver vmDriver;

    protected DebuggerCallbacks(
            DebuggerTaskCallback callback,
            com.basic4gl.language.core.runtime.DebuggerCallbackMessage message,
            VM vm,
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

    public com.basic4gl.language.core.runtime.DebuggerCallbackMessage getMessage() {
        return callbackMessage;
    }

    public void setMessage(com.basic4gl.language.core.runtime.DebuggerCallbackMessage mMessage) {
        this.callbackMessage.setMessage(mMessage);
    }

    public void pause(String message) {
        InstructionPosition instructionPosition = null;
        if (vm.isIPValid()) {
            instructionPosition = vm.getIPInSourceCode();
        }
        com.basic4gl.language.core.runtime.VMStatus vmStatus = new com.basic4gl.language.core.runtime.VMStatus(
                vm.isDone(), vm.hasError(), abbreviate(vm.getError(), MAX_STATUS_ERROR_CHARS));
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

    public void message(com.basic4gl.language.core.runtime.DebuggerCallbackMessage message) {
        callbackMessage.setMessage(message);
        taskCallback.message(message);
    }

    public void message(CallbackMessage message) {
        com.basic4gl.language.core.runtime.DebuggerCallbackMessage debuggerCallbackMessage = null;

        if (message != null) {
            com.basic4gl.language.core.runtime.VMStatus vmStatus = new com.basic4gl.language.core.runtime.VMStatus(
                    vm.isDone(), vm.hasError(), abbreviate(vm.getError(), MAX_STATUS_ERROR_CHARS));
            debuggerCallbackMessage = new com.basic4gl.language.core.runtime.DebuggerCallbackMessage(
                    message.getStatus(), message.getText(), vmStatus);
        }
        callbackMessage.setMessage(debuggerCallbackMessage);
        taskCallback.message(debuggerCallbackMessage);
    }

    private static String abbreviate(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "... [truncated]";
    }
}
