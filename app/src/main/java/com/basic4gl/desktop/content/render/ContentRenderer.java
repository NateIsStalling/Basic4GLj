package com.basic4gl.desktop.content.render;

import java.io.IOException;
import javax.swing.JComponent;

public interface ContentRenderer {

    boolean supports(String mediaType);

    JComponent render(ContentRenderRequest request) throws IOException;
}
