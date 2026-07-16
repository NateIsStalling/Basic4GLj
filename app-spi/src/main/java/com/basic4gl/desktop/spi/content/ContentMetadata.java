package com.basic4gl.desktop.spi.content;

public class ContentMetadata {
    private String category;
    private String description;
    private String[] tags;
    private String author;
    private String version;

    public ContentMetadata() {
        tags = new String[0];
    }

    public ContentMetadata(String category, String description, String[] tags, String author, String version) {
        this.category = category;
        this.description = description;
        this.tags = tags;
        this.author = author;
        this.version = version;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
