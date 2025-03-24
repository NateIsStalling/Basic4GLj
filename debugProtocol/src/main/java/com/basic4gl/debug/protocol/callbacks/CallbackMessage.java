package com.basic4gl.debug.protocol.callbacks;

import com.basic4gl.debug.protocol.types.InstructionPosition;
import com.google.gson.Gson;

/**
 * Created by Nate on 2/19/2015.
 */
public class CallbackMessage {
    public static final int FAILED = -1;
    public static final int STOPPED = 0;
    public static final int WORKING = 1;
    public static final int SUCCESS = 2;
    public static final int PAUSED = 3;

    public int status;
    public String text;
    public InstructionPosition instructionPosition;

    public CallbackMessage() {
        this.status = STOPPED;
        this.text = "";
    }

    public CallbackMessage(int status, String message) {
        this.status = status;
        this.text = message;
    }

    public void setMessage(int status, String message) {
        this.status = status;
        this.text = message;
    }

    public void setMessage(CallbackMessage message) {
        this.status = message.status;
        this.text = message.text;
    }

    public void setSourcePosition(InstructionPosition instructionPosition) {
        this.instructionPosition = instructionPosition;
    }

    public void setSourcePosition(int row, int column) {
        this.instructionPosition = new InstructionPosition(row, column);
    }

    public InstructionPosition getSourcePosition() {
        return instructionPosition;
    }

    public static CallbackMessage fromJson(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, CallbackMessage.class);
        } catch (Exception e) {
            return null;
        }
    }
}
