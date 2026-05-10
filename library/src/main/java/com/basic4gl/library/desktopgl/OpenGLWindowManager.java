package com.basic4gl.library.desktopgl;

import com.basic4gl.library.desktopgl.util.GLUtil;
import com.basic4gl.runtime.HasErrorState;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

import static com.basic4gl.library.desktopgl.util.GLUtil.gluPerspective;
import static org.lwjgl.opengl.GL11.*;

/**
 * Manages creating and recreating the OpenGL window.
 * Also manages swapping buffers and orchestrating 2D content like text and sprites.
 * Abstract base class. Must be implemented for specific OpenGL window library (e.g. GLFW)
 */
public abstract class OpenGLWindowManager extends HasErrorState {
    private int framebufferWidth;
    private int framebufferHeight;
    private int windowWidth;
    private int windowHeight;

    private void applyViewportAndProjection() {
        int viewportWidth = getFramebufferWidth();
        int viewportHeight = getFramebufferHeight();

        GL11.glViewport(0, 0, viewportWidth, viewportHeight); // Reset The Current Viewport
        glMatrixMode(GL_PROJECTION); // Select The Projection Matrix
        glLoadIdentity(); // Reset The Projection Matrix
        gluPerspective(
                60.0f,
                (float) viewportWidth / (float) viewportHeight,
                1.0f,
                1000.0f);

        glMatrixMode(GL_MODELVIEW); // Select The Modelview Matrix
        glLoadIdentity(); // Reset The Modelview Matrix
    }

    private void setOpenGLDefaults() {
        applyViewportAndProjection();
        try { glShadeModel(GL_SMOOTH); }
        catch (Exception e) { ; }
        try { glClearColor(0.0f, 0.0f, 0.0f, 0.5f); }
        catch (Exception e) { ; }
        try { glClearDepth(1.0f); }
        catch (Exception e) { ; }
        try { glEnable(GL_DEPTH_TEST); }
        catch (Exception e) { ; }
        try { glDepthFunc(GL_LEQUAL); }
        catch (Exception e) { ; }
        try { glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); }
        catch (Exception e) { ; }
        try { glDisable(GL_TEXTURE_2D); }
        catch (Exception e) { ; }

        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        swapBuffers();
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    }

    private final ArrayList<OpenGLWindowCreatedListener> windowCreatedListeners = new ArrayList<>();
    private final ArrayList<OpenGLBeforeDestroyWindowListener> beforeDestroyWindowListeners = new ArrayList();

    protected boolean isWindowCreated;
    protected OpenGLWindowParams activeParams = new OpenGLWindowParams();
    protected boolean isWindowShowing;

    // Must be implemented in base class

    /// Destroy the window (if exists)
    protected abstract void internalDestroyWindow();

    /// Create a new window based on the given parameters, and make active
    protected abstract void internalCreateWindow(OpenGLWindowParams params);

    /// Make window active
    protected abstract void internalActivateWindow();

    protected abstract void internalDeactivateWindow();

    protected abstract boolean internalIsCloseRequested();

    protected abstract void internalSwapBuffers();

    public abstract int getScreenWidth();

    public abstract int getScreenHeight();

    public abstract void beep();

    public OpenGLWindowParams pendingParams = new OpenGLWindowParams();

    public OpenGLWindowManager() {
        isWindowCreated = false;
        isWindowShowing = false;
        framebufferWidth = 0;
        framebufferHeight = 0;
        windowWidth = 0;
        windowHeight = 0;
    }

    public void dispose() {

    }

    public OpenGLWindowParams getActiveParams() { return activeParams; }

    private OpenGLWindowParams buildResolvedActiveParams() {
        OpenGLWindowParams resolved = new OpenGLWindowParams(pendingParams);
        resolved.isFullscreen = pendingParams.isFullscreen;
        resolved.width = pendingParams.width == 0 ? getScreenWidth() : pendingParams.width;
        resolved.height = pendingParams.height == 0 ? getScreenHeight() : pendingParams.height;
        return resolved;
    }

    public void recreateWindow() {
        OpenGLWindowParams previousActiveParams = new OpenGLWindowParams(activeParams);

        // Destroy any existing window first
        destroyWindow();

        // Create new window
        clearError();
        activeParams = buildResolvedActiveParams();
        isWindowCreated = true;
        isWindowShowing = false;
        internalCreateWindow(activeParams);

        if (hasError()) {
            isWindowCreated = false;
            isWindowShowing = false;
            activeParams = previousActiveParams;
            return;
        }

        internalActivateWindow();
        isWindowShowing = true;
        setOpenGLDefaults();

        // Call window created callbacks
        for (OpenGLWindowCreatedListener listener : windowCreatedListeners)
        {
            listener.onOpenGLWindowCreated();
        }
    }
    public void activateWindow() {
        if (isWindowCreated && !isWindowShowing)
        {
            internalActivateWindow();
            isWindowShowing = true;
        }
    }
    public void deactivateWindow() {
        if (isWindowCreated && isWindowShowing)
        {
            internalDeactivateWindow();
            isWindowShowing = false;
        }
    }
    public void destroyWindow() {
        if (isWindowCreated)
        {
            // Call before window destroyed callbacks
            for (OpenGLBeforeDestroyWindowListener listener : beforeDestroyWindowListeners)
            {
                listener.onBeforeDestroyOpenGLWindow();
            }

            internalDestroyWindow();
            isWindowCreated = false;
            framebufferWidth = 0;
            framebufferHeight = 0;
            windowWidth = 0;
            windowHeight = 0;
        }
    }
    public boolean isCloseRequested() {
        return isWindowCreated && internalIsCloseRequested();
    }
    public void swapBuffers() {
        if (isWindowCreated)
        {
            internalSwapBuffers();
        }
    }

    public boolean isWindowCreated() { return isWindowCreated; }
    public void subscribeWindowCreated(OpenGLWindowCreatedListener listener)	{ windowCreatedListeners.add(listener); }

    public void subscribeBeforeDestroyWindow(OpenGLBeforeDestroyWindowListener listener) { beforeDestroyWindowListeners.add(listener); }
    public int getWindowWidth() {
        int width = windowWidth;
        if (width <= 0) {
            width = activeParams.width;
            if (width == 0) {
                width = getScreenWidth();
            }
        }
        return width;
    }
    public int getWindowHeight() {
        int height = windowHeight;
        if (height <= 0) {
            height = activeParams.height;
            if (height == 0) {
                height = getScreenHeight();
            }
        }
        return height;
    }
    public int getPendingWindowWidth(){
        int width = pendingParams.width;
        if (width == 0) {
            width = getScreenWidth();
        }
        return width;
    }

    public int getPendingWindowHeight() {
        int height = pendingParams.height;
        if (height == 0) {
            height = getScreenHeight();
        }
        return height;
    }

    protected void updateFramebufferSize(int width, int height) {
        int newWidth = Math.max(width, 1);
        int newHeight = Math.max(height, 1);
        boolean changed = newWidth != framebufferWidth || newHeight != framebufferHeight;

        framebufferWidth = newWidth;
        framebufferHeight = newHeight;

        // Fullscreen Retina transitions can report final drawable size after activation.
        if (isWindowCreated && changed) {
            try {
                applyViewportAndProjection();
            } catch (Exception ignored) {
            }
        }
    }

    protected void updateWindowSize(int width, int height) {
        windowWidth = Math.max(width, 1);
        windowHeight = Math.max(height, 1);
    }

    public int getFramebufferWidth() {
        return framebufferWidth > 0 ? framebufferWidth : getWindowWidth();
    }

    public int getFramebufferHeight() {
        return framebufferHeight > 0 ? framebufferHeight : getWindowHeight();
    }

    public boolean isExtensionSupported(String extension) {
        return GLUtil.isExtensionSupported(extension);
    }
}
