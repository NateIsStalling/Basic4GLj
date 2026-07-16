package com.basic4gl.desktop.spi.content;

import java.util.List;
import java.util.Set;

public interface IndexedContent {

    /**
     * Stable and unique within the provider.
     */
    String id();

    String title();

    String description();

    /**
     * Logical UI hierarchy, not necessarily filesystem directories.
     */
    List<String> categoryPath();

    Set<String> tags();

    int sortOrder();
}
