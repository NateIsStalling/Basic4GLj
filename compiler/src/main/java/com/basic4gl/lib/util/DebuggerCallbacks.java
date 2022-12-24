package com.basic4gl.lib.util;

import com.basic4gl.compiler.util.IVMDriver;

/**
 * Created by Nate on 11/23/2015.
 */
public abstract class DebuggerCallbacks {
    private final TaskCallback mCallback;
    private final CallbackMessage mMessage;
    private final IVMDriver mDriver;

    protected DebuggerCallbacks(
        TaskCallback callback,
        CallbackMessage message,
        // TODO circular dependency with start???
        IVMDriver driver) {

        mCallback = callback;
        mMessage = message;
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

    public CallbackMessage getMessage(){
        return mMessage;
    }
    public void setMessage(CallbackMessage mMessage) {
        this.mMessage.setMessage(mMessage);
    }

    public void pause(String message){
        mMessage.setMessage(CallbackMessage.PAUSED, message);
        mCallback.message(mMessage);
        try{
        //Wait for IDE to unpause the application
        synchronized (mMessage) {
            while (mMessage.status == CallbackMessage.PAUSED) {
                //Go easy on the processor
                Thread.sleep(10);

                // Keep driver responsive while paused
                mDriver.handleEvents();
                mMessage.wait(100);
//                System.out.println("paused");
            }
        }
        } catch (InterruptedException e){//Do nothing
        }
    }

    public void resume(){
        final CallbackMessage message = mMessage;
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
    public void message(CallbackMessage message){
        mMessage.setMessage(message);
        mCallback.message(message);
    }

}
