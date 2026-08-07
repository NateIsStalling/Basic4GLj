package com.basic4gl.desktop.content.render;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public final class PlainTextContentRenderer implements ContentRenderer {

    @Override
    public boolean supports(String mediaType) {
        return RendererSupport.mediaTypeEquals(mediaType, "text/plain");
    }

    @Override
    public JComponent render(ContentRenderRequest request) throws IOException {
        JTextArea textArea =
                new JTextArea(RendererSupport.readString(request.materializedRoot(), request.currentPath()));
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }
}
