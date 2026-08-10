package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.desktop.spi.ProjectSettingsPage;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JvmProjectSettingsPage implements ProjectSettingsPage {
    private final IConfigurableAppSettings appSettings;

    private JTextArea jvmArgumentsTextArea;
    private JCheckBox enableJvmDebugCheckbox;
    private JCheckBox waitForAttachCheckbox;
    private JLabel debugPortLabel;
    private JTextField debugPortField;
    private JLabel debugPortHintLabel;
    private JLabel debugPortErrorLabel;
    private JComponent pageComponent;

    public JvmProjectSettingsPage(IConfigurableAppSettings appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    public String getPageId() {
        return "jvm-settings";
    }

    @Override
    public String getPageTitle() {
        return "JVM Settings";
    }

    @Override
    public String getPageDescription() {
        return "Configure JVM launch options for .jar targets.";
    }

    @Override
    public JComponent createPageComponent() {
        if (pageComponent != null) {
            return pageComponent;
        }

        JPanel page = new JPanel(new BorderLayout(0, 12));
        JPanel advancedBody = new JPanel(new BorderLayout(0, 12));

        jvmArgumentsTextArea = new JTextArea();
        jvmArgumentsTextArea.setLineWrap(false);
        jvmArgumentsTextArea.setTabSize(4);
        jvmArgumentsTextArea.setText(String.join(System.lineSeparator(), appSettings.getJvmArguments()));

        JScrollPane jvmArgumentsScrollPane = new JScrollPane(jvmArgumentsTextArea);
        configureSmoothScrolling(jvmArgumentsScrollPane);
        advancedBody.add(createTitledPanel("JVM Exec Options", jvmArgumentsScrollPane), BorderLayout.CENTER);

        JPanel debugOptionsPanel = new JPanel(new GridBagLayout());
        debugOptionsPanel.setBorder(new EmptyBorder(6, 8, 6, 8));
        GridBagConstraints debugOptionsConstraints = new GridBagConstraints();
        debugOptionsConstraints.gridx = 0;
        debugOptionsConstraints.gridy = 0;
        debugOptionsConstraints.gridwidth = 2;
        debugOptionsConstraints.anchor = GridBagConstraints.WEST;
        debugOptionsConstraints.insets = new Insets(0, 0, 8, 0);

        enableJvmDebugCheckbox = new JCheckBox("Enable JDWP debugger");
        enableJvmDebugCheckbox.setSelected(appSettings.isJvmDebuggingEnabled());
        debugOptionsPanel.add(enableJvmDebugCheckbox, debugOptionsConstraints);

        debugOptionsConstraints.gridy++;
        waitForAttachCheckbox = new JCheckBox("Suspend until debugger attaches");
        waitForAttachCheckbox.setSelected(appSettings.isJvmDebugSuspendUntilAttach());
        debugOptionsPanel.add(waitForAttachCheckbox, debugOptionsConstraints);

        debugOptionsConstraints.gridy++;
        debugOptionsConstraints.gridwidth = 1;
        debugOptionsConstraints.insets = new Insets(0, 0, 0, 10);
        debugPortLabel = new JLabel("Debug Port (Optional)");
        debugOptionsPanel.add(debugPortLabel, debugOptionsConstraints);

        debugOptionsConstraints.gridx = 1;
        debugOptionsConstraints.weightx = 1.0;
        debugOptionsConstraints.fill = GridBagConstraints.HORIZONTAL;
        debugPortField = new JTextField();
        Integer jvmDebugPortOverride = appSettings.getJvmDebugPortOverride();
        debugPortField.setText(jvmDebugPortOverride == null ? "" : Integer.toString(jvmDebugPortOverride));
        debugPortField.setToolTipText("Leave empty to auto-select a free debug port for each run session.");
        debugOptionsPanel.add(debugPortField, debugOptionsConstraints);

        debugOptionsConstraints.gridx = 1;
        debugOptionsConstraints.gridy++;
        debugOptionsConstraints.weightx = 1.0;
        debugOptionsConstraints.fill = GridBagConstraints.HORIZONTAL;
        debugOptionsConstraints.insets = new Insets(4, 0, 0, 0);
        debugPortHintLabel = new JLabel("Leave blank to auto-select an available port each run.");
        debugPortHintLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        debugOptionsPanel.add(debugPortHintLabel, debugOptionsConstraints);

        debugOptionsConstraints.gridy++;
        debugOptionsConstraints.insets = new Insets(6, 0, 0, 0);
        debugPortErrorLabel = new JLabel(" ");
        debugPortErrorLabel.setForeground(new Color(176, 0, 32));
        debugOptionsPanel.add(debugPortErrorLabel, debugOptionsConstraints);

        debugPortField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                clearDebugPortError();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                clearDebugPortError();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                clearDebugPortError();
            }
        });

        enableJvmDebugCheckbox.addActionListener(e -> {
            if (!enableJvmDebugCheckbox.isSelected()) {
                clearDebugPortError();
            }
            updateJvmDebugControlsEnabled();
        });
        updateJvmDebugControlsEnabled();

        advancedBody.add(createTitledPanel("Debugger", debugOptionsPanel), BorderLayout.SOUTH);
        page.add(advancedBody, BorderLayout.CENTER);

        JButton resetAdvancedDefaultsButton = new JButton("Reset To Defaults");
        resetAdvancedDefaultsButton.addActionListener(e -> resetAdvancedDefaults());
        JPanel advancedFooter = new JPanel(new BorderLayout());
        advancedFooter.add(resetAdvancedDefaultsButton, BorderLayout.WEST);
        page.add(advancedFooter, BorderLayout.SOUTH);

        pageComponent = page;
        return pageComponent;
    }

    private JPanel createTitledPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setBorder(new EmptyBorder(4, 4, 0, 4));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void configureSmoothScrolling(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.setWheelScrollingEnabled(true);
    }

    private void clearDebugPortError() {
        if (debugPortErrorLabel != null) {
            debugPortErrorLabel.setText(" ");
        }
    }

    private void showDebugPortError(String message) {
        if (debugPortErrorLabel != null) {
            debugPortErrorLabel.setText(message == null || message.trim().isEmpty() ? "Invalid debug port." : message);
        }
    }

    private void updateJvmDebugControlsEnabled() {
        boolean jvmDebuggingEnabled = enableJvmDebugCheckbox.isSelected();
        waitForAttachCheckbox.setEnabled(jvmDebuggingEnabled);
        debugPortLabel.setEnabled(jvmDebuggingEnabled);
        debugPortField.setEnabled(jvmDebuggingEnabled);
        debugPortHintLabel.setEnabled(jvmDebuggingEnabled);
    }

    private void resetAdvancedDefaults() {
        jvmArgumentsTextArea.setText("");
        enableJvmDebugCheckbox.setSelected(false);
        waitForAttachCheckbox.setSelected(false);
        debugPortField.setText("");
        clearDebugPortError();
        updateJvmDebugControlsEnabled();
    }

    private Integer parseOptionalPortOrThrow(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            int port = Integer.parseInt(text.trim());
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Debug port must be between 1 and 65535, or left blank.");
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Debug port must be a whole number between 1 and 65535, or left blank.");
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

    @Override
    public void onApply() {
        Integer parsedDebugPort = appSettings.getJvmDebugPortOverride();
        if (enableJvmDebugCheckbox.isSelected()) {
            try {
                parsedDebugPort = parseOptionalPortOrThrow(debugPortField.getText());
            } catch (IllegalArgumentException ex) {
                Integer previousPort = appSettings.getJvmDebugPortOverride();
                debugPortField.setText(previousPort == null ? "" : Integer.toString(previousPort));
                showDebugPortError(ex.getMessage());
                debugPortField.requestFocusInWindow();
                debugPortField.selectAll();
                throw ex;
            }
        }
        clearDebugPortError();

        appSettings.setJvmArguments(parseProgramArguments(jvmArgumentsTextArea.getText()));
        appSettings.setJvmDebuggingEnabled(enableJvmDebugCheckbox.isSelected());
        appSettings.setJvmDebugSuspendUntilAttach(waitForAttachCheckbox.isSelected());
        appSettings.setJvmDebugPortOverride(parsedDebugPort);
    }

    @Override
    public int getSortOrder() {
        return 210;
    }
}
