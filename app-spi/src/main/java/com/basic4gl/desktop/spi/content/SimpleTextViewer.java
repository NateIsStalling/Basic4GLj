package com.basic4gl.desktop.spi.content;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;

/**
 * Simple text file viewer
 *
 * Features:
 * - Display plain text files
 * - Read-only display
 * - Syntax-agnostic (works with any text format)
 */
public class SimpleTextViewer implements FileViewer {

    private final JTextArea textArea;
    private final JScrollPane scrollPane;
    private Path currentPath;

    public SimpleTextViewer() {
        this.textArea = new JTextArea();
        this.textArea.setEditable(false);
        this.textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        this.textArea.setBackground(Color.WHITE);
        this.textArea.setForeground(Color.BLACK);
        this.textArea.setMargin(new Insets(5, 5, 5, 5));
        this.textArea.setLineWrap(false);

        this.scrollPane = new JScrollPane(textArea);
        this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    @Override
    public void loadFile(Path path) throws FileViewerException {
        try {
            if (!Files.exists(path)) {
                throw new FileViewerException("File not found: " + path);
            }

            // Limit file size to prevent memory issues (50 MB)
            long fileSize = Files.size(path);
            if (fileSize > 50 * 1024 * 1024) {
                throw new FileViewerException("File too large to display: " + fileSize + " bytes");
            }

            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            textArea.setText(content);
            textArea.setCaretPosition(0);
            currentPath = path;

        } catch (FileViewerException e) {
            throw e;
        } catch (Exception e) {
            throw new FileViewerException("Failed to load text file: " + e.getMessage(), e);
        }
    }

    @Override
    public JComponent getComponent() {
        return scrollPane;
    }

    @Override
    public boolean canHandle(String filename, String mimeType) {
        String lower = (filename != null ? filename.toLowerCase() : "");
        String mime = (mimeType != null ? mimeType.toLowerCase() : "");

        // Check by extension
        if (lower.endsWith(".txt")
                || lower.endsWith(".json")
                || lower.endsWith(".xml")
                || lower.endsWith(".html")
                || lower.endsWith(".htm")
                || lower.endsWith(".css")
                || lower.endsWith(".java")
                || lower.endsWith(".cpp")
                || lower.endsWith(".c")
                || lower.endsWith(".h")
                || lower.endsWith(".py")
                || lower.endsWith(".js")
                || lower.endsWith(".md")
                || lower.endsWith(".config")
                || lower.endsWith(".log")) {
            return true;
        }

        // Check by MIME type
        return mime.startsWith("text/");
    }

    @Override
    public String getName() {
        return "Text Viewer";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void dispose() {
        textArea.setText("");
        currentPath = null;
    }
}
