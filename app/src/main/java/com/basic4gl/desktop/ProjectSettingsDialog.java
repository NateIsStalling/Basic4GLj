package com.basic4gl.desktop;

import com.basic4gl.lib.util.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Created by Nate on 2/5/2015.
 */
public class ProjectSettingsDialog implements ConfigurationFormPanel.IOnConfigurationChangeListener {

    private static final String BUILD_SETTINGS_CARD = "Build Settings";
    private static final String SAFE_MODE_CARD = "Safe Mode";

    private final JDialog dialog;
    private final JDialog libraryInfoDialog;

    private final JComboBox<String> builderComboBox;
    private final JButton libraryInfoButton;

    private final JTextPane infoTextPane;

    // Libraries
    private java.util.List<Library> libraries;
    private java.util.List<Integer> builders; // Indexes of libraries that can be launch targets
    private int currentBuilder; // Index value of target

    private final ConfigurationFormPanel configPane;

    private final IConfigurableAppSettings appSettings;

    public ProjectSettingsDialog(Frame parent, IConfigurableAppSettings appSettings) {

        this.appSettings = appSettings;

        Locale locale = new Locale("en", "US");
        ResourceBundle resources = ResourceBundle.getBundle("labels", locale);

        dialog = new JDialog(parent);

        dialog.setTitle("Project Settings");
        dialog.setResizable(true);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        // Library info sub-dialog
        libraryInfoDialog = new JDialog(dialog, "Library Info", Dialog.ModalityType.DOCUMENT_MODAL);
        libraryInfoDialog.setResizable(true);
        libraryInfoDialog.setLayout(new BorderLayout());
        libraryInfoDialog.add(createLibraryInfoHeader("Details for the selected build target."), BorderLayout.NORTH);

        infoTextPane = new JTextPane();
        infoTextPane.setEditable(false);
        infoTextPane.setBackground(UIManager.getColor("Panel.background"));
        infoTextPane.setBorder(new EmptyBorder(8, 10, 8, 10));
        infoTextPane.setMargin(new Insets(4, 2, 4, 2));
        JScrollPane libraryInfoScrollPane = new JScrollPane(infoTextPane);
        libraryInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        libraryInfoScrollPane.setBorder(new EmptyBorder(0, 12, 12, 12));
        configureSmoothScrolling(libraryInfoScrollPane);
        libraryInfoDialog.add(libraryInfoScrollPane, BorderLayout.CENTER);

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

        JPanel contentPane = new JPanel(new BorderLayout());
        dialog.add(contentPane, BorderLayout.CENTER);

        DefaultListModel<String> sections = new DefaultListModel<>();
        sections.addElement(BUILD_SETTINGS_CARD);
        sections.addElement(SAFE_MODE_CARD);
        JList<String> sectionsList = new JList<>(sections);
        sectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionsList.setFixedCellHeight(30);
        sectionsList.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel cardsPane = new JPanel(new CardLayout());

        // Build settings card
        JPanel buildSettingsCard = new JPanel(new BorderLayout(0, 12));
        buildSettingsCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        buildSettingsCard.add(
                createSectionHeader("Build Settings", "Select a build target and configure its export options."),
                BorderLayout.NORTH);

        JPanel buildSettingsBody = new JPanel(new BorderLayout(0, 12));

        JPanel targetSelectionPane = new JPanel(new GridBagLayout());
        targetSelectionPane.setBorder(new EmptyBorder(6, 8, 6, 8));
        GridBagConstraints targetConstraints = new GridBagConstraints();

        targetConstraints.gridx = 0;
        targetConstraints.gridy = 0;
        targetConstraints.anchor = GridBagConstraints.WEST;
        targetConstraints.insets = new Insets(0, 0, 0, 10);
        targetSelectionPane.add(new JLabel("Target"), targetConstraints);

        targetConstraints.gridx = 1;
        targetConstraints.weightx = 1.0;
        targetConstraints.fill = GridBagConstraints.HORIZONTAL;
        targetConstraints.insets = new Insets(0, 0, 0, 10);
        builderComboBox = new JComboBox<>();
        targetSelectionPane.add(builderComboBox, targetConstraints);

        targetConstraints.gridx = 2;
        targetConstraints.weightx = 0;
        targetConstraints.fill = GridBagConstraints.NONE;
        targetConstraints.insets = new Insets(0, 0, 0, 0);
        libraryInfoButton = new JButton("Library Info...");
        libraryInfoButton.addActionListener(e -> {
            libraryInfoDialog.setLocationRelativeTo(dialog);
            libraryInfoDialog.setVisible(true);
        });
        targetSelectionPane.add(libraryInfoButton, targetConstraints);
        buildSettingsBody.add(targetSelectionPane, BorderLayout.NORTH);

        configPane = new ConfigurationFormPanel(this);
        configPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(configPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configureSmoothScrolling(targetPropertiesScrollPane);
        buildSettingsBody.add(createTitledPanel("Configuration", targetPropertiesScrollPane), BorderLayout.CENTER);

        buildSettingsCard.add(buildSettingsBody, BorderLayout.CENTER);

        // Safe mode settings card
        JPanel safeModeSettingsCard = new JPanel(new BorderLayout(0, 12));
        safeModeSettingsCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        safeModeSettingsCard.add(
                createSectionHeader("Safe Mode", "Control filesystem restrictions for programs you run in the editor."),
                BorderLayout.NORTH);

        JTextPane safeModeDescriptionTextPane = new JTextPane();
        safeModeDescriptionTextPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        safeModeDescriptionTextPane.setEditable(false);
        safeModeDescriptionTextPane.setBackground(UIManager.getColor("Panel.background"));
        safeModeDescriptionTextPane.setText(resources.getString("safeModeDescription"));

        JScrollPane safeModeSettingsScrollPane = new JScrollPane(safeModeDescriptionTextPane);
        safeModeSettingsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configureSmoothScrolling(safeModeSettingsScrollPane);
        safeModeSettingsCard.add(createTitledPanel("Details", safeModeSettingsScrollPane), BorderLayout.CENTER);

        JCheckBox safeModeCheckbox = new JCheckBox(resources.getString("safeModeCheckbox"));
        safeModeCheckbox.setSelected(appSettings.isSandboxModeEnabled());
        safeModeCheckbox.setBorder(new EmptyBorder(8, 8, 8, 8));
        JPanel safeModeFooter = new JPanel(new BorderLayout());
        safeModeFooter.add(safeModeCheckbox, BorderLayout.WEST);
        safeModeSettingsCard.add(safeModeFooter, BorderLayout.SOUTH);

        cardsPane.add(buildSettingsCard, BUILD_SETTINGS_CARD);
        cardsPane.add(safeModeSettingsCard, SAFE_MODE_CARD);

        JScrollPane sectionsScrollPane = new JScrollPane(sectionsList);
        sectionsScrollPane.setBorder(new MatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground")));
        sectionsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configureSmoothScrolling(sectionsScrollPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sectionsScrollPane, cardsPane);
        splitPane.setDividerLocation(160);
        splitPane.setResizeWeight(0);
        splitPane.setBorder(null);
        contentPane.add(splitPane, BorderLayout.CENTER);

        sectionsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            String selectedSection = sectionsList.getSelectedValue();
            if (selectedSection == null) {
                return;
            }
            ((CardLayout) cardsPane.getLayout()).show(cardsPane, selectedSection);
        });
        sectionsList.setSelectedIndex(0);

        // Buttons
        JPanel buttonPane = new JPanel();
        dialog.add(buttonPane, BorderLayout.SOUTH);
        JButton applyButton = new JButton("Apply");
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(applyButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(okButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);

        // Action listeners
        applyButton.addActionListener(e -> applyChanges(safeModeCheckbox, false));
        okButton.addActionListener(e -> applyChanges(safeModeCheckbox, true));
        cancelButton.addActionListener(e -> ProjectSettingsDialog.this.setVisible(false));

        builderComboBox.addActionListener(e -> {
            JComboBox<?> cb = (JComboBox<?>) e.getSource();
            if (cb == null) {
                return;
            }
            selectBuilder(cb.getSelectedIndex());
        });

        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 420));
        dialog.setSize(new Dimension(700, 480));
        dialog.setLocationRelativeTo(parent);
    }

    private JPanel createSectionHeader(String title, String description) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        Font baseFont = titleLabel.getFont();
        titleLabel.setFont(baseFont.deriveFont(Font.BOLD, baseFont.getSize() + 3f));
        titleLabel.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        header.add(titleLabel);
        header.add(descriptionLabel);
        return header;
    }

    private JPanel createTitledPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));

        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setBorder(new EmptyBorder(4, 4, 0, 4));
        panel.add(titleLabel, BorderLayout.NORTH);
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

    private void applyChanges(JCheckBox safeModeCheckbox, boolean closeDialog) {
        if (currentBuilder >= 0) {
            configPane.applyConfig();
        }

        appSettings.setSandboxModeEnabled(safeModeCheckbox.isSelected());
        if (closeDialog) {
            setVisible(false);
        }
    }

    private void selectBuilder(int builderIndex) {
        if (builders == null || builders.isEmpty() || builderIndex < 0 || builderIndex >= builders.size()) {
            currentBuilder = -1;
            infoTextPane.setText("No build targets available.");
            configPane.removeAll();
            configPane.revalidate();
            configPane.repaint();
            setBuildSettingsEnabled(false);
            return;
        }

        currentBuilder = builderIndex;
        Library target = libraries.get(builders.get(currentBuilder));

        infoTextPane.setText(target.description());
        infoTextPane.setCaretPosition(0);
        configPane.setConfiguration(new Configuration(((Builder) target).getConfiguration()));
        setBuildSettingsEnabled(true);
    }

    private void setBuildSettingsEnabled(boolean enabled) {
        builderComboBox.setEnabled(enabled);
        libraryInfoButton.setEnabled(enabled);
        configPane.setEnabled(enabled);
    }

    public void setVisible(boolean visible) {
        dialog.setVisible(visible);
    }

    public void setLibraries(java.util.List<Library> libraries, int currentBuilder) {
        builderComboBox.removeAllItems();
        this.libraries = libraries;
        builders = new ArrayList<>();

        int i = 0;
        for (Library lib : this.libraries) {
            if (lib instanceof Builder) {
                builders.add(i);
                builderComboBox.addItem(this.libraries.get(i).name());
            }
            i++;
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
    }

    public int getCurrentBuilder() {
        return currentBuilder;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        if (currentBuilder < 0 || builders == null || currentBuilder >= builders.size()) {
            return;
        }

        Builder builder = (Builder) libraries.get(builders.get(currentBuilder));

        builder.setConfiguration(configuration);
    }
}
