package com.basic4gl.desktop.util;

import javax.swing.*;

public class SwingIconUtil {
    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ClassLoader.getSystemClassLoader().getResource(
                path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find resource file: " + path);
            return null;
        }
    }
}
