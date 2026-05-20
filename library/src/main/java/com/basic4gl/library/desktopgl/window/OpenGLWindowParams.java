package com.basic4gl.library.desktopgl.window;

public class OpenGLWindowParams {
    public boolean isFullscreen;
    public int width;
    public int height;
    public int bpp;
    public boolean isBordered;
    public boolean isResizable;
    public boolean isStencilBufferRequired;
    public String title;

    public OpenGLWindowParams() {
        isFullscreen = false;
        width = 1024;
        height = 768;
        bpp = 0;
        isBordered = true;
        isResizable = false;
        isStencilBufferRequired = false;
        title = "OpenGL";
    }

    public OpenGLWindowParams(OpenGLWindowParams source) {
        isFullscreen = source.isFullscreen;
        width = source.width;
        height = source.height;
        bpp = source.bpp;
        isBordered = source.isBordered;
        isResizable = source.isResizable;
        isStencilBufferRequired = source.isStencilBufferRequired;
        title = source.title;
    }
}
