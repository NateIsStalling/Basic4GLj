package com.basic4gl.desktop;

import com.basic4gl.desktop.debugger.IDebugger;
import com.basic4gl.desktop.debugger.commands.EvaluateWatchHandler;
import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.desktop.editor.ITabProvider;
import com.basic4gl.desktop.editor.IToggleBreakpointListener;
import com.basic4gl.desktop.editor.IncludeLinkGenerator;
import com.basic4gl.desktop.util.*;
import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.stackframe.UserFuncStackFrame;
import org.fife.ui.rsyntaxtextarea.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static com.basic4gl.desktop.Theme.*;
import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;

/**
 * Created by Nate on 2/24/2015.
 */
public class MainWindow implements
        IEditorPresenter,
        ITabProvider,
        IToggleBreakpointListener {

    private CaretListener TrackCaretPosition = new CaretListener() {
        @Override
        public void caretUpdate(CaretEvent e) {
            JTextArea component = (JTextArea) e.getSource();
            int caretpos = component.getCaretPosition();
            int row = 0;
            int column = 0;
            try {
                row = component.getLineOfOffset(caretpos);
                column = caretpos - component.getLineStartOffset(row);

                mCursorPosLabel.setText((column + 1) + ":" + (row + 1));
            } catch (BadLocationException ex) {
                mCursorPosLabel.setText(0 + ":" + 0);
                ex.printStackTrace();
            }
        }
    };

    // Window
    JFrame mFrame = new JFrame(Application.APPLICATION_NAME);
    JMenuBar mMenuBar = new JMenuBar();
    JToolBar mToolBar = new JToolBar();
    JSplitPane mMainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JSplitPane mDebugPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    JTabbedPane mTabControl = new JTabbedPane();
    JPanel mStatusPanel = new JPanel();

    JMenu mFileMenu = new JMenu("File");
    JMenu mEditMenu = new JMenu("Edit");
    JMenu mViewMenu = new JMenu("View");
    JMenu mDebugMenu = new JMenu("Debug");
    JMenu mAppMenu = new JMenu("Application");
    JMenu mHelpMenu = new JMenu("Help");
    JMenu mBookmarkSubMenu = new JMenu("Bookmarks");
    JMenu mBreakpointSubMenu = new JMenu("Breakpoints");

    // Menu Items
    JMenuItem mNewMenuItem = new JMenuItem("New Program");
    JMenuItem mOpenMenuItem = new JMenuItem("Open Program...");
    JMenuItem mSaveMenuItem = new JMenuItem("Save");
    JMenuItem mSaveAsMenuItem = new JMenuItem("Save As...");
    JMenuItem mExportMenuItem = new JMenuItem("Export...");

    JMenuItem mUndoMenuItem = new JMenuItem("Undo");
    JMenuItem mRedoMenuItem = new JMenuItem("Redo");
    JMenuItem mCutMenuItem = new JMenuItem("Cut");
    JMenuItem mCopyMenuItem = new JMenuItem("Copy");
    JMenuItem mPasteMenuItem = new JMenuItem("Paste");
    JMenuItem mSelectAllMenuItem = new JMenuItem("Select All");

    JMenuItem mNextBookmarkMenuItem = new JMenuItem("Next");
    JMenuItem mPrevBookmarkMenuItem = new JMenuItem("Previous");
    JMenuItem mToggleBookmarkMenuItem = new JMenuItem("Toggle Bookmark");
    JMenuItem mFindReplaceMenuItem = new JMenuItem("Find/Replace...");
    JCheckBoxMenuItem mDebugMenuItem = new JCheckBoxMenuItem("Debug Mode");

    JMenuItem mSettingsMenuItem = new JMenuItem("Project Settings");
    JMenuItem mRunMenuItem = new JMenuItem("Run Program");
    JMenuItem mNextBreakpointMenuItem = new JMenuItem("View Next");
    JMenuItem mPrevBreakpointMenuItem = new JMenuItem("View Previous");
    JMenuItem mToggleBreakpointMenuItem = new JMenuItem("Toggle Breakpoint");
    JMenuItem mPlayPauseMenuItem = new JMenuItem("Play/Pause");
    JMenuItem mStepOverMenuItem = new JMenuItem("Step Over");
    JMenuItem mStepIntoMenuItem = new JMenuItem("Step Into");
    JMenuItem mStepOutOfMenuItem = new JMenuItem("Step Out of");

    JMenuItem mFunctionListMenuItem = new JMenuItem("Function List");
    JMenuItem mAboutMenuItem = new JMenuItem("About");

    // Toolbar Buttons
    JButton mNewButton = new JButton(createImageIcon(ICON_NEW));
    JButton mOpenButton = new JButton(createImageIcon(ICON_OPEN));
    JButton mSaveButton = new JButton(createImageIcon(ICON_SAVE));
    JButton mRunButton = new JButton(createImageIcon(ICON_RUN_APP));

    JToggleButton mDebugButton = new JToggleButton(createImageIcon(ICON_DEBUG));
    JButton mPlayButton = new JButton(createImageIcon(ICON_PLAY));
    JButton mStepOverButton = new JButton(createImageIcon(ICON_STEP_OVER));
    JButton mStepInButton = new JButton(createImageIcon(ICON_STEP_IN));
    JButton mStepOutButton = new JButton(createImageIcon(ICON_STEP_OUT));

    // Labels
    JLabel mCompStatusLabel = new JLabel("");    // Compiler/VM Status
    JLabel mCursorPosLabel = new JLabel("0:0"); // Cursor Position

    // Debugging
    DefaultListModel mWatchListModel = new DefaultListModel();
    JList mWatchListBox = new JList(mWatchListModel);
    JScrollPane mWatchListScrollPane = new JScrollPane(mWatchListBox);
    JPanel mWatchListFrame = new JPanel();
    DefaultListModel mGosubListModel = new DefaultListModel();
    JList mGosubListBox = new JList(mGosubListModel);
    JScrollPane mGosubListScrollPane = new JScrollPane(mGosubListBox);
    JPanel mGosubFrame = new JPanel();


    // Editors
    BasicEditor mEditor;
    FileManager mFileManager;
    IDebugger mDebugger;

    IncludeLinkGenerator mLinkGenerator = new IncludeLinkGenerator(this);



    // Debugging
    private boolean mDebugMode = false;
    private List<String> mWatches = new ArrayList<String>();

    private boolean mDelayScreenSwitch = false;            // Set when stepping. Delays switching to the output window for the first 1000 op-codes.
    // (To prevent excessive screen mode switches when debugging full-screen programs.)
    private String mLine;
    private boolean mDone;



    static String libraryPath;

    public static void main(String[] args) {
        libraryPath = args[0];
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Basic4GLj");

        new MainWindow();
    }

    public MainWindow() {
        // Create and set up the window.
        mFrame.setIconImage(createImageIcon(Application.ICON_LOGO_SMALL).getImage());
        mFrame.setPreferredSize(new Dimension(696, 480));
        mFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        mMenuBar.add(mFileMenu);
        mMenuBar.add(mEditMenu);
        mMenuBar.add(mViewMenu);
        mMenuBar.add(mDebugMenu);
        mMenuBar.add(mAppMenu);
        mMenuBar.add(mHelpMenu);

        mFileMenu.add(mNewMenuItem);
        mFileMenu.add(mOpenMenuItem);
        mFileMenu.add(mSaveMenuItem);
        mFileMenu.add(mSaveAsMenuItem);
        mFileMenu.add(new JSeparator());
        mFileMenu.add(mExportMenuItem);

        mEditMenu.add(mUndoMenuItem);
        mEditMenu.add(mRedoMenuItem);
        mEditMenu.add(new JSeparator());
        mEditMenu.add(mCutMenuItem);
        mEditMenu.add(mCopyMenuItem);
        mEditMenu.add(mPasteMenuItem);
        mEditMenu.add(new JSeparator());
        mEditMenu.add(mSelectAllMenuItem);

        mViewMenu.add(mBookmarkSubMenu);
        mBookmarkSubMenu.add(mNextBookmarkMenuItem);
        mBookmarkSubMenu.add(mPrevBookmarkMenuItem);
        mBookmarkSubMenu.add(mToggleBookmarkMenuItem);

        mDebugMenu.add(mPlayPauseMenuItem);
        mDebugMenu.add(mStepOverMenuItem);
        mDebugMenu.add(mStepIntoMenuItem);
        mDebugMenu.add(mStepOutOfMenuItem);
        mDebugMenu.add(new JSeparator());
        mDebugMenu.add(mBreakpointSubMenu);
        mBreakpointSubMenu.add(mNextBreakpointMenuItem);
        mBreakpointSubMenu.add(mPrevBreakpointMenuItem);
        mBreakpointSubMenu.add(mToggleBreakpointMenuItem);
        mDebugMenu.add(new JSeparator());
        mDebugMenu.add(mDebugMenuItem);

        mAppMenu.add(mRunMenuItem);
        mAppMenu.add(new JSeparator());
        mAppMenu.add(mSettingsMenuItem);

        mHelpMenu.add(mFunctionListMenuItem);
        mHelpMenu.add(new JSeparator());
        mHelpMenu.add(mAboutMenuItem);

        mNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        mNewMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionNew();
            }
        });
        mOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        mOpenMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionOpen();
            }
        });
        mSaveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        mSaveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        mSaveAsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSaveAs();
            }
        });
        mExportMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.SetMode(ApMode.AP_STOPPED);
                if (mFileManager.editorCount() == 0) {
                    JOptionPane.showMessageDialog(mFrame, "Nothing to export", "Cannot export",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Clear source code from parser
                mEditor.mComp.Parser().SourceCode().clear();

                if (!mEditor.LoadProgramIntoCompiler()) {
                    mCompStatusLabel.setText(mEditor.mPreprocessor.getError());
                    return;
                }
                ExportDialog dialog = new ExportDialog(MainWindow.this, mEditor.mComp, mEditor.mPreprocessor, mFileManager.mFileEditors);
                dialog.setLibraries(mEditor.mLibraries, mEditor.mCurrentBuilder);
                dialog.setVisible(true);
                mEditor.mCurrentBuilder = dialog.getCurrentBuilder();
            }
        });
        mUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        mUndoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.undo(i);
            }
        });
        mRedoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        mRedoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.redo(i);
            }
        });
        mCutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        mCutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.cut(i);
            }
        });
        mCopyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        mCopyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.copy(i);
            }
        });
        mPasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        mPasteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.Paste(i);
            }
        });
        mSelectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        mSelectAllMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.SelectAll(i);
            }
        });
        mNextBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        mNextBookmarkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.SelectNextBookmark(i);
            }
        });
        mPrevBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.SHIFT_MASK));
        mPrevBookmarkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.SelectPreviousBookmark(i);
            }
        });

        mPlayPauseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.actionRun();
            }
        });
        mPlayPauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        mPlayPauseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.actionPlayPause();
            }
        });
        mStepOverMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        mStepOverMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mDelayScreenSwitch = true;
                mEditor.actionStep();
            }
        });
        mStepIntoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        mStepIntoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mDelayScreenSwitch = true;
                mEditor.actionStepInto();
            }
        });
        mStepOutOfMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, ActionEvent.SHIFT_MASK));
        mStepOutOfMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mDelayScreenSwitch = true;
                mEditor.actionStepOutOf();
            }
        });

        mToggleBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK));
        mToggleBookmarkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.ToggleBookmark(i);
            }
        });
        mNextBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        mNextBreakpointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.SelectNextBreakpoint(i);
            }
        });
        mPrevBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.SHIFT_MASK));
        mPrevBreakpointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.SelectPreviousBreakpoint(i);
            }
        });
        mToggleBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK));
        mToggleBreakpointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                mFileManager.toggleBreakpoint(i);
            }
        });
        mDebugMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionDebugMode();
            }
        });
        mRunMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.actionRun();
            }
        });
        mSettingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectSettingsDialog dialog = new ProjectSettingsDialog(mFrame);
                dialog.setLibraries(mEditor.mLibraries, mEditor.mCurrentBuilder);
                dialog.setVisible(true);
                mEditor.mCurrentBuilder = dialog.getCurrentBuilder();
            }
        });
        mFunctionListMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReferenceWindow window = new ReferenceWindow(mFrame);
                window.populate(mEditor.mComp);
                window.setVisible(true);
            }
        });
        mAboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog(mFrame);
            }
        });

        //Debugger
        mWatchListFrame.setLayout(new BorderLayout());
        JLabel watchlistLabel = new JLabel("Watchlist");
        watchlistLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        mWatchListFrame.add(watchlistLabel, BorderLayout.NORTH);
        mWatchListFrame.add(mWatchListScrollPane, BorderLayout.CENTER);

        mWatchListBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    // Double-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    EditWatch();
                }
            }
        });

        mWatchListBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    EditWatch();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                    DeleteWatch();
                else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    mWatchListBox.setSelectedIndex(mWatches.size());
                    EditWatch();
                }
            }
        });

        mWatchListBox.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                UpdateWatchHint();
            }
        });

        mGosubFrame.setLayout(new BorderLayout());
        JLabel callstackLabel = new JLabel("Callstack");
        callstackLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        mGosubFrame.add(callstackLabel, BorderLayout.NORTH);
        mGosubFrame.add(mGosubListScrollPane, BorderLayout.CENTER);

        mDebugPane.setLeftComponent(mWatchListFrame);
        mDebugPane.setRightComponent(mGosubFrame);

        //Toolbar
        mToolBar.add(mNewButton);
        mToolBar.add(mOpenButton);
        mToolBar.add(mSaveButton);
        mToolBar.addSeparator();
        mToolBar.add(mRunButton);
        mToolBar.addSeparator();
        mToolBar.add(mDebugButton);
        mToolBar.addSeparator();
        mToolBar.add(mPlayButton);
        mToolBar.add(mStepOverButton);
        mToolBar.add(mStepInButton);
        mToolBar.add(mStepOutButton);

        mNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionNew();
            }
        });
        mOpenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionOpen();
            }
        });
        mSaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        mRunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.actionRun();
            }
        });

        mDebugButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionDebugMode();
            }
        });
        mPlayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.actionPlayPause();
            }
        });
        mStepOverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.actionStep();
            }
        });
        mStepInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.actionStepInto();
            }
        });
        mStepOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mEditor.actionStepOutOf();
            }
        });
        mRunButton.setToolTipText("Run the program!");

        mToolBar.setAlignmentY(1);
        mToolBar.setFloatable(false);

        //Status Panel
        JPanel panelStatusInfo = new JPanel(new BorderLayout());
        JPanel panelStatusCursor = new JPanel();

        mStatusPanel.setLayout(new BoxLayout(mStatusPanel, BoxLayout.LINE_AXIS));

        mStatusPanel.add(panelStatusCursor);
        mStatusPanel.add(new JSeparator(JSeparator.VERTICAL));
        mStatusPanel.add(panelStatusInfo);

        panelStatusCursor.add(mCursorPosLabel, BorderLayout.CENTER);
        panelStatusInfo.add(mCompStatusLabel, BorderLayout.LINE_START);

        mStatusPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
        panelStatusInfo.setBorder(new EmptyBorder(0, 5, 0, 5));
        panelStatusInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        panelStatusCursor.setBorder(new EmptyBorder(0, 5, 0, 5));
        panelStatusCursor.setMaximumSize(new Dimension(96, 24));

        // Tabs
        UIManager.put("TabbedPane.selected", new Color(220, 220, 220));
        UIManager.put("TabbedPane.contentAreaColor", new Color(220, 220, 220));
        UIManager.put("TabbedPane.shadow", Color.LIGHT_GRAY);
        SwingUtilities.updateComponentTreeUI(mTabControl);
        mTabControl.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
            }
        });
        mTabControl.setBackground(Color.LIGHT_GRAY);

        // The following line enables to use scrolling tabs.
        mTabControl.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        mMainPane.setTopComponent(mTabControl);
        mDebugPane.setLeftComponent(mWatchListFrame);
        mDebugPane.setRightComponent(mGosubFrame);

        // Add controls to window
        mFrame.add(mToolBar, BorderLayout.NORTH);
        mFrame.add(mMainPane, BorderLayout.CENTER);
        mFrame.add(mStatusPanel, BorderLayout.SOUTH);
        mFrame.setJMenuBar(mMenuBar);

        mFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                // Stop program running
                if (mEditor.mMode == ApMode.AP_RUNNING || mEditor.mMode == ApMode.AP_PAUSED) {
                    mEditor.SetMode(ApMode.AP_STOPPED);
                    return;
                }

                // Save file before closing
                if (!MultifileCheckSaveChanges())
                    return;

                //TODO Add libraries
                // Library cleanup functions
                //ShutDownTomWindowsBasicLib();

                mFrame.dispose();
                System.exit(0);
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
        mFileManager = new FileManager();
        Preprocessor preprocessor = new Preprocessor(2, new EditorSourceFileServer(mFileManager), new DiskFileServer());
        Debugger debugger = new Debugger(preprocessor.LineNumberMap());
        TomVM vm = new TomVM(debugger);
        TomBasicCompiler comp = new TomBasicCompiler(vm);

        mEditor = new BasicEditor(libraryPath, mFileManager, this, preprocessor, debugger, vm, comp);


        //TODO Confirm this doesn't break if app is ever signed
        //getParent
        mFileManager.mAppDirectory = new File(".").getAbsolutePath();

        if (new File(mFileManager.mAppDirectory, "Programs").exists()) {
            mFileManager.mRunDirectory = mFileManager.mAppDirectory + "\\Programs";
        } else {
            mFileManager.mRunDirectory = mFileManager.mAppDirectory;
        }
        mFileManager.mFileDirectory = mFileManager.mRunDirectory;
        mFileManager.mCurrentDirectory = mFileManager.mFileDirectory;

        // TODO this should be done as a callback
        RefreshActions(mEditor.mMode);
        RefreshDebugDisplays(mEditor.mMode);

        mEditor.InitLibraries();
        ResetProject();

        // Display the window.
        mFrame.pack();
        mFrame.setLocationRelativeTo(null);
        mFrame.setVisible(true);
    }

    public JFrame getFrame() {
        return mFrame;
    }

    private void ResetProject() {
        // Clear out the current project and setup a new basic one with a single
        // source-file.

        // Close existing editors
        mTabControl.removeAll();
        mFileManager.mFileEditors.clear();

        // Create a default tab
        addTab();


        //Display the editor
        mTabControl.setSelectedIndex(0);

    }


    @Override
    public int getFileTabIndex(String filename) {
        return mFileManager.getFileTabIndex(filename);
    }

    @Override
    public int getTabIndex(String filePath) {
        return mFileManager.getTabIndex(filePath);
    }

    @Override
    public void setSelectedTabIndex(int index) {
        mTabControl.setSelectedIndex(index);
    }

    @Override
    public void openTab(String filename) {
        File file = new File(mFileManager.mCurrentDirectory, filename);

        System.out.println("Open tab: " + filename);
        System.out.println("Path: " + file.getAbsolutePath());

        MainWindow.this.addTab(FileEditor.open(file, mFileManager, this, mLinkGenerator));

        mTabControl.setSelectedIndex(mTabControl.getTabCount() - 1);
    }



    void actionNew() {
        if (MultifileCheckSaveChanges()) {

            mFileManager.mRunDirectory = mFileManager.mFileDirectory;
            mFileManager.mCurrentDirectory = mFileManager.mRunDirectory;

            //Clear file editors
            this.mTabControl.removeAll();
            mFileManager.mFileEditors.clear();

            this.addTab();

        }
    }

    void actionOpen() {
        if (MultifileCheckSaveChanges()) {
            mFileManager.mCurrentDirectory = mFileManager.mFileDirectory;
            FileEditor editor = FileEditor.open(mFrame, mFileManager, this, mLinkGenerator);
            if (editor != null) {
                //TODO Check if file should open as new tab or project
                //For now just open as new project
                //So... close all current tabs
                closeAll();


                // Set current directory to main file directory
                // Must be done BEFORE setting the long filename, because the short
                // filename will be calculated based on the current dir.
                mFileManager.mFileDirectory = new File(editor.getFilePath()).getParent();
                mFileManager.mRunDirectory = mFileManager.mFileDirectory;

                mFileManager.mCurrentDirectory = mFileManager.mRunDirectory;

                //Display file
                addTab(editor);
            }
        }
    }

    boolean FileCheckSaveChanges(int index) {

        // Is sub-file modified?
        FileEditor editor = mFileManager.mFileEditors.get(index);
        if (editor.isModified()) {
            int result = JOptionPane.showConfirmDialog(mFrame,
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
        Mutable<String> description = new Mutable<String>("");
        if (mFileManager.MultifileModified(description)) {

            int result = JOptionPane.showConfirmDialog(mFrame,
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
        for (int i = 0; i < mFileManager.mFileEditors.size(); i++)
            if (mFileManager.mFileEditors.get(i).isModified()) {
                mTabControl.setSelectedIndex(i);
                if (!actionSave(i))
                    return false;
            }

        return true;
    }

    boolean actionSave() {
        //Save content of current tab
        if (mFileManager.mFileEditors.isEmpty()
                || this.mTabControl.getTabCount() == 0
                || this.mTabControl.getSelectedIndex() == -1)
            return false;

        int index = mTabControl.getSelectedIndex();
        boolean saved = mFileManager.mFileEditors.get(index).save(false, mFileManager.mCurrentDirectory);
        if (saved) {
            //TODO Check if index of main file
            int main = 0;
            if (index == main) {
                mFileManager.mFileDirectory = new File(mFileManager.mFileEditors.get(index).getFilePath()).getParent();
                mFileManager.mRunDirectory = mFileManager.mFileDirectory;
                mFileManager.mCurrentDirectory = mFileManager.mRunDirectory;
            }
            mTabControl.setTitleAt(index, mFileManager.mFileEditors.get(index).getTitle());
            mTabControl.getTabComponentAt(index).invalidate();
        }
        return saved;
    }

    boolean actionSave(int index) {
        //Save content of current tab
        if (mFileManager.mFileEditors.isEmpty()
                || this.mTabControl.getTabCount() == 0
                || this.mTabControl.getSelectedIndex() == -1)
            return false;

        boolean saved = mFileManager.mFileEditors.get(index).save(false, mFileManager.mCurrentDirectory);
        if (saved) {
            //TODO Check if main file
            int main = 0;
            if (index == main) {
                mFileManager.mFileDirectory = new File(mFileManager.mFileEditors.get(index).getFilePath()).getParent();
                mFileManager.mRunDirectory = mFileManager.mFileDirectory;
                mFileManager.mCurrentDirectory = mFileManager.mRunDirectory;
            }
            mTabControl.setTitleAt(index, mFileManager.mFileEditors.get(index).getTitle());
            mTabControl.getTabComponentAt(index).invalidate();
        }
        return saved;
    }

    void actionSaveAs() {
        //Save content of current tab as new file
        if (mFileManager.mFileEditors.isEmpty()
                || this.mTabControl.getTabCount() == 0
                || this.mTabControl.getSelectedIndex() == -1)
            return;
        int index = mTabControl.getSelectedIndex();

        mFileManager.mCurrentDirectory = mFileManager.mFileDirectory;

        if (mFileManager.mFileEditors.get(index).save(true, mFileManager.mCurrentDirectory)) {
            //TODO get current main file
            int main = 0;
            if (index == main) {
                mFileManager.mFileDirectory = new File(mFileManager.mFileEditors.get(index).getFilePath()).getParent();
                mFileManager.mRunDirectory = mFileManager.mFileDirectory;
                mFileManager.mCurrentDirectory = mFileManager.mRunDirectory;
            }

        } else {
            //Restore Current directory
            mFileManager.mCurrentDirectory = mFileManager.mRunDirectory;
        }
        mTabControl.setTitleAt(index, mFileManager.mFileEditors.get(index).getTitle());
        mTabControl.getTabComponentAt(index).invalidate();
    }

    private void actionDebugMode() {
        // Toggle debug mode
        mDebugMode = !mDebugMode;
        mDebugMenuItem.setSelected(mDebugMode);
        mDebugButton.setSelected(mDebugMode);

        RefreshDebugDisplays(mEditor.mMode);
    }

    public void closeAll() {
        for (int i = mTabControl.getTabCount() - 1; i >= 0; i--) {
            closeTab(i);
        }

        // Reset default run directory to programs folder
        mFileManager.mRunDirectory = mFileManager.mAppDirectory + "\\Programs";

        // Clear DLLs, breakpoints, bookmarks etc
        //m_dlls.Clear();
        mEditor.mDebugger.ClearUserBreakPts();

        // Refresh UI
        RefreshActions(mEditor.mMode);
    }

    public void closeTab(int index) {
        mTabControl.remove(index);
        mFileManager.mFileEditors.remove(index);
    }

    public void addTab() {
        final FileEditor editor = new FileEditor(mFileManager, this, mLinkGenerator);
        addTab(editor);
    }

    public void addTab(FileEditor editor) {
        int count = mFileManager.editorCount();
        mFileManager.mFileEditors.add(editor);
        mTabControl.addTab(editor.getTitle(), editor.pane);

        final FileEditor edit = editor;
        edit.editorPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                int index = getTabIndex(edit.getFilePath());
                edit.setModified();
                mTabControl.setTitleAt(index, edit.getTitle());
                mTabControl.getTabComponentAt(index).invalidate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                int index = getTabIndex(edit.getFilePath());
                edit.setModified();
                mTabControl.setTitleAt(index, edit.getTitle());
                mTabControl.getTabComponentAt(index).invalidate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                int index = getTabIndex(edit.getFilePath());
                edit.setModified();
                mTabControl.setTitleAt(index, edit.getTitle());
                mTabControl.getTabComponentAt(index).invalidate();
            }
        });
        final ButtonTabComponent tabComponent = new ButtonTabComponent(mTabControl);
        tabComponent.getButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.indexOfTabComponent(tabComponent);
                if (i != -1) {
                    if (FileCheckSaveChanges(i)) {
                        //Clear file's breakpoints
                        FileEditor editor = mFileManager.mFileEditors.get(i);
                        List<Integer> breakpoints = editor.getBreakpoints();
                        String file = editor.getFilePath();

                        for (Integer line : breakpoints) {
                            mEditor.toggleBreakpt(file, line);
                        }

                        //Remove tab
                        mTabControl.remove(i);
                        mFileManager.mFileEditors.remove(i);

                        //Refresh controls if no files open
                        if (mFileManager.editorCount() == 0) {
                            mEditor.SetMode(ApMode.AP_CLOSED);
                        }
                    }

                }
            }
        });
        mTabControl.setTabComponentAt(mTabControl.getTabCount() - 1, tabComponent);

        //Allow user to see cursor position
        editor.editorPane.addCaretListener(TrackCaretPosition);
        mCursorPosLabel.setText(0 + ":" + 0); //Reset label

        //Set tab as read-only if App is running or paused
        boolean readOnly = mEditor.mMode != ApMode.AP_STOPPED;
        editor.editorPane.setEditable(!readOnly);

        //TODO set syntax highlight colors

        //Refresh interface if there was previously no tabs open
        if (count == 0) {
            mEditor.SetMode(ApMode.AP_STOPPED);
        }
    }

    @Override
    public void PlaceCursorAtProcessed(final int row, int col) {

        // Place cursor at position corresponding to row, col in post-processed file.
        // Find corresponding source position
        Mutable<String> filename = new Mutable<String>("");
        Mutable<Integer> fileRow = new Mutable<Integer>(0);
        mEditor.mPreprocessor.LineNumberMap().SourceFromMain(filename, fileRow, row);

        final String file = filename.get();
        final int r = fileRow.get();
        final int c = col;

        // Find (and show) corresponding editor frame
        if (r >= 0) {
            int index = getTabIndex(file);
            if (index == -1) {
                //Attempt to open tab
                addTab(FileEditor.open(new File(file), mFileManager, this, mLinkGenerator));
                index = mTabControl.getTabCount() - 1;
                //return;
            }

            mTabControl.setSelectedIndex(index);

            final JTextArea frame = mFileManager.mFileEditors.get(getTabIndex(file)).editorPane;

            // Set focus
            frame.grabFocus();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int col = c;
                    // Place cursor
                    if (r >= 0) {
                        try {
                            JTextArea textArea = mFileManager.mFileEditors.get(mTabControl.getSelectedIndex()).editorPane;
                            int offset = textArea.getLineStartOffset(r);

                            //Reduce column position if it would place the cursor at the next line
                            if (textArea.getLineCount() > r + 1
                                    && offset + col == textArea.getLineStartOffset(r + 1)) {
                                offset = textArea.getLineStartOffset(r + 1) - 1;
                            } else {
                                offset += col;
                            }

                            frame.setCaretPosition(offset);
                        } catch (Exception ex) {
                            //Do nothing
                        }
                    }
                }
            });

        }

    }


    @Override
    public void onPause() {

        // Place editor into paused mode
        mEditor.SetMode(ApMode.AP_PAUSED);
        RefreshActions(mEditor.mMode);

        // Place editor into debug mode
        mDebugMode = true;
        mDebugMenuItem.setSelected(true);
        mDebugButton.setSelected(true);
        RefreshDebugDisplays(mEditor.mMode);

        //TODO Add VMViewer
        //VMView().SetVMIsRunning(false);
    }

    @Override
    public void onApplicationClosing() {
        // Get focus back
        mFrame.requestFocus();
        if (!mFileManager.mFileEditors.isEmpty() && mTabControl.getTabCount() != 0) {
            //TODO set tab to file that error occurred in
            mFileManager.mFileEditors.get(mTabControl.getSelectedIndex()).editorPane.grabFocus();
        }
    }

    @Override
    public void setCompilerStatus(String error) {
        mCompStatusLabel.setText(error);
    }


    @Override
    public void onModeChanged(ApMode mode, String statusMsg){
        if (mode == ApMode.AP_CLOSED) {
            mCopyMenuItem.setEnabled(true);
            mSelectAllMenuItem.setEnabled(true);

            mStepOverButton.setEnabled(true);
            mStepInButton.setEnabled(true);
            mStepOutButton.setEnabled(true);
            mStepOverMenuItem.setEnabled(true);
            mStepIntoMenuItem.setEnabled(true);
            mStepOutOfMenuItem.setEnabled(true);
            mPlayButton.setEnabled(true);
            mPlayPauseMenuItem.setEnabled(true);
            mRunMenuItem.setEnabled(true);
            mRunButton.setEnabled(true);

            mSaveAsMenuItem.setEnabled(true);
            mSaveMenuItem.setEnabled(true);
            mSaveButton.setEnabled(true);

            mBreakpointSubMenu.setEnabled(true);
            mBookmarkSubMenu.setEnabled(true);
        }

        // Update UI
        RefreshActions(mEditor.mMode);
        RefreshDebugDisplays(mEditor.mMode);
        mCompStatusLabel.setText(statusMsg);

        // Notify virtual machine view
        //TODO Implement VM Viewer
        //VMView().SetVMIsRunning(mode == ApMode.AP_RUNNING);
    }


    @Override
    public void RefreshActions(ApMode mode) {
        //TODO get main file index
        int main = 0;

        // Enable/disable actions to reflect state
        switch (mode) {
            case AP_CLOSED:
                mSettingsMenuItem.setEnabled(false);
                mExportMenuItem.setEnabled(false);

                mOpenMenuItem.setEnabled(false);
                mOpenButton.setEnabled(false);

                mCutMenuItem.setEnabled(false);
                mPasteMenuItem.setEnabled(false);
                mUndoMenuItem.setEnabled(false);
                mRedoMenuItem.setEnabled(false);

                mRunMenuItem.setText("Run Program");
                mRunButton.setIcon(createImageIcon(ICON_RUN_APP));

                mCopyMenuItem.setEnabled(false);
                mSelectAllMenuItem.setEnabled(false);

                mStepOverButton.setEnabled(false);
                mStepInButton.setEnabled(false);
                mStepOutButton.setEnabled(false);
                mStepOverMenuItem.setEnabled(false);
                mStepIntoMenuItem.setEnabled(false);
                mStepOutOfMenuItem.setEnabled(false);
                mPlayButton.setEnabled(false);
                mPlayPauseMenuItem.setEnabled(false);
                mRunMenuItem.setEnabled(false);
                mRunButton.setEnabled(false);

                mSaveAsMenuItem.setEnabled(false);
                mSaveMenuItem.setEnabled(false);
                mSaveButton.setEnabled(false);

                mBreakpointSubMenu.setEnabled(false);
                mBookmarkSubMenu.setEnabled(false);

                mCompStatusLabel.setText("");
                break;
            case AP_STOPPED:
                for (int i = 0; i < mTabControl.getTabCount(); i++) {
                    ((ButtonTabComponent) mTabControl.getTabComponentAt(i)).getButton().setEnabled(true);
                }

                mSettingsMenuItem.setEnabled(true);
                mExportMenuItem.setEnabled(true);

                mNewMenuItem.setEnabled(true);
                mOpenMenuItem.setEnabled(true);
                mNewButton.setEnabled(true);
                mOpenButton.setEnabled(true);

                mCutMenuItem.setEnabled(true);
                mPasteMenuItem.setEnabled(true);
                mUndoMenuItem.setEnabled(true);
                mRedoMenuItem.setEnabled(true);

                mFileManager.SetReadOnly(false);
                mRunMenuItem.setText("Run Program");
                mRunButton.setIcon(createImageIcon(ICON_RUN_APP));
                break;

            case AP_RUNNING:
                if (main > -1 && main < mTabControl.getTabCount())
                    ((ButtonTabComponent) mTabControl.getTabComponentAt(main)).getButton().setEnabled(false);

                mSettingsMenuItem.setEnabled(false);
                mExportMenuItem.setEnabled(false);

                mNewMenuItem.setEnabled(false);
                mOpenMenuItem.setEnabled(false);
                mNewButton.setEnabled(false);
                mOpenButton.setEnabled(false);

                mCutMenuItem.setEnabled(false);
                mPasteMenuItem.setEnabled(false);
                mUndoMenuItem.setEnabled(false);
                mRedoMenuItem.setEnabled(false);

                mFileManager.SetReadOnly(true);
                mRunMenuItem.setText("Stop Program");
                mRunButton.setIcon(createImageIcon(ICON_STOP_APP));
                break;

            case AP_PAUSED:
                if (main > -1 && main < mTabControl.getTabCount()) {
                    ((ButtonTabComponent) mTabControl.getTabComponentAt(main)).getButton().setEnabled(false);
                }

                mSettingsMenuItem.setEnabled(false);
                mExportMenuItem.setEnabled(false);

                mNewMenuItem.setEnabled(false);
                mOpenMenuItem.setEnabled(false);
                mNewButton.setEnabled(false);
                mOpenButton.setEnabled(false);

                mCutMenuItem.setEnabled(false);
                mPasteMenuItem.setEnabled(false);
                mUndoMenuItem.setEnabled(false);
                mRedoMenuItem.setEnabled(false);

                mFileManager.SetReadOnly(true);
                mRunMenuItem.setText("Stop Program");
                mRunButton.setIcon(createImageIcon(ICON_STOP_APP));
                break;
        }
    }

    @Override
    public void RefreshDebugDisplays(ApMode mode) {

        // Show/hide debug controls
        mPlayButton.setVisible(mDebugMode);
        mStepOverButton.setVisible(mDebugMode);
        mStepInButton.setVisible(mDebugMode);
        mStepOutButton.setVisible(mDebugMode);

        //TODO Show/hide debug pane
        if (mDebugMode) {
            mMainPane.setResizeWeight(0.7);
            //mDebugPane.setEnabled(true);
            mMainPane.setEnabled(true);
            mMainPane.setBottomComponent(mDebugPane);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    mDebugPane.setDividerLocation(0.7);
                }
            });
        } else {
            mMainPane.remove(mDebugPane);
            //mDebugPane.setEnabled(false);
            mMainPane.setEnabled(false);
        }

        if (mode != ApMode.AP_CLOSED) {
            mPlayButton.setIcon(mode == ApMode.AP_RUNNING ? createImageIcon(ICON_PAUSE) : createImageIcon(ICON_PLAY));
            mPlayButton.setEnabled(true);
            mStepOverButton.setEnabled(mode != ApMode.AP_RUNNING);
            mStepInButton.setEnabled(mode != ApMode.AP_RUNNING);
            mStepOutButton.setEnabled(mode == ApMode.AP_PAUSED && (mEditor.mVM.UserCallStack().size() > 0));
        }
        if (!mDebugMode) {
            return;
        }

        // Clear debug controls
        mWatchListModel.clear();
        mGosubListModel.clear();

        for (String watch : mWatches) {

            mWatchListModel.addElement(watch + ": " + mEditor.evaluateWatch(watch, true));
        }
        mWatchListModel.addElement(" ");              // Last line is blank, and can be clicked on to add new watch

        if (mEditor.mMode != ApMode.AP_PAUSED) {
            return;
        }

        // Update call stack
        mGosubListModel.addElement("IP");
        Vector<UserFuncStackFrame> callStack = mEditor.mVM.UserCallStack();
        for (int i2 = 0; i2 < callStack.size(); i2++) {
            UserFuncStackFrame frame = callStack.get(callStack.size() - i2 - 1);

            // User functions have positive indices
            if (frame.userFuncIndex >= 0) {
                mGosubListModel.addElement(mEditor.mComp.GetUserFunctionName(frame.userFuncIndex) + "()");

                // Otherwise must be a gosub
            } else {
                mGosubListModel.addElement("gosub " + mEditor.mComp.DescribeStackCall(frame.returnAddr));
            }
        }

    }

    private void EditWatch() {
        String newWatch, oldWatch;

        // Find watch
        int index = mWatchListBox.getSelectedIndex();
        int saveIndex = index;

        // Extract watch text
        if (index > -1 && index < mWatches.size()) {
            oldWatch = mWatches.get(index);
        } else {
            oldWatch = "";
        }

        // Prompt for new text
        newWatch = (String) JOptionPane.showInputDialog(mFrame, "Enter variable/expression:", "Watch variable",
                JOptionPane.PLAIN_MESSAGE, null, null, oldWatch);

        // Update/insert/delete watch
        if (newWatch != null) {
            newWatch = newWatch.trim();
            if (newWatch.equals("")) {
                //User entered an empty value
                if (index > -1 && index < mWatches.size()) {
                    mWatches.remove(index);
                }
            } else {
                if (index > -1 && index < mWatches.size()) {
                    mWatches.set(index, newWatch);
                } else {
                    mWatches.add(newWatch);
                }
            }
        }
        RefreshDebugDisplays(mEditor.mMode);
        mWatchListBox.setSelectedIndex(saveIndex);
        UpdateWatchHint();
    }

    void DeleteWatch() {

        // Find watch
        int index = mWatchListBox.getSelectedIndex();
        int saveIndex = index;

        // Delete watch
        if (index > -1 && index < mWatches.size()) {
            mWatches.remove(index);
        }

        RefreshDebugDisplays(mEditor.mMode);
        mWatchListBox.setSelectedIndex(saveIndex);
        UpdateWatchHint();
    }

    private void UpdateWatchHint() {
        int index = mWatchListBox.getSelectedIndex();
        if (index > -1 && index < mWatches.size()) {
            mWatchListBox.setToolTipText((String) mWatchListModel.get(index));
        } else {
            mWatchListBox.setToolTipText("");
        }
    }

    @Override
    public void onToggleBreakpoint(String filePath, int line) {
        mEditor.toggleBreakpt(filePath, line);
    }
}