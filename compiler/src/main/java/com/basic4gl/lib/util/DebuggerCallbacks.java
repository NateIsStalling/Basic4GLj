package com.basic4gl.lib.util;

import com.basic4gl.compiler.util.IVMDriver;

/**
 * Created by Nate on 11/23/2015.
 */
public abstract class DebuggerCallbacks {
    private final TaskCallback mCallback;
    private final CallbackMessage mMessage;
    private IVMDriver mDriver;

    protected DebuggerCallbacks(TaskCallback callback, CallbackMessage message){
        mCallback = callback;
        mMessage = message;
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
            }
        }
        } catch (InterruptedException e){//Do nothing
        }
    }

    public void resume(){
        synchronized (mMessage) {
            mMessage.status = CallbackMessage.WORKING;
            mMessage.notify();
        }
    }

    public void message(){
        mCallback.message(mMessage);
    }
    public void message(CallbackMessage message){
        mMessage.setMessage(message);
        mCallback.message(message);
    }

}
