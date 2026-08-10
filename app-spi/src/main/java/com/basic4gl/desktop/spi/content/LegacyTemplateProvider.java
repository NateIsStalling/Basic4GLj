package com.basic4gl.desktop.spi.content;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Deprecated
public final class LegacyTemplateProvider implements TemplateProvider {

    private final Template template;
    private final TemplateDescriptor descriptor;

    public LegacyTemplateProvider(Template template) {
        this.template = template;
        ContentMetadata metadata = template.getMetadata();
        Content[] content = template.getContent() == null ? new Content[0] : template.getContent();
        String entryPoint = content.length == 0 || content[0].getFile() == null
                ? ""
                : content[0].getFile().getName();
        this.descriptor = new TemplateDescriptor(
                template.getName(),
                template.getName(),
                metadata.getDescription(),
                List.of(metadata.getCategory() == null ? "Legacy" : metadata.getCategory()),
                Set.of(metadata.getTags() == null ? new String[0] : metadata.getTags()),
                0,
                entryPoint,
                List.of());
    }

    @Override
    public String id() {
        return "legacy-template-" + descriptor.id();
    }

    @Override
    public String version() {
        return template.getMetadata().getVersion() == null
                ? "legacy"
                : template.getMetadata().getVersion();
    }

    @Override
    public Collection<TemplateDescriptor> getIndex() {
        return List.of(descriptor);
    }

    @Override
    public void instantiate(String templateId, TemplateCreationRequest request) throws IOException {
        if (!descriptor.id().equals(templateId)) {
            throw new IOException("Unknown legacy template: " + templateId);
        }
        Files.createDirectories(request.destination());
        Content[] content = template.getContent() == null ? new Content[0] : template.getContent();
        for (Content item : content) {
            if (item.getFile() == null || !item.getFile().isFile()) {
                continue;
            }
            Path target =
                    request.destination().resolve(item.getFile().getName()).normalize();
            if (!target.startsWith(request.destination())) {
                throw new IOException("Legacy template resource escapes destination: "
                        + item.getFile().getName());
            }
            try (InputStream input = Files.newInputStream(item.getFile().toPath())) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
