package com.basic4gl.desktop.content;

import com.basic4gl.desktop.content.render.ContentNavigationHandler;
import com.basic4gl.desktop.content.render.ContentRenderRequest;
import com.basic4gl.desktop.content.render.ContentRenderer;
import com.basic4gl.desktop.content.render.HtmlContentRenderer;
import com.basic4gl.desktop.content.render.MarkdownContentRenderer;
import com.basic4gl.desktop.content.render.PlainTextContentRenderer;
import com.basic4gl.desktop.content.render.UnsupportedContentRenderer;
import com.basic4gl.desktop.editor.IFileViewer;
import com.basic4gl.desktop.spi.content.ContentDocument;
import com.basic4gl.desktop.spi.content.ContentPaths;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class ContentDocumentViewer implements IFileViewer {

    private final String contentId;
    private final String title;
    private final ContentDocument rootDocument;
    private final Path materializedRoot;
    private final List<ContentRenderer> renderers;
    private final JPanel contentPane = new JPanel(new BorderLayout());
    private final JPanel documentHost = new JPanel(new BorderLayout());
    private final JButton backButton = new JButton("Back");
    private final JButton forwardButton = new JButton("Forward");
    private final JButton homeButton = new JButton("Home");
    private final JButton keepOpenButton = new JButton("Keep Open");
    private final ArrayDeque<String> backStack = new ArrayDeque<>();
    private final ArrayDeque<String> forwardStack = new ArrayDeque<>();

    private String currentPath;
    private boolean pinned;

    public ContentDocumentViewer(String contentId, String title, ContentDocument document, Path materializedRoot)
            throws IOException {
        this(
                contentId,
                title,
                document,
                materializedRoot,
                List.of(
                        new MarkdownContentRenderer(),
                        new HtmlContentRenderer(),
                        new PlainTextContentRenderer(),
                        new UnsupportedContentRenderer()));
    }

    public ContentDocumentViewer(
            String contentId,
            String title,
            ContentDocument document,
            Path materializedRoot,
            List<ContentRenderer> renderers)
            throws IOException {
        this.contentId = contentId == null || contentId.isBlank() ? "content" : contentId;
        this.title = title == null || title.isBlank() ? "Documentation" : title;
        this.rootDocument = document;
        this.materializedRoot = materializedRoot;
        this.renderers = List.copyOf(renderers);
        this.currentPath = ContentPaths.normalize(document.entryPath());
        buildChrome();
        renderCurrent();
    }

    public void navigateTo(String normalizedPath) {
        String nextPath = ContentPaths.normalize(normalizedPath);
        if (nextPath.equals(currentPath)) {
            return;
        }
        backStack.push(currentPath);
        forwardStack.clear();
        currentPath = nextPath;
        renderOrShowError();
    }

    public void goBack() {
        if (backStack.isEmpty()) {
            return;
        }
        forwardStack.push(currentPath);
        currentPath = backStack.pop();
        renderOrShowError();
    }

    public void goForward() {
        if (forwardStack.isEmpty()) {
            return;
        }
        backStack.push(currentPath);
        currentPath = forwardStack.pop();
        renderOrShowError();
    }

    public void goHome() {
        navigateTo(rootDocument.entryPath());
    }

    public boolean isPinned() {
        return pinned;
    }

    public void keepOpen() {
        pinned = true;
        keepOpenButton.setEnabled(false);
    }

    public String getCurrentPath() {
        return currentPath;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getFilePath() {
        return "content:" + contentId + "#" + currentPath;
    }

    @Override
    public JComponent getContentPane() {
        return contentPane;
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public String getShortFilename() {
        return title;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void setModified() {}

    @Override
    public ViewerType getViewerType() {
        return ViewerType.DOCUMENTATION_VIEWER;
    }

    @Override
    public boolean hasPreview() {
        return false;
    }

    @Override
    public void setViewMode(ViewMode viewMode) {}

    @Override
    public ViewMode getViewMode() {
        return ViewMode.PREVIEW;
    }

    private void buildChrome() {
        JPanel toolbar = new JPanel();
        toolbar.add(backButton);
        toolbar.add(forwardButton);
        toolbar.add(homeButton);
        toolbar.add(keepOpenButton);
        toolbar.add(new JLabel(title));

        backButton.addActionListener(e -> goBack());
        forwardButton.addActionListener(e -> goForward());
        homeButton.addActionListener(e -> goHome());
        keepOpenButton.addActionListener(e -> keepOpen());

        contentPane.add(toolbar, BorderLayout.NORTH);
        contentPane.add(documentHost, BorderLayout.CENTER);
        refreshButtons();
    }

    private void renderOrShowError() {
        try {
            renderCurrent();
        } catch (IOException | RuntimeException ex) {
            showError(ex);
        }
    }

    private void renderCurrent() throws IOException {
        ContentDocument currentDocument =
                new ContentDocument(mediaTypeForPath(currentPath), rootDocument.source(), currentPath);
        ContentRenderer renderer = rendererFor(currentDocument.mediaType());
        JComponent component = renderer.render(new ContentRenderRequest(
                currentDocument, materializedRoot, currentPath, new ContentNavigationHandler() {
                    @Override
                    public void navigateTo(String normalizedPath) {
                        ContentDocumentViewer.this.navigateTo(normalizedPath);
                    }

                    @Override
                    public void openExternal(URI uri) {}
                }));
        documentHost.removeAll();
        documentHost.add(component, BorderLayout.CENTER);
        refreshButtons();
        documentHost.revalidate();
        documentHost.repaint();
    }

    private ContentRenderer rendererFor(String mediaType) {
        return renderers.stream()
                .filter(renderer -> renderer.supports(mediaType))
                .findFirst()
                .orElseGet(UnsupportedContentRenderer::new);
    }

    private String mediaTypeForPath(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
            return "text/markdown";
        }
        if (lower.endsWith(".html") || lower.endsWith(".htm")) {
            return "text/html";
        }
        if (lower.endsWith(".txt")) {
            return "text/plain";
        }
        return rootDocument.mediaType();
    }

    private void refreshButtons() {
        backButton.setEnabled(!backStack.isEmpty());
        forwardButton.setEnabled(!forwardStack.isEmpty());
        homeButton.setEnabled(!currentPath.equals(rootDocument.entryPath()));
    }

    private void showError(Exception ex) {
        SwingUtilities.invokeLater(() -> {
            documentHost.removeAll();
            documentHost.add(new JLabel("Unable to open documentation: " + ex.getMessage()), BorderLayout.CENTER);
            refreshButtons();
            documentHost.revalidate();
            documentHost.repaint();
        });
    }
}
