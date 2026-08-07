package com.basic4gl.desktop.content;

import static org.junit.Assert.*;

import com.basic4gl.desktop.spi.content.ContentResource;
import com.basic4gl.desktop.spi.content.ContentSource;
import com.basic4gl.desktop.spi.content.DirectoryContentSource;
import com.basic4gl.desktop.spi.content.MapContentSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ContentMaterializerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void materializesMapContentAndReusesSameVersion() throws Exception {
        ContentMaterializer materializer =
                new ContentMaterializer(temporaryFolder.newFolder("cache").toPath());
        MapContentSource source = MapContentSource.fromBytes(Map.of(
                "index.md", "Index".getBytes(StandardCharsets.UTF_8),
                "images/sprite.png", "Image".getBytes(StandardCharsets.UTF_8)));

        Path first = materializer.materialize("plugin id", "docs", "1.0", source);
        Path second = materializer.materialize("plugin id", "docs", "1.0", source);

        assertEquals(first, second);
        assertEquals("Index", Files.readString(first.resolve("index.md")));
        assertEquals("Image", Files.readString(first.resolve("images/sprite.png")));
        assertTrue(first.toString().contains("plugin_id"));
    }

    @Test
    public void providerVersionChangesMaterializedDirectory() throws Exception {
        ContentMaterializer materializer =
                new ContentMaterializer(temporaryFolder.newFolder("cache").toPath());
        MapContentSource source =
                MapContentSource.fromBytes(Map.of("index.md", "Index".getBytes(StandardCharsets.UTF_8)));

        Path version1 = materializer.materialize("plugin", "docs", "1", source);
        Path version2 = materializer.materialize("plugin", "docs", "2", source);

        assertNotEquals(version1, version2);

        materializer.deleteObsoleteVersions("plugin", "docs", "2");

        assertFalse(Files.exists(version1));
        assertTrue(Files.exists(version2));
    }

    @Test
    public void materializesDirectorySource() throws Exception {
        Path root = temporaryFolder.newFolder("docs").toPath();
        Files.createDirectories(root.resolve("tutorials"));
        Files.writeString(root.resolve("tutorials/sprites.md"), "Sprites");

        ContentMaterializer materializer =
                new ContentMaterializer(temporaryFolder.newFolder("cache").toPath());
        Path materialized = materializer.materialize("plugin", "docs", "1", new DirectoryContentSource(root));

        assertEquals("Sprites", Files.readString(materialized.resolve("tutorials/sprites.md")));
    }

    @Test
    public void rejectsUnsafeResourcePaths() throws Exception {
        ContentMaterializer materializer =
                new ContentMaterializer(temporaryFolder.newFolder("cache").toPath());

        assertThrows(IOException.class, () -> materializer.materialize("plugin", "docs", "1", new UnsafeSource()));
    }

    private static final class UnsafeSource implements ContentSource {
        @Override
        public InputStream open(String normalizedPath) {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public Collection<ContentResource> resources() {
            return List.of(new ContentResource("../../outside.md"));
        }
    }
}
