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
public class ProjectSettingsDialog implements ConfigurationFormPanel.IOnConfigurationChangeListener {

    private final JDialog dialog;

    private final JComboBox builderComboBox;

    private final JTextPane infoTextPane;
    //Libraries
    private java.util.List<Library> libraries;
    private java.util.List<Integer> builders;        //Indexes of libraries that can be launch targets
    private int currentBuilder;            //Index value of target

    private ConfigurationFormPanel configPane;

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


        configPane = new ConfigurationFormPanel(this);
        //mConfigPane.setBackground(Color.LIGHT_GRAY);

        configPane.setBorder(new EmptyBorder(4, 4, 4, 4));
        JScrollPane targetPropertiesScrollPane = new JScrollPane(configPane);
        targetPropertiesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanel.add(targetPropertiesScrollPane, BorderLayout.CENTER);
        configPane.setLayout(new BoxLayout(configPane, BoxLayout.Y_AXIS));
        configPane.setAlignmentX(0f);


        applyButton.addActionListener(e -> {
            if (currentBuilder != -1) {
                configPane.applyConfig();
            }
        });
        acceptButton.addActionListener(e -> {
            if (currentBuilder != -1) {
                configPane.applyConfig();
            }
            ProjectSettingsDialog.this.setVisible(false);
        });
        cancelButton.addActionListener(e -> ProjectSettingsDialog.this.setVisible(false));

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
        Library target = libraries.get(builders.get(currentBuilder));

        //TODO Display target info
        infoTextPane.setText(target.description());
        configPane.setConfiguration(new Configuration(((Builder) target).getConfiguration()));
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


    @Override
    public void OnConfigurationChanged(Configuration configuration) {
        Builder builder = (Builder) libraries.get(builders.get(currentBuilder));

        builder.setConfiguration(configuration);
    }
}
