package com.basic4gl.desktop;

import com.basic4gl.compiler.util.IAssetExportBuilder;
import com.basic4gl.desktop.content.FileEditor;
import com.basic4gl.desktop.spi.*;
import com.basic4gl.desktop.util.EditorSourceFile;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Created by Nate on 2/5/2015.
 */
public class ExportDialog implements com.basic4gl.desktop.spi.ConfigurationFormPanel.IOnConfigurationChangeListener {

    private final CompilerService compiler;
    private final PreprocessorService preprocessor;
    private final LanguageService languageService;

    private final Vector<FileEditor> fileEditors;

    private final JDialog dialog;
    private final JDialog libraryInfoDialog;
    private final JTabbedPane tabs;
    private final JComboBox<String> builderComboBox;
    private final JButton libraryInfoButton;
    private final DefaultListModel<String> embeddedAssetsModel;
    private final JList<String> embeddedAssetsList;

    private final JTextField filePathTextField;
    private final String exportBaseDirectory;
    private final java.util.List<ProjectExportPage> contributedPages;

    private final JTextPane infoTextPane;
    private final com.basic4gl.desktop.spi.ConfigurationFormPanel configPane;
    private final JButton exportButton;
    private final JProgressBar exportProgressBar;
    private File lastExportDestination;

    // Libraries
    private java.util.List<Builder> builders;
    private int currentBuilder; // Index value of target in mTargets

    public ExportDialog(
            Frame parent,
            CompilerService compiler,
            PreprocessorService preprocessor,
            LanguageService languageService,
            Vector<FileEditor> editors,
            String exportBaseDirectory,
            java.util.List<ProjectExportPage> contributedExportPages) {

        this.compiler = compiler;
        this.preprocessor = preprocessor;
        this.languageService = languageService;

        fileEditors = editors;
        this.exportBaseDirectory = com.basic4gl.language.adapter.FileUtil.separatorsToSystem(exportBaseDirectory);
        this.contributedPages =
                new ArrayList<>(contributedExportPages == null ? Collections.emptyList() : contributedExportPages);
        this.contributedPages.sort(Comparator.comparingInt(ProjectExportPage::getSortOrder)
                .thenComparing(ProjectExportPage::getPageTitle, String.CASE_INSENSITIVE_ORDER));

        dialog = new JDialog(parent);

        dialog.setTitle("Export Project");
        dialog.setResizable(false);
        dialog.setModal(true);

        libraryInfoDialog = new JDialog(dialog, "Library Info", Dialog.ModalityType.DOCUMENT_MODAL);
        libraryInfoDialog.setResizable(true);
        libraryInfoDialog.setLayout(new BorderLayout());
        libraryInfoDialog.add(createLibraryInfoHeader("Details for the selected export target."), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        dialog.add(tabs);

        JPanel buttonPane = new JPanel();
        dialog.add(buttonPane, BorderLayout.SOUTH);
        exportProgressBar = new JProgressBar();
        exportProgressBar.setVisible(false);
        exportProgressBar.setStringPainted(true);
        exportProgressBar.setString("Ready");
        exportProgressBar.setIndeterminate(false);
        exportProgressBar.putClientProperty("JComponent.minimumWidth", 260);
        exportProgressBar.setMinimumSize(new Dimension(220, exportProgressBar.getPreferredSize().height));
        exportButton = new JButton("Export");
        JButton cancelButton = new JButton("Cancel");
        exportButton.addActionListener(e -> export());
        cancelButton.addActionListener(e -> ExportDialog.this.setVisible(false));
        buttonPane.setLayout(new BorderLayout(10, 0));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel progressPane = new JPanel(new BorderLayout());
        progressPane.add(exportProgressBar, BorderLayout.CENTER);
        buttonPane.add(progressPane, BorderLayout.CENTER);

        JPanel actionButtonsPane = new JPanel();
        actionButtonsPane.setLayout(new BoxLayout(actionButtonsPane, BoxLayout.LINE_AXIS));
        actionButtonsPane.add(exportButton);
        actionButtonsPane.add(Box.createRigidArea(new Dimension(10, 0)));
        actionButtonsPane.add(cancelButton);
        buttonPane.add(actionButtonsPane, BorderLayout.EAST);

        SwingUtilities.updateComponentTreeUI(tabs);
        tabs.setUI(new FlatTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
            }
        });
        tabs.setBackground(Color.LIGHT_GRAY);

        // The following line enables to use scrolling tabs.
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // File tab
        JPanel filePane = new JPanel();
        filePane.setLayout(new BoxLayout(filePane, BoxLayout.LINE_AXIS));

        // Settings tab
        JPanel targetPane = new JPanel();
        targetPane.setLayout(new BorderLayout());

        // Assets tab
        JPanel assetsPane = new JPanel(new BorderLayout(0, 10));
        assetsPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        tabs.addTab("File", createExportTab("File", "Choose where to save the exported project.", filePane));

        tabs.addTab("Settings", createExportTab("Settings", "Configure the selected export target.", targetPane));

        tabs.addTab(
                "Assets", createExportTab("Assets", "Include additional files in the exported package.", assetsPane));

        for (ProjectExportPage page : this.contributedPages) {
            tabs.addTab(
                    page.getPageTitle(),
                    createExportTab(page.getPageTitle(), page.getPageDescription(), page.createPageComponent()));
        }

        // Configure File tab
        filePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        filePathTextField = new JTextField();
        filePathTextField.setMaximumSize(
                new Dimension((int) filePathTextField.getMaximumSize().getWidth(), 28));
        JButton fileButton = new JButton("...");
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileNameExtensionFilter currentFilter = null;
                JFileChooser dialog = new JFileChooser();
                dialog.setAcceptAllFileFilterUsed(false);
                for (int i = 0; i < builders.size(); i++) {
                    com.basic4gl.desktop.spi.Builder builder = (com.basic4gl.desktop.spi.Builder) builders.get(i);
                    FileNameExtensionFilter filter =
                            new FileNameExtensionFilter(builder.getFileDescription(), builder.getFileExtension());
                    if (i == currentBuilder) {
                        currentFilter = filter;
                    }
                    dialog.addChoosableFileFilter(filter);
                }
                if (currentFilter != null) {
                    dialog.setFileFilter(currentFilter);
                }

                int result = dialog.showSaveDialog(ExportDialog.this.dialog);

                if (result == JFileChooser.APPROVE_OPTION) {
                    String path = dialog.getSelectedFile().getAbsolutePath();
                    if (((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions().length > 0) {
                        // Append extension if needed
                        String extension = ((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions()[0];
                        if (!path.endsWith("." + extension)) {
                            path += "." + extension;
                        }
                    }

                    // Update file path
                    filePathTextField.setText(path);

                    // Change current target to match file extension if applicable
                    int index = Arrays.asList(dialog.getChoosableFileFilters()).indexOf(dialog.getFileFilter());
                    if (index != currentBuilder) {
                        builderComboBox.setSelectedIndex(index);
                    }
                }
            }
        });

        filePane.add(new JLabel("File name:"));
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(filePathTextField);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(fileButton);

        // Configure Settings tab
        JPanel targetSelectionPane = new JPanel(new GridBagLayout());
        targetPane.add(targetSelectionPane, BorderLayout.NORTH);
        targetSelectionPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints targetConstraints = new GridBagConstraints();
        targetConstraints.gridx = 0;
        targetConstraints.gridy = 0;
        targetConstraints.anchor = GridBagConstraints.WEST;
        targetConstraints.insets = new Insets(0, 0, 0, 10);
        targetSelectionPane.add(new JLabel("Target"), targetConstraints);

        builderComboBox = new JComboBox<>();
        targetConstraints.gridx = 1;
        targetConstraints.weightx = 1.0;
        targetConstraints.fill = GridBagConstraints.HORIZONTAL;
        targetSelectionPane.add(builderComboBox, targetConstraints);

        libraryInfoButton = new JButton("Library Info...");
        targetConstraints.gridx = 2;
        targetConstraints.weightx = 0;
        targetConstraints.fill = GridBagConstraints.NONE;
        targetConstraints.insets = new Insets(0, 10, 0, 0);
        targetSelectionPane.add(libraryInfoButton, targetConstraints);

        infoTextPane = new JTextPane();
        infoTextPane.setEditable(false);
        infoTextPane.setBackground(UIManager.getColor("Panel.background"));
        infoTextPane.setBorder(new EmptyBorder(8, 10, 8, 10));
        infoTextPane.setMargin(new Insets(4, 2, 4, 2));
        JScrollPane targetInfoScrollPane = new JScrollPane(infoTextPane);
        targetInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configureSmoothScrolling(targetInfoScrollPane);

        JPanel libraryInfoPane = new JPanel(new BorderLayout());
        libraryInfoPane.setBorder(new EmptyBorder(0, 12, 12, 12));
        libraryInfoPane.add(targetInfoScrollPane, BorderLayout.CENTER);
        libraryInfoDialog.add(libraryInfoPane, BorderLayout.CENTER);

        JButton closeLibraryInfoButton = new JButton("Close");
        closeLibraryInfoButton.addActionListener(e -> libraryInfoDialog.setVisible(false));
        JPanel libraryInfoFooter = new JPanel();
        libraryInfoFooter.setLayout(new BoxLayout(libraryInfoFooter, BoxLayout.LINE_AXIS));
        libraryInfoFooter.setBorder(new EmptyBorder(0, 12, 12, 12));
        libraryInfoFooter.add(Box.createHorizontalGlue());
        libraryInfoFooter.add(closeLibraryInfoButton);
        libraryInfoDialog.add(libraryInfoFooter, BorderLayout.SOUTH);
        libraryInfoDialog.setMinimumSize(new Dimension(420, 300));
        libraryInfoDialog.setSize(new Dimension(460, 320));

        JPanel propertiesPanel = new JPanel(new BorderLayout());
        propertiesPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JLabel propertiesLabel = new JLabel("Properties:");
        propertiesLabel.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
        propertiesPanel.add(propertiesLabel, BorderLayout.PAGE_START);

        configPane = new com.basic4gl.desktop.spi.ConfigurationFormPanel(this);
        configPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(configPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configureSmoothScrolling(targetPropertiesScrollPane);
        propertiesPanel.add(targetPropertiesScrollPane, BorderLayout.CENTER);
        targetPane.add(propertiesPanel, BorderLayout.CENTER);

        JLabel assetsDescription = new JLabel("Embedded files to include in exported package:");
        assetsPane.add(assetsDescription, BorderLayout.NORTH);

        embeddedAssetsModel = new DefaultListModel<>();
        embeddedAssetsList = new JList<>(embeddedAssetsModel);
        embeddedAssetsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane assetsScrollPane = new JScrollPane(embeddedAssetsList);
        configureSmoothScrolling(assetsScrollPane);
        assetsPane.add(assetsScrollPane, BorderLayout.CENTER);

        JPanel assetsButtonPane = new JPanel();
        assetsButtonPane.setLayout(new BoxLayout(assetsButtonPane, BoxLayout.LINE_AXIS));
        JButton addAssetButton = new JButton("Add...");
        JButton removeAssetButton = new JButton("Remove");
        JButton clearAssetsButton = new JButton("Clear");
        assetsButtonPane.add(addAssetButton);
        assetsButtonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        assetsButtonPane.add(removeAssetButton);
        assetsButtonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        assetsButtonPane.add(clearAssetsButton);
        assetsButtonPane.add(Box.createHorizontalGlue());
        assetsPane.add(assetsButtonPane, BorderLayout.SOUTH);

        addAssetButton.addActionListener(e -> promptAndAddAssets());
        removeAssetButton.addActionListener(e -> {
            java.util.List<String> selected = embeddedAssetsList.getSelectedValuesList();
            for (String value : selected) {
                embeddedAssetsModel.removeElement(value);
            }
        });
        clearAssetsButton.addActionListener(e -> embeddedAssetsModel.clear());

        libraryInfoButton.addActionListener(e -> {
            libraryInfoDialog.setLocationRelativeTo(dialog);
            libraryInfoDialog.setVisible(true);
        });

        builderComboBox.addActionListener(e -> {
            JComboBox<?> cb = (JComboBox<?>) e.getSource();
            if (cb == null) {
                return;
            }
            int builderIndex = cb.getSelectedIndex();
            selectBuilder(builderIndex);
        });
        // JScrollPane scrollPane = new ScrollPane(textLicenses);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 420));
        dialog.setSize(new Dimension(700, 480));
        dialog.setLocationRelativeTo(parent);
    }

    private JPanel createExportTab(String title, String description, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        Font baseFont = titleLabel.getFont();
        titleLabel.setFont(baseFont.deriveFont(Font.BOLD, baseFont.getSize() + 3f));
        titleLabel.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel descriptionLabel = new JLabel(description == null ? "" : description);
        descriptionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        header.add(titleLabel);
        header.add(descriptionLabel);

        panel.add(header, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLibraryInfoHeader(String description) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(12, 12, 8, 12));

        JLabel titleLabel = new JLabel("About Library");
        Font baseFont = titleLabel.getFont();
        titleLabel.setFont(baseFont.deriveFont(Font.BOLD, baseFont.getSize() + 4f));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        descriptionLabel.setBorder(new EmptyBorder(2, 0, 0, 0));

        header.add(titleLabel);
        header.add(descriptionLabel);
        return header;
    }

    private void configureSmoothScrolling(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.setWheelScrollingEnabled(true);
    }

    private void selectBuilder(int builderIndex) {
        if (builders == null || builders.isEmpty() || builderIndex < 0 || builderIndex >= builders.size()) {
            currentBuilder = -1;
            infoTextPane.setText("No export targets available.");
            configPane.removeAll();
            configPane.revalidate();
            configPane.repaint();
            setTargetSettingsEnabled(false);
            exportButton.setEnabled(false);
            return;
        }

        currentBuilder = builderIndex;
        Builder target = builders.get(currentBuilder);

        infoTextPane.setText(target.getDescription());
        infoTextPane.setCaretPosition(0);
        configPane.setConfiguration(new Configuration(target.getConfiguration()));
        for (ProjectExportPage page : contributedPages) {
            page.onBuilderSelected(target);
        }
        setTargetSettingsEnabled(true);
        exportButton.setEnabled(true);
    }

    private void setTargetSettingsEnabled(boolean enabled) {
        builderComboBox.setEnabled(enabled);
        libraryInfoButton.setEnabled(enabled);
        configPane.setEnabled(enabled);
    }

    public void setVisible(boolean visible) {
        dialog.setVisible(visible);
    }

    public void setBuilders(java.util.List<Builder> builders, int currentBuilder) {
        builderComboBox.removeAllItems();
        this.builders = builders;
        for (Builder builder : this.builders) {
            // TODO 6/2026 need to review why this cared about FunctionLibrary here - this should be handled elsewhere
            // already
            //            if (lib instanceof FunctionLibrary) {
            //                compiler.addConstants(((FunctionLibrary) lib).constants());
            //                compiler.addFunctions(lib, ((FunctionLibrary) lib).specs());
            //            }
            builderComboBox.addItem(builder.getName());
        }

        if (builders.isEmpty()) {
            this.currentBuilder = -1;
            selectBuilder(-1);
            return;
        }

        int selectedBuilder = currentBuilder;
        if (selectedBuilder < 0 || selectedBuilder >= builders.size()) {
            selectedBuilder = 0;
        }

        this.currentBuilder = selectedBuilder;
        builderComboBox.setSelectedIndex(selectedBuilder);
        selectBuilder(selectedBuilder);

        embeddedAssetsModel.clear();
        com.basic4gl.desktop.spi.Builder selected = (com.basic4gl.desktop.spi.Builder) builders.get(selectedBuilder);
        if (selected instanceof IAssetExportBuilder) {
            for (String asset : ((IAssetExportBuilder) selected).getExportAssets()) {
                embeddedAssetsModel.addElement(asset);
            }
        }
        mergeDetectedAssetsFromSource();
    }

    public int getCurrentBuilder() {
        return currentBuilder;
    }

    private void promptAndAddAssets() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        int result = chooser.showOpenDialog(dialog);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] files = chooser.getSelectedFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            if (file == null) {
                continue;
            }
            String normalizedPath = normalizeAssetPath(file);
            if (!embeddedAssetsModel.contains(normalizedPath)) {
                embeddedAssetsModel.addElement(normalizedPath);
            }
        }
    }

    private String normalizeAssetPath(File file) {
        Path selected = file.toPath().toAbsolutePath().normalize();
        Path baseDir = getExportBasePath();
        try {
            Path relative = baseDir.relativize(selected);
            String relPath = relative.toString().replace('\\', '/');
            if (!relPath.startsWith("..")) {
                return relPath;
            }
        } catch (IllegalArgumentException ignored) {
            // Different roots on some platforms: keep absolute path.
        }
        return selected.toString().replace('\\', '/');
    }

    private Path getExportBasePath() {
        if (exportBaseDirectory != null && !exportBaseDirectory.isBlank()) {
            return Paths.get(exportBaseDirectory).toAbsolutePath().normalize();
        }
        return Paths.get("").toAbsolutePath().normalize();
    }

    private void applySelectedAssets(com.basic4gl.desktop.spi.Builder builder) {
        if (!(builder instanceof IAssetExportBuilder)) {
            return;
        }

        IAssetExportBuilder assetBuilder = (IAssetExportBuilder) builder;
        assetBuilder.setExportAssetBaseDirectory(exportBaseDirectory);

        java.util.List<String> assets = new ArrayList<>();
        for (int i = 0; i < embeddedAssetsModel.size(); i++) {
            assets.add(embeddedAssetsModel.get(i));
        }
        assetBuilder.setExportAssets(assets);
    }

    private void mergeDetectedAssetsFromSource() {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (int i = 0; i < embeddedAssetsModel.size(); i++) {
            merged.add(embeddedAssetsModel.get(i));
        }
        merged.addAll(detectAssetsFromSourceLiterals());

        embeddedAssetsModel.clear();
        for (String asset : merged) {
            embeddedAssetsModel.addElement(asset);
        }
    }

    private java.util.List<String> detectAssetsFromSourceLiterals() {
        LinkedHashSet<String> detected = new LinkedHashSet<>();

        for (FileEditor editor : fileEditors) {
            String sourcePath = editor.getFilePath();
            if (sourcePath == null || sourcePath.isBlank()) {
                continue;
            }

            File parentDir = new File(sourcePath).getAbsoluteFile().getParentFile();
            File baseDir = getExportBasePath().toFile();
            String text = editor.getEditorPane().getText();
            if (text == null || text.isEmpty()) {
                continue;
            }

            for (String literal : languageService.extractStringLiterals(text)) {
                if (literal == null || literal.isBlank()) {
                    continue;
                }

                String normalizedLiteral = com.basic4gl.language.adapter.FileUtil.separatorsToSystem(literal);
                File candidate = new File(normalizedLiteral);
                if (!candidate.isAbsolute()) {
                    File baseCandidate = new File(baseDir, normalizedLiteral);
                    if (baseCandidate.exists() && baseCandidate.isFile()) {
                        candidate = baseCandidate;
                    } else {
                        candidate = parentDir == null ? baseCandidate : new File(parentDir, normalizedLiteral);
                    }
                }

                if (candidate.exists() && candidate.isFile()) {
                    detected.add(normalizeAssetPath(candidate));
                }
            }
        }

        return new ArrayList<>(detected);
    }

    private void export() {
        try {
            File dest;
            int decision;

            if (currentBuilder < 0 || builders == null || currentBuilder >= builders.size()) {
                JOptionPane.showMessageDialog(dialog, "No export target selected.");
                return;
            }

            com.basic4gl.desktop.spi.Builder builder = builders.get(currentBuilder);

            configPane.applyConfig();
            mergeDetectedAssetsFromSource();
            applySelectedAssets(builder);
            for (ProjectExportPage page : contributedPages) {
                page.onExport(builder);
            }

            if (!filePathTextField.getText().isEmpty()) {
                dest = new File(filePathTextField.getText());
                if (dest.isDirectory()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a filename.");
                    return;
                }
                if (!filePathTextField.getText().endsWith("." + builder.getFileExtension())) {
                    dest = new File(filePathTextField.getText() + "." + builder.getFileExtension());
                }

                if (dest.exists()) {
                    Object[] options = {"Yes", "No"};
                    decision = JOptionPane.showOptionDialog(
                            dialog,
                            "File already exists! Overwrite?",
                            "Confirm",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[1]);
                    if (decision == 1) {
                        return;
                    }
                }
                enableComponents(tabs, false);
                exportButton.setEnabled(false);
                setExportInProgress(true, "Preparing export...");
                lastExportDestination = dest;
                ExportWorker export = new ExportWorker(compiler, builder, dest, new ExportCallback());
                export.execute();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please enter a filename.");
                return;
            }
        } catch (Exception e1) {
            enableComponents(tabs, true);
            exportButton.setEnabled(true);
            setExportInProgress(false);
            e1.printStackTrace();
        }
    }

    private void setExportInProgress(boolean inProgress) {
        setExportInProgress(inProgress, inProgress ? "Exporting..." : "Ready");
    }

    private void setExportInProgress(boolean inProgress, String status) {
        exportProgressBar.setVisible(inProgress);
        exportProgressBar.setIndeterminate(inProgress);
        setExportStatus(status);
    }

    private void setExportStatus(String status) {
        if (status == null || status.isBlank()) {
            status = "Exporting...";
        }
        exportProgressBar.setString(status);
    }

    private String getShowInFileManagerLabel() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return "Show in Explorer";
        }
        if (osName.contains("mac")) {
            return "Show in Finder";
        }
        return "Show in File Manager";
    }

    private void showExportSuccessDialog() {
        String revealLabel = getShowInFileManagerLabel();
        Object[] options = {"OK", revealLabel};
        int result = JOptionPane.showOptionDialog(
                dialog,
                "Export successful!",
                "Success",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

        // User chose the OS-specific "Show in ..." action.
        if (result == 1) {
            openExportInFileManager();
        }
    }

    private void openExportInFileManager() {
        try {
            if (lastExportDestination == null) {
                return;
            }

            File target = lastExportDestination;
            File directory = target.isDirectory() ? target : target.getParentFile();
            if (directory == null || !directory.exists()) {
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(directory);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    dialog,
                    "Export succeeded, but the destination folder could not be opened.",
                    "Open Folder Failed",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        if (currentBuilder < 0 || builders == null || currentBuilder >= builders.size()) {
            return;
        }

        com.basic4gl.desktop.spi.Builder builder = builders.get(currentBuilder);

        builder.setConfiguration(configuration);
    }

    private class ExportWorker extends SwingWorker<Object, CallbackMessage> {
        private final CompilerService compiler;
        private final com.basic4gl.desktop.spi.Builder builder;
        private final File dest;
        private final ExportCallback exportCallback;
        private CallbackMessage callbackMessage;

        public ExportWorker(
                CompilerService compiler,
                com.basic4gl.desktop.spi.Builder builder,
                File dest,
                ExportCallback callback) {
            this.compiler = compiler;
            this.builder = builder;
            this.dest = dest;
            exportCallback = callback;
        }

        @Override
        protected void done() {
            int success;
            if (exportCallback != null) {
                publish(callbackMessage);
            }
        }

        @Override
        protected void process(java.util.List<CallbackMessage> chunks) {
            for (CallbackMessage message : chunks) {
                exportCallback.message(message);
            }
        }

        @Override
        protected Object doInBackground() throws Exception {
            callbackMessage = new CallbackMessage(CallbackMessage.WORKING, "");
            publish(new CallbackMessage(CallbackMessage.WORKING, "Compiling source..."));

            if (fileEditors.isEmpty()) {
                callbackMessage.setMessage(CallbackMessage.FAILED, "No files are open");
                return false;
            }

            if (!compile()) {
                callbackMessage.setMessage(CallbackMessage.FAILED, compiler.getError());
                return null; // TODO Throw error
            }
            publish(new CallbackMessage(CallbackMessage.WORKING, "Packaging export archive..."));
            // Export to file
            FileOutputStream stream = new FileOutputStream(dest);

            try {
                boolean success = builder.export(dest.getName(), stream, exportCallback);

                if (success) {
                    callbackMessage.setMessage(CallbackMessage.SUCCESS, "Exported successful");
                } else {
                    callbackMessage.setMessage(CallbackMessage.FAILED, "Export failed");
                }
            } catch (Exception e) {
                e.printStackTrace();

                callbackMessage.setMessage(CallbackMessage.FAILED, e.getMessage());
            }

            return null;
        }

        // Program control
        private boolean compile() {

            // Clear source code from parser
            compiler.clear();

            // Load code into preprocessor; may be unnecessary
            if (!loadProgramIntoCompiler()) {
                callbackMessage.setMessage(CallbackMessage.FAILED, preprocessor.getError());
                return false;
            }

            // Compile
            compiler.clearError();
            compiler.compile();

            // Return result
            if (compiler.hasError()) {
                callbackMessage.setMessage(CallbackMessage.FAILED, compiler.getError());
                return false;
            }

            // Reset Virtual machine
            // mVM.Reset ();

            callbackMessage.setMessage(CallbackMessage.WORKING, "User's code compiled");
            return true;
        }

        // Compilation and execution routines
        private boolean loadProgramIntoCompiler() {
            // TODO Get editor assigned as main file
            return preprocessor.preprocess(new EditorSourceFile(
                    fileEditors.get(0).getEditorPane(), fileEditors.get(0).getFilePath()));
        }
    }

    public void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container) component, enable);
            }
        }
    }

    public class ExportCallback implements TaskCallback {

        @Override
        public void message(CallbackMessage message) {
            if (message.getStatus() == CallbackMessage.WORKING) {
                String statusText = message.getText();
                setExportStatus(statusText == null || statusText.isBlank() ? "Exporting..." : statusText);
                return;
            }
            if (message.getStatus() == CallbackMessage.SUCCESS) {
                System.out.println("Export successful.");
                setExportStatus("Export complete");
                showExportSuccessDialog();
                dialog.setVisible(false);
            } else if (message.getStatus() == CallbackMessage.FAILED) {
                System.out.println("Export failed.");
                setExportStatus("Export failed");
                JOptionPane.showMessageDialog(dialog, message.getText(), "Export failed.", JOptionPane.ERROR_MESSAGE);
            }
            setExportInProgress(false);
            enableComponents(tabs, true);
            exportButton.setEnabled(true);
        }
    }
}
