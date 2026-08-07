package com.basic4gl.desktop.util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.*;

public final class KeyStrokeUtil {

    public static String getShortcutString(KeyStroke keyStroke) {
        if (keyStroke == null) {
            return "";
        }

        int modifiers = keyStroke.getModifiers();
        int keyCode = keyStroke.getKeyCode();

        ArrayList<String> modifierSymbol = new ArrayList<>();

        Toolkit toolkit = Toolkit.getDefaultToolkit();

        if ((modifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) {
            modifierSymbol.add("Shift");
        }

        if ((modifiers & toolkit.getMenuShortcutKeyMask()) != 0
                || (modifiers & toolkit.getMenuShortcutKeyMaskEx()) == 0) {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                modifierSymbol.add("⌘"); // Command symbol for macOS
            } else {
                modifierSymbol.add("Ctrl"); // Control for Windows/Linux
            }
        }

        return String.join(" + ", modifierSymbol) + " " + KeyEvent.getKeyText(keyCode);
    }
}
