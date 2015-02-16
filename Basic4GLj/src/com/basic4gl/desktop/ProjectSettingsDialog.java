package com.basic4gl.desktop;

import com.basic4gl.lib.util.Configuration;
import com.basic4gl.lib.util.Library;
import com.basic4gl.lib.util.Target;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by Nate on 2/5/2015.
 */
public class ProjectSettingsDialog {

    JDialog mDialog;

    JComboBox mTargetComboBox;
    JLabel mTargetDescriptionLabel;
    JLabel mTargetRunnableLabel;

    JTextPane mTargetInfoTextPane;
    JPanel mTargetConfigPane;
    //Libraries
    private java.util.List<Library> mLibraries;
    private java.util.List<Integer> mTargets;        //Indexes of libraries that can be launch targets
    private int mCurrentTarget;            //Index value of target in mTargets

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
                if (mCurrentTarget != -1)
                    applyConfig();
            }
        });
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mCurrentTarget != -1)
                    applyConfig();
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
        tabbedPane.setUI(new BasicTabbedPaneUI() {
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
        mTargetComboBox = new JComboBox();
        mTargetComboBox.setBorder(new EmptyBorder(0, 10, 0, 10));
        targetSelectionPane.add(mTargetComboBox);

        mTargetRunnableLabel = new JLabel(MainWindow.APPLICATION_DESCRIPTION);
        mTargetRunnableLabel.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 12));
        mTargetRunnableLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        targetSelectionPane.add(mTargetRunnableLabel);

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
        mTargetInfoTextPane = new JTextPane();
        //mTargetInfoTextPane.setBackground(Color.LIGHT_GRAY);
        mTargetInfoTextPane.setEditable(false);
        JScrollPane targetInfoScrollPane = new JScrollPane(mTargetInfoTextPane);
        targetInfoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoPanel.add(targetInfoScrollPane, BorderLayout.CENTER);

        JPanel propertiesPanel = new JPanel();
        buildInfoPane.add(propertiesPanel);

        propertiesPanel.setLayout(new BorderLayout());
        propertiesPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JLabel propertiesLabel = new JLabel("Properties:");
        propertiesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        propertiesPanel.add(propertiesLabel, BorderLayout.PAGE_START);
        mTargetConfigPane = new JPanel();
        //mTargetConfigPane.setBackground(Color.LIGHT_GRAY);

        mTargetConfigPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(mTargetConfigPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanel.add(targetPropertiesScrollPane, BorderLayout.CENTER);
        mTargetConfigPane.setLayout(new BoxLayout(mTargetConfigPane, BoxLayout.Y_AXIS));
        mTargetConfigPane.setAlignmentX(0f);

/*
        JPanel descriptionPanel = new JPanel();
        infoPanel.add(descriptionPanel);
        BoxLayout descriptionLayout = new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS);

        descriptionPanel.setLayout(descriptionLayout);
        descriptionPanel.setBorder(new EmptyBorder(10, 5, 10, 5));

        mTargetDescriptionLabel = new JLabel(MainWindow.APPLICATION_DESCRIPTION);
        mTargetDescriptionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        mTargetDescriptionLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        descriptionPanel.add(mTargetDescriptionLabel);

        descriptionPanel.add(Box.createVerticalGlue());
*/



        mTargetComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                if (cb == null)
                    return;
                mCurrentTarget = cb.getSelectedIndex();
                Library target = mLibraries.get(mTargets.get(mCurrentTarget));

                mTargetRunnableLabel.setText(((Target)target).isRunnable() ?
                    "Build target is runnable":
                    "Build target is NOT runnable");

                //TODO Display target info
                mTargetInfoTextPane.setText(target.description());
                //Load settings
                mSettingComponents.clear();
                mTargetConfigPane.removeAll();
                mCurrentConfig = new Configuration(((Target) target).getConfiguration());
                String[] field;
                int param;
                String val;
                for (int i = 0; i < mCurrentConfig.getSettingCount(); i++){
                    JLabel label;
                    field = mCurrentConfig.getField(i);
                    param = mCurrentConfig.getParamType(i);
                    val = mCurrentConfig.getValue(i);

                    //Override parameter type if field contains multiple values
                    if (field.length > 1)
                        param = Configuration.PARAM_CHOICE;

                    switch (param){
                        case Configuration.PARAM_STRING:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            label.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mTargetConfigPane.add(label);
                            JTextField textField = new JTextField(val);
                            textField.setBorder(new EmptyBorder(4, 4,4,4));
                            mSettingComponents.add(textField);
                            mTargetConfigPane.add(textField);
                            break;
                        case Configuration.PARAM_BOOL:
                            JCheckBox checkBox = new JCheckBox(field[0]);
                            checkBox.setAlignmentX(0f);
                            checkBox.setSelected(Boolean.valueOf(val));
                            checkBox.setBorder(new EmptyBorder(4, 4, 4, 4));
                            mSettingComponents.add(checkBox);
                            mTargetConfigPane.add(checkBox);
                            break;
                        case Configuration.PARAM_INT:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            mTargetConfigPane.add(label);
                            JSpinner spinner = new JSpinner(new SpinnerNumberModel(Integer.valueOf(val).intValue(), 0, Short.MAX_VALUE, 1 ));
                            mSettingComponents.add(spinner);
                            mTargetConfigPane.add(spinner);
                            break;
                        case Configuration.PARAM_CHOICE:
                            label = new JLabel(field[0]);
                            label.setAlignmentX(0f);
                            label.setBorder(new EmptyBorder(4,4,4,4));
                            mTargetConfigPane.add(label);
                            label.setHorizontalAlignment(SwingConstants.LEFT);
                            JComboBox comboBox = new JComboBox();
                            for (int j = 1; j<field.length; j++)
                                comboBox.addItem(field[j]);
                            mSettingComponents.add(comboBox);
                            mTargetConfigPane.add(comboBox);
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
        if (mCurrentConfig == null)
            return;
        Target target = (Target)mLibraries.get(mTargets.get(mCurrentTarget));
        int param;
        for (int i = 0; i < mCurrentConfig.getSettingCount(); i++) {
            param = mCurrentConfig.getParamType(i);
            val = "0";
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
        target.setConfiguration(mCurrentConfig);
    }
    public void setVisible(boolean visible){
        mDialog.setVisible(visible);
    }

    public void setLibraries(java.util.List<Library> libraries, java.util.List<Integer> targets, int currentTarget){
        mLibraries = libraries;
        mTargets = targets;
        mCurrentTarget = currentTarget;

        mTargetComboBox.removeAllItems();
        for (Integer i: mTargets)
            mTargetComboBox.addItem(mLibraries.get(i).name());
        mTargetComboBox.setSelectedIndex(currentTarget);
    }


    public int getCurrentTarget(){
        return mCurrentTarget;
    }


}
