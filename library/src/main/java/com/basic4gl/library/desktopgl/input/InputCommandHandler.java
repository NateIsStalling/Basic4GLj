package com.basic4gl.library.desktopgl.input;

import static org.lwjgl.system.windows.User32.*;

import com.basic4gl.language.core.extensions.Basic4GLLongRunningFunction;
import com.basic4gl.language.core.runtime.VM;
import com.basic4gl.library.desktopgl.content.Content2DManager;
import com.basic4gl.library.desktopgl.content.GLTextGrid;
import com.basic4gl.runtime.TomVM;

public class InputCommandHandler implements Basic4GLLongRunningFunction {
    private VM vm;
    private OpenGLKeyboard keyboard;
    private Content2DManager contentManager;
    private GLTextGrid textGrid;

    private int left; // Leftmost cursor position
    private boolean saveCursor;
    private boolean isStarted;
    private int unsubscribeHandle;

    private void processKeys() {
        processKeys(false);
    }

    private void processKeys(boolean forceRedraw) {
        // Keyboard input
        char c;
        int sc;
        do {
            c = 0;
            sc = keyboard.getNextScanKey();
            if (sc == 0) {
                c = keyboard.getNextKey();
            }

            switch (sc) {
                case VK_LEFT:
                    if (textGrid.getCursorX() > left) {
                        textGrid.setCursorPosition(textGrid.getCursorX() - 1, textGrid.getCursorY());
                        forceRedraw = true;
                    }
                    break;
                case VK_RIGHT:
                    if (textGrid.getCursorX() < textGrid.getColumns() - 1) {
                        textGrid.setCursorPosition(textGrid.getCursorX() + 1, textGrid.getCursorY());
                        forceRedraw = true;
                    }
                    break;
                case VK_DELETE:
                    textGrid.delete();
                    forceRedraw = true;
                    break;
                case VK_BACK:
                    if (textGrid.getCursorX() > left) {
                        textGrid.backspace();
                        forceRedraw = true;
                    }
                    break;
            }

            // Enter key
            // Note: Testing for return character (13) instead of scan key VK_ENTER, because some older Basic4GL
            // programs
            // rely on this behaviour. I.e. they call input immediately after inkey$() returns 13, at which point
            // the scan key buffer contains a VK_ENTER, but the return character has been removed from the character
            // buffer.
            // (The return character is a special case non-printable character that is added to the character buffer.
            // This is for compatibility with older Basic4GL versions)
            if (c == 13) {
                int lineOffset = textGrid.getCursorY() * textGrid.getColumns();
                int right = textGrid.getColumns();
                char[] chars = textGrid.getChars();
                while (right > left && chars[lineOffset + right - 1] <= ' ') // Trim spaces from right
                {
                    right--;
                }
                while (left < right && chars[lineOffset + left] <= ' ') // Trim spaces from left
                {
                    left++;
                }
                String result = "";
                for (int i = left; i < right; i++) {
                    result = result + chars[lineOffset + i];
                }

                // Restore cursor, perform newline and update screen
                textGrid.newLine();
                if (!saveCursor) {
                    textGrid.hideCursor();
                }
                redraw();

                // Pass result to Basic4GL and end function
                vm.setRegString(result);
                vm.endLongRunningFunction();
                return;
            }

            if (c >= ' ') {
                if (textGrid.getCursorX() < textGrid.getColumns() - 1 && textGrid.insert()) {
                    char[] buf = new char[2];
                    buf[0] = c;
                    buf[1] = 0;
                    String str = new String(buf);
                    textGrid.write(str);
                    forceRedraw = true;
                }
            }
        } while (c != 0 || sc != 0);

        if (forceRedraw) {
            redraw();
        }
    }

    private void redraw() {
        // Draw text
        contentManager.fullRedraw();
    }

    public InputCommandHandler(
            VM vm, OpenGLKeyboard keyboard, Content2DManager contentManager, GLTextGrid textGrid) {
        this.vm = vm;
        this.keyboard = keyboard;
        this.contentManager = contentManager;
        this.textGrid = textGrid;
        this.isStarted = false;
        this.unsubscribeHandle = 0;
        // Record leftmost cursor position
        left = textGrid.getCursorX();
        saveCursor = textGrid.isCursorShowing();
        textGrid.showCursor();

        // Ask to be notified of keyboard events
        unsubscribeHandle = keyboard.subscribeKeyPressed((isScanKey) -> {
            processKeys();
        });

        // Clear keyboard buffers before proceeding
        keyboard.clearKeyBuffers();
    }

    public void dispose() {
        keyboard.unsubscribeKeyPressed(unsubscribeHandle);
        unsubscribeHandle = 0;
    }

    public boolean isPolled() {
        // We want to be polled once, so that we can process any already buffered keypresses, and possibly
        // end the long running function immediately if the buffer contains an ENTER keypress.
        // After that, if the function is still active, we don't need to be polled, and can wait for keypress
        // notifications.
        return !isStarted;
    }

    public void poll() {
        isStarted = true;
        processKeys(true);
    }

    public boolean deleteWhenDone() {
        return true;
    }

    public void cancel() {
        // (Nothing to do)
    }
}
