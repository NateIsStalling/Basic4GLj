package com.basic4gl.desktop.content.render;

import static org.junit.Assert.*;

import com.basic4gl.desktop.content.ContentMaterializer;
import com.basic4gl.desktop.spi.content.ContentDocument;
import com.basic4gl.desktop.spi.content.MapContentSource;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ContentRendererTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void markdownRendererRewritesImagesAndInternalLinks() throws Exception {
        String markdown = "# Drawing Sprites\n"
                + "![Sprite](../images/sprite.png)\n"
                + "[Next](page2.md)\n"
                + "![Remote](https://example.invalid/remote.png)\n"
                + "<script>alert('x')</script>";
        MapContentSource source = MapContentSource.fromBytes(Map.of(
                "tutorials/sprites.md", markdown.getBytes(StandardCharsets.UTF_8),
                "tutorials/page2.md", "Next".getBytes(StandardCharsets.UTF_8),
                "images/sprite.png", pngBytes()));
        Path root = new ContentMaterializer(temporaryFolder.newFolder("cache").toPath())
                .materialize("plugin", "docs", "1", source);

        JComponent component = new MarkdownContentRenderer()
                .render(new ContentRenderRequest(
                        new ContentDocument("text/markdown", source, "tutorials/sprites.md"),
                        root,
                        "tutorials/sprites.md",
                        ContentNavigationHandler.NO_OP));

        String html = html(component);
        assertTrue(html.contains(root.resolve("images/sprite.png").toUri().toString()));
        assertTrue(html.contains("content:tutorials/page2.md"));
        assertFalse(html.contains("https://example.invalid/remote.png"));
        assertTrue(html.contains("&lt;script&gt;alert('x')&lt;/script&gt;"));
    }

    @Test
    public void htmlRendererDisplaysHtmlContent() throws Exception {
        MapContentSource source =
                MapContentSource.fromBytes(Map.of("index.html", "<h1>Hello</h1>".getBytes(StandardCharsets.UTF_8)));
        Path root = new ContentMaterializer(temporaryFolder.newFolder("cache").toPath())
                .materialize("plugin", "docs", "1", source);

        JComponent component = new HtmlContentRenderer()
                .render(new ContentRenderRequest(
                        new ContentDocument("text/html", source, "index.html"),
                        root,
                        "index.html",
                        ContentNavigationHandler.NO_OP));

        assertTrue(html(component).contains("<h1>Hello</h1>"));
    }

    @Test
    public void plainTextRendererDisplaysReadOnlyText() throws Exception {
        MapContentSource source =
                MapContentSource.fromBytes(Map.of("readme.txt", "Hello".getBytes(StandardCharsets.UTF_8)));
        Path root = new ContentMaterializer(temporaryFolder.newFolder("cache").toPath())
                .materialize("plugin", "docs", "1", source);

        JComponent component = new PlainTextContentRenderer()
                .render(new ContentRenderRequest(
                        new ContentDocument("text/plain", source, "readme.txt"),
                        root,
                        "readme.txt",
                        ContentNavigationHandler.NO_OP));

        JTextArea textArea = find(component, JTextArea.class);
        assertEquals("Hello", textArea.getText());
        assertFalse(textArea.isEditable());
    }

    @Test
    public void unsupportedRendererDoesNotCrash() {
        MapContentSource source = MapContentSource.fromBytes(Map.of("data.bin", new byte[0]));

        JComponent component = new UnsupportedContentRenderer()
                .render(new ContentRenderRequest(
                        new ContentDocument("application/octet-stream", source, "data.bin"),
                        Path.of("."),
                        "data.bin",
                        ContentNavigationHandler.NO_OP));

        assertTrue(html(component).contains("Unsupported content"));
    }

    private static String html(JComponent component) {
        JEditorPane pane = find(component, JEditorPane.class);
        Object html = pane.getClientProperty("basic4gl.content.html");
        return html == null ? pane.getText() : html.toString();
    }

    private static byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    private static <T extends Component> T find(Component component, Class<T> type) {
        if (type.isInstance(component)) {
            return type.cast(component);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                T found = find(child, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
