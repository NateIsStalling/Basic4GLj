package com.basic4gl.desktop.content;

import static com.basic4gl.desktop.util.HtmlUtil.markdownToHtml;

import com.basic4gl.desktop.MainWindow;
import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.content.FileViewerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;

public class MarkdownViewer extends HtmlViewer {

    private static final String DOCS_MARKDOWN_STYLESHEET_RESOURCE = "/css/docs-markdown.css";
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

    @Override
    public void loadFile(PluginContext context, Path path) throws FileViewerException {
        file = path.toFile();

        try {
            String markdown = Files.readString(path, StandardCharsets.UTF_8);
            String html = buildMarkdownDocumentHtml(markdownToHtml(markdown));

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

    private String readTextResource(String resourcePath) {
        try (InputStream input = MainWindow.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return "";
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("Unable to load resource " + resourcePath + ": " + ex.getMessage());
            return "";
        }
    }

    private String buildMarkdownDocumentHtml(String bodyHtml) {
        String stylesheetText = readTextResource(DOCS_MARKDOWN_STYLESHEET_RESOURCE);
        return "<!doctype html><html><head><meta charset='UTF-8'><style>"
                + stylesheetText
                + "</style></head>"
                + bodyHtml
                + "</html>";
    }
}
