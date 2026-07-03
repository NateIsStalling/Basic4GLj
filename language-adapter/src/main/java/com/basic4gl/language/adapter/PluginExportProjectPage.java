package com.basic4gl.language.adapter;

import com.basic4gl.compiler.util.IPluginExportBuilder;
import com.basic4gl.desktop.spi.Builder;
import com.basic4gl.desktop.spi.ProjectExportPage;
import com.basic4gl.library.plugin.PluginJAR;
import com.basic4gl.library.plugin.PluginJARFile;
import com.basic4gl.library.plugin.PluginJARManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PluginExportProjectPage implements ProjectExportPage {
    private final PluginJARManager pluginManager;
    private final Supplier<String> defaultDirectorySupplier;

    private JComponent pageComponent;
    private javax.swing.DefaultListModel<String> pluginModel;
    private JList<String> pluginList;

    public PluginExportProjectPage(PluginJARManager pluginManager, Supplier<String> defaultDirectorySupplier) {
        this.pluginManager = pluginManager;
        this.defaultDirectorySupplier = defaultDirectorySupplier;
    }

    @Override
    public String getPageId() {
        return "plugins";
    }

    @Override
    public String getPageTitle() {
        return "Plugins";
    }

    @Override
    public String getPageDescription() {
        return "Bundle plugin JARs with the exported project.";
    }

    @Override
    public int getSortOrder() {
        return 300;
    }

    @Override
    public JComponent createPageComponent() {
        if (pageComponent != null) {
            return pageComponent;
        }

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JLabel description = new JLabel("Plugin JARs included in export (copied to plugins/):");
        root.add(description, BorderLayout.NORTH);

        pluginModel = new javax.swing.DefaultListModel<>();
        pluginList = new JList<>(pluginModel);
        pluginList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane pluginScrollPane = new JScrollPane(pluginList);
        pluginScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(pluginScrollPane, BorderLayout.CENTER);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        JButton addButton = new JButton("Add...");
        JButton removeButton = new JButton("Remove");
        JButton clearButton = new JButton("Clear");
        JButton addLoadedButton = new JButton("Add Loaded");

        addButton.addActionListener(e -> promptAndAddPlugins());
        removeButton.addActionListener(e -> {
            List<String> selected = pluginList.getSelectedValuesList();
            for (String value : selected) {
                pluginModel.removeElement(value);
            }
        });
        clearButton.addActionListener(e -> pluginModel.clear());
        addLoadedButton.addActionListener(e -> mergeLoadedPlugins());

        buttonPane.add(addButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(removeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(clearButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(addLoadedButton);
        buttonPane.add(Box.createHorizontalGlue());
        root.add(buttonPane, BorderLayout.SOUTH);

        pageComponent = root;
        return pageComponent;
    }

    @Override
    public void onBuilderSelected(Builder builder) {
        if (pluginModel == null) {
            createPageComponent();
        }

        boolean supported = builder instanceof IPluginExportBuilder;
        setEnabledRecursive(pageComponent, supported);
        if (!supported) {
            return;
        }

        pluginModel.clear();
        IPluginExportBuilder pluginBuilder = (IPluginExportBuilder) builder;
        for (String pluginPath : pluginBuilder.getExportPlugins()) {
            String normalizedPath = normalizePluginPath(pluginPath);
            if (normalizedPath != null && !pluginModel.contains(normalizedPath)) {
                pluginModel.addElement(normalizedPath);
            }
        }
        mergeLoadedPlugins();
    }

    @Override
    public void onExport(Builder builder) {
        if (!(builder instanceof IPluginExportBuilder pluginBuilder)) {
            return;
        }

        mergeLoadedPlugins();

        ArrayList<String> selectedPlugins = new ArrayList<>();
        for (int i = 0; i < pluginModel.size(); i++) {
            selectedPlugins.add(pluginModel.get(i));
        }
        pluginBuilder.setExportPlugins(selectedPlugins);
    }

    private void promptAndAddPlugins() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Plugin JAR Files", "jar"));

        String initialDirectory = defaultDirectorySupplier == null ? null : normalizePluginPath(defaultDirectorySupplier.get());
        if (initialDirectory != null) {
            File initialDirFile = new File(initialDirectory);
            File directory = initialDirFile.isDirectory() ? initialDirFile : initialDirFile.getParentFile();
            if (directory != null && directory.exists()) {
                chooser.setCurrentDirectory(directory);
            }
        }

        int result = chooser.showOpenDialog(pageComponent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] files = chooser.getSelectedFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            String normalizedPath = normalizePluginPath(file == null ? null : file.getPath());
            if (normalizedPath != null && !pluginModel.contains(normalizedPath)) {
                pluginModel.addElement(normalizedPath);
            }
        }
    }

    private void mergeLoadedPlugins() {
        if (pluginModel == null || pluginManager == null) {
            return;
        }

        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (int i = 0; i < pluginModel.size(); i++) {
            String existing = normalizePluginPath(pluginModel.get(i));
            if (existing != null) {
                merged.add(existing);
            }
        }

        for (PluginJAR loadedJar : pluginManager.loadedJARs()) {
            PluginJARFile fileDetails = loadedJar == null ? null : loadedJar.getFileDetails();
            if (fileDetails == null || !fileDetails.isCompatible()) {
                continue;
            }
            String sourceDirectory = fileDetails.getSourceDirectory();
            String filename = fileDetails.getFilename();
            if (sourceDirectory == null || sourceDirectory.isBlank() || filename == null || filename.isBlank()) {
                continue;
            }
            String normalizedPath = normalizePluginPath(Path.of(sourceDirectory, filename).toString());
            if (normalizedPath != null) {
                merged.add(normalizedPath);
            }
        }

        pluginModel.clear();
        for (String path : merged) {
            pluginModel.addElement(path);
        }
    }

    private String normalizePluginPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            return Path.of(path).toAbsolutePath().normalize().toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setEnabledRecursive(Component component, boolean enabled) {
        if (component == null) {
            return;
        }
        component.setEnabled(enabled);
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                setEnabledRecursive(child, enabled);
            }
        }
    }
}
