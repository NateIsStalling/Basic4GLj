package com.basic4gl.desktop.spi.content;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DirectoryTemplateProvider implements TemplateProvider {

    private static final Set<String> SHARED_DIRECTORIES = Set.of("include", "data", "files", "sounds", "textures");

    private final String id;
    private final String version;
    private final ContentSource source;
    private final List<String> rootCategory;
    private final List<TemplateDescriptor> index;

    public DirectoryTemplateProvider(String id, String version, Path root, List<String> rootCategory)
            throws IOException {
        this(id, version, new DirectoryContentSource(root), rootCategory);
    }

    public DirectoryTemplateProvider(String id, String version, ContentSource source, List<String> rootCategory)
            throws IOException {
        this.id = ContentValidation.requireNonBlank(id, "id");
        this.version = ContentValidation.requireNonBlank(version, "version");
        this.source = source;
        this.rootCategory = ContentValidation.copyList(rootCategory);
        this.index = buildIndex();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public Collection<TemplateDescriptor> getIndex() {
        return index;
    }

    @Override
    public void instantiate(String templateId, TemplateCreationRequest request) throws IOException {
        TemplateDescriptor descriptor = find(templateId);
        Files.createDirectories(request.destination());
        copyResource(descriptor.entryPoint(), request.destination());
        for (ContentResource resource : source.resources()) {
            if (DirectoryDocumentProvider.hidden(resource.path())) {
                continue;
            }
            if (isSharedResource(resource.path())) {
                copyResource(resource.path(), request.destination());
            }
        }
    }

    protected List<TemplateDescriptor> buildIndex() throws IOException {
        List<TemplateDescriptor> descriptors = new ArrayList<>();
        int sortOrder = 0;
        for (ContentResource resource : source.resources()) {
            String path = resource.path();
            if (DirectoryDocumentProvider.hidden(path)
                    || path.contains("/")
                    || !path.toLowerCase(Locale.ROOT).endsWith(".gb")) {
                continue;
            }
            descriptors.add(new TemplateDescriptor(
                    path,
                    DirectoryDocumentProvider.splitTitle(path.replaceFirst("\\.[^.]+$", "")),
                    "",
                    rootCategory.isEmpty() ? List.of("Samples") : rootCategory,
                    Set.of("sample"),
                    sortOrder++,
                    path,
                    List.of()));
        }
        return List.copyOf(descriptors);
    }

    private TemplateDescriptor find(String templateId) throws IOException {
        for (TemplateDescriptor descriptor : index) {
            if (descriptor.id().equals(templateId)) {
                return descriptor;
            }
        }
        throw new IOException("Unknown template: " + templateId);
    }

    private void copyResource(String path, Path destination) throws IOException {
        String safePath = ContentPaths.normalize(path);
        Path target = destination
                .resolve(safePath.replace("/", destination.getFileSystem().getSeparator()))
                .normalize();
        if (!target.startsWith(destination)) {
            throw new IOException("Template resource escapes destination: " + path);
        }
        Files.createDirectories(target.getParent());
        try (InputStream input = source.open(safePath)) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private boolean isSharedResource(String path) {
        int slash = path.indexOf('/');
        if (slash <= 0) {
            return false;
        }
        return SHARED_DIRECTORIES.contains(path.substring(0, slash).toLowerCase(Locale.ROOT));
    }
}
