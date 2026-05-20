package com.basic4gl.library.desktopgl.input;

import com.basic4gl.library.desktopgl.window.OpenGLWindowManager;

import java.util.*;

import static org.lwjgl.system.windows.User32.VK_PAUSE;

public class OpenGLKeyboard {

    /// Callback functions for when key pressed.
    /// Parameter is whether key is scankey (true) or ASCII character (false)
    private ArrayList<KeyPressedCallback> keyPressedCallbacks = new ArrayList<>();
    private int unsubscribeHandleGenerator;
    private boolean isPausePressed;

    private void notifySubscribers(boolean isScanKey){
        // Iterate a COPY of the subscriber set, in case the subscriber unsubscribes
        // during processing of the notification. (E.g. Input$ BASIC command handler
        // completes when ENTER key is pressed).
        List<KeyPressedCallback> callbackCopy = new ArrayList<>(keyPressedCallbacks);
        for (KeyPressedCallback c : callbackCopy)
        {
            c.callback.onKeyPressed(isScanKey);
        }
    }
    private void processScanKey(int key, byte keybit){
        scanKeyMasks[key] |= keybit;

        // Add to buffer
        if (scanKeyBuffer.size() < 16) {
            scanKeyBuffer.add(key);
        }

        // Notify subscribers of scan key press
        notifySubscribers(true);
    }

    protected Queue<Integer> scanKeyBuffer = new ArrayDeque<>();
    protected Queue<Character> charBuffer = new ArrayDeque<>();
    protected byte[] scanKeyMasks = new byte[256];
    protected byte[] charMasks = new byte[128];

    protected OpenGLKeyboard(OpenGLWindowManager windowManager) {

        unsubscribeHandleGenerator = 0;
        isPausePressed = false;

        clearKeyState();
        windowManager.subscribeWindowCreated(() ->
        {
            clearKeyBuffers();
            clearKeyState();
        });
    }

    // Process keypresses, update bufferes and notify subscribers.
    // Descendent classes should call these in response to keyboard events.
    protected void scanKeyPress(int key, char c){
        // Special case: Detect PAUSE
        if (key == VK_PAUSE) {
            isPausePressed = true;
        }

        // Buffer scan key and mark as down
        if (key != 0) {
            processScanKey(key, (byte) 1);
        }

        // Mark character as down
        if (c != 0) {
            charMasks[c] |= 1;
        }
    }

    protected void scanKeyRelease(int key, char c) {
        // Mark scan key as up
        if (key != 0) {
            scanKeyMasks[key] &= ~1;
        }

        // Mark character as up
        if (c != 0) {
            charMasks[c] &= ~1;
        }
    }
    protected void keyPress(char c){
        if (charBuffer.size() < 16) {
            charBuffer.add(c);
        }

        // Notify subscribers of keypress
        notifySubscribers(false);
    }

    // Keyboard buffers
    public char getNextKey() {
        Character c = charBuffer.poll(); // null if empty
        return c != null ? c : 0;
    }

    public int getNextScanKey() {
        Integer key = scanKeyBuffer.poll(); // null if empty
        return key != null ? key : 0;
    }

    public void clearKeyBuffers(){
        charBuffer.clear();
        scanKeyBuffer.clear();
    }

    public void clearKeyState()
    {
        for (int i = 0; i < 256; i++) {
            scanKeyMasks[i] = 0;
        }
        for (int i = 0; i < 128; i++) {
            charMasks[i] = 0;
        }
    }

    // Key state
    public boolean isKeyDown(char key) {
        if (key >= 0 && key <= 127) {
            return charMasks[key] != 0;
        }
        return false;
    }
    public boolean isScanKeyDown(int scanKey){
        if (scanKey >= 0 && scanKey <= 255) {
            return scanKeyMasks[scanKey] != 0;
        }
        return false;
    }
    public boolean isPausePressed(){
        boolean result = isPausePressed;
        isPausePressed = false;				// Reset flag on read
        return result;
    }

    // Key notification
    public int subscribeKeyPressed(KeyPressedListener callback){
        int unsubscribeHandle = ++unsubscribeHandleGenerator;
        keyPressedCallbacks.add(new KeyPressedCallback(unsubscribeHandle, callback));
        return unsubscribeHandle;
    }
    public void unsubscribeKeyPressed(int unsubscribeHandle){
        Iterator<KeyPressedCallback> it = keyPressedCallbacks.iterator();
        while (it.hasNext()) {
            KeyPressedCallback entry = it.next();
            if (entry.unsubscribeHandle == unsubscribeHandle) {
                it.remove(); // safe removal during iteration
            }
        }
    }

    /**
     * Fake keypresses (e.g. for JoyKeys())
     * @param scanKey
     * @param c
     * @param keybit
     * @param isDown
     */
    public void fakeKeyState(int scanKey, char c, byte keybit, boolean isDown){
        if (scanKey != 0)
        {
            // Set/clear corresponding bit
            if (isDown) {
                if ((scanKeyMasks[scanKey] & keybit) == 0) {
                    processScanKey(scanKey, keybit);
                }
            }
            else {
                scanKeyMasks[scanKey] &= ~keybit;
            }
        }

        if (c != 0)
        {
            if (isDown) {
                boolean wasPressed = (charMasks[c] & keybit) != 0;
                charMasks[c] |= keybit;

                // Add to char buffer if wasn't previously pressed.
                // This also triggers notifications.
                if (!wasPressed) {
                    keyPress(c);
                }
            }
            else {
                charMasks[c] &= ~keybit;
            }
        }
    }
}
