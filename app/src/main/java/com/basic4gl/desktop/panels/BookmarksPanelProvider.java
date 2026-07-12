package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;

import javax.swing.*;

import static com.basic4gl.desktop.Theme.ICON_MENU_BOOKMARKS;

public class BookmarksPanelProvider implements IEditorPanelProvider {
    @Override
    public String id() {
        return "bookmarks";
    }

    @Override
    public String getTitle() {
        return "Bookmarks";
    }

    @Override
    public String getIconPath() {
        return ICON_MENU_BOOKMARKS;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.WEST;
    }

    @Override
    public JPanel build(PluginContext context) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        JButton next = new JButton("Next bookmark");
        next.addActionListener(e -> context.commands().selectNextBookmark());
        JButton previous = new JButton("Previous bookmark");
        previous.addActionListener(e -> context.commands().selectPreviousBookmark());
        JButton toggle = new JButton("Toggle bookmark");
        toggle.addActionListener(e -> context.commands().toggleBookmark());

        panel.add(next);
        panel.add(previous);
        panel.add(toggle);
        return panel;
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
