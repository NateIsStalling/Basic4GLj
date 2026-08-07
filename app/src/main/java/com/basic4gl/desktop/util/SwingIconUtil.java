package com.basic4gl.desktop.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

public class SwingIconUtil {
    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ClassLoader.getSystemClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find resource file: " + path);
            return null;
        }
    }

    public static ImageIcon createImageIcon(String path, Color tint) {
        ImageIcon baseIcon = createImageIcon(path);
        if (baseIcon == null || tint == null) {
            return baseIcon;
        }
        return new ImageIcon(tintImage(baseIcon.getImage(), tint));
    }

    public static Icon buildImageThumbnailIcon(File file, int maxWidth, int maxHeight) {
        try {
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(file);
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                return null;
            }
            double scale = Math.min((double) maxWidth / image.getWidth(), (double) maxHeight / image.getHeight());
            scale = Math.min(1.0d, scale);
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException ex) {
            return null;
        }
    }

    public static Icon createScaledIcon(String iconPath, int size) {
        ImageIcon icon = createImageIcon(iconPath);
        if (icon == null) {
            return null;
        }
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static Icon createScaledIcon(String iconPath, int size, Color tint) {
        ImageIcon icon = createImageIcon(iconPath, tint);
        if (icon == null) {
            return null;
        }
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static BufferedImage tintImage(Image source, Color tint) {
        int width = source.getWidth(null);
        int height = source.getHeight(null);
        BufferedImage sourceImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sourceGraphics = sourceImage.createGraphics();
        sourceGraphics.drawImage(source, 0, 0, null);
        sourceGraphics.dispose();

        BufferedImage tinted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int tintRgb = tint.getRGB() & 0x00FFFFFF;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = sourceImage.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                tinted.setRGB(x, y, (alpha << 24) | tintRgb);
            }
        }
        return tinted;
    }
}
