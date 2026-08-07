package com.basic4gl.desktop.spi.content;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public record TemplateCreationRequest(Path destination, String projectName, Map<String, String> variables) {

    public TemplateCreationRequest {
        destination = Objects.requireNonNull(destination, "destination");
        projectName = ContentValidation.optionalString(projectName);
        variables = variables == null ? Map.of() : Map.copyOf(variables);
    }
}
