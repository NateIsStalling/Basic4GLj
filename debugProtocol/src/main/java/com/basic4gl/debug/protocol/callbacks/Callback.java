package com.basic4gl.debug.protocol.callbacks;

public class Callback extends ProtocolMessage {

    public static final String TYPE = "callback";

    protected String command;

    protected int requestId;

    /**
     * Outcome of the command.
     * If true, the command was successful and the callback contains the expected data.
     * If false, the command failed and the callback may contain an ErrorCallback
     */
    protected boolean success = true;

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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCommand() {
        return command;
    }
}
