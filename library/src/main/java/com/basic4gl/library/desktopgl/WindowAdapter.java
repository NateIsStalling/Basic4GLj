package com.basic4gl.library.desktopgl;

/**
 * Interface for plugins
 */
class WindowAdapter implements IB4GLOpenGLWindow {
	public int getWidth() {
		return OpenGLBasicLib.getAppWindow().getWidth();
	}

	public int getHeight() {
		return OpenGLBasicLib.getAppWindow().getHeight();
	}

	public int getBPP() {
		return OpenGLBasicLib.getAppWindow().getBpp();
	}

	public boolean isFullscreen() {
		return OpenGLBasicLib.getAppWindow().isFullScreen();
	}

	public void swapBuffers() {
		OpenGLBasicLib.getAppWindow().swapBuffers();
	}

	public String getTitle() {
		return OpenGLBasicLib.getAppWindow().getTitle();
	}
}
