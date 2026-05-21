package com.basic4gl.library.desktopgl.glfw;

import com.basic4gl.library.desktopgl.input.OpenGLKeyboard;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.windows.User32.*;

public class GLFWKeyboard extends OpenGLKeyboard {

    class KeyCallback extends GLFWKeyCallback {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            handleKeypress(window, key, scancode, action, mods);
        }
    }

    class CharCallback extends GLFWCharCallback {
        @Override
        public void invoke(long window, int codepoint) {
            HandleChar((char) codepoint);
        }
    }

    public GLFWKeyboard(GLFWWindowManager windowManager){
        super(windowManager);

        // Hookup keyboard event when window is created
        windowManager.subscribeWindowCreated(() ->
        {
            glfwSetKeyCallback(windowManager.getGLFWWindow(), new KeyCallback());
            glfwSetCharCallback(windowManager.getGLFWWindow(), new CharCallback());
        });

        windowManager.subscribeBeforeDestroyWindow(() ->
        {
            System.out.println("destroy key callbacks");
            GLFWKeyCallback keyCallback = glfwSetKeyCallback(windowManager.getGLFWWindow(), null);
            if (keyCallback != null) {
                keyCallback.free();
            }
            GLFWCharCallback charCallback = glfwSetCharCallback(windowManager.getGLFWWindow(), null);
            if (charCallback != null) {
                charCallback.free();
            }
        });
    }


    int scanKeyFromGLFWKey(int key, boolean ignoreLeftRight)
    {
        switch (key)
        {
            case GLFW_KEY_BACKSPACE: return VK_BACK;
            case GLFW_KEY_TAB: return VK_TAB;
            case GLFW_KEY_ENTER: return VK_RETURN;
            case GLFW_KEY_PAUSE: return VK_PAUSE;
            case GLFW_KEY_CAPS_LOCK: return VK_CAPITAL;
            case GLFW_KEY_ESCAPE: return VK_ESCAPE;
            case GLFW_KEY_SPACE: return VK_SPACE;
            case GLFW_KEY_PAGE_UP: return VK_PRIOR;
            case GLFW_KEY_PAGE_DOWN: return VK_NEXT;
            case GLFW_KEY_END: return VK_END;
            case GLFW_KEY_HOME: return VK_HOME;
            case GLFW_KEY_LEFT: return VK_LEFT;
            case GLFW_KEY_UP: return VK_UP;
            case GLFW_KEY_RIGHT: return VK_RIGHT;
            case GLFW_KEY_DOWN: return VK_DOWN;
            case GLFW_KEY_PRINT_SCREEN: return VK_PRINT;
            case GLFW_KEY_INSERT: return VK_INSERT;
            case GLFW_KEY_DELETE: return VK_DELETE;

            // Alt/Menu keys
            case GLFW_KEY_LEFT_ALT: return ignoreLeftRight ? VK_MENU : VK_LMENU;
            case GLFW_KEY_RIGHT_ALT: return ignoreLeftRight ? VK_MENU : VK_RMENU;

            // Windows/Super keys + application menu key
            case GLFW_KEY_LEFT_SUPER: return VK_LWIN;
            case GLFW_KEY_RIGHT_SUPER: return VK_RWIN;
            case GLFW_KEY_MENU: return VK_APPS;

            case GLFW_KEY_KP_0: return VK_NUMPAD0;
            case GLFW_KEY_KP_1: return VK_NUMPAD1;
            case GLFW_KEY_KP_2: return VK_NUMPAD2;
            case GLFW_KEY_KP_3: return VK_NUMPAD3;
            case GLFW_KEY_KP_4: return VK_NUMPAD4;
            case GLFW_KEY_KP_5: return VK_NUMPAD5;
            case GLFW_KEY_KP_6: return VK_NUMPAD6;
            case GLFW_KEY_KP_7: return VK_NUMPAD7;
            case GLFW_KEY_KP_8: return VK_NUMPAD8;
            case GLFW_KEY_KP_9: return VK_NUMPAD9;
            case GLFW_KEY_KP_MULTIPLY: return VK_MULTIPLY;
            case GLFW_KEY_KP_ADD: return VK_ADD;
            case GLFW_KEY_KP_SUBTRACT: return VK_SUBTRACT;
            case GLFW_KEY_KP_DECIMAL: return VK_DECIMAL;
            case GLFW_KEY_KP_DIVIDE: return VK_DIVIDE;
            case GLFW_KEY_F1: return VK_F1;
            case GLFW_KEY_F2: return VK_F2;
            case GLFW_KEY_F3: return VK_F3;
            case GLFW_KEY_F4: return VK_F4;
            case GLFW_KEY_F5: return VK_F5;
            case GLFW_KEY_F6: return VK_F6;
            case GLFW_KEY_F7: return VK_F7;
            case GLFW_KEY_F8: return VK_F8;
            case GLFW_KEY_F9: return VK_F9;
            case GLFW_KEY_F10: return VK_F10;
            case GLFW_KEY_F11: return VK_F11;
            case GLFW_KEY_F12: return VK_F12;
            case GLFW_KEY_LEFT_SHIFT: return ignoreLeftRight ? VK_SHIFT : VK_LSHIFT;
            case GLFW_KEY_RIGHT_SHIFT: return ignoreLeftRight ? VK_SHIFT : VK_RSHIFT;
            case GLFW_KEY_LEFT_CONTROL: return ignoreLeftRight ? VK_CONTROL : VK_LCONTROL;
            case GLFW_KEY_RIGHT_CONTROL: return ignoreLeftRight ? VK_CONTROL : VK_RCONTROL;
            default:
                // Return none if unrecognised
                return 0;
        }
    }

    char charFromGlfwKey(int key)
    {
        // Return key if in printable range.
        if (key >= GLFW_KEY_SPACE && key <= GLFW_KEY_GRAVE_ACCENT) {
            return (char) key;
        }

        // Special case! Return key (13) is allowed as well, for backward compatibility with previous Basic4GL versions
        if (key == GLFW_KEY_ENTER || key == GLFW_KEY_KP_ENTER) {
            return 13;
        }

        // Don't treat any other scan keys as characters
        return 0;
    }

    void handleKeypress(long window, int key, int scancode, int action, int mods)
    {
        // Special case: ESCAPE key closes window
        if (key == GLFW_KEY_ESCAPE) {
            glfwSetWindowShouldClose(window, true);
        }

        // Push Windows scancode to queue.
        int scankey = scanKeyFromGLFWKey(key, false);
        int scanKeyForBuffer = scanKeyFromGLFWKey(key, true);
        char c = charFromGlfwKey(key);
        if (scankey == 0 && c == 0) {
            // Unknown scan key
            return;
        }
        switch (action)
        {
            case GLFW_PRESS:
            case GLFW_REPEAT:
                scanKeyPress(scanKeyForBuffer, c);

                // Special case! Return key is also added to buffer (for compatibility with older Basic4GLs)
                if (c == 13) {
                    keyPress(c);
                }

                break;

            case GLFW_RELEASE:
                scanKeyRelease(scanKeyForBuffer, c);
                break;
        }
    }

    void HandleChar(char codepoint)
    {
        // Process ASCII characters only
        if (codepoint < 128)
        {
            keyPress(codepoint);
        }
    }
}
