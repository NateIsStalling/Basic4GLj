package com.basic4gl.lib.util;

import com.basic4gl.compiler.util.IVMDriver;
import com.basic4gl.runtime.InstructionPos;
import com.basic4gl.runtime.TomVM;

/**
 * Created by Nate on 11/23/2015.
 */
public abstract class DebuggerCallbacks {
    private final DebuggerTaskCallback mCallback;
    private final DebuggerCallbackMessage mMessage;
    private final TomVM mVM;
    private final IVMDriver mDriver;

    protected DebuggerCallbacks(
        DebuggerTaskCallback callback,
        DebuggerCallbackMessage message,
        TomVM vm,
        // TODO circular dependency with start???
        IVMDriver driver) {

        mCallback = callback;
        mMessage = message;
        mVM = vm;
        mDriver = driver;
    }

    /**
     * Occurs before IVMDriver onPreExecute
     */
    public abstract void onPreLoad();
    /**
     * Occurs after IVMDriver onPreExecute
     */
    public abstract void onPostLoad();

    public DebuggerCallbackMessage getMessage(){
        return mMessage;
    }
    public void setMessage(DebuggerCallbackMessage mMessage) {
        this.mMessage.setMessage(mMessage);
    }

    public void pause(String message){
        InstructionPos instructionPos = null;
        if (mVM.IPValid()) {
            instructionPos = mVM.GetIPInSourceCode();
        }
        VMStatus vmStatus = new VMStatus(mVM.Done(), mVM.hasError(), mVM.getError());
        mMessage.setMessage(CallbackMessage.PAUSED, message, vmStatus);
        mMessage.setInstructionPosition(instructionPos);

        mCallback.message(mMessage);
        try{
        //Wait for IDE to unpause the application
//        synchronized (mMessage) {
            while (mMessage.status == CallbackMessage.PAUSED) {
                //Go easy on the processor
                Thread.sleep(10);

                // Keep driver responsive while paused
                mDriver.handleEvents();
//                mMessage.wait(100);
//                System.out.println("paused");

                //Check if program was stopped while paused
                if (Thread.currentThread().isInterrupted() || mVM.hasError() || mVM.Done() || mDriver.isClosing())
                    break;
            }
//        }
        } catch (InterruptedException e){//Do nothing
        }
    }

    public void resume(){
        final DebuggerCallbackMessage message = mMessage;
        //TODO this is specifically gross;
        // the pause/resume was previously managed from separate threads
        // when the GLWindow was launched in a new thread instead of separate process
        // the MainEditor class would have it's own reference to mMessage to notify
        // allowing synchronized blocks for pause/resume
        //  with notify when the status changes in the other thread
        // BUT now the MainEditor and GLWindow are now separate processes
        // so mMessage is only managed by one thread
        // and the syncronized block does not work as expected
        // so start a new thread to notify message here as a workaround
        Thread handler = new Thread() {
            @Override
            public void run() {
                synchronized (message) {
                    message.status = CallbackMessage.WORKING;
                    message.notify();
                }
            }
        };
        handler.start();
    }

    public void message(){
        mCallback.message(mMessage);
    }

    public void message(DebuggerCallbackMessage message){
        mMessage.setMessage(message);
        mCallback.message(message);
    }

    public void message(CallbackMessage message){
        DebuggerCallbackMessage debuggerCallbackMessage = null;

        if (message != null) {
            VMStatus vmStatus = new VMStatus(mVM.Done(), mVM.hasError(), mVM.getError());
            debuggerCallbackMessage = new DebuggerCallbackMessage(message.getStatus(), message.getText(), vmStatus);
        }
        mMessage.setMessage(debuggerCallbackMessage);
        mCallback.message(debuggerCallbackMessage);
    }
}
