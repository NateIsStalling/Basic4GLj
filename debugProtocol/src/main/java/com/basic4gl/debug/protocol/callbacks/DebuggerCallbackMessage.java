package com.basic4gl.debug.protocol.callbacks;

import com.basic4gl.debug.protocol.types.InstructionPosition;
import com.basic4gl.debug.protocol.types.VMStatus;
import com.google.gson.Gson;

/**
 * Created by Nate on 2/19/2015.
 */
public class DebuggerCallbackMessage {
    public static final int FAILED = -1;
    public static final int STOPPED = 0;
    public static final int WORKING = 1;
    public static final int SUCCESS = 2;
    public static final int PAUSED = 3;


    public int status;
    public String text;

    public InstructionPosition instructionPosition;

    public VMStatus vmStatus;

    public DebuggerCallbackMessage(){
        this.status = STOPPED;
        this.text = "";
    }

    public DebuggerCallbackMessage(int status, String message, VMStatus vmStatus){
        this.status = status;
        this.text = message;
        this.vmStatus = VMStatus.copy(vmStatus);
    }

    public void setMessage(int status, String message, VMStatus vmStatus){
        this.status = status;
        this.text = message;
        this.vmStatus = VMStatus.copy(vmStatus);
    }

    public void setMessage(DebuggerCallbackMessage message){
        this.status = message.status;
        this.text = message.text;
        this.vmStatus = VMStatus.copy(message.getVMStatus());
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

    public VMStatus getVMStatus() {
        return vmStatus;
    }

    public static DebuggerCallbackMessage FromJson(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, DebuggerCallbackMessage.class);
        } catch (Exception e) {
            return null;
        }
    }
}

