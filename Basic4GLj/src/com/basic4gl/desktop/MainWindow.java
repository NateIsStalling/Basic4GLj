package com.basic4gl.desktop;

import com.basic4gl.desktop.util.DiskFileServer;
import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.TomBasicCompiler.LanguageSyntax;
import com.basic4gl.desktop.util.EditorSourceFile;
import com.basic4gl.desktop.util.EditorSourceFileServer;
import com.basic4gl.desktop.util.MainEditor;
import com.basic4gl.lib.desktopgl.BuilderDesktopGL;
import com.basic4gl.lib.desktopgl.GLTextGridWindow;
import com.basic4gl.lib.util.*;
import com.basic4gl.util.Mutable;
import com.basic4gl.vm.Debugger;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.VMState;
import com.basic4gl.vm.stackframe.UserFuncStackFrame;
import com.basic4gl.vm.types.OpCode;
import com.basic4gl.vm.types.ValType;
import org.fife.ui.rsyntaxtextarea.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by Nate on 2/24/2015.
 */
public class MainWindow implements MainEditor {

    //Window Constants
    public static final String APPLICATION_NAME = "Basic4GLj";
    public static final String APPLICATION_VERSION = "Alpha 0.3.0";
    public static final String APPLICATION_BUILD_DATE = "11/15/2015";
    public static final String APPLICATION_COPYRIGHT = "(c) 2015, Nathaniel Nielsen";
    public static final String APPLICATION_DESCRIPTION = "Basic4GL for Java";
    public static final String APPLICATION_WEBSITE = "www.stallingsoftware.com";
    public static final String APPLICATION_CONTACT = "support@stallingsoftware.com";

    public static final String ICON_LOGO_SMALL = "images/logox32.png";
    public static final String ICON_LOGO_LARGE = "images/logox128.png";

    private static final String ICON_RUN_APP = "images/icon_run.png";
    private static final String ICON_STOP_APP = "images/icon_stop.png";
    private static final String ICON_NEW = "images/icon_new.png";
    private static final String ICON_OPEN = "images/icon_open.png";
    private static final String ICON_SAVE = "images/icon_save.png";
    private static final String ICON_DEBUG = "images/icon_debug.png";
    private static final String ICON_PLAY = "images/icon_play.png";
    private static final String ICON_PAUSE = "images/icon_pause.png";
    private static final String ICON_STEP_OVER = "images/icon_step_over.png";
    private static final String ICON_STEP_IN = "images/icon_step_in.png";
    private static final String ICON_STEP_OUT = "images/icon_step_out.png";


    static final int GB_STEPS_UNTIL_REFRESH = 1000;
    static final String EOL = "\r\n";

    enum ApMode {
        AP_CLOSED, AP_STOPPED, AP_PAUSED, AP_RUNNING
    }

    // Window
    JFrame mFrame           = new JFrame(APPLICATION_NAME);
    JMenuBar mMenuBar       = new JMenuBar();
    JToolBar mToolBar       = new JToolBar();
    JSplitPane mMainPane    = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    JSplitPane mDebugPane   = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    JTabbedPane mTabControl = new JTabbedPane();
    JPanel mStatusPanel     = new JPanel();

    JMenu mFileMenu             = new JMenu("File");
    JMenu mEditMenu             = new JMenu("Edit");
    JMenu mViewMenu             = new JMenu("View");
    JMenu mDebugMenu            = new JMenu("Debug");
    JMenu mAppMenu              = new JMenu("Application");
    JMenu mHelpMenu             = new JMenu("Help");
    JMenu mBookmarkSubMenu      = new JMenu("Bookmarks");
    JMenu mBreakpointSubMenu    = new JMenu("Breakpoints");

    // Menu Items
    JMenuItem mNewMenuItem    = new JMenuItem("New Program");
    JMenuItem mOpenMenuItem   = new JMenuItem("Open Program...");
    JMenuItem mSaveMenuItem   = new JMenuItem("Save");
    JMenuItem mSaveAsMenuItem = new JMenuItem("Save As...");
    JMenuItem mExportMenuItem = new JMenuItem("Export...");

    JMenuItem mUndoMenuItem       = new JMenuItem("Undo");
    JMenuItem mRedoMenuItem       = new JMenuItem("Redo");
    JMenuItem mCutMenuItem        = new JMenuItem("Cut");
    JMenuItem mCopyMenuItem       = new JMenuItem("Copy");
    JMenuItem mPasteMenuItem      = new JMenuItem("Paste");
    JMenuItem mSelectAllMenuItem  = new JMenuItem("Select All");

    JMenuItem mNextBookmarkMenuItem   = new JMenuItem("Next");
    JMenuItem mPrevBookmarkMenuItem   = new JMenuItem("Previous");
    JMenuItem mToggleBookmarkMenuItem = new JMenuItem("Toggle Bookmark");
    JMenuItem mFindReplaceMenuItem    = new JMenuItem("Find/Replace...");
    JCheckBoxMenuItem mDebugMenuItem  = new JCheckBoxMenuItem("Debug Mode");

    JMenuItem mSettingsMenuItem         = new JMenuItem("Project Settings");
    JMenuItem mRunMenuItem              = new JMenuItem("Run Program");
    JMenuItem mNextBreakpointMenuItem   = new JMenuItem("View Next");
    JMenuItem mPrevBreakpointMenuItem   = new JMenuItem("View Previous");
    JMenuItem mToggleBreakpointMenuItem = new JMenuItem("Toggle Breakpoint");
    JMenuItem mPlayPauseMenuItem        = new JMenuItem("Play/Pause");
    JMenuItem mStepOverMenuItem        = new JMenuItem("Step Over");
    JMenuItem mStepIntoMenuItem        = new JMenuItem("Step Into");
    JMenuItem mStepOutOfMenuItem        = new JMenuItem("Step Out of");

    JMenuItem mFunctionListMenuItem   = new JMenuItem("Function List");
    JMenuItem mAboutMenuItem          = new JMenuItem("About");

    // Toolbar Buttons
    JButton mNewButton      = new JButton(createImageIcon(ICON_NEW));
    JButton mOpenButton     = new JButton(createImageIcon(ICON_OPEN));
    JButton mSaveButton     = new JButton(createImageIcon(ICON_SAVE));
    JButton mRunButton      = new JButton(createImageIcon(ICON_RUN_APP));

    JToggleButton mDebugButton  = new JToggleButton(createImageIcon(ICON_DEBUG));
    JButton mPlayButton         = new JButton(createImageIcon(ICON_PLAY));
    JButton mStepOverButton     = new JButton(createImageIcon(ICON_STEP_OVER));
    JButton mStepInButton       = new JButton(createImageIcon(ICON_STEP_IN));
    JButton mStepOutButton      = new JButton(createImageIcon(ICON_STEP_OUT));

    // Labels
    JLabel mCompStatusLabel    = new JLabel("");    // Compiler/VM Status
    JLabel mCursorPosLabel     = new JLabel("0:0"); // Cursor Position

    // Debugging
    DefaultListModel mWatchListModel    = new DefaultListModel();
    JList mWatchListBox                 = new JList(mWatchListModel);
    JScrollPane mWatchListScrollPane    = new JScrollPane(mWatchListBox);
    JPanel mWatchListFrame              = new JPanel();
    DefaultListModel mGosubListModel    = new DefaultListModel();
    JList  mGosubListBox                = new JList(mGosubListModel);
    JScrollPane mGosubListScrollPane    = new JScrollPane(mGosubListBox);
    JPanel mGosubFrame                  = new JPanel();

    // Virtual machine and compiler
    private TomVM               mVM;		// Virtual machine
    private TomBasicCompiler    mComp;      // Compiler
    //private FileOpener          mFiles;
    private CallbackMessage mMessage;

    // Preprocessor
    private Preprocessor mPreprocessor;

    // Debugger
    private Debugger mDebugger;

    // State
    LanguageSyntax  mLanguageSyntax     = LanguageSyntax.LS_BASIC4GL;

    // Editors
    Vector<FileEditor> mFileEditors     = new Vector<FileEditor>();
    IncludeLinkGenerator mLinkGenerator = new IncludeLinkGenerator();

    // Editor state
    ApMode          mMode   = ApMode.AP_STOPPED;
    private String  mAppDirectory,  // Application directory (where basic4gl.exe is)
            mFileDirectory, // File I/O in this directory
            mRunDirectory;  // Basic4GL program are run in this directory

    private String mCurrentDirectory;   //Current working directory
    // Debugging
    private boolean         mDebugMode  = false;
    private List<String>  	mWatches    = new ArrayList<String>();

    private boolean         mDelayScreenSwitch  = false;            // Set when stepping. Delays switching to the output window for the first 1000 op-codes.
    // (To prevent excessive screen mode switches when debugging full-screen programs.)
    private String mLine;
    private boolean mDone;

    //Libraries
    private List<Library> mLibraries    = new ArrayList<Library>();
    private List<Integer> mBuilders     = new ArrayList<Integer>();   //Indexes of libraries that can be launch targets
    private int mCurrentBuilder          = -1;                  //Index of mTarget in mTargets
    private Builder mBuilder;                                   //Build target for user's code

    public static void main(String[] args) {
        new MainWindow();
    }

    public MainWindow(){

        // Create and set up the window.
        mFrame.setIconImage(createImageIcon(ICON_LOGO_SMALL).getImage());
        mFrame.setPreferredSize(new Dimension(696, 480));
        mFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE );

        mMenuBar.add(mFileMenu);
        mMenuBar.add(mEditMenu);
        mMenuBar.add(mViewMenu);
        mMenuBar.add(mDebugMenu);
        mMenuBar.add(mAppMenu);
        mMenuBar.add(mHelpMenu);

        mFileMenu.add(mNewMenuItem); mFileMenu.add(mOpenMenuItem); mFileMenu.add(mSaveMenuItem);
        mFileMenu.add(mSaveAsMenuItem); mFileMenu.add(new JSeparator()); mFileMenu.add(mExportMenuItem);

        mEditMenu.add(mUndoMenuItem); mEditMenu.add(mRedoMenuItem); mEditMenu.add(new JSeparator());
        mEditMenu.add(mCutMenuItem); mEditMenu.add(mCopyMenuItem); mEditMenu.add(mPasteMenuItem);
        mEditMenu.add(new JSeparator()); mEditMenu.add(mSelectAllMenuItem);

        mViewMenu.add(mBookmarkSubMenu);
        mBookmarkSubMenu.add(mNextBookmarkMenuItem); mBookmarkSubMenu.add(mPrevBookmarkMenuItem);
        mBookmarkSubMenu.add(mToggleBookmarkMenuItem);

        mDebugMenu.add(mPlayPauseMenuItem); mDebugMenu.add(mStepOverMenuItem);
        mDebugMenu.add(mStepIntoMenuItem); mDebugMenu.add(mStepOutOfMenuItem);
        mDebugMenu.add(new JSeparator());
        mDebugMenu.add(mBreakpointSubMenu);
        mBreakpointSubMenu.add(mNextBreakpointMenuItem);
        mBreakpointSubMenu.add(mPrevBreakpointMenuItem);
        mBreakpointSubMenu.add(mToggleBreakpointMenuItem);
        mDebugMenu.add(new JSeparator());
        mDebugMenu.add(mDebugMenuItem);

        mAppMenu.add(mRunMenuItem);
        mAppMenu.add(new JSeparator()); mAppMenu.add(mSettingsMenuItem);

        mHelpMenu.add(mFunctionListMenuItem); mHelpMenu.add(new JSeparator());
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
            public void actionPerformed(ActionEvent e) { actionSave();}
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
                SetMode(ApMode.AP_STOPPED);
                if (mFileEditors.size() == 0){
                    JOptionPane.showMessageDialog(mFrame, "Nothing to export", "Cannot export",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Clear source code from parser
                mComp.Parser().SourceCode().clear();

                if (!LoadProgramIntoCompiler()) {
                    mCompStatusLabel.setText(mPreprocessor.getError());
                    return;
                }
                ExportDialog dialog = new ExportDialog(MainWindow.this, mComp, mPreprocessor, mFileEditors);
                dialog.setLibraries(mLibraries, mCurrentBuilder);
                dialog.setVisible(true);
                mCurrentBuilder = dialog.getCurrentBuilder();
            }
        });
        mUndoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        mUndoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    if (mFileEditors.get(i).editorPane.canUndo())
                        mFileEditors.get(i).editorPane.undoLastAction();
            }
        });
        mRedoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        mRedoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    if (mFileEditors.get(i).editorPane.canRedo())
                        mFileEditors.get(i).editorPane.redoLastAction();
            }
        });
        mCutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        mCutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    mFileEditors.get(i).editorPane.cut();
            }
        });
        mCopyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        mCopyMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    mFileEditors.get(i).editorPane.copy();
            }
        });
        mPasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        mPasteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    mFileEditors.get(i).editorPane.paste();
            }
        });
        mSelectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        mSelectAllMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    mFileEditors.get(i).editorPane.selectAll();
            }
        });
        mNextBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        mNextBookmarkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    mFileEditors.get(i).gotoNextBookmark(true);
                }
            }
        });
        mPrevBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.SHIFT_MASK));
        mPrevBookmarkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    mFileEditors.get(i).gotoNextBookmark(false);
                }
            }
        });

        mPlayPauseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { actionRun();}
        });
        mPlayPauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        mPlayPauseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { actionPlayPause();}
        });
        mStepOverMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        mStepOverMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { actionStep();}
        });
        mStepIntoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        mStepIntoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { actionStepInto();}
        });
        mStepOutOfMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, ActionEvent.SHIFT_MASK));
        mStepOutOfMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { actionStepOutOf();}
        });

        mToggleBookmarkMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK));
        mToggleBookmarkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    mFileEditors.get(i).toggleBookmark();
                }
            }
        });
        mNextBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        mNextBreakpointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    mFileEditors.get(i).gotoNextBreakpoint(true);
                }
            }
        });
        mPrevBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.SHIFT_MASK));
        mPrevBreakpointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    mFileEditors.get(i).gotoNextBreakpoint(false);
                }
            }
        });
        mToggleBreakpointMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK));
        mToggleBreakpointMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    mFileEditors.get(i).toggleBreakpoint();
                }
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
                actionRun();
            }
        });
        mSettingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectSettingsDialog dialog = new ProjectSettingsDialog(mFrame);
                dialog.setLibraries(mLibraries, mCurrentBuilder);
                dialog.setVisible(true);
                mCurrentBuilder = dialog.getCurrentBuilder();
            }
        });
        mFunctionListMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReferenceWindow window = new ReferenceWindow(mFrame);
                window.populate(mComp);
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
        watchlistLabel.setBorder(new EmptyBorder(4,8,4,8));
        mWatchListFrame.add(watchlistLabel, BorderLayout.NORTH);
        mWatchListFrame.add(mWatchListScrollPane, BorderLayout.CENTER);

        mWatchListBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    // Double-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    EditWatch ();
                }
            }
        });

        mWatchListBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    EditWatch();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                DeleteWatch ();
                else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    mWatchListBox.setSelectedIndex(mWatches.size ());
                    EditWatch ();
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
        callstackLabel.setBorder(new EmptyBorder(4,8,4,8));
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
                actionRun();
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
            public void actionPerformed(ActionEvent e) { actionPlayPause();}
        });
        mStepOverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { actionStep();}
        });
        mStepInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { actionStepInto();}
        });
        mStepOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { actionStepOutOf();}
        });
        mRunButton.setToolTipText("Run the program!");

        mToolBar.setAlignmentY(1);
        mToolBar.setFloatable(false);

        //Status Panel
        JPanel panelStatusInfo      = new JPanel(new BorderLayout());
        JPanel panelStatusCursor    = new JPanel();

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
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosing(WindowEvent e) {
                // Stop program running
                if (mMode == ApMode.AP_RUNNING || mMode == ApMode.AP_PAUSED) {
                    SetMode(ApMode.AP_STOPPED);
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
        //Initialize syntax highlighting
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/basic4gl", "com.basic4gl.desktop.BasicTokenMaker");

        //mDLLs(GetCurrentDir().c_str(), false)
        mPreprocessor = new Preprocessor(2, new EditorSourceFileServer(this), new DiskFileServer());
        mDebugger = new Debugger(mPreprocessor.LineNumberMap());
        mVM = new TomVM(mDebugger);
        mComp = new TomBasicCompiler(mVM);

        //TODO Confirm this doesn't break if app is ever signed
        //getParent
        mAppDirectory      		= new File(".").getAbsolutePath();

        if (new File(mAppDirectory, "Programs").exists())
            mRunDirectory    	= mAppDirectory + "\\Programs";
        else
            mRunDirectory    	= mAppDirectory;
        mFileDirectory     		= mRunDirectory;
        mCurrentDirectory       = mFileDirectory;

        RefreshActions();
        RefreshDebugDisplays();

        InitLibraries();
        ResetProject();

        // Display the window.
        mFrame.pack();
        mFrame.setLocationRelativeTo(null);
        mFrame.setVisible(true);
    }
    public JFrame getFrame(){
        return mFrame;
    }
    private void ResetProject() {
        // Clear out the current project and setup a new basic one with a single
        // source-file.

        // Close existing editors
        mTabControl.removeAll();
        this.mFileEditors.clear();

        // Create a default tab
        addTab();


        //Display the editor
        mTabControl.setSelectedIndex(0);

    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ClassLoader.getSystemClassLoader().getResource(
                path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find resource file: " + path);
            return null;
        }
    }
    private int getTabIndex(String path){
        int i = 0;
        boolean found = false;
        for (; i < mFileEditors.size(); i++){
            if (mFileEditors.get(i).getFilePath().equals(path)){
                found = true;
                break;
            }
        }
        return found ? i : -1;
    }

    void actionNew() {
        if (MultifileCheckSaveChanges()) {

            mRunDirectory = mFileDirectory;
            mCurrentDirectory = mRunDirectory;

            //Clear file editors
            this.mTabControl.removeAll();
            this.mFileEditors.clear();

            this.addTab();

        }
    }

    void actionOpen() {
        if (MultifileCheckSaveChanges()) {
            mCurrentDirectory = mFileDirectory;
        FileEditor editor = FileEditor.open(mFrame, this, mLinkGenerator);
        if (editor != null) {
                //TODO Check if file should open as new tab or project
                //For now just open as new project
                //So... close all current tabs
                closeAll();


                // Set current directory to main file directory
                // Must be done BEFORE setting the long filename, because the short
                // filename will be calculated based on the current dir.
                mFileDirectory = new File(editor.getFilePath()).getParent();
                mRunDirectory = mFileDirectory;

                mCurrentDirectory = mRunDirectory;

                //Display file
                addTab(editor);
            }
        }
    }
    boolean FileCheckSaveChanges(int index) {

        // Is sub-file modified?
        FileEditor editor = mFileEditors.get(index);
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
        if (MultifileModified(description)) {

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
        for (int i = 0; i < mFileEditors.size(); i++)
            if (mFileEditors.get(i).isModified()) {
                mTabControl.setSelectedIndex(i);
                if (!actionSave(i))
                    return false;
            }

        return true;
    }

    boolean MultifileModified(Mutable<String> description) {
        boolean result = false;
        String desc = "";

        for (int i = 0; i < mFileEditors.size(); i++) {
            if (mFileEditors.get(i).isModified()) {
                result = true;
                if (!desc.equals(""))
                    desc += ", ";
                String filename = mFileEditors.get(i).getShortFilename();

                desc += filename;
            }
        }
        description.set(desc);
        return result;
    }

    boolean actionSave() {
        //Save content of current tab
        if (this.mFileEditors.isEmpty()
                || this.mTabControl.getTabCount() == 0
                || this.mTabControl.getSelectedIndex() == -1)
            return false;

        int index = mTabControl.getSelectedIndex();
        boolean saved = mFileEditors.get(index).save(false, mCurrentDirectory);
        if (saved) {
            //TODO Check if index of main file
            int main = 0;
            if (index == main) {
                mFileDirectory = new File(mFileEditors.get(index).getFilePath()).getParent();
                mRunDirectory = mFileDirectory;
                mCurrentDirectory = mRunDirectory;
            }
            mTabControl.setTitleAt(index, mFileEditors.get(index).getTitle());
            mTabControl.getTabComponentAt(index).invalidate();
        }
        return saved;
    }

    boolean actionSave(int index) {
        //Save content of current tab
        if (this.mFileEditors.isEmpty()
                || this.mTabControl.getTabCount() == 0
                || this.mTabControl.getSelectedIndex() == -1)
            return false;

        boolean saved = mFileEditors.get(index).save(false, mCurrentDirectory);
        if (saved) {
            //TODO Check if main file
            int main = 0;
            if (index == main) {
                mFileDirectory = new File(mFileEditors.get(index).getFilePath()).getParent();
                mRunDirectory = mFileDirectory;
                mCurrentDirectory = mRunDirectory;
            }
            mTabControl.setTitleAt(index, mFileEditors.get(index).getTitle());
            mTabControl.getTabComponentAt(index).invalidate();
        }
        return saved;
    }

    void actionSaveAs() {
        //Save content of current tab as new file
        if (this.mFileEditors.isEmpty()
                || this.mTabControl.getTabCount() == 0
                || this.mTabControl.getSelectedIndex() == -1)
            return;
        int index = mTabControl.getSelectedIndex();

        mCurrentDirectory = mFileDirectory;

        if (mFileEditors.get(index).save(true, mCurrentDirectory)) {
            //TODO get current main file
            int main = 0;
            if (index == main) {
                mFileDirectory = new File(mFileEditors.get(index).getFilePath()).getParent();
                mRunDirectory = mFileDirectory;
                mCurrentDirectory = mRunDirectory;
            }

        } else {
            //Restore Current directory
            mCurrentDirectory = mRunDirectory;
        }
        mTabControl.setTitleAt(index, mFileEditors.get(index).getTitle());
        mTabControl.getTabComponentAt(index).invalidate();
    }
    private void actionRun()
    {
        if (mMode == ApMode.AP_STOPPED) {

            // Compile and run program from start
            if (Compile ())
                Continue ();
        }
        else  {

            // Stop program completely.
            if (mBuilder != null && mBuilder.getTarget() != null)
                mBuilder.getTarget().stop();
            SetMode(ApMode.AP_STOPPED);
        }
    }
    private void actionPlayPause()    {
        switch (mMode) {
            case AP_RUNNING:
                // Pause program
                mVM.Pause ();   //Rely on callbacks to alert the main window that the VM was paused to update UI

                break;

            case AP_STOPPED:
                if (Compile ())                 // When stopped, Play is exactly the same as Run
                    Continue ();
                break;

            case AP_PAUSED:
                if (mMessage != null)
                    synchronized (mMessage) {
                        mMessage.notify();
                    }
                Continue ();                    // When paused, play continues from where program was halted.
                break;
        }
    }
    private void actionStep(){
        DoStep (1);
    }
    private void actionStepInto(){
        DoStep (2);
    }
    private void actionStepOutOf(){
        DoStep (3);
    }
    private void actionDebugMode()
    {
        // Toggle debug mode
        mDebugMode = !mDebugMode;
        mDebugMenuItem.setSelected(mDebugMode);
        mDebugButton.setSelected(mDebugMode);

        RefreshDebugDisplays();
    }

    public void closeAll() {
        for (int i = mTabControl.getTabCount() - 1; i >= 0; i--)
            closeTab(i);

        // Reset default run directory to programs folder
        mRunDirectory = mAppDirectory + "\\Programs";

        // Clear DLLs, breakpoints, bookmarks etc
        //m_dlls.Clear();
        mDebugger.ClearUserBreakPts();

        // Refresh UI
        RefreshActions ();
    }

    public void closeTab(int index){
        mTabControl.remove(index);
        mFileEditors.remove(index);
    }
    public void addTab() {
        final FileEditor editor = new FileEditor(this, mLinkGenerator);
        addTab(editor);
    }

    public void addTab(FileEditor editor) {
        int count = mFileEditors.size();
        mFileEditors.add(editor);
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
                        FileEditor editor = mFileEditors.get(i);
                        List<Integer> breakpoints = editor.getBreakpoints();
                        String file = editor.getFilePath();
                        for (Integer line: breakpoints)
                            MainWindow.this.toggleBreakpt(file, line);

                        //Remove tab
                        mTabControl.remove(i);
                        mFileEditors.remove(i);

                        //Refresh controls if no files open
                        if (mFileEditors.size() == 0)
                            SetMode(ApMode.AP_CLOSED);
                    }

                }
            }
        });
        mTabControl.setTabComponentAt(mTabControl.getTabCount() - 1, tabComponent);

        //Allow user to see cursor position
        editor.editorPane.addCaretListener(TrackCaretPosition);
        mCursorPosLabel.setText(0 + ":" + 0); //Reset label

        //Set tab as read-only if App is running or paused
        boolean readOnly = mMode != ApMode.AP_STOPPED;
        editor.editorPane.setEditable(!readOnly);

        //TODO set syntax highlight colors

        //Refresh interface if there was previously no tabs open
        if (count == 0)
            SetMode(ApMode.AP_STOPPED);
    }
    private void PlaceCursorAtProcessed(final int row, int col) {

        // Place cursor at position corresponding to row, col in post-processed file.
        // Find corresponding source position
        Mutable<String> filename = new Mutable<String>("");
        Mutable<Integer> fileRow = new Mutable<Integer>(0);
        mPreprocessor.LineNumberMap().SourceFromMain(filename, fileRow, row);

        final String file = filename.get();
        final int r = fileRow.get();
        final int c = col;

        // Find (and show) corresponding editor frame
        if (r >= 0) {
            int index = getTabIndex(file);
            if (index == -1){
                //Attempt to open tab
                addTab(FileEditor.open(new File(file), MainWindow.this, mLinkGenerator));
                index = mTabControl.getTabCount() - 1;
                //return;
            }

            mTabControl.setSelectedIndex(index);

            final JTextArea frame = mFileEditors.get(getTabIndex(file)).editorPane;

            // Set focus
            frame.grabFocus();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int col = c;
                    // Place cursor
                    if (r >= 0) {
                        try {
                            JTextArea textArea = mFileEditors.get(mTabControl.getSelectedIndex()).editorPane;
                            int offset = textArea.getLineStartOffset(r);

                            //Reduce column position if it would place the cursor at the next line
                            if (textArea.getLineCount() > r + 1
                                    && offset + col == textArea.getLineStartOffset(r + 1))
                                offset = textArea.getLineStartOffset(r + 1) - 1;
                            else
                                offset += col;

                            frame.setCaretPosition(offset);
                        } catch (Exception ex) {
                            //Do nothing
                        }
                    }
                }
            });

        }

    }
    private void InitLibraries() {
        // TODO Implement standard libraries
        // Plug in constant and function libraries
		/*
		 * InitTomStdBasicLib (mComp); // Standard library
		 * InitTomWindowsBasicLib (mComp, &m_files); // Windows specific
		 * library InitTomOpenGLBasicLib (mComp, m_glWin, &m_files); // OpenGL
		 * InitTomTextBasicLib (mComp, m_glWin, m_glText); // Basic
		 * text/sprites InitGLBasicLib_gl (mComp); InitGLBasicLib_glu (mComp);
		 * InitTomJoystickBasicLib (mComp, m_glWin); // Joystick support
		 * InitTomTrigBasicLib (mComp); // Trigonometry library
		 * InitTomFileIOBasicLib (mComp, &m_files); // File I/O library
		 * InitTomNetBasicLib (mComp); // Networking
		 */

        //TODO Load libraries dynamically
        mLibraries.add(new com.basic4gl.lib.standard.Standard());
        mLibraries.add(new com.basic4gl.lib.standard.TrigBasicLib());
        mLibraries.add(new com.basic4gl.lib.standard.FileIOBasicLib());
        mLibraries.add(new com.basic4gl.lib.standard.WindowsBasicLib());
        mLibraries.add(new com.basic4gl.lib.desktopgl.JoystickBasicLib());
        mLibraries.add(new com.basic4gl.lib.desktopgl.TextBasicLib());
        mLibraries.add(new com.basic4gl.lib.desktopgl.OpenGLBasicLib());
        mLibraries.add(new com.basic4gl.lib.desktopgl.GLUBasicLib());
        mLibraries.add(new com.basic4gl.lib.desktopgl.GLBasicLib_gl());
        mLibraries.add(GLTextGridWindow.getInstance(mComp));
        mLibraries.add(BuilderDesktopGL.getInstance(mComp));

        //TODO Add more libraries
        int i = 0;
        for (Library lib : mLibraries) {
            lib.init(mComp); //Allow libraries to register function overloads
            mComp.AddConstants(lib.constants());
            mComp.AddFunctions(lib, lib.specs());
            if (lib instanceof Builder) {
                mBuilders.add(i);
            }
            i++;
        }
        //Set default target
        if (mBuilders.size() > 0)
            mCurrentBuilder = 0;

        //Initialize highlighting
        //mKeywords = new HashMap<String,Color>();
        BasicTokenMaker.mReservedWords.clear();
        BasicTokenMaker.mFunctions.clear();
        BasicTokenMaker.mConstants.clear();
        BasicTokenMaker.mOperators.clear();
        for (String s : mComp.m_reservedWords)
            BasicTokenMaker.mReservedWords.add(s);

        for (String s : mComp.Constants().keySet())
            BasicTokenMaker.mConstants.add(s);

        for (String s : mComp.m_functionIndex.keySet())
            BasicTokenMaker.mFunctions.add(s);

        for (String s : mComp.getBinaryOperators())
            BasicTokenMaker.mOperators.add(s);
        for (String s : mComp.getUnaryOperators())
            BasicTokenMaker.mOperators.add(s);

    }

    private void Pause() {

        // Place editor into paused mode
        SetMode(ApMode.AP_PAUSED);
        RefreshActions();

        // Place editor into debug mode
        mDebugMode = true;
        mDebugMenuItem.setSelected(true);
        mDebugButton.setSelected(true);
        RefreshDebugDisplays();

        //TODO Add VMViewer
        //VMView().SetVMIsRunning(false);
    }


    private void SetMode(ApMode mode) {

        // Set the mMode parameter.
        // Handles sending the appropriate notifications to the plugin DLLs,
        // updating the UI and status messages.
        if (mMode != mode) {
            String statusMsg = "";

            // Send appropriate notifications to libraries and plugin DLLs
            if (mode == ApMode.AP_RUNNING) {
                if (mMode == ApMode.AP_STOPPED) {
                    //if (!mDLLs.ProgramStart()) {
                    //    MessageDlg(mDLLs.Error().c_str(), mtError, TMsgDlgButtons() << mbOK, 0);
                    //    return;
                    //}
                }
                //else if (mMode == ApMode.AP_PAUSED)
                    //mDLLs.ProgramResume();
                statusMsg = "Running...";
            }
            else if (mode == ApMode.AP_STOPPED && mMode != ApMode.AP_STOPPED
                    && mMode != ApMode.AP_CLOSED) {
                if (mVM.Done () && !mVM.hasError())
                    statusMsg = "Program completed";
                else if (mVM.hasError())
                    statusMsg = mVM.getError();
                else
                    statusMsg = "Program stopped";
                //mDLLs.ProgramEnd();
                mVM.ClearResources();

                // Inform libraries
                //StopTomSoundBasicLib();
            }
            else if (mode == ApMode.AP_PAUSED && mMode == ApMode.AP_RUNNING) {
                statusMsg = "Program paused. Click play button to resume.";
                //mDLLs.ProgramPause();
            }

            if (mMode == ApMode.AP_CLOSED) {
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

            // Set mode
            mMode = mode;

            // Update UI
            RefreshActions();
            RefreshDebugDisplays();
            mCompStatusLabel.setText(statusMsg);

            // Notify virtual machine view
            //TODO Implement VM Viewer
            //VMView().SetVMIsRunning(mode == ApMode.AP_RUNNING);
        }
    }
    private void SetReadOnly(boolean readOnly){
        for (int i = 0; i < mFileEditors.size(); i++)
            mFileEditors.get(i).editorPane.setEditable(!readOnly);
    }
    private void RefreshActions ()
    {
        //TODO get main file index
        int main = 0;

        // Enable/disable actions to reflect state
        switch (mMode) {
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
                for (int i = 0; i < mTabControl.getTabCount(); i++)
                    ((ButtonTabComponent)mTabControl.getTabComponentAt(i)).getButton().setEnabled(true);

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

                SetReadOnly(false);
                mRunMenuItem.setText("Run Program");
                mRunButton.setIcon(createImageIcon(ICON_RUN_APP));
                break;

            case AP_RUNNING:
                if (main > -1 && main < mTabControl.getTabCount())
                    ((ButtonTabComponent)mTabControl.getTabComponentAt(main)).getButton().setEnabled(false);

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

                SetReadOnly(true);
                mRunMenuItem.setText("Stop Program");
                mRunButton.setIcon(createImageIcon(ICON_STOP_APP));
                break;

            case AP_PAUSED:
                if (main > -1 && main < mTabControl.getTabCount())
                    ((ButtonTabComponent)mTabControl.getTabComponentAt(main)).getButton().setEnabled(false);

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

                SetReadOnly(true);
                mRunMenuItem.setText("Stop Program");
                mRunButton.setIcon(createImageIcon(ICON_STOP_APP));
                break;
        }
    }

    private void RefreshDebugDisplays (){

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

        if (mMode != ApMode.AP_CLOSED) {
            mPlayButton.setIcon(mMode == ApMode.AP_RUNNING ? createImageIcon(ICON_PAUSE) : createImageIcon(ICON_PLAY));
            mPlayButton.setEnabled(true);
            mStepOverButton.setEnabled(mMode != ApMode.AP_RUNNING);
            mStepInButton.setEnabled(mMode != ApMode.AP_RUNNING);
            mStepOutButton.setEnabled(mMode == ApMode.AP_PAUSED && (mVM.UserCallStack().size() > 0));
        }
        if (!mDebugMode)
            return;

        // Clear debug controls
        mWatchListModel.clear();
        mGosubListModel.clear();

        for (String watch: mWatches)
            mWatchListModel.addElement(watch + ": " + EvaluateWatch(watch, true));
        mWatchListModel.addElement(" ");              // Last line is blank, and can be clicked on to add new watch

        if (mMode != ApMode.AP_PAUSED)
            return;

        // Update call stack
        mGosubListModel.addElement("IP");
        Vector<UserFuncStackFrame> callStack = mVM.UserCallStack();
        for (int i2 = 0; i2 < callStack.size(); i2++) {
            UserFuncStackFrame frame = callStack.get(callStack.size() - i2 - 1);

            // User functions have positive indices
            if (frame.userFuncIndex >= 0)
                mGosubListModel.addElement(mComp.GetUserFunctionName(frame.userFuncIndex) + "()");

                // Otherwise must be a gosub
            else
                mGosubListModel.addElement("gosub " + mComp.DescribeStackCall(frame.returnAddr));
        }

    }
    private void EditWatch () {
        String newWatch, oldWatch;

        // Find watch
        int index = mWatchListBox.getSelectedIndex();
        int saveIndex = index;

        // Extract watch text
        if (index > -1 && index < mWatches.size())
            oldWatch = mWatches.get(index);
        else
            oldWatch = "";

        // Prompt for new text
        newWatch = (String) JOptionPane.showInputDialog(mFrame, "Enter variable/expression:","Watch variable",
                JOptionPane.PLAIN_MESSAGE,null, null, oldWatch);

        // Update/insert/delete watch
        if (newWatch != null) {
            newWatch = newWatch.trim();
            if (newWatch.equals("")) {
                //User entered an empty value
                if (index > -1 && index < mWatches.size())
                    mWatches.remove(index);
            } else {
                if (index > -1 && index < mWatches.size())
                    mWatches.set(index, newWatch);
                else
                    mWatches.add(newWatch);
            }
        }
        RefreshDebugDisplays ();
        mWatchListBox.setSelectedIndex(saveIndex);
        UpdateWatchHint ();
    }

    void DeleteWatch () {

        // Find watch
        int index = mWatchListBox.getSelectedIndex();
        int saveIndex = index;

        // Delete watch
        if (index > -1 && index < mWatches.size())
            mWatches.remove(index);

        RefreshDebugDisplays ();
        mWatchListBox.setSelectedIndex(saveIndex);
        UpdateWatchHint ();
    }

    private void UpdateWatchHint () {
        int index = mWatchListBox.getSelectedIndex();
        if (index > -1 && index < mWatches.size())
            mWatchListBox.setToolTipText((String)mWatchListModel.get(index));
        else
            mWatchListBox.setToolTipText("");
    }
    private String EvaluateWatch (String watch, boolean canCallFunc) {
        if (mMode == ApMode.AP_RUNNING)
            return "???";

        // Save virtual machine state
        VMState state   = mVM.getState();
        ApMode saveMode = mMode;
        try {

            // Setup compiler "in function" state to match the the current VM user
            // stack state.
            int currentFunction;
            if (    mVM.CurrentUserFrame() < 0 ||
                    mVM.UserCallStack().lastElement().userFuncIndex < 0)
                currentFunction = -1;
            else
                currentFunction = mVM.UserCallStack().get(mVM.CurrentUserFrame()).userFuncIndex;

            boolean inFunction = currentFunction >= 0;

            // Compile watch expression
            // This also gives us the expression result type
            int codeStart = mVM.InstructionCount ();
            ValType valType = new ValType();
            //TODO Possibly means to pass parameters by ref
            if (!mComp.TempCompileExpression (watch, valType, inFunction, currentFunction))
                return mComp.getError();

            if (!canCallFunc)
                // Expressions aren't allowed to call functions for mouse-over hints.
                // Scan compiled code for OP_CALL_FUNC or OP_CALL_OPERATOR_FUNC
                for (int i = codeStart; i < mVM.InstructionCount (); i++)
                    if (mVM.Instruction(i).mOpCode == OpCode.OP_CALL_FUNC
                            ||  mVM.Instruction(i).mOpCode == OpCode.OP_CALL_OPERATOR_FUNC
                            ||  mVM.Instruction(i).mOpCode == OpCode.OP_CALL_DLL
                            ||  mVM.Instruction(i).mOpCode == OpCode.OP_CREATE_USER_FRAME)
                        return "Mouse hints can't call functions. Use watch instead.";

            // Run compiled code
            mVM.GotoInstruction (codeStart);
            mMode = ApMode.AP_RUNNING;
            do {

                // Run the virtual machine for a certain number of steps
                //TODO Continue
                mVM.Continue(GB_STEPS_UNTIL_REFRESH);

                // Process windows messages (to keep application responsive)
                //Application.ProcessMessages ();
                //mGLWin.ProcessWindowsMessages();
                //TODO Implement pausing
                //if (mTarget.PausePressed ())           // Check for pause key. (This allows us to pause when in full screen mode. Useful for debugging.)
                //    mVM.Pause ();
            } while (       mMode == ApMode.AP_RUNNING
                    &&  !mVM.hasError()
                    &&  !mVM.Done ()
                    &&  !mVM.Paused ()
                    &&  !mBuilder.getTarget().isClosing());

            // Error occurred?
            if (mVM.hasError())
                return mVM.getError();

            // Execution didn't finish?
            if (!mVM.Done ())
                return "???";

            // Convert expression result to string
            return DisplayVariable(valType);
        } finally {
            mVM.SetState (state);
            mMode = saveMode;
            RefreshActions ();
            // TODO Add VM viewer
            //VMView().RefreshVMView();
        }
    }

    private String DisplayVariable(ValType valType) {
        if (valType.Equals(ValType.VTP_STRING))                                  // String is special case.
            return "\"" + mVM.RegString () + "\"";                 // Stored in string register.
        else {
            String temp;
            try {
                Mutable<Integer> maxChars = new Mutable<Integer>(TomVM.DATA_TO_STRING_MAX_CHARS);
                temp = mVM.ValToString (mVM.Reg (), valType, maxChars);
            } catch (Exception ex) {

                // Floating point errors can be raised when converting floats to string
                /*switch (ex.getCause()) {
                    case EXCEPTION_FLT_DENORMAL_OPERAND:
                    case EXCEPTION_FLT_DIVIDE_BY_ZERO:
                    case EXCEPTION_FLT_INEXACT_RESULT:
                    case EXCEPTION_FLT_INVALID_OPERATION:
                    case EXCEPTION_FLT_OVERFLOW:
                    case EXCEPTION_FLT_STACK_CHECK:
                    case EXCEPTION_FLT_UNDERFLOW:
                    case EXCEPTION_INT_DIVIDE_BY_ZERO:
                    case EXCEPTION_INT_OVERFLOW:
                        temp = "Floating point exception";
                    default:*/
                        temp = "An exception occurred";
                //}
            }
            return temp;
        }
    }



    @Override
    public int editorCount() {
        return mFileEditors.size();
    }
    @Override
    public JTextArea getEditor(int index) {
        return mFileEditors.get(index).editorPane;
    }
    public String getFilename(int index) {
        return mFileEditors.get(index).getFilePath();
    }
    public String getCurrentDirectory() { return mCurrentDirectory;}
    @Override
    public boolean isVMRunning() {
        return mMode == ApMode.AP_RUNNING;
    }

    @Override
    public int getVMRow(String filename) {
            if (mMode == ApMode.AP_RUNNING || !mVM.IPValid())
                return -1;

            // Find IP row
            Mutable<Integer> row = new Mutable<Integer>(-1), col = new Mutable<Integer>(-1);
            mVM.GetIPInSourceCode(row, col);

            // Convert to corresponding position in source file
            return mPreprocessor.LineNumberMap().SourceFromMain(filename, row.get());

    }

    @Override
    public int isBreakpt(String filename, int line) {
        return mDebugger.IsUserBreakPt(filename, line)
                ? 1
                : 0;
    }

    @Override
    public boolean toggleBreakpt(String filename, int line) {
        boolean isBreakpoint = mDebugger.ToggleUserBreakPt(filename, line);
        // If program is not running, breakpoints will be patched as soon as it
        // resumes or restarts.
        // If it IS running, however we must explicitly force a re-patch to ensure
        // the change is registered.
        //TODO Address potential concurrency issue
        if (mMode == ApMode.AP_RUNNING)
            mVM.RepatchBreakpts();
        return isBreakpoint;
    }

    @Override
    public String getVariableAt(String line, int x) {
        char[] l = line.toCharArray();
        // Find character
        if (x < 1 || x > l.length || l [x] <= ' ')
            return "";

        // Scan to right of word
        int right = x + 1;
        while (right <= l.length
                && (    (l [right] >= 'a' && l [right] <= 'z')
                ||  (l [right] >= 'A' && l [right] <= 'Z')
                ||  (l [right] >= '0' && l [right] <= '9')
                ||  l [right] == '_'
                ||  l [right] == '#'
                ||  l [right] == '$'))
            right++;

        // Scan left
        int left = x;
        while (left > 0
                && (    (l [left] >= 'a' && l [left] <= 'z')
                ||  (l [left] >= 'A' && l [left] <= 'Z')
                ||  (l [left] >= '0' && l [left] <= '9')
                ||  l [left] == '.'
                ||  l [left] == '_'
                ||  l [left] == '#'
                ||  l [left] == '$'
                ||  l [left] == ')')) {

            // Skip over brackets
            if (l [left] == ')') {
                int level = 1;
                left--;
                while (level > 0 && left > 0) {
                    if (l [left] == ')')         level++;
                    else if (l [left] == '(')    level--;
                    left--;
                }
                while (left > 0 && l [left] <= ' ')
                    left--;
            }
            else
                left--;
        }
        left++;

        // Trim whitespace from left
        while (left < right && l [left] <= ' ')
            left++;

        // Return result
        if (left < right)
            //TODO Possibly wrong second parameter
            return line.substring(left, right - left);
        else
            return "";
    }

    @Override
    public String evaluateVariable(String variable) {
        return EvaluateWatch(variable, false);
    }

    @Override
    public void insertDeleteLines(String filename, int fileLineNo, int delta) {
        mDebugger.InsertDeleteLines(filename, fileLineNo, delta);
    }

    @Override
    public void jumpToFile(String filename) {

    }

    @Override
    public void refreshUI() {
        RefreshActions();
    }


    // Compilation and execution routines
    private boolean LoadProgramIntoCompiler (){
        //TODO Get editor assigned as main file
        return mPreprocessor.Preprocess(
                new EditorSourceFile(getEditor(0), getFilename(0)),
                mComp.Parser());
    }

    private boolean Compile (){
        // Compile the program, and reset the IP to the start.
        // Returns true if program compiled successfully. False if compiler error occurred.
        if (mFileEditors.isEmpty()) {
            mCompStatusLabel.setText("No files are open");
            return false;
        }

        SetMode(ApMode.AP_STOPPED);

        // Compile
        if (!LoadProgramIntoCompiler()) {
            PlaceCursorAtProcessed(mComp.Parser().SourceCode().size() - 1, 0);
            mCompStatusLabel.setText(mPreprocessor.getError());
            return false;
        }
        mComp.clearError();
        mComp.Compile ();

        // Inform virtual machine view that code has changed
        //TODO add VM viewer
        //VMView().RefreshVMView();

        if (mComp.hasError()) {
            PlaceCursorAtProcessed((int)mComp.Line(), (int)mComp.Col());
            mCompStatusLabel.setText(mComp.getError());
            return false;
        }

        // Reset Virtual machine
        mVM.Reset ();

        // TODO Reset OpenGL state
        //mGLWin.ResetGL ();
        //mTarget.reset();
        //mTarget.activate();
        //mGLWin.OpenGLDefaults ();

        //TODO Reset file directory
        //SetCurrentDir(mRunDirectory);

        return true;
    }
    // Debugging
    private void DoStep (int type){
        if (mMode == ApMode.AP_RUNNING)
            return;

        // Recompile program if necessary
        if (mMode == ApMode.AP_STOPPED && !Compile ())
            return;

        // Patch in temp breakpoints
        switch (type) {
            case 1: mVM.AddStepBreakPts (false); break;        // Step over
            case 2: mVM.AddStepBreakPts (true);  break;        // Step into
            case 3:
                if (!mVM.AddStepOutBreakPt ())                 // Step out
                    return;                                     // (No gosub to step out of)
                break;
        }

        // Resume running program
        mDelayScreenSwitch = true;
        Continue ();
    }
    // IVMViewInterface
    void ExecuteSingleOpCode() {
        if (mVM.InstructionCount() > 0) {
            //TODO Continue
            //DoContinue(1);
            /*
            // Invalidate source gutter so that current IP pointer moves
            TSourceFileFrm* frame = mSourceFrames[SourcePages.ActivePageIndex];
            frame.SourceMemo.InvalidateGutter();
            frame.SourceMemo.Invalidate();*/

            // Debug displays need refreshing
            RefreshDebugDisplays ();
        }
    }
    private void Continue (){
        //Get current build target
        if ((mCurrentBuilder > -1 && mCurrentBuilder < mBuilders.size()) &&
                (mBuilders.get(mCurrentBuilder) > -1 && mBuilders.get(mCurrentBuilder) < mLibraries.size()) &&
                mLibraries.get(mBuilders.get(mCurrentBuilder)) instanceof Builder)
            mBuilder = (Builder) mLibraries.get(mBuilders.get(mCurrentBuilder));
        else
            mBuilder = null;

        // Resume running the current program

        // Set running state
        SetMode(ApMode.AP_RUNNING);
        if (mMode != ApMode.AP_RUNNING)
            return;

        // Show and activate OpenGL window
        if (mBuilder.getTarget() != null) {
            if (!mBuilder.getTarget().isVisible()) {
                mBuilder.getTarget().reset();
                mBuilder.getTarget().activate();
                int counter = 0;
                //if (!mDelayScreenSwitch) {
                mBuilder.getTarget().show(new DebugCallback());
            } else {
                synchronized (mMessage) {
                    mMessage.status = CallbackMessage.WORKING;
                    mMessage.notify();
                }
            }
        }

        //}
        //else {
            //counter = 2;            // Activate screen second time around main loop.
        //}
        // Run loop

        // Kick the virtual machine over the next op-code before patching in the breakpoints.
        // otherwise we would never get past a breakpoint once we hit it, because we would
        // keep on hitting it immediately and returning.
        //TODO Continue
        //DoContinue(1);
        /*do {

            if (counter > 0 && --counter == 0)
                mTarget.show(new DebugCallback());

            //TODO Continue
            //DoContinue(GB_STEPS_UNTIL_REFRESH);

            // Process windows messages (to keep application responsive)
            //Application.ProcessMessages ();
            //mGLWin.ProcessWindowsMessages();
            //TODO implement pausing
            //if (mTarget.PausePressed ())           // Check for pause key. (This allows us to pause when in full screen mode. Useful for debugging.)
            //   Pause();
        } while (       mMode == ApMode.AP_RUNNING
                &&  !mVM.hasError ()
                &&  !mVM.Done ()
                &&  !mVM.Paused ()
                &&  !mTarget.isClosing());
*/

        // Clear temp breakpoints (user ones still remain)
        // This also patches out all breakpoints.
    }
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
    public class IncludeLinkGenerator implements LinkGenerator
    {
        static final String INCLUDE = "include ";

        /**
         * Separators used to determine words in text.
         */
        private final java.util.List<String> textSeparators =
                Arrays.asList("\n");
                //Arrays.asList(",", ";", "\n", "|", "{", "}", "[", "]", "=", "\"", "'", "*", "%", "&", "?");

        @Override
        public LinkGeneratorResult isLinkAtOffset ( RSyntaxTextArea source, final int pos )
        {
            final String code = source.getText ();
            final int wordStart = getWordStart ( code, pos );
            final int wordEnd = getWordEnd ( code, pos );
            final String word = code.substring ( wordStart, wordEnd );
            final String link;
            final Dimension key;


            final LinkGeneratorResult value;
            if (word.startsWith(INCLUDE)){
                link = code.substring ( wordStart + INCLUDE.length(), wordEnd ).trim();
                key = new Dimension ( wordStart + INCLUDE.length(), wordEnd );
            } else {
                return null;
            }

            if ( word != null )
            {
                value = new LinkGeneratorResult ()
                {
                    @Override
                    public HyperlinkEvent execute ()
                    {

                        File file = new File(mCurrentDirectory, link);
                        int index;
                        index = getTabIndex(file.getAbsolutePath());
                        if (index != -1) {
                            mTabControl.setSelectedIndex(index);
                        } else {

                            System.out.println("Open tab: " + link);
                            System.out.println("Path: " + file.getAbsolutePath());
                            MainWindow.this.addTab(FileEditor.open(file, MainWindow.this, mLinkGenerator));
                            mTabControl.setSelectedIndex(mTabControl.getTabCount() - 1);
                        }
                        return new HyperlinkEvent ( this, HyperlinkEvent.EventType.EXITED, null );
                    }

                    @Override
                    public int getSourceOffset ()
                    {
                        return wordStart;
                    }
                };
            }
            else
            {
                value = null;
            }
            return value;
        }

        /**
         * Returns a word start index at the specified location.
         *
         * @param text     text to retrieve the word start index from
         * @param location word location
         * @return word start index
         */
        public int getWordStart ( final String text, final int location )
        {
            int wordStart = location;
            while ( wordStart > 0 && !textSeparators.contains ( text.substring ( wordStart - 1, wordStart ) ) )
            {
                wordStart--;
            }
            return wordStart;
        }

        /**
         * Returns a word end index at the specified location.
         *
         * @param text     text to retrieve the word end index from
         * @param location word location
         * @return word end index
         */
        public int getWordEnd ( final String text, final int location )
        {
            int wordEnd = location;
            while ( wordEnd < text.length () && !textSeparators.contains ( text.substring ( wordEnd, wordEnd + 1 ) ) )
            {
                wordEnd++;
            }
            return wordEnd;
        }
    }
    //TODO Reimplement callbacks

    public class DebugCallback implements TaskCallback {

        @Override
        public void message(CallbackMessage message) {
            mMessage = message;
            if (message.status == CallbackMessage.WORKING)
                return;
            //TODO Pause
            if (message.status == CallbackMessage.PAUSED) {
                Pause();
            }

            //TODO determine if if-block is needed
            // Determine whether we are paused or stopped. (If we are paused, we can
            // resume from the current position. If we are stopped, we cannot.)
            if (mVM.Paused () && !mVM.hasError() && !mVM.Done () && !mBuilder.getTarget().isClosing())
                Pause();
            else {
                SetMode(ApMode.AP_STOPPED);
                //Program completed
            }
            RefreshActions ();
            RefreshDebugDisplays ();
            mVM.ClearTempBreakPts ();

            // Handle GL window
            if (mBuilder.getTarget().isClosing())                // Explicitly closed
                mBuilder.getTarget().hide();                   // Hide it


            //mTarget.setClosing(false);
            //if (!mBuilder.getTarget().isVisible())
            //    mBuilder.getTarget().reset();

            // Get focus back
            if (!(mBuilder.getTarget().isVisible() && !mBuilder.getTarget().isFullscreen () && mVM.Done ())) {  // If program ended cleanly in windowed mode, leave focus on OpenGL window
                mFrame.requestFocus();
                if (!mFileEditors.isEmpty() && mTabControl.getTabCount() != 0) {
                    //TODO set tab to file that error occurred in
                    mFileEditors.get(mTabControl.getSelectedIndex()).editorPane.grabFocus();
                }
            }

            // Place cursor on current instruction
            //TODO Set as callbacks
            if (mVM.hasError() || mMode == ApMode.AP_PAUSED && mVM.IPValid()) {
                Mutable<Integer> line = new Mutable<Integer>(0), col = new Mutable<Integer>(0);

                mVM.GetIPInSourceCode(line, col);
                PlaceCursorAtProcessed(line.get(), col.get());
            }
        }

    }
}