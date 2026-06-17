package com.basic4gl.library.desktopgl.glfw;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glGetString;

import com.basic4gl.library.desktopgl.window.OpenGLWindowManager;
import com.basic4gl.library.desktopgl.window.OpenGLWindowParams;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;

public class GLFWWindowManager extends OpenGLWindowManager {
    private long window;
    private GLFWFramebufferSizeCallback framebufferSizeCallback;
    private GLFWWindowSizeCallback windowSizeCallback;
    private boolean pendingSizeRefresh;

    private void refreshFramebufferSize() {
        if (window == 0) {
            return;
        }
        IntBuffer fbWidth = BufferUtils.createIntBuffer(1);
        IntBuffer fbHeight = BufferUtils.createIntBuffer(1);
        glfwGetFramebufferSize(window, fbWidth, fbHeight);
        updateFramebufferSize(fbWidth.get(0), fbHeight.get(0));
    }

    private void refreshWindowSize() {
        if (window == 0) {
            return;
        }
        IntBuffer winWidth = BufferUtils.createIntBuffer(1);
        IntBuffer winHeight = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window, winWidth, winHeight);
        updateWindowSize(winWidth.get(0), winHeight.get(0));
    }

    private void centerWindowOnPrimaryMonitor(int targetWidth, int targetHeight) {
        if (window == 0) {
            return;
        }

        long monitor = glfwGetPrimaryMonitor();
        if (monitor == 0) {
            return;
        }

        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if (mode == null) {
            return;
        }

        IntBuffer monitorX = BufferUtils.createIntBuffer(1);
        IntBuffer monitorY = BufferUtils.createIntBuffer(1);
        glfwGetMonitorPos(monitor, monitorX, monitorY);

        int width = Math.max(targetWidth, 1);
        int height = Math.max(targetHeight, 1);
        int x = monitorX.get(0) + Math.max((mode.width() - width) / 2, 0);
        int y = monitorY.get(0) + Math.max((mode.height() - height) / 2, 0);
        glfwSetWindowPos(window, x, y);
    }

    public long getGLFWWindow() {
        return window;
    }

    @Override
    protected void internalDestroyWindow() {
        assertTrue(window != 0);
        glfwSetFramebufferSizeCallback(window, null);
        glfwSetWindowSizeCallback(window, null);
        if (framebufferSizeCallback != null) {
            framebufferSizeCallback.free();
            framebufferSizeCallback = null;
        }
        if (windowSizeCallback != null) {
            windowSizeCallback.free();
            windowSizeCallback = null;
        }
        glfwDestroyWindow(window);
        window = 0;
    }
    @Override
    protected void internalCreateWindow(OpenGLWindowParams params) {
        assertTrue(window == 0);

        // --- ALL hints must come BEFORE glfwCreateWindow; they only affect the next window. ---
        glfwDefaultWindowHints(); // reset, since the user can recreate within a session
        glfwWindowHint(GLFW_RESIZABLE, params.isResizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_DECORATED, params.isBordered ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_STENCIL_BITS, params.isStencilBufferRequired ? 8 : 0);
        glfwWindowHint(GLFW_DEPTH_BITS, 16);     // base class enables GL_DEPTH_TEST — keep a depth buffer
        glfwWindowHint(GLFW_SAMPLES, 0);         // no MSAA, matches the WGL pfd
        glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);

        // Don't let GLFW upscale the framebuffer on a scaled display. Render at logical size,
        // the way the DPI-unaware WGL build did. SCALE_TO_MONITOR defaults false; set it explicitly.
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_FALSE);
        // GLFW 3.4+ only — pins framebuffer to window size on high-DPI. Delete if the constant
        // doesn't exist in your LWJGL/GLFW version:
        // glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_FALSE);

        // Legacy compatibility context — matches the old plain wglCreateContext path.
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE);

        if (params.isFullscreen && params.bpp == 16) {
            glfwWindowHint(GLFW_RED_BITS, 5);  glfwWindowHint(GLFW_GREEN_BITS, 6);  glfwWindowHint(GLFW_BLUE_BITS, 5);
        } else if (params.isFullscreen && params.bpp == 32) {
            glfwWindowHint(GLFW_RED_BITS, 8);  glfwWindowHint(GLFW_GREEN_BITS, 8);  glfwWindowHint(GLFW_BLUE_BITS, 8);
        } else {
            glfwWindowHint(GLFW_RED_BITS, GLFW_DONT_CARE);
            glfwWindowHint(GLFW_GREEN_BITS, GLFW_DONT_CARE);
            glfwWindowHint(GLFW_BLUE_BITS, GLFW_DONT_CARE);
        }

        int width = params.width;
        int height = params.height;

        // Fullscreen at desktop resolution => use the monitor's NATIVE video mode.
        // NOTE: the old code multiplied these by glfwGetMonitorContentScale(), which inflated the
        // render target above native (≈2.25x at 150% scaling). That multiply is the regression vs the
        // WGL ChangeDisplaySettings path — it's gone.
        boolean usesDesktopResolution = pendingParams.width == 0 || pendingParams.height == 0;
        if (width <= 0 || height <= 0 || (params.isFullscreen && usesDesktopResolution)) {
            GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (mode != null) { width = mode.width(); height = mode.height(); }
        }

        window = glfwCreateWindow(width, height, params.title,
                params.isFullscreen ? glfwGetPrimaryMonitor() : 0, 0);
        if (window == 0) {
            System.out.println("Failed to create GLFW window");
            setError("Error creating window");
            return;
        }
        if (!params.isFullscreen) {
            centerWindowOnPrimaryMonitor(width, height);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // orthogonal to the perf fix; just restores the old driver-default pacing
        GL.createCapabilities();

        // --- One-shot diagnostic. Pull these once, then delete. ---
        System.out.println("GL_VERSION=" +glGetString(GL_VERSION));
        System.out.println("GL_RENDERER=" + glGetString(org.lwjgl.opengl.GL11.GL_RENDERER));
        int[] fbw = new int[1], fbh = new int[1];
        glfwGetFramebufferSize(window, fbw, fbh);
        System.out.println("FB=" + fbw[0] + "x" + fbh[0] + " requested=" + width + "x" + height);

        refreshWindowSize();
        refreshFramebufferSize();

        windowSizeCallback = new GLFWWindowSizeCallback() {
            @Override public void invoke(long w, int cw, int ch) { updateWindowSize(cw, ch); }
        };
        glfwSetWindowSizeCallback(window, windowSizeCallback);
        framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override public void invoke(long w, int fw, int fh) { updateFramebufferSize(fw, fh); }
        };
        glfwSetFramebufferSizeCallback(window, framebufferSizeCallback);

        pendingSizeRefresh = true;
    }

    @Override
    protected void internalActivateWindow() {
        assertTrue(window != 0);

        // Give window focus.

        // Cross-platform GLFW focus/raise
        glfwShowWindow(window);
        glfwFocusWindow(window);

        // Fullscreen Retina transitions can finalize drawable size when shown/activated.
        refreshWindowSize();
        refreshFramebufferSize();
        if (activeParams.isFullscreen) {
            // Give GLFW one event refresh to apply late fullscreen drawable-size updates.
            glfwPollEvents();
            refreshWindowSize();
            refreshFramebufferSize();
        }
        pendingSizeRefresh = true;
    }

    @Override
    protected void internalDeactivateWindow() {
        assertTrue(window != 0);

        if (activeParams.isFullscreen) {
            // Switch away from fullscreen window by minimising it
            glfwIconifyWindow(window);
            // HWND handle = glfwGetWin32Window(window);
            // ShowWindow(handle, SW_HIDE); 		    			// Hide the window
            // ChangeDisplaySettings(nullptr, 0);
        }
    }

    @Override
    protected boolean internalIsCloseRequested() {
        assertTrue(window != 0);
        return glfwWindowShouldClose(window);
    }

    @Override
    protected void internalSwapBuffers() {
        assertTrue(window != 0);
        if (pendingSizeRefresh) {
            refreshWindowSize();
            refreshFramebufferSize();
            pendingSizeRefresh = false;
        }
        glfwSwapBuffers(window);
    }

    @Override
    public int getScreenWidth() {
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return mode.width();
    }

    @Override
    public int getScreenHeight() {
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return mode.height();
    }

    @Override
    public void beep() {
        // TODO without AWT!
        // java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public GLFWWindowManager() {
        super();
        window = 0;
        framebufferSizeCallback = null;
        windowSizeCallback = null;
        pendingSizeRefresh = false;
        if (!glfwInit()) {
            setError("glfwInit() failed");
            return;
        }

        //        atexit(glfwTerminate);
    }

    public void dispose() {
        if (window != 0) {
            internalDestroyWindow();
        }
    }
}
