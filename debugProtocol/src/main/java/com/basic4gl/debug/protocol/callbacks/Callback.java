package com.basic4gl.debug.protocol.callbacks;

public class Callback extends ProtocolMessage {

    public static final String TYPE = "callback";

    protected String command;

    protected int requestId;

    public Callback() {
        super(TYPE);
    }

    public Callback(String command) {
        super(TYPE);
        this.command = command;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
}
