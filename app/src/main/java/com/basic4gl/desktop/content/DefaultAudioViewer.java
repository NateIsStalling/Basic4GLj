package com.basic4gl.desktop.content;

import com.basic4gl.desktop.spi.PluginContext;
import com.basic4gl.desktop.spi.content.FileViewer;
import com.basic4gl.desktop.spi.content.FileViewerException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.*;
import javax.swing.*;

/**
 * Default audio viewer with basic playback controls
 *
 * Features:
 * - Play/pause/stop controls
 * - Progress slider with seek capability
 * - Volume control
 * - Displays audio information (duration, format, etc.)
 * - Supports WAV and AU formats (via Java Sound API)
 */
public class DefaultAudioViewer implements FileViewer {

    private final JPanel panel;
    private final JButton playButton;
    private final JButton pauseButton;
    private final JButton stopButton;
    private final JSlider progressSlider;
    private final JSlider volumeSlider;
    private final JLabel infoLabel;
    private final JLabel timeLabel;

    private Clip audioClip;
    private Thread updateThread;
    private volatile boolean shouldStop = false;
    private Path currentPath;
    private String errorMessage = "";

    public DefaultAudioViewer() {
        this.panel = createUIPanel();
        this.playButton = new JButton("Play");
        this.pauseButton = new JButton("Pause");
        this.stopButton = new JButton("Stop");
        this.progressSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        this.volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 80);
        this.infoLabel = new JLabel("No file loaded");
        this.timeLabel = new JLabel("00:00 / 00:00");

        setupUI();
        setupListeners();
    }

    private JPanel createUIPanel() {
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.setBackground(Color.DARK_GRAY);
        return main;
    }

    private void setupUI() {
        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(infoLabel, BorderLayout.CENTER);
        infoPanel.add(timeLabel, BorderLayout.EAST);

        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        // Progress slider
        progressSlider.setEnabled(false);
        JPanel progressPanel = new JPanel(new BorderLayout(5, 0));
        progressPanel.setOpaque(false);
        progressPanel.add(new JLabel("Progress:"), BorderLayout.WEST);
        progressPanel.add(progressSlider, BorderLayout.CENTER);

        // Volume control
        JPanel volumePanel = new JPanel(new BorderLayout(5, 0));
        volumePanel.setOpaque(false);
        volumePanel.add(new JLabel("Volume:"), BorderLayout.WEST);
        volumePanel.add(volumeSlider, BorderLayout.CENTER);

        // Combine all
        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        controlPanel.setOpaque(false);
        controlPanel.add(buttonPanel);
        controlPanel.add(progressPanel);
        controlPanel.add(volumePanel);

        panel.add(infoPanel, BorderLayout.NORTH);
        panel.add(controlPanel, BorderLayout.CENTER);
    }

    private void setupListeners() {
        playButton.addActionListener(e -> play());
        pauseButton.addActionListener(e -> pause());
        stopButton.addActionListener(e -> stop());

        volumeSlider.addChangeListener(e -> {
            if (audioClip != null) {
                float volume = volumeSlider.getValue() / 100.0f;
                FloatControl volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(20.0f * (float) Math.log10(volume));
            }
        });

        progressSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (audioClip != null && audioClip.isRunning()) {
                    long newPosition = (long) (progressSlider.getValue() / 100.0 * audioClip.getMicrosecondLength());
                    audioClip.setMicrosecondPosition(newPosition);
                }
            }
        });
    }

    @Override
    public void loadFile(PluginContext context, Path path) throws FileViewerException {
        try {
            stop();

            if (!Files.exists(path)) {
                throw new FileViewerException("File not found: " + path);
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(path.toFile());
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);

            currentPath = path;
            errorMessage = "";

            // Update UI
            AudioFormat format = audioStream.getFormat();
            infoLabel.setText(String.format(
                    "Loaded: %s | Format: %.0f Hz, %d bits, %d channels",
                    path.getFileName(), format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels()));

            progressSlider.setEnabled(true);
            playButton.setEnabled(true);

            updateProgressDisplay();

        } catch (UnsupportedAudioFileException | IOException e) {
            errorMessage = e.getMessage();
            throw new FileViewerException("Failed to load audio file: " + e.getMessage(), e);
        } catch (LineUnavailableException e) {
            errorMessage = e.getMessage();
            throw new FileViewerException("Audio line not available: " + e.getMessage(), e);
        }
    }

    private void play() {
        if (audioClip != null) {
            if (audioClip.isRunning()) {
                return;
            }
            audioClip.start();
            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stopButton.setEnabled(true);
            startUpdateThread();
        }
    }

    private void pause() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
        }
    }

    private void stop() {
        shouldStop = true;
        if (audioClip != null) {
            audioClip.stop();
            audioClip.setMicrosecondPosition(0);
        }
        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
        progressSlider.setValue(0);
        timeLabel.setText("00:00 / 00:00");
    }

    private void startUpdateThread() {
        if (updateThread == null || !updateThread.isAlive()) {
            shouldStop = false;
            updateThread = new Thread(this::updateProgress);
            updateThread.setDaemon(true);
            updateThread.start();
        }
    }

    private void updateProgress() {
        while (!shouldStop && audioClip != null && audioClip.isRunning()) {
            try {
                updateProgressDisplay();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        updateProgressDisplay();
    }

    private void updateProgressDisplay() {
        if (audioClip != null) {
            long duration = audioClip.getMicrosecondLength();
            long current = audioClip.getMicrosecondPosition();

            if (duration > 0) {
                int progress = (int) ((current * 100.0) / duration);
                progressSlider.setValue(progress);
            }

            timeLabel.setText(formatTime(current) + " / " + formatTime(duration));
        }
    }

    private String formatTime(long microseconds) {
        long seconds = microseconds / 1_000_000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public boolean canHandle(String filename, String mimeType) {
        String lower = (filename != null ? filename.toLowerCase() : "");
        String mime = (mimeType != null ? mimeType.toLowerCase() : "");

        // Check by extension - Java Sound API natively supports WAV and AU
        if (lower.endsWith(".wav") || lower.endsWith(".au")) {
            return true;
        }

        // Check by MIME type
        return mime.equals("audio/wav") || mime.equals("audio/x-wav") || mime.equals("audio/basic");
    }

    @Override
    public String getName() {
        return "Audio Viewer";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void dispose() {
        shouldStop = true;
        if (audioClip != null) {
            audioClip.close();
            audioClip = null;
        }
        if (updateThread != null) {
            try {
                updateThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        currentPath = null;
    }
}
