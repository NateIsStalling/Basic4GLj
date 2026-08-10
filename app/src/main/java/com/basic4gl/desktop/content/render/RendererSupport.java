package com.basic4gl.desktop.content.render;

import com.basic4gl.desktop.spi.content.ContentPaths;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;

final class RendererSupport {

    private RendererSupport() {}

    static boolean mediaTypeEquals(String actual, String expected) {
        String normalizedActual =
                actual == null ? "" : actual.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return normalizedActual.equals(expected);
    }

    static String readString(Path materializedRoot, String currentPath) throws IOException {
        Path path = resolveMaterializedPath(materializedRoot, currentPath);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    static Path resolveMaterializedPath(Path materializedRoot, String normalizedPath) throws IOException {
        String safePath = ContentPaths.normalize(normalizedPath);
        Path path = materializedRoot
                .resolve(safePath.replace("/", materializedRoot.getFileSystem().getSeparator()))
                .normalize();
        if (!path.startsWith(materializedRoot)) {
            throw new IOException("Content path escapes materialized root: " + normalizedPath);
        }
        return path;
    }

    static JComponent htmlComponent(String html, ContentNavigationHandler navigationHandler) {
        JEditorPane pane = new JEditorPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.putClientProperty("basic4gl.content.html", html);
        pane.addHyperlinkListener(event -> {
            if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            String description = event.getDescription();
            if (description != null && description.startsWith("content:")) {
                navigationHandler.navigateTo(description.substring("content:".length()));
                return;
            }
            if (event.getURL() != null) {
                URI uri = URI.create(event.getURL().toExternalForm());
                if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                    navigationHandler.openExternal(uri);
                }
            }
        });
        pane.setText(html);
        pane.setCaretPosition(0);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(pane), BorderLayout.CENTER);
        return panel;
    }
}
