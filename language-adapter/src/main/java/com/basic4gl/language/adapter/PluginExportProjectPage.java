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
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PluginExportProjectPage implements ProjectExportPage {
    private final PluginJARManager pluginManager;
    private final Supplier<String> defaultDirectorySupplier;

    private JComponent pageComponent;
    private DefaultListModel<String> pluginModel;
    private JList<String> pluginList;
    private JLabel statusLabel;

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

        JLabel description = new JLabel("Plugin libraries to include in the exported package:");
        root.add(description, BorderLayout.NORTH);

        pluginModel = new DefaultListModel<>();
        pluginList = new JList<>(pluginModel);
        pluginList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pluginList.setCellRenderer(new PluginPathRenderer());

        JScrollPane pluginScrollPane = new JScrollPane(pluginList);
        pluginScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(pluginScrollPane, BorderLayout.CENTER);

        JButton addLoadedButton = new JButton("Add Loaded");
        addLoadedButton.addActionListener(e -> mergeLoadedPlugins());

        JButton addButton = new JButton("Add...");
        addButton.addActionListener(e -> promptAndAddPlugins());

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> {
            List<String> selected = pluginList.getSelectedValuesList();
            for (String value : selected) {
                pluginModel.removeElement(value);
            }
            updateStatusLabel();
        });

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            pluginModel.clear();
            updateStatusLabel();
        });

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(addLoadedButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(addButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(removeButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(clearButton);
        buttonPane.add(Box.createHorizontalGlue());

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.add(buttonPane, BorderLayout.WEST);
        footer.add(statusLabel, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);

        pageComponent = root;
        updateStatusLabel();
        return pageComponent;
    }

    @Override
    public void onBuilderSelected(Builder builder) {
        ensureUiCreated();

        boolean supported = builder instanceof IPluginExportBuilder;
        setEnabledRecursive(pageComponent, supported);
        if (!supported) {
            statusLabel.setText("Plugin export is not supported by the selected target.");
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

    private void ensureUiCreated() {
        if (pluginModel == null || pluginList == null || statusLabel == null || pageComponent == null) {
            createPageComponent();
        }
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
        updateStatusLabel();
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
        updateStatusLabel();
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

    private void normalizeButtonWidths(JButton... buttons) {
        int maxWidth = 0;
        int maxHeight = 0;
        for (JButton button : buttons) {
            if (button == null) {
                continue;
            }
            Dimension preferred = button.getPreferredSize();
            if (preferred == null) {
                continue;
            }
            maxWidth = Math.max(maxWidth, preferred.width);
            maxHeight = Math.max(maxHeight, preferred.height);
        }
        if (maxWidth <= 0 || maxHeight <= 0) {
            return;
        }
        Dimension normalized = new Dimension(maxWidth, maxHeight);
        for (JButton button : buttons) {
            if (button == null) {
                continue;
            }
            button.setPreferredSize(normalized);
            button.setMinimumSize(normalized);
            button.setMaximumSize(new Dimension(maxWidth, maxHeight));
        }
    }

    private void updateStatusLabel() {
        if (statusLabel == null || pluginModel == null) {
            return;
        }
        int count = pluginModel.size();
        statusLabel.setText(count == 1 ? "1 plugin selected for export." : count + " plugins selected for export.");
    }

    private static class PluginPathRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String path = value == null ? "" : value.toString();
            String filename = path;
            try {
                Path p = Path.of(path);
                Path fileName = p.getFileName();
                if (fileName != null) {
                    filename = fileName.toString();
                }
            } catch (Exception ignored) {
            }
            label.setText(filename);
            label.setToolTipText(path);
            return label;
        }
    }
}
