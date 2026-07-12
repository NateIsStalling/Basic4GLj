package com.basic4gl.desktop.util;

public final class HtmlUtil {
    private HtmlUtil() {
    }

    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }


    public static String markdownToHtml(String markdown) {
        StringBuilder html = new StringBuilder("<html><body style='font-family:sans-serif;'>");
        for (String line : markdown.split("\\R", -1)) {
            String escaped = escapeHtml(line);
            if (escaped.startsWith("### ")) {
                html.append("<h3>").append(escaped.substring(4)).append("</h3>");
            } else if (escaped.startsWith("## ")) {
                html.append("<h2>").append(escaped.substring(3)).append("</h2>");
            } else if (escaped.startsWith("# ")) {
                html.append("<h1>").append(escaped.substring(2)).append("</h1>");
            } else if (escaped.startsWith("- ")) {
                html.append("<p>&bull; ").append(escaped.substring(2)).append("</p>");
            } else if (escaped.isBlank()) {
                html.append("<br/>");
            } else {
                html.append("<p>").append(escaped).append("</p>");
            }
        }
        html.append("</body></html>");
        return html.toString();
    }

}
