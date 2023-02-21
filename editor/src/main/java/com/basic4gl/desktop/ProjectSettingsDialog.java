package com.basic4gl.desktop;

import com.basic4gl.lib.util.Builder;
import com.basic4gl.lib.util.Configuration;
import com.basic4gl.lib.util.Library;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by Nate on 2/5/2015.
 */
public class ProjectSettingsDialog {

    JDialog mDialog;

    JComboBox mBuilderComboBox;
    JLabel mBuilderDescriptionLabel;

    JTextPane mInfoTextPane;
    JPanel mConfigPane;
    //Libraries
    private java.util.List<Library> mLibraries;
    private java.util.List<Integer> mBuilders;        //Indexes of libraries that can be launch targets
    private int mCurrentBuilder;            //Index value of target in mTargets

    private java.util.List<JComponent> mSettingComponents = new ArrayList<JComponent>();
    private Configuration mCurrentConfig;
    public ProjectSettingsDialog(Frame parent) {
        mDialog = new JDialog(parent);

        mDialog.setTitle("Project Settings");
        mDialog.setResizable(false);
        mDialog.setModal(true);

        JTabbedPane tabbedPane = new JTabbedPane();
        mDialog.add(tabbedPane);

        JPanel buttonPane = new JPanel();
        mDialog.add(buttonPane, BorderLayout.SOUTH);
        JButton applyButton = new JButton("Apply");
        JButton acceptButton = new JButton("Accept");
        JButton cancelButton = new JButton("Cancel");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mCurrentBuilder != -1) {
                    applyConfig();
                }
            }
        });
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mCurrentBuilder != -1) {
                    applyConfig();
                }
                ProjectSettingsDialog.this.setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectSettingsDialog.this.setVisible(false);
            }
        });
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
        mBuilderComboBox = new JComboBox();
        mBuilderComboBox.setBorder(new EmptyBorder(0, 10, 0, 10));
        targetSelectionPane.add(mBuilderComboBox);

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
        mInfoTextPane = new JTextPane();
        //mInfoTextPane.setBackground(Color.LIGHT_GRAY);
        mInfoTextPane.setEditable(false);
        JScrollPane targetInfoScrollPane = new JScrollPane(mInfoTextPane);
        targetInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoPanel.add(targetInfoScrollPane, BorderLayout.CENTER);

        JPanel propertiesPanel = new JPanel();
        buildInfoPane.add(propertiesPanel);

        propertiesPanel.setLayout(new BorderLayout());
        propertiesPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JLabel propertiesLabel = new JLabel("Configuration:");
        propertiesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        propertiesPanel.add(propertiesLabel, BorderLayout.PAGE_START);
        mConfigPane = new JPanel();
        //mConfigPane.setBackground(Color.LIGHT_GRAY);

        mConfigPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(mConfigPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanel.add(targetPropertiesScrollPane, BorderLayout.CENTER);
        mConfigPane.setLayout(new BoxLayout(mConfigPane, BoxLayout.Y_AXIS));
        mConfigPane.setAlignmentX(0f);

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



        mBuilderComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                if (cb == null) {
                    return;
                }
                mCurrentBuilder = cb.getSelectedIndex();
                Library builder = mLibraries.get(mBuilders.get(mCurrentBuilder));

                //TODO Display target info
                mInfoTextPane.setText(builder.description());
                //Load settings
                mSettingComponents.clear();
                mConfigPane.removeAll();
                mCurrentConfig = new Configuration(((Builder) builder).getConfiguration());
                String[] field;
                int param;
                String val;
                for (int i = 0; i < mCurrentConfig.getSettingCount(); i++) {
                    JLabel label;
                    field = mCurrentConfig.getField(i);
                    param = mCurrentConfig.getParamType(i);
                    val = mCurrentConfig.getValue(i);

                    //Override parameter type if field contains multiple values
                    if (field.length > 1) {
                        param = Configuration.PARAM_CHOICE;
                    }

                    switch (param) {
                        case Configuration.PARAM_HEADING:
                            label = new JLabel(field[0]);
                            Font font = label.getFont();
                            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
                            label.setBorder(new EmptyBorder(6, 6, 6, 6));
                            mConfigPane.add(label);
                            mSettingComponents.add(null);
                            break;
                        case Configuration.PARAM_DIVIDER:
                            mConfigPane.add(Box.createVerticalStrut(16));
                            mConfigPane.add(new JSeparator(JSeparator.HORIZONTAL));
                            mConfigPane.add(Box.createVerticalStrut(2));
                            mSettingComponents.add(null);
                            break;
                        case Configuration.PARAM_STRING:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            label.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mConfigPane.add(label);
                            JTextField textField = new JTextField(val);
                            textField.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mSettingComponents.add(textField);
                            mConfigPane.add(textField);
                            break;
                        case Configuration.PARAM_BOOL:
                            JCheckBox checkBox = new JCheckBox(field[0]);
                            checkBox.setAlignmentX(0f);
                            checkBox.setSelected(Boolean.valueOf(val));
                            checkBox.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mSettingComponents.add(checkBox);
                            mConfigPane.add(checkBox);
                            break;
                        case Configuration.PARAM_INT:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            mConfigPane.add(label);
                            JSpinner spinner = new JSpinner(new SpinnerNumberModel(Integer.valueOf(val).intValue(), 0, Short.MAX_VALUE, 1));
                            mSettingComponents.add(spinner);
                            mConfigPane.add(spinner);
                            break;
                        case Configuration.PARAM_CHOICE:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            label.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mConfigPane.add(label);
                            label.setHorizontalAlignment(SwingConstants.LEFT);
                            JComboBox comboBox = new JComboBox();
                            for (int j = 1; j < field.length; j++) {
                                comboBox.addItem(field[j]);
                            }
                            comboBox.setSelectedIndex(Integer.valueOf(val));
                            mSettingComponents.add(comboBox);
                            mConfigPane.add(comboBox);
                            break;
                    }
                }
            }
        });
        //JScrollPane scrollPane = new ScrollPane(textLicenses);
        mDialog.pack();
        mDialog.setSize(new Dimension(464, 346));
        mDialog.setLocationRelativeTo(parent);
    }

    private void applyConfig(){
        String val;
        if (mCurrentConfig == null) {
            return;
        }
        Builder builder = (Builder)mLibraries.get(mBuilders.get(mCurrentBuilder));
        int param;
        for (int i = 0; i < mCurrentConfig.getSettingCount(); i++) {
            param = mCurrentConfig.getParamType(i);
            val = "0";
            if (mSettingComponents.get(i) == null) {
                continue;
            }
            switch (param) {
                case Configuration.PARAM_STRING:
                    val = ((JTextField)mSettingComponents.get(i)).getText();
                    break;
                case Configuration.PARAM_INT:
                    val = String.valueOf(((JSpinner) mSettingComponents.get(i)).getValue());
                    break;
                case Configuration.PARAM_BOOL:
                    val = String.valueOf(((JCheckBox) mSettingComponents.get(i)).isSelected());
                    break;
                case Configuration.PARAM_CHOICE:
                    val = String.valueOf(((JComboBox)mSettingComponents.get(i)).getSelectedIndex());
                    break;
            }
            mCurrentConfig.setValue(i, val);
        }
        builder.setConfiguration(mCurrentConfig);
    }
    public void setVisible(boolean visible){
        mDialog.setVisible(visible);
    }

    public void setLibraries(java.util.List<Library> libraries, int currentBuilder){
        mBuilderComboBox.removeAllItems();
        mCurrentBuilder = currentBuilder;
        mLibraries = libraries;
        mBuilders = new ArrayList<>();
        int i = 0;
        for (Library lib : mLibraries) {
            if (lib instanceof Builder) {
                mBuilders.add(i);
                mBuilderComboBox.addItem(mLibraries.get(i).name());
            }
            i++;
        }
        mBuilderComboBox.setSelectedIndex(currentBuilder);
    }


    public int getCurrentBuilder(){
        return mCurrentBuilder;
    }


}
