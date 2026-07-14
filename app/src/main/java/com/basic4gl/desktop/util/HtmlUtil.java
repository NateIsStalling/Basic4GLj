package com.basic4gl.desktop.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public final class HtmlUtil {
    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer MARKDOWN_RENDERER = HtmlRenderer.builder().build();

    private HtmlUtil() {
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }


    public static String markdownToHtml(String markdown) {
        String input = markdown == null ? "" : markdown;
        Node document = MARKDOWN_PARSER.parse(input);
        String htmlBody = MARKDOWN_RENDERER.render(document);
        return "<body class='markdown-body'>" + htmlBody + "</body>";
    }

}
