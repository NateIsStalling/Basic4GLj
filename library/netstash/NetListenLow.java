package com.basic4gl.library.net;

import com.basic4gl.runtime.HasErrorState;

public class NetListenLow extends HasErrorState {
    public boolean ConnectionPending() {
        return false;
    }

    public void RejectConnection() {
    }
}
