package com.basic4gl.desktop.content;

import com.basic4gl.desktop.editor.IFileViewer;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.content.FileViewer;
import com.basic4gl.desktop.spi.content.FileViewerException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.*;

public class HtmlViewer implements FileViewer, IFileViewer {
    private static final AtomicBoolean JAVAFX_INITIALIZED = new AtomicBoolean(false);

    private final JFXPanel panel;

    private WebView webView;

    protected File file;

    protected String source;
    protected boolean readOnly = false;

    protected ViewMode viewMode = ViewMode.PREVIEW;

    public HtmlViewer() {
        panel = new JFXPanel();

        ensureJavaFxInitialized();
        Platform.runLater(() -> {
            try {
                webView = new WebView();
                panel.setScene(new Scene(webView));
            } catch (Throwable ex) {
                showDocsFallback(panel, "", ex);
            }
        });
    }

    public HtmlViewer(PluginContext pluginContext, File file) {
        this();
        try {
            loadFile(pluginContext, file.toPath());
        } catch (FileViewerException ex) {
            pluginContext.dialogs().showDialog("Unable to load HTML file: " + ex.getMessage());
        }
    }

    @Override
    public void loadFile(PluginContext context, Path path) throws FileViewerException {
        file = path.toFile();

        try {

            String html = Files.readString(path, StandardCharsets.UTF_8);

            panel.putClientProperty("docs.path", path);

            loadHtmlContent(html);

        } catch (IOException ex) {
            context.dialogs().showDialog("Unable to load file: " + ex.getMessage());
        } catch (Throwable ex) {
            context.dialogs().showDialog("Unable to render html file: " + ex.getMessage());
        }
    }

    protected void loadHtmlContent(String html) {

        ensureJavaFxInitialized();

        Platform.runLater(() -> {
            try {
                webView.getEngine().loadContent(html, "text/html");
            } catch (Throwable ex) {
                showDocsFallback(panel, "", ex);
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public boolean canHandle(String filename, String mimeType) {
        if (filename.toLowerCase().endsWith(".html") || filename.toLowerCase().endsWith(".htm")) {
            return true;
        }

        return mimeType.equals("text/html");
    }

    @Override
    public String getName() {
        return "HTML Viewer";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void dispose() {
        // No resources to dispose
    }

    private void ensureJavaFxInitialized() {
        if (!JAVAFX_INITIALIZED.compareAndSet(false, true)) {
            return;
        }
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized by JFXPanel startup.
        }
        Platform.setImplicitExit(false);
    }

    private void showDocsFallback(JFXPanel panel, String html, Throwable ex) {
        System.err.println("Unable to initialize JavaFX WebView: " + ex.getMessage());
        SwingUtilities.invokeLater(() -> {
            JEditorPane fallbackPane = new JEditorPane();
            fallbackPane.setEditable(false);
            fallbackPane.setContentType("text/html");
            fallbackPane.setText(html);
            fallbackPane.setCaretPosition(0);
            panel.setLayout(new BorderLayout());
            panel.add(new JScrollPane(fallbackPane), BorderLayout.CENTER);
            panel.revalidate();
            panel.repaint();
        });
    }

    @Override
    public String getTitle() {
        return file != null ? file.getName() : "[HTML]";
    }

    @Override
    public String getFilePath() {
        return file != null ? file.getAbsolutePath() : "";
    }

    @Override
    public JComponent getContentPane() {
        return panel;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getShortFilename() {
        return file != null ? file.getName() : "[HTML]";
    }

    @Override
    public boolean isModified() {
        // TODO implement switching between preview and edit mode, wrapping TextFileViewer
        return false;
    }

    @Override
    public void setModified() {
        // TODO implement switching between preview and edit mode, wrapping TextFileViewer
    }

    @Override
    public ViewerType getViewerType() {
        return ViewerType.HTML_VIEWER;
    }

    @Override
    public boolean hasPreview() {
        return true;
    }

    @Override
    public void setViewMode(ViewMode viewMode) {
        if (viewMode == ViewMode.DEFAULT) {
            this.viewMode = ViewMode.PREVIEW;
        } else {
            this.viewMode = viewMode;
        }
    }

    @Override
    public ViewMode getViewMode() {
        return viewMode;
    }
}
