package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;

import javax.swing.*;
import java.awt.*;

import static com.basic4gl.desktop.Theme.*;

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
    public String getActiveIconPath() {
        return ICON_MENU_DOCS_SOLID;
    }

    @Override
    public String getInactiveIconPath() {
        return ICON_MENU_DOCS;
    }

    @Override
    public Color getActiveIconTint() {
        return null;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.EAST;
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
