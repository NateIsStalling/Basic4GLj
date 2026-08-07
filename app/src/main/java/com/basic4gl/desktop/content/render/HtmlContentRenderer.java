package com.basic4gl.desktop.content.render;

import java.io.IOException;
import javax.swing.JComponent;

public final class HtmlContentRenderer implements ContentRenderer {

    @Override
    public boolean supports(String mediaType) {
        return RendererSupport.mediaTypeEquals(mediaType, "text/html");
    }

    @Override
    public JComponent render(ContentRenderRequest request) throws IOException {
        String html = RendererSupport.readString(request.materializedRoot(), request.currentPath());
        return RendererSupport.htmlComponent(html, request.navigationHandler());
    }
}
