package com.basic4gl.desktop.spi.content;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

public final class ContentPaths {

    private ContentPaths() {}

    public static String normalize(String path) {
        rejectUnsafePath(path);
        String normalizedSeparators = path.replace('\\', '/');
        Deque<String> segments = new ArrayDeque<>();
        for (String segment : normalizedSeparators.split("/+")) {
            if (segment.isEmpty() || ".".equals(segment)) {
                continue;
            }
            if ("..".equals(segment)) {
                if (segments.isEmpty()) {
                    throw new IllegalArgumentException("Path escapes the content root: " + path);
                }
                segments.removeLast();
                continue;
            }
            segments.addLast(segment);
        }
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Path must not resolve to the content root");
        }
        return String.join("/", segments);
    }

    public static String resolve(String currentDocumentPath, String relativeTarget) {
        String currentPath = normalize(currentDocumentPath);
        rejectUnsafePath(relativeTarget);
        String parent = "";
        int lastSlash = currentPath.lastIndexOf('/');
        if (lastSlash >= 0) {
            parent = currentPath.substring(0, lastSlash);
        }
        String combined = parent.isBlank() ? relativeTarget : parent + "/" + relativeTarget;
        return normalize(combined);
    }

    private static void rejectUnsafePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path must not be null or blank");
        }
        String trimmed = path.trim();
        String slashPath = trimmed.replace('\\', '/');
        if (slashPath.startsWith("/") || slashPath.startsWith("//")) {
            throw new IllegalArgumentException("Absolute paths are not allowed: " + path);
        }
        if (slashPath.matches("^[A-Za-z]:($|/.*)")) {
            throw new IllegalArgumentException("Windows absolute paths are not allowed: " + path);
        }
        String lower = slashPath.toLowerCase(Locale.ROOT);
        if (lower.matches("^[a-z][a-z0-9+.-]*:.*")) {
            throw new IllegalArgumentException("URI schemes are not allowed: " + path);
        }
    }
}
