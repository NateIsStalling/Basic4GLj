package com.basic4gl.lib.util;

/**
 * Created by Nate on 2/19/2015.
 */
public class CallbackMessage {
    public static final int FAILED = -1;
    public static final int STOPPED = 0;
    public static final int WORKING = 1;
    public static final int SUCCESS = 2;

    public int status;
    public String text;
    public CallbackMessage(int status, String message){
        this.status = status;
        this.text = message;
    }
}
