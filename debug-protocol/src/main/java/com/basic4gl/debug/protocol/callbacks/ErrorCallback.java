package com.basic4gl.debug.protocol.callbacks;

import com.basic4gl.debug.protocol.types.Message;

public class ErrorCallback extends Callback {
    public static final String COMMAND = "error";

    public ErrorCallback() {
        super(COMMAND);
    }

    public Message error;
}
