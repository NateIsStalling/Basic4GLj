package com.basic4gl.runtime.core.opengl;

/**
 * Interface to the OpenGL window.
 */
public interface IB4GLOpenGLWindow {
    int getWidth();

    int getHeight();

    int getBPP(); // (Bits per pixel)

    boolean isFullscreen();

    void swapBuffers();

    String getTitle();
}
