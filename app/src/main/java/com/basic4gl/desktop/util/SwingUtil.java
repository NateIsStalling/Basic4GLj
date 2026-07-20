package com.basic4gl.desktop.util;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import javax.swing.*;

public class SwingUtil {

    public static void configureSmoothScrolling(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.setWheelScrollingEnabled(true);
    }

    public static void hideSplitPaneHandle(JSplitPane splitPane) {
        if (splitPane == null) {
            return;
        }

        splitPane.putClientProperty(FlatClientProperties.STYLE, "style: plain");

        splitPane.setOneTouchExpandable(false);
    }

    public static Color createLighterPanelBackground() {
        Color base = UIManager.getColor("Panel.background");
        if (base == null) {
            base = new Color(238, 238, 238);
        }
        return new Color(
                Math.min(255, base.getRed() + 8),
                Math.min(255, base.getGreen() + 8),
                Math.min(255, base.getBlue() + 8));
    }
}
