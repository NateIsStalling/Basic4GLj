package com.basic4gl.desktop;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;
import static com.formdev.flatlaf.FlatClientProperties.*;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.callbacks.StackTraceCallback;
import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.desktop.debugger.DebugServerConstants;
import com.basic4gl.desktop.debugger.DebugServerFactory;
import com.basic4gl.desktop.editor.*;
import com.basic4gl.desktop.spi.*;
import com.basic4gl.desktop.spi.language.FunctionDefinition;
import com.basic4gl.desktop.spi.language.LabelDefinition;
import com.basic4gl.desktop.spi.language.VariableDefinition;
import com.basic4gl.desktop.vmview.DebugControlsListener;
import com.basic4gl.desktop.vmview.VirtualMachineViewDialog;
import com.basic4gl.language.core.extensions.FunctionLibrary;
import com.basic4gl.language.core.extensions.Library;
import com.basic4gl.language.core.internal.Mutable;
import com.basic4gl.language.core.types.BasicValType;
import com.basic4gl.language.core.types.FunctionSpecification;
import com.basic4gl.language.core.types.ValType;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
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
                MenuService {

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
    private final JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private final JSplitPane debugPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JTabbedPane tabControl = new JTabbedPane();
    private final JTabbedPane splitTabControl = new JTabbedPane();
    private final JSplitPane editorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JPanel primaryTabHost = new JPanel(new BorderLayout());
    private final JButton addTabDropdownButton = new JButton("+");
    private final JSplitPane workspacePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JSplitPane contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JPanel leftSidebarContainer = new JPanel(new BorderLayout());
    private final JPanel leftSidebarContent = new JPanel(new CardLayout());
    private final JToolBar leftSidebarRail = new JToolBar(SwingConstants.VERTICAL);
    private final ButtonGroup leftSidebarGroup = new ButtonGroup();
    private final Map<String, JToggleButton> leftSidebarButtons = new HashMap<>();
    private final JTabbedPane docsTabs = new JTabbedPane();
    private final JPanel rightDocsContainer = new JPanel(new BorderLayout());
    private final JToolBar rightDocsRail = new JToolBar(SwingConstants.VERTICAL);
    private final ButtonGroup rightDocsGroup = new ButtonGroup();
    private final Map<String, JToggleButton> rightDocsButtons = new HashMap<>();
    private final JTree fileBrowserTree = new JTree();
    private final JTree assetsTree = new JTree();
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    private final DefaultListModel<String> assetsListModel = new DefaultListModel<>();
    private final JList<String> assetsList = new JList<>(assetsListModel);
    private final JComboBox<String> runTargetCombo = new JComboBox<>();
    private boolean updatingRunTargetCombo = false;
    private final DefaultListModel<ReferenceItem> referenceListModel = new DefaultListModel<>();
    private final JList<ReferenceItem> referenceList = new JList<>(referenceListModel);
    private final JTextField referenceSearchField = new JTextField();
    private final JComboBox<String> referenceKindFilter =
            new JComboBox<>(new String[] {"All", "Functions", "Constants", "Labels", "Variables", "Structs"});
    private final JComboBox<String> referenceSourceFilter =
            new JComboBox<>(new String[] {"All sources", "Builtin", "Libraries", "Program"});
    private final JComboBox<String> referenceLibraryFilter = new JComboBox<>(new String[] {"All libraries"});
    private final JButton referenceFiltersButton = new JButton("Filters");
    private final JPopupMenu referenceFiltersPopup = new JPopupMenu();
    private final JTextPane referenceDetailsPane = new JTextPane();
    private final JButton referenceInsertButton = new JButton("Insert");
    private final javax.swing.Timer referenceFilterDebounceTimer =
            new javax.swing.Timer(120, e -> filterReferenceItems());
    private boolean updatingReferenceFilters = false;
    private static final String REFERENCE_NO_MATCHES_HTML =
            "<html><body style='font-family:sans-serif;padding:6px;'>No matches.</body></html>";
    private static final String REFERENCE_SELECT_PROMPT_HTML =
            "<html><body style='font-family:sans-serif;padding:6px;'>Select an entry.</body></html>";
    private final java.util.List<ReferenceItem> allReferenceItems = new ArrayList<>();
    // Language support is shared between the symbol indexer and (via BasicTokenMaker) the editor.
    private final com.basic4gl.desktop.language.LanguageSupport languageSupport =
            new com.basic4gl.desktop.language.Basic4GLLanguageSupport();
    private final SymbolIndexer symbolIndexer =
            new SymbolIndexer(languageSupport, this::collectAllSourceText, this::updateProgramSymbols);
    private int lastProgramSymbolsFingerprint = Integer.MIN_VALUE;
    private int expandedLeftSidebarWidth = 260;
    private int expandedRightDocsWidth = 320;
    private String activeLeftSidebarKey = "files";
    private String activeRightDocsKey = "functions";
    private JPanel emptyTabPanel;

    private static final class ReferenceItem {
        final String kind;
        final String name;
        final String signature;
        final String library;
        final String details;
        final String insertText;
        final int caretOffset;

        ReferenceItem(
                String kind,
                String name,
                String signature,
                String library,
                String details,
                String insertText,
                int caretOffset) {
            this.kind = kind;
            this.name = name;
            this.signature = signature;
            this.library = library;
            this.details = details;
            this.insertText = insertText;
            this.caretOffset = caretOffset;
        }

        @Override
        public String toString() {
            return signature;
        }
    }

    private static final class AssetItem {
        final String title;
        final String subtitle;
        final File file;
        final Icon icon;

        AssetItem(String title, String subtitle, File file, Icon icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.file = file;
            this.icon = icon;
        }

        boolean isOpenable() {
            return file != null && file.isFile();
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final JMenu bookmarkSubMenu = new JMenu("Bookmarks");
    private final JMenu breakpointSubMenu = new JMenu("Breakpoints");
    private final JMenu helpMenu = new JMenu("Help");

    // Menu Items
    private final JMenuItem newMenuItem = new JMenuItem("New Program");
    private final JMenuItem openMenuItem = new JMenuItem("Open Program...");
    private final JMenuItem recentSubMenu = new JMenu("Open Recent");
    private final JMenuItem clearRecentMenuItem = new JMenuItem("Clear Recently Opened...");
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

    private final JToggleButton debugButton = new JToggleButton(createImageIcon(ICON_DEBUG));
    private final JButton playButton = new JButton(createImageIcon(ICON_PLAY));
    private final JButton stepOverButton = new JButton(createImageIcon(ICON_STEP_OVER));
    private final JButton stepInButton = new JButton(createImageIcon(ICON_STEP_IN));
    private final JButton stepOutButton = new JButton(createImageIcon(ICON_STEP_OUT));
    private final JButton exportButton = new JButton(createImageIcon(ICON_EXPORT));
    private final JButton settingsButton = new JButton(createImageIcon(ICON_SETTINGS));
    private final JSeparator debugSeparator = new JSeparator(JSeparator.VERTICAL);
    // Labels
    private final JLabel compilerStatusLabel = new JLabel(""); // Compiler/VM Status
    private final JLabel cursorPositionLabel = new JLabel("0:0"); // Cursor Position

    // Debugging
    private final DefaultListModel<String> watchListModel = new DefaultListModel<>();
    private final JList<String> watchListBox = new JList<>(watchListModel);
    private final DefaultListModel<String> gosubListModel = new DefaultListModel<>();

    // Editors
    private BasicEditor basicEditor;
    private FileManager fileManager;

    private IncludeLinkGenerator linkGenerator = new IncludeLinkGenerator(this);

    private SearchContext searchContext;

    // Debugging
    private boolean isDebugMode = false;
    private VirtualMachineViewDialog virtualMachineViewDialog;
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
                File outputBin = new File(appDirectory, "lib/library-1.0-SNAPSHOT.jar");
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
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, toolkit.getMenuShortcutKeyMask()));
        clearRecentMenuItem.addActionListener(e -> actionClearRecent());
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
            selectRightDocsSection("functions");
        });

        aboutMenuItem.addActionListener(e -> showAboutDialog());

        if (SystemInfo.isMacOS) {
            // hide menu items that are in macOS application menu
            aboutMenuItem.setVisible(false);
            settingsMenuItem.setVisible(false);
            // TODO mExitMenuItem.setVisible(false);
        }

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

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.add(newButton);
        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(runButton);
        toolBar.addSeparator();
        toolBar.add(debugButton);
        toolBar.addSeparator();
        toolBar.add(playButton);
        toolBar.add(runTargetCombo);
        toolBar.add(stepOverButton);
        toolBar.add(stepInButton);
        toolBar.add(stepOutButton);
        toolBar.add(debugSeparator);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(exportButton);
        toolBar.add(settingsButton);

        newButton.addActionListener(e -> actionNew());
        openButton.addActionListener(e -> actionOpen());
        saveButton.addActionListener(e -> actionSave());
        runButton.addActionListener(e -> basicEditor.actionRun());

        debugButton.addActionListener(e -> actionDebugMode());
        playButton.addActionListener(e -> basicEditor.actionPlayPause());
        stepOverButton.addActionListener(e -> basicEditor.actionStep());
        stepInButton.addActionListener(e -> basicEditor.actionStepInto());
        stepOutButton.addActionListener(e -> basicEditor.actionStepOutOf());
        exportButton.addActionListener(e -> actionExport());
        settingsButton.addActionListener(e -> showSettings());
        runButton.setToolTipText("Run the program!");
        runTargetCombo.setToolTipText("Select the runnable source file");
        runTargetCombo.setMaximumSize(new Dimension(260, 30));
        runTargetCombo.addActionListener(e -> onRunTargetSelectionChanged());

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
                            List<Integer> breakpoints = editor.getBreakpoints();
                            String file = editor.getFilePath();

                            for (Integer line : breakpoints) {
                                basicEditor.toggleBreakpt(file, line);
                            }

                            // Remove tab
                            tabControl.remove(tabIndex);
                            fileManager.getFileEditors().remove(tabIndex.intValue());
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
        configureSidebar();
        configureDocsPane();

        editorSplitPane.setLeftComponent(primaryTabHost);
        editorSplitPane.setRightComponent(splitTabControl);
        editorSplitPane.setResizeWeight(0.7);

        mainPane.setTopComponent(primaryTabHost);

        debugPane.setLeftComponent(watchListFrame);
        debugPane.setRightComponent(gosubFrame);

        contentPane.setLeftComponent(mainPane);
        contentPane.setRightComponent(rightDocsContainer);
        contentPane.setResizeWeight(0.74);

        workspacePane.setLeftComponent(leftSidebarContainer);
        workspacePane.setRightComponent(contentPane);
        workspacePane.setResizeWeight(0.18);
        workspacePane.setDividerLocation(expandedLeftSidebarWidth);
        contentPane.setDividerLocation(Math.max(200, frame.getPreferredSize().width - expandedRightDocsWidth));

        // Add controls to window
        frame.add(toolBar, BorderLayout.NORTH);
        frame.add(workspacePane, BorderLayout.CENTER);
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

        basicEditor = new BasicEditor(outputBinPath, fileManager, this, this);

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
        refreshRunnableFileControls();
        populateDocsFromCompiler();
        refreshSidebarContent();

        // Warm up the debug server
        DebugServerFactory.startDebugServer(debugServerBinPath, DebugServerConstants.DEFAULT_DEBUG_SERVER_PORT);

        // Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
                fileManager.getFileEditors(),
                fileManager.getCurrentDirectory(),
                contributedExportPages);
        dialog.setBuilders(basicEditor.getBuilders(), basicEditor.currentBuilder);
        dialog.setVisible(true);
        basicEditor.currentBuilder = dialog.getCurrentBuilder();
    }

    @Override
    public void setRecentItems(List<File> files) {
        recentSubMenu.removeAll();
        for (File file : files) {

            JMenuItem fileMenuItem = new JMenuItem(file.getName());
            recentSubMenu.add(fileMenuItem);
            fileMenuItem.addActionListener(e -> {
                actionOpen(file);
            });
        }
        recentSubMenu.add(new JSeparator());
        recentSubMenu.add(clearRecentMenuItem);
        clearRecentMenuItem.setEnabled(!files.isEmpty());
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
        symbolIndexer.shutdown();
        System.exit(0);
    }

    private void resetProject() {
        // Clear out the current project and setup a new basic one with a single
        // source-file.

        // Close existing editors
        tabControl.removeAll();
        fileManager.getFileEditors().clear();

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

        System.out.println("Open tab: " + filename);
        System.out.println("Path: " + file.getAbsolutePath());

        MainWindow.this.addTab(FileEditor.open(file, this, fileManager, this, linkGenerator, searchContext));

        tabControl.setSelectedIndex(tabControl.getTabCount() - 1);
    }

    public void openTab(File file) {
        System.out.println("Open tab: " + file.getName());
        System.out.println("Path: " + file.getAbsolutePath());

        MainWindow.this.addTab(FileEditor.open(file, this, fileManager, this, linkGenerator, searchContext));

        tabControl.setSelectedIndex(tabControl.getTabCount() - 1);
    }

    void actionNew() {
        if (multifileCheckSaveChanges()) {

            fileManager.setRunDirectory(fileManager.getFileDirectory());
            fileManager.setCurrentDirectory(fileManager.getRunDirectory());

            // Clear file editors
            this.tabControl.removeAll();
            fileManager.getFileEditors().clear();

            this.addTab();
            refreshSidebarContent();
        }
    }

    void actionOpen() {
        actionOpen(null);
    }

    void actionOpen(File file) {
        if (multifileCheckSaveChanges()) {
            FileEditor editor = null;
            if (file != null) {
                fileManager.setCurrentDirectory(fileManager.getFileDirectory());
                editor = FileEditor.open(file, this, fileManager, this, linkGenerator, searchContext);
            } else {
                fileManager.setCurrentDirectory(fileManager.getFileDirectory());
                editor = FileEditor.open(frame, this, fileManager, this, linkGenerator, searchContext);
            }

            openEditor(editor);
        }
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

    boolean fileCheckSaveChanges(int index) {

        // Is sub-file modified?
        FileEditor editor = fileManager.getFileEditors().get(index);
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
            tabControl.setTitleAt(index, fileManager.getFileEditors().get(index).getTitle());
            tabControl.getTabComponentAt(index).invalidate();
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
            tabControl.setTitleAt(index, fileManager.getFileEditors().get(index).getTitle());
            tabControl.getTabComponentAt(index).invalidate();
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
        tabControl.setTitleAt(index, fileManager.getFileEditors().get(index).getTitle());
        tabControl.getTabComponentAt(index).invalidate();
    }

    private void actionDebugMode() {
        // Toggle debug mode
        isDebugMode = !isDebugMode;
        debugMenuItem.setSelected(isDebugMode);
        debugButton.setSelected(isDebugMode);

        refreshDebugDisplays(basicEditor.getMode());
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

        java.util.List<com.basic4gl.desktop.language.SymbolDeclaration> declarations =
                collectOpenFileDeclarations();
        java.util.List<com.basic4gl.desktop.language.SymbolDeclaration> matches = declarations.stream()
                .filter(d -> ("label".equals(d.kind()) || "variable".equals(d.kind()))
                        && d.name().equalsIgnoreCase(symbol))
                .toList();

        if (matches.isEmpty()) {
            setCompilerStatus("Declaration not found for: " + symbol);
            return;
        }

        com.basic4gl.desktop.language.SymbolDeclaration selected =
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

    private java.util.List<com.basic4gl.desktop.language.SymbolDeclaration> collectOpenFileDeclarations() {
        java.util.List<com.basic4gl.desktop.language.SymbolDeclaration> declarations = new ArrayList<>();
        for (FileEditor editor : fileManager.getFileEditors()) {
            String fileId = editor.getFilePath();
            if (fileId == null || fileId.isBlank()) {
                File file = editor.getFile();
                fileId = file != null ? file.getAbsolutePath() : "<unsaved:" + editor.getTitle() + ">";
            }
            declarations.addAll(languageSupport.extractDeclarations(editor.getEditorPane().getText(), fileId));
        }
        return declarations;
    }

    private com.basic4gl.desktop.language.SymbolDeclaration chooseDeclarationForCaret(
            java.util.List<com.basic4gl.desktop.language.SymbolDeclaration> matches, String activeFile, int caretLine) {
        java.util.List<com.basic4gl.desktop.language.SymbolDeclaration> sameFile = matches.stream()
                .filter(d -> Objects.equals(d.fileId(), activeFile))
                .toList();
        java.util.List<com.basic4gl.desktop.language.SymbolDeclaration> candidates =
                sameFile.isEmpty() ? matches : sameFile;

        com.basic4gl.desktop.language.SymbolDeclaration best = null;
        int bestScore = Integer.MAX_VALUE;
        for (com.basic4gl.desktop.language.SymbolDeclaration candidate : candidates) {
            int score = declarationScore(candidate, activeFile, caretLine);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private int declarationScore(
            com.basic4gl.desktop.language.SymbolDeclaration declaration, String activeFile, int caretLine) {
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

    private com.basic4gl.desktop.language.SymbolDeclaration promptUserForDeclaration(
            java.util.List<com.basic4gl.desktop.language.SymbolDeclaration> matches,
            com.basic4gl.desktop.language.SymbolDeclaration preferred) {
        Object[] options = matches.stream().map(this::formatDeclarationChoice).toArray();
        Object initial = preferred != null ? formatDeclarationChoice(preferred) : (options.length > 0 ? options[0] : null);
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
        for (com.basic4gl.desktop.language.SymbolDeclaration declaration : matches) {
            if (formatDeclarationChoice(declaration).equals(selectedText)) {
                return declaration;
            }
        }
        return preferred;
    }

    private String formatDeclarationChoice(com.basic4gl.desktop.language.SymbolDeclaration declaration) {
        String fileLabel = declaration.fileId();
        File f = fileLabel == null ? null : new File(fileLabel);
        if (f != null && f.getName() != null && !f.getName().isBlank()) {
            fileLabel = f.getName();
        }
        return declaration.kind() + "  " + declaration.signature()
                + "  (" + fileLabel + ":" + (declaration.line() + 1) + ")";
    }

    private void goToDeclarationLocation(com.basic4gl.desktop.language.SymbolDeclaration declaration) {
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
            targetOffset = Math.min(lineStart + Math.max(0, declaration.column()), pane.getDocument().getLength());
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
        fileManager.getFileEditors().remove(index);
        fileManager.ensureRunnableFileValid();
        refreshRunnableFileControls();
        refreshSidebarContent();
        symbolIndexer.schedule();
    }

    public void addTab() {
        final FileEditor editor = new FileEditor(this, fileManager, this, linkGenerator, searchContext);
        addTab(editor);
    }

    public void addTab(FileEditor editor) {

        int count = fileManager.editorCount();
        fileManager.getFileEditors().add(editor);

        // replace emptyTabPanel if needed
        mainPane.setTopComponent(getActiveEditorHost());

        tabControl.addTab(editor.getTitle(), editor.getContentPane());

        final FileEditor edit = editor;
        File file = edit.getFile();
        if (file != null) {
            basicEditor.notifyFileOpened(file);
            basicEditor.onFileOpened(edit);
        }

        edit.getEditorPane().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int index = getTabIndex(edit.getFilePath());
                edit.setModified();
                tabControl.setTitleAt(index, edit.getTitle());
                symbolIndexer.schedule();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int index = getTabIndex(edit.getFilePath());
                edit.setModified();
                tabControl.setTitleAt(index, edit.getTitle());
                symbolIndexer.schedule();
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
        editor.getEditorPane().addCaretListener(TrackCaretPosition);
        cursorPositionLabel.setText(0 + ":" + 0); // Reset label

        // Set tab as read-only if App is running or paused
        boolean readOnly = basicEditor.getMode() != ApMode.AP_STOPPED;
        editor.getEditorPane().setEditable(!readOnly);

        // TODO set syntax highlight colors

        // Refresh interface if there was previously no tabs open
        if (count == 0) {
            basicEditor.setMode(ApMode.AP_STOPPED, null);
        }

        fileManager.ensureRunnableFileValid();
        refreshRunnableFileControls();
        refreshSidebarContent();
        symbolIndexer.schedule();
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

        // Place editor into debug mode
        isDebugMode = true;
        debugMenuItem.setSelected(true);
        debugButton.setSelected(true);
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
    public void onCompileSucceeded() {
        populateDocsFromCompiler();
        // Also sync the indexer immediately so the debounced background pass
        // reflects the compiled state right away.
        symbolIndexer.indexNow();
    }

    @Override
    public void onModeChanged(ApMode mode, String statusMsg) {
        if (mode != ApMode.AP_CLOSED) {
            copyMenuItem.setEnabled(true);
            findMenuItem.setEnabled(true);
            replaceMenuItem.setEnabled(true);
            goToDeclarationMenuItem.setEnabled(true);
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

                emptyTabPanel = new EmptyTabPanel(
                        this,
                        newMenuItem.getAccelerator(),
                        openMenuItem.getAccelerator(),
                        basicEditor.getRecentFiles());
                mainPane.setTopComponent(emptyTabPanel);
                break;
            case AP_STOPPED:
                mainPane.setTopComponent(getActiveEditorHost());
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
                mainPane.setTopComponent(getActiveEditorHost());
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

        // Show/hide debug controls
        playButton.setVisible(isDebugMode);
        stepOverButton.setVisible(isDebugMode);
        stepInButton.setVisible(isDebugMode);
        stepOutButton.setVisible(isDebugMode);
        debugSeparator.setVisible(isDebugMode);

        // TODO Show/hide debug pane
        if (isDebugMode) {
            mainPane.setResizeWeight(0.7);
            // mDebugPane.setEnabled(true);
            mainPane.setEnabled(true);
            mainPane.setBottomComponent(debugPane);
            SwingUtilities.invokeLater(() -> debugPane.setDividerLocation(0.7));
        } else {
            mainPane.remove(debugPane);
            // mDebugPane.setEnabled(false);
            mainPane.setEnabled(false);
        }

        if (mode != ApMode.AP_CLOSED) {
            playButton.setIcon(mode == ApMode.AP_RUNNING ? createImageIcon(ICON_PAUSE) : createImageIcon(ICON_PLAY));
            playButton.setEnabled(mode != ApMode.AP_WAITING);
            playPauseMenuItem.setEnabled(mode != ApMode.AP_WAITING);
            stepOverButton.setEnabled(mode != ApMode.AP_RUNNING && mode != ApMode.AP_WAITING);
            stepInButton.setEnabled(mode != ApMode.AP_RUNNING && mode != ApMode.AP_WAITING);

            // TODO 12/2022 determine appropriate state for mStepOutButton;
            // does the editor even need to care about UserCallStack size with remote debugger protocol
            // setup?
            stepOutButton.setEnabled(mode == ApMode.AP_PAUSED);
            // TODO old mStepOutButton.setEnabled(mode == ApMode.AP_PAUSED &&
            // (mEditor.mVM.UserCallStack().size() > 0));
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

        for (String label : basicEditor.getLanguageService().buildFriendlyCallStackLabels(stackTraceCallback)) {
            gosubListModel.addElement(label);
        }
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
        int index = 0;
        for (String watch : basicEditor.getWatches()) {
            if (Objects.equals(watch, evaluatedWatch)) {
                watchListModel.setElementAt(watch + ": " + result, index);
            }
            index++;
        }
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
        // Clear debug controls
        watchListModel.clear();

        for (String watch : basicEditor.getWatches()) {

            watchListModel.addElement(watch + ": " + "???");
        }
        watchListModel.addElement(" "); // Last line is blank, and can be clicked on to add new watch
    }

    private void editWatch() {
        String newWatch, oldWatch;

        // Find watch
        int index = watchListBox.getSelectedIndex();
        int saveIndex = index;

        // Extract watch text
        oldWatch = basicEditor.getWatchOrDefault(index);

        // Prompt for new text
        newWatch = (String) JOptionPane.showInputDialog(
                frame, "Enter variable/expression:", "Watch variable", JOptionPane.PLAIN_MESSAGE, null, null, oldWatch);

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
    public void onToggleBreakpoint(String filePath, int line) {
        basicEditor.toggleBreakpt(filePath, line);
    }

    private Component getActiveEditorHost() {
        return splitTabControl.getTabCount() == 0 ? primaryTabHost : editorSplitPane;
    }

    private void configurePrimaryTabHost() {
        addTabDropdownButton.setFocusable(false);
        addTabDropdownButton.setToolTipText("Create a new tab or open an asset");
        addTabDropdownButton.setMargin(new Insets(2, 8, 2, 8));
        addTabDropdownButton.addActionListener(e -> showCreateTabMenu(addTabDropdownButton));
        tabControl.putClientProperty(TABBED_PANE_LEADING_COMPONENT, addTabDropdownButton);
        primaryTabHost.add(tabControl, BorderLayout.CENTER);
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
                            mainPane.setTopComponent(primaryTabHost);
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
        JMenuItem setRunnable = new JMenuItem("Set as runnable file");
        setRunnable.addActionListener(x -> {
            fileManager.setRunnableFilePath(
                    fileManager.getFileEditors().get(tabIndex).getFilePath());
            refreshRunnableFileControls();
        });
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
        File source = fileManager.getFileEditors().get(tabIndex).getFile();
        if (source == null) {
            return;
        }

        FileEditor preview = FileEditor.open(source, this, fileManager, this, linkGenerator, searchContext);
        preview.getEditorPane().setEditable(false);
        splitTabControl.addTab(preview.getTitle() + " (split)", preview.getContentPane());
        splitTabControl.setSelectedIndex(splitTabControl.getTabCount() - 1);
        mainPane.setTopComponent(getActiveEditorHost());
        SwingUtilities.invokeLater(() -> editorSplitPane.setDividerLocation(0.68));
    }

    private void popOutTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= fileManager.getFileEditors().size()) {
            return;
        }
        File source = fileManager.getFileEditors().get(tabIndex).getFile();
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

        JMenuItem openReadmeItem = new JMenuItem("Open README.md in docs");
        openReadmeItem.addActionListener(e -> openMarkdownInDocsTab(new File("README.md")));
        popup.add(openReadmeItem);

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
        if (selected.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
            openMarkdownInDocsTab(selected);
        } else {
            openTab(selected);
        }
    }

    private void configureSidebar() {
        leftSidebarRail.setFloatable(false);
        leftSidebarRail.setRollover(true);

        leftSidebarContent.add(buildFileBrowserPanel(), "files");
        leftSidebarContent.add(buildAssetsPanel(), "assets");
        leftSidebarContent.add(buildBookmarkActionsPanel(), "bookmarks");
        leftSidebarContent.add(buildDebugActionsPanel(), "debug");

        addLeftSidebarButton("files", createImageIcon(ICON_MENU_FOLDER), "Files");
        addLeftSidebarButton("assets", createImageIcon(ICON_MENU_ASSETS), "Assets");
        addLeftSidebarButton("bookmarks", createImageIcon(ICON_MENU_BOOKMARKS), "Bookmarks");
        leftSidebarRail.add(Box.createVerticalGlue());
        addLeftSidebarButton("debug", createImageIcon(ICON_MENU_DEBUG), "Debug");

        leftSidebarContainer.add(leftSidebarRail, BorderLayout.WEST);
        leftSidebarContainer.add(leftSidebarContent, BorderLayout.CENTER);
        selectLeftSidebarSection("files", true);
    }

    private JPanel buildFileBrowserPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Workspace Browser");
        title.setBorder(new EmptyBorder(4, 8, 0, 8));
        JButton refresh = new JButton("Refresh");
        refresh.setFocusable(false);
        refresh.addActionListener(e -> refreshFileBrowserTree());
        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        fileBrowserTree.setRootVisible(true);
        fileBrowserTree.setShowsRootHandles(true);
        fileBrowserTree.setRowHeight(22);
        fileBrowserTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof File file) {
                    label.setText(file == null ? "" : fileSystemView.getSystemDisplayName(file));
                    if (label.getText() == null || label.getText().isBlank()) {
                        label.setText(file.getName().isBlank() ? file.getPath() : file.getName());
                    }
                    label.setIcon(fileSystemView.getSystemIcon(file));
                    label.setToolTipText(file.getAbsolutePath());
                }
                return label;
            }
        });
        fileBrowserTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                TreePath path = fileBrowserTree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (!(userObject instanceof File file) || !file.isFile()) {
                    return;
                }
                if (file.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
                    openMarkdownInDocsTab(file);
                } else {
                    openTab(file);
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(fileBrowserTree);
        configureSmoothScrolling(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAssetsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Assets");
        title.setBorder(new EmptyBorder(4, 8, 0, 8));
        JButton refresh = new JButton("Refresh");
        refresh.setFocusable(false);
        refresh.addActionListener(e -> refreshAssetsLibrary());
        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        assetsTree.setRootVisible(false);
        assetsTree.setShowsRootHandles(true);
        assetsTree.setRowHeight(24);
        assetsTree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof AssetItem item) {
                    label.setText(item.subtitle == null || item.subtitle.isBlank()
                            ? item.title
                            : "<html><b>" + escapeHtml(item.title) + "</b><br/><span style='color:gray;'>"
                                    + escapeHtml(item.subtitle) + "</span></html>");
                    label.setIcon(item.icon);
                    label.setToolTipText(item.file != null ? item.file.getAbsolutePath() : item.subtitle);
                }
                return label;
            }
        });

        assetsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) {
                    return;
                }
                TreePath path = assetsTree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }
                Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (!(userObject instanceof AssetItem item) || !item.isOpenable()) {
                    return;
                }
                if (item.file.getName().toLowerCase(Locale.ROOT).endsWith(".md")) {
                    openMarkdownInDocsTab(item.file);
                } else {
                    openTab(item.file);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(assetsTree);
        configureSmoothScrolling(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBookmarkActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton next = new JButton("Next bookmark");
        next.addActionListener(e -> fileManager.selectNextBookmark(tabControl.getSelectedIndex()));
        JButton previous = new JButton("Previous bookmark");
        previous.addActionListener(e -> fileManager.selectPreviousBookmark(tabControl.getSelectedIndex()));
        JButton toggle = new JButton("Toggle bookmark");
        toggle.addActionListener(e -> fileManager.toggleBookmark(tabControl.getSelectedIndex()));

        panel.add(next);
        panel.add(previous);
        panel.add(toggle);
        return panel;
    }

    private JPanel buildDebugActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton toggleDebug = new JButton("Toggle debug mode");
        toggleDebug.addActionListener(e -> actionDebugMode());
        JButton playPause = new JButton("Play/Pause");
        playPause.addActionListener(e -> basicEditor.actionPlayPause());
        JButton stepOver = new JButton("Step over");
        stepOver.addActionListener(e -> basicEditor.actionStep());
        JButton stepInto = new JButton("Step into");
        stepInto.addActionListener(e -> basicEditor.actionStepInto());
        JButton stepOut = new JButton("Step out");
        stepOut.addActionListener(e -> basicEditor.actionStepOutOf());

        panel.add(toggleDebug);
        panel.add(playPause);
        panel.add(stepOver);
        panel.add(stepInto);
        panel.add(stepOut);
        return panel;
    }

    private void configureDocsPane() {
        JPanel lookupPanel = new JPanel(new BorderLayout(6, 6));
        JPanel lookupHeader = new JPanel(new BorderLayout(6, 6));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        referenceFiltersButton.setFocusable(false);
        referenceFiltersButton.setToolTipText("Open reference filters");
        leftHeader.add(referenceFiltersButton);
        lookupHeader.add(leftHeader, BorderLayout.WEST);

        lookupHeader.add(referenceSearchField, BorderLayout.CENTER);
        referenceInsertButton.setFocusable(false);
        referenceInsertButton.setEnabled(false);
        lookupHeader.add(referenceInsertButton, BorderLayout.EAST);

        referenceSearchField.setToolTipText("Search by name, signature, or library");
        referenceKindFilter.setToolTipText("Filter by kind");
        referenceSourceFilter.setToolTipText("Filter by builtin, libraries, or program symbols");
        referenceLibraryFilter.setToolTipText("Filter by library name");
        referenceKindFilter.setPrototypeDisplayValue("Functions");
        rebuildReferenceFiltersPopup();

        referenceFilterDebounceTimer.setRepeats(false);

        referenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        referenceList.setFixedCellHeight(20);
        referenceList.setPrototypeCellValue(new ReferenceItem(
                "function", "prototype", "prototype(symbol, arg)", "Builtin", "", "", 0));
        referenceList.setCellRenderer(new DefaultListCellRenderer() {
            private final ImageIcon functionIcon = createImageIcon(ICON_FUNCTION);
            private final ImageIcon variableIcon = createImageIcon(ICON_VARIABLE);
            private final ImageIcon labelIcon = createImageIcon(ICON_LABEL);
            private final ImageIcon structIcon = createImageIcon(ICON_STRUCT);

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label =
                        (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ReferenceItem item) {
                    label.setText(item.signature);
                    if ("function".equals(item.kind) || "userfunc".equals(item.kind)) {
                        label.setIcon(functionIcon);
                    } else if ("label".equals(item.kind)) {
                        label.setIcon(labelIcon);
                    } else if ("struc".equals(item.kind)) {
                        label.setIcon(structIcon);
                    } else {
                        label.setIcon(variableIcon);
                    }
                    label.setToolTipText(null);
                }
                return label;
            }
        });

        referenceDetailsPane.setEditable(false);
        referenceDetailsPane.setContentType("text/html");

        JSplitPane lookupSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        lookupSplit.setResizeWeight(0.65);
        lookupSplit.setTopComponent(new JScrollPane(referenceList));
        lookupSplit.setBottomComponent(new JScrollPane(referenceDetailsPane));

        lookupPanel.add(lookupHeader, BorderLayout.NORTH);
        lookupPanel.add(lookupSplit, BorderLayout.CENTER);
        docsTabs.addTab("Reference", lookupPanel);

        referenceSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                requestFilterReferenceItems();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                requestFilterReferenceItems();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                requestFilterReferenceItems();
            }
        });
        referenceList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateReferenceSelectionDetails();
            }
        });
        referenceKindFilter.addActionListener(e -> {
            if (!updatingReferenceFilters) {
                updateReferenceFiltersButtonTooltip();
                filterReferenceItems();
            }
        });
        referenceSourceFilter.addActionListener(e -> {
            if (!updatingReferenceFilters) {
                updateReferenceFiltersButtonTooltip();
                filterReferenceItems();
            }
        });
        referenceLibraryFilter.addActionListener(e -> {
            if (!updatingReferenceFilters) {
                updateReferenceFiltersButtonTooltip();
                filterReferenceItems();
            }
        });
        referenceFiltersButton.addActionListener(e -> {
            rebuildReferenceFiltersPopup();
            referenceFiltersPopup.show(referenceFiltersButton, 0, referenceFiltersButton.getHeight());
        });
        referenceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    insertSelectedReference();
                }
            }
        });
        referenceInsertButton.addActionListener(e -> insertSelectedReference());
        updateReferenceFiltersButtonTooltip();

        rightDocsRail.setFloatable(false);
        rightDocsRail.setRollover(true);

        addRightDocsButton("functions", createImageIcon(ICON_MENU_FUNCTIONS), "Reference lookup");
        addRightDocsButton("docs", createImageIcon(ICON_MENU_HELP), "Markdown docs");

        rightDocsContainer.add(rightDocsRail, BorderLayout.EAST);
        rightDocsContainer.add(docsTabs, BorderLayout.CENTER);
        selectRightDocsSection("functions");
    }

    private void addLeftSidebarButton(String key, Icon icon, String tooltip) {
        JToggleButton button = createRailButton(icon, tooltip);
        button.addActionListener(e -> onLeftSidebarButtonPressed(key));
        leftSidebarGroup.add(button);
        leftSidebarButtons.put(key, button);
        leftSidebarRail.add(button);
    }

    private void addRightDocsButton(String key, Icon icon, String tooltip) {
        JToggleButton button = createRailButton(icon, tooltip);
        button.addActionListener(e -> onRightDocsButtonPressed(key));
        rightDocsGroup.add(button);
        rightDocsButtons.put(key, button);
        rightDocsRail.add(button);
    }

    private JToggleButton createRailButton(Icon icon, String tooltip) {
        JToggleButton button = new JToggleButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        button.setMargin(new Insets(8, 8, 8, 8));
        button.setMaximumSize(new Dimension(38, 38));
        button.setPreferredSize(new Dimension(38, 38));
        return button;
    }

    private void configureSmoothScrolling(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scrollPane.setWheelScrollingEnabled(true);
    }

    private void onLeftSidebarButtonPressed(String key) {
        if (Objects.equals(activeLeftSidebarKey, key) && isLeftSidebarExpanded()) {
            collapseLeftSidebar();
            return;
        }
        selectLeftSidebarSection(key, true);
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

    private boolean isLeftSidebarExpanded() {
        return workspacePane.getDividerLocation() > leftSidebarRail.getPreferredSize().width + 24;
    }

    private void collapseLeftSidebar() {
        if (workspacePane.getDividerLocation() > leftSidebarRail.getPreferredSize().width + 24) {
            expandedLeftSidebarWidth = workspacePane.getDividerLocation();
        }
        workspacePane.setDividerLocation(leftSidebarRail.getPreferredSize().width + 6);
    }

    private void expandLeftSidebar() {
        int target = Math.max(expandedLeftSidebarWidth, 180);
        workspacePane.setDividerLocation(target);
    }

    private void onRightDocsButtonPressed(String key) {
        if (Objects.equals(activeRightDocsKey, key) && isRightDocsExpanded()) {
            collapseRightDocs();
            return;
        }
        selectRightDocsSection(key);
    }

    private void selectRightDocsSection(String key) {
        activeRightDocsKey = key;

        JToggleButton button = rightDocsButtons.get(key);
        if (button != null) {
            button.setSelected(true);
        }

        if ("docs".equals(key) && docsTabs.getTabCount() > 1) {
            docsTabs.setSelectedIndex(docsTabs.getTabCount() - 1);
        } else if (docsTabs.getTabCount() > 0) {
            docsTabs.setSelectedIndex(0);
        }

        expandRightDocs();
        docsTabs.requestFocusInWindow();
    }

    private boolean isRightDocsExpanded() {
        int docsWidth = contentPane.getWidth() - contentPane.getDividerLocation();
        return docsWidth > rightDocsRail.getPreferredSize().width + 28;
    }

    private void collapseRightDocs() {
        int docsWidth = contentPane.getWidth() - contentPane.getDividerLocation();
        if (docsWidth > rightDocsRail.getPreferredSize().width + 28) {
            expandedRightDocsWidth = docsWidth;
        }
        int collapsedWidth = rightDocsRail.getPreferredSize().width + 8;
        contentPane.setDividerLocation(Math.max(120, contentPane.getWidth() - collapsedWidth));
    }

    private void expandRightDocs() {
        int targetDocsWidth = Math.max(expandedRightDocsWidth, 220);
        int newDivider = Math.max(120, contentPane.getWidth() - targetDocsWidth);
        contentPane.setDividerLocation(newDivider);
    }

    private void refreshSidebarContent() {
        refreshFileBrowserTree();
        refreshAssetsLibrary();
    }

    private void refreshFileBrowserTree() {
        File root = new File(fileManager.getCurrentDirectory());
        DefaultMutableTreeNode rootNode = buildFileTreeNode(root, 0, 5);
        fileBrowserTree.setModel(new DefaultTreeModel(rootNode));
        if (fileBrowserTree.getRowCount() > 0) {
            fileBrowserTree.expandRow(0);
        }
    }

    private DefaultMutableTreeNode buildFileTreeNode(File file, int depth, int maxDepth) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
        if (!file.isDirectory() || depth >= maxDepth) {
            return node;
        }

        File[] children = file.listFiles();
        if (children == null) {
            return node;
        }
        Arrays.sort(children, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File child : children) {
            if (child.getName().startsWith(".")) {
                continue;
            }
            node.add(buildFileTreeNode(child, depth + 1, maxDepth));
        }
        return node;
    }

    private void refreshAssetsLibrary() {
        File rootDir = new File(fileManager.getCurrentDirectory());
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
                new AssetItem(
                        "Assets",
                        "Workspace resources, libraries, and embedded literals",
                        null,
                        createImageIcon(ICON_MENU_ASSETS)));

        DefaultMutableTreeNode workspaceNode = buildMediaTypeSection(
                "Workspace Resources",
                collectWorkspaceAssets(rootDir, 0, 4),
                rootDir,
                createImageIcon(ICON_MENU_FOLDER));
        if (workspaceNode != null) {
            rootNode.add(workspaceNode);
        }

        DefaultMutableTreeNode literalNode = buildMediaTypeSection(
                "Embedded Literals",
                detectLiteralAssets(rootDir),
                rootDir,
                createImageIcon(ICON_MENU_ASSETS));
        if (literalNode != null) {
            rootNode.add(literalNode);
        }

        DefaultMutableTreeNode librariesNode = buildLibraryResourcesNode(rootDir);
        if (librariesNode != null) {
            rootNode.add(librariesNode);
        }

        assetsTree.setModel(new DefaultTreeModel(rootNode));
        for (int i = 0; i < Math.min(4, assetsTree.getRowCount()); i++) {
            assetsTree.expandRow(i);
        }
    }

    private DefaultMutableTreeNode buildMediaTypeSection(String title, java.util.List<File> files, File baseDir, Icon sectionIcon) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        Map<String, java.util.List<File>> byMediaType = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (File file : files) {
            String mediaType = getMediaTypeLabel(file);
            byMediaType.computeIfAbsent(mediaType, k -> new ArrayList<>()).add(file);
        }
        DefaultMutableTreeNode section = new DefaultMutableTreeNode(
                new AssetItem(title, files.size() + " file(s)", null, sectionIcon));
        for (String mediaType : List.of("Images", "Audio", "Video", "Text", "Documents", "Other")) {
            java.util.List<File> bucket = byMediaType.get(mediaType);
            if (bucket == null || bucket.isEmpty()) {
                continue;
            }
            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(
                    new AssetItem(mediaType, bucket.size() + " file(s)", null, createImageIcon(ICON_MENU_FOLDER)));
            bucket.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            for (File file : bucket) {
                typeNode.add(new DefaultMutableTreeNode(createAssetItem(file, baseDir, mediaType)));
            }
            section.add(typeNode);
        }
        return section;
    }

    private DefaultMutableTreeNode buildLibraryResourcesNode(File baseDir) {
        if (basicEditor == null || basicEditor.getLibraries() == null || basicEditor.getLibraries().isEmpty()) {
            return null;
        }

        DefaultMutableTreeNode librariesNode = new DefaultMutableTreeNode(
                new AssetItem("Libraries", "Bookmarked libraries and their resource files", null, createImageIcon(ICON_MENU_FUNCTIONS)));
        for (Library library : basicEditor.getLibraries()) {
            if (library == null) {
                continue;
            }
            DefaultMutableTreeNode libNode = new DefaultMutableTreeNode(
                    new AssetItem(
                            library.name() == null || library.name().isBlank() ? "(unnamed library)" : library.name(),
                            library.description(),
                            null,
                            createImageIcon(ICON_MENU_BOOKMARKS)));

            java.util.List<File> libraryFiles = new ArrayList<>();
            collectResolvedLibraryAssets(library.getDependencies(), baseDir, libraryFiles);
            collectResolvedLibraryAssets(library.getClassPathObjects(), baseDir, libraryFiles);
            libraryFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            for (File file : libraryFiles) {
                libNode.add(new DefaultMutableTreeNode(createAssetItem(file, baseDir, "Library resource")));
            }

            if (libNode.getChildCount() == 0) {
                libNode.add(new DefaultMutableTreeNode(
                        new AssetItem("No local resources", "Library has no resolvable resource files in the workspace", null, createImageIcon(ICON_MENU_HELP))));
            }
            librariesNode.add(libNode);
        }
        return librariesNode;
    }

    private void collectResolvedLibraryAssets(java.util.List<String> paths, File baseDir, java.util.List<File> out) {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        for (String path : paths) {
            if (path == null || path.isBlank()) {
                continue;
            }
            File resolved = resolveAssetReference(path, baseDir, null);
            if (resolved == null || !resolved.exists()) {
                continue;
            }
            if (resolved.isDirectory()) {
                collectWorkspaceAssets(resolved, 0, 2, out);
            } else {
                out.add(resolved);
            }
        }
    }

    private java.util.List<File> detectLiteralAssets(File baseDir) {
        java.util.LinkedHashSet<File> detected = new java.util.LinkedHashSet<>();
        if (fileManager == null) {
            return new ArrayList<>();
        }

        for (com.basic4gl.desktop.editor.FileEditor editor : fileManager.getFileEditors()) {
            if (editor == null || editor.getEditorPane() == null) {
                continue;
            }
            String text = editor.getEditorPane().getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            File sourceFile = editor.getFile();
            File sourceParent = sourceFile != null ? sourceFile.getAbsoluteFile().getParentFile() : null;
            for (String literal : ExportDialog.extractStringLiterals(text)) {
                if (literal == null || literal.isBlank()) {
                    continue;
                }
                File resolved = resolveAssetReference(literal, baseDir, sourceParent);
                if (resolved != null && resolved.exists() && resolved.isFile()) {
                    detected.add(resolved);
                }
            }
        }
        return new ArrayList<>(detected);
    }

    private java.util.List<File> collectWorkspaceAssets(File directory, int depth, int maxDepth) {
        java.util.List<File> assets = new ArrayList<>();
        collectWorkspaceAssets(directory, depth, maxDepth, assets);
        return assets;
    }

    private void collectWorkspaceAssets(File directory, int depth, int maxDepth, java.util.List<File> out) {
        if (directory == null || !directory.isDirectory() || depth > maxDepth) {
            return;
        }
        if (shouldSkipAssetDirectory(directory)) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File file : files) {
            if (file == null || file.getName().startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                collectWorkspaceAssets(file, depth + 1, maxDepth, out);
            } else if (isKnownAssetFile(file)) {
                out.add(file);
            }
        }
    }

    private boolean shouldSkipAssetDirectory(File directory) {
        String name = directory.getName().toLowerCase(Locale.ROOT);
        return name.equals("build")
                || name.equals("out")
                || name.equals("target")
                || name.equals("bin")
                || name.equals("dist")
                || name.equals("node_modules")
                || name.equals(".gradle")
                || name.equals(".git");
    }

    private AssetItem createAssetItem(File file, File baseDir, String subtitlePrefix) {
        String subtitle = subtitlePrefix;
        if (file != null) {
            String relative = formatRelativePath(file, baseDir);
            if (relative != null && !relative.isBlank()) {
                subtitle = subtitle == null || subtitle.isBlank() ? relative : subtitle + " • " + relative;
            }
        }
        return new AssetItem(file != null ? file.getName() : "(unknown)", subtitle, file, file != null ? fileSystemView.getSystemIcon(file) : createImageIcon(ICON_MENU_FOLDER));
    }

    private File resolveAssetReference(String literal, File baseDir, File sourceParent) {
        if (literal == null || literal.isBlank()) {
            return null;
        }
        String normalized = com.basic4gl.lib.util.FileUtil.separatorsToSystem(literal);
        File candidate = new File(normalized);
        if (candidate.isAbsolute()) {
            return candidate;
        }
        if (baseDir != null) {
            File workspace = new File(baseDir, normalized);
            if (workspace.exists()) {
                return workspace;
            }
        }
        if (sourceParent != null) {
            File sibling = new File(sourceParent, normalized);
            if (sibling.exists()) {
                return sibling;
            }
        }
        return candidate;
    }

    private boolean isKnownAssetFile(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return getMediaTypeLabel(file) != null && !"Other".equals(getMediaTypeLabel(file));
    }

    private String getMediaTypeLabel(File file) {
        if (file == null) {
            return "Other";
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".gif")
                || name.endsWith(".bmp")
                || name.endsWith(".webp")
                || name.endsWith(".ico")) {
            return "Images";
        }
        if (name.endsWith(".wav") || name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".flac")) {
            return "Audio";
        }
        if (name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".webm")) {
            return "Video";
        }
        if (name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".json") || name.endsWith(".xml")
                || name.endsWith(".csv") || name.endsWith(".ini") || name.endsWith(".cfg") || name.endsWith(".properties")) {
            return "Text";
        }
        if (name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx") || name.endsWith(".rtf")) {
            return "Documents";
        }
        return "Other";
    }

    private String formatRelativePath(File file, File baseDir) {
        if (file == null) {
            return "";
        }
        if (baseDir != null) {
            try {
                java.nio.file.Path relative = baseDir.getAbsoluteFile().toPath().normalize().relativize(file.getAbsoluteFile().toPath().normalize());
                String text = relative.toString().replace('\\', '/');
                if (!text.startsWith("..")) {
                    return text;
                }
            } catch (Exception ignored) {
                // Fall back to file name below.
            }
        }
        return file.getName();
    }

    private void refreshRunnableFileControls() {
        if (fileManager == null) {
            return;
        }

        updatingRunTargetCombo = true;
        runTargetCombo.removeAllItems();

        for (FileEditor editor : fileManager.getFileEditors()) {
            runTargetCombo.addItem(editor.getShortFilename());
        }

        int runnableIndex = fileManager.getRunnableFileIndex();
        if (runnableIndex >= 0 && runnableIndex < runTargetCombo.getItemCount()) {
            runTargetCombo.setSelectedIndex(runnableIndex);
        }

        runTargetCombo.setEnabled(runTargetCombo.getItemCount() > 0);
        updatingRunTargetCombo = false;
    }

    private void onRunTargetSelectionChanged() {
        if (updatingRunTargetCombo || fileManager == null) {
            return;
        }
        int index = runTargetCombo.getSelectedIndex();
        if (index >= 0 && index < fileManager.getFileEditors().size()) {
            fileManager.setRunnableFilePath(
                    fileManager.getFileEditors().get(index).getFilePath());
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
    private String collectAllSourceText() {
        if (fileManager == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (com.basic4gl.desktop.editor.FileEditor fe : fileManager.getFileEditors()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(fe.getEditorPane().getText());
        }
        return sb.toString();
    }

    /**
     * Called on the EDT by the {@link SymbolIndexer} callback after each debounce cycle.
     * Replaces all "Program" (user-defined) reference items with the freshly scanned symbols and
     * refreshes the reference panel.
     */
    private void updateProgramSymbols(List<com.basic4gl.desktop.language.IndexedSymbol> symbols) {
        int fingerprint = 1;
        for (com.basic4gl.desktop.language.IndexedSymbol symbol : symbols) {
            fingerprint = 31 * fingerprint + Objects.hash(symbol.kind(), symbol.name(), symbol.signature());
        }
        if (fingerprint == lastProgramSymbolsFingerprint) {
            return;
        }
        lastProgramSymbolsFingerprint = fingerprint;

        // Remove all existing Program-sourced items
        allReferenceItems.removeIf(item -> "Program".equals(item.library));

        // Add newly scanned symbols
        for (com.basic4gl.desktop.language.IndexedSymbol sym : symbols) {
            String details;
            String insertText;
            int caretOffset;
            switch (sym.kind()) {
                case "userfunc" -> {
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(sym.name()) + "</h3>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> User Function"
                            + "<br/><b>Source:</b> Program</p>"
                            + "<p style='margin:0;'>" + escapeHtml(sym.signature()) + "</p>"
                            + "</body></html>";
                    insertText = sym.name() + "()";
                    caretOffset = sym.name().length() + 1;
                }
                case "label" -> {
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(sym.name()) + "</h3>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> Label"
                            + "<br/><b>Usage:</b> <code>gosub " + escapeHtml(sym.name()) + "</code>"
                            + " / <code>goto " + escapeHtml(sym.name()) + "</code></p>"
                            + "</body></html>";
                    insertText = sym.name();
                    caretOffset = sym.name().length();
                }
                case "struc" -> {
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(sym.name()) + "</h3>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> Struct"
                            + "<br/><b>Source:</b> Program</p>"
                            + "<p style='margin:0;'>" + escapeHtml(sym.signature()) + "</p>"
                            + "</body></html>";
                    insertText = sym.name();
                    caretOffset = sym.name().length();
                }
                default -> { // "variable"
                    details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                            + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(sym.name()) + "</h3>"
                            + "<p style='margin:0 0 4px 0;'><b>Type:</b> Variable"
                            + "<br/><b>Source:</b> Program</p>"
                            + "<p style='margin:0;'>" + escapeHtml(sym.signature()) + "</p>"
                            + "</body></html>";
                    insertText = sym.name();
                    caretOffset = sym.name().length();
                }
            }
            allReferenceItems.add(new ReferenceItem(
                    sym.kind(), sym.name(), sym.signature(), "Program", details, insertText, caretOffset));
        }

        allReferenceItems.sort(Comparator.comparing((ReferenceItem item) -> item.name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(item -> item.kind));
        rebuildLibraryFilterOptions();
        filterReferenceItems();
        refreshAssetsLibrary();
    }

    private void populateDocsFromCompiler() {
        if (basicEditor == null || basicEditor.getCompiler() == null) {
            return;
        }
        allReferenceItems.clear();
        allReferenceItems.addAll(buildFunctionReferenceItems(basicEditor.getLanguageService()));
        allReferenceItems.addAll(buildConstantReferenceItems(basicEditor.getLanguageService()));
        allReferenceItems.addAll(buildLabelReferenceItems(basicEditor.getLanguageService()));
        allReferenceItems.addAll(buildVariableReferenceItems(basicEditor.getLanguageService()));
        allReferenceItems.sort(Comparator.comparing((ReferenceItem item) -> item.name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(item -> item.kind));
        rebuildLibraryFilterOptions();
        filterReferenceItems();
    }

    private java.util.List<ReferenceItem> buildFunctionReferenceItems(
            LanguageService comp) {
        java.util.List<ReferenceItem> items = new ArrayList<>();
        for  (FunctionDefinition item : comp.getFunctionDefinitions()) {
            if (item == null) {
                continue;
            }
            StringBuilder argsOnly = new StringBuilder();
            if (item.parameters() != null) {
                for (VariableDefinition arg : item.parameters()) {
                    if (argsOnly.length() > 0) {
                        argsOnly.append(", ");
                    }
                    argsOnly.append(arg.signature());
                }
            }
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                    + "<h3 style='margin:0 0 8px 0;'>"
                    + escapeHtml(item.name())
                    + "</h3><p style='margin:0 0 4px 0;'><b>Type:</b> Function<br/><b>Library:</b> "
                    + escapeHtml(item.packageName())
                    + "</p><p style='margin:0;'>"
                    + escapeHtml(item.signature())
                    + "</p></body></html>";
            String insertText = item.hasBrackets() ? item.name() + "()" : item.name() + " ";
            int caretOffset = item.hasBrackets() ? item.name().length() + 1 : insertText.length();
            if (item.hasBrackets() && argsOnly.length() > 0) {
                insertText = item.name() + "(" + argsOnly + ")";
                caretOffset = item.name().length() + 1;
            }
            items.add(new ReferenceItem(
                    "function", item.name(), item.signature(), item.packageName(), details, insertText, caretOffset));
        }
        return items;
    }

    private java.util.List<ReferenceItem> buildConstantReferenceItems(LanguageService comp) {
        java.util.List<ReferenceItem> items = new ArrayList<>();
        for (VariableDefinition  item : comp.getConstantDefinitions()) {
            if (item == null) {
                continue;
            }
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                    + "<h3 style='margin:0 0 8px 0;'>"
                    + escapeHtml(item.name())
                    + "</h3><p style='margin:0 0 4px 0;'><b>Type:</b> Constant<br/><b>Library:</b> "
                    + escapeHtml(item.packageName())
                    + "</p><p style='margin:0;'>"
                    + escapeHtml(item.signature())
                    + "</p></body></html>";
            items.add(new ReferenceItem("constant", item.name(), item.signature(), item.packageName(), details, item.name(), item.name().length()));
        }

        return items;
    }

    private java.util.List<ReferenceItem> buildLabelReferenceItems(LanguageService comp) {
        java.util.List<ReferenceItem> items = new ArrayList<>();
        for (LabelDefinition label : comp.getLabelDefinitions()) {
            if (label == null) {
                continue;
            }
            String signature = label.signature();
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                    + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(label.name())
                    + "</h3><p style='margin:0 0 4px 0;'><b>Type:</b> Label<br/><b>Usage:</b> "
                    + "<code>" + escapeHtml(label.usage()) + "</code>"
                    + "</p></body></html>";
            items.add(new ReferenceItem(
                    "label", label.name(), signature, "Program", details, label.name(), label.name().length()));
        }
        return items;
    }

    private java.util.List<ReferenceItem> buildVariableReferenceItems(LanguageService comp) {
        java.util.List<ReferenceItem> items = new ArrayList<>();
        for (VariableDefinition variable :
                comp.getVariableDefinitions()) {
            if (variable == null || variable.name() == null || variable.name().isEmpty()) {
                continue;
            }
            String typeStr = variable.type().name();
            String signature = variable.signature();
            String details = "<html><body style='font-family:sans-serif;padding:6px;'>"
                    + "<h3 style='margin:0 0 8px 0;'>" + escapeHtml(variable.name())
                    + "</h3><p style='margin:0 0 4px 0;'><b>Type:</b> Variable<br/><b>Data type:</b> "
                    + escapeHtml(typeStr) + "<br/><b>Source:</b> Program</p></body></html>";
            items.add(new ReferenceItem(
                    "variable", variable.name(), signature, "Program", details, variable.name(), variable.name().length()));
        }
        return items;
    }

    private void rebuildLibraryFilterOptions() {
        String selected = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All libraries");
        Set<String> libraries = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ReferenceItem item : allReferenceItems) {
            if (item.library != null) {
                libraries.add(item.library);
            }
        }

        updatingReferenceFilters = true;
        try {
            referenceLibraryFilter.removeAllItems();
            referenceLibraryFilter.addItem("All libraries");
            for (String library : libraries) {
                referenceLibraryFilter.addItem(library);
            }
            referenceLibraryFilter.setSelectedItem(libraries.contains(selected) ? selected : "All libraries");
        } finally {
            updatingReferenceFilters = false;
        }
        rebuildReferenceFiltersPopup();
        updateReferenceFiltersButtonTooltip();
    }

    private void rebuildReferenceFiltersPopup() {
        referenceFiltersPopup.removeAll();

        JMenu typeMenu = new JMenu("Type");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "All", "All");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Functions", "Functions");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Constants", "Constants");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Labels", "Labels");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Variables", "Variables");
        addReferenceRadioItems(typeMenu, referenceKindFilter, "Structs", "Structs");

        JMenu sourceMenu = new JMenu("Source");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "All sources", "All sources");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "Builtin", "Builtin");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "Libraries", "Libraries");
        addReferenceRadioItems(sourceMenu, referenceSourceFilter, "Program", "Program");

        JMenu libraryMenu = new JMenu("Library");
        for (int i = 0; i < referenceLibraryFilter.getItemCount(); i++) {
            String item = referenceLibraryFilter.getItemAt(i);
            if (item != null) {
                addReferenceRadioItems(libraryMenu, referenceLibraryFilter, item, item);
            }
        }

        JMenuItem resetItem = new JMenuItem("Reset filters");
        resetItem.addActionListener(e -> {
            updatingReferenceFilters = true;
            try {
                referenceKindFilter.setSelectedItem("All");
                referenceSourceFilter.setSelectedItem("All sources");
                referenceLibraryFilter.setSelectedItem("All libraries");
            } finally {
                updatingReferenceFilters = false;
            }
            updateReferenceFiltersButtonTooltip();
            filterReferenceItems();
        });

        referenceFiltersPopup.add(typeMenu);
        referenceFiltersPopup.add(sourceMenu);
        referenceFiltersPopup.add(libraryMenu);
        referenceFiltersPopup.addSeparator();
        referenceFiltersPopup.add(resetItem);
    }

    private void addReferenceRadioItems(JMenu menu, JComboBox<String> combo, String label, String value) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(label, Objects.equals(combo.getSelectedItem(), value));
        item.addActionListener(e -> combo.setSelectedItem(value));
        menu.add(item);
    }

    private void updateReferenceFiltersButtonTooltip() {
        String type = Objects.toString(referenceKindFilter.getSelectedItem(), "All");
        String source = Objects.toString(referenceSourceFilter.getSelectedItem(), "All sources");
        String library = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All libraries");
        referenceFiltersButton.setToolTipText("Type: " + type + " | Source: " + source + " | Library: " + library);
    }

    private void requestFilterReferenceItems() {
        referenceFilterDebounceTimer.restart();
    }

    private void filterReferenceItems() {
        String query = referenceSearchField.getText();
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String selectedKind = Objects.toString(referenceKindFilter.getSelectedItem(), "All");
        String selectedSource = Objects.toString(referenceSourceFilter.getSelectedItem(), "All sources");
        String selectedLibrary = Objects.toString(referenceLibraryFilter.getSelectedItem(), "All libraries");

        ReferenceItem previousSelection = referenceList.getSelectedValue();
        java.util.List<ReferenceItem> matches = new ArrayList<>();
        for (ReferenceItem item : allReferenceItems) {
            boolean kindMatches = "All".equals(selectedKind)
                    || ("Functions".equals(selectedKind)
                            && ("function".equals(item.kind) || "userfunc".equals(item.kind)))
                    || ("Constants".equals(selectedKind) && "constant".equals(item.kind))
                    || ("Labels".equals(selectedKind) && "label".equals(item.kind))
                    || ("Variables".equals(selectedKind) && "variable".equals(item.kind))
                    || ("Structs".equals(selectedKind) && "struc".equals(item.kind));
            boolean sourceMatches = "All sources".equals(selectedSource)
                    || ("Builtin".equals(selectedSource)
                            && item.library != null
                            && "Builtin".equalsIgnoreCase(item.library))
                    || ("Libraries".equals(selectedSource)
                            && item.library != null
                            && !"Builtin".equalsIgnoreCase(item.library)
                            && !"Program".equalsIgnoreCase(item.library))
                    || ("Program".equals(selectedSource)
                            && item.library != null
                            && "Program".equalsIgnoreCase(item.library));
            boolean libraryMatches = "All libraries".equals(selectedLibrary)
                    || (item.library != null && selectedLibrary.equals(item.library));
            if (needle.isEmpty()
                    || item.name.toLowerCase(Locale.ROOT).contains(needle)
                    || item.signature.toLowerCase(Locale.ROOT).contains(needle)
                    || item.kind.toLowerCase(Locale.ROOT).contains(needle)
                    || (item.library != null
                            && item.library.toLowerCase(Locale.ROOT).contains(needle))) {
                if (kindMatches && sourceMatches && libraryMatches) {
                    matches.add(item);
                }
            }
        }

        referenceListModel.clear();
        for (ReferenceItem match : matches) {
            referenceListModel.addElement(match);
        }

        if (!referenceListModel.isEmpty()) {
            if (previousSelection != null && matches.contains(previousSelection)) {
                referenceList.setSelectedValue(previousSelection, true);
            } else {
                referenceList.setSelectedIndex(0);
            }
        } else {
            referenceDetailsPane.setText(REFERENCE_NO_MATCHES_HTML);
            referenceInsertButton.setEnabled(false);
        }
    }

    private void updateReferenceSelectionDetails() {
        ReferenceItem item = referenceList.getSelectedValue();
        if (item == null) {
            referenceDetailsPane.setText(REFERENCE_SELECT_PROMPT_HTML);
            referenceInsertButton.setEnabled(false);
            return;
        }
        referenceDetailsPane.setText(item.details);
        referenceDetailsPane.setCaretPosition(0);
        referenceInsertButton.setEnabled(true);
    }

    private void insertSelectedReference() {
        ReferenceItem item = referenceList.getSelectedValue();
        if (item == null) {
            return;
        }
        int selectedTab = tabControl.getSelectedIndex();
        if (selectedTab < 0 || selectedTab >= fileManager.getFileEditors().size()) {
            return;
        }
        JTextArea editorPane = fileManager.getFileEditors().get(selectedTab).getEditorPane();
        int insertStart = editorPane.getSelectionStart();
        editorPane.replaceSelection(item.insertText);
        editorPane.setCaretPosition(Math.min(
                insertStart + item.caretOffset, editorPane.getDocument().getLength()));
        editorPane.requestFocusInWindow();
    }


    private void openMarkdownInDocsTab(File file) {
        File resolved = file.isAbsolute() ? file : new File(fileManager.getCurrentDirectory(), file.getPath());
        if (!resolved.exists()) {
            resolved = file;
        }
        if (!resolved.exists()) {
            JOptionPane.showMessageDialog(frame, "Markdown file not found: " + file.getPath());
            return;
        }

        String tabTitle = resolved.getName();
        for (int i = 0; i < docsTabs.getTabCount(); i++) {
            if (tabTitle.equals(docsTabs.getTitleAt(i))) {
                docsTabs.setSelectedIndex(i);
                selectRightDocsSection("docs");
                return;
            }
        }

        try {
            String markdown = Files.readString(resolved.toPath(), StandardCharsets.UTF_8);
            JEditorPane pane = new JEditorPane();
            pane.setEditable(false);
            pane.setContentType("text/html");
            pane.setText(markdownToHtml(markdown));
            pane.setCaretPosition(0);

            docsTabs.addTab(tabTitle, new JScrollPane(pane));
            docsTabs.setSelectedIndex(docsTabs.getTabCount() - 1);
            selectRightDocsSection("docs");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Unable to open markdown file: " + ex.getMessage());
        }
    }

    private String markdownToHtml(String markdown) {
        StringBuilder html = new StringBuilder("<html><body style='font-family:sans-serif;'>");
        for (String line : markdown.split("\\R", -1)) {
            String escaped = escapeHtml(line);
            if (escaped.startsWith("### ")) {
                html.append("<h3>").append(escaped.substring(4)).append("</h3>");
            } else if (escaped.startsWith("## ")) {
                html.append("<h2>").append(escaped.substring(3)).append("</h2>");
            } else if (escaped.startsWith("# ")) {
                html.append("<h1>").append(escaped.substring(2)).append("</h1>");
            } else if (escaped.startsWith("- ")) {
                html.append("<p>&bull; ").append(escaped.substring(2)).append("</p>");
            } else if (escaped.isBlank()) {
                html.append("<br/>");
            } else {
                html.append("<p>").append(escaped).append("</p>");
            }
        }
        html.append("</body></html>");
        return html.toString();
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
    public void onNewClick() {
        actionNew();
    }

    @Override
    public void onOpenClick() {
        actionOpen();
    }

    @Override
    public void onOpenClick(File file) {
        actionOpen(file);
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
