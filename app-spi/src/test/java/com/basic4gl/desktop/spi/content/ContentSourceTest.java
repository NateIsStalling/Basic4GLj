package com.basic4gl.desktop.spi.content;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ContentSourceTest {

    @TempDir
    Path tempDir;

    @Test
    void mapContentSourceOpensAndListsNormalizedResources() throws Exception {
        MapContentSource source =
                MapContentSource.fromBytes(Map.of("docs\\index.md", "Hello".getBytes(StandardCharsets.UTF_8)));

        assertEquals("Hello", readString(source.open("docs/index.md")));
        assertEquals(Set.of("docs/index.md"), paths(source));
        assertThrows(IllegalArgumentException.class, () -> source.open("../outside.md"));
        assertThrows(FileNotFoundException.class, () -> source.open("missing.md"));
    }

    @Test
    void directoryContentSourceOpensAndListsResources() throws Exception {
        Path docs = Files.createDirectories(tempDir.resolve("docs/images"));
        Files.writeString(tempDir.resolve("docs/index.md"), "Index");
        Files.writeString(docs.resolve("sprite.png"), "Image");

        DirectoryContentSource source = new DirectoryContentSource(tempDir);

        assertEquals("Index", readString(source.open("docs/index.md")));
        assertEquals(Set.of("docs/index.md", "docs/images/sprite.png"), paths(source));
        assertThrows(IllegalArgumentException.class, () -> source.open("../outside.md"));
    }

    @Test
    void zipContentSourceOpensAndListsResourcesUnderPrefix() throws Exception {
        Path zipPath = tempDir.resolve("docs.zip");
        writeZip(
                zipPath,
                Map.of(
                        "content/index.md", "Index",
                        "content/images/sprite.png", "Image",
                        "other/ignored.md", "Ignored"));

        ZipContentSource source = new ZipContentSource(zipPath, "content");

        assertEquals("Index", readString(source.open("index.md")));
        assertEquals(Set.of("index.md", "images/sprite.png"), paths(source));
    }

    @Test
    void jarContentSourceUsesZipBehavior() throws Exception {
        Path jarPath = tempDir.resolve("docs.jar");
        writeZip(jarPath, Map.of("docs/index.md", "Index"));

        JarContentSource source = new JarContentSource(jarPath, "docs");

        assertEquals("Index", readString(source.open("index.md")));
        assertEquals(Set.of("index.md"), paths(source));
    }

    private static Set<String> paths(ContentSource source) throws Exception {
        return source.resources().stream().map(ContentResource::path).collect(Collectors.toSet());
    }

    private static String readString(InputStream input) throws Exception {
        try (input) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void writeZip(Path zipPath, Map<String, String> entries) throws Exception {
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                output.putNextEntry(new ZipEntry(entry.getKey()));
                output.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                output.closeEntry();
            }
        }
    }
}
