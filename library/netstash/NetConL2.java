package com.basic4gl.library.net;

import com.basic4gl.runtime.HasErrorState;

public class NetConL2 extends HasErrorState {
    public boolean isConnected() {
        return false;
    }

    public boolean HandShaking() {
        return false;
    }

    public boolean DataPending() {
        return false;
    }

    public int PendingChannel() {
        return 1;
    }

    public void receive(byte[] buffer, int size) {
    }

    public void Connect(String addressString) {
    }

    public void dispose() {
    }
}
