package com.basic4gl.desktop.spi.content;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;

/**
 * Default image viewer with sane configuration
 *
 * Features:
 * - Supports common image formats (PNG, JPEG, GIF, BMP, etc.)
 * - Automatic scaling to fit window
 * - Preserves aspect ratio
 * - Efficient memory usage
 * - Scrollable for large images
 */
public class DefaultImageViewer implements FileViewer {

    private final JPanel panel;
    private BufferedImage image;
    private Path currentPath;
    private String errorMessage = "";

    public DefaultImageViewer() {
        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                    int panelWidth = getWidth();
                    int panelHeight = getHeight();
                    int imgWidth = image.getWidth();
                    int imgHeight = image.getHeight();

                    // Calculate scaling to fit panel while preserving aspect ratio
                    double scale = Math.min((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
                    scale = Math.min(scale, 1.0); // Don't upscale

                    int scaledWidth = (int) (imgWidth * scale);
                    int scaledHeight = (int) (imgHeight * scale);

                    int x = (panelWidth - scaledWidth) / 2;
                    int y = (panelHeight - scaledHeight) / 2;

                    g2d.drawImage(image, x, y, scaledWidth, scaledHeight, null);
                }
            }
        };

        panel.setBackground(Color.DARK_GRAY);
        panel.setLayout(null);
    }

    @Override
    public void loadFile(Path path) throws FileViewerException {
        try {
            if (!Files.exists(path)) {
                throw new FileViewerException("File not found: " + path);
            }

            byte[] imageBytes = Files.readAllBytes(path);
            image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));

            if (image == null) {
                throw new FileViewerException("Unable to read image file. Unsupported format?");
            }

            currentPath = path;
            errorMessage = "";
            panel.repaint();

        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw new FileViewerException("Failed to load image: " + e.getMessage(), e);
        }
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public boolean canHandle(String filename, String mimeType) {
        String lower = (filename != null ? filename.toLowerCase() : "");
        String mime = (mimeType != null ? mimeType.toLowerCase() : "");

        // Check by extension
        if (lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".bmp")
                || lower.endsWith(".webp")
                || lower.endsWith(".tiff")
                || lower.endsWith(".tif")
                || lower.endsWith(".ico")) {
            return true;
        }

        // Check by MIME type
        return mime.startsWith("image/");
    }

    @Override
    public String getName() {
        return "Image Viewer";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void dispose() {
        if (image != null) {
            image.flush();
            image = null;
        }
        currentPath = null;
    }
}
