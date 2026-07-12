package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;

import javax.swing.*;

import static com.basic4gl.desktop.Theme.ICON_MENU_HELP;

public class DocsPanelProvider implements IEditorPanelProvider {
    @Override
    public String id() {
        return "docs";
    }

    @Override
    public String getTitle() {
        return "Documentation";
    }

    @Override
    public String getIconPath() {
        return ICON_MENU_HELP;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.WEST;
    }

    @Override
    public JPanel build(PluginContext context) {
        return null;
    }

    @Override
    public void refresh(EditorPlugin languageProvider) {

    }

    @Override
    public void onFileModified(String filePath) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void onCompileSucceeded() {

    }
}
