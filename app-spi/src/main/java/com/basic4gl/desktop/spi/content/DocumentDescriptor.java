package com.basic4gl.desktop.spi.content;

import java.util.List;
import java.util.Set;

public record DocumentDescriptor(
        String id,
        String title,
        String description,
        List<String> categoryPath,
        Set<String> tags,
        int sortOrder,
        List<String> relatedTemplateIds)
        implements IndexedContent {

    public DocumentDescriptor {
        id = ContentValidation.requireNonBlank(id, "id");
        title = ContentValidation.requireNonBlank(title, "title");
        description = ContentValidation.optionalString(description);
        categoryPath = ContentValidation.copyList(categoryPath);
        tags = ContentValidation.copySet(tags);
        relatedTemplateIds = ContentValidation.copyList(relatedTemplateIds);
    }
}
