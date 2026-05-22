package com.basic4gl.desktop.spi.language;

public record FunctionDefinition (
        String name,
        String signature,
        VariableDefinition type,
        VariableDefinition[] parameters,
        String description,
        String packageName,
        boolean hasBrackets){
}
