package com.basic4gl.language.adapter;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.basic4gl.desktop.spi.SourceFileService;
import org.junit.jupiter.api.Test;

class FileServiceAdapterTest {

    @Test
    void openSourceFile_whenServiceReturnsNull_returnsNull() {
        SourceFileService sourceFileService = filename -> null;
        FileServiceAdapter adapter = new FileServiceAdapter(sourceFileService);

        assertNull(adapter.openSourceFile("missing.bas"));
    }
}
