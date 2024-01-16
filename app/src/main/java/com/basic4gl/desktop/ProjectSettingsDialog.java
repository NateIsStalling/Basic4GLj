package com.basic4gl.desktop;

import com.basic4gl.lib.util.Builder;
import com.basic4gl.lib.util.Configuration;
import com.basic4gl.lib.util.Library;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Nate on 2/5/2015.
 */
public class ProjectSettingsDialog {

    JDialog dialog;

    JComboBox builderComboBox;
    JLabel builderDescriptionLabel;

    JTextPane infoTextPane;
    JPanel configPane;
    //Libraries
    private java.util.List<Library> libraries;
    private java.util.List<Integer> builders;        //Indexes of libraries that can be launch targets
    private int currentBuilder;            //Index value of target

    private final java.util.List<JComponent> settingComponents = new ArrayList<JComponent>();
    private Configuration currentConfig;

    public ProjectSettingsDialog(Frame parent) {
        dialog = new JDialog(parent);

        dialog.setTitle("Project Settings");
        dialog.setResizable(false);
        dialog.setModal(true);

        JTabbedPane tabbedPane = new JTabbedPane();
        dialog.add(tabbedPane);

        JPanel buttonPane = new JPanel();
        dialog.add(buttonPane, BorderLayout.SOUTH);
        JButton applyButton = new JButton("Apply");
        JButton acceptButton = new JButton("Accept");
        JButton cancelButton = new JButton("Cancel");
        applyButton.addActionListener(e -> {
            if (currentBuilder != -1) {
                applyConfig();
            }
        });
        acceptButton.addActionListener(e -> {
            if (currentBuilder != -1) {
                applyConfig();
            }
            ProjectSettingsDialog.this.setVisible(false);
        });
        cancelButton.addActionListener(e -> ProjectSettingsDialog.this.setVisible(false));

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(applyButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(acceptButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);


        SwingUtilities.updateComponentTreeUI(tabbedPane);
        tabbedPane.setUI(new FlatTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
            }
        });
        tabbedPane.setBackground(Color.LIGHT_GRAY);

        // The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JPanel buildPane = new JPanel();
        buildPane.setLayout(new BorderLayout());
        tabbedPane.addTab("Build", buildPane);

        JPanel targetSelectionPane = new JPanel();
        buildPane.add(targetSelectionPane, BorderLayout.NORTH);
        targetSelectionPane.setLayout(new BoxLayout(targetSelectionPane, BoxLayout.LINE_AXIS));
        targetSelectionPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        targetSelectionPane.add(new JLabel("Target"));
        builderComboBox = new JComboBox();
        builderComboBox.setBorder(new EmptyBorder(0, 10, 0, 10));
        targetSelectionPane.add(builderComboBox);

        JPanel buildInfoPane = new JPanel();
        buildPane.add(buildInfoPane, BorderLayout.CENTER);
        GridLayout buildInfoPaneLayout = new GridLayout(1, 2);
        buildInfoPane.setLayout(buildInfoPaneLayout);

        JPanel infoPanel = new JPanel();
        buildInfoPane.add(infoPanel);

        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JLabel infoLabel = new JLabel("Library Info:");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(infoLabel, BorderLayout.PAGE_START);
        infoTextPane = new JTextPane();
        //mInfoTextPane.setBackground(Color.LIGHT_GRAY);
        infoTextPane.setEditable(false);
        JScrollPane targetInfoScrollPane = new JScrollPane(infoTextPane);
        targetInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoPanel.add(targetInfoScrollPane, BorderLayout.CENTER);

        JPanel propertiesPanel = new JPanel();
        buildInfoPane.add(propertiesPanel);

        propertiesPanel.setLayout(new BorderLayout());
        propertiesPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JLabel propertiesLabel = new JLabel("Configuration:");
        propertiesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        propertiesPanel.add(propertiesLabel, BorderLayout.PAGE_START);
        configPane = new JPanel();
        //mConfigPane.setBackground(Color.LIGHT_GRAY);

        configPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(configPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanel.add(targetPropertiesScrollPane, BorderLayout.CENTER);
        configPane.setLayout(new BoxLayout(configPane, BoxLayout.Y_AXIS));
        configPane.setAlignmentX(0f);

/*
        JPanel descriptionPanel = new JPanel();
        infoPanel.add(descriptionPanel);
        BoxLayout descriptionLayout = new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS);

        descriptionPanel.setLayout(descriptionLayout);
        descriptionPanel.setBorder(new EmptyBorder(10, 5, 10, 5));

        mBuilderDescriptionLabel = new JLabel(MainWindow.APPLICATION_DESCRIPTION);
        mBuilderDescriptionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        mBuilderDescriptionLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        descriptionPanel.add(mBuilderDescriptionLabel);

        descriptionPanel.add(Box.createVerticalGlue());
*/

        builderComboBox.addActionListener(e -> {
            JComboBox cb = (JComboBox) e.getSource();
            if (cb == null) {
                return;
            }
            int builderIndex = cb.getSelectedIndex();
            selectBuilder(builderIndex);
        });

        //JScrollPane scrollPane = new ScrollPane(textLicenses);
        dialog.pack();
        dialog.setSize(new Dimension(464, 346));
        dialog.setLocationRelativeTo(parent);
    }

    private void selectBuilder(int builderIndex) {
        currentBuilder = builderIndex;
        Library builder = libraries.get(builders.get(currentBuilder));

        //TODO Display target info
        infoTextPane.setText(builder.description());
        //Load settings
        settingComponents.clear();
        configPane.removeAll();
        currentConfig = new Configuration(((Builder) builder).getConfiguration());
        String[] field;
        int paramType;
        String configValue;

        // Create form input for configuration
        for (int i = 0; i < currentConfig.getSettingCount(); i++) {
            field = currentConfig.getField(i);
            paramType = currentConfig.getParamType(i);
            configValue = currentConfig.getValue(i);

            addConfigurationField(field, paramType, configValue);
        }
    }

    private void addConfigurationField(String[] field, int paramType, String value) {
        JLabel label;

        //Override parameter type if field contains multiple values
        if (field.length > 1) {
            paramType = Configuration.PARAM_CHOICE;
        }

        switch (paramType) {
            case Configuration.PARAM_HEADING:
                label = new JLabel(field[0]);
                Font font = label.getFont();
                label.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
                label.setBorder(new EmptyBorder(6, 6, 6, 6));
                configPane.add(label);
                settingComponents.add(null);
                break;
            case Configuration.PARAM_DIVIDER:
                configPane.add(Box.createVerticalStrut(16));
                configPane.add(new JSeparator(JSeparator.HORIZONTAL));
                configPane.add(Box.createVerticalStrut(2));
                settingComponents.add(null);
                break;
            case Configuration.PARAM_STRING:
                label = new JLabel(field[0]);
                label.setAlignmentX(0f);
                label.setBorder(new EmptyBorder(4, 4, 4, 4));
                configPane.add(label);
                JTextField textField = new JTextField(value);
                textField.setBorder(new EmptyBorder(4, 4, 4, 4));
                settingComponents.add(textField);
                configPane.add(textField);
                break;
            case Configuration.PARAM_BOOL:
                JCheckBox checkBox = new JCheckBox(field[0]);
                checkBox.setAlignmentX(0f);
                checkBox.setSelected(Boolean.valueOf(value));
                checkBox.setBorder(new EmptyBorder(4, 4, 4, 4));
                settingComponents.add(checkBox);
                configPane.add(checkBox);
                break;
            case Configuration.PARAM_INT:
                label = new JLabel(field[0]);
                label.setAlignmentX(0f);
                configPane.add(label);
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(Integer.valueOf(value).intValue(), 0, Short.MAX_VALUE, 1));
                settingComponents.add(spinner);
                configPane.add(spinner);
                break;
            case Configuration.PARAM_CHOICE:
                label = new JLabel(field[0]);
                label.setAlignmentX(0f);
                label.setBorder(new EmptyBorder(4, 4, 4, 4));
                configPane.add(label);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                JComboBox comboBox = new JComboBox();
                for (int j = 1; j < field.length; j++) {
                    comboBox.addItem(field[j]);
                }
                comboBox.setSelectedIndex(Integer.valueOf(value));
                settingComponents.add(comboBox);
                configPane.add(comboBox);
                break;
        }
    }

    private void applyConfig(){
        String val;
        if (currentConfig == null) {
            return;
        }
        Builder builder = (Builder) libraries.get(builders.get(currentBuilder));
        int param;
        for (int i = 0; i < currentConfig.getSettingCount(); i++) {
            param = currentConfig.getParamType(i);
            val = "0";
            if (settingComponents.get(i) == null) {
                continue;
            }
            switch (param) {
                case Configuration.PARAM_STRING:
                    val = ((JTextField) settingComponents.get(i)).getText();
                    break;
                case Configuration.PARAM_INT:
                    val = String.valueOf(((JSpinner) settingComponents.get(i)).getValue());
                    break;
                case Configuration.PARAM_BOOL:
                    val = String.valueOf(((JCheckBox) settingComponents.get(i)).isSelected());
                    break;
                case Configuration.PARAM_CHOICE:
                    val = String.valueOf(((JComboBox) settingComponents.get(i)).getSelectedIndex());
                    break;
            }
            currentConfig.setValue(i, val);
        }
        builder.setConfiguration(currentConfig);
    }
    public void setVisible(boolean visible){
        dialog.setVisible(visible);
    }

    public void setLibraries(java.util.List<Library> libraries, int currentBuilder){
        builderComboBox.removeAllItems();
        this.currentBuilder = currentBuilder;
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
        builderComboBox.setSelectedIndex(currentBuilder);
    }


    public int getCurrentBuilder(){
        return currentBuilder;
    }


}
