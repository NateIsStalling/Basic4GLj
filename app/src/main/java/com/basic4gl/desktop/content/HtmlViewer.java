package com.basic4gl.desktop.content;

import com.basic4gl.desktop.editor.IFileViewer;
import com.basic4gl.desktop.editor.IFileEditorActionListener;
import com.basic4gl.desktop.editor.IToggleBreakpointListener;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.util.IFileManager;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.fife.ui.rsyntaxtextarea.LinkGenerator;
import org.fife.ui.rtextarea.SearchContext;

public class HtmlViewer implements FileViewer, IFileViewer {
    private static final AtomicBoolean JAVAFX_INITIALIZED = new AtomicBoolean(false);

    private final JPanel contentPane = new JPanel(new BorderLayout());
    private final JFXPanel panel;

    private WebView webView;

    private TextFileViewer textViewer;

    protected File file;

    protected String source;
    protected boolean readOnly = false;

    protected ViewMode viewMode = ViewMode.PREVIEW;

    public HtmlViewer() {
        panel = new JFXPanel();
        showViewMode();

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

    public HtmlViewer(
            PluginContext pluginContext,
            File file,
            IFileEditorActionListener actionListener,
            IFileManager fileManager,
            IToggleBreakpointListener toggleBreakpointListener,
            LinkGenerator linkGenerator,
            SearchContext searchContext) {
        this();
        textViewer = new TextFileViewer(
                file, actionListener, fileManager, toggleBreakpointListener, linkGenerator, searchContext);
        textViewer.getFileEditor().getEditorPane().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onEditorDocumentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onEditorDocumentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onEditorDocumentChanged();
            }
        });
        try {
            loadFile(pluginContext, file.toPath());
        } catch (FileViewerException ex) {
            pluginContext.dialogs().showDialog("Unable to load HTML file: " + ex.getMessage());
        }
        showViewMode();
    }

    @Override
    public void loadFile(PluginContext context, Path path) throws FileViewerException {
        file = path.toFile();

        try {

            String html = Files.readString(path, StandardCharsets.UTF_8);
            source = html;

            panel.putClientProperty("docs.path", path);

            loadHtmlContent(renderPreviewHtml(html));

        } catch (IOException ex) {
            context.dialogs().showDialog("Unable to load file: " + ex.getMessage());
        } catch (Throwable ex) {
            context.dialogs().showDialog("Unable to render html file: " + ex.getMessage());
        }
    }

    protected String renderPreviewHtml(String source) {
        return source == null ? "" : source;
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
        return contentPane;
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
        return contentPane;
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
        return textViewer != null && textViewer.isModified();
    }

    @Override
    public void setModified() {
        if (textViewer != null) {
            textViewer.setModified();
        }
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
        showViewMode();
    }

    @Override
    public ViewMode getViewMode() {
        return viewMode;
    }

    public FileEditor getFileEditor() {
        return textViewer != null ? textViewer.getFileEditor() : null;
    }

    private void onEditorDocumentChanged() {
        source = textViewer.getFileEditor().getEditorPane().getText();
        if (viewMode == ViewMode.PREVIEW || viewMode == ViewMode.EDITOR_AND_PREVIEW) {
            loadHtmlContent(renderPreviewHtml(source));
        }
    }

    private void showViewMode() {
        contentPane.removeAll();
        removeFromParent(panel);
        JComponent editorContent = textViewer != null ? textViewer.getContentPane() : null;
        if (editorContent != null) {
            removeFromParent(editorContent);
        }

        if (viewMode == ViewMode.EDITOR && editorContent != null) {
            contentPane.add(editorContent, BorderLayout.CENTER);
        } else if (viewMode == ViewMode.EDITOR_AND_PREVIEW && editorContent != null) {
            loadHtmlContent(renderPreviewHtml(textViewer.getFileEditor().getEditorPane().getText()));
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorContent, panel);
            splitPane.setResizeWeight(0.5);
            contentPane.add(splitPane, BorderLayout.CENTER);
        } else {
            if (textViewer != null) {
                loadHtmlContent(renderPreviewHtml(textViewer.getFileEditor().getEditorPane().getText()));
            }
            contentPane.add(panel, BorderLayout.CENTER);
        }

        contentPane.revalidate();
        contentPane.repaint();
    }

    private void removeFromParent(Component component) {
        Container parent = component.getParent();
        if (parent != null) {
            parent.remove(component);
        }
    }
}
