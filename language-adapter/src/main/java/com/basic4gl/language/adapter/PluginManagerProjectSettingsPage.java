package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.desktop.spi.ProjectSettingsPage;
import com.basic4gl.library.plugin.PluginJAR;
import com.basic4gl.library.plugin.PluginJARDetails;
import com.basic4gl.library.plugin.PluginJARFile;
import com.basic4gl.library.plugin.PluginJARManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class PluginManagerProjectSettingsPage implements ProjectSettingsPage {
    private final IConfigurableAppSettings appSettings;
    private final PluginJARManager pluginManager;
    private final java.util.function.Supplier<String> defaultDirectorySupplier;
    private final java.util.function.Supplier<List<String>> recentDirectoriesSupplier;
    private final Runnable onSettingsApplied;
    private final Runnable onPluginStateChanged;

    private JComponent pageComponent;
    private DefaultListModel<String> pluginSourceListModel;
    private JList<String> pluginSourceList;
    private JButton addSourceButton;
    private JButton addSourceButtonInstalled;
    private JButton projectDirectoryButton;
    private JButton removeSourceButton;
    private DefaultTableModel pluginTableModel;
    private JTable pluginTable;
    private JCheckBox showUnsupportedCheckbox;
    private JLabel statusLabel;
    private final Map<String, String> rowErrorsByJar = new HashMap<>();
    private final Map<String, String> pluginDisplayNamesByRowKey = new HashMap<>();
    private final Set<String> incompatiblePluginRows = new HashSet<>();
    private boolean suppressPluginTableEvents = false;
    private Timer pluginDirectoryRefreshTimer;
    private String lastScannedSourcesKey;

    public PluginManagerProjectSettingsPage(
            IConfigurableAppSettings appSettings,
            PluginJARManager pluginManager,
            java.util.function.Supplier<String> defaultDirectorySupplier,
            java.util.function.Supplier<List<String>> recentDirectoriesSupplier,
            Runnable onSettingsApplied,
            Runnable onPluginStateChanged) {
        this.appSettings = appSettings;
        this.pluginManager = pluginManager;
        this.defaultDirectorySupplier = defaultDirectorySupplier;
        this.recentDirectoriesSupplier = recentDirectoriesSupplier;
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
        return "Manage Basic4GL language plugins.";
    }

    @Override
    public JComponent createPageComponent() {
        if (pageComponent != null) {
            return pageComponent;
        }

        JPanel container = new JPanel(new BorderLayout(0, 12));

        JPanel directoryPanel = new JPanel(new BorderLayout(8, 8));
        directoryPanel.setBorder(BorderFactory.createTitledBorder("Plugin Folders"));

        pluginSourceListModel = new DefaultListModel<>();
        pluginSourceList = new JList<>(pluginSourceListModel);
        pluginSourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginSourceList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            syncSourceActionButtons();
        });
        JScrollPane sourceListScrollPane = new JScrollPane(pluginSourceList);
        sourceListScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel sourceActionsPanel = new JPanel();
        sourceActionsPanel.setLayout(new BoxLayout(sourceActionsPanel, BoxLayout.Y_AXIS));
        addSourceButton = new JButton("Add Folder...");
        addSourceButton.addActionListener(e -> addFolder());
        projectDirectoryButton = new JButton("Add Project Folder");
        projectDirectoryButton.addActionListener(e -> {
            String projectDirectory = normalizeNullable(defaultDirectorySupplier.get());
            if (projectDirectory != null) {
                addOrMoveSource(projectDirectory);
            }
        });
        removeSourceButton = new JButton("Remove");
        removeSourceButton.addActionListener(e -> removeSelectedSource());

        sourceActionsPanel.add(addSourceButton);
        sourceActionsPanel.add(Box.createVerticalStrut(12));
        sourceActionsPanel.add(projectDirectoryButton);
        sourceActionsPanel.add(Box.createVerticalStrut(12));
        sourceActionsPanel.add(removeSourceButton);
        sourceActionsPanel.add(Box.createVerticalGlue());
        normalizeButtonWidths(addSourceButton, removeSourceButton, projectDirectoryButton);

        JPanel sourceBodyPanel = new JPanel(new BorderLayout(8, 0));
        sourceBodyPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        sourceBodyPanel.add(sourceListScrollPane, BorderLayout.CENTER);
        sourceBodyPanel.add(sourceActionsPanel, BorderLayout.EAST);

        directoryPanel.add(sourceBodyPanel, BorderLayout.CENTER);

        pluginTableModel =
                new DefaultTableModel(
                        new Object[] {"Enabled", "Name", "Source", "Version", "Description", "Details"}, 0) {
                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        if (columnIndex == 0) {
                            return Boolean.class;
                        }
                        return String.class;
                    }

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return (column == 0 || column == 5) && !isIncompatibleRow(row);
                    }
                };
        pluginTable = new JTable(pluginTableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                boolean incompatible = isIncompatibleRow(row);

                if (isCellSelected(row, column)) {
                    component.setBackground(getSelectionBackground());
                    component.setForeground(
                            incompatible ? UIManager.getColor("Label.disabledForeground") : getSelectionForeground());
                } else {
                    component.setBackground(getBackground());
                    component.setForeground(
                            incompatible ? UIManager.getColor("Label.disabledForeground") : getForeground());
                }
                component.setEnabled(true);
                return component;
            }
        };
        pluginTable.setFillsViewportHeight(true);
        pluginTable.getColumnModel().getColumn(0).setMinWidth(90);
        pluginTable.getColumnModel().getColumn(0).setPreferredWidth(105);
        pluginTable.getColumnModel().getColumn(0).setMaxWidth(130);
        pluginTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        // Render column 1 using plugin metadata name when available; model still stores filename
        pluginTable.getColumnModel().getColumn(1).setCellRenderer(new PluginNameRenderer());
        pluginTable.getColumnModel().removeColumn(pluginTable.getColumnModel().getColumn(2));
        pluginTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        pluginTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        pluginTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        pluginTable.getColumnModel().getColumn(4).setMaxWidth(140);
        pluginTable.getColumnModel().getColumn(4).setCellRenderer(new DetailsButtonRenderer());
        pluginTable.getColumnModel().getColumn(4).setCellEditor(new DetailsButtonEditor());
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

        JPanel pluginsPanel = new JPanel(new BorderLayout(0, 8));
        JPanel pluginHeaderPanel = new JPanel(new BorderLayout(8, 0));
        pluginHeaderPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        JPanel pluginHeaderActions = new JPanel();
        pluginHeaderActions.setLayout(new BoxLayout(pluginHeaderActions, BoxLayout.X_AXIS));
        pluginHeaderActions.setBorder(BorderFactory.createEmptyBorder());
        showUnsupportedCheckbox = new JCheckBox("Show unsupported", false);
        showUnsupportedCheckbox.addActionListener(e -> refreshPluginTable());
        JButton refreshPluginsButton = new JButton("Refresh");
        refreshPluginsButton.addActionListener(e -> refreshPluginTable());
        addSourceButtonInstalled = new JButton("Add Folder...");
        addSourceButtonInstalled.addActionListener(e -> addFolder());
        normalizeButtonWidths(addSourceButtonInstalled, refreshPluginsButton);
        pluginHeaderActions.add(Box.createHorizontalGlue());
        pluginHeaderActions.add(showUnsupportedCheckbox);
        pluginHeaderActions.add(Box.createHorizontalStrut(8));
        pluginHeaderActions.add(refreshPluginsButton);
        pluginHeaderActions.add(Box.createHorizontalStrut(8));
        pluginHeaderActions.add(addSourceButtonInstalled);
        pluginHeaderPanel.add(pluginHeaderActions, BorderLayout.CENTER);
        pluginsPanel.add(pluginHeaderPanel, BorderLayout.NORTH);
        pluginsPanel.add(pluginTableScrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(176, 0, 32));
        pluginsPanel.add(statusLabel, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Installed", pluginsPanel);
        tabs.addTab("Sources", directoryPanel);
        container.add(tabs, BorderLayout.CENTER);

        initializeSourceList();
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
        List<String> pluginDirectories = getPrioritizedSourceDirectories();

        appSettings.setPluginDirectories(pluginDirectories);
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
        List<String> configuredDirectories = appSettings.getPluginDirectories();
        String configured = (configuredDirectories == null || configuredDirectories.isEmpty())
                ? appSettings.getPluginDirectory()
                : configuredDirectories.get(0);
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

    private void addFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Plugin Directory");
        String currentValue = normalizeNullable(pluginSourceList == null ? null : pluginSourceList.getSelectedValue());
        if (currentValue != null) {
            chooser.setCurrentDirectory(new File(currentValue));
        } else {
            String fallback = normalizeNullable(defaultDirectorySupplier.get());
            if (fallback != null) {
                chooser.setCurrentDirectory(new File(fallback));
            }
        }
        if (chooser.showOpenDialog(pageComponent) == JFileChooser.APPROVE_OPTION) {
            addOrMoveSource(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void initializeSourceList() {
        pluginSourceListModel.clear();
        List<String> configuredDirectories = appSettings.getPluginDirectories();
        if (configuredDirectories != null && !configuredDirectories.isEmpty()) {
            for (String directory : configuredDirectories) {
                addSourceIfMissing(normalizeNullable(directory));
            }
        } else {
            String initialDirectory = normalizeNullable(resolveInitialPluginDirectory());
            if (initialDirectory != null) {
                pluginSourceListModel.addElement(initialDirectory);
            }
        }
        List<String> recentDirectories =
                recentDirectoriesSupplier == null ? List.of() : recentDirectoriesSupplier.get();
        if (recentDirectories != null) {
            for (String directory : recentDirectories) {
                addSourceIfMissing(normalizeNullable(directory));
            }
        }
        if (!pluginSourceListModel.isEmpty()) {
            pluginSourceList.setSelectedIndex(0);
        }
        syncSourceActionButtons();
    }

    private void addSourceIfMissing(String directory) {
        if (directory == null) {
            return;
        }
        for (int i = 0; i < pluginSourceListModel.size(); i++) {
            if (directory.equalsIgnoreCase(pluginSourceListModel.get(i))) {
                return;
            }
        }
        pluginSourceListModel.addElement(directory);
    }

    private void addOrMoveSource(String directory) {
        if (directory == null) {
            return;
        }
        int existingIndex = findSourceIndex(directory);
        if (existingIndex >= 0) {
            pluginSourceListModel.remove(existingIndex);
        }
        pluginSourceListModel.add(0, directory);
        pluginSourceList.setSelectedIndex(0);
        schedulePluginTableRefresh();
        syncSourceActionButtons();
    }

    private void removeSelectedSource() {
        int selectedIndex = pluginSourceList.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        pluginSourceListModel.remove(selectedIndex);
        if (pluginSourceListModel.isEmpty()) {
            pluginSourceList.clearSelection();
        } else {
            int nextIndex = Math.min(selectedIndex, pluginSourceListModel.size() - 1);
            pluginSourceList.setSelectedIndex(nextIndex);
        }
        schedulePluginTableRefresh();
        syncSourceActionButtons();
    }

    private int findSourceIndex(String directory) {
        for (int i = 0; i < pluginSourceListModel.size(); i++) {
            if (directory.equalsIgnoreCase(pluginSourceListModel.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void syncSourceActionButtons() {
        if (pluginSourceList == null) {
            return;
        }
        if (removeSourceButton != null) {
            removeSourceButton.setEnabled(pluginSourceList.getSelectedIndex() >= 0);
        }
        if (projectDirectoryButton != null) {
            projectDirectoryButton.setEnabled(normalizeNullable(defaultDirectorySupplier.get()) != null);
        }
    }

    private void refreshPluginTable() {
        if (statusLabel == null || pluginTableModel == null) {
            return;
        }
        statusLabel.setText(" ");
        pluginTableModel.setRowCount(0);

        List<String> directories = getPrioritizedSourceDirectories();
        if (directories.isEmpty()) {
            rowErrorsByJar.clear();
            pluginDisplayNamesByRowKey.clear();
            incompatiblePluginRows.clear();
            updateShowUnsupportedLabel(0);
            lastScannedSourcesKey = null;
            statusLabel.setText("Set at least one plugin source to scan for plugin JARs.");
            return;
        }
        String sourcesKey = String.join("|", directories).toLowerCase();
        if (!sourcesKey.equals(lastScannedSourcesKey)) {
            rowErrorsByJar.clear();
            lastScannedSourcesKey = sourcesKey;
        }

        pluginManager.setDirectories(directories);
        List<PluginJARFile> jarFiles = new ArrayList<>(pluginManager.getJARFiles());
        jarFiles.sort(
                Comparator.comparing((PluginJARFile file) -> sourceSortIndex(file.getSourceDirectory(), directories))
                        .thenComparing(PluginJARFile::getFilename, String.CASE_INSENSITIVE_ORDER));
        pluginDisplayNamesByRowKey.clear();
        incompatiblePluginRows.clear();
        boolean showUnsupported = showUnsupportedCheckbox == null || showUnsupportedCheckbox.isSelected();

        suppressPluginTableEvents = true;
        for (PluginJARFile file : jarFiles) {
            String rowKey = buildRowKey(file.getFilename(), file.getSourceDirectory());
            if (!file.isCompatible()) {
                incompatiblePluginRows.add(rowKey);
                if (!showUnsupported) {
                    continue;
                }
            }
            String displayName = resolvePluginDisplayName(file);
            if (displayName != null) {
                pluginDisplayNamesByRowKey.put(rowKey, displayName);
            }
            String version = file.getVersion() == null
                    ? "-"
                    : file.getVersion().getMajorVersion() + "."
                            + file.getVersion().getMinorVersion();
            String rowDescription = resolveDescription(file);
            pluginTableModel.addRow(new Object[] {
                file.isLoaded() && file.isCompatible(),
                file.getFilename(),
                file.getSourceDirectory(),
                version,
                rowDescription,
                "View Details"
            });
        }
        updateShowUnsupportedLabel(incompatiblePluginRows.size());
        suppressPluginTableEvents = false;

        if (pluginTableModel.getRowCount() == 0) {
            statusLabel.setText("No plugin jars found in configured sources.");
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
            String sourceDirectory = (String) pluginTableModel.getValueAt(i, 2);
            if (filename != null && !filename.isBlank()) {
                desiredLoadStates.put(
                        buildRowKey(filename, sourceDirectory), Boolean.TRUE.equals(pluginTableModel.getValueAt(i, 0)));
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
            String sourceDirectory = (String) pluginTableModel.getValueAt(i, 2);
            if (filename == null || filename.isBlank()) {
                continue;
            }
            Boolean desired = desiredLoadStates.get(buildRowKey(filename, sourceDirectory));
            if (desired != null) {
                pluginTableModel.setValueAt(
                        desired && !isIncompatibleRowKey(buildRowKey(filename, sourceDirectory)), i, 0);
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
        String sourceDirectory = (String) pluginTableModel.getValueAt(row, 2);
        if (filename == null || filename.isBlank()) {
            return;
        }
        String rowKey = buildRowKey(filename, sourceDirectory);
        if (isIncompatibleRowKey(rowKey)) {
            if (shouldBeLoaded) {
                suppressPluginTableEvents = true;
                pluginTableModel.setValueAt(false, row, 0);
                suppressPluginTableEvents = false;
            }
            return;
        }
        boolean isLoaded = pluginManager.isLoaded(filename, sourceDirectory);
        if (shouldBeLoaded == isLoaded) {
            return;
        }

        boolean success = shouldBeLoaded
                ? pluginManager.loadPlugin(filename, sourceDirectory)
                : pluginManager.unloadPlugin(filename, sourceDirectory);
        if (!success) {
            String error = pluginManager.getError();
            rowErrorsByJar.put(rowKey, error);
            statusLabel.setText(error == null || error.isBlank() ? "Plugin action failed." : error);
            suppressPluginTableEvents = true;
            pluginTableModel.setValueAt(isLoaded, row, 0);
            pluginTableModel.setValueAt(error, row, 4);
            suppressPluginTableEvents = false;
            return;
        }

        rowErrorsByJar.remove(rowKey);
        if (triggerSyntaxRefresh) {
            onPluginStateChanged.run();
        }
        refreshPluginRow(filename, sourceDirectory, row);
    }

    private void refreshPluginRow(String filename, String sourceDirectory, int row) {
        if (row < 0 || row >= pluginTableModel.getRowCount()) {
            return;
        }
        PluginJAR loaded = pluginManager.findByFilenameAndSource(filename, sourceDirectory);
        PluginJARFile jarFile = loaded != null ? loaded.getFileDetails() : null;
        if (jarFile == null) {
            List<PluginJARFile> jarFiles = pluginManager.getJARFiles();
            for (PluginJARFile file : jarFiles) {
                String fileSourceDirectory = file.getSourceDirectory();
                if (filename.equals(file.getFilename())
                        && ((sourceDirectory == null && fileSourceDirectory == null)
                                || (sourceDirectory != null
                                        && fileSourceDirectory != null
                                        && sourceDirectory.equalsIgnoreCase(fileSourceDirectory)))) {
                    jarFile = file;
                    break;
                }
            }
        }

        if (jarFile == null) {
            return;
        }

        String version = jarFile.getVersion() == null
                ? "-"
                : jarFile.getVersion().getMajorVersion() + "."
                        + jarFile.getVersion().getMinorVersion();
        String description = resolveDescription(jarFile);
        String rowKey = buildRowKey(filename, sourceDirectory);
        if (!jarFile.isCompatible()) {
            incompatiblePluginRows.add(rowKey);
        } else {
            incompatiblePluginRows.remove(rowKey);
        }
        String displayName = resolvePluginDisplayName(jarFile);
        if (displayName == null) {
            pluginDisplayNamesByRowKey.remove(rowKey);
        } else {
            pluginDisplayNamesByRowKey.put(rowKey, displayName);
        }

        suppressPluginTableEvents = true;
        pluginTableModel.setValueAt(jarFile.isLoaded() && jarFile.isCompatible(), row, 0);
        pluginTableModel.setValueAt(jarFile.getFilename(), row, 1);
        pluginTableModel.setValueAt(jarFile.getSourceDirectory(), row, 2);
        pluginTableModel.setValueAt(version, row, 3);
        pluginTableModel.setValueAt(description, row, 4);
        pluginTableModel.setValueAt("View", row, 5);
        suppressPluginTableEvents = false;
    }

    private String resolveDescription(PluginJARFile jarFile) {
        if (jarFile == null) {
            return "";
        }
        String rowKey = buildRowKey(jarFile.getFilename(), jarFile.getSourceDirectory());
        if (rowErrorsByJar.containsKey(rowKey)) {
            return rowErrorsByJar.get(rowKey);
        }
        if (jarFile.getDescription() != null && !jarFile.getDescription().isBlank()) {
            return jarFile.getDescription();
        }
        return jarFile.getFilename() == null ? "" : jarFile.getFilename();
    }

    private String resolvePluginDisplayName(PluginJARFile jarFile) {
        if (jarFile == null || !jarFile.isCompatible()) {
            return null;
        }
        String pluginName = jarFile.getPluginName();
        return pluginName == null || pluginName.isBlank() ? null : pluginName;
    }

    private void schedulePluginTableRefresh() {
        if (pluginTableModel == null || statusLabel == null) {
            return;
        }
        if (pluginDirectoryRefreshTimer == null) {
            pluginDirectoryRefreshTimer = new Timer(250, e -> refreshPluginTable());
            pluginDirectoryRefreshTimer.setRepeats(false);
        }
        pluginDirectoryRefreshTimer.restart();
    }

    private void updateShowUnsupportedLabel(int unsupportedCount) {
        if (showUnsupportedCheckbox == null) {
            return;
        }
        showUnsupportedCheckbox.setText("Show unsupported (" + Math.max(0, unsupportedCount) + ")");
    }

    private void normalizeButtonWidths(AbstractButton... buttons) {
        int maxWidth = 0;
        int maxHeight = 0;
        for (AbstractButton button : buttons) {
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
        for (AbstractButton button : buttons) {
            if (button == null) {
                continue;
            }
            button.setPreferredSize(normalized);
            button.setMinimumSize(normalized);
            button.setMaximumSize(new Dimension(maxWidth, maxHeight));
        }
    }

    private List<String> getPrioritizedSourceDirectories() {
        ArrayList<String> sources = new ArrayList<>();
        if (pluginSourceListModel != null) {
            for (int i = 0; i < pluginSourceListModel.size(); i++) {
                String directory = normalizeNullable(pluginSourceListModel.get(i));
                if (directory == null) {
                    continue;
                }
                boolean exists = false;
                for (String existing : sources) {
                    if (existing.equalsIgnoreCase(directory)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    sources.add(directory);
                }
            }
        }

        if (sources.isEmpty()) {
            return sources;
        }
        ArrayList<String> prioritized = new ArrayList<>();
        prioritized.add(sources.get(0));
        if (sources.size() > 1) {
            ArrayList<String> remaining = new ArrayList<>(sources.subList(1, sources.size()));
            remaining.sort(String.CASE_INSENSITIVE_ORDER);
            prioritized.addAll(remaining);
        }
        return prioritized;
    }

    private int sourceSortIndex(String sourceDirectory, List<String> sortedSources) {
        if (sourceDirectory == null || sortedSources == null || sortedSources.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        for (int i = 0; i < sortedSources.size(); i++) {
            if (sourceDirectory.equalsIgnoreCase(sortedSources.get(i))) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    private String buildRowKey(String filename, String sourceDirectory) {
        String file = filename == null ? "" : filename.trim().toLowerCase();
        String source = sourceDirectory == null ? "" : sourceDirectory.trim().toLowerCase();
        return source + "|" + file;
    }

    private boolean isIncompatibleRow(int row) {
        if (row < 0 || row >= pluginTableModel.getRowCount()) {
            return false;
        }
        String filename = (String) pluginTableModel.getValueAt(row, 1);
        String sourceDirectory = (String) pluginTableModel.getValueAt(row, 2);
        return isIncompatibleRowKey(buildRowKey(filename, sourceDirectory));
    }

    private boolean isIncompatibleRowKey(String rowKey) {
        return rowKey != null && incompatiblePluginRows.contains(rowKey);
    }

    private void showDetailsDialog(int row) {
        if (row < 0 || row >= pluginTableModel.getRowCount()) {
            return;
        }
        String filename = (String) pluginTableModel.getValueAt(row, 1);
        String sourceDirectory = (String) pluginTableModel.getValueAt(row, 2);
        String rowKey = buildRowKey(filename, sourceDirectory);
        if (filename == null || filename.isBlank() || isIncompatibleRowKey(rowKey)) {
            return;
        }

        PluginJARDetails details = pluginManager.getPluginDetails(filename, sourceDirectory);
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(pageComponent),
                "Plugin Details: " + filename,
                Dialog.ModalityType.MODELESS);
        dialog.setLayout(new BorderLayout(0, 8));

        JPanel metadataPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Summary row
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        metadataPanel.add(new JLabel("Summary:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String summaryText = details.getMetadataSummary() == null
                        || details.getMetadataSummary().isBlank()
                ? "-"
                : details.getMetadataSummary();
        JLabel summaryLabel = new JLabel(summaryText);
        metadataPanel.add(summaryLabel, gbc);

        // Compatible row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        metadataPanel.add(new JLabel("Compatible:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        metadataPanel.add(new JLabel(details.isCompatible() ? "Yes" : "No"), gbc);

        // Metadata details row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        metadataPanel.add(new JLabel("Details:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String metadataDetailsText = details.getMetadataDetails() == null
                        || details.getMetadataDetails().isBlank()
                ? "-"
                : details.getMetadataDetails();
        JLabel metadataDetailsLabel = new JLabel(formatMetadataDetailsHtml(metadataDetailsText));
        metadataDetailsLabel.setVerticalAlignment(SwingConstants.TOP);
        metadataPanel.add(metadataDetailsLabel, gbc);

        metadataPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        dialog.add(metadataPanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Functions", createListPane(details.getFunctions()));
        tabs.addTab("Constants", createListPane(details.getConstants()));
        tabs.addTab("Structures", createTextPane(details.getStructures()));
        dialog.add(tabs, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(new Dimension(760, 520));
        dialog.setLocationRelativeTo(pageComponent);
        dialog.setVisible(true);
    }

    private String formatMetadataDetailsHtml(String details) {
        String escaped = escapeHtml(details == null ? "" : details);
        return "<html>" + escaped.replace(System.lineSeparator(), "<br>").replace("\n", "<br>") + "</html>";
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private JScrollPane createListPane(List<String> entries) {
        DefaultListModel<String> model = new DefaultListModel<>();
        if (entries == null || entries.isEmpty()) {
            model.addElement("(none)");
        } else {
            for (String entry : entries) {
                model.addElement(entry);
            }
        }
        JList<String> list = new JList<>(model);
        return new JScrollPane(list);
    }

    private JScrollPane createTextPane(String text) {
        JTextArea area = new JTextArea(text == null || text.isBlank() ? "(none)" : text);
        area.setEditable(false);
        area.setLineWrap(false);
        area.setCaretPosition(0);
        return new JScrollPane(area);
    }

    private class PluginNameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String filename = value == null ? "" : value.toString();
            String display = filename;
            int modelRow = table.convertRowIndexToModel(row);
            Object srcObj = table.getModel().getValueAt(modelRow, 2);
            String source = srcObj == null ? null : srcObj.toString();
            String displayName = pluginDisplayNamesByRowKey.get(buildRowKey(filename, source));
            if (displayName != null) {
                display = displayName;
            }

            return super.getTableCellRendererComponent(table, display, isSelected, hasFocus, row, column);
        }
    }

    private class DetailsButtonRenderer extends JButton implements TableCellRenderer {
        DetailsButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean incompatible = isIncompatibleRow(row);
            setText("View");
            setEnabled(!incompatible);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(UIManager.getColor("Button.background"));
                setForeground(UIManager.getColor("Button.foreground"));
            }
            return this;
        }
    }

    private class DetailsButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button = new JButton("View");
        private int row = -1;

        DetailsButtonEditor() {
            button.addActionListener(this::onClick);
        }

        @Override
        public Object getCellEditorValue() {
            return "View";
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            button.setEnabled(!isIncompatibleRow(row));
            return button;
        }

        private void onClick(ActionEvent event) {
            fireEditingStopped();
            showDetailsDialog(row);
        }
    }
}
