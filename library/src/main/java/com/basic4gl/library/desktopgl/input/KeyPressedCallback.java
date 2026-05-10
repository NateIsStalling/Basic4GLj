package com.basic4gl.library.desktopgl.input;

public class KeyPressedCallback {
    int unsubscribeHandle;
    KeyPressedListener callback;
    KeyPressedCallback(int unsubscribeHandle, KeyPressedListener callback) {
        this.unsubscribeHandle = unsubscribeHandle;
        this.callback = callback;
    }
}
