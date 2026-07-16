package com.basic4gl.desktop.content.catalog;

import static org.junit.Assert.*;

import com.basic4gl.desktop.spi.content.DocumentDescriptor;
import com.basic4gl.desktop.spi.content.DocumentProvider;
import com.basic4gl.desktop.spi.content.TemplateCreationRequest;
import com.basic4gl.desktop.spi.content.TemplateDescriptor;
import com.basic4gl.desktop.spi.content.TemplateProvider;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ContentPanelModelTest {

    @Test
    public void scopesUseTagsInsteadOfKindEnums() {
        ContentCatalog catalog = catalog();
        ContentPanelModel model = new ContentPanelModel();

        assertEquals(List.of("Drawing Sprites", "Sprite Demo"), titles(model.items(catalog, ContentScope.LEARN, "")));
        assertEquals(List.of("Library Reference"), titles(model.items(catalog, ContentScope.REFERENCE, "")));
        assertEquals(List.of("Sprite Demo"), titles(model.items(catalog, ContentScope.SAMPLES, "")));
        assertEquals(
                List.of("Blank Program", "Drawing Sprites", "Library Reference", "Sprite Demo"),
                titles(model.items(catalog, ContentScope.ALL, "")));
    }

    @Test
    public void browseBuildsCategoryHierarchy() {
        ContentBrowseNode root = new ContentPanelModel().browse(catalog(), ContentScope.LEARN);

        assertEquals("Learn", root.name());
        assertEquals(1, root.children().size());
        ContentBrowseNode graphics = root.children().get(0);
        assertEquals("Graphics", graphics.name());
        assertEquals(List.of("Drawing Sprites", "Sprite Demo"), titles(graphics.items()));
    }

    @Test
    public void searchReturnsFlatRankedResultsWithinScope() {
        List<ContentPanelItem> results = new ContentPanelModel().items(catalog(), ContentScope.ALL, "sprite");

        assertEquals(List.of("Sprite Demo", "Drawing Sprites"), titles(results));
    }

    @Test
    public void summaryUsesTagDrivenLabelsAndActions() {
        ContentPanelModel model = new ContentPanelModel();
        ContentPanelItem sample =
                model.items(catalog(), ContentScope.SAMPLES, "").get(0);

        ContentSelectionSummary summary = model.summary(sample);

        assertEquals("Sprite Demo", summary.title());
        assertEquals("Sample", summary.kindLabel());
        assertEquals("Open Sample", summary.primaryAction());
        assertEquals("Graphics", summary.category());
        assertEquals(List.of("drawing-sprites"), summary.relatedIds());
    }

    private static ContentCatalog catalog() {
        ContentCatalog catalog = new ContentCatalog();
        catalog.registerDocumentProvider(
                "plugin",
                "Plugin",
                new Docs(List.of(
                        new DocumentDescriptor(
                                "drawing-sprites",
                                "Drawing Sprites",
                                "Learn graphics",
                                List.of("Graphics"),
                                Set.of("learn", "tutorial"),
                                0,
                                List.of("sprite-demo")),
                        new DocumentDescriptor(
                                "reference",
                                "Library Reference",
                                "API docs",
                                List.of("Reference"),
                                Set.of("reference"),
                                0,
                                List.of()))));
        catalog.registerTemplateProvider(
                "plugin",
                "Plugin",
                new Templates(List.of(
                        new TemplateDescriptor(
                                "blank",
                                "Blank Program",
                                "Starter",
                                List.of("Basics"),
                                Set.of("starter"),
                                0,
                                "main.gb",
                                List.of()),
                        new TemplateDescriptor(
                                "sprite-demo",
                                "Sprite Demo",
                                "Sample",
                                List.of("Graphics"),
                                Set.of("sample"),
                                1,
                                "main.gb",
                                List.of("drawing-sprites")))));
        return catalog;
    }

    private static List<String> titles(List<ContentPanelItem> items) {
        return items.stream().map(ContentPanelItem::title).toList();
    }

    private record Docs(Collection<DocumentDescriptor> descriptors) implements DocumentProvider {
        @Override
        public String id() {
            return "docs";
        }

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

    private record Templates(Collection<TemplateDescriptor> descriptors) implements TemplateProvider {
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
            return descriptors;
        }

        @Override
        public void instantiate(String templateId, TemplateCreationRequest request) {}
    }
}
