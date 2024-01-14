package com.basic4gl.desktop;

import com.basic4gl.debug.protocol.types.StackFrame;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.desktop.debugger.DebugServerConstants;
import com.basic4gl.desktop.debugger.DebugServerFactory;
import com.basic4gl.desktop.editor.*;
import com.basic4gl.desktop.util.*;
import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.util.SystemInfo;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;
import static com.formdev.flatlaf.FlatClientProperties.*;

/**
 * Created by Nate on 2/24/2015.
 */
public class MainWindow implements
        IEditorPresenter,
        ITabProvider,
        IToggleBreakpointListener,
        IFileEditorActionListener {

    private final CaretListener TrackCaretPosition = new CaretListener() {
        @Override
        public void caretUpdate(CaretEvent e) {
            JTextArea component = (JTextArea) e.getSource();
            int caretpos = component.getCaretPosition();
            int row = 0;
            int column = 0;
            try {
                row = component.getLineOfOffset(caretpos);
                column = caretpos - component.getLineStartOffset(row);

                cursorPosLabel.setText((column + 1) + ":" + (row + 1));
            } catch (BadLocationException ex) {
                cursorPosLabel.setText(0 + ":" + 0);
                ex.printStackTrace();
            }
        }
    };

    // Window
    private final JFrame frame = new JFrame(BuildInfo.APPLICATION_NAME);
    private final JMenuBar menuBar = new JMenuBar();
    private final JToolBar toolBar = new JToolBar();
    private final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private final JSplitPane debugPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JTabbedPane tabControl = new JTabbedPane();
    private final JPanel statusPanel = new JPanel();

    private final JMenu fileMenu = new JMenu("File");
    private final JMenu editMenu = new JMenu("Edit");
    private final JMenu viewMenu = new JMenu("View");
    private final JMenu debugMenu = new JMenu("Debug");
    private final JMenu appMenu = new JMenu("Application");
    private final JMenu helpMenu = new JMenu("Help");
    private final JMenu bookmarkSubMenu = new JMenu("Bookmarks");
    private final JMenu breakpointSubMenu = new JMenu("Breakpoints");

    // Menu Items
    private final JMenuItem newMenuItem = new JMenuItem("New Program");
    private final JMenuItem openMenuItem = new JMenuItem("Open Program...");
    private final JMenuItem saveMenuItem = new JMenuItem("Save");
    private final JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
    private final JMenuItem exportMenuItem = new JMenuItem("Export...");

    private final JMenuItem undoMenuItem = new JMenuItem("Undo");
    private final JMenuItem redoMenuItem = new JMenuItem("Redo");
    private final JMenuItem cutMenuItem = new JMenuItem("Cut");
    private final JMenuItem copyMenuItem = new JMenuItem("Copy");
    private final JMenuItem pasteMenuItem = new JMenuItem("Paste");
    private final JMenuItem selectAllMenuItem = new JMenuItem("Select All");

    private final JMenuItem nextBookmarkMenuItem = new JMenuItem("Next");
    private final JMenuItem previousBookmarkMenuItem = new JMenuItem("Previous");
    private final JMenuItem toggleBookmarkMenuItem = new JMenuItem("Toggle Bookmark");
    private final JMenuItem findMenuItem = new JMenuItem("Find");
    private final JMenuItem replaceMenuItem = new JMenuItem("Replace");
    private final JCheckBoxMenuItem debugMenuItem = new JCheckBoxMenuItem("Debug Mode");

    private final JMenuItem settingsMenuItem = new JMenuItem("Project Settings");
    private final JMenuItem runMenuItem = new JMenuItem("Run Program");
    private final JMenuItem nextBreakpointMenuItem = new JMenuItem("View Next");
    private final JMenuItem previousBreakpointMenuItem = new JMenuItem("View Previous");
    private final JMenuItem toggleBreakpointMenuItem = new JMenuItem("Toggle Breakpoint");
    private final JMenuItem playPauseMenuItem = new JMenuItem("Play/Pause");
    private final JMenuItem stepOverMenuItem = new JMenuItem("Step Over");
    private final JMenuItem stepIntoMenuItem = new JMenuItem("Step Into");
    private final JMenuItem stepOutOfMenuItem = new JMenuItem("Step Out of");

    private final JMenuItem functionListMenuItem = new JMenuItem("Function List");
    private final JMenuItem aboutMenuItem = new JMenuItem("About");

    // Toolbar Buttons
    private final JButton newButton = new JButton(createImageIcon(ICON_NEW));
    private final JButton openButton = new JButton(createImageIcon(ICON_OPEN));
    private final JButton saveButton = new JButton(createImageIcon(ICON_SAVE));
    private final JButton runButton = new JButton(createImageIcon(ICON_RUN_APP));

    private final JToggleButton debugButton = new JToggleButton(createImageIcon(ICON_DEBUG));
    private final JButton playButton = new JButton(createImageIcon(ICON_PLAY));
    private final JButton stepOverButton = new JButton(createImageIcon(ICON_STEP_OVER));
    private final JButton stepInButton = new JButton(createImageIcon(ICON_STEP_IN));
    private final JButton stepOutButton = new JButton(createImageIcon(ICON_STEP_OUT));

    // Labels
    private final JLabel compilerStatusLabel = new JLabel("");    // Compiler/VM Status
    private final JLabel cursorPosLabel = new JLabel("0:0"); // Cursor Position

    // Debugging
    private final DefaultListModel<String> watchListModel = new DefaultListModel<>();
    private final JList <String> watchListBox = new JList<>(watchListModel);
    private final JScrollPane watchListScrollPane = new JScrollPane(watchListBox);
    private final JPanel watchListFrame = new JPanel();
    private final DefaultListModel<String> gosubListModel = new DefaultListModel<>();
    private final JList<String> gosubListBox = new JList<>(gosubListModel);
    private final JScrollPane gosubListScrollPane = new JScrollPane(gosubListBox);
    private final JPanel gosubFrame = new JPanel();

    // Editors
    BasicEditor basicEditor;
    FileManager fileManager;

    IncludeLinkGenerator linkGenerator = new IncludeLinkGenerator(this);

    SearchContext searchContext;

    // Debugging
    private boolean isDebugMode = false;

    // Set when stepping. Delays switching to the output window for the first 1000 op-codes.
    // (To prevent excessive screen mode switches when debugging full-screen programs.)
    private boolean isDelayScreenSwitchEnabled = false;

    // TODO create config file
    static String debugServerBinPath;
    static String outputBinPath;
    static String applicationStoragePath;

    public static void main(String[] args) {
        // Location to store logs
        applicationStoragePath = System.getProperty("user.home") +
            System.getProperty("file.separator") +
            BuildInfo.APPLICATION_NAME;

        if (args.length >= 2) {
            // for debugging; set by gradle debugAll
            outputBinPath = args[0];
            debugServerBinPath = args[1];
        } else if (System.getProperty("jpackage.app-path") != null) {
            // app was built with jpackage; the output window and debug server binaries should be bundled with it
            String appPath = System.getProperty("jpackage.app-path");
            File appDirectory = new File(appPath).getParentFile();
            // TODO these should NOT be hardcoded; figure out how to generate some appconfig.json with builds to include these as config settings
            outputBinPath = new File(appDirectory, "Basic4GLjOutput").getAbsolutePath();
            debugServerBinPath = new File(appDirectory, "Basic4GLjDebugServer").getAbsolutePath();
        } else {
            // TODO these should NOT be hardcoded; figure out how to generate some appconfig.json with builds to include these as config settings
            outputBinPath = "lib/library-1.0-SNAPSHOT.jar";
            debugServerBinPath = "lib/debugServer-1.0-SNAPSHOT.jar";
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Basic4GLj" );
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Basic4GLj");

        FlatLightLaf.setup();

        PrintStream out = null;
        try {
            String logFilePath = new File(applicationStoragePath, "Basic4GLj.log").getAbsolutePath();

            File file = new File(logFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            out = new PrintStream(new FileOutputStream(file.getAbsolutePath(), true), true);
            System.setOut(out);
            System.setErr(out);
        } catch (IOException e) {
            System.err.println("Unable to log to file");
            e.printStackTrace();
        }

        new MainWindow();
    }

    public MainWindow() {
        FlatDesktop.setAboutHandler(() -> showAboutDialog());
        FlatDesktop.setPreferencesHandler(() -> showSettings());
        FlatDesktop.setQuitHandler(response -> tryCloseWindow());

//        findDialog = new FindDialog(mFrame, this);
//        replaceDialog = new ReplaceDialog(mFrame, this);

        // This ties the properties of the two dialogs together (match case,
        // regex, etc.).
//        context = findDialog.getSearchContext();
//        replaceDialog.setSearchContext(context);

        searchContext = new SearchContext();

        // Create and set up the window.
        frame.setIconImage(createImageIcon(BuildInfo.ICON_LOGO_SMALL).getImage());
        frame.setPreferredSize(new Dimension(696, 480));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(debugMenu);
        menuBar.add(appMenu);
        menuBar.add(helpMenu);

        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(exportMenuItem);

        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        editMenu.add(new JSeparator());
        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.add(new JSeparator());
        editMenu.add(findMenuItem);
        editMenu.add(replaceMenuItem);
        editMenu.add(new JSeparator());
        editMenu.add(selectAllMenuItem);

        viewMenu.add(bookmarkSubMenu);
        bookmarkSubMenu.add(nextBookmarkMenuItem);
        bookmarkSubMenu.add(previousBookmarkMenuItem);
        bookmarkSubMenu.add(toggleBookmarkMenuItem);

        debugMenu.add(playPauseMenuItem);
        debugMenu.add(stepOverMenuItem);
        debugMenu.add(stepIntoMenuItem);
        debugMenu.add(stepOutOfMenuItem);
        debugMenu.add(new JSeparator());
        debugMenu.add(breakpointSubMenu);
        breakpointSubMenu.add(nextBreakpointMenuItem);
        breakpointSubMenu.add(previousBreakpointMenuItem);
        breakpointSubMenu.add(toggleBreakpointMenuItem);
        debugMenu.add(new JSeparator());
        debugMenu.add(debugMenuItem);

        appMenu.add(runMenuItem);
        appMenu.add(new JSeparator());
        appMenu.add(settingsMenuItem);

        helpMenu.add(functionListMenuItem);
        helpMenu.add(new JSeparator());
        helpMenu.add(aboutMenuItem);

//        searchField = new JTextField(30);
//        mToolBar.add(searchField);
//        regexCB = new JCheckBox("Regex");
//        mToolBar.add(regexCB);
//        matchCaseCB = new JCheckBox("Match Case");
//        mToolBar.add(matchCaseCB);

        Toolkit toolkit = Toolkit.getDefaultToolkit();

        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, toolkit.getMenuShortcutKeyMask()));
        newMenuItem.addActionListener(e -> actionNew());
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, toolkit.getMenuShortcutKeyMask()));
        openMenuItem.addActionListener(e -> actionOpen());
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, toolkit.getMenuShortcutKeyMask()));
        saveMenuItem.addActionListener(e -> actionSave());
        saveAsMenuItem.addActionListener(e -> actionSaveAs());
        exportMenuItem.addActionListener(e -> {
            basicEditor.SetMode(ApMode.AP_STOPPED, null);
            if (fileManager.editorCount() == 0) {
                JOptionPane.showMessageDialog(frame, "Nothing to export", "Cannot export",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Clear source code from parser
            basicEditor.mComp.Parser().getSourceCode().clear();

            if (!basicEditor.LoadProgramIntoCompiler()) {
                compilerStatusLabel.setText(basicEditor.mPreprocessor.getError());
                return;
            }
            ExportDialog dialog = new ExportDialog(MainWindow.this, basicEditor.mComp, basicEditor.mPreprocessor, fileManager.mFileEditors);
            dialog.setLibraries(basicEditor.mLibraries, basicEditor.mCurrentBuilder);
            dialog.setVisible(true);
            basicEditor.mCurrentBuilder = dialog.getCurrentBuilder();
        });
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, toolkit.getMenuShortcutKeyMask()));
        undoMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.undo(i);
        });
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, toolkit.getMenuShortcutKeyMask()));
        redoMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.redo(i);
        });
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, toolkit.getMenuShortcutKeyMask()));
        cutMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.cut(i);
        });
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, toolkit.getMenuShortcutKeyMask()));
        copyMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.copy(i);
        });
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, toolkit.getMenuShortcutKeyMask()));
        pasteMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.Paste(i);
        });
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, toolkit.getMenuShortcutKeyMask()));
        findMenuItem.addActionListener(e -> {
            showFindReplaceMenu(false);
        });
        replaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_MASK | toolkit.getMenuShortcutKeyMask()));
        replaceMenuItem.addActionListener(e -> {
            showFindReplaceMenu(true);
        });
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, toolkit.getMenuShortcutKeyMask()));
        selectAllMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.SelectAll(i);
        });
        nextBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        nextBookmarkMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.SelectNextBookmark(i);
        });
        previousBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_MASK));
        previousBookmarkMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.SelectPreviousBookmark(i);
        });

        playPauseMenuItem.addActionListener(e -> basicEditor.actionRun());
        playPauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        playPauseMenuItem.addActionListener(e -> basicEditor.actionPlayPause());
        stepOverMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        stepOverMenuItem.addActionListener(e -> {
            isDelayScreenSwitchEnabled = true;
            basicEditor.actionStep();
        });
        stepIntoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        stepIntoMenuItem.addActionListener(e -> {
            isDelayScreenSwitchEnabled = true;
            basicEditor.actionStepInto();
        });
        stepOutOfMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_MASK));
        stepOutOfMenuItem.addActionListener(e -> {
            isDelayScreenSwitchEnabled = true;
            basicEditor.actionStepOutOf();
        });

        toggleBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, toolkit.getMenuShortcutKeyMask()));
        toggleBookmarkMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.ToggleBookmark(i);
        });
        nextBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        nextBreakpointMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.SelectNextBreakpoint(i);
        });
        previousBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK));
        previousBreakpointMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.SelectPreviousBreakpoint(i);
        });
        toggleBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, toolkit.getMenuShortcutKeyMask()));
        toggleBreakpointMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.toggleBreakpoint(i);
        });
        debugMenuItem.addActionListener(e -> actionDebugMode());
        runMenuItem.addActionListener(e -> basicEditor.actionRun());
        settingsMenuItem.addActionListener(e -> {
            showSettings();
        });
        functionListMenuItem.addActionListener(e -> {
            ReferenceWindow window = new ReferenceWindow(frame);
            window.populate(basicEditor.mComp);
            window.setVisible(true);
        });
        aboutMenuItem.addActionListener(e -> showAboutDialog());

        if (SystemInfo.isMacOS) {
            // hide menu items that are in macOS application menu
            aboutMenuItem.setVisible(false);
            settingsMenuItem.setVisible(false);
            //TODO mExitMenuItem.setVisible(false);
        }

        //Debugger
        watchListFrame.setLayout(new BorderLayout());
        JLabel watchlistLabel = new JLabel("Watchlist");
        watchlistLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        watchListFrame.add(watchlistLabel, BorderLayout.NORTH);
        watchListFrame.add(watchListScrollPane, BorderLayout.CENTER);

        watchListBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    // Double-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    EditWatch();
                }
            }
        });

        watchListBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    EditWatch();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    DeleteWatch();
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    watchListBox.setSelectedIndex(basicEditor.getWatchListSize());
                    EditWatch();
                }
            }
        });

        watchListBox.addListSelectionListener(e -> UpdateWatchHint());

        gosubFrame.setLayout(new BorderLayout());
        JLabel callstackLabel = new JLabel("Callstack");
        callstackLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        gosubFrame.add(callstackLabel, BorderLayout.NORTH);
        gosubFrame.add(gosubListScrollPane, BorderLayout.CENTER);

        debugPane.setLeftComponent(watchListFrame);
        debugPane.setRightComponent(gosubFrame);

        //Toolbar
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(runButton);
        toolBar.addSeparator();
        toolBar.add(debugButton);
        toolBar.addSeparator();
        toolBar.add(playButton);
        toolBar.add(stepOverButton);
        toolBar.add(stepInButton);
        toolBar.add(stepOutButton);

        newButton.addActionListener(e -> actionNew());
        openButton.addActionListener(e -> actionOpen());
        saveButton.addActionListener(e -> actionSave());
        runButton.addActionListener(e -> basicEditor.actionRun());

        debugButton.addActionListener(e -> actionDebugMode());
        playButton.addActionListener(e -> basicEditor.actionPlayPause());
        stepOverButton.addActionListener(e -> basicEditor.actionStep());
        stepInButton.addActionListener(e -> basicEditor.actionStepInto());
        stepOutButton.addActionListener(e -> basicEditor.actionStepOutOf());
        runButton.setToolTipText("Run the program!");

        toolBar.setAlignmentY(1);
        toolBar.setFloatable(false);

        //Status Panel
        JPanel panelStatusInfo = new JPanel(new BorderLayout());
        JPanel panelStatusCursor = new JPanel();

        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));

        statusPanel.add(panelStatusCursor);
        statusPanel.add(new JSeparator(JSeparator.VERTICAL));
        statusPanel.add(panelStatusInfo);

        panelStatusCursor.add(cursorPosLabel, BorderLayout.CENTER);
        panelStatusInfo.add(compilerStatusLabel, BorderLayout.LINE_START);

        statusPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
        panelStatusInfo.setBorder(new EmptyBorder(0, 5, 0, 5));
        panelStatusInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        panelStatusCursor.setBorder(new EmptyBorder(0, 5, 0, 5));
        panelStatusCursor.setMaximumSize(new Dimension(96, 24));

        // Tabs
        UIManager.put("TabbedPane.selected", new Color(220, 220, 220));
        UIManager.put("TabbedPane.contentAreaColor", new Color(220, 220, 220));

        UIManager.put("TabbedPane.closeArc", 999);
        UIManager.put("TabbedPane.closeCrossFilledSize", 5.5f);
        UIManager.put("TabbedPane.closeIcon", new FlatTabbedPaneCloseIcon());

        UIManager.put("TabbedPane.selectedBackground", Color.white);

        SwingUtilities.updateComponentTreeUI(tabControl);
        tabControl.setUI(new FlatTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
            }
        });

        // The following line enables to use scrolling tabs.
        tabControl.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabControl.putClientProperty( TABBED_PANE_TAB_CLOSABLE, true );
        tabControl.putClientProperty( TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close" );
        tabControl.putClientProperty( TABBED_PANE_TAB_CLOSE_CALLBACK,
                (BiConsumer<JTabbedPane, Integer>) (tabPane, tabIndex) -> {
                    if (tabIndex != -1) {
                        if (FileCheckSaveChanges(tabIndex)) {
                            //Clear file's breakpoints
                            FileEditor editor = fileManager.mFileEditors.get(tabIndex);
                            List<Integer> breakpoints = editor.getBreakpoints();
                            String file = editor.getFilePath();

                            for (Integer line : breakpoints) {
                                basicEditor.toggleBreakpt(file, line);
                            }

                            //Remove tab
                            tabControl.remove(tabIndex);
                            fileManager.mFileEditors.remove(tabIndex.intValue());

                            //Refresh controls if no files open
                            if (fileManager.editorCount() == 0) {
                                basicEditor.SetMode(ApMode.AP_CLOSED, null);
                            }
                        }
                    }
                } );

        mainPane.setTopComponent(tabControl);
        debugPane.setLeftComponent(watchListFrame);
        debugPane.setRightComponent(gosubFrame);

        // Add controls to window
        frame.add(toolBar, BorderLayout.NORTH);
        frame.add(mainPane, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);
        frame.setJMenuBar(menuBar);

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                tryCloseWindow();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        //Initialize syntax highlighting
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        // TODO don't hardcode this classname
        atmf.putMapping("text/basic4gl", "com.basic4gl.desktop.editor.BasicTokenMaker");

        //mDLLs(GetCurrentDir().c_str(), false)
        fileManager = new FileManager();
        Preprocessor preprocessor = new Preprocessor(2, new EditorSourceFileServer(fileManager), new DiskFileServer());
        Debugger debugger = new Debugger(preprocessor.getLineNumberMap());
        TomVM vm = new TomVM(debugger);
        TomBasicCompiler comp = new TomBasicCompiler(vm);
        basicEditor = new BasicEditor(outputBinPath, fileManager, this, preprocessor, debugger, comp);


        //TODO Confirm this doesn't break if app is ever signed
        //getParent
        fileManager.mAppDirectory = new File(".").getAbsolutePath();

        if (new File(fileManager.mAppDirectory, "Programs").exists()) {
            fileManager.mRunDirectory = fileManager.mAppDirectory + "\\Programs";
        } else {
            fileManager.mRunDirectory = fileManager.mAppDirectory;
        }
        fileManager.mFileDirectory = fileManager.mRunDirectory;
        fileManager.mCurrentDirectory = fileManager.mFileDirectory;

        // TODO this should be done as a callback
        RefreshActions(basicEditor.mMode);
        RefreshDebugDisplays(basicEditor.mMode);

        basicEditor.InitLibraries();
        ResetProject();


        // Warm up the debug server
        DebugServerFactory.startDebugServer(
            debugServerBinPath,
            DebugServerConstants.DEFAULT_DEBUG_SERVER_PORT);

        // Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void showAboutDialog() {
        new AboutDialog(frame);
    }

    private void showSettings() {
        ProjectSettingsDialog dialog = new ProjectSettingsDialog(frame);
        dialog.setLibraries(basicEditor.mLibraries, basicEditor.mCurrentBuilder);
        dialog.setVisible(true);
        basicEditor.mCurrentBuilder = dialog.getCurrentBuilder();
    }

    public JFrame getFrame() {
        return frame;
    }

    private void tryCloseWindow() {
        // Stop program running
        if (basicEditor.mMode == ApMode.AP_RUNNING || basicEditor.mMode == ApMode.AP_PAUSED) {
            basicEditor.SetMode(ApMode.AP_STOPPED, null);
            return;
        }

        // Save file before closing
        if (!MultifileCheckSaveChanges()) {
            return;
        }

        //TODO Add libraries
        // Library cleanup functions
        //ShutDownTomWindowsBasicLib();

        frame.dispose();
        System.exit(0);
    }

    private void ResetProject() {
        // Clear out the current project and setup a new basic one with a single
        // source-file.

        // Close existing editors
        tabControl.removeAll();
        fileManager.mFileEditors.clear();

        // Create a default tab
        addTab();


        //Display the editor
        tabControl.setSelectedIndex(0);
    }


    @Override
    public int getFileTabIndex(String filename) {
        return fileManager.getFileTabIndex(filename);
    }

    @Override
    public int getTabIndex(String filePath) {
        return fileManager.getTabIndex(filePath);
    }

    @Override
    public void setSelectedTabIndex(int index) {
        tabControl.setSelectedIndex(index);
    }

    @Override
    public void openTab(String filename) {
        File file = new File(fileManager.mCurrentDirectory, filename);

        System.out.println("Open tab: " + filename);
        System.out.println("Path: " + file.getAbsolutePath());

        MainWindow.this.addTab(FileEditor.open(file, this, fileManager, this, linkGenerator, searchContext));

        tabControl.setSelectedIndex(tabControl.getTabCount() - 1);
    }



    void actionNew() {
        if (MultifileCheckSaveChanges()) {

            fileManager.mRunDirectory = fileManager.mFileDirectory;
            fileManager.mCurrentDirectory = fileManager.mRunDirectory;

            //Clear file editors
            this.tabControl.removeAll();
            fileManager.mFileEditors.clear();

            this.addTab();
        }
    }

    void actionOpen() {
        if (MultifileCheckSaveChanges()) {
            fileManager.mCurrentDirectory = fileManager.mFileDirectory;
            FileEditor editor = FileEditor.open(frame, this, fileManager, this, linkGenerator, searchContext);
            if (editor != null) {
                //TODO Check if file should open as new tab or project
                //For now just open as new project
                //So... close all current tabs
                closeAll();


                // Set current directory to main file directory
                // Must be done BEFORE setting the long filename, because the short
                // filename will be calculated based on the current dir.
                fileManager.mFileDirectory = new File(editor.getFilePath()).getParent();
                fileManager.mRunDirectory = fileManager.mFileDirectory;

                fileManager.mCurrentDirectory = fileManager.mRunDirectory;

                //Display file
                addTab(editor);
            }
        }
    }

    boolean FileCheckSaveChanges(int index) {

        // Is sub-file modified?
        FileEditor editor = fileManager.mFileEditors.get(index);
        if (editor.isModified()) {
            int result = JOptionPane.showConfirmDialog(frame,
                    "Save changes to " + editor.getShortFilename(),
                    "Confirm",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            switch (result) {

                case JOptionPane.YES_OPTION:
                    return actionSave(index);

                case JOptionPane.NO_OPTION:
                    return true;

                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }

        return true;
    }

    boolean MultifileCheckSaveChanges() {
        Mutable<String> description = new Mutable<>("");
        if (fileManager.MultifileModified(description)) {

            int result = JOptionPane.showConfirmDialog(frame,
                    "Save changes to " + description.get(),
                    "Confirm",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            switch (result) {

                case JOptionPane.YES_OPTION:
                    return MultifileSaveAll();

                case JOptionPane.NO_OPTION:
                    return true;

                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }

        return true;
    }

    boolean MultifileSaveAll() {

        // Save all modified files
        for (int i = 0; i < fileManager.mFileEditors.size(); i++) {
            if (fileManager.mFileEditors.get(i).isModified()) {
                tabControl.setSelectedIndex(i);
                if (!actionSave(i)) {
                    return false;
                }
            }
        }

        return true;
    }

    boolean actionSave() {
        //Save content of current tab
        if (fileManager.mFileEditors.isEmpty()
                || this.tabControl.getTabCount() == 0
                || this.tabControl.getSelectedIndex() == -1) {
            return false;
        }

        int index = tabControl.getSelectedIndex();
        boolean saved = fileManager.mFileEditors.get(index).save(false, fileManager.mCurrentDirectory);
        if (saved) {
            //TODO Check if index of main file
            int main = 0;
            if (index == main) {
                fileManager.mFileDirectory = new File(fileManager.mFileEditors.get(index).getFilePath()).getParent();
                fileManager.mRunDirectory = fileManager.mFileDirectory;
                fileManager.mCurrentDirectory = fileManager.mRunDirectory;
            }
            tabControl.setTitleAt(index, fileManager.mFileEditors.get(index).getTitle());
            tabControl.getTabComponentAt(index).invalidate();
        }
        return saved;
    }

    boolean actionSave(int index) {
        //Save content of current tab
        if (fileManager.mFileEditors.isEmpty()
                || this.tabControl.getTabCount() == 0
                || this.tabControl.getSelectedIndex() == -1) {
            return false;
        }

        boolean saved = fileManager.mFileEditors.get(index).save(false, fileManager.mCurrentDirectory);
        if (saved) {
            //TODO Check if main file
            int main = 0;
            if (index == main) {
                fileManager.mFileDirectory = new File(fileManager.mFileEditors.get(index).getFilePath()).getParent();
                fileManager.mRunDirectory = fileManager.mFileDirectory;
                fileManager.mCurrentDirectory = fileManager.mRunDirectory;
            }
            tabControl.setTitleAt(index, fileManager.mFileEditors.get(index).getTitle());
            tabControl.getTabComponentAt(index).invalidate();
        }
        return saved;
    }

    void actionSaveAs() {
        //Save content of current tab as new file
        if (fileManager.mFileEditors.isEmpty()
                || this.tabControl.getTabCount() == 0
                || this.tabControl.getSelectedIndex() == -1) {
            return;
        }
        int index = tabControl.getSelectedIndex();

        fileManager.mCurrentDirectory = fileManager.mFileDirectory;

        if (fileManager.mFileEditors.get(index).save(true, fileManager.mCurrentDirectory)) {
            //TODO get current main file
            int main = 0;
            if (index == main) {
                fileManager.mFileDirectory = new File(fileManager.mFileEditors.get(index).getFilePath()).getParent();
                fileManager.mRunDirectory = fileManager.mFileDirectory;
                fileManager.mCurrentDirectory = fileManager.mRunDirectory;
            }

        } else {
            //Restore Current directory
            fileManager.mCurrentDirectory = fileManager.mRunDirectory;
        }
        tabControl.setTitleAt(index, fileManager.mFileEditors.get(index).getTitle());
        tabControl.getTabComponentAt(index).invalidate();
    }

    private void actionDebugMode() {
        // Toggle debug mode
        isDebugMode = !isDebugMode;
        debugMenuItem.setSelected(isDebugMode);
        debugButton.setSelected(isDebugMode);

        RefreshDebugDisplays(basicEditor.mMode);
    }

    public void closeAll() {
        for (int i = tabControl.getTabCount() - 1; i >= 0; i--) {
            closeTab(i);
        }

        // Reset default run directory to programs folder
        fileManager.mRunDirectory = fileManager.mAppDirectory + "\\Programs";

        // Clear DLLs, breakpoints, bookmarks etc
        //m_dlls.Clear();
        basicEditor.mDebugger.ClearUserBreakPts();

        // Refresh UI
        RefreshActions(basicEditor.mMode);
    }

    public void closeTab(int index) {
        tabControl.remove(index);
        fileManager.mFileEditors.remove(index);
    }

    public void addTab() {
        final FileEditor editor = new FileEditor(this, fileManager, this, linkGenerator, searchContext);
        addTab(editor);
    }

    public void addTab(FileEditor editor) {
        int count = fileManager.editorCount();
        fileManager.mFileEditors.add(editor);
        tabControl.addTab(editor.getTitle(), editor.getContentPane());

        final FileEditor edit = editor;
        edit.editorPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int index = getTabIndex(edit.getFilePath());
                edit.setModified();
                tabControl.setTitleAt(index, edit.getTitle());
//                mTabControl.getTabComponentAt(index).invalidate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int index = getTabIndex(edit.getFilePath());
                edit.setModified();
                tabControl.setTitleAt(index, edit.getTitle());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int index = getTabIndex(edit.getFilePath());
                edit.setModified();
                tabControl.setTitleAt(index, edit.getTitle());
            }
        });

        //Allow user to see cursor position
        editor.editorPane.addCaretListener(TrackCaretPosition);
        cursorPosLabel.setText(0 + ":" + 0); //Reset label

        //Set tab as read-only if App is running or paused
        boolean readOnly = basicEditor.mMode != ApMode.AP_STOPPED;
        editor.editorPane.setEditable(!readOnly);

        //TODO set syntax highlight colors

        //Refresh interface if there was previously no tabs open
        if (count == 0) {
            basicEditor.SetMode(ApMode.AP_STOPPED, null);
        }
    }

    @Override
    public void PlaceCursorAtProcessed(final int row, int col) {

        // Place cursor at position corresponding to row, col in post-processed file.
        // Find corresponding source position
        Mutable<String> filename = new Mutable<String>("");
        Mutable<Integer> fileRow = new Mutable<Integer>(0);
        basicEditor.mPreprocessor.getLineNumberMap().getSourceFromMain(filename, fileRow, row);

        final String file = filename.get();
        final int r = fileRow.get();
        final int c = col;

        // Find (and show) corresponding editor frame
        if (r >= 0) {
            int index = getTabIndex(file);
            if (index == -1) {
                //Attempt to open tab
                addTab(FileEditor.open(new File(file),this, fileManager, this, linkGenerator, searchContext));
                index = tabControl.getTabCount() - 1;
                //return;
            }

            tabControl.setSelectedIndex(index);

            final JTextArea frame = fileManager.mFileEditors.get(getTabIndex(file)).editorPane;

            // Set focus
            frame.grabFocus();

            SwingUtilities.invokeLater(() -> {
                int col1 = c;
                // Place cursor
                if (r >= 0) {
                    try {
                        JTextArea textArea = fileManager.mFileEditors.get(tabControl.getSelectedIndex()).editorPane;
                        int offset = textArea.getLineStartOffset(r);

                        //Reduce column position if it would place the cursor at the next line
                        if (textArea.getLineCount() > r + 1
                                && offset + col1 == textArea.getLineStartOffset(r + 1)) {
                            offset = textArea.getLineStartOffset(r + 1) - 1;
                        } else {
                            offset += col1;
                        }

                        frame.setCaretPosition(offset);
                    } catch (Exception ex) {
                        //Do nothing
                    }
                }
            });
        }
    }


    @Override
    public void onPause() {

        // Place editor into paused mode
        basicEditor.SetMode(ApMode.AP_PAUSED, null);
        RefreshActions(basicEditor.mMode);

        // Place editor into debug mode
        isDebugMode = true;
        debugMenuItem.setSelected(true);
        debugButton.setSelected(true);
        RefreshDebugDisplays(basicEditor.mMode);

        //TODO Add VMViewer
        //VMView().SetVMIsRunning(false);
    }

    @Override
    public void onApplicationClosing() {
        // Get focus back
        frame.requestFocus();
        if (!fileManager.mFileEditors.isEmpty() && tabControl.getTabCount() != 0) {
            //TODO set tab to file that error occurred in
            fileManager.mFileEditors.get(tabControl.getSelectedIndex()).editorPane.grabFocus();
        }
    }

    @Override
    public void setCompilerStatus(String error) {
        compilerStatusLabel.setText(error);
    }


    @Override
    public void onModeChanged(ApMode mode, String statusMsg){
        if (mode != ApMode.AP_CLOSED) {
            copyMenuItem.setEnabled(true);
            findMenuItem.setEnabled(true);
            replaceMenuItem.setEnabled(true);
            selectAllMenuItem.setEnabled(true);

            stepOverButton.setEnabled(true);
            stepInButton.setEnabled(true);
            stepOutButton.setEnabled(true);
            stepOverMenuItem.setEnabled(true);
            stepIntoMenuItem.setEnabled(true);
            stepOutOfMenuItem.setEnabled(true);
            playButton.setEnabled(true);
            playPauseMenuItem.setEnabled(true);
            runMenuItem.setEnabled(true);
            runButton.setEnabled(true);

            saveAsMenuItem.setEnabled(true);
            saveMenuItem.setEnabled(true);
            saveButton.setEnabled(true);

            breakpointSubMenu.setEnabled(true);
            bookmarkSubMenu.setEnabled(true);
        }

        // Update UI
        RefreshActions(basicEditor.mMode);
        RefreshDebugDisplays(basicEditor.mMode);
        compilerStatusLabel.setText(statusMsg);

        // Notify virtual machine view
        //TODO Implement VM Viewer
        //VMView().SetVMIsRunning(mode == ApMode.AP_RUNNING);
    }


    @Override
    public void RefreshActions(ApMode mode) {


        // Enable/disable actions to reflect state
        switch (mode) {
            case AP_CLOSED:
                settingsMenuItem.setEnabled(false);
                exportMenuItem.setEnabled(false);

                openMenuItem.setEnabled(true);
                openButton.setEnabled(true);

                cutMenuItem.setEnabled(false);
                pasteMenuItem.setEnabled(false);
                undoMenuItem.setEnabled(false);
                redoMenuItem.setEnabled(false);

                runMenuItem.setText("Run Program");
                runButton.setIcon(createImageIcon(ICON_RUN_APP));

                copyMenuItem.setEnabled(false);
                findMenuItem.setEnabled(false);
                replaceMenuItem.setEnabled(false);
                selectAllMenuItem.setEnabled(false);

                stepOverButton.setEnabled(false);
                stepInButton.setEnabled(false);
                stepOutButton.setEnabled(false);
                stepOverMenuItem.setEnabled(false);
                stepIntoMenuItem.setEnabled(false);
                stepOutOfMenuItem.setEnabled(false);
                playButton.setEnabled(false);
                playPauseMenuItem.setEnabled(false);
                runMenuItem.setEnabled(false);
                runButton.setEnabled(false);

                saveAsMenuItem.setEnabled(false);
                saveMenuItem.setEnabled(false);
                saveButton.setEnabled(false);

                breakpointSubMenu.setEnabled(false);
                bookmarkSubMenu.setEnabled(false);

                compilerStatusLabel.setText("");
                break;
            case AP_STOPPED:
                setClosingTabsEnabled(true);

                settingsMenuItem.setEnabled(true);
                exportMenuItem.setEnabled(true);

                newMenuItem.setEnabled(true);
                openMenuItem.setEnabled(true);
                newButton.setEnabled(true);
                openButton.setEnabled(true);

                cutMenuItem.setEnabled(true);
                pasteMenuItem.setEnabled(true);
                undoMenuItem.setEnabled(true);
                redoMenuItem.setEnabled(true);

                fileManager.setReadOnly(false);
                runMenuItem.setText("Run Program");
                runButton.setIcon(createImageIcon(ICON_RUN_APP));
                break;

            case AP_RUNNING:
            case AP_PAUSED:
                setClosingTabsEnabled(false);

                settingsMenuItem.setEnabled(false);
                exportMenuItem.setEnabled(false);

                newMenuItem.setEnabled(false);
                openMenuItem.setEnabled(false);
                newButton.setEnabled(false);
                openButton.setEnabled(false);

                cutMenuItem.setEnabled(false);
                pasteMenuItem.setEnabled(false);
                undoMenuItem.setEnabled(false);
                redoMenuItem.setEnabled(false);

                fileManager.setReadOnly(true);
                runMenuItem.setText("Stop Program");
                runButton.setIcon(createImageIcon(ICON_STOP_APP));
                break;

        }
    }

    @Override
    public void RefreshDebugDisplays(ApMode mode) {

        // Show/hide debug controls
        playButton.setVisible(isDebugMode);
        stepOverButton.setVisible(isDebugMode);
        stepInButton.setVisible(isDebugMode);
        stepOutButton.setVisible(isDebugMode);

        //TODO Show/hide debug pane
        if (isDebugMode) {
            mainPane.setResizeWeight(0.7);
            //mDebugPane.setEnabled(true);
            mainPane.setEnabled(true);
            mainPane.setBottomComponent(debugPane);
            SwingUtilities.invokeLater(() -> debugPane.setDividerLocation(0.7));
        } else {
            mainPane.remove(debugPane);
            //mDebugPane.setEnabled(false);
            mainPane.setEnabled(false);
        }

        if (mode != ApMode.AP_CLOSED) {
            playButton.setIcon(mode == ApMode.AP_RUNNING ? createImageIcon(ICON_PAUSE) : createImageIcon(ICON_PLAY));
            playButton.setEnabled(true);
            stepOverButton.setEnabled(mode != ApMode.AP_RUNNING);
            stepInButton.setEnabled(mode != ApMode.AP_RUNNING);

            //TODO 12/2022 determine appropriate state for mStepOutButton;
            // does the editor even need to care about UserCallStack size with remote debugger protocol setup?
            stepOutButton.setEnabled(mode == ApMode.AP_PAUSED);
            //TODO old mStepOutButton.setEnabled(mode == ApMode.AP_PAUSED && (mEditor.mVM.UserCallStack().size() > 0));
        }
        if (!isDebugMode) {
            return;
        }

        if (mode != ApMode.AP_PAUSED) {
            // Clear debug controls
            gosubListModel.clear();
        }
    }

    @Override
    public void updateCallStack(StackTraceCallback stackTraceCallback) {

        // Clear debug controls
        gosubListModel.clear();

        // Update call stack
        gosubListModel.addElement("IP");

        int totalFrames = stackTraceCallback.stackFrames.size(); // callback.totalFrames may be larger if paging is enforced
        for (int i2 = 0; i2 < totalFrames; i2++) {
            StackFrame frame = stackTraceCallback.stackFrames.get(totalFrames - i2 - 1);

            // User functions have positive indices
            Integer userFuncIndex = NumberUtil.parseIntOrNull(frame.name);
            if (userFuncIndex != null) {
                if (userFuncIndex >= 0) {
                    // TODO 12/2022 migrate GetUserFunctionName to LineNumberMapping and handle in the DebugCommandAdapter;
                    //  would like to rely on frame.name to align with Microsoft's DAP specification
                    gosubListModel.addElement(basicEditor.mComp.getUserFunctionName(userFuncIndex) + "()");

                    // Otherwise must be a gosub
                } else {
                    // TODO 12/2022 migrate DescribeStackCall to LineNumberMapping and handle in the DebugCommandAdapter;
                    //  would like to rely on frame.name to align with Microsoft's DAP specification
                    Integer returnAddr = NumberUtil.parseIntOrNull(frame.instructionPointer);
                    String gosubLabel = returnAddr != null
                        ? basicEditor.mComp.DescribeStackCall(returnAddr)
                        : "???";
                    gosubListModel.addElement("gosub " + gosubLabel);
                }
            } else {
                gosubListModel.addElement(frame.name);
            }
        }
    }

    @Override
    public void updateEvaluateWatch(String evaluatedWatch, String result) {
        int index = 0;
        for (String watch : basicEditor.getWatches()) {
            if (Objects.equals(watch, evaluatedWatch)) {
                watchListModel.setElementAt(watch +": " + result, index);
            }
            index++;
        }
    }

    @Override
    public void refreshWatchList() {
        // Clear debug controls
        watchListModel.clear();

        for (String watch : basicEditor.getWatches()) {

            watchListModel.addElement(watch + ": " + "???");
        }
        watchListModel.addElement(" ");              // Last line is blank, and can be clicked on to add new watch
    }

    private void EditWatch() {
        String newWatch, oldWatch;

        // Find watch
        int index = watchListBox.getSelectedIndex();
        int saveIndex = index;

        // Extract watch text
        oldWatch = basicEditor.getWatchOrDefault(index);

        // Prompt for new text
        newWatch = (String) JOptionPane.showInputDialog(frame, "Enter variable/expression:", "Watch variable",
                JOptionPane.PLAIN_MESSAGE, null, null, oldWatch);

        basicEditor.updateWatch(newWatch, index);

        watchListBox.setSelectedIndex(saveIndex);
        UpdateWatchHint();
    }

    void DeleteWatch() {

        // Find watch
        int index = watchListBox.getSelectedIndex();
        int saveIndex = index;

        // Delete watch
        basicEditor.removeWatchAt(index);

        watchListBox.setSelectedIndex(saveIndex);
        UpdateWatchHint();
    }

    private void UpdateWatchHint() {
        int index = watchListBox.getSelectedIndex();
        if (index > -1 && index < basicEditor.getWatchListSize()) {
            watchListBox.setToolTipText((String) watchListModel.get(index));
        } else {
            watchListBox.setToolTipText("");
        }
    }

    @Override
    public void onToggleBreakpoint(String filePath, int line) {
        basicEditor.toggleBreakpt(filePath, line);
    }

    private void setClosingTabsEnabled(boolean enabled){
        tabControl.putClientProperty( TABBED_PANE_TAB_CLOSABLE, enabled );
        //TODO get main file index
//        int main = 0;
        //TODO only disable closing the tab with the main program
//        if (enabled) {
//            for (int i = 0; i < mTabControl.getTabCount(); i++) {
//                ((ButtonTabComponent) mTabControl.getTabComponentAt(i)).getButton().setEnabled(true);
//            }
//        } else {
//            if (main > -1 && main < mTabControl.getTabCount()) {
//                ((ButtonTabComponent) mTabControl.getTabComponentAt(main)).getButton().setEnabled(false);
//            }
//        }
    }

    private void showFindReplaceMenu(boolean replace) {
        if (fileManager.mFileEditors.isEmpty()
                || this.tabControl.getTabCount() == 0
                || this.tabControl.getSelectedIndex() == -1) {

            return;
        }

        int index = tabControl.getSelectedIndex();
        fileManager.mFileEditors.get(index).toggleFindToolBar(replace);
    }

    @Override
    public void onSearchResult(String message) {
        setCompilerStatus(message);
    }
}