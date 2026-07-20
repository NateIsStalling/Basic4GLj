package com.basic4gl.desktop.spi.content;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ContentPathsTest {

    @Test
    void normalizesSeparatorsAndDotSegments() {
        assertEquals("tutorials/images/sprite.png", ContentPaths.normalize("tutorials\\./images//sprite.png"));
        assertEquals("images/sprite.png", ContentPaths.normalize("tutorials/../images/sprite.png"));
    }

    @Test
    void resolvesRelativeTargetsAgainstCurrentDocumentParent() {
        assertEquals(
                "docs/images/sprite.png", ContentPaths.resolve("docs/tutorials/sprites.md", "../images/sprite.png"));
        assertEquals("docs/tutorials/page2.md", ContentPaths.resolve("docs/tutorials/sprites.md", "page2.md"));
    }

    @Test
    void rejectsEscapesAbsolutePathsAndUriSchemes() {
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize("../../outside.png"));
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize("/absolute/path"));
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize("C:\\absolute\\path"));
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize("file:///tmp/index.md"));
        assertThrows(
                IllegalArgumentException.class, () -> ContentPaths.normalize("jar:file:///tmp/docs.jar!/index.md"));
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize("https://example.invalid/index.md"));
    }

    @Test
    void rejectsBlankAndRootOnlyPaths() {
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize(null));
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize(" "));
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize("."));
        assertThrows(IllegalArgumentException.class, () -> ContentPaths.normalize("a/.."));
    }
}
