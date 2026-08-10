package com.basic4gl.desktop.content;

import com.basic4gl.desktop.content.catalog.TemplateCatalogEntry;
import com.basic4gl.desktop.spi.content.ContentPaths;
import com.basic4gl.desktop.spi.content.TemplateCreationRequest;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TemplateInstantiator {

    public Optional<Path> instantiate(
            TemplateCatalogEntry entry, Path destination, String projectName, Map<String, String> variables)
            throws IOException {
        Objects.requireNonNull(entry, "entry");
        Path safeDestination = Objects.requireNonNull(destination, "destination")
                .toAbsolutePath()
                .normalize();
        if (Files.exists(safeDestination)) {
            throw new FileAlreadyExistsException(safeDestination.toString());
        }

        entry.provider()
                .instantiate(
                        entry.descriptor().id(), new TemplateCreationRequest(safeDestination, projectName, variables));

        String entryPoint = entry.descriptor().entryPoint();
        if (entryPoint == null || entryPoint.isBlank()) {
            return Optional.empty();
        }
        String normalizedEntryPoint = ContentPaths.normalize(entryPoint);
        Path entryPointPath = safeDestination
                .resolve(normalizedEntryPoint.replace(
                        "/", safeDestination.getFileSystem().getSeparator()))
                .normalize();
        if (!entryPointPath.startsWith(safeDestination)) {
            throw new IOException("Template entry point escapes destination: " + entryPoint);
        }
        return Optional.of(entryPointPath);
    }
}
