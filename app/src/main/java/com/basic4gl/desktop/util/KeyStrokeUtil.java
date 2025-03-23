package com.basic4gl.desktop.util;

import java.awt.event.KeyEvent;
import javax.swing.*;

public final class KeyStrokeUtil {

	public static String getShortcutString(KeyStroke keyStroke) {
		if (keyStroke == null) {
			return "";
		}

		int modifiers = keyStroke.getModifiers();
		int keyCode = keyStroke.getKeyCode();

		String modifierSymbol;
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			modifierSymbol = "⌘"; // Command symbol for macOS
		} else {
			modifierSymbol = "Ctrl"; // Control for Windows/Linux
		}

		return modifierSymbol + " " + KeyEvent.getKeyText(keyCode);
	}
}
