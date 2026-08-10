package com.basic4gl.desktop.spi;

import javax.swing.*;

/**
 * Extension point for contributing custom tabs to the project export dialog.
 */
public interface ProjectExportPage {
    String getPageId();

    String getPageTitle();

    String getPageDescription();

    JComponent createPageComponent();

    default void onBuilderSelected(Builder builder) {}

    default void onExport(Builder builder) {}

    default int getSortOrder() {
        return 0;
    }
}
