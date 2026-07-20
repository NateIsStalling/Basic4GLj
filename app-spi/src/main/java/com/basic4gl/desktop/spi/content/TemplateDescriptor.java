package com.basic4gl.desktop.spi.content;

import java.util.List;
import java.util.Set;

public record TemplateDescriptor(
        String id,
        String title,
        String description,
        List<String> categoryPath,
        Set<String> tags,
        int sortOrder,
        String entryPoint,
        List<String> relatedDocumentIds)
        implements IndexedContent {

    public TemplateDescriptor {
        id = ContentValidation.requireNonBlank(id, "id");
        title = ContentValidation.requireNonBlank(title, "title");
        description = ContentValidation.optionalString(description);
        categoryPath = ContentValidation.copyList(categoryPath);
        tags = ContentValidation.copySet(tags);
        entryPoint = ContentValidation.optionalString(entryPoint);
        relatedDocumentIds = ContentValidation.copyList(relatedDocumentIds);
    }
}
