package com.basic4gl.desktop.content.render;

import com.basic4gl.desktop.spi.content.ContentDocument;
import java.nio.file.Path;
import java.util.Objects;

public record ContentRenderRequest(
        ContentDocument document,
        Path materializedRoot,
        String currentPath,
        ContentNavigationHandler navigationHandler) {

    public ContentRenderRequest {
        document = Objects.requireNonNull(document, "document");
        materializedRoot = Objects.requireNonNull(materializedRoot, "materializedRoot")
                .toAbsolutePath()
                .normalize();
        currentPath = currentPath == null || currentPath.isBlank() ? document.entryPath() : currentPath;
        navigationHandler = navigationHandler == null ? ContentNavigationHandler.NO_OP : navigationHandler;
    }
}
