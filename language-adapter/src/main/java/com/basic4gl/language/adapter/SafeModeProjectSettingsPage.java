package com.basic4gl.language.adapter;

import com.basic4gl.app.desktop.config.IConfigurableAppSettings;
import com.basic4gl.desktop.spi.ProjectSettingsPage;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SafeModeProjectSettingsPage implements ProjectSettingsPage {
    private final IConfigurableAppSettings appSettings;
    private JCheckBox safeModeCheckbox;
    private JComponent pageComponent;

    public SafeModeProjectSettingsPage(IConfigurableAppSettings appSettings) {
        this.appSettings = appSettings;
    }

    @Override
    public String getPageId() {
        return "safe-mode";
    }

    @Override
    public String getPageTitle() {
        return "Safe Mode";
    }

    @Override
    public String getPageDescription() {
        return "Control filesystem restrictions for programs you run in the editor.";
    }

    @Override
    public JComponent createPageComponent() {
        if (pageComponent != null) {
            return pageComponent;
        }

        Locale locale = new Locale("en", "US");
        ResourceBundle resources = ResourceBundle.getBundle("labels", locale);

        JPanel safeModeSettingsCard = new JPanel(new BorderLayout(0, 12));
        JTextPane safeModeDescriptionTextPane = new JTextPane();
        safeModeDescriptionTextPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        safeModeDescriptionTextPane.setEditable(false);
        safeModeDescriptionTextPane.setBackground(UIManager.getColor("Panel.background"));
        safeModeDescriptionTextPane.setText(resources.getString("safeModeDescription"));
        safeModeDescriptionTextPane.setCaretPosition(0);

        JScrollPane safeModeSettingsScrollPane = new JScrollPane(safeModeDescriptionTextPane);
        safeModeSettingsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        configureSmoothScrolling(safeModeSettingsScrollPane);
        safeModeSettingsCard.add(safeModeSettingsScrollPane, BorderLayout.CENTER);

        safeModeCheckbox = new JCheckBox(resources.getString("safeModeCheckbox"));
        safeModeCheckbox.setSelected(appSettings.isSandboxModeEnabled());
        safeModeCheckbox.setBorder(new EmptyBorder(8, 8, 8, 8));
        JPanel safeModeFooter = new JPanel(new BorderLayout());
        safeModeFooter.add(safeModeCheckbox, BorderLayout.WEST);
        safeModeSettingsCard.add(safeModeFooter, BorderLayout.SOUTH);

        pageComponent = safeModeSettingsCard;
        return pageComponent;
    }

    private void configureSmoothScrolling(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.setWheelScrollingEnabled(true);
    }

    @Override
    public void onApply() {
        if (safeModeCheckbox != null) {
            appSettings.setSandboxModeEnabled(safeModeCheckbox.isSelected());
        }
    }

    @Override
    public int getSortOrder() {
        return 200;
    }
}
