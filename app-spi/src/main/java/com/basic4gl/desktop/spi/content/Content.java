package com.basic4gl.desktop.spi.content;

import java.io.File;

public class Content {
    private String name;
    private ContentMetadata metadata;

    private File file;
    private boolean readonly;

    public Content(String name, ContentMetadata metadata, File file, boolean readonly) {
        this.name = name;
        this.metadata = metadata != null ? metadata : new ContentMetadata();
        this.file = file;
        this.readonly = readonly;
    }

    public String getName() {
        return name;
    }

    public ContentMetadata getMetadata() {
        return metadata;
    }

    public File getFile() {
        return file;
    }

    public boolean isReadonly() {
        return readonly;
    }
}
