package com.basic4gl.desktop.spi.content;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ContentDescriptorTest {

    @Test
    void documentDescriptorRejectsBlankIdsAndTitles() {
        assertThrows(
                IllegalArgumentException.class, () -> new DocumentDescriptor("", "Title", null, null, null, 0, null));
        assertThrows(
                IllegalArgumentException.class, () -> new DocumentDescriptor("doc", " ", null, null, null, 0, null));
    }

    @Test
    void documentDescriptorUsesEmptyDefaultsAndDefensiveCopies() {
        List<String> categoryPath = new ArrayList<>(List.of("Graphics"));
        Set<String> tags = new HashSet<>(Set.of("learn"));
        List<String> relatedTemplateIds = new ArrayList<>(List.of("sprite-demo"));

        DocumentDescriptor descriptor =
                new DocumentDescriptor("sprites", "Sprites", null, categoryPath, tags, 5, relatedTemplateIds);

        categoryPath.add("Mutated");
        tags.add("mutated");
        relatedTemplateIds.add("mutated");

        assertEquals("", descriptor.description());
        assertEquals(List.of("Graphics"), descriptor.categoryPath());
        assertEquals(Set.of("learn"), descriptor.tags());
        assertEquals(List.of("sprite-demo"), descriptor.relatedTemplateIds());
        assertThrows(
                UnsupportedOperationException.class,
                () -> descriptor.categoryPath().add("Nope"));
    }

    @Test
    void templateDescriptorRejectsBlankIdsAndUsesEmptyDefaults() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TemplateDescriptor(" ", "Template", null, null, null, 0, null, null));

        TemplateDescriptor descriptor = new TemplateDescriptor("starter", "Starter", null, null, null, 0, null, null);

        assertEquals("", descriptor.description());
        assertEquals("", descriptor.entryPoint());
        assertEquals(List.of(), descriptor.categoryPath());
        assertEquals(Set.of(), descriptor.tags());
        assertEquals(List.of(), descriptor.relatedDocumentIds());
    }

    @Test
    void contentDocumentRequiresMediaTypeSourceAndEntryPath() {
        ContentSource source = new MapLikeTestSource();

        assertThrows(IllegalArgumentException.class, () -> new ContentDocument("", source, "index.md"));
        assertThrows(NullPointerException.class, () -> new ContentDocument("text/markdown", null, "index.md"));
        assertThrows(IllegalArgumentException.class, () -> new ContentDocument("text/markdown", source, " "));
    }

    @Test
    void templateCreationRequestCopiesVariablesAndDefaultsProjectName() {
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Sprite Demo");

        TemplateCreationRequest request = new TemplateCreationRequest(Path.of("out"), null, variables);
        variables.put("name", "Changed");

        assertEquals("", request.projectName());
        assertEquals(Map.of("name", "Sprite Demo"), request.variables());
        assertThrows(
                UnsupportedOperationException.class, () -> request.variables().put("other", "value"));
    }

    private static final class MapLikeTestSource implements ContentSource {
        @Override
        public java.io.InputStream open(String normalizedPath) {
            return new java.io.ByteArrayInputStream(new byte[0]);
        }

        @Override
        public java.util.Collection<ContentResource> resources() {
            return List.of();
        }
    }
}
