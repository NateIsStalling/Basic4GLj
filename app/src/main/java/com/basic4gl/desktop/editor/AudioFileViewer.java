package com.basic4gl.desktop.editor;

import java.awt.*;
import java.io.File;
import javax.swing.*;

/**
 * Audio file viewer for playing WAV, OGG, MP3, FLAC, and other audio formats.
 * Provides a player interface with:
 * - Play/Pause controls
 * - Progress bar
 * - Volume control
 * - File information display
 *
 * Note: Audio playback implementation depends on the audio library availability.
 * Currently provides a UI framework that can be extended with actual playback functionality.
 */
public class AudioFileViewer implements IFileViewer {
    private final File file;
    private final JPanel contentPanel;

    public AudioFileViewer(File file) {
        this.file = file;
        this.contentPanel = createAudioPlayerUI();
    }

    private JPanel createAudioPlayerUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Player controls
        JPanel controlsPanel = createControlsPanel();
        mainPanel.add(controlsPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // File info
        if (file != null && file.exists()) {
            JLabel fileNameLabel = new JLabel("File: " + file.getName());
            fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            fileNameLabel.setFont(fileNameLabel.getFont().deriveFont(Font.BOLD, 14f));
            panel.add(fileNameLabel);

            JLabel fileSizeLabel = new JLabel(String.format("Size: %.2f MB", file.length() / (1024.0 * 1024.0)));
            fileSizeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            fileSizeLabel.setFont(fileSizeLabel.getFont().deriveFont(12f));
            panel.add(fileSizeLabel);

            panel.add(Box.createVerticalStrut(20));
        }

        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton playButton = new JButton("▶ Play");
        JButton pauseButton = new JButton("⏸ Pause");
        JButton stopButton = new JButton("⏹ Stop");

        playButton.addActionListener(e -> JOptionPane.showMessageDialog(
                contentPanel,
                "Audio playback: Please implement audio playback support",
                "Feature Not Yet Implemented",
                JOptionPane.INFORMATION_MESSAGE));
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        panel.add(buttonPanel);

        // Progress bar
        panel.add(Box.createVerticalStrut(20));
        JLabel progressLabel = new JLabel("Time: 0:00 / 0:00");
        progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(progressLabel);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, progressBar.getPreferredSize().height));
        panel.add(progressBar);

        // Volume control
        panel.add(Box.createVerticalStrut(20));
        JPanel volumePanel = new JPanel();
        volumePanel.setLayout(new BoxLayout(volumePanel, BoxLayout.X_AXIS));
        JLabel volumeLabel = new JLabel("Volume: ");
        JSlider volumeSlider = new JSlider(0, 100, 80);
        volumeSlider.setMaximumSize(new Dimension(300, volumeSlider.getPreferredSize().height));
        volumePanel.add(volumeLabel);
        volumePanel.add(volumeSlider);
        panel.add(volumePanel);

        // Info panel
        panel.add(Box.createVerticalStrut(20));
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Info"));
        JTextArea infoArea = new JTextArea("Audio player interface ready.\n\n" + "To enable playback, integrate with:\n"
                + "- Paulscode SoundSystem (available in this project)\n"
                + "- JavaFX MediaPlayer\n"
                + "- Tritonus (Java Sound alternative)\n\n"
                + "For now, you can open this file with a system audio player\n"
                + "using the 'Open With' context menu.");
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setMargin(new Insets(10, 10, 10, 10));
        infoPanel.add(new JScrollPane(infoArea));
        panel.add(infoPanel);

        return panel;
    }

    @Override
    public String getTitle() {
        if (file == null) {
            return "[Audio]";
        }
        return file.getName();
    }

    @Override
    public String getFilePath() {
        return file != null ? file.getAbsolutePath() : "";
    }

    @Override
    public JComponent getContentPane() {
        return contentPanel;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getShortFilename() {
        return file != null ? file.getName() : "[Audio]";
    }

    @Override
    public boolean isModified() {
        // Audio files are read-only
        return false;
    }

    @Override
    public void setModified() {
        // Audio files are read-only
    }

    @Override
    public ViewerType getViewerType() {
        return ViewerType.AUDIO_VIEWER;
    }
}
