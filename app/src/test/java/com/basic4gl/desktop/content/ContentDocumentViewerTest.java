package com.basic4gl.desktop.content;

import static org.junit.Assert.*;

import com.basic4gl.desktop.editor.IFileViewer;
import com.basic4gl.desktop.spi.content.ContentDocument;
import com.basic4gl.desktop.spi.content.MapContentSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ContentDocumentViewerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void documentationViewerTracksNavigationHistoryAndPinnedState() throws Exception {
        MapContentSource source = MapContentSource.fromBytes(Map.of(
                "index.md", "# Home".getBytes(StandardCharsets.UTF_8),
                "page2.txt", "Page 2".getBytes(StandardCharsets.UTF_8)));
        Path root = new ContentMaterializer(temporaryFolder.newFolder("cache").toPath())
                .materialize("plugin", "docs", "1", source);

        ContentDocumentViewer viewer = new ContentDocumentViewer(
                "plugin:docs:index", "Docs", new ContentDocument("text/markdown", source, "index.md"), root);

        assertEquals(IFileViewer.ViewerType.DOCUMENTATION_VIEWER, viewer.getViewerType());
        assertEquals("index.md", viewer.getCurrentPath());
        assertFalse(viewer.isModified());
        assertTrue(viewer.getFilePath().startsWith("content:plugin:docs:index#index.md"));

        viewer.navigateTo("page2.txt");
        assertEquals("page2.txt", viewer.getCurrentPath());

        viewer.goBack();
        assertEquals("index.md", viewer.getCurrentPath());

        viewer.goForward();
        assertEquals("page2.txt", viewer.getCurrentPath());

        viewer.goHome();
        assertEquals("index.md", viewer.getCurrentPath());

        viewer.keepOpen();
        assertTrue(viewer.isPinned());
    }
}
