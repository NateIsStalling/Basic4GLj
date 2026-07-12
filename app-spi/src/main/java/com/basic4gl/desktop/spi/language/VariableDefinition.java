package com.basic4gl.desktop.spi.language;

public record VariableDefinition(
        String name,
        String signature,
        TypeDefinition type,
        String value,
        String description,
        String packageName,
        boolean readOnly,
        String scope,
        String source) {}
