package com.basic4gl.desktop;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;
import static com.basic4gl.desktop.util.SwingIconUtil.createScaledIcon;
import static com.basic4gl.desktop.util.SwingUtil.hideSplitPaneHandle;
import static com.formdev.flatlaf.FlatClientProperties.*;

import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.desktop.content.*;
import com.basic4gl.desktop.debugger.DebugServerConstants;
import com.basic4gl.desktop.debugger.DebugServerFactory;
import com.basic4gl.desktop.debugger.IDebugPresenter;
import com.basic4gl.desktop.editor.*;
import com.basic4gl.desktop.language.SymbolIndexer;
import com.basic4gl.desktop.panels.*;
import com.basic4gl.desktop.spi.*;
import com.basic4gl.desktop.util.BasicDialogService;
import com.basic4gl.desktop.util.RoundedCardPanel;
import com.basic4gl.desktop.vmview.DebugControlsListener;
import com.basic4gl.desktop.vmview.VirtualMachineViewDialog;
import com.basic4gl.language.core.internal.Mutable;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.SearchContext;

/**
 * Created by Nate on 2/24/2015.
 */
public class MainWindow
        implements IEditorPresenter,
                ITabProvider,
                IToggleBreakpointListener,
                IFileEditorActionListener,
                IFileManagerListener,
                EmptyTabPanel.IEmptyTabPanelListener,
                MenuService,
                EditorCommandsService {

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

                cursorPositionLabel.setText((column + 1) + ":" + (row + 1));
            } catch (BadLocationException ex) {
                cursorPositionLabel.setText(0 + ":" + 0);
                ex.printStackTrace();
            }
        }
    };

    // Window
    private final JFrame frame = new JFrame(BuildInfo.APPLICATION_NAME);
    private final JSplitPane mainPane;
    private final JTabbedPane tabControl = new JTabbedPane();
    private final JTabbedPane splitTabControl = new JTabbedPane();
    private final JSplitPane editorSplitPane;
    private final JPanel primaryTabHost = new JPanel(new BorderLayout());
    private final JButton addTabDropdownButton = new JButton(createScaledIcon(ICON_ADD, 18));
    private final JPanel fileViewModeTabs = createSegmentedButtonStrip();
    private final JToggleButton editViewButton =
            createFileViewModeButton("Editor", ICON_EDIT, IFileViewer.ViewMode.EDITOR, "first");
    private final JToggleButton editPreviewViewButton = createFileViewModeButton(
            "Editor and Preview", ICON_EDIT_PREVIEW, IFileViewer.ViewMode.EDITOR_AND_PREVIEW, "middle");
    private final JToggleButton previewViewButton =
            createFileViewModeButton("Preview", ICON_PREVIEW, IFileViewer.ViewMode.PREVIEW, "last");
    private final JPanel fileViewModeTabsHost = createSegmentedButtonStripHost(fileViewModeTabs);
    private final JPanel centerPaneHost = new JPanel(new BorderLayout());
    private final JPanel topPaneHost = new JPanel(new BorderLayout());
    private final JPanel leftRailsHost = new JPanel();
    private final JSplitPane workspacePane;
    private final JSplitPane contentPane;
    private final JPanel bottomBarContainer = new JPanel(new BorderLayout());
    private final JPanel leftSidebarContent = new JPanel(new CardLayout());
    private final JToolBar leftSidebarRail = new JToolBar(SwingConstants.VERTICAL);
    private final ButtonGroup leftSidebarGroup = new ButtonGroup();
    private final Map<String, JToggleButton> leftSidebarButtons = new HashMap<>();
    private final JPanel bottomBarContent = new JPanel(new CardLayout());
    private final JToolBar bottomBarRail = new JToolBar(SwingConstants.VERTICAL);
    private final ButtonGroup bottomBarGroup = new ButtonGroup();
    private final Map<String, JToggleButton> bottomBarButtons = new HashMap<>();

    private final JPanel rightDocsContainer = new JPanel(new BorderLayout());
    private final JPanel rightDocsContent = new JPanel(new CardLayout());
    private final JToolBar rightDocsRail = new JToolBar(SwingConstants.VERTICAL);
    private final ButtonGroup rightDocsGroup = new ButtonGroup();
    private final Map<String, JToggleButton> rightDocsButtons = new HashMap<>();

    private final JButton runTargetButton = new JButton();
    private final JPopupMenu runTargetPopup = new JPopupMenu();
    private boolean runTargetFollowsCurrentTab = true;

    private int expandedLeftSidebarWidth = 260;
    private int expandedRightDocsWidth = 320;
    private int expandedBottomBarHeight = 220;
    private int workspacePaneDividerSize;
    private int contentPaneDividerSize;
    private int mainPaneDividerSize;
    private boolean leftSidebarCollapsed;
    private boolean rightDocsCollapsed;
    private boolean bottomBarCollapsed;
    private String activeLeftSidebarKey = "files";
    private String activeRightDocsKey;
    private String activeBottomBarKey;
    private JPanel emptyTabPanel;
    private final java.util.List<File> recentWorkspaces = new ArrayList<>();
    private static final String RECENT_WORKSPACES_FILE = "recent-workspaces.properties";
    private static final String RECENT_WORKSPACES_KEY = "RECENT_WORKSPACES";
    private static final int MAX_RECENT_WORKSPACES = 10;
    private static final Dimension TAB_HEADER_ICON_BUTTON_SIZE = new Dimension(30, 30);
    private static final Dimension TAB_VIEW_MODE_BUTTON_SIZE = new Dimension(34, 30);
    private static final String TAB_FILE_VIEWER_PROPERTY = "basic4gl.fileViewer";
    private static final Color SEGMENTED_BACKGROUND = new Color(0xE0E0E0);
    private static final String SEGMENTED_BUTTON_STYLE = "arc: 14; borderWidth: 0; focusWidth: 0; innerFocusWidth: 0;"
            + " margin: 6,8,6,8; background: #E0E0E0;"
            + " hoverBackground: #D6D6D6; selectedBackground: #FFFFFF";

    private final JMenu bookmarkSubMenu = new JMenu("Bookmarks");
    private final JMenu breakpointSubMenu = new JMenu("Breakpoints");
    private final JMenu helpMenu = new JMenu("Help");

    // Menu Items
    private final JMenuItem newMenuItem = new JMenuItem("New Program");
    private final JMenuItem openMenuItem = new JMenuItem("Open Program...");
    private final JMenuItem openFolderMenuItem = new JMenuItem("Open Folder...");
    private final JMenuItem recentSubMenu = new JMenu("Open Recent");
    private final JMenuItem clearRecentMenuItem = new JMenuItem("Clear Recently Opened...");
    private final JMenuItem clearRecentWorkspacesMenuItem = new JMenuItem("Clear Recent Workspaces...");
    private final JMenuItem saveMenuItem = new JMenuItem("Save");
    private final JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
    private final JMenuItem exportMenuItem = new JMenuItem("Export...");

    private final JMenuItem undoMenuItem = new JMenuItem("Undo");
    private final JMenuItem redoMenuItem = new JMenuItem("Redo");
    private final JMenuItem cutMenuItem = new JMenuItem("Cut");
    private final JMenuItem copyMenuItem = new JMenuItem("Copy");
    private final JMenuItem pasteMenuItem = new JMenuItem("Paste");
    private final JMenuItem selectAllMenuItem = new JMenuItem("Select All");

    private final JMenuItem findMenuItem = new JMenuItem("Find");
    private final JMenuItem replaceMenuItem = new JMenuItem("Replace");
    private final JMenuItem goToDeclarationMenuItem = new JMenuItem("Go to Declaration");
    private final JCheckBoxMenuItem debugMenuItem = new JCheckBoxMenuItem("Debug Mode");

    private final JMenuItem settingsMenuItem = new JMenuItem("Project Settings");
    private final JMenuItem runMenuItem = new JMenuItem("Run Program");
    private final JMenuItem playPauseMenuItem = new JMenuItem("Play/Pause");
    private final JMenuItem stepOverMenuItem = new JMenuItem("Step Over");
    private final JMenuItem stepIntoMenuItem = new JMenuItem("Step Into");
    private final JMenuItem stepOutOfMenuItem = new JMenuItem("Step Out of");
    private final JMenuItem viewVirtualMachineMenuItem = new JMenuItem("View Virtual Machine");

    // Toolbar Buttons
    private final JButton newButton = new JButton(createImageIcon(ICON_NEW));
    private final JButton openButton = new JButton(createImageIcon(ICON_OPEN));
    private final JButton saveButton = new JButton(createImageIcon(ICON_SAVE));
    private final JButton runButton = new JButton(createImageIcon(ICON_RUN_APP));

    private final JButton exportButton = new JButton(createImageIcon(ICON_EXPORT));
    private final JButton settingsButton = new JButton(createImageIcon(ICON_SETTINGS));
    // Labels
    private final JLabel compilerStatusLabel = new JLabel(""); // Compiler/VM Status
    private final JLabel cursorPositionLabel = new JLabel("0:0"); // Cursor Position

    // Editors
    private BasicEditor basicEditor;
    private FileManager fileManager;

    IEditorPanelProvider[] panels;

    private IncludeLinkGenerator linkGenerator = new IncludeLinkGenerator(this);

    private SearchContext searchContext;

    // Debugging
    private VirtualMachineViewDialog virtualMachineViewDialog;
    private IDebugPresenter debugPresenter;
    private int lastSourceRow = -1;
    private int lastSourceColumn = -1;

    // Set when stepping. Delays switching to the output window for the first 1000 op-codes.
    // (To prevent excessive screen mode switches when debugging full-screen programs.)
    private boolean isDelayScreenSwitchEnabled = false;

    // TODO create config file
    private static String debugServerBinPath;
    private static String outputBinPath;
    private static String applicationStoragePath;

    public static void main(String[] args) {
        // Location to store logs
        applicationStoragePath =
                System.getProperty("user.home") + System.getProperty("file.separator") + BuildInfo.APPLICATION_NAME;

        if (args.length >= 2) {
            // for debugging; set by gradle debugAll
            outputBinPath = args[0];
            debugServerBinPath = args[1];
        } else if (System.getProperty("jpackage.app-path") != null) {
            // app was built with jpackage; the output window and debug server binaries should be bundled
            // with it
            String appPath = System.getProperty("jpackage.app-path");
            File appDirectory = new File(appPath).getParentFile();
            // TODO these should NOT be hardcoded; figure out how to generate some appconfig.json with
            // builds to include these as config settings
            outputBinPath = new File(appDirectory, "Basic4GLjOutput").getAbsolutePath();
            debugServerBinPath = new File(appDirectory, "Basic4GLjDebugServer").getAbsolutePath();
        } else {
            // TODO these should NOT be hardcoded; figure out how to generate some appconfig.json with
            // builds to include these as config settings
            String appHome = System.getenv("APP_HOME"); // APP_HOME is defined in scripts distributed with build
            if (appHome != null && !appHome.trim().isEmpty()) {
                File appDirectory = new File(appHome);
                File outputBin = new File(appDirectory, "lib/app-runtime-1.0-SNAPSHOT.jar");
                File debugServerBin = new File(appDirectory, "lib/debug-server-1.0-SNAPSHOT.jar");

                if (outputBin.exists()) {
                    outputBinPath = outputBin.getAbsolutePath();
                } else {
                    outputBinPath = "lib/app-runtime-1.0-SNAPSHOT.jar";
                }

                if (debugServerBin.exists()) {
                    debugServerBinPath = debugServerBin.getAbsolutePath();
                } else {
                    debugServerBinPath = "lib/debug-server-1.0-SNAPSHOT.jar";
                }
            } else {
                outputBinPath = "lib/app-runtime-1.0-SNAPSHOT.jar";
                debugServerBinPath = "lib/debug-server-1.0-SNAPSHOT.jar";
            }
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Basic4GLj");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Basic4GLj");

        FlatLightLaf.setup();
        UIManager.put("SplitPaneDivider.style", "plain");

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

        mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        workspacePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        JMenu debugMenu = new JMenu("Debug");
        menuBar.add(debugMenu);
        JMenu appMenu = new JMenu("Application");
        menuBar.add(appMenu);

        menuBar.add(helpMenu);

        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(openFolderMenuItem);
        fileMenu.add(recentSubMenu);
        fileMenu.add(new JSeparator());
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
        editMenu.add(goToDeclarationMenuItem);
        editMenu.add(new JSeparator());
        editMenu.add(selectAllMenuItem);

        viewMenu.add(bookmarkSubMenu);
        JMenuItem nextBookmarkMenuItem = new JMenuItem("Next");
        bookmarkSubMenu.add(nextBookmarkMenuItem);
        JMenuItem previousBookmarkMenuItem = new JMenuItem("Previous");
        bookmarkSubMenu.add(previousBookmarkMenuItem);
        JMenuItem toggleBookmarkMenuItem = new JMenuItem("Toggle Bookmark");
        bookmarkSubMenu.add(toggleBookmarkMenuItem);

        debugMenu.add(playPauseMenuItem);
        debugMenu.add(stepOverMenuItem);
        debugMenu.add(stepIntoMenuItem);
        debugMenu.add(stepOutOfMenuItem);
        debugMenu.add(new JSeparator());
        debugMenu.add(breakpointSubMenu);
        JMenuItem nextBreakpointMenuItem = new JMenuItem("View Next");
        breakpointSubMenu.add(nextBreakpointMenuItem);
        JMenuItem previousBreakpointMenuItem = new JMenuItem("View Previous");
        breakpointSubMenu.add(previousBreakpointMenuItem);
        JMenuItem toggleBreakpointMenuItem = new JMenuItem("Toggle Breakpoint");
        breakpointSubMenu.add(toggleBreakpointMenuItem);
        debugMenu.add(new JSeparator());
        debugMenu.add(debugMenuItem);
        debugMenu.add(new JSeparator());
        debugMenu.add(viewVirtualMachineMenuItem);

        appMenu.add(runMenuItem);
        appMenu.add(new JSeparator());
        appMenu.add(settingsMenuItem);

        JMenuItem functionListMenuItem = new JMenuItem("Function List");
        helpMenu.add(functionListMenuItem);
        helpMenu.add(new JSeparator());
        JMenuItem aboutMenuItem = new JMenuItem("About");
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
        openFolderMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, toolkit.getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
        openFolderMenuItem.addActionListener(e -> actionOpenFolder());
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, toolkit.getMenuShortcutKeyMask()));
        clearRecentMenuItem.addActionListener(e -> actionClearRecent());
        clearRecentWorkspacesMenuItem.addActionListener(e -> actionClearRecentWorkspaces());
        saveMenuItem.addActionListener(e -> actionSave());
        saveAsMenuItem.addActionListener(e -> actionSaveAs());
        exportMenuItem.addActionListener(e -> actionExport());
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
            fileManager.paste(i);
        });
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, toolkit.getMenuShortcutKeyMask()));
        findMenuItem.addActionListener(e -> {
            showFindReplaceMenu(false);
        });
        replaceMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.SHIFT_MASK | toolkit.getMenuShortcutKeyMask()));
        replaceMenuItem.addActionListener(e -> {
            showFindReplaceMenu(true);
        });
        goToDeclarationMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, toolkit.getMenuShortcutKeyMask()));
        goToDeclarationMenuItem.addActionListener(e -> actionGoToDeclaration());
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, toolkit.getMenuShortcutKeyMask()));
        selectAllMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.selectAll(i);
        });
        nextBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        nextBookmarkMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.selectNextBookmark(i);
        });
        previousBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_MASK));
        previousBookmarkMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.selectPreviousBookmark(i);
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
        viewVirtualMachineMenuItem.addActionListener(e -> {
            if (virtualMachineViewDialog == null || !virtualMachineViewDialog.isDisplayable()) {
                virtualMachineViewDialog = new VirtualMachineViewDialog(frame);
                virtualMachineViewDialog.setSeeValueHandler(
                        expression -> basicEditor.evaluateVmViewVariable(expression));
                virtualMachineViewDialog.setDebugControlsHandler(new DebugControlsListener() {
                    @Override
                    public void onPlayPauseRequested() {
                        basicEditor.actionPlayPause();
                    }

                    @Override
                    public void onStepRequested() {
                        basicEditor.actionStepInto();
                    }

                    @Override
                    public void onStepOverRequested() {
                        basicEditor.actionStep();
                    }

                    @Override
                    public void onStepOutRequested() {
                        basicEditor.actionStepOutOf();
                    }
                });
            }
            if (lastSourceRow >= 0) {
                virtualMachineViewDialog.setCurrentSourcePosition(lastSourceRow, lastSourceColumn);
            }
            virtualMachineViewDialog.setVmRunning(basicEditor.getMode() == ApMode.AP_RUNNING);
            virtualMachineViewDialog.setVisible(true);
            if (basicEditor.getMode() == ApMode.AP_PAUSED) {
                basicEditor.refreshCallStack();
                basicEditor.refreshDisassembly();
                basicEditor.refreshVariables();
            }
        });
        toggleBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, toolkit.getMenuShortcutKeyMask()));
        toggleBookmarkMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.toggleBookmark(i);
        });
        nextBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        nextBreakpointMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.selectNextBreakpoint(i);
        });
        previousBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK));
        previousBreakpointMenuItem.addActionListener(e -> {
            int i = tabControl.getSelectedIndex();
            fileManager.selectPreviousBreakpoint(i);
        });
        toggleBreakpointMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, toolkit.getMenuShortcutKeyMask()));
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
            selectRightDocsSection("symbols");
        });

        aboutMenuItem.addActionListener(e -> showAboutDialog());

        if (SystemInfo.isMacOS) {
            // hide menu items that are in macOS application menu
            aboutMenuItem.setVisible(false);
            settingsMenuItem.setVisible(false);
            // TODO mExitMenuItem.setVisible(false);
        }

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(runTargetButton);
        toolBar.add(runButton);
        toolBar.addSeparator();
        toolBar.add(exportButton);
        toolBar.add(settingsButton);

        newButton.addActionListener(e -> actionNew());
        openButton.addActionListener(e -> actionOpen());
        saveButton.addActionListener(e -> actionSave());
        runButton.addActionListener(e -> basicEditor.actionRun());

        exportButton.addActionListener(e -> actionExport());
        settingsButton.addActionListener(e -> showSettings());
        runButton.setToolTipText("Run the program!");
        runTargetButton.setToolTipText("Select the runnable source file");
        runTargetButton.setMaximumSize(new Dimension(260, 30));
        runTargetButton.setFocusable(false);
        runTargetButton.setIcon(createScaledIcon(ICON_CHEVRON_DOWN, 18));
        runTargetButton.setHorizontalTextPosition(SwingConstants.LEFT);
        runTargetButton.setIconTextGap(6);
        runTargetButton.putClientProperty("JButton.buttonType", "toolBarButton");
        runTargetButton.setOpaque(false);
        runTargetButton.setMargin(new Insets(5, 8, 5, 8));
        Font runTargetFont = runTargetButton.getFont();
        runTargetButton.setFont(new Font(runTargetFont.getName(), Font.BOLD, runTargetFont.getSize()));
        runTargetButton.setForeground(new Color(0x424242));
        runTargetButton.addActionListener(e -> showRunTargetPopup());

        toolBar.setAlignmentY(1);
        toolBar.setFloatable(false);

        // Status Panel
        JPanel panelStatusInfo = new JPanel(new BorderLayout());
        JPanel panelStatusCursor = new JPanel();

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));

        statusPanel.add(panelStatusCursor);
        statusPanel.add(new JSeparator(JSeparator.VERTICAL));
        statusPanel.add(panelStatusInfo);

        panelStatusCursor.add(cursorPositionLabel, BorderLayout.CENTER);
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

        tabControl.putClientProperty(TABBED_PANE_TAB_CLOSABLE, true);
        tabControl.putClientProperty(TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "Close");
        tabControl.putClientProperty(
                TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabPane, tabIndex) -> {
                    if (tabIndex != -1) {
                        if (fileCheckSaveChanges(tabIndex)) {
                            // Clear file's breakpoints
                            FileEditor editor = fileManager.getFileEditors().get(tabIndex);
                            if (editor != null) {
                                List<Integer> breakpoints = editor.getBreakpoints();
                                String file = editor.getFilePath();

                                for (Integer line : breakpoints) {
                                    basicEditor.toggleBreakpt(file, line);
                                }
                            }

                            // Remove tab
                            tabControl.remove(tabIndex);
                            fileManager.getFileEditors().remove(tabIndex.intValue());
                            refreshFileViewModeButtons();
                            fileManager.ensureRunnableFileValid();
                            refreshRunnableFileControls();

                            // Refresh controls if no files open
                            if (fileManager.editorCount() == 0) {
                                basicEditor.setMode(ApMode.AP_CLOSED, null);
                            }
                        }
                    }
                });

        configurePrimaryTabHost();
        configureSplitTabs();
        configureTabContextMenu();

        editorSplitPane.setLeftComponent(primaryTabHost);
        editorSplitPane.setRightComponent(splitTabControl);
        editorSplitPane.setResizeWeight(0.7);
        hideSplitPaneHandle(editorSplitPane);

        contentPane.setLeftComponent(primaryTabHost);
        contentPane.setRightComponent(rightDocsContainer);
        contentPane.setResizeWeight(0.74);
        hideSplitPaneHandle(contentPane);
        contentPaneDividerSize = contentPane.getDividerSize();

        workspacePane.setLeftComponent(leftSidebarContent);
        workspacePane.setRightComponent(contentPane);
        workspacePane.setResizeWeight(0.18);
        workspacePane.setDividerLocation(expandedLeftSidebarWidth);
        hideSplitPaneHandle(workspacePane);
        workspacePaneDividerSize = workspacePane.getDividerSize();

        contentPane.setDividerLocation(Math.max(200, frame.getPreferredSize().width - expandedRightDocsWidth));

        topPaneHost.add(workspacePane, BorderLayout.CENTER);

        mainPane.setTopComponent(topPaneHost);
        mainPane.setBottomComponent(bottomBarContainer);
        mainPane.setResizeWeight(1.0);
        hideSplitPaneHandle(mainPane);
        mainPaneDividerSize = mainPane.getDividerSize();

        leftRailsHost.setLayout(new BoxLayout(leftRailsHost, BoxLayout.Y_AXIS));
        leftRailsHost.add(leftSidebarRail);
        leftRailsHost.add(Box.createVerticalGlue());
        leftRailsHost.add(bottomBarRail);

        centerPaneHost.add(leftRailsHost, BorderLayout.WEST);
        centerPaneHost.add(mainPane, BorderLayout.CENTER);
        centerPaneHost.add(rightDocsRail, BorderLayout.EAST);

        // Add controls to window
        frame.add(toolBar, BorderLayout.NORTH);
        frame.add(centerPaneHost, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);
        frame.setJMenuBar(menuBar);

        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosing(WindowEvent e) {
                tryCloseWindow();
            }

            @Override
            public void windowClosed(WindowEvent e) {}

            @Override
            public void windowIconified(WindowEvent e) {}

            @Override
            public void windowDeiconified(WindowEvent e) {}

            @Override
            public void windowActivated(WindowEvent e) {}

            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        // Initialize syntax highlighting
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        // TODO don't hardcode this classname
        atmf.putMapping("text/basic4gl", "com.basic4gl.desktop.editor.BasicTokenMaker");

        fileManager = new FileManager(this);

        basicEditor = new BasicEditor(outputBinPath, fileManager, this, new BasicDialogService(this.frame), this, this);

        debugPresenter = new DebugPanelProvider(basicEditor);

        panels = new IEditorPanelProvider[] {
            new FileBrowserPanelProvider(),
            new AssetsPanelProvider(fileManager),
            new BookmarksPanelProvider(),
            (IEditorPanelProvider) debugPresenter,
            new SymbolsPanelProvider(),
            new DocsPanelProvider(),
        };

        configureLeftSidebar();
        configureRightSidebar();

        // TODO Confirm this doesn't break if app is ever signed
        // getParent
        fileManager.setAppDirectory(new File(".").getAbsolutePath());

        if (new File(fileManager.getAppDirectory(), "Programs").exists()) {
            fileManager.setRunDirectory(fileManager.getAppDirectory() + "\\Programs");
        } else {
            fileManager.setRunDirectory(fileManager.getAppDirectory());
        }
        fileManager.setFileDirectory(fileManager.getRunDirectory());
        fileManager.setCurrentDirectory(fileManager.getFileDirectory());

        basicEditor.onCurrentDirectoryChanged(fileManager.getCurrentDirectory());

        // TODO this should be done as a callback
        refreshActions(basicEditor.getMode());
        refreshDebugDisplays(basicEditor.getMode());

        basicEditor.initLibraries();
        resetProject();
        basicEditor.loadSettings();
        loadRecentWorkspaces();
        setRecentItems(basicEditor.getRecentFiles());
        refreshRunnableFileControls();
        refreshSidebarContent();

        // Warm up the debug server
        DebugServerFactory.startDebugServer(debugServerBinPath, DebugServerConstants.DEFAULT_DEBUG_SERVER_PORT);

        // Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            collapseBottomBar();
            collapseRightDocs();
        });
    }

    private void actionExport() {
        basicEditor.setMode(ApMode.AP_STOPPED, null);
        if (fileManager.editorCount() == 0) {
            JOptionPane.showMessageDialog(frame, "Nothing to export", "Cannot export", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clear source code from parser
        basicEditor.getCompiler().clear();

        if (!basicEditor.loadProgramIntoCompiler()) {
            compilerStatusLabel.setText(basicEditor.getPreprocessor().getError());
            return;
        }
        List<ProjectExportPage> contributedExportPages =
                Arrays.asList(basicEditor.getBasic4gl().getProjectExportPages());
        ExportDialog dialog = new ExportDialog(
                frame,
                basicEditor.getCompiler(),
                basicEditor.getPreprocessor(),
                basicEditor.getLanguageService(),
                fileManager.getFileEditors(),
                fileManager.getCurrentDirectory(),
                contributedExportPages);
        dialog.setBuilders(basicEditor.getBuilders(), basicEditor.currentBuilder);
        dialog.setVisible(true);
        basicEditor.currentBuilder = dialog.getCurrentBuilder();
    }

    @Override
    public void setRecentItems(List<File> files) {
        List<File> recentFiles = files == null ? Collections.emptyList() : files;
        recentSubMenu.removeAll();

        boolean hasRecentFiles = !recentFiles.isEmpty();
        boolean hasRecentWorkspaces = !recentWorkspaces.isEmpty();

        if (hasRecentFiles) {
            JMenuItem filesHeader = new JMenuItem("Recent Files");
            filesHeader.setEnabled(false);
            recentSubMenu.add(filesHeader);
        }

        for (File file : recentFiles) {

            JMenuItem fileMenuItem = new JMenuItem(file.getName());
            recentSubMenu.add(fileMenuItem);
            fileMenuItem.addActionListener(e -> {
                openFileWithPreferredViewer(file);
            });
        }

        if (hasRecentFiles || hasRecentWorkspaces) {
            recentSubMenu.add(new JSeparator());
        }

        clearRecentMenuItem.setEnabled(hasRecentFiles);
        recentSubMenu.add(clearRecentMenuItem);

        if (hasRecentWorkspaces) {
            recentSubMenu.add(new JSeparator());
            JMenuItem workspacesHeader = new JMenuItem("Recent Workspaces");
            workspacesHeader.setEnabled(false);
            recentSubMenu.add(workspacesHeader);
            for (File workspace : recentWorkspaces) {
                JMenuItem workspaceItem = new JMenuItem(
                        workspace.getName().isBlank() ? workspace.getAbsolutePath() : workspace.getName());
                workspaceItem.setToolTipText(workspace.getAbsolutePath());
                workspaceItem.addActionListener(e -> setWorkspaceDirectory(workspace));
                recentSubMenu.add(workspaceItem);
            }
            recentSubMenu.add(new JSeparator());
            recentSubMenu.add(clearRecentWorkspacesMenuItem);
        }
        clearRecentWorkspacesMenuItem.setEnabled(hasRecentWorkspaces);
    }

    @Override
    public void refreshSyntaxHighlighting() {
        if (fileManager == null) {
            return;
        }
        for (FileEditor editor : fileManager.getFileEditors()) {
            editor.refreshSyntaxHighlighting();
        }
    }

    private void showAboutDialog() {
        new AboutDialog(frame);
    }

    private void showSettings() {
        List<ProjectSettingsPage> contributedPages =
                Arrays.asList(basicEditor.getBasic4gl().getProjectSettingsPages());
        ProjectSettingsDialog dialog = new ProjectSettingsDialog(
                frame,
                basicEditor.getBasic4gl().getConfigurableAppSettings(),
                contributedPages,
                basicEditor::refreshSyntaxHighlighting);
        dialog.setBuilders(basicEditor.getBuilders(), basicEditor.currentBuilder);
        dialog.setVisible(true);
        basicEditor.currentBuilder = dialog.getCurrentBuilder();
    }

    public JFrame getFrame() {
        return frame;
    }

    private void tryCloseWindow() {
        // Stop or cancel active runtime before allowing close.
        if (basicEditor.getMode() == ApMode.AP_RUNNING
                || basicEditor.getMode() == ApMode.AP_PAUSED
                || basicEditor.getMode() == ApMode.AP_WAITING) {
            basicEditor.stopOrCancelRunningApplication();
            return;
        }

        // Save file before closing
        if (!multifileCheckSaveChanges()) {
            return;
        }

        // TODO Add libraries
        // Library cleanup functions
        // ShutDownTomWindowsBasicLib();

        frame.dispose();
        for (IEditorPanelProvider panel : panels) {
            panel.dispose();
        }
        System.exit(0);
    }

    private void resetProject() {
        // Clear out the current project and setup a new basic one with a single
        // source-file.

        // Close existing editors
        tabControl.removeAll();
        fileManager.getFileEditors().clear();
        refreshFileViewModeButtons();

        // Create a default tab
        addTab();

        // Display the editor
        tabControl.setSelectedIndex(0);
        fileManager.ensureRunnableFileValid();
        refreshRunnableFileControls();
        refreshSidebarContent();
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
        File file = new File(fileManager.getCurrentDirectory(), filename);
        int existingIndex = findOpenTabIndexByPath(file.getAbsolutePath());
        if (existingIndex >= 0) {
            tabControl.setSelectedIndex(existingIndex);
            return;
        }

        System.out.println("Open tab: " + filename);
        System.out.println("Path: " + file.getAbsolutePath());

        MainWindow.this.addTab(FileEditor.open(file, this, fileManager, this, linkGenerator, searchContext));

        tabControl.setSelectedIndex(tabControl.getTabCount() - 1);
    }

    public void openTab(File file) {
        if (file == null) {
            return;
        }
        File absoluteFile = file.getAbsoluteFile();
        int existingIndex = findOpenTabIndexByPath(absoluteFile.getAbsolutePath());
        if (existingIndex >= 0) {
            tabControl.setSelectedIndex(existingIndex);
            return;
        }

        System.out.println("Open tab: " + file.getName());
        System.out.println("Path: " + file.getAbsolutePath());

        // Use the FileViewerFactory to determine the appropriate viewer
        IFileViewer viewer = FileViewerFactory.createViewer(
                absoluteFile,
                null, // auto-detect viewer type
                this,
                fileManager,
                this,
                linkGenerator,
                searchContext,
                basicEditor);

        addTab(viewer);

        tabControl.setSelectedIndex(tabControl.getTabCount() - 1);
        registerWorkspace(file.getParentFile());
    }

    void actionNew() {
        if (multifileCheckSaveChanges()) {

            fileManager.setRunDirectory(fileManager.getFileDirectory());
            fileManager.setCurrentDirectory(fileManager.getRunDirectory());

            // Clear file editors
            this.tabControl.removeAll();
            fileManager.getFileEditors().clear();
            refreshFileViewModeButtons();

            this.addTab();
            refreshSidebarContent();
        }
    }

    void actionOpen() {
        actionOpen(null);
    }

    void actionOpen(File file) {
        if (multifileCheckSaveChanges()) {
            if (file != null) {
                fileManager.setCurrentDirectory(fileManager.getFileDirectory());
                openFileWithPreferredViewer(file);
                return;
            }

            FileEditor editor;
            {
                fileManager.setCurrentDirectory(fileManager.getFileDirectory());
                editor = FileEditor.open(frame, this, fileManager, this, linkGenerator, searchContext);
            }

            openEditor(editor);
        }
    }

    @Override
    public void actionOpenFolder() {
        JFileChooser chooser = new JFileChooser(fileManager.getCurrentDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        int result = chooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        setWorkspaceDirectory(chooser.getSelectedFile());
    }

    @Override
    public void openFileWithPreferredViewer(File file) {
        if (file == null) {
            return;
        }
        File absoluteFile = file.getAbsoluteFile();
        int existingIndex = findOpenTabIndexByPath(absoluteFile.getAbsolutePath());
        if (existingIndex >= 0) {
            tabControl.setSelectedIndex(existingIndex);
            return;
        }
        openTab(absoluteFile);
    }

    void openEditor(FileEditor editor) {
        if (editor != null) {
            // TODO Check if file should open as new tab or project
            // For now just open as new project
            // So... close all current tabs
            closeAll();

            // Set current directory to main file directory
            // Must be done BEFORE setting the long filename, because the short
            // filename will be calculated based on the current dir.
            fileManager.setFileDirectory(new File(editor.getFilePath()).getParent());
            fileManager.setRunDirectory(fileManager.getFileDirectory());

            fileManager.setCurrentDirectory(fileManager.getRunDirectory());
            registerWorkspace(new File(fileManager.getRunDirectory()));

            // Display file
            addTab(editor);
            refreshSidebarContent();
        }
    }

    void actionClearRecent() {
        int result = JOptionPane.showConfirmDialog(
                frame,
                "Clear recently opened files?",
                "Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            basicEditor.clearRecentFiles();
        }
    }

    void actionClearRecentWorkspaces() {
        int result = JOptionPane.showConfirmDialog(
                frame,
                "Clear recently opened workspaces?",
                "Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            recentWorkspaces.clear();
            saveRecentWorkspaces();
            setRecentItems(basicEditor.getRecentFiles());
            refreshEmptyStateRecentItems();
        }
    }

    boolean fileCheckSaveChanges(int index) {

        // Is sub-file modified?
        FileEditor editor = fileManager.getFileEditors().get(index);
        if (editor == null) {
            return true;
        }
        if (editor.isModified()) {
            int result = JOptionPane.showConfirmDialog(
                    frame,
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

    boolean multifileCheckSaveChanges() {
        Mutable<String> description = new Mutable<>("");
        if (fileManager.isMultifileModified(description)) {

            int result = JOptionPane.showConfirmDialog(
                    frame,
                    "Save changes to " + description.get(),
                    "Confirm",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            switch (result) {
                case JOptionPane.YES_OPTION:
                    return multifileSaveAll();

                case JOptionPane.NO_OPTION:
                    return true;

                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }

        return true;
    }

    boolean multifileSaveAll() {

        // Save all modified files
        for (int i = 0; i < fileManager.getFileEditors().size(); i++) {
            if (fileManager.getFileEditors().get(i).isModified()) {
                tabControl.setSelectedIndex(i);
                if (!actionSave(i)) {
                    return false;
                }
            }
        }

        return true;
    }

    boolean actionSave() {
        // Save content of current tab
        if (fileManager.getFileEditors().isEmpty()
                || this.tabControl.getTabCount() == 0
                || this.tabControl.getSelectedIndex() == -1) {
            return false;
        }

        int index = tabControl.getSelectedIndex();
        basicEditor.onFileSaving(fileManager.getFileEditors().get(index));
        boolean saved = fileManager.getFileEditors().get(index).save(false, fileManager.getCurrentDirectory());
        if (saved) {
            int main = fileManager.getRunnableFileIndex();
            if (index == main) {
                fileManager.setFileDirectory(
                        new File(fileManager.getFileEditors().get(index).getFilePath()).getParent());
                fileManager.setRunDirectory(fileManager.getFileDirectory());
                fileManager.setCurrentDirectory(fileManager.getRunDirectory());
            }
            refreshTabTitle(index);
            refreshRunnableFileControls();
        }
        return saved;
    }

    boolean actionSave(int index) {
        // Save content of current tab
        if (fileManager.getFileEditors().isEmpty()
                || this.tabControl.getTabCount() == 0
                || this.tabControl.getSelectedIndex() == -1) {
            return false;
        }

        basicEditor.onFileSaving(fileManager.getFileEditors().get(index));
        boolean saved = fileManager.getFileEditors().get(index).save(false, fileManager.getCurrentDirectory());
        if (saved) {
            int main = fileManager.getRunnableFileIndex();
            if (index == main) {
                fileManager.setFileDirectory(
                        new File(fileManager.getFileEditors().get(index).getFilePath()).getParent());
                fileManager.setRunDirectory(fileManager.getFileDirectory());
                fileManager.setCurrentDirectory(fileManager.getRunDirectory());
            }
            refreshTabTitle(index);
            refreshRunnableFileControls();
        }
        return saved;
    }

    void actionSaveAs() {
        // Save content of current tab as new file
        if (fileManager.getFileEditors().isEmpty()
                || this.tabControl.getTabCount() == 0
                || this.tabControl.getSelectedIndex() == -1) {
            return;
        }
        int index = tabControl.getSelectedIndex();

        fileManager.setCurrentDirectory(fileManager.getFileDirectory());

        basicEditor.onFileSaving(fileManager.getFileEditors().get(index));
        if (fileManager.getFileEditors().get(index).save(true, fileManager.getCurrentDirectory())) {
            int main = fileManager.getRunnableFileIndex();
            if (index == main) {
                fileManager.setFileDirectory(
                        new File(fileManager.getFileEditors().get(index).getFilePath()).getParent());
                fileManager.setRunDirectory(fileManager.getFileDirectory());
                fileManager.setCurrentDirectory(fileManager.getRunDirectory());
            }

        } else {
            // Restore Current directory
            fileManager.setCurrentDirectory(fileManager.getRunDirectory());
        }
        refreshTabTitle(index);
        refreshRunnableFileControls();
    }

    private void refreshTabTitle(int index) {
        tabControl.setTitleAt(index, fileManager.getFileEditors().get(index).getTitle());

        Component tabComponent = tabControl.getTabComponentAt(index);
        if (tabComponent != null) {
            tabComponent.invalidate();
        }
        tabControl.revalidate();
        tabControl.repaint();
    }

    private void actionDebugMode() {
        if (Objects.equals(activeBottomBarKey, "debug") && isBottomBarExpanded()) {
            collapseBottomBar();
            return;
        }
        selectBottomBarSection("debug", true);
    }

    private void actionGoToDeclaration() {
        int selectedTab = tabControl.getSelectedIndex();
        if (selectedTab < 0 || selectedTab >= fileManager.getFileEditors().size()) {
            return;
        }

        FileEditor activeEditor = fileManager.getFileEditors().get(selectedTab);
        JTextArea editorPane = activeEditor.getEditorPane();
        String symbol = getIdentifierAtCaret(editorPane);
        if (symbol == null || symbol.isBlank()) {
            setCompilerStatus("No identifier at caret");
            return;
        }

        String activeFile = activeEditor.getFilePath();
        int caretLine = 0;
        try {
            caretLine = editorPane.getLineOfOffset(editorPane.getCaretPosition());
        } catch (BadLocationException ignored) {
        }

        java.util.List<com.basic4gl.desktop.spi.language.SymbolDeclaration> declarations =
                collectOpenFileDeclarations();
        java.util.List<com.basic4gl.desktop.spi.language.SymbolDeclaration> matches = declarations.stream()
                .filter(d -> ("label".equals(d.kind()) || "variable".equals(d.kind()))
                        && d.name().equalsIgnoreCase(symbol))
                .toList();

        if (matches.isEmpty()) {
            setCompilerStatus("Declaration not found for: " + symbol);
            return;
        }

        com.basic4gl.desktop.spi.language.SymbolDeclaration selected =
                chooseDeclarationForCaret(matches, activeFile, caretLine);
        if (selected == null) {
            return;
        }

        if (matches.size() > 1) {
            selected = promptUserForDeclaration(matches, selected);
            if (selected == null) {
                return;
            }
        }

        goToDeclarationLocation(selected);
        setCompilerStatus("Declaration: " + selected.signature());
    }

    private java.util.List<com.basic4gl.desktop.spi.language.SymbolDeclaration> collectOpenFileDeclarations() {
        java.util.List<com.basic4gl.desktop.spi.language.SymbolDeclaration> declarations = new ArrayList<>();
        for (FileEditor editor : fileManager.getFileEditors()) {
            String fileId = editor.getFilePath();
            if (fileId == null || fileId.isBlank()) {
                File file = editor.getFile();
                fileId = file != null ? file.getAbsolutePath() : "<unsaved:" + editor.getTitle() + ">";
            }
            declarations.addAll(basicEditor
                    .getLanguageSupport()
                    .extractDeclarations(editor.getEditorPane().getText(), fileId));
        }
        return declarations;
    }

    private com.basic4gl.desktop.spi.language.SymbolDeclaration chooseDeclarationForCaret(
            java.util.List<com.basic4gl.desktop.spi.language.SymbolDeclaration> matches,
            String activeFile,
            int caretLine) {
        java.util.List<com.basic4gl.desktop.spi.language.SymbolDeclaration> sameFile = matches.stream()
                .filter(d -> Objects.equals(d.fileId(), activeFile))
                .toList();
        java.util.List<com.basic4gl.desktop.spi.language.SymbolDeclaration> candidates =
                sameFile.isEmpty() ? matches : sameFile;

        com.basic4gl.desktop.spi.language.SymbolDeclaration best = null;
        int bestScore = Integer.MAX_VALUE;
        for (com.basic4gl.desktop.spi.language.SymbolDeclaration candidate : candidates) {
            int score = declarationScore(candidate, activeFile, caretLine);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private int declarationScore(
            com.basic4gl.desktop.spi.language.SymbolDeclaration declaration, String activeFile, int caretLine) {
        int score = 0;
        if (!Objects.equals(declaration.fileId(), activeFile)) {
            score += 1_000_000;
        }
        int lineDistance = Math.abs(caretLine - declaration.line());
        score += lineDistance;
        if (declaration.line() > caretLine) {
            // Prefer declarations above the current caret location.
            score += 5_000;
        }
        // Prefer first declaration over later re-dims when ambiguous.
        score += Math.max(0, declaration.declarationIndex() - 1) * 50;
        return score;
    }

    private com.basic4gl.desktop.spi.language.SymbolDeclaration promptUserForDeclaration(
            java.util.List<com.basic4gl.desktop.spi.language.SymbolDeclaration> matches,
            com.basic4gl.desktop.spi.language.SymbolDeclaration preferred) {
        Object[] options = matches.stream().map(this::formatDeclarationChoice).toArray();
        Object initial =
                preferred != null ? formatDeclarationChoice(preferred) : (options.length > 0 ? options[0] : null);
        Object selected = JOptionPane.showInputDialog(
                frame,
                "Multiple declarations found. Choose destination:",
                "Go to Declaration",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                initial);
        if (selected == null) {
            return null;
        }
        String selectedText = selected.toString();
        for (com.basic4gl.desktop.spi.language.SymbolDeclaration declaration : matches) {
            if (formatDeclarationChoice(declaration).equals(selectedText)) {
                return declaration;
            }
        }
        return preferred;
    }

    private String formatDeclarationChoice(com.basic4gl.desktop.spi.language.SymbolDeclaration declaration) {
        String fileLabel = declaration.fileId();
        File f = fileLabel == null ? null : new File(fileLabel);
        if (f != null && f.getName() != null && !f.getName().isBlank()) {
            fileLabel = f.getName();
        }
        return declaration.kind() + "  " + declaration.signature() + "  (" + fileLabel + ":" + (declaration.line() + 1)
                + ")";
    }

    private void goToDeclarationLocation(com.basic4gl.desktop.spi.language.SymbolDeclaration declaration) {
        String filePath = declaration.fileId();
        int index = getTabIndex(filePath);
        if (index == -1 && filePath != null && !filePath.startsWith("<unsaved:")) {
            File file = new File(filePath);
            if (file.exists()) {
                addTab(FileEditor.open(file, this, fileManager, this, linkGenerator, searchContext));
                index = getTabIndex(filePath);
            }
        }
        if (index < 0 || index >= fileManager.getFileEditors().size()) {
            return;
        }

        tabControl.setSelectedIndex(index);
        JTextArea pane = fileManager.getFileEditors().get(index).getEditorPane();
        int targetOffset;
        try {
            int lineStart = pane.getLineStartOffset(Math.max(0, declaration.line()));
            targetOffset = Math.min(
                    lineStart + Math.max(0, declaration.column()),
                    pane.getDocument().getLength());
        } catch (BadLocationException e) {
            targetOffset = Math.min(pane.getDocument().getLength(), pane.getCaretPosition());
        }
        pane.requestFocusInWindow();
        pane.setCaretPosition(targetOffset);
    }

    private String getIdentifierAtCaret(JTextArea editorPane) {
        String text = editorPane.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        int caret = editorPane.getCaretPosition();
        caret = Math.max(0, Math.min(caret, text.length()));

        if (caret > 0 && (caret == text.length() || !isIdentifierChar(text.charAt(caret)))) {
            caret--;
        }
        if (caret < 0 || caret >= text.length() || !isIdentifierChar(text.charAt(caret))) {
            return null;
        }

        int start = caret;
        while (start > 0 && isIdentifierChar(text.charAt(start - 1))) {
            start--;
        }
        int end = caret + 1;
        while (end < text.length() && isIdentifierChar(text.charAt(end))) {
            end++;
        }
        return text.substring(start, end);
    }

    private boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    public void closeAll() {
        for (int i = tabControl.getTabCount() - 1; i >= 0; i--) {
            closeTab(i);
        }

        // Reset default run directory to programs folder
        fileManager.setRunDirectory(fileManager.getAppDirectory() + "\\Programs");

        // Clear plugins, breakpoints, bookmarks etc
        basicEditor.onCloseAll();

        // Refresh UI
        refreshActions(basicEditor.getMode());
        refreshRunnableFileControls();
        refreshSidebarContent();
    }

    public void closeTab(int index) {
        tabControl.remove(index);
        if (index >= 0 && index < fileManager.getFileEditors().size()) {
            fileManager.getFileEditors().remove(index);
        }
        refreshFileViewModeButtons();
        fileManager.ensureRunnableFileValid();
        refreshRunnableFileControls();
        refreshSidebarContent();
    }

    public void addTab() {
        final FileEditor editor = new FileEditor(this, fileManager, this, linkGenerator, searchContext);
        addTab(editor);
    }

    public void addTab(FileEditor editor) {
        addTab(new TextFileViewer(editor));
    }

    /**
     * Adds a file viewer tab for all viewer types, including text editors, images, audio, and docs.
     */
    public void addTab(IFileViewer viewer) {
        int count = tabControl.getTabCount();
        FileViewerWrapper wrapper = new FileViewerWrapper(viewer);

        // For backward compatibility with FileEditor code, also add to fileManager if it's a text editor
        if (wrapper.isTextEditor()) {
            fileManager.getFileEditors().add(wrapper.getFileEditor());
        } else {
            // Add a placeholder to keep indices aligned
            fileManager.getFileEditors().add(null);
        }

        // replace emptyTabPanel if needed
        setEditorContent(getActiveEditorHost());

        JComponent contentPane = viewer.getContentPane();
        contentPane.putClientProperty(TAB_FILE_VIEWER_PROPERTY, viewer);
        tabControl.addTab(viewer.getTitle(), contentPane);

        File file = viewer.getFile();
        if (file != null) {
            basicEditor.notifyFileOpened(file);
            if (wrapper.isTextEditor()) {
                basicEditor.onFileOpened(wrapper.getFileEditor());
            }
        }

        // Add document listener only for text editors
        if (wrapper.isTextEditor()) {
            final FileEditor edit = wrapper.getFileEditor();
            JTextArea editorPane = edit.getEditorPane();
            editorPane.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    int index = getTabIndex(edit.getFilePath());
                    edit.setModified();
                    tabControl.setTitleAt(index, edit.getTitle());

                    for (IEditorPanelProvider panel : panels) {
                        panel.onFileModified(edit.getFilePath());
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    int index = getTabIndex(edit.getFilePath());
                    edit.setModified();
                    tabControl.setTitleAt(index, edit.getTitle());

                    for (IEditorPanelProvider panel : panels) {
                        panel.onFileModified(edit.getFilePath());
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (e.getLength() == 0) {
                        // ignore empty changes - eg: syntax highlighting refreshed
                        return;
                    }
                    int index = getTabIndex(edit.getFilePath());
                    edit.setModified();
                    tabControl.setTitleAt(index, edit.getTitle());
                }
            });

            // Allow user to see cursor position
            editorPane.addCaretListener(TrackCaretPosition);
            cursorPositionLabel.setText(0 + ":" + 0); // Reset label

            // Set tab as read-only if App is running or paused
            boolean readOnly = basicEditor.getMode() != ApMode.AP_STOPPED;
            editorPane.setEditable(!readOnly);
        }

        // Refresh interface if there were previously no tabs open
        if (count == 0) {
            basicEditor.setMode(ApMode.AP_STOPPED, null);
        }

        fileManager.ensureRunnableFileValid();
        refreshRunnableFileControls();
        refreshSidebarContent();
    }

    public void openDocumentationPreview(ContentDocumentViewer viewer) {
        int existingPreviewIndex = findUnpinnedDocumentationPreviewTab();
        if (existingPreviewIndex >= 0) {
            closeTab(existingPreviewIndex);
        }
        addTab(viewer);
        tabControl.setSelectedIndex(tabControl.getTabCount() - 1);
    }

    private int findUnpinnedDocumentationPreviewTab() {
        for (int i = 0; i < tabControl.getTabCount(); i++) {
            IFileViewer viewer = getFileViewerAt(i);
            if (viewer instanceof ContentDocumentViewer documentViewer && !documentViewer.isPinned()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void placeCursorAtProcessed(final int row, int col) {
        lastSourceRow = row;
        lastSourceColumn = col;

        // Place cursor at position corresponding to row, col in post-processed file.
        FileLineNumber fileLineNumber = basicEditor.getLanguageService().getFileLineNumberFromMain(row);
        final String file = fileLineNumber.getFilename();
        final int r = fileLineNumber.getLineNumber();
        final int c = col;

        // Find (and show) corresponding editor frame
        if (r >= 0) {
            if (virtualMachineViewDialog != null && virtualMachineViewDialog.isDisplayable()) {
                virtualMachineViewDialog.setCurrentSourcePosition(r, c);
            }

            int index = getTabIndex(file);
            if (index == -1) {
                // Attempt to open tab
                addTab(FileEditor.open(new File(file), this, fileManager, this, linkGenerator, searchContext));
                index = tabControl.getTabCount() - 1;
                // return;
            }

            tabControl.setSelectedIndex(index);

            final JTextArea frame =
                    fileManager.getFileEditors().get(getTabIndex(file)).getEditorPane();

            // Set focus
            frame.grabFocus();

            SwingUtilities.invokeLater(() -> {
                int col1 = c;
                // Place cursor
                if (r >= 0) {
                    try {
                        JTextArea textArea = fileManager
                                .getFileEditors()
                                .get(tabControl.getSelectedIndex())
                                .getEditorPane();
                        int offset = textArea.getLineStartOffset(r);

                        // Reduce column position if it would place the cursor at the next line
                        if (textArea.getLineCount() > r + 1 && offset + col1 == textArea.getLineStartOffset(r + 1)) {
                            offset = textArea.getLineStartOffset(r + 1) - 1;
                        } else {
                            offset += col1;
                        }

                        frame.setCaretPosition(offset);
                    } catch (Exception ex) {
                        // Do nothing
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {

        // Place editor into paused mode
        basicEditor.setMode(ApMode.AP_PAUSED, null);
        refreshActions(basicEditor.getMode());

        // Open debug panel when execution pauses.
        selectBottomBarSection("debug", true);
        refreshDebugDisplays(basicEditor.getMode());

        // TODO Add VMViewer
        // VMView().SetVMIsRunning(false);
    }

    @Override
    public void onApplicationClosing() {
        // Get focus back
        frame.requestFocus();
        if (!fileManager.getFileEditors().isEmpty() && tabControl.getTabCount() != 0) {
            // TODO set tab to file that error occurred in
            fileManager
                    .getFileEditors()
                    .get(tabControl.getSelectedIndex())
                    .getEditorPane()
                    .grabFocus();
        }
    }

    @Override
    public void setCompilerStatus(String error) {
        compilerStatusLabel.setText(error);
    }

    @Override
    public void updateCallStack(StackTraceCallback message) {
        debugPresenter.updateCallStack(message);
    }

    @Override
    public void onCompileSucceeded() {

        for (IEditorPanelProvider panel : panels) {
            panel.onCompileSucceeded();
        }
    }

    @Override
    public void onModeChanged(ApMode mode, String statusMsg) {
        if (mode != ApMode.AP_CLOSED) {
            copyMenuItem.setEnabled(true);
            findMenuItem.setEnabled(true);
            replaceMenuItem.setEnabled(true);
            goToDeclarationMenuItem.setEnabled(true);
            selectAllMenuItem.setEnabled(true);

            stepOverMenuItem.setEnabled(true);
            stepIntoMenuItem.setEnabled(true);
            stepOutOfMenuItem.setEnabled(true);
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
        refreshActions(basicEditor.getMode());
        refreshDebugDisplays(basicEditor.getMode());
        compilerStatusLabel.setText(statusMsg);

        // Notify virtual machine view
        if (virtualMachineViewDialog != null && virtualMachineViewDialog.isDisplayable()) {
            virtualMachineViewDialog.setVmRunning(mode == ApMode.AP_RUNNING);
        }
    }

    @Override
    public void refreshActions(ApMode mode) {

        // Enable/disable actions to reflect state
        switch (mode) {
            case AP_CLOSED:
                settingsMenuItem.setEnabled(false);
                settingsButton.setEnabled(false);

                exportMenuItem.setEnabled(false);
                exportButton.setEnabled(false);

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
                goToDeclarationMenuItem.setEnabled(false);
                selectAllMenuItem.setEnabled(false);

                stepOverMenuItem.setEnabled(false);
                stepIntoMenuItem.setEnabled(false);
                stepOutOfMenuItem.setEnabled(false);
                playPauseMenuItem.setEnabled(false);
                runMenuItem.setEnabled(false);
                runButton.setEnabled(false);

                saveAsMenuItem.setEnabled(false);
                saveMenuItem.setEnabled(false);
                saveButton.setEnabled(false);

                breakpointSubMenu.setEnabled(false);
                bookmarkSubMenu.setEnabled(false);

                compilerStatusLabel.setText("");

                emptyTabPanel = new EmptyTabPanel(
                        this,
                        newMenuItem.getAccelerator(),
                        openMenuItem.getAccelerator(),
                        openFolderMenuItem.getAccelerator(),
                        basicEditor.getRecentFiles(),
                        recentWorkspaces);
                setEditorContent(emptyTabPanel);
                break;
            case AP_STOPPED:
                setEditorContent(getActiveEditorHost());
                setClosingTabsEnabled(true);
                settingsMenuItem.setEnabled(true);
                settingsButton.setEnabled(true);
                exportMenuItem.setEnabled(true);
                exportButton.setEnabled(true);
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

            case AP_WAITING:
                setClosingTabsEnabled(false);
                settingsMenuItem.setEnabled(false);
                settingsButton.setEnabled(false);
                exportMenuItem.setEnabled(false);
                exportButton.setEnabled(false);
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

            case AP_RUNNING:
            case AP_PAUSED:
                setEditorContent(getActiveEditorHost());
                setClosingTabsEnabled(false);

                settingsMenuItem.setEnabled(false);
                settingsButton.setEnabled(false);

                exportMenuItem.setEnabled(false);
                exportButton.setEnabled(false);

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
    public void refreshDebugDisplays(ApMode mode) {
        debugPresenter.refreshDebugControls(mode);
        playPauseMenuItem.setEnabled(mode != ApMode.AP_WAITING && mode != ApMode.AP_CLOSED);

        if (mode == ApMode.AP_CLOSED && isBottomBarExpanded()) {
            collapseBottomBar();
        }

        if (mode != ApMode.AP_PAUSED) {
            debugPresenter.clearCallStack();
        }

        syncDebugMenuSelection();
    }

    @Override
    public void updateVmViewCallStack(StackTraceCallback stackTraceCallback) {
        if (virtualMachineViewDialog != null && virtualMachineViewDialog.isDisplayable()) {
            virtualMachineViewDialog.updateCallStack(
                    basicEditor.getLanguageService().toVmViewFriendlyCallStack(stackTraceCallback));
        }
    }

    @Override
    public void updateVmViewDisassembly(DisassembleCallback disassembleCallback) {
        if (virtualMachineViewDialog != null && virtualMachineViewDialog.isDisplayable()) {
            virtualMachineViewDialog.updateDisassembly(disassembleCallback);
        }
    }

    @Override
    public void updateVmViewVariables(VariablesCallback variablesCallback) {
        if (virtualMachineViewDialog != null && virtualMachineViewDialog.isDisplayable()) {
            virtualMachineViewDialog.updateVariables(variablesCallback);
        }
    }

    @Override
    public void updateEvaluateWatch(String evaluatedWatch, String result) {
        debugPresenter.updateEvaluateWatch(evaluatedWatch, result);
    }

    @Override
    public void updateVmViewVariableValue(String expression, String result) {
        if (virtualMachineViewDialog != null && virtualMachineViewDialog.isDisplayable()) {
            virtualMachineViewDialog.applySeeValueResult(expression, result);
        }
    }

    @Override
    public void updateVmViewError(String scope, String message) {
        if (virtualMachineViewDialog != null && virtualMachineViewDialog.isDisplayable()) {
            virtualMachineViewDialog.showError(scope, message);
        }
    }

    @Override
    public void refreshWatchList() {
        debugPresenter.refreshWatchList();
    }

    @Override
    public void onToggleBreakpoint(String filePath, int line) {
        basicEditor.toggleBreakpt(filePath, line);
    }

    private Component getActiveEditorHost() {
        return splitTabControl.getTabCount() == 0 ? primaryTabHost : editorSplitPane;
    }

    private void setEditorContent(Component component) {
        contentPane.setLeftComponent(component);
    }

    private void configurePrimaryTabHost() {
        addTabDropdownButton.setFocusable(false);
        addTabDropdownButton.setToolTipText("Create a new tab or open an asset");
        addTabDropdownButton.putClientProperty("JButton.buttonType", "toolBarButton");
        addTabDropdownButton.putClientProperty(
                "FlatLaf.style", "arc: 14; focusWidth: 0; innerFocusWidth: 0; margin: 6,6,6,6");
        addTabDropdownButton.setOpaque(false);
        addTabDropdownButton.setMargin(new Insets(6, 6, 6, 6));
        addTabDropdownButton.setPreferredSize(TAB_HEADER_ICON_BUTTON_SIZE);
        addTabDropdownButton.setMinimumSize(TAB_HEADER_ICON_BUTTON_SIZE);
        addTabDropdownButton.setMaximumSize(TAB_HEADER_ICON_BUTTON_SIZE);
        addTabDropdownButton.addActionListener(e -> showCreateTabMenu(addTabDropdownButton));
        tabControl.putClientProperty(TABBED_PANE_LEADING_COMPONENT, addTabDropdownButton);
        ButtonGroup viewModeButtons = new ButtonGroup();
        viewModeButtons.add(editViewButton);
        viewModeButtons.add(editPreviewViewButton);
        viewModeButtons.add(previewViewButton);
        fileViewModeTabs.add(editViewButton);
        fileViewModeTabs.add(editPreviewViewButton);
        fileViewModeTabs.add(previewViewButton);
        fileViewModeTabsHost.setVisible(false);
        tabControl.putClientProperty(TABBED_PANE_TRAILING_COMPONENT, fileViewModeTabsHost);
        tabControl.addChangeListener(e -> onSelectedTabChanged());
        primaryTabHost.add(tabControl, BorderLayout.CENTER);
    }

    private JToggleButton createFileViewModeButton(
            String tooltip, String iconPath, IFileViewer.ViewMode viewMode, String segmentPosition) {
        JToggleButton button = new JToggleButton(createScaledIcon(iconPath, 18));
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.putClientProperty("JButton.buttonType", "segmented");
        button.putClientProperty("JButton.segmentPosition", segmentPosition);
        button.putClientProperty("FlatLaf.style", SEGMENTED_BUTTON_STYLE);
        button.setOpaque(false);
        button.setMargin(new Insets(6, 8, 6, 8));
        button.setPreferredSize(TAB_VIEW_MODE_BUTTON_SIZE);
        button.setMinimumSize(TAB_VIEW_MODE_BUTTON_SIZE);
        button.setMaximumSize(TAB_VIEW_MODE_BUTTON_SIZE);
        button.addActionListener(e -> setSelectedFileViewMode(viewMode));
        return button;
    }

    private JPanel createSegmentedButtonStrip() {
        JPanel panel = new RoundedCardPanel(RoundedCardPanel.DEFAULT_ARC);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBackground(SEGMENTED_BACKGROUND);
        panel.setBorder(new EmptyBorder(1, 1, 1, 1));
        return panel;
    }

    private JPanel createSegmentedButtonStripHost(JPanel strip) {
        JPanel host = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        host.setOpaque(false);
        host.setBorder(new EmptyBorder(2, 4, 2, 0));
        host.add(strip);
        return host;
    }

    private void setSelectedFileViewMode(IFileViewer.ViewMode viewMode) {
        IFileViewer viewer = getSelectedFileViewer();
        if (viewer == null || !viewer.hasPreview()) {
            refreshFileViewModeButtons();
            return;
        }
        viewer.setViewMode(viewMode);
        viewer.getContentPane().revalidate();
        viewer.getContentPane().repaint();
        refreshFileViewModeButtons();
    }

    private void refreshFileViewModeButtons() {
        IFileViewer viewer = getSelectedFileViewer();
        boolean hasPreview = viewer != null && viewer.hasPreview();
        fileViewModeTabsHost.setVisible(hasPreview);
        if (!hasPreview) {
            return;
        }

        IFileViewer.ViewMode viewMode = viewer.getViewMode();
        editViewButton.setSelected(viewMode == IFileViewer.ViewMode.EDITOR);
        editPreviewViewButton.setSelected(viewMode == IFileViewer.ViewMode.EDITOR_AND_PREVIEW);
        previewViewButton.setSelected(
                viewMode == IFileViewer.ViewMode.PREVIEW || viewMode == IFileViewer.ViewMode.DEFAULT);
    }

    private IFileViewer getSelectedFileViewer() {
        return getFileViewerAt(tabControl.getSelectedIndex());
    }

    private IFileViewer getFileViewerAt(int index) {
        if (index < 0 || index >= tabControl.getTabCount()) {
            return null;
        }
        Component component = tabControl.getComponentAt(index);
        if (component instanceof JComponent tabContent
                && tabContent.getClientProperty(TAB_FILE_VIEWER_PROPERTY) instanceof IFileViewer viewer) {
            return viewer;
        }
        return null;
    }

    private void configureSplitTabs() {
        splitTabControl.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        splitTabControl.putClientProperty(TABBED_PANE_TAB_CLOSABLE, true);
        splitTabControl.putClientProperty(
                TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabPane, tabIndex) -> {
                    if (tabIndex >= 0) {
                        splitTabControl.remove(tabIndex);
                        if (splitTabControl.getTabCount() == 0
                                && basicEditor != null
                                && basicEditor.getMode() != ApMode.AP_CLOSED) {
                            setEditorContent(primaryTabHost);
                        }
                    }
                });
    }

    private void configureTabContextMenu() {
        tabControl.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowTabPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowTabPopup(e);
            }
        });
    }

    private void maybeShowTabPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        int tabIndex = tabControl.indexAtLocation(e.getX(), e.getY());
        if (tabIndex < 0 || tabIndex >= fileManager.getFileEditors().size()) {
            return;
        }

        JPopupMenu popup = new JPopupMenu();
        FileEditor contextEditor = fileManager.getFileEditors().get(tabIndex);
        JMenuItem setRunnable = new JMenuItem("Set as runnable file");
        setRunnable.addActionListener(x -> {
            if (contextEditor == null) {
                return;
            }
            runTargetFollowsCurrentTab = false;
            fileManager.setRunnableFileEditor(contextEditor);
            refreshRunnableFileControls();
        });
        setRunnable.setEnabled(contextEditor != null);
        popup.add(setRunnable);

        JMenuItem splitPreview = new JMenuItem("Split right");
        splitPreview.addActionListener(x -> openSplitPreview(tabIndex));
        popup.add(splitPreview);

        JMenuItem popOut = new JMenuItem("Pop out tab");
        popOut.addActionListener(x -> popOutTab(tabIndex));
        popup.add(popOut);

        popup.show(tabControl, e.getX(), e.getY());
    }

    private void openSplitPreview(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= fileManager.getFileEditors().size()) {
            return;
        }
        FileEditor editor = fileManager.getFileEditors().get(tabIndex);
        if (editor == null) {
            return;
        }
        File source = editor.getFile();
        if (source == null) {
            return;
        }

        FileEditor preview = FileEditor.open(source, this, fileManager, this, linkGenerator, searchContext);
        preview.getEditorPane().setEditable(false);
        splitTabControl.addTab(preview.getTitle() + " (split)", preview.getContentPane());
        splitTabControl.setSelectedIndex(splitTabControl.getTabCount() - 1);
        setEditorContent(getActiveEditorHost());
        SwingUtilities.invokeLater(() -> editorSplitPane.setDividerLocation(0.68));
    }

    private void popOutTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= fileManager.getFileEditors().size()) {
            return;
        }
        FileEditor editor = fileManager.getFileEditors().get(tabIndex);
        if (editor == null) {
            return;
        }
        File source = editor.getFile();
        if (source == null) {
            return;
        }

        FileEditor preview = FileEditor.open(source, this, fileManager, this, linkGenerator, searchContext);
        preview.getEditorPane().setEditable(false);

        JFrame popout = new JFrame("Pop out: " + preview.getShortFilename());
        popout.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        popout.setLayout(new BorderLayout());
        popout.add(preview.getContentPane(), BorderLayout.CENTER);
        popout.setSize(new Dimension(720, 480));
        popout.setLocationRelativeTo(frame);
        popout.setVisible(true);
    }

    private void showCreateTabMenu(Component anchor) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem newTabItem = new JMenuItem("New Program Tab");
        newTabItem.addActionListener(e -> addTab());
        popup.add(newTabItem);

        JMenuItem openAssetItem = new JMenuItem("Open Asset or Docs File...");
        openAssetItem.addActionListener(e -> actionOpenAsset());
        popup.add(openAssetItem);

        popup.show(anchor, 0, anchor.getHeight());
    }

    private void actionOpenAsset() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(fileManager.getCurrentDirectory()));
        int result = chooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();

        openFileWithPreferredViewer(selected);
    }

    private void configureLeftSidebar() {
        leftSidebarRail.setFloatable(false);
        leftSidebarRail.setRollover(true);
        bottomBarRail.setFloatable(false);
        bottomBarRail.setRollover(true);

        Arrays.stream(panels)
                .filter(x -> x.getLayoutConstraints() == EditorLayout.WEST)
                .forEach(x -> {
                    leftSidebarContent.add(x.build(this.basicEditor), x.id());
                    addLeftSidebarButton(
                            x.id(),
                            createImageIcon(x.getActiveIconPath(), x.getActiveIconTint()),
                            createImageIcon(x.getInactiveIconPath()),
                            x.getTitle());
                });

        Arrays.stream(panels)
                .filter(x -> x.getLayoutConstraints() == EditorLayout.SOUTH)
                .forEach(x -> {
                    bottomBarContent.add(x.build(this.basicEditor), x.id());
                    addBottomBarButton(
                            x.id(),
                            createImageIcon(x.getActiveIconPath(), x.getActiveIconTint()),
                            createImageIcon(x.getInactiveIconPath()),
                            x.getTitle());
                });

        bottomBarContainer.add(bottomBarContent, BorderLayout.CENTER);

        // Select first panel if available
        Arrays.stream(panels)
                .filter(x -> x.getLayoutConstraints() == EditorLayout.WEST)
                .findFirst()
                .ifPresent(x -> {
                    selectLeftSidebarSection(x.id(), true);
                });
    }

    private void configureRightSidebar() {
        rightDocsRail.setFloatable(false);
        rightDocsRail.setRollover(true);
        rightDocsContainer.add(rightDocsContent, BorderLayout.CENTER);
        Arrays.stream(panels)
                .filter(x -> x.getLayoutConstraints() == EditorLayout.EAST)
                .forEach(x -> {
                    addRightDocsButton(
                            x.id(),
                            createImageIcon(x.getActiveIconPath(), x.getActiveIconTint()),
                            createImageIcon(x.getInactiveIconPath()),
                            x.getTitle());
                    JComponent content = x.build(this.basicEditor);
                    if (content != null) {
                        rightDocsContent.add(content, x.id());
                    }
                });

        Arrays.stream(panels)
                .filter(x -> x.getLayoutConstraints() == EditorLayout.EAST)
                .findFirst()
                .ifPresent(x -> selectRightDocsSection(x.id()));
    }

    private void addLeftSidebarButton(String key, Icon selectedIcon, Icon icon, String tooltip) {
        JToggleButton button = createRailButton(selectedIcon, icon, tooltip);
        button.addActionListener(e -> onLeftSidebarButtonPressed(key));
        leftSidebarGroup.add(button);
        leftSidebarButtons.put(key, button);
        leftSidebarRail.add(button);
    }

    private void addRightDocsButton(String key, Icon selectedIcon, Icon icon, String tooltip) {
        JToggleButton button = createRailButton(selectedIcon, icon, tooltip);
        button.addActionListener(e -> onRightDocsButtonPressed(key));
        rightDocsGroup.add(button);
        rightDocsButtons.put(key, button);
        rightDocsRail.add(button);
    }

    private void addBottomBarButton(String key, Icon selectedIcon, Icon icon, String tooltip) {
        JToggleButton button = createRailButton(selectedIcon, icon, tooltip);
        button.addActionListener(e -> onBottomBarButtonPressed(key));
        bottomBarGroup.add(button);
        bottomBarButtons.put(key, button);
        bottomBarRail.add(button);
    }

    private JToggleButton createRailButton(Icon selectedIcon, Icon icon, String tooltip) {
        JToggleButton button = new JToggleButton(icon);
        button.setSelectedIcon(selectedIcon != null ? selectedIcon : icon);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setMargin(new Insets(8, 8, 8, 8));
        button.setMaximumSize(new Dimension(38, 38));
        button.setPreferredSize(new Dimension(38, 38));
        return button;
    }

    private void onLeftSidebarButtonPressed(String key) {
        if (Objects.equals(activeLeftSidebarKey, key) && isLeftSidebarExpanded()) {
            collapseLeftSidebar();
            return;
        }
        selectLeftSidebarSection(key, true);
    }

    private void onBottomBarButtonPressed(String key) {
        if (Objects.equals(activeBottomBarKey, key) && isBottomBarExpanded()) {
            collapseBottomBar();
            return;
        }
        selectBottomBarSection(key, true);
    }

    private void selectLeftSidebarSection(String key, boolean ensureExpanded) {
        CardLayout layout = (CardLayout) leftSidebarContent.getLayout();
        layout.show(leftSidebarContent, key);
        activeLeftSidebarKey = key;

        JToggleButton button = leftSidebarButtons.get(key);
        if (button != null) {
            button.setSelected(true);
        }

        if (ensureExpanded) {
            expandLeftSidebar();
        }
    }

    private void selectBottomBarSection(String key, boolean ensureExpanded) {
        if (!bottomBarButtons.containsKey(key)) {
            return;
        }
        activeBottomBarKey = key;
        CardLayout layout = (CardLayout) bottomBarContent.getLayout();
        layout.show(bottomBarContent, key);
        JToggleButton button = bottomBarButtons.get(key);
        if (button != null) {
            button.setSelected(true);
        }

        if (ensureExpanded) {
            expandBottomBar();
        }
        syncDebugMenuSelection();
    }

    private boolean isLeftSidebarExpanded() {
        return !leftSidebarCollapsed && workspacePane.getLeftComponent() == leftSidebarContent;
    }

    private void collapseLeftSidebar() {
        if (workspacePane.getLeftComponent() == leftSidebarContent) {
            int currentWidth = leftSidebarContent.getWidth();
            if (currentWidth <= 12) {
                currentWidth = workspacePane.getDividerLocation();
            }
            if (currentWidth > 12) {
                expandedLeftSidebarWidth = currentWidth;
            }
        }

        leftSidebarCollapsed = true;
        activeLeftSidebarKey = null;
        leftSidebarGroup.clearSelection();

        workspacePane.setResizeWeight(0.0);
        workspacePane.setDividerSize(0);
        workspacePane.setLeftComponent(null);
        workspacePane.revalidate();
        workspacePane.repaint();
    }

    private void expandLeftSidebar() {
        leftSidebarCollapsed = false;
        if (workspacePane.getLeftComponent() != leftSidebarContent) {
            workspacePane.setLeftComponent(leftSidebarContent);
        }
        workspacePane.setDividerSize(workspacePaneDividerSize);
        workspacePane.setResizeWeight(0.18);
        workspacePane.revalidate();

        int target = Math.max(expandedLeftSidebarWidth, 180);
        SwingUtilities.invokeLater(() -> {
            if (!leftSidebarCollapsed && workspacePane.getLeftComponent() == leftSidebarContent) {
                setDividerLocationClamped(workspacePane, target);
            }
        });
    }

    private boolean isBottomBarExpanded() {
        return !bottomBarCollapsed && mainPane.getBottomComponent() == bottomBarContainer;
    }

    private void collapseBottomBar() {
        if (mainPane.getBottomComponent() == bottomBarContainer) {
            int currentHeight = bottomBarContainer.getHeight();
            if (currentHeight <= 12 && mainPane.getHeight() > 0) {
                currentHeight = mainPane.getHeight() - mainPane.getDividerLocation() - mainPane.getDividerSize();
            }
            if (currentHeight > 12) {
                expandedBottomBarHeight = Math.max(120, currentHeight);
            }
        }

        bottomBarCollapsed = true;
        activeBottomBarKey = null;
        bottomBarGroup.clearSelection();

        mainPane.setResizeWeight(1.0);
        mainPane.setDividerSize(0);
        mainPane.setBottomComponent(null);
        mainPane.revalidate();
        mainPane.repaint();
        syncDebugMenuSelection();
    }

    private void expandBottomBar() {
        bottomBarCollapsed = false;
        if (mainPane.getBottomComponent() != bottomBarContainer) {
            mainPane.setBottomComponent(bottomBarContainer);
        }
        mainPane.setDividerSize(mainPaneDividerSize);
        mainPane.setResizeWeight(1.0);
        mainPane.revalidate();

        int targetBottomHeight = Math.max(expandedBottomBarHeight, 140);
        SwingUtilities.invokeLater(() -> {
            if (!bottomBarCollapsed
                    && mainPane.getBottomComponent() == bottomBarContainer
                    && mainPane.getHeight() > 0) {
                int newDivider = mainPane.getHeight() - targetBottomHeight - mainPane.getDividerSize();
                setDividerLocationClamped(mainPane, newDivider);
            }
        });
        syncDebugMenuSelection();
    }

    private void syncDebugMenuSelection() {
        debugMenuItem.setSelected(isBottomBarExpanded() && Objects.equals(activeBottomBarKey, "debug"));
    }

    private void onRightDocsButtonPressed(String key) {
        if (Objects.equals(activeRightDocsKey, key) && isRightDocsExpanded()) {
            collapseRightDocs();
            return;
        }
        selectRightDocsSection(key);
    }

    private void selectRightDocsSection(String key) {
        if (!rightDocsButtons.containsKey(key)) {
            return;
        }
        activeRightDocsKey = key;
        CardLayout layout = (CardLayout) rightDocsContent.getLayout();
        layout.show(rightDocsContent, key);

        JToggleButton button = rightDocsButtons.get(key);
        if (button != null) {
            button.setSelected(true);
        }

        expandRightDocs();
    }

    private boolean isRightDocsExpanded() {
        return !rightDocsCollapsed && contentPane.getRightComponent() == rightDocsContainer;
    }

    private void collapseRightDocs() {
        if (contentPane.getRightComponent() == rightDocsContainer) {
            int currentWidth = rightDocsContainer.getWidth();
            if (currentWidth <= 12 && contentPane.getWidth() > 0) {
                currentWidth = contentPane.getWidth() - contentPane.getDividerLocation() - contentPane.getDividerSize();
            }
            if (currentWidth > 12) {
                expandedRightDocsWidth = currentWidth;
            }
        }

        rightDocsCollapsed = true;
        activeRightDocsKey = null;
        rightDocsGroup.clearSelection();

        contentPane.setResizeWeight(1.0);
        contentPane.setDividerSize(0);
        contentPane.setRightComponent(null);
        contentPane.revalidate();
        contentPane.repaint();
    }

    private void expandRightDocs() {
        rightDocsCollapsed = false;
        if (contentPane.getRightComponent() != rightDocsContainer) {
            contentPane.setRightComponent(rightDocsContainer);
        }
        contentPane.setDividerSize(contentPaneDividerSize);
        contentPane.setResizeWeight(0.74);
        contentPane.revalidate();

        int targetDocsWidth = Math.max(expandedRightDocsWidth, 220);
        SwingUtilities.invokeLater(() -> {
            if (!rightDocsCollapsed
                    && contentPane.getRightComponent() == rightDocsContainer
                    && contentPane.getWidth() > 0) {
                int newDivider = contentPane.getWidth() - targetDocsWidth - contentPane.getDividerSize();
                setDividerLocationClamped(contentPane, newDivider);
            }
        });
    }

    private void setDividerLocationClamped(JSplitPane splitPane, int requestedLocation) {
        int minimum = splitPane.getMinimumDividerLocation();
        int maximum = splitPane.getMaximumDividerLocation();
        if (maximum < minimum) {
            splitPane.setDividerLocation(requestedLocation);
            return;
        }
        splitPane.setDividerLocation(Math.max(minimum, Math.min(requestedLocation, maximum)));
    }

    private void refreshSidebarContent() {
        for (IEditorPanelProvider panel : panels) {
            panel.refresh(this.basicEditor.getBasic4gl());
        }
    }

    private void refreshRunnableFileControls() {
        if (fileManager == null) {
            return;
        }

        FileEditor currentEditor = getCurrentTextEditor();
        if (runTargetFollowsCurrentTab && currentEditor != null) {
            updateRunnableFileToCurrentTab(currentEditor);
        }

        runTargetButton.setText(getRunTargetButtonLabel(currentEditor));
        runTargetButton.setEnabled(currentEditor != null || hasTextEditors());
        rebuildRunTargetPopup(currentEditor);
    }

    private boolean hasTextEditors() {
        for (int i = 0; i < fileManager.getFileEditors().size(); i++) {
            if (fileManager.getFileEditors().get(i) != null) {
                return true;
            }
        }
        return false;
    }

    private String getRunTargetButtonLabel(FileEditor currentEditor) {
        if (runTargetFollowsCurrentTab) {
            return currentRunTargetLabel(currentEditor);
        }

        FileEditor runnableEditor = getTextEditorAt(fileManager.getRunnableFileIndex());
        if (runnableEditor != null) {
            return runnableEditor.getShortFilename();
        }

        return currentRunTargetLabel(currentEditor);
    }

    private String currentRunTargetLabel(FileEditor currentEditor) {
        if (currentEditor == null) {
            return "";
        }
        String name = currentEditor.getShortFilename();
        return "Current Program [" + name + "]";
    }

    private void showRunTargetPopup() {
        refreshRunnableFileControls();
        runTargetPopup.show(runTargetButton, 0, runTargetButton.getHeight());
    }

    private void rebuildRunTargetPopup(FileEditor currentEditor) {
        runTargetPopup.removeAll();
        ButtonGroup runTargetGroup = new ButtonGroup();

        JRadioButtonMenuItem currentItem = new JRadioButtonMenuItem(currentRunTargetLabel(currentEditor));
        currentItem.setSelected(runTargetFollowsCurrentTab);
        currentItem.addActionListener(e -> {
            runTargetFollowsCurrentTab = true;
            updateRunnableFileToCurrentTab(getCurrentTextEditor());
            refreshRunnableFileControls();
        });
        runTargetGroup.add(currentItem);
        runTargetPopup.add(currentItem);

        if (hasTextEditors()) {
            runTargetPopup.addSeparator();
        }

        FileEditor runnableEditor = getTextEditorAt(fileManager.getRunnableFileIndex());
        for (int i = 0; i < fileManager.getFileEditors().size(); i++) {
            FileEditor editor = fileManager.getFileEditors().get(i);
            if (editor == null) {
                continue;
            }

            JRadioButtonMenuItem item = new JRadioButtonMenuItem(editor.getShortFilename());
            item.setSelected(!runTargetFollowsCurrentTab && editor == runnableEditor);
            item.addActionListener(e -> {
                runTargetFollowsCurrentTab = false;
                fileManager.setRunnableFileEditor(editor);
                refreshRunnableFileControls();
            });
            runTargetGroup.add(item);
            runTargetPopup.add(item);
        }
    }

    private void onSelectedTabChanged() {
        refreshFileViewModeButtons();
        if (runTargetFollowsCurrentTab) {
            refreshRunnableFileControls();
        } else {
            rebuildRunTargetPopup(getCurrentTextEditor());
        }
    }

    private FileEditor getCurrentTextEditor() {
        return getTextEditorAt(tabControl.getSelectedIndex());
    }

    private FileEditor getTextEditorAt(int index) {
        if (fileManager == null || index < 0 || index >= fileManager.getFileEditors().size()) {
            return null;
        }
        return fileManager.getFileEditors().get(index);
    }

    private void updateRunnableFileToCurrentTab(FileEditor currentEditor) {
        if (currentEditor != null) {
            fileManager.setRunnableFileEditor(currentEditor);
        } else {
            fileManager.ensureRunnableFileValid();
        }
    }

    // -------------------------------------------------------------------------
    // Symbol indexer support
    // -------------------------------------------------------------------------

    /**
     * Concatenates the text of every open editor tab into a single string, separated by newlines.
     * This gives the {@link SymbolIndexer} full visibility of all open files for the debounced
     * background scan.
     */
    public String collectAllSourceText() {
        if (fileManager == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (FileEditor fe : fileManager.getFileEditors()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(fe.getEditorPane().getText());
        }
        return sb.toString();
    }

    @Override
    public void selectNextBookmark() {
        fileManager.selectNextBookmark(tabControl.getSelectedIndex());
    }

    @Override
    public void selectPreviousBookmark() {
        fileManager.selectPreviousBookmark(tabControl.getSelectedIndex());
    }

    @Override
    public void toggleBookmark() {
        fileManager.toggleBookmark(tabControl.getSelectedIndex());
    }

    @Override
    public List<BookmarkInfo> listBookmarks() {
        List<BookmarkInfo> bookmarks = new ArrayList<>();
        for (FileEditor editor : fileManager.getFileEditors()) {
            if (editor == null) {
                continue;
            }
            String filePath = editor.getFilePath();
            String fileName = editor.getShortFilename();
            for (FileEditor.BookmarkLine bookmark : editor.getBookmarks()) {
                bookmarks.add(new BookmarkInfo(filePath, fileName, bookmark.lineNumber(), bookmark.lineText()));
            }
        }
        bookmarks.sort(Comparator.comparing(BookmarkInfo::fileName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(BookmarkInfo::lineNumber));
        return bookmarks;
    }

    @Override
    public void goToBookmark(String filePath, int lineNumber) {
        int index = getTabIndex(filePath);
        if (index == -1 && filePath != null && !filePath.isBlank()) {
            File file = new File(filePath);
            if (file.exists()) {
                addTab(FileEditor.open(file, this, fileManager, this, linkGenerator, searchContext));
                index = getTabIndex(filePath);
            }
        }
        if (index < 0 || index >= fileManager.getFileEditors().size()) {
            return;
        }
        tabControl.setSelectedIndex(index);
        FileEditor editor = fileManager.getFileEditors().get(index);
        if (editor != null) {
            editor.goToLine(lineNumber);
        }
    }

    private int findOpenTabIndexByPath(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return -1;
        }
        for (int i = 0; i < tabControl.getTabCount(); i++) {
            IFileViewer viewer = getFileViewerAt(i);
            if (viewer == null || viewer.getFilePath() == null) {
                continue;
            }
            if (absolutePath.equals(viewer.getFilePath())) {
                return i;
            }
        }
        return fileManager.getTabIndex(absolutePath);
    }

    public void setWorkspaceDirectory(File folder) {
        if (folder == null) {
            return;
        }
        File absoluteFolder = folder.getAbsoluteFile();
        if (!absoluteFolder.exists() || !absoluteFolder.isDirectory()) {
            JOptionPane.showMessageDialog(frame, "Folder not found: " + absoluteFolder.getAbsolutePath());
            return;
        }
        fileManager.setCurrentDirectory(absoluteFolder.getAbsolutePath());
        registerWorkspace(absoluteFolder);
        refreshSidebarContent();
    }

    @Override
    public void insertText(String text, int caretOffset) {

        int selectedTab = tabControl.getSelectedIndex();
        if (selectedTab < 0 || selectedTab >= fileManager.getFileEditors().size()) {
            return;
        }
        JTextArea editorPane = fileManager.getFileEditors().get(selectedTab).getEditorPane();
        int insertStart = editorPane.getSelectionStart();
        editorPane.replaceSelection(text);
        editorPane.setCaretPosition(
                Math.min(insertStart + caretOffset, editorPane.getDocument().getLength()));
        editorPane.requestFocusInWindow();
    }

    private void registerWorkspace(File folder) {
        if (folder == null) {
            return;
        }
        File absolute = folder.getAbsoluteFile();
        if (!absolute.exists() || !absolute.isDirectory()) {
            return;
        }
        recentWorkspaces.removeIf(existing -> existing == null
                || !existing.exists()
                || existing.getAbsoluteFile().equals(absolute));
        recentWorkspaces.add(0, absolute);
        while (recentWorkspaces.size() > MAX_RECENT_WORKSPACES) {
            recentWorkspaces.remove(recentWorkspaces.size() - 1);
        }
        saveRecentWorkspaces();
        setRecentItems(basicEditor.getRecentFiles());
        refreshEmptyStateRecentItems();
    }

    private void loadRecentWorkspaces() {
        recentWorkspaces.clear();
        File config = new File(applicationStoragePath, RECENT_WORKSPACES_FILE);
        if (!config.exists()) {
            return;
        }
        Properties properties = new Properties();
        try (FileInputStream stream = new FileInputStream(config)) {
            properties.load(stream);
            String csv = properties.getProperty(RECENT_WORKSPACES_KEY, "");
            for (String entry : csv.split(",")) {
                if (entry == null || entry.isBlank()) {
                    continue;
                }
                File folder = new File(entry.trim()).getAbsoluteFile();
                if (folder.exists() && folder.isDirectory()) {
                    recentWorkspaces.add(folder);
                }
                if (recentWorkspaces.size() >= MAX_RECENT_WORKSPACES) {
                    break;
                }
            }
        } catch (IOException ignored) {
            // Ignore workspace history load errors to avoid interrupting startup.
        }
    }

    private void saveRecentWorkspaces() {
        File config = new File(applicationStoragePath, RECENT_WORKSPACES_FILE);
        Properties properties = new Properties();
        String csv = recentWorkspaces.stream()
                .filter(Objects::nonNull)
                .map(File::getAbsolutePath)
                .distinct()
                .limit(MAX_RECENT_WORKSPACES)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        properties.setProperty(RECENT_WORKSPACES_KEY, csv);
        try {
            File parent = config.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileOutputStream out = new FileOutputStream(config)) {
                properties.store(out, "Recent workspaces");
            }
        } catch (IOException ignored) {
            // Ignore workspace history save errors.
        }
    }

    private void refreshEmptyStateRecentItems() {
        if (!(emptyTabPanel instanceof EmptyTabPanel) || basicEditor == null) {
            return;
        }
        if (basicEditor.getMode() != ApMode.AP_CLOSED) {
            return;
        }
        emptyTabPanel = new EmptyTabPanel(
                this,
                newMenuItem.getAccelerator(),
                openMenuItem.getAccelerator(),
                openFolderMenuItem.getAccelerator(),
                basicEditor.getRecentFiles(),
                recentWorkspaces);
        setEditorContent(emptyTabPanel);
    }

    private void setClosingTabsEnabled(boolean enabled) {
        tabControl.putClientProperty(TABBED_PANE_TAB_CLOSABLE, enabled);
        // TODO get main file index
        //        int main = 0;
        // TODO only disable closing the tab with the main program
        //        if (enabled) {
        //            for (int i = 0; i < mTabControl.getTabCount(); i++) {
        //                ((ButtonTabComponent)
        // mTabControl.getTabComponentAt(i)).getButton().setEnabled(true);
        //            }
        //        } else {
        //            if (main > -1 && main < mTabControl.getTabCount()) {
        //                ((ButtonTabComponent)
        // mTabControl.getTabComponentAt(main)).getButton().setEnabled(false);
        //            }
        //        }
    }

    private void showFindReplaceMenu(boolean replace) {
        if (fileManager.getFileEditors().isEmpty()
                || this.tabControl.getTabCount() == 0
                || this.tabControl.getSelectedIndex() == -1) {

            return;
        }

        int index = tabControl.getSelectedIndex();
        fileManager.getFileEditors().get(index).toggleFindToolBar(replace);
    }

    @Override
    public void onSearchResult(String message) {
        setCompilerStatus(message);
    }

    @Override
    public void onBookmarksChanged(String filePath) {
        for (IEditorPanelProvider panel : panels) {
            panel.onFileModified(filePath);
        }
    }

    @Override
    public void onNewClick() {
        actionNew();
    }

    @Override
    public void onOpenClick() {
        actionOpen();
    }

    @Override
    public void onOpenClick(File file) {
        openFileWithPreferredViewer(file);
    }

    @Override
    public void onOpenFolderClick() {
        actionOpenFolder();
    }

    @Override
    public void onOpenWorkspaceClick(File folder) {
        setWorkspaceDirectory(folder);
    }

    @Override
    public void onCurrentDirectoryChanged(String directory) {
        basicEditor.onCurrentDirectoryChanged(directory);
        // TODO move into editor
        refreshSidebarContent();
    }

    @Override
    public void addHelp(String label, com.basic4gl.desktop.spi.MenuActionListener listener) {
        JMenuItem helpMenuItem = new JMenuItem(label);
        helpMenuItem.addActionListener(e -> listener.actionPerformed(frame, e));
        helpMenu.add(helpMenuItem);
    }
}
