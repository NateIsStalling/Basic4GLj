package com.basic4gl.library.desktopgl.input;

public interface KeyPressedListener {
    /**
     * Parameter is whether key is scankey (true) or ASCII character (false)
     * @param isScanKey
     */
    void onKeyPressed(boolean isScanKey);
}
