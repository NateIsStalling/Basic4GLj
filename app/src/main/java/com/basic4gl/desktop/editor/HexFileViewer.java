package com.basic4gl.desktop.editor;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.*;

/**
 * Hex file viewer for displaying binary files in hexadecimal format.
 * Provides a read-only view showing:
 * - Byte offsets on the left
 * - Hexadecimal representation in the center
 * - ASCII representation on the right
 * - File statistics
 */
public class HexFileViewer implements IFileViewer {
    private final File file;
    private final JPanel contentPanel;
    private final JTextArea hexArea;
    private static final int BYTES_PER_LINE = 16;

    public HexFileViewer(File file) {
        this.file = file;
        this.hexArea = new JTextArea();
        this.contentPanel = new JPanel(new BorderLayout());

        setupUI();

        if (file != null && file.exists()) {
            loadFileAsHex();
        } else {
            showError("File not found or is not readable");
        }
    }

    private void setupUI() {
        hexArea.setEditable(false);
        hexArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        hexArea.setTabSize(4);

        JScrollPane scrollPane = new JScrollPane(hexArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Info panel at bottom
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel(" File: " + (file != null ? file.getName() : "[Binary]"));
        infoPanel.add(infoLabel);
        contentPanel.add(infoPanel, BorderLayout.SOUTH);
    }

    private void loadFileAsHex() {
        try {
            StringBuilder hexContent = new StringBuilder();
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[BYTES_PER_LINE];
                long offset = 0;
                int bytesRead;

                hexContent.append("Offset      Hex Bytes                                          ASCII\n");
                hexContent.append("--------    --------------------------------------------------  ----------------\n");

                while ((bytesRead = fis.read(buffer)) != -1) {
                    // Offset
                    hexContent.append(String.format("%08X    ", offset));

                    // Hex representation
                    StringBuilder hexBuilder = new StringBuilder();
                    StringBuilder asciiBuilder = new StringBuilder();

                    for (int i = 0; i < BYTES_PER_LINE; i++) {
                        if (i < bytesRead) {
                            byte b = buffer[i];
                            hexBuilder.append(String.format("%02X ", b & 0xFF));

                            char c = (char) (b & 0xFF);
                            if (Character.isWhitespace(c) && c != ' ') {
                                asciiBuilder.append('.');
                            } else if (c >= 32 && c < 127) {
                                asciiBuilder.append(c);
                            } else {
                                asciiBuilder.append('.');
                            }
                        } else {
                            hexBuilder.append("   ");
                            asciiBuilder.append(" ");
                        }
                    }

                    hexContent.append(String.format("%-48s ", hexBuilder.toString()));
                    hexContent.append(asciiBuilder.toString());
                    hexContent.append("\n");

                    offset += bytesRead;
                }
            }
            hexArea.setText(hexContent.toString());
            hexArea.setCaretPosition(0);
        } catch (IOException e) {
            showError("Error reading file: " + e.getMessage());
        }
    }

    private void showError(String message) {
        hexArea.setText("Error: " + message);
    }

    @Override
    public String getTitle() {
        if (file == null) {
            return "[Binary]";
        }
        return file.getName() + " (Hex)";
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
        return file != null ? file.getName() : "[Binary]";
    }

    @Override
    public boolean isModified() {
        // Binary files are read-only
        return false;
    }

    @Override
    public void setModified() {
        // Binary files are read-only
    }

    @Override
    public ViewerType getViewerType() {
        return ViewerType.HEX_VIEWER;
    }
}
