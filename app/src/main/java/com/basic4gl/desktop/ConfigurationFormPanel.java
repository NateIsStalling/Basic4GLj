package com.basic4gl.desktop;

import com.basic4gl.lib.util.Configuration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class ConfigurationFormPanel extends JPanel {

    private final IOnConfigurationChangeListener listener;

    public ConfigurationFormPanel(IOnConfigurationChangeListener listener) {
        super();
        this.listener = listener;
    }

    private final JPanel configPane = this;
    private final java.util.List<JComponent> settingComponents = new ArrayList<JComponent>();

    private Configuration currentConfig;

    @Override
    public void removeAll() {
        super.removeAll();
        settingComponents.clear();
    }

    public void addConfigurationField(String[] field, int paramType, String value) {
        JPanel wrapper;
        JLabel label;

        //Override parameter type if field contains multiple values
        if (field.length > 1) {
            paramType = Configuration.PARAM_CHOICE;
        }

        switch (paramType) {
            case Configuration.PARAM_HEADING:
                wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
                label = new JLabel(field[0]);
                Font font = label.getFont();
                label.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
                label.setForeground(new Color(66, 66, 66));
                label.setBorder(new EmptyBorder(6, 6, 6, 6));

                wrapper.add(label);
                configPane.add(wrapper);
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
    public void setConfiguration(Configuration configuration) {
        currentConfig = configuration;
        //Load settings
        removeAll();

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

    public void applyConfig(){
        String val;
        if (currentConfig == null) {
            return;
        }
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
        listener.OnConfigurationChanged(currentConfig);
    }
    public static interface IOnConfigurationChangeListener {

        public void OnConfigurationChanged(Configuration configuration);
    }
}
