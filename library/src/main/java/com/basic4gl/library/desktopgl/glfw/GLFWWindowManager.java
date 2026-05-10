package com.basic4gl.library.desktopgl.glfw;

import com.basic4gl.library.desktopgl.OpenGLWindowManager;
import com.basic4gl.library.desktopgl.OpenGLWindowParams;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.opengl.GL11.GL_TRUE;

public class GLFWWindowManager extends OpenGLWindowManager {
    private long window;


    public long getGLFWWindow() { return window; }

    @Override
    protected void internalDestroyWindow()
    {
        assertTrue(window != 0);
        glfwDestroyWindow(window);
        window = 0;
    }

    @Override
    protected void internalCreateWindow(OpenGLWindowParams params)
    {
        assertTrue(window == 0);

        // Some params must specified as "hints"
        // Note: Must specify them even if set to defaults, because user can change them multiple
        // times within a single session.
        glfwWindowHint(GLFW_RESIZABLE, params.isResizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_DECORATED, params.isBordered ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_STENCIL_BITS, params.isStencilBufferRequired ? 8 : 0);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GL_TRUE); // handle monitor resolution scaling
        if (params.isFullscreen && params.bpp == 16)
        {
            glfwWindowHint(GLFW_RED_BITS, 5);
            glfwWindowHint(GLFW_GREEN_BITS, 6);
            glfwWindowHint(GLFW_BLUE_BITS, 5);
        }
        else if (params.isFullscreen && params.bpp == 32)
        {
            glfwWindowHint(GLFW_RED_BITS, 8);
            glfwWindowHint(GLFW_GREEN_BITS, 8);
            glfwWindowHint(GLFW_BLUE_BITS, 8);
        }
        else
        {
            glfwWindowHint(GLFW_RED_BITS, GLFW_DONT_CARE);
            glfwWindowHint(GLFW_GREEN_BITS, GLFW_DONT_CARE);
            glfwWindowHint(GLFW_BLUE_BITS, GLFW_DONT_CARE);
        }

        // Calculate actual dimensions
        int width = params.width;
        int height = params.height;
        if (width == 0 || height == 0)
        {
            // Use desktop resolution
            GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            width = mode.width();
            height = mode.height();
        }

        // Create the window
        window = glfwCreateWindow(
                width,
                height,
                params.title,
                params.isFullscreen ? glfwGetPrimaryMonitor() : 0,
                0);

        if (window == 0) {
            System.out.println("Failed to create GLFW window");
            setError("Error creating window");
        }

        // Make OpenGL context current
        glfwMakeContextCurrent(window);


        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Load OpenGL extensions
//        if (!gladLoadGLLoader(reinterpret_cast<GLADloadproc>(glfwGetProcAddress))) {
//            SetError("gladLoadGLLoader() failed");
//        }
    }

    @Override
    protected void internalActivateWindow() {
        assertTrue(window != 0);

        if (activeParams.isFullscreen)
        {
            glfwRestoreWindow(window);
        }

        // Give window focus.

        // Cross-platform GLFW focus/raise
        glfwShowWindow(window);
        glfwFocusWindow(window);
    }

    @Override
    protected void internalDeactivateWindow() {
        assertTrue(window != 0);

        if (activeParams.isFullscreen)
        {
            // Switch away from fullscreen window by minimising it
            glfwIconifyWindow(window);
            //HWND handle = glfwGetWin32Window(window);
            //ShowWindow(handle, SW_HIDE); 		    			// Hide the window
            //ChangeDisplaySettings(nullptr, 0);
        }
    }

    @Override
    protected boolean internalIsCloseRequested()
    {
        assertTrue(window != 0);
        return glfwWindowShouldClose(window);
    }

    @Override
    protected void internalSwapBuffers()
    {
        assertTrue(window != 0);
        glfwSwapBuffers(window);
    }

    @Override
    public int getScreenWidth()
    {
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return mode.width();
    }

    @Override
    public int getScreenHeight()
    {
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        return mode.height();
    }

    @Override
    public void beep() {
        // TODO without AWT!
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public GLFWWindowManager()
    {
        super();
        window = 0;
        if (!glfwInit())
        {
            setError("glfwInit() failed");
            return;
        }

//        atexit(glfwTerminate);
    }

    public void dispose()
    {
        if (window != 0) {
            internalDestroyWindow();
        }
    }
}
