package com.basic4gl.desktop.spi.content;

public interface ContentService {
    ContentRegistration registerDocumentProvider(DocumentProvider provider);

    ContentRegistration registerTemplateProvider(TemplateProvider provider);

    @Deprecated
    default void registerTemplate(Template template) {
        registerTemplateProvider(new LegacyTemplateProvider(template));
    }

    @Deprecated
    default void registerDocument(Content content) {
        registerDocumentProvider(new LegacyContentDocumentProvider(content));
    }
}
