package com.basic4gl.debug.protocol.callbacks;

public class Callback {

    public static final String TYPE = "callback";

    protected String type = TYPE;

    protected String command;

    public Callback() {

    }

    public Callback(String command) {
        this.command = command;
    }
}
