package com.basic4gl.desktop.content;

import com.basic4gl.desktop.MainWindow;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class MarkdownHtmlSupport {

    private static final String DOCS_MARKDOWN_STYLESHEET_RESOURCE = "/css/docs-markdown.css";

    private MarkdownHtmlSupport() {}

    public static String buildMarkdownDocumentHtml(String bodyHtml) {
        String stylesheetText = readTextResource(DOCS_MARKDOWN_STYLESHEET_RESOURCE);
        return "<!doctype html><html><head><meta charset='UTF-8'><style>"
                + stylesheetText
                + "</style></head>"
                + bodyHtml
                + "</html>";
    }

    private static String readTextResource(String resourcePath) {
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
}
