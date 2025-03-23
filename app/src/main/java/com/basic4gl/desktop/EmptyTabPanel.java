package com.basic4gl.desktop;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;

import com.basic4gl.desktop.util.KeyStrokeUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatButton;
import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.*;

public class EmptyTabPanel extends JPanel {
  private final JPanel contents;

  public EmptyTabPanel(
      IEmptyTabPanelListener listener,
      KeyStroke newFileKeyStroke,
      KeyStroke openFileKeyStroke,
      List<File> recentFiles) {
    super();
    setLayout(new BorderLayout());

    contents = new JPanel();
    contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
    contents.setAlignmentX(Component.LEFT_ALIGNMENT);

    JScrollPane scrollPane = new JScrollPane(contents);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    contents.add(buildGetStartedPanel(listener, newFileKeyStroke, openFileKeyStroke));

    contents.add(
        new Box.Filler(new Dimension(16, 16), new Dimension(16, 16), new Dimension(16, 16)));

    contents.add(buildRecentFilesPanel(listener, recentFiles));

    add(scrollPane);
  }

  private JPanel buildGetStartedPanel(
      IEmptyTabPanelListener listener, KeyStroke newFileKeyStroke, KeyStroke openFileKeyStroke) {
    JPanel getStartedPanel = new JPanel();
    getStartedPanel.setLayout(new BoxLayout(getStartedPanel, BoxLayout.Y_AXIS));
    getStartedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
    wrapper.setMaximumSize(new Dimension(400, 50));
    JLabel getStartedLabel = new JLabel("Get Started");
    Font font = getStartedLabel.getFont();
    getStartedLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
    getStartedLabel.setForeground(new Color(66, 66, 66));
    getStartedLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    getStartedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    wrapper.add(getStartedLabel);
    getStartedPanel.add(wrapper);

    wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
    wrapper.setMaximumSize(new Dimension(400, 50));
    FlatButton newButton = new FlatButton();
    newButton.setText("New Program");
    newButton.setIcon(createImageIcon(ICON_NEW));
    newButton.setBackground(null);
    newButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    newButton.setPreferredSize(new Dimension(160, 30));
    newButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    newButton.setHorizontalAlignment(SwingConstants.LEFT);
    newButton.addActionListener(
        (x) -> {
          listener.OnNewClick();
        });

    wrapper.add(newButton);

    addShortCutLabel(wrapper, newFileKeyStroke);
    getStartedPanel.add(wrapper);

    wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
    wrapper.setMaximumSize(new Dimension(400, 50));
    FlatButton openButton = new FlatButton();
    openButton.setText("Open Program...");
    openButton.setIcon(createImageIcon(ICON_OPEN));
    openButton.setBackground(null);
    openButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    openButton.setPreferredSize(new Dimension(160, 30));
    openButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    openButton.setHorizontalAlignment(SwingConstants.LEFT);
    openButton.addActionListener(
        (x) -> {
          listener.OnOpenClick();
        });

    wrapper.add(openButton);
    addShortCutLabel(wrapper, openFileKeyStroke);
    getStartedPanel.add(wrapper);

    return getStartedPanel;
  }

  private JPanel buildRecentFilesPanel(IEmptyTabPanelListener listener, List<File> recentFiles) {
    JPanel recentFilePanel = new JPanel();
    recentFilePanel.setLayout(new BoxLayout(recentFilePanel, BoxLayout.Y_AXIS));
    recentFilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
    wrapper.setMaximumSize(new Dimension(400, 50));
    wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel recentLabel = new JLabel("Recent Files");
    Font font = recentLabel.getFont();
    recentLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 2));
    recentLabel.setForeground(new Color(66, 66, 66));
    recentLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

    wrapper.add(recentLabel);
    recentFilePanel.add(wrapper);

    for (File file : recentFiles.stream().limit(5).toList()) {

      JPanel recentItem = new JPanel(new FlowLayout(FlowLayout.LEFT));
      recentItem.setAlignmentX(Component.LEFT_ALIGNMENT);
      recentItem.setMaximumSize(new Dimension(400, 30));
      recentItem.setBorder(null);

      JButton recentItemButton = new FlatButton();
      recentItemButton.setBackground(null);
      recentItemButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
      recentItemButton.setText(file.getName());
      recentItemButton.addActionListener(
          (x) -> {
            listener.OnOpenClick(file);
          });

      recentItem.add(recentItemButton);

      recentFilePanel.add(recentItem);
    }

    return recentFilePanel;
  }

  private void addShortCutLabel(JPanel parent, KeyStroke keyStroke) {
    for (String s : KeyStrokeUtil.getShortcutString(keyStroke).split(" ")) {
      JLabel l = new JLabel(s);
      if (!s.equals("+")) {

        l.putClientProperty(FlatClientProperties.STYLE, "arc: 10;");
        l.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        l.putClientProperty("JComponent.minimumWidth", 200);
        l.putClientProperty(
            "JComponent.padding", new Insets(10, 15, 10, 15)); // Adds internal padding

        l.setForeground(new Color(0xFF000000, true));
        l.setBackground(new Color(0x9FFFFFFF, true));
      }
      parent.add(l);
    }
  }

  public static interface IEmptyTabPanelListener {
    void OnNewClick();

    void OnOpenClick();

    void OnOpenClick(File file);
  }
}
