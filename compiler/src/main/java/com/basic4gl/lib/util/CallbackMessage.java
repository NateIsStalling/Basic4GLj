package com.basic4gl.lib.util;

import java.util.Objects;

/**
 * Created by Nate on 2/19/2015.
 */
public class CallbackMessage {
    public static final int FAILED = -1;
    public static final int STOPPED = 0;
    public static final int WORKING = 1;
    public static final int SUCCESS = 2;
    public static final int PAUSED = 3;


    protected int status;
    protected String text;

    public CallbackMessage(){
        this.status = STOPPED;
        this.text = "";
    }
    public CallbackMessage(int status, String message){
        this.status = status;
        this.text = message;
    }
    public boolean setMessage(int status, String message){
        if (this.status == status && Objects.equals(this.text, message)) {
            // no change
            return false;
        }
        this.status = status;
        this.text = message;

        return true;
    }
    public boolean setMessage(CallbackMessage message){
        if (message == null) {
            // no change
            return false;
        }
        return setMessage(message.status, message.text);
    }

    public boolean setStatus(int status) {
        boolean didChange = this.status != status;
        this.status = status;
        return didChange;
    }

    public int getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }
}
