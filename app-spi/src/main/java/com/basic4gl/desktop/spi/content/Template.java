package com.basic4gl.desktop.spi.content;

public class Template {

    private String name;
    private ContentMetadata metadata;
    private Content[] content;

    public Template(String name, ContentMetadata metadata, Content[] content) {
        this.name = name;
        this.metadata = metadata != null ? metadata : new ContentMetadata();
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContentMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ContentMetadata metadata) {
        this.metadata = metadata;
    }

    public Content[] getContent() {
        return content;
    }

    public void setContent(Content[] content) {
        this.content = content;
    }

    public String getCategory() {
        return metadata.getCategory();
    }
}
