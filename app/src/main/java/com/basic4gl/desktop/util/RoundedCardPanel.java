package com.basic4gl.desktop.util;

import javax.swing.*;
import java.awt.*;

public class RoundedCardPanel extends JPanel {
    public static final int DEFAULT_ARC = 14;

    private final int arc;

    public RoundedCardPanel() {
        this(DEFAULT_ARC);
    }

    public RoundedCardPanel(int arc) {
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}