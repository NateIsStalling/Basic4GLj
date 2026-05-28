package com.basic4gl.library.desktopgl.glfw;

import static org.lwjgl.glfw.GLFW.*;

import com.basic4gl.library.desktopgl.input.OpenGLMouse;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

public class GLFWMouse implements OpenGLMouse {

    private long window;
    private int cursorInputMode;

    private double x, y;
    private double prevX, prevY;
    private double prevScrollY;
    private int wheelDelta;
    private boolean isCursorVisible;

    // Functions

    class MouseCursorPosCallback implements GLFWCursorPosCallbackI {
        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            cursorPosCallback(xoffset, yoffset);
        }
    }

    class MouseScrollCallback implements GLFWScrollCallbackI {
        @Override
        public void invoke(long window, double xoffset, double yoffset) {
            scrollCallback(xoffset, yoffset);
        }
    }

    // GlfwMouse methods

    private boolean setCursorInputMode(int mode) {
        if (window != 0 && cursorInputMode != mode) {
            glfwSetInputMode(window, GLFW_CURSOR, mode);
            cursorInputMode = mode;
            return true;
        }

        return false;
    }

    private void positionMode() {
        // For backwards compatibility with previous Basic4GL versions,
        // once cursor has been hidden (by executing MouseXD() or MouseYD())
        // it remains hidden, even if using regular positions again.
        setCursorInputMode(isCursorVisible ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_HIDDEN);
    }

    private void offsetMode() {
        if (setCursorInputMode(GLFW_CURSOR_DISABLED)) {
            // Set previous position to current position, so that initial XD and YD return 0.
            prevX = x;
            prevY = y;
            isCursorVisible = false;
        }
    }

    public GLFWMouse(GLFWWindowManager windowManager) {
        window = 0;
        cursorInputMode = GLFW_CURSOR_NORMAL;
        x = 0.0;
        y = 0.0;
        prevX = 0.0;
        prevY = 0.0;
        prevScrollY = 0.0;
        wheelDelta = 0;
        isCursorVisible = true;

        // Get window handle when window has been created
        windowManager.subscribeWindowCreated(() -> {
            // Store window
            window = windowManager.getGLFWWindow();

            // Reset state
            cursorInputMode = GLFW_CURSOR_NORMAL;
            x = 0.0;
            y = 0.0;
            prevX = 0.0;
            prevY = 0.0;
            prevScrollY = 0.0;
            wheelDelta = 0;
            isCursorVisible = true;

            // Hide cursor if in full screen mode
            if (windowManager.getActiveParams().isFullscreen) {
                setCursorInputMode(GLFW_CURSOR_HIDDEN);
                isCursorVisible = false;
            }

            // Subscribe to scroll events
            glfwSetScrollCallback(window, new MouseScrollCallback());
            glfwSetCursorPosCallback(window, new MouseCursorPosCallback());
        });
        windowManager.subscribeBeforeDestroyWindow(() -> {
            glfwSetScrollCallback(window, null);
            glfwSetCursorPosCallback(window, null);
            setCursorInputMode(
                    GLFW_CURSOR_NORMAL); // Seems to be required to prevent cursor locking to window's region in some
            // cases
            window = 0;
        });
    }

    @Override
    public float getX() {
        if (window == 0) {
            return 0.0f;
        }
        positionMode();
        return (float) x;
    }

    @Override
    public float getY() {
        if (window == 0) {
            return 0.0f;
        }
        positionMode();
        return (float) y;
    }

    @Override
    public float getXD() {
        if (window == 0) {
            return 0.0f;
        }
        // TODO Review Qt note for porting to Java - is this still required?
        // Because we're using the Qt event loop, glfwPollEvents is not called normally.
        // However it seems to be required for relative mouse input to function correctly.
        glfwPollEvents();
        offsetMode();
        double offsetX = x - prevX;
        prevX = x;
        return (float) offsetX;
    }

    @Override
    public float getYD() {
        if (window == 0) {
            return 0.0f;
        }
        // TODO Review Qt note for porting to Java - is this still required?
        // Because we're using the Qt event loop, glfwPollEvents is not called normally.
        // However it seems to be required for relative mouse input to function correctly.
        glfwPollEvents();
        offsetMode();
        double offsetY = y - prevY;
        prevY = y;
        return (float) offsetY;
    }

    @Override
    public boolean getButton(int index) {
        if (window == 0) {
            return false;
        }
        return index >= GLFW_MOUSE_BUTTON_1
                && index <= GLFW_MOUSE_BUTTON_8
                && glfwGetMouseButton(window, index) == GLFW_PRESS;
    }

    @Override
    public int getWheelDelta() {
        if (window == 0) {
            return 0;
        }
        int result = wheelDelta;
        wheelDelta = 0;
        return result;
    }

    public void cursorPosCallback(double xpos, double ypos) {
        x = xpos;
        y = ypos;
    }

    public void scrollCallback(double xoffset, double yoffset) {
        double scrollY = prevScrollY + yoffset;
        wheelDelta += (int) (Math.floor(scrollY) - Math.floor(prevScrollY));
        prevScrollY = scrollY;
    }
}
