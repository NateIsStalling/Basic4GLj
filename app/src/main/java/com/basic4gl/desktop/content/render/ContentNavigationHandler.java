package com.basic4gl.desktop.content.render;

import java.net.URI;

public interface ContentNavigationHandler {

    ContentNavigationHandler NO_OP = new ContentNavigationHandler() {
        @Override
        public void navigateTo(String normalizedPath) {}

        @Override
        public void openExternal(URI uri) {}
    };

    void navigateTo(String normalizedPath);

    void openExternal(URI uri);
}
