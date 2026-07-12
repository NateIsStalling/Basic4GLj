package com.basic4gl.desktop.panels;

import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.desktop.BasicEditor;
import com.basic4gl.desktop.debugger.IDebugPresenter;
import com.basic4gl.desktop.editor.ApMode;
import com.basic4gl.desktop.spi.EditorPlugin;
import com.basic4gl.desktop.spi.PluginContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static com.basic4gl.desktop.Theme.ICON_PAUSE;
import static com.basic4gl.desktop.Theme.ICON_PLAY;
import static com.basic4gl.desktop.Theme.ICON_STEP_IN;
import static com.basic4gl.desktop.Theme.ICON_STEP_OUT;
import static com.basic4gl.desktop.Theme.ICON_STEP_OVER;
import static com.basic4gl.desktop.Theme.ICON_MENU_DEBUG;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;

public class DebugPanelProvider implements IEditorPanelProvider, IDebugPresenter {

    private final JSplitPane debugPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JButton playButton = new JButton(createImageIcon(ICON_PLAY));
    private final JButton stepOverButton = new JButton(createImageIcon(ICON_STEP_OVER));
    private final JButton stepInButton = new JButton(createImageIcon(ICON_STEP_IN));
    private final JButton stepOutButton = new JButton(createImageIcon(ICON_STEP_OUT));
    // Debugging
    private final DefaultListModel<String> watchListModel = new DefaultListModel<>();
    private final JList<String> watchListBox = new JList<>(watchListModel);
    private final DefaultListModel<String> gosubListModel = new DefaultListModel<>();

    private final BasicEditor basicEditor;
    private PluginContext context;


    public DebugPanelProvider(BasicEditor basicEditor) {
        //TODO this is working up to a circular dependency.. can't init BasicEditor with this as IDebugPresenter
        this.basicEditor = basicEditor;
    }

    @Override
    public String id() {
        return "debug";
    }

    @Override
    public String getTitle() {
        return "Debug";
    }

    @Override
    public String getIconPath() {
        return ICON_MENU_DEBUG;
    }

    @Override
    public EditorLayout getLayoutConstraints() {
        return EditorLayout.SOUTH;
    }

    @Override
    public JPanel build(PluginContext context) {
        this.context = context;

        JPanel panel = new JPanel(new BorderLayout());

        JToolBar debugToolBar = new JToolBar();
        debugToolBar.setFloatable(false);
        debugToolBar.add(playButton);
        debugToolBar.add(stepOverButton);
        debugToolBar.add(stepInButton);
        debugToolBar.add(stepOutButton);

        playButton.setToolTipText("Play/Pause");
        stepOverButton.setToolTipText("Step Over");
        stepInButton.setToolTipText("Step In");
        stepOutButton.setToolTipText("Step Out");

        playButton.addActionListener(e -> context.debugger().actionPlayPause());
        stepOverButton.addActionListener(e -> context.debugger().actionStep());
        stepInButton.addActionListener(e -> context.debugger().actionStepInto());
        stepOutButton.addActionListener(e -> context.debugger().actionStepOutOf());

        panel.add(debugToolBar, BorderLayout.NORTH);
        panel.add(buildDebugPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDebugPanel() {
        // Debugger
        JPanel watchListFrame = new JPanel();
        watchListFrame.setLayout(new BorderLayout());
        JLabel watchlistLabel = new JLabel("Watchlist");
        watchlistLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        watchListFrame.add(watchlistLabel, BorderLayout.NORTH);
        JScrollPane watchListScrollPane = new JScrollPane(watchListBox);
        watchListFrame.add(watchListScrollPane, BorderLayout.CENTER);

        watchListBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    // Double-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    editWatch();
                }
            }
        });

        watchListBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    editWatch();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteWatch();
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    watchListBox.setSelectedIndex(basicEditor.getWatchListSize());
                    editWatch();
                }
            }
        });

        watchListBox.addListSelectionListener(e -> updateWatchHint());

        JPanel gosubFrame = new JPanel();
        gosubFrame.setLayout(new BorderLayout());
        JLabel callstackLabel = new JLabel("Callstack");
        callstackLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        gosubFrame.add(callstackLabel, BorderLayout.NORTH);
        JList<String> gosubListBox = new JList<>(gosubListModel);
        JScrollPane gosubListScrollPane = new JScrollPane(gosubListBox);
        gosubFrame.add(gosubListScrollPane, BorderLayout.CENTER);

        debugPane.setLeftComponent(watchListFrame);
        debugPane.setRightComponent(gosubFrame);

        JPanel debugPanel = new JPanel();
        debugPanel.setLayout(new BorderLayout());
        debugPanel.add(debugPane, BorderLayout.CENTER);

        return debugPanel;
    }

    @Override
    public void updateCallStack(StackTraceCallback stackTraceCallback) {

        // Clear debug controls
        gosubListModel.clear();

        for (String label : basicEditor.getLanguageService().buildFriendlyCallStackLabels(stackTraceCallback)) {
            gosubListModel.addElement(label);
        }
    }

    @Override
    public void refreshWatchList() {
        // Clear debug controls
        watchListModel.clear();

        for (String watch : basicEditor.getWatches()) {

            watchListModel.addElement(watch + ": " + "???");
        }
        watchListModel.addElement(" "); // Last line is blank, and can be clicked on to add new watch
    }

    private void editWatch() {
        if (context == null) {
            return;
        }

        String newWatch, oldWatch;

        // Find watch
        int index = watchListBox.getSelectedIndex();
        int saveIndex = index;

        // Extract watch text
        oldWatch = basicEditor.getWatchOrDefault(index);

        // Prompt for new text
        newWatch = context.dialogs().showInputDialog("Enter variable/expression:", "Watch variable", oldWatch);

        basicEditor.updateWatch(newWatch, index);

        watchListBox.setSelectedIndex(saveIndex);
        updateWatchHint();
    }

    void deleteWatch() {

        // Find watch
        int index = watchListBox.getSelectedIndex();
        int saveIndex = index;

        // Delete watch
        basicEditor.removeWatchAt(index);

        watchListBox.setSelectedIndex(saveIndex);
        updateWatchHint();
    }

    private void updateWatchHint() {
        int index = watchListBox.getSelectedIndex();
        if (index > -1 && index < basicEditor.getWatchListSize()) {
            watchListBox.setToolTipText((String) watchListModel.get(index));
        } else {
            watchListBox.setToolTipText("");
        }
    }

    @Override
    public void updateEvaluateWatch(String evaluatedWatch, String result) {
        int index = 0;
        for (String watch : basicEditor.getWatches()) {
            if (Objects.equals(watch, evaluatedWatch)) {
                watchListModel.setElementAt(watch + ": " + result, index);
            }
            index++;
        }
    }

    @Override
    public void clearCallStack() {
        gosubListModel.clear();
    }

    @Override
    public void refreshDebugControls(ApMode mode) {
        if (mode != ApMode.AP_CLOSED) {
            playButton.setIcon(mode == ApMode.AP_RUNNING ? createImageIcon(ICON_PAUSE) : createImageIcon(ICON_PLAY));
            playButton.setEnabled(mode != ApMode.AP_WAITING);
            stepOverButton.setEnabled(mode != ApMode.AP_RUNNING && mode != ApMode.AP_WAITING);
            stepInButton.setEnabled(mode != ApMode.AP_RUNNING && mode != ApMode.AP_WAITING);
            stepOutButton.setEnabled(mode == ApMode.AP_PAUSED);
            return;
        }

        playButton.setEnabled(false);
        stepOverButton.setEnabled(false);
        stepInButton.setEnabled(false);
        stepOutButton.setEnabled(false);
    }

    @Override
    public void refresh(EditorPlugin languageProvider) {

    }

    @Override
    public void onFileModified(String filePath) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void onCompileSucceeded() {

    }
}
