package com.basic4gl.desktop.spi.content;

public record ContentResource(String path) {

    public ContentResource {
        path = ContentValidation.requireNonBlank(path, "path");
    }
}
