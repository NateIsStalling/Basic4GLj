package com.basic4gl.library.desktopgl.glfw;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.glfw.GLFW.*;

import com.basic4gl.library.desktopgl.window.OpenGLWindowManager;
import com.basic4gl.library.desktopgl.window.OpenGLWindowParams;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
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

        // Some params must specified as "hints"
        // Note: Must specify them even if set to defaults, because user can change them multiple
        // times within a single session.
        glfwWindowHint(GLFW_RESIZABLE, params.isResizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_DECORATED, params.isBordered ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_STENCIL_BITS, params.isStencilBufferRequired ? 8 : 0);

        if (params.isFullscreen && params.bpp == 16) {
            glfwWindowHint(GLFW_RED_BITS, 5);
            glfwWindowHint(GLFW_GREEN_BITS, 6);
            glfwWindowHint(GLFW_BLUE_BITS, 5);
        } else if (params.isFullscreen && params.bpp == 32) {
            glfwWindowHint(GLFW_RED_BITS, 8);
            glfwWindowHint(GLFW_GREEN_BITS, 8);
            glfwWindowHint(GLFW_BLUE_BITS, 8);
        } else {
            glfwWindowHint(GLFW_RED_BITS, GLFW_DONT_CARE);
            glfwWindowHint(GLFW_GREEN_BITS, GLFW_DONT_CARE);
            glfwWindowHint(GLFW_BLUE_BITS, GLFW_DONT_CARE);
        }

        // Dimensions are resolved by OpenGLWindowManager before creation.
        int width = params.width;
        int height = params.height;
        boolean usesDesktopResolution = pendingParams.width == 0 || pendingParams.height == 0;
        if (params.isFullscreen && usesDesktopResolution) {
            long monitor = glfwGetPrimaryMonitor();
            FloatBuffer scaleX = BufferUtils.createFloatBuffer(1);
            FloatBuffer scaleY = BufferUtils.createFloatBuffer(1);
            glfwGetMonitorContentScale(monitor, scaleX, scaleY);
            float sx = scaleX.get(0);
            float sy = scaleY.get(0);
            if (sx > 1.0f || sy > 1.0f) {
                width = Math.max(1, Math.round(width * sx));
                height = Math.max(1, Math.round(height * sy));
            }
        }
        if (width <= 0 || height <= 0) {
            GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (mode != null) {
                width = mode.width();
                height = mode.height();
            }
        }

        // Create the window
        window = glfwCreateWindow(width, height, params.title, params.isFullscreen ? glfwGetPrimaryMonitor() : 0, 0);

        if (window == 0) {
            System.out.println("Failed to create GLFW window");
            setError("Error creating window");
            return;
        }

        if (!params.isFullscreen) {
            centerWindowOnPrimaryMonitor(width, height);
        }

        // Make OpenGL context current
        glfwMakeContextCurrent(window);
        org.lwjgl.glfw.GLFW.glfwSwapInterval(0);
        // Initialize OpenGL bindings before any framebuffer-driven GL state updates.
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        refreshWindowSize();
        // Use drawable framebuffer size (not logical window size) for Retina/high-DPI rendering.
        refreshFramebufferSize();

        windowSizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                updateWindowSize(width, height);
            }
        };
        glfwSetWindowSizeCallback(window, windowSizeCallback);

        framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                updateFramebufferSize(width, height);
            }
        };
        glfwSetFramebufferSizeCallback(window, framebufferSizeCallback);

        // Fullscreen Retina can settle final drawable size one frame later.
        pendingSizeRefresh = true;

        // Load OpenGL extensions
        //        if (!gladLoadGLLoader(reinterpret_cast<GLADloadproc>(glfwGetProcAddress))) {
        //            SetError("gladLoadGLLoader() failed");
        //        }
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
