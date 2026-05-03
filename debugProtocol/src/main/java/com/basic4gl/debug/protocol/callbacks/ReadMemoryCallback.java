package com.basic4gl.debug.protocol.callbacks;

public class ReadMemoryCallback extends Callback {
    public static final String COMMAND = "readMemory";

    private String address;
    private String data;
    private Integer unreadableBytes;

    public ReadMemoryCallback() {
        super(COMMAND);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getUnreadableBytes() {
        return unreadableBytes;
    }

    public void setUnreadableBytes(Integer unreadableBytes) {
        this.unreadableBytes = unreadableBytes;
    }
}

