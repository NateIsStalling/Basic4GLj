package com.basic4gl.desktop.content.catalog;

import com.basic4gl.desktop.spi.content.ContentRegistration;
import com.basic4gl.desktop.spi.content.DocumentDescriptor;
import com.basic4gl.desktop.spi.content.DocumentProvider;
import com.basic4gl.desktop.spi.content.TemplateDescriptor;
import com.basic4gl.desktop.spi.content.TemplateProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ContentCatalog {

    private final Map<String, List<DocumentCatalogEntry>> documentProviders = new LinkedHashMap<>();
    private final Map<String, List<TemplateCatalogEntry>> templateProviders = new LinkedHashMap<>();
    private final Set<String> providerKeys = new HashSet<>();
    private final List<ContentCatalogListener> listeners = new ArrayList<>();

    public synchronized ContentRegistration registerDocumentProvider(
            String pluginId, String pluginDisplayName, DocumentProvider provider) {
        Objects.requireNonNull(provider, "provider");
        String providerId = requireNonBlank(provider.id(), "provider.id");
        String providerVersion = requireNonBlank(provider.version(), "provider.version");
        String providerKey = providerKey(pluginId, providerId);
        rejectDuplicateProvider(providerKey);

        Collection<DocumentDescriptor> descriptors = requireIndex(provider.getIndex(), "document index");
        List<DocumentCatalogEntry> entries = new ArrayList<>();
        Set<String> itemIds = new HashSet<>();
        for (DocumentDescriptor descriptor : descriptors) {
            Objects.requireNonNull(descriptor, "document descriptor");
            if (!itemIds.add(descriptor.id())) {
                throw new IllegalArgumentException(
                        "Duplicate document ID in provider " + providerId + ": " + descriptor.id());
            }
            entries.add(new DocumentCatalogEntry(
                    new ContentGlobalId(pluginId, providerId, descriptor.id()),
                    pluginDisplayName,
                    providerVersion,
                    descriptor,
                    provider));
        }

        providerKeys.add(providerKey);
        documentProviders.put(providerKey, List.copyOf(entries));
        warnForMissingDocumentRelationships(pluginId, entries);
        notifyListeners();
        return new Registration(providerKey);
    }

    public synchronized ContentRegistration registerTemplateProvider(
            String pluginId, String pluginDisplayName, TemplateProvider provider) {
        Objects.requireNonNull(provider, "provider");
        String providerId = requireNonBlank(provider.id(), "provider.id");
        String providerVersion = requireNonBlank(provider.version(), "provider.version");
        String providerKey = providerKey(pluginId, providerId);
        rejectDuplicateProvider(providerKey);

        Collection<TemplateDescriptor> descriptors = requireIndex(provider.getIndex(), "template index");
        List<TemplateCatalogEntry> entries = new ArrayList<>();
        Set<String> itemIds = new HashSet<>();
        for (TemplateDescriptor descriptor : descriptors) {
            Objects.requireNonNull(descriptor, "template descriptor");
            if (!itemIds.add(descriptor.id())) {
                throw new IllegalArgumentException(
                        "Duplicate template ID in provider " + providerId + ": " + descriptor.id());
            }
            entries.add(new TemplateCatalogEntry(
                    new ContentGlobalId(pluginId, providerId, descriptor.id()),
                    pluginDisplayName,
                    providerVersion,
                    descriptor,
                    provider));
        }

        providerKeys.add(providerKey);
        templateProviders.put(providerKey, List.copyOf(entries));
        warnForMissingTemplateRelationships(pluginId, entries);
        notifyListeners();
        return new Registration(providerKey);
    }

    public synchronized List<DocumentCatalogEntry> documents() {
        return documentProviders.values().stream().flatMap(List::stream).toList();
    }

    public synchronized List<TemplateCatalogEntry> templates() {
        return templateProviders.values().stream().flatMap(List::stream).toList();
    }

    public synchronized void addListener(ContentCatalogListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public synchronized void removeListener(ContentCatalogListener listener) {
        listeners.remove(listener);
    }

    public void unregisterPlugin(String pluginId) {
        requireNonBlank(pluginId, "pluginId");
        boolean removed;
        synchronized (this) {
            removed = providerKeys.removeIf(providerKey -> providerKey.startsWith(pluginId + ":"));
            documentProviders.keySet().removeIf(providerKey -> providerKey.startsWith(pluginId + ":"));
            templateProviders.keySet().removeIf(providerKey -> providerKey.startsWith(pluginId + ":"));
        }
        if (removed) {
            notifyListeners();
        }
    }

    private void unregisterProvider(String providerKey) {
        synchronized (this) {
            if (!providerKeys.remove(providerKey)) {
                return;
            }
            documentProviders.remove(providerKey);
            templateProviders.remove(providerKey);
        }
        notifyListeners();
    }

    private void rejectDuplicateProvider(String providerKey) {
        if (providerKeys.contains(providerKey)) {
            throw new IllegalArgumentException("Duplicate content provider ID for plugin: " + providerKey);
        }
    }

    private static <T> Collection<T> requireIndex(Collection<T> index, String description) {
        if (index == null) {
            throw new IllegalArgumentException(description + " must not be null");
        }
        return index;
    }

    private static String providerKey(String pluginId, String providerId) {
        return requireNonBlank(pluginId, "pluginId") + ":" + providerId;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
        return value;
    }

    private void warnForMissingDocumentRelationships(String pluginId, List<DocumentCatalogEntry> entries) {
        Set<String> templateIds = templateIds(pluginId);
        for (DocumentCatalogEntry entry : entries) {
            for (String templateId : entry.descriptor().relatedTemplateIds()) {
                if (!templateIds.contains(templateId)) {
                    System.err.println("Missing related template '" + templateId + "' for document '"
                            + entry.descriptor().id() + "'");
                }
            }
        }
    }

    private void warnForMissingTemplateRelationships(String pluginId, List<TemplateCatalogEntry> entries) {
        Set<String> documentIds = documentIds(pluginId);
        for (TemplateCatalogEntry entry : entries) {
            for (String documentId : entry.descriptor().relatedDocumentIds()) {
                if (!documentIds.contains(documentId)) {
                    System.err.println("Missing related document '" + documentId + "' for template '"
                            + entry.descriptor().id() + "'");
                }
            }
        }
    }

    private Set<String> documentIds(String pluginId) {
        Set<String> ids = new HashSet<>();
        for (DocumentCatalogEntry entry : documents()) {
            if (entry.globalId().pluginId().equals(pluginId)) {
                ids.add(entry.descriptor().id());
            }
        }
        return ids;
    }

    private Set<String> templateIds(String pluginId) {
        Set<String> ids = new HashSet<>();
        for (TemplateCatalogEntry entry : templates()) {
            if (entry.globalId().pluginId().equals(pluginId)) {
                ids.add(entry.descriptor().id());
            }
        }
        return ids;
    }

    private void notifyListeners() {
        List<ContentCatalogListener> listenerSnapshot;
        synchronized (this) {
            listenerSnapshot = List.copyOf(listeners);
        }
        for (ContentCatalogListener listener : listenerSnapshot) {
            listener.contentCatalogChanged();
        }
    }

    private final class Registration implements ContentRegistration {
        private final String providerKey;
        private boolean closed;

        private Registration(String providerKey) {
            this.providerKey = providerKey;
        }

        @Override
        public synchronized void close() {
            if (closed) {
                return;
            }
            closed = true;
            unregisterProvider(providerKey);
        }
    }
}
