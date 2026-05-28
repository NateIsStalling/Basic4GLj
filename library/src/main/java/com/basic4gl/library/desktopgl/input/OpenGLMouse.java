package com.basic4gl.library.desktopgl.input;

public interface OpenGLMouse {
    float getX();

    float getY();

    float getXD();

    float getYD();

    boolean getButton(int index);

    int getWheelDelta();
}
