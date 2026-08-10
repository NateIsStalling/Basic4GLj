package com.basic4gl.desktop.spi;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ConfigurationFormPanel extends JPanel implements Scrollable {

    private static final int LABEL_COLUMN_WIDTH = 150;

    private final IOnConfigurationChangeListener listener;
    private final java.util.List<JComponent> settingComponents = new ArrayList<>();
    private Configuration currentConfig;
    private int nextRow = 0;

    public ConfigurationFormPanel(IOnConfigurationChangeListener listener) {
        super(new GridBagLayout());
        this.listener = listener;
    }

    @Override
    public void removeAll() {
        super.removeAll();
        settingComponents.clear();
        nextRow = 0;
    }

    public void addConfigurationField(String[] field, int paramType, String value, String infoText) {
        if (field.length > 1) {
            paramType = Configuration.PARAM_CHOICE;
        }

        switch (paramType) {
            case Configuration.PARAM_HEADING:
                JLabel heading = new JLabel(field[0]);
                Font font = heading.getFont();
                heading.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
                heading.setForeground(new Color(66, 66, 66));
                JPanel headingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                headingRow.setBorder(new EmptyBorder(6, 6, 6, 6));
                headingRow.add(heading);
                addFullWidthRow(headingRow);
                settingComponents.add(null);
                break;
            case Configuration.PARAM_DIVIDER:
                addFullWidthRow(Box.createVerticalStrut(12));
                addFullWidthRow(new JSeparator(JSeparator.HORIZONTAL));
                addFullWidthRow(Box.createVerticalStrut(2));
                settingComponents.add(null);
                break;
            case Configuration.PARAM_STRING:
                JTextField textField = new JTextField(value);
                textField.setMinimumSize(new Dimension(80, textField.getPreferredSize().height));
                textField.setToolTipText(infoText);
                settingComponents.add(textField);
                addLabeledEditorRow(field[0], textField, infoText);
                break;
            case Configuration.PARAM_BOOL:
                JCheckBox checkBox = new JCheckBox(field[0]);
                checkBox.setSelected(Boolean.parseBoolean(value));
                checkBox.setBorder(new EmptyBorder(4, 4, 4, 4));
                checkBox.setToolTipText(infoText);
                JPanel boolRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                boolRow.setBorder(new EmptyBorder(2, 4, 2, 4));
                boolRow.add(checkBox);
                settingComponents.add(checkBox);
                addFullWidthRow(boolRow);
                addInfoTextRow(infoText);
                break;
            case Configuration.PARAM_INT:
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(Integer.parseInt(value), 0, Short.MAX_VALUE, 1));
                spinner.setPreferredSize(new Dimension(110, spinner.getPreferredSize().height));
                spinner.setMinimumSize(new Dimension(80, spinner.getPreferredSize().height));
                spinner.setToolTipText(infoText);
                settingComponents.add(spinner);
                addLabeledEditorRow(field[0], spinner, infoText);
                break;
            case Configuration.PARAM_CHOICE:
                JComboBox<String> comboBox = new JComboBox<>();
                for (int j = 1; j < field.length; j++) {
                    comboBox.addItem(field[j]);
                }
                comboBox.setSelectedIndex(Integer.parseInt(value));
                comboBox.setMinimumSize(new Dimension(100, comboBox.getPreferredSize().height));
                comboBox.setToolTipText(infoText);
                settingComponents.add(comboBox);
                addLabeledEditorRow(field[0], comboBox, infoText);
                break;
        }
    }

    private void addLabeledEditorRow(String labelText, JComponent editor, String infoText) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBorder(new EmptyBorder(3, 2, 3, 2));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(2, 0, 2, 8);

        JLabel label = new JLabel(labelText);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new Dimension(LABEL_COLUMN_WIDTH, label.getPreferredSize().height));
        label.setToolTipText(infoText == null || infoText.isBlank() ? labelText : infoText);
        row.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        editor.setMaximumSize(new Dimension(Integer.MAX_VALUE, editor.getPreferredSize().height));
        row.add(editor, gbc);

        addFullWidthRow(row);
        addInfoTextRow(infoText);
    }

    private void addInfoTextRow(String infoText) {
        if (infoText == null || infoText.isBlank()) {
            return;
        }
        JLabel info = new JLabel("<html><div style='text-align: left;'>" + infoText + "</div></html>");
        info.setHorizontalAlignment(SwingConstants.LEFT);
        info.setForeground(UIManager.getColor("Label.disabledForeground"));
        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setBorder(new EmptyBorder(0, 6, 6, 6));
        infoRow.add(info, BorderLayout.CENTER);
        addFullWidthRow(infoRow);
    }

    private void addFullWidthRow(Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = nextRow++;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(component, gbc);
    }

    public void setConfiguration(Configuration configuration) {
        currentConfig = configuration;
        removeAll();

        for (int i = 0; i < currentConfig.getSettingCount(); i++) {
            addConfigurationField(
                    currentConfig.getField(i),
                    currentConfig.getParamType(i),
                    currentConfig.getValue(i),
                    currentConfig.getFieldInfoText(i));
        }

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = nextRow;
        filler.weightx = 1.0;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.BOTH;
        add(Box.createGlue(), filler);

        revalidate();
        repaint();
    }

    public void applyConfig() {
        if (currentConfig == null) {
            return;
        }

        for (int i = 0; i < currentConfig.getSettingCount(); i++) {
            if (settingComponents.get(i) == null) {
                continue;
            }

            String val = "0";
            switch (currentConfig.getParamType(i)) {
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
                    val = String.valueOf(((JComboBox<?>) settingComponents.get(i)).getSelectedIndex());
                    break;
            }
            currentConfig.setValue(i, val);
        }

        listener.onConfigurationChanged(currentConfig);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(visibleRect.height - 16, 16);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public interface IOnConfigurationChangeListener {
        void onConfigurationChanged(Configuration configuration);
    }
}
