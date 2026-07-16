package com.basic4gl.desktop.content;

import static org.junit.Assert.*;

import com.basic4gl.desktop.content.catalog.ContentGlobalId;
import com.basic4gl.desktop.content.catalog.TemplateCatalogEntry;
import com.basic4gl.desktop.spi.content.TemplateCreationRequest;
import com.basic4gl.desktop.spi.content.TemplateDescriptor;
import com.basic4gl.desktop.spi.content.TemplateProvider;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TemplateInstantiatorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void instantiatesGeneratedTemplateAndReturnsEntryPoint() throws Exception {
        GeneratedTemplateProvider provider = new GeneratedTemplateProvider();
        TemplateCatalogEntry entry = new TemplateCatalogEntry(
                new ContentGlobalId("plugin", "templates", "sample"),
                "Plugin",
                "1",
                new TemplateDescriptor(
                        "sample", "Sample", "", List.of("Graphics"), Set.of("sample"), 0, "src/main.gb", List.of()),
                provider);
        Path destination = temporaryFolder.getRoot().toPath().resolve("SpriteDemo");

        Optional<Path> entryPoint =
                new TemplateInstantiator().instantiate(entry, destination, "Sprite Demo", Map.of("message", "Hello"));

        assertTrue(entryPoint.isPresent());
        assertEquals(destination.resolve("src/main.gb").toAbsolutePath().normalize(), entryPoint.get());
        assertEquals("Hello", Files.readString(entryPoint.get()));
    }

    @Test
    public void refusesExistingDestinationBeforeCallingProvider() throws Exception {
        GeneratedTemplateProvider provider = new GeneratedTemplateProvider();
        Path destination = temporaryFolder.newFolder("Existing").toPath();
        TemplateCatalogEntry entry = new TemplateCatalogEntry(
                new ContentGlobalId("plugin", "templates", "sample"),
                "Plugin",
                "1",
                new TemplateDescriptor("sample", "Sample", "", List.of(), Set.of("sample"), 0, "main.gb", List.of()),
                provider);

        assertThrows(FileAlreadyExistsException.class, () -> new TemplateInstantiator()
                .instantiate(entry, destination, "", Map.of()));
        assertFalse(provider.called);
    }

    private static final class GeneratedTemplateProvider implements TemplateProvider {
        private boolean called;

        @Override
        public String id() {
            return "templates";
        }

        @Override
        public String version() {
            return "1";
        }

        @Override
        public Collection<TemplateDescriptor> getIndex() {
            return List.of();
        }

        @Override
        public void instantiate(String templateId, TemplateCreationRequest request) throws IOException {
            called = true;
            Path entryPoint = request.destination().resolve("src/main.gb");
            Files.createDirectories(entryPoint.getParent());
            Files.writeString(entryPoint, request.variables().getOrDefault("message", ""));
        }
    }
}
