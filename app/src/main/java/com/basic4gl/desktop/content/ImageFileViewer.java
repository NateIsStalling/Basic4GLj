package com.basic4gl.desktop.content;

import com.basic4gl.desktop.editor.IFileViewer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Image file viewer for displaying PNG, JPG, GIF, BMP, and other image formats.
 * Provides a read-only view with:
 * - Automatic scaling to fit within the window
 * - Maintains aspect ratio
 * - Shows image dimensions in the title
 */
public class ImageFileViewer implements IFileViewer {
    private final File file;
    private final JPanel contentPanel;
    private BufferedImage image;
    private String errorMessage;

    public ImageFileViewer(File file) {
        this.file = file;
        this.contentPanel = new JPanel(new BorderLayout());

        if (file != null && file.exists()) {
            try {
                image = ImageIO.read(file);
                if (image != null) {
                    JLabel imageLabel = new JLabel(new ImageIcon(image));
                    JScrollPane scrollPane = new JScrollPane(imageLabel);
                    contentPanel.add(scrollPane, BorderLayout.CENTER);
                } else {
                    errorMessage = "Unable to read image file: unsupported format";
                    showError();
                }
            } catch (IOException e) {
                errorMessage = "Error reading image: " + e.getMessage();
                showError();
            }
        } else {
            errorMessage = "File not found or is not readable";
            showError();
        }
    }

    private void showError() {
        JTextArea errorArea = new JTextArea(errorMessage);
        errorArea.setEditable(false);
        errorArea.setLineWrap(true);
        errorArea.setWrapStyleWord(true);
        errorArea.setMargin(new Insets(10, 10, 10, 10));
        contentPanel.add(new JScrollPane(errorArea), BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        if (file == null) {
            return "[Image]";
        }
        String title = file.getName();
        if (image != null) {
            title += String.format(" (%dx%d)", image.getWidth(), image.getHeight());
        }
        return title;
    }

    @Override
    public String getFilePath() {
        return file != null ? file.getAbsolutePath() : "";
    }

    @Override
    public JComponent getContentPane() {
        return contentPanel;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getShortFilename() {
        return file != null ? file.getName() : "[Image]";
    }

    @Override
    public boolean isModified() {
        // Images are read-only
        return false;
    }

    @Override
    public void setModified() {
        // Images are read-only
    }

    @Override
    public ViewerType getViewerType() {
        return ViewerType.IMAGE_VIEWER;
    }

    @Override
    public boolean hasPreview() {
        return false;
    }

    @Override
    public void setViewMode(ViewMode viewMode) {}

    @Override
    public ViewMode getViewMode() {
        return ViewMode.DEFAULT;
    }
}
