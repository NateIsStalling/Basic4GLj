package com.basic4gl.library.desktopgl;

/**
 * Interface for plugins
 */
class WindowAdapter implements IB4GLOpenGLWindow {
  public int getWidth() {
    return OpenGLBasicLib.appWindow.Width();
  }

  public int getHeight() {
    return OpenGLBasicLib.appWindow.Height();
  }

  public int getBPP() {
    return OpenGLBasicLib.appWindow.Bpp();
  }

  public boolean isFullscreen() {
    return OpenGLBasicLib.appWindow.FullScreen();
  }

  public void swapBuffers() {
    OpenGLBasicLib.appWindow.SwapBuffers();
  }

  public String getTitle() {
    return OpenGLBasicLib.appWindow.Title();
  }
}
