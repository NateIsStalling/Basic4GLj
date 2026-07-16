package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;
import java.awt.*;
import javax.swing.*;

public interface IEditorPanelProvider {
    String id();

    String getTitle();

    String getActiveIconPath();

    String getInactiveIconPath();

    Color getActiveIconTint();

    EditorLayout getLayoutConstraints();

    JPanel build(PluginContext context);

    void refresh(EditorPlugin languageProvider);

    void onFileModified(String filePath);

    void dispose();
    //
    //    void onTabClosed();
    // TODO cleanup hooks; this should be a separate listener that can be registered with PluginContext from build()
    void onCompileSucceeded();
    //

}
