package com.basic4gl.desktop.content;

import static com.basic4gl.desktop.util.HtmlUtil.markdownToHtml;

import com.basic4gl.desktop.editor.IFileEditorActionListener;
import com.basic4gl.desktop.editor.IToggleBreakpointListener;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.content.FileViewerException;
import com.basic4gl.desktop.util.IFileManager;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;
import org.fife.ui.rsyntaxtextarea.LinkGenerator;
import org.fife.ui.rtextarea.SearchContext;

public class MarkdownViewer extends HtmlViewer {

    private static final String DOCS_EXPLORER_TAB_TITLE = "Explorer";

    public MarkdownViewer() {
        super();
    }

    public MarkdownViewer(PluginContext pluginContext, File file) {
        super();
        try {
            loadFile(pluginContext, file.toPath());
        } catch (FileViewerException ex) {
            pluginContext.dialogs().showDialog("Unable to load markdown file: " + ex.getMessage());
        }
    }

    public MarkdownViewer(
            PluginContext pluginContext,
            File file,
            IFileEditorActionListener actionListener,
            IFileManager fileManager,
            IToggleBreakpointListener toggleBreakpointListener,
            LinkGenerator linkGenerator,
            SearchContext searchContext) {
        super(pluginContext, file, actionListener, fileManager, toggleBreakpointListener, linkGenerator, searchContext);
    }

    @Override
    public void loadFile(PluginContext context, Path path) throws FileViewerException {
        file = path.toFile();

        try {
            String markdown = Files.readString(path, StandardCharsets.UTF_8);
            source = markdown;
            String html = renderPreviewHtml(markdown);

            loadHtmlContent(html);

        } catch (IOException ex) {
            context.dialogs().showDialog("Unable to open markdown file: " + ex.getMessage());
        } catch (Throwable ex) {
            context.dialogs().showDialog("Unable to render markdown file: " + ex.getMessage());
        }
    }

    @Override
    public String getTitle() {
        return file != null ? file.getName() : "[Markdown]";
    }

    @Override
    public String getShortFilename() {
        return file != null ? file.getName() : "[Markdown]";
    }

    @Override
    public ViewerType getViewerType() {
        return ViewerType.MARKDOWN_VIEWER;
    }

    @Override
    protected String renderPreviewHtml(String source) {
        return MarkdownHtmlSupport.buildMarkdownDocumentHtml(markdownToHtml(source == null ? "" : source));
    }
}
