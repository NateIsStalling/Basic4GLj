package com.basic4gl.desktop.panels;

import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;

import javax.swing.*;

import static com.basic4gl.desktop.Theme.ICON_MENU_DEBUG;

public class DebugPanelProvider implements IEditorPanelProvider {
    @Override
    public String id() {
        return "debug";
    }

    @Override
    public String getTitle() {
        return "Debug";
    }

    @Override
    public String getIconPath() {
        return ICON_MENU_DEBUG;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.SOUTH;
    }

    @Override
    public JPanel build(PluginContext context) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//
//        JButton toggleDebug = new JButton("Toggle debug mode");
//        toggleDebug.addActionListener(e -> actionDebugMode());
        JButton playPause = new JButton("Play/Pause");
        playPause.addActionListener(e -> context.debugger().actionPlayPause());
        JButton stepOver = new JButton("Step over");
        stepOver.addActionListener(e -> context.debugger().actionStep());
        JButton stepInto = new JButton("Step into");
        stepInto.addActionListener(e -> context.debugger().actionStepInto());
        JButton stepOut = new JButton("Step out");
        stepOut.addActionListener(e -> context.debugger().actionStepOutOf());

//        panel.add(toggleDebug);
        panel.add(playPause);
        panel.add(stepOver);
        panel.add(stepInto);
        panel.add(stepOut);
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
