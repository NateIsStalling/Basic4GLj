package com.basic4gl.desktop.spi.content;

import java.nio.file.Path;

public final class JarContentSource extends ZipContentSource {

    public JarContentSource(Path jarPath) {
        super(jarPath);
    }

    public JarContentSource(Path jarPath, String rootPrefix) {
        super(jarPath, rootPrefix);
    }
}
