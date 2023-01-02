package com.basic4gl.debug.protocol.callbacks;

public class EvaluateWatchCallback extends Callback {
    public static final String COMMAND = "evaluate-watch";

    public EvaluateWatchCallback() {
        super(COMMAND);
    }

    protected String result;

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
