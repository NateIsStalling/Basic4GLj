package com.basic4gl.desktop.content.catalog;

import static org.junit.Assert.*;

import com.basic4gl.desktop.spi.content.ContentRegistration;
import com.basic4gl.desktop.spi.content.DocumentDescriptor;
import com.basic4gl.desktop.spi.content.DocumentProvider;
import com.basic4gl.desktop.spi.content.TemplateCreationRequest;
import com.basic4gl.desktop.spi.content.TemplateDescriptor;
import com.basic4gl.desktop.spi.content.TemplateProvider;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class ContentCatalogTest {

    @Test
    public void registersAndRemovesDocumentProviders() {
        ContentCatalog catalog = new ContentCatalog();
        AtomicInteger changes = new AtomicInteger();
        catalog.addListener(changes::incrementAndGet);

        ContentRegistration registration = catalog.registerDocumentProvider(
                "plugin", "Plugin", new TestDocumentProvider("docs", List.of(document("sprites", "Sprites"))));

        assertEquals(1, catalog.documents().size());
        assertEquals(
                "plugin:docs:sprites", catalog.documents().get(0).globalId().value());
        assertEquals(1, changes.get());

        registration.close();
        registration.close();

        assertTrue(catalog.documents().isEmpty());
        assertEquals(2, changes.get());
    }

    @Test
    public void rejectsDuplicateProviderIdsForSamePluginAcrossProviderTypes() {
        ContentCatalog catalog = new ContentCatalog();
        catalog.registerDocumentProvider(
                "plugin", "Plugin", new TestDocumentProvider("content", List.of(document("doc", "Doc"))));

        assertThrows(
                IllegalArgumentException.class,
                () -> catalog.registerTemplateProvider(
                        "plugin",
                        "Plugin",
                        new TestTemplateProvider("content", List.of(template("sample", "Sample")))));
    }

    @Test
    public void duplicateItemIdsRejectProviderAtomically() {
        ContentCatalog catalog = new ContentCatalog();

        assertThrows(
                IllegalArgumentException.class,
                () -> catalog.registerDocumentProvider(
                        "plugin",
                        "Plugin",
                        new TestDocumentProvider("docs", List.of(document("same", "One"), document("same", "Two")))));

        assertTrue(catalog.documents().isEmpty());
    }

    @Test
    public void missingRelationshipsDoNotRejectProvider() {
        ContentCatalog catalog = new ContentCatalog();

        catalog.registerDocumentProvider(
                "plugin",
                "Plugin",
                new TestDocumentProvider(
                        "docs",
                        List.of(new DocumentDescriptor(
                                "sprites",
                                "Sprites",
                                "",
                                List.of("Graphics"),
                                Set.of("learn"),
                                0,
                                List.of("missing-template")))));

        assertEquals(1, catalog.documents().size());
    }

    @Test
    public void pluginUnloadRemovesOwnedProviders() {
        ContentCatalog catalog = new ContentCatalog();
        catalog.registerDocumentProvider(
                "plugin1", "Plugin 1", new TestDocumentProvider("docs", List.of(document("doc", "Doc"))));
        catalog.registerTemplateProvider(
                "plugin1", "Plugin 1", new TestTemplateProvider("samples", List.of(template("sample", "Sample"))));
        catalog.registerDocumentProvider(
                "plugin2", "Plugin 2", new TestDocumentProvider("docs", List.of(document("other", "Other"))));

        catalog.unregisterPlugin("plugin1");

        assertEquals(1, catalog.documents().size());
        assertEquals("plugin2:docs:other", catalog.documents().get(0).globalId().value());
        assertTrue(catalog.templates().isEmpty());
    }

    @Test
    public void searchIndexesDocumentsAndTemplatesByMetadata() {
        ContentCatalog catalog = new ContentCatalog();
        catalog.registerDocumentProvider(
                "plugin",
                "Plugin",
                new TestDocumentProvider(
                        "docs",
                        List.of(new DocumentDescriptor(
                                "sprites",
                                "Drawing Sprites",
                                "Graphics tutorial",
                                List.of("Graphics"),
                                Set.of("learn"),
                                0,
                                List.of()))));
        catalog.registerTemplateProvider(
                "plugin2",
                "Samples Plugin",
                new TestTemplateProvider(
                        "samples",
                        List.of(new TemplateDescriptor(
                                "sprite-demo",
                                "Sprite Demo",
                                "Animation sample",
                                List.of("Graphics"),
                                Set.of("sample"),
                                0,
                                "main.gb",
                                List.of()))));

        List<ContentSearchResult> results =
                new ContentSearchIndex().search(catalog.documents(), catalog.templates(), "sprite");

        assertEquals(2, results.size());
        assertEquals("Sprite Demo", results.get(0).content().title());
        assertEquals("Drawing Sprites", results.get(1).content().title());
    }

    private static DocumentDescriptor document(String id, String title) {
        return new DocumentDescriptor(id, title, "", List.of(), Set.of(), 0, List.of());
    }

    private static TemplateDescriptor template(String id, String title) {
        return new TemplateDescriptor(id, title, "", List.of(), Set.of("sample"), 0, "main.gb", List.of());
    }

    private record TestDocumentProvider(String id, Collection<DocumentDescriptor> descriptors)
            implements DocumentProvider {
        @Override
        public String version() {
            return "1";
        }

        @Override
        public Collection<DocumentDescriptor> getIndex() {
            return descriptors;
        }

        @Override
        public com.basic4gl.desktop.spi.content.ContentDocument openDocument(String documentId) throws IOException {
            throw new IOException("Not used");
        }
    }

    private record TestTemplateProvider(String id, Collection<TemplateDescriptor> descriptors)
            implements TemplateProvider {
        @Override
        public String version() {
            return "1";
        }

        @Override
        public Collection<TemplateDescriptor> getIndex() {
            return descriptors;
        }

        @Override
        public void instantiate(String templateId, TemplateCreationRequest request) {}
    }
}
