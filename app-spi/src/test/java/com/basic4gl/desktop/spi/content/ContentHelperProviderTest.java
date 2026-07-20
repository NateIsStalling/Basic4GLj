package com.basic4gl.desktop.spi.content;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ContentHelperProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void directoryDocumentProviderInfersMarkdownDescriptors() throws Exception {
        Files.writeString(tempDir.resolve("index.md"), "# Welcome\nStart");
        Files.writeString(tempDir.resolve("language-guide.md"), "# Language Guide\nReference");

        DirectoryDocumentProvider provider = new DirectoryDocumentProvider("docs", "1", tempDir, List.of("Basic4GL"));

        assertEquals(2, provider.getIndex().size());
        DocumentDescriptor index = provider.getIndex().stream()
                .filter(descriptor -> descriptor.id().equals("index.md"))
                .findFirst()
                .orElseThrow();
        assertEquals("Welcome", index.title());
        assertTrue(index.tags().contains("learn"));

        ContentDocument document = provider.openDocument("language-guide.md");
        assertEquals("text/markdown", document.mediaType());
    }

    @Test
    void directoryTemplateProviderCopiesSelectedProgramAndSharedDirectoriesOnly() throws Exception {
        Files.writeString(tempDir.resolve("SampleOne.gb"), "one");
        Files.writeString(tempDir.resolve("SampleTwo.gb"), "two");
        Files.createDirectories(tempDir.resolve("Data"));
        Files.writeString(tempDir.resolve("Data/sprite.png"), "asset");
        Files.createDirectories(tempDir.resolve("Ignore"));
        Files.writeString(tempDir.resolve("Ignore/file.txt"), "ignored");

        DirectoryTemplateProvider provider = new DirectoryTemplateProvider("samples", "1", tempDir, List.of("Samples"));
        Path destination = tempDir.resolveSibling("out");

        provider.instantiate("SampleOne.gb", new TemplateCreationRequest(destination, "Sample One", null));

        assertEquals("one", Files.readString(destination.resolve("SampleOne.gb")));
        assertFalse(Files.exists(destination.resolve("SampleTwo.gb")));
        assertEquals("asset", Files.readString(destination.resolve("Data/sprite.png")));
        assertFalse(Files.exists(destination.resolve("Ignore/file.txt")));
    }

    @Test
    void classpathContentSourceEnumeratesFromIndex() throws Exception {
        Path classes = Files.createDirectories(tempDir.resolve("classes"));
        Files.createDirectories(classes.resolve("content/docs"));
        Files.writeString(classes.resolve("content/docs/index.md"), "# Index");
        Files.writeString(classes.resolve("content/docs.index"), "index.md\n", StandardCharsets.UTF_8);

        try (URLClassLoader classLoader =
                new URLClassLoader(new java.net.URL[] {classes.toUri().toURL()})) {
            ClasspathContentSource source =
                    new ClasspathContentSource(classLoader, "content/docs", "content/docs.index");

            assertEquals(List.of(new ContentResource("index.md")), source.resources());
            assertEquals("# Index", new String(source.open("index.md").readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
