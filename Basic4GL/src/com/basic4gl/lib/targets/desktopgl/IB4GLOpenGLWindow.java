package com.basic4gl.lib.targets.desktopgl;

////////////////////////////////////////////////////////////////////////////////
//  IB4GLOpenGLWindow
//
/// Interface to the OpenGL window.

public interface IB4GLOpenGLWindow {
    int  Width();
    int  Height();
    int  BPP();          // (Bits per pixel)
    boolean  Fullscreen();
    void  SwapBuffers();
    String Title();
}