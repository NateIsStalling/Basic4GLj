package com.basic4gl.desktop;

import com.basic4gl.desktop.util.SwingIconUtil;
import java.awt.*;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Created by Nate on 1/12/2015.
 */
public class AboutDialog {
    private final JDialog dialog;

    public AboutDialog(Frame parent) {
        Locale locale = new Locale("en", "US");
        ResourceBundle resources = ResourceBundle.getBundle("build", locale);

        dialog = new JDialog(parent);

        dialog.setTitle("About");

        dialog.setResizable(false);
        dialog.setModal(true);

        JPanel mainPanel = new JPanel();
        dialog.add(mainPanel);
        GridLayout mainLayout = new GridLayout(2, 1);
        mainPanel.setLayout(mainLayout);

        JPanel infoPanel = new JPanel();
        mainPanel.add(infoPanel);
        GridLayout infoLayout1 = new GridLayout(1, 2);
        infoPanel.setLayout(infoLayout1);
        JLabel labelLogo = new JLabel(SwingIconUtil.createImageIcon(BuildInfo.ICON_LOGO_LARGE));
        infoPanel.add(labelLogo);

        JPanel descriptionPanel = new JPanel();
        infoPanel.add(descriptionPanel);
        BoxLayout descriptionLayout = new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS);

        descriptionPanel.setLayout(descriptionLayout);
        descriptionPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
        descriptionPanel.add(new JLabel(BuildInfo.APPLICATION_NAME));
        JLabel labelDescription = new JLabel(BuildInfo.APPLICATION_DESCRIPTION);
        labelDescription.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 12));
        labelDescription.setBorder(new EmptyBorder(5, 5, 5, 5));
        descriptionPanel.add(labelDescription);
        descriptionPanel.add(new JLabel("Version: " + resources.getString(BuildInfo.APPLICATION_VERSION_RESOURCE)));
        descriptionPanel.add(
                new JLabel("Build Date: " + resources.getString(BuildInfo.APPLICATION_BUILD_DATE_RESOURCE)));
        descriptionPanel.add(Box.createVerticalGlue());
        descriptionPanel.add(new JLabel(BuildInfo.APPLICATION_COPYRIGHT));
        descriptionPanel.add(new JLabel(BuildInfo.APPLICATION_WEBSITE));

        JTextPane textLicenses = new JTextPane();
        JScrollPane scrollLicenses = new JScrollPane(textLicenses);
        scrollLicenses.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textLicenses.setEditable(false);

        java.net.URL helpURL = ClassLoader.getSystemClassLoader().getResource("about.html");
        if (helpURL != null) {
            try {
                textLicenses.setPage(helpURL);
            } catch (IOException e) {
                System.err.println("Attempted to read a bad URL: " + helpURL);
            }
        } else {
            System.err.println("Couldn't find file: about.html");
        }
        mainPanel.add(scrollLicenses);
        // JScrollPane scrollPane = new ScrollPane(textLicenses);
        dialog.pack();
        dialog.setSize(new Dimension(360, 320));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
