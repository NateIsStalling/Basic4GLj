package com.basic4gl.desktop;

import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.desktop.spi.Builder;
import com.basic4gl.desktop.spi.Configuration;
import com.basic4gl.desktop.spi.ProjectSettingsPage;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class ProjectSettingsDialog
        implements com.basic4gl.desktop.spi.ConfigurationFormPanel.IOnConfigurationChangeListener {

    private static final String BUILD_SETTINGS_CARD = "Build Settings";
    private static final String PROGRAM_ARGUMENTS_CARD = "Program Arguments";

    private final JDialog dialog;
    private final JDialog libraryInfoDialog;
    private JComboBox<String> builderComboBox;
    private JButton libraryInfoButton;
    private final JTextPane infoTextPane;
    private com.basic4gl.desktop.spi.ConfigurationFormPanel configPane;
    private final IConfigurableAppSettings appSettings;
    private final List<ProjectSettingsPage> contributedPages;
    private final Runnable onSettingsApplied;

    private List<Builder> builders;
    private int currentBuilder;

    public ProjectSettingsDialog(
            Frame parent,
            IConfigurableAppSettings appSettings,
            List<ProjectSettingsPage> contributedProjectSettingsPages,
            Runnable onSettingsApplied) {
        this.appSettings = appSettings;
        this.onSettingsApplied = onSettingsApplied;
        this.contributedPages = new ArrayList<>(contributedProjectSettingsPages);
        this.contributedPages.sort(Comparator.comparingInt(ProjectSettingsPage::getSortOrder)
                .thenComparing(ProjectSettingsPage::getPageTitle, String.CASE_INSENSITIVE_ORDER));

        dialog = new JDialog(parent);
        dialog.setTitle("Project Settings");
        dialog.setResizable(true);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

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

        DefaultListModel<SectionItem> sections = new DefaultListModel<>();
        sections.addElement(new SectionItem("Build Settings", BUILD_SETTINGS_CARD));
        sections.addElement(new SectionItem("Program Arguments", PROGRAM_ARGUMENTS_CARD));
        for (ProjectSettingsPage page : this.contributedPages) {
            sections.addElement(new SectionItem(page.getPageTitle(), cardIdForPage(page)));
        }
        JList<SectionItem> sectionsList = new JList<>(sections);
        sectionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionsList.setFixedCellHeight(30);
        sectionsList.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel cardsPane = new JPanel(new CardLayout());

        JPanel buildSettingsCard = createBuildSettingsCard();
        JTextArea argumentsTextArea = new JTextArea();
        JPanel programArgumentsCard = createProgramArgumentsCard(argumentsTextArea);

        cardsPane.add(buildSettingsCard, BUILD_SETTINGS_CARD);
        cardsPane.add(programArgumentsCard, PROGRAM_ARGUMENTS_CARD);

        for (ProjectSettingsPage page : this.contributedPages) {
            cardsPane.add(createContributedCard(page), cardIdForPage(page));
        }

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
            SectionItem selectedSection = sectionsList.getSelectedValue();
            if (selectedSection == null) {
                return;
            }
            ((CardLayout) cardsPane.getLayout()).show(cardsPane, selectedSection.cardId);
        });
        sectionsList.setSelectedIndex(0);

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

        applyButton.addActionListener(e -> applyChanges(argumentsTextArea, false));
        okButton.addActionListener(e -> applyChanges(argumentsTextArea, true));
        cancelButton.addActionListener(e -> setVisible(false));

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

    private JPanel createBuildSettingsCard() {
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

        configPane = new com.basic4gl.desktop.spi.ConfigurationFormPanel(this);
        configPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(configPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configureSmoothScrolling(targetPropertiesScrollPane);
        buildSettingsBody.add(createTitledPanel("Configuration", targetPropertiesScrollPane), BorderLayout.CENTER);

        buildSettingsCard.add(buildSettingsBody, BorderLayout.CENTER);
        return buildSettingsCard;
    }

    private JPanel createProgramArgumentsCard(JTextArea argumentsTextArea) {
        JPanel programArgumentsCard = new JPanel(new BorderLayout(0, 12));
        programArgumentsCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        programArgumentsCard.add(
                createSectionHeader(
                        "Program Arguments", "Enter one argument per line to pass to programs run from the IDE."),
                BorderLayout.NORTH);

        argumentsTextArea.setLineWrap(false);
        argumentsTextArea.setTabSize(4);
        argumentsTextArea.setText(String.join(System.lineSeparator(), appSettings.getProgramArguments()));

        JScrollPane argumentsScrollPane = new JScrollPane(argumentsTextArea);
        configureSmoothScrolling(argumentsScrollPane);
        programArgumentsCard.add(createTitledPanel("Arguments", argumentsScrollPane), BorderLayout.CENTER);
        return programArgumentsCard;
    }

    private JPanel createContributedCard(ProjectSettingsPage page) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.add(createSectionHeader(page.getPageTitle(), page.getPageDescription()), BorderLayout.NORTH);
        card.add(page.createPageComponent(), BorderLayout.CENTER);
        return card;
    }

    private String cardIdForPage(ProjectSettingsPage page) {
        return "contrib:" + page.getPageId();
    }

    private JPanel createSectionHeader(String title, String description) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        Font baseFont = titleLabel.getFont();
        titleLabel.setFont(baseFont.deriveFont(Font.BOLD, baseFont.getSize() + 3f));
        titleLabel.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel descriptionLabel = new JLabel(Objects.requireNonNullElse(description, ""));
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

    private void applyChanges(JTextArea argumentsTextArea, boolean closeDialog) {
        if (currentBuilder >= 0) {
            configPane.applyConfig();
        }

        appSettings.setProgramArguments(parseProgramArguments(argumentsTextArea.getText()));

        try {
            for (ProjectSettingsPage page : contributedPages) {
                page.onApply();
            }
            if (onSettingsApplied != null) {
                onSettingsApplied.run();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    dialog, ex.getMessage(), "Invalid Project Settings", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (closeDialog) {
            setVisible(false);
        }
    }

    private List<String> parseProgramArguments(String text) {
        List<String> args = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return args;
        }

        String[] lines = text.split("\\R", -1);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            args.add(line.trim());
        }
        return args;
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
        Builder target = builders.get(currentBuilder);

        infoTextPane.setText(target.getDescription());
        infoTextPane.setCaretPosition(0);
        configPane.setConfiguration(new Configuration(target.getConfiguration()));
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

    public void setBuilders(List<Builder> builders, int currentBuilder) {
        builderComboBox.removeAllItems();
        this.builders = builders;

        for (Builder builder : this.builders) {
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
    }

    public int getCurrentBuilder() {
        return currentBuilder;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        if (currentBuilder < 0 || builders == null || currentBuilder >= builders.size()) {
            return;
        }

        Builder builder = builders.get(currentBuilder);
        builder.setConfiguration(configuration);
    }

    private static class SectionItem {
        private final String label;
        private final String cardId;

        private SectionItem(String label, String cardId) {
            this.label = label;
            this.cardId = cardId;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
