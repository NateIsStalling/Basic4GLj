package com.basic4gl.desktop.content.render;

import static com.basic4gl.desktop.util.HtmlUtil.escapeHtml;

import javax.swing.JComponent;

public final class UnsupportedContentRenderer implements ContentRenderer {

    @Override
    public boolean supports(String mediaType) {
        return true;
    }

    @Override
    public JComponent render(ContentRenderRequest request) {
        String html = "<!doctype html><html><body style='font-family:sans-serif;padding:12px;'>"
                + "<h2>Unsupported content</h2><p>This document uses media type <code>"
                + escapeHtml(request.document().mediaType())
                + "</code>, which cannot be displayed by this version of Basic4GLj.</p></body></html>";
        return RendererSupport.htmlComponent(html, request.navigationHandler());
    }
}
