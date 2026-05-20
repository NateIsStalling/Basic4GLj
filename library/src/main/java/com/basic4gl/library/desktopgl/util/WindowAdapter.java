package com.basic4gl.library.desktopgl.util;

import com.basic4gl.library.desktopgl.window.OpenGLWindowManager;
import com.basic4gl.runtime.core.opengl.IB4GLOpenGLWindow;

/**
 * Window interface for plugins
 */
public class WindowAdapter implements IB4GLOpenGLWindow {
    private OpenGLWindowManager windowManager;
    public WindowAdapter(OpenGLWindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override
    public int getWidth() {
        return windowManager.getWindowWidth();
    }

    @Override
    public int getHeight() {
        return windowManager.getWindowHeight();
    }

    @Override
    public int getBPP() {
        return windowManager.getActiveParams().bpp;
    }

    @Override
    public boolean isFullscreen() {
        return windowManager.getActiveParams().isFullscreen;
    }

    @Override
    public void swapBuffers() {
        windowManager.swapBuffers();
    }

    @Override
    public String getTitle() {
        return windowManager.getActiveParams().title;
    }
}
