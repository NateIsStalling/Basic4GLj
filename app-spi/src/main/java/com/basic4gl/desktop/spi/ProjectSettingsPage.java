package com.basic4gl.desktop.spi;

import javax.swing.*;

public interface ProjectSettingsPage {
    String getPageId();

    String getPageTitle();

    String getPageDescription();

    JComponent createPageComponent();

    void onApply();

    default int getSortOrder() {
        return 0;
    }
}
