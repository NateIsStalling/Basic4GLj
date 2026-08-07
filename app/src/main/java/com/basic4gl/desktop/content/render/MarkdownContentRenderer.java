package com.basic4gl.desktop.content.render;

import com.basic4gl.desktop.content.MarkdownHtmlSupport;
import com.basic4gl.desktop.spi.content.ContentPaths;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;

public final class MarkdownContentRenderer implements ContentRenderer {

    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;
    private static final int MAX_IMAGE_DIMENSION = 8192;

    @Override
    public boolean supports(String mediaType) {
        return RendererSupport.mediaTypeEquals(mediaType, "text/markdown")
                || RendererSupport.mediaTypeEquals(mediaType, "text/x-markdown");
    }

    @Override
    public JComponent render(ContentRenderRequest request) throws IOException {
        String markdown = RendererSupport.readString(request.materializedRoot(), request.currentPath());
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder()
                .escapeHtml(true)
                .attributeProviderFactory(
                        new ContentAttributeProviderFactory(request.materializedRoot(), request.currentPath()))
                .build();
        Node document = parser.parse(markdown);
        String html = MarkdownHtmlSupport.buildMarkdownDocumentHtml(
                "<body class='markdown-body'>" + renderer.render(document) + "</body>");
        return RendererSupport.htmlComponent(html, request.navigationHandler());
    }

    private record ContentAttributeProviderFactory(Path materializedRoot, String currentPath)
            implements AttributeProviderFactory {
        @Override
        public AttributeProvider create(AttributeProviderContext context) {
            return new ContentAttributeProvider(materializedRoot, currentPath);
        }
    }

    private record ContentAttributeProvider(Path materializedRoot, String currentPath) implements AttributeProvider {
        @Override
        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
            if (node instanceof Image) {
                rewriteImage(attributes);
            } else if (node instanceof Link) {
                rewriteLink(attributes);
            }
        }

        private void rewriteImage(Map<String, String> attributes) {
            String destination = attributes.get("src");
            if (destination == null || isExternal(destination)) {
                attributes.put("src", missingImagePlaceholder("Remote image blocked"));
                return;
            }
            try {
                String resolved = ContentPaths.resolve(currentPath, destination);
                Path imagePath = RendererSupport.resolveMaterializedPath(materializedRoot, resolved);
                if (!Files.isRegularFile(imagePath) || !isAllowedImage(imagePath)) {
                    attributes.put("src", missingImagePlaceholder("Image unavailable"));
                    return;
                }
                attributes.put("src", imagePath.toUri().toString());
            } catch (IOException | IllegalArgumentException ex) {
                attributes.put("src", missingImagePlaceholder("Image unavailable"));
            }
        }

        private void rewriteLink(Map<String, String> attributes) {
            String destination = attributes.get("href");
            if (destination == null) {
                return;
            }
            if (isExternal(destination)) {
                return;
            }
            try {
                String resolved = ContentPaths.resolve(currentPath, destination);
                if (isNavigableDocument(resolved)) {
                    attributes.put("href", "content:" + resolved);
                } else {
                    Path target = RendererSupport.resolveMaterializedPath(materializedRoot, resolved);
                    attributes.put("href", target.toUri().toString());
                }
            } catch (IOException | IllegalArgumentException ex) {
                attributes.put("href", "#");
            }
        }

        private boolean isAllowedImage(Path imagePath) throws IOException {
            if (Files.size(imagePath) > MAX_IMAGE_BYTES) {
                return false;
            }
            try (ImageInputStream input = ImageIO.createImageInputStream(imagePath.toFile())) {
                if (input == null) {
                    return true;
                }
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) {
                    return true;
                }
                ImageReader reader = readers.next();
                try {
                    reader.setInput(input);
                    return reader.getWidth(0) <= MAX_IMAGE_DIMENSION && reader.getHeight(0) <= MAX_IMAGE_DIMENSION;
                } finally {
                    reader.dispose();
                }
            }
        }

        private boolean isNavigableDocument(String path) {
            String lower = path.toLowerCase(Locale.ROOT);
            return lower.endsWith(".md")
                    || lower.endsWith(".markdown")
                    || lower.endsWith(".html")
                    || lower.endsWith(".htm")
                    || lower.endsWith(".txt");
        }

        private boolean isExternal(String destination) {
            String lower = destination.toLowerCase(Locale.ROOT);
            return lower.startsWith("http://")
                    || lower.startsWith("https://")
                    || lower.startsWith("file:")
                    || lower.startsWith("jar:");
        }

        private String missingImagePlaceholder(String message) {
            String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='220' height='40'>"
                    + "<rect width='100%' height='100%' fill='#f5f5f5' stroke='#bdbdbd'/>"
                    + "<text x='10' y='25' fill='#616161' font-family='sans-serif' font-size='13'>"
                    + message + "</text></svg>";
            return "data:image/svg+xml;utf8," + URLEncoder.encode(svg, StandardCharsets.UTF_8);
        }
    }
}
