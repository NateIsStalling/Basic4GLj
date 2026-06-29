package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.desktop.spi.ProjectSettingsPage;
import com.basic4gl.library.plugin.PluginJARFile;
import com.basic4gl.library.plugin.PluginJARManager;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

public class PluginManagerProjectSettingsPage implements ProjectSettingsPage {
    private final IConfigurableAppSettings appSettings;
    private final PluginJARManager pluginManager;
    private final java.util.function.Supplier<String> defaultDirectorySupplier;
    private final Runnable onSettingsApplied;
    private final Runnable onPluginStateChanged;

    private JComponent pageComponent;
    private JTextField pluginDirectoryField;
    private JTextField mavenLinkField;
    private DefaultTableModel pluginTableModel;
    private JTable pluginTable;
    private JLabel statusLabel;
    private final Map<String, String> rowErrorsByJar = new HashMap<>();
    private boolean suppressPluginTableEvents = false;
    private Timer pluginDirectoryRefreshTimer;
    private String lastScannedDirectory;

    public PluginManagerProjectSettingsPage(
            IConfigurableAppSettings appSettings,
            PluginJARManager pluginManager,
            java.util.function.Supplier<String> defaultDirectorySupplier,
            Runnable onSettingsApplied,
            Runnable onPluginStateChanged) {
        this.appSettings = appSettings;
        this.pluginManager = pluginManager;
        this.defaultDirectorySupplier = defaultDirectorySupplier;
        this.onSettingsApplied = onSettingsApplied;
        this.onPluginStateChanged = onPluginStateChanged;
    }

    @Override
    public String getPageId() {
        return "plugin-manager";
    }

    @Override
    public String getPageTitle() {
        return "Plugins";
    }

    @Override
    public String getPageDescription() {
        return "Manage local plugin JARs and optional Maven source metadata.";
    }

    @Override
    public JComponent createPageComponent() {
        if (pageComponent != null) {
            return pageComponent;
        }

        JPanel container = new JPanel(new BorderLayout(0, 12));

        JPanel directoryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 8);
        directoryPanel.add(new JLabel("Plugin Directory"), gbc);

        pluginDirectoryField = new JTextField(resolveInitialPluginDirectory());
        pluginDirectoryField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                schedulePluginTableRefresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                schedulePluginTableRefresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                schedulePluginTableRefresh();
            }
        });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        directoryPanel.add(pluginDirectoryField, gbc);

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseForDirectory());
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 6, 0);
        directoryPanel.add(browseButton, gbc);

        JButton projectDirectoryButton = new JButton("Use Project Directory");
        projectDirectoryButton.addActionListener(e -> pluginDirectoryField.setText(defaultDirectorySupplier.get()));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 8, 8);
        directoryPanel.add(projectDirectoryButton, gbc);

        JButton refreshButton = new JButton("Refresh JAR List");
        refreshButton.addActionListener(e -> refreshPluginTable());
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        directoryPanel.add(refreshButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 8);
        directoryPanel.add(new JLabel("Maven Link"), gbc);

        mavenLinkField = new JTextField(appSettings.getPluginMavenLink() == null ? "" : appSettings.getPluginMavenLink());
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        directoryPanel.add(mavenLinkField, gbc);

        JButton openLinkButton = new JButton("Open");
        openLinkButton.addActionListener(e -> openMavenLink());
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        directoryPanel.add(openLinkButton, gbc);

        JLabel mavenHintLabel = new JLabel("Maven link is metadata only. Remote plugins are not auto-downloaded.");
        mavenHintLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        directoryPanel.add(mavenHintLabel, gbc);
        gbc.gridwidth = 1;

        container.add(directoryPanel, BorderLayout.NORTH);

        pluginTableModel = new DefaultTableModel(new Object[] {"Loaded", "Plugin JAR", "Version", "Description"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        pluginTable = new JTable(pluginTableModel);
        pluginTable.setFillsViewportHeight(true);
        pluginTable.getColumnModel().getColumn(0).setMaxWidth(70);
        pluginTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        pluginTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        pluginTable.getColumnModel().getColumn(3).setPreferredWidth(360);
        pluginTableModel.addTableModelListener(e -> {
            if (suppressPluginTableEvents || e.getType() != TableModelEvent.UPDATE || e.getColumn() != 0) {
                return;
            }
            int firstRow = e.getFirstRow();
            int lastRow = e.getLastRow();
            if (firstRow < 0 || lastRow < firstRow) {
                return;
            }
            for (int row = firstRow; row <= lastRow; row++) {
                applyPluginLoadStateForRow(row, true);
            }
        });

        JScrollPane pluginTableScrollPane = new JScrollPane(pluginTable);
        pluginTableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        container.add(pluginTableScrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(176, 0, 32));
        container.add(statusLabel, BorderLayout.SOUTH);

        pageComponent = container;
        refreshPluginTable();
        return pageComponent;
    }

    @Override
    public void onApply() {
        if (pluginDirectoryRefreshTimer != null && pluginDirectoryRefreshTimer.isRunning()) {
            pluginDirectoryRefreshTimer.stop();
        }
        if (pluginTable != null && pluginTable.isEditing()) {
            pluginTable.getCellEditor().stopCellEditing();
        }
        Map<String, Boolean> desiredLoadStates = snapshotDesiredLoadStates();
        String pluginDirectory = normalizeNullable(pluginDirectoryField.getText());
        String mavenLink = normalizeNullable(mavenLinkField.getText());

        if (mavenLink != null) {
            validateMavenLink(mavenLink);
        }

        appSettings.setPluginDirectory(pluginDirectory);
        appSettings.setPluginMavenLink(mavenLink);
        onSettingsApplied.run();

        refreshPluginTable();
        restoreDesiredLoadStates(desiredLoadStates);
        applyPluginSelections(true);
        refreshPluginTable();
        onPluginStateChanged.run();
    }

    @Override
    public int getSortOrder() {
        return 220;
    }

    private String resolveInitialPluginDirectory() {
        String configured = appSettings.getPluginDirectory();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        String fallback = defaultDirectorySupplier.get();
        return fallback == null ? "" : fallback;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void browseForDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Plugin Directory");
        String currentValue = normalizeNullable(pluginDirectoryField.getText());
        if (currentValue != null) {
            chooser.setCurrentDirectory(new File(currentValue));
        }
        if (chooser.showOpenDialog(pageComponent) == JFileChooser.APPROVE_OPTION) {
            pluginDirectoryField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void openMavenLink() {
        String link = normalizeNullable(mavenLinkField.getText());
        if (link == null) {
            return;
        }
        try {
            validateMavenLink(link);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(link));
            }
        } catch (Exception ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    private void validateMavenLink(String mavenLink) {
        try {
            URI uri = new URI(mavenLink);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http"))) {
                throw new IllegalArgumentException("Maven link must be an http(s) URL.");
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Maven link must be a valid URL.");
        }
    }

    private void refreshPluginTable() {
        statusLabel.setText(" ");
        pluginTableModel.setRowCount(0);

        String directory = normalizeNullable(pluginDirectoryField.getText());
        if (directory == null) {
            rowErrorsByJar.clear();
            lastScannedDirectory = null;
            statusLabel.setText("Set a plugin directory to scan for plugin JARs.");
            return;
        }
        if (!directory.equals(lastScannedDirectory)) {
            rowErrorsByJar.clear();
            lastScannedDirectory = directory;
        }

        pluginManager.setDirectory(directory);
        List<PluginJARFile> jarFiles = new ArrayList<>(pluginManager.getJARFiles());
        jarFiles.sort(Comparator.comparing(PluginJARFile::getFilename, String.CASE_INSENSITIVE_ORDER));

        suppressPluginTableEvents = true;
        for (PluginJARFile file : jarFiles) {
            String version =
                    file.getVersion() == null ? "-" : file.getVersion().getMajorVersion() + "." + file.getVersion().getMinorVersion();
            String rowDescription = resolveDescription(file);
            pluginTableModel.addRow(new Object[] {file.isLoaded(), file.getFilename(), version, rowDescription});
        }
        suppressPluginTableEvents = false;

        if (jarFiles.isEmpty()) {
            statusLabel.setText("No plugin jars found in " + directory);
        } else if (pluginManager.getError() != null && !pluginManager.getError().isBlank()) {
            statusLabel.setText(pluginManager.getError());
        }
    }

    private void applyPluginSelections(boolean triggerSyntaxRefresh) {
        for (int i = 0; i < pluginTableModel.getRowCount(); i++) {
            applyPluginLoadStateForRow(i, triggerSyntaxRefresh);
        }
    }

    private Map<String, Boolean> snapshotDesiredLoadStates() {
        Map<String, Boolean> desiredLoadStates = new LinkedHashMap<>();
        if (pluginTableModel == null) {
            return desiredLoadStates;
        }
        for (int i = 0; i < pluginTableModel.getRowCount(); i++) {
            String filename = (String) pluginTableModel.getValueAt(i, 1);
            if (filename != null && !filename.isBlank()) {
                desiredLoadStates.put(filename, Boolean.TRUE.equals(pluginTableModel.getValueAt(i, 0)));
            }
        }
        return desiredLoadStates;
    }

    private void restoreDesiredLoadStates(Map<String, Boolean> desiredLoadStates) {
        if (desiredLoadStates == null || desiredLoadStates.isEmpty() || pluginTableModel == null) {
            return;
        }
        suppressPluginTableEvents = true;
        for (int i = 0; i < pluginTableModel.getRowCount(); i++) {
            String filename = (String) pluginTableModel.getValueAt(i, 1);
            if (filename == null || filename.isBlank()) {
                continue;
            }
            Boolean desired = desiredLoadStates.get(filename);
            if (desired != null) {
                pluginTableModel.setValueAt(desired, i, 0);
            }
        }
        suppressPluginTableEvents = false;
    }

    private void applyPluginLoadStateForRow(int row, boolean triggerSyntaxRefresh) {
        if (row < 0 || row >= pluginTableModel.getRowCount()) {
            return;
        }

        boolean shouldBeLoaded = Boolean.TRUE.equals(pluginTableModel.getValueAt(row, 0));
        String filename = (String) pluginTableModel.getValueAt(row, 1);
        if (filename == null || filename.isBlank()) {
            return;
        }
        boolean isLoaded = pluginManager.isLoaded(filename);
        if (shouldBeLoaded == isLoaded) {
            return;
        }

        boolean success = shouldBeLoaded ? pluginManager.loadPlugin(filename) : pluginManager.unloadPlugin(filename);
        if (!success) {
            String error = pluginManager.getError();
            rowErrorsByJar.put(filename, error);
            statusLabel.setText(error == null || error.isBlank() ? "Plugin action failed." : error);
            suppressPluginTableEvents = true;
            pluginTableModel.setValueAt(isLoaded, row, 0);
            pluginTableModel.setValueAt(error, row, 3);
            suppressPluginTableEvents = false;
            return;
        }

        rowErrorsByJar.remove(filename);
        if (triggerSyntaxRefresh) {
            onPluginStateChanged.run();
        }
        refreshPluginRow(filename, row);
    }

    private void refreshPluginRow(String filename, int row) {
        if (row < 0 || row >= pluginTableModel.getRowCount()) {
            return;
        }
        PluginJARFile jarFile = pluginManager.find(filename) != null ? pluginManager.find(filename).getFileDetails() : null;
        if (jarFile == null) {
            List<PluginJARFile> jarFiles = pluginManager.getJARFiles();
            for (PluginJARFile file : jarFiles) {
                if (filename.equals(file.getFilename())) {
                    jarFile = file;
                    break;
                }
            }
        }

        if (jarFile == null) {
            return;
        }

        String version =
                jarFile.getVersion() == null ? "-" : jarFile.getVersion().getMajorVersion() + "." + jarFile.getVersion().getMinorVersion();
        String description = resolveDescription(jarFile);

        suppressPluginTableEvents = true;
        pluginTableModel.setValueAt(jarFile.isLoaded(), row, 0);
        pluginTableModel.setValueAt(version, row, 2);
        pluginTableModel.setValueAt(description, row, 3);
        suppressPluginTableEvents = false;
    }

    private String resolveDescription(PluginJARFile jarFile) {
        if (jarFile == null) {
            return "";
        }
        String filename = jarFile.getFilename();
        if (filename != null && rowErrorsByJar.containsKey(filename)) {
            return rowErrorsByJar.get(filename);
        }
        if (jarFile.getDescription() != null && !jarFile.getDescription().isBlank()) {
            return jarFile.getDescription();
        }
        return filename == null ? "" : filename;
    }

    private void schedulePluginTableRefresh() {
        if (pluginDirectoryRefreshTimer == null) {
            pluginDirectoryRefreshTimer = new Timer(250, e -> refreshPluginTable());
            pluginDirectoryRefreshTimer.setRepeats(false);
        }
        pluginDirectoryRefreshTimer.restart();
    }
}
