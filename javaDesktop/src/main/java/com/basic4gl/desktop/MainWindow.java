package com.basic4gl.desktop;

import com.basic4gl.desktop.debugger.*;
import com.basic4gl.desktop.editor.BasicTokenMaker;
import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.desktop.editor.ITabProvider;
import com.basic4gl.desktop.editor.IncludeLinkGenerator;
import com.basic4gl.desktop.util.*;
import com.basic4gl.compiler.Preprocessor;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.TomBasicCompiler.LanguageSyntax;
import com.basic4gl.library.desktopgl.BuilderDesktopGL;
import com.basic4gl.library.desktopgl.GLTextGridWindow;
import com.basic4gl.lib.util.*;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.basic4gl.desktop.util.SwingIconUtil.createImageIcon;

/**
 * Created by Nate on 2/24/2015.
 */
public class MainWindow implements MainEditor,
        IApplicationHost,
        ITabProvider,
        IFileProvider {

    private static final String IMAGE_DIRECTORY = "images/";
    private static final String THEME_DIRECTORY = IMAGE_DIRECTORY + "programmer-art/";
    private static final String ICON_RUN_APP = THEME_DIRECTORY + "icon_run.png";
    private static final String ICON_STOP_APP = THEME_DIRECTORY + "icon_stop.png";
    private static final String ICON_NEW = THEME_DIRECTORY + "icon_new.png";
    private static final String ICON_OPEN = THEME_DIRECTORY + "icon_open.png";
    private static final String ICON_SAVE = THEME_DIRECTORY + "icon_save.png";
    private static final String ICON_DEBUG = THEME_DIRECTORY + "icon_debug.png";
    private static final String ICON_PLAY = THEME_DIRECTORY + "icon_play.png";
    private static final String ICON_PAUSE = THEME_DIRECTORY + "icon_pause.png";
    private static final String ICON_STEP_OVER = THEME_DIRECTORY + "icon_step_over.png";
    private static final String ICON_STEP_IN = THEME_DIRECTORY + "icon_step_in.png";
    private static final String ICON_STEP_OUT = THEME_DIRECTORY + "icon_step_out.png";

    static final int GB_STEPS_UNTIL_REFRESH = 1000;
    static final String EOL = "\r\n";

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

    @Override
    public boolean isApplicationRunning() {
        return mMode == MainWindow.ApMode.AP_RUNNING;
    }

    @Override
    public void continueApplication() {
        mMode = MainWindow.ApMode.AP_RUNNING;
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
        } while (mMode == MainWindow.ApMode.AP_RUNNING
                && !mVM.hasError()
                && !mVM.Done()
                && !mVM.Paused()
                && !mBuilder.getVMDriver().isClosing());
    }

    @Override
    public void pushApplicationState() {
        mTempMode = mMode;
    }

    @Override
    public void restoreHostState() {
        mMode = mTempMode;
        RefreshActions();
    }

    @Override
    public void resumeApplication() {
        mDelayScreenSwitch = true;
        ContinueHandler handler = new ContinueHandler();
        handler.Continue();
    }

    @Override
    public boolean isApplicationStopped() {
        return mMode == MainWindow.ApMode.AP_STOPPED;
    }

    enum ApMode {
        AP_CLOSED, AP_STOPPED, AP_PAUSED, AP_RUNNING
    }

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

    // Virtual machine and compiler
    private VmWorker mWorker;       // Debugging
    private TomVM mVM;              // Virtual machine
    private TomBasicCompiler mComp; // Compiler
    private FileOpener mFiles;
    private final CallbackMessage mMessage = new CallbackMessage();

    // Preprocessor
    private Preprocessor mPreprocessor;

    // Debugger
    private Debugger mDebugger;

    // State
    LanguageSyntax mLanguageSyntax = LanguageSyntax.LS_BASIC4GL;

    // Editors
    Vector<FileEditor> mFileEditors = new Vector<FileEditor>();
    IncludeLinkGenerator mLinkGenerator = new IncludeLinkGenerator(this);

    // Editor state
    ApMode mMode = ApMode.AP_STOPPED;
    ApMode mTempMode = ApMode.AP_STOPPED;

    private String mAppDirectory,  // Application directory (where basic4gl.exe is)
            mFileDirectory, // File I/O in this directory
            mRunDirectory;  // Basic4GL program are run in this directory

    private String mCurrentDirectory;   //Current working directory
    // Debugging
    private boolean mDebugMode = false;
    private List<String> mWatches = new ArrayList<String>();

    private boolean mDelayScreenSwitch = false;            // Set when stepping. Delays switching to the output window for the first 1000 op-codes.
    // (To prevent excessive screen mode switches when debugging full-screen programs.)
    private String mLine;
    private boolean mDone;

    //Libraries
    private List<Library> mLibraries = new ArrayList<Library>();
    private List<Integer> mBuilders = new ArrayList<Integer>();   //Indexes of libraries that can be launch targets
    private int mCurrentBuilder = -1;                  //Index of mTarget in mTargets
    private Builder mBuilder;                          //Build target for user's code


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
                SetMode(ApMode.AP_STOPPED);
                if (mFileEditors.size() == 0) {
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
            public void actionPerformed(ActionEvent e) {
                actionRun();
            }
        });
        mPlayPauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        mPlayPauseMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionPlayPause();
            }
        });
        mStepOverMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        mStepOverMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionStep();
            }
        });
        mStepIntoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        mStepIntoMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionStepInto();
            }
        });
        mStepOutOfMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, ActionEvent.SHIFT_MASK));
        mStepOutOfMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionStepOutOf();
            }
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
            public void actionPerformed(ActionEvent e) {
                actionPlayPause();
            }
        });
        mStepOverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionStep();
            }
        });
        mStepInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionStepInto();
            }
        });
        mStepOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionStepOutOf();
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
        mPreprocessor = new Preprocessor(2, new EditorSourceFileServer(this), new DiskFileServer());
        mDebugger = new Debugger(mPreprocessor.LineNumberMap());
        mVM = new TomVM(mDebugger);
        mComp = new TomBasicCompiler(mVM);

        //TODO Confirm this doesn't break if app is ever signed
        //getParent
        mAppDirectory = new File(".").getAbsolutePath();

        if (new File(mAppDirectory, "Programs").exists()) {
            mRunDirectory = mAppDirectory + "\\Programs";
        } else {
            mRunDirectory = mAppDirectory;
        }
        mFileDirectory = mRunDirectory;
        mCurrentDirectory = mFileDirectory;

        RefreshActions();
        RefreshDebugDisplays();

        InitLibraries();
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
        this.mFileEditors.clear();

        // Create a default tab
        addTab();


        //Display the editor
        mTabControl.setSelectedIndex(0);

    }

    public int getFileTabIndex(String filename) {
        File file = new File(mCurrentDirectory, filename);
        return getTabIndex(file.getAbsolutePath());
    }

    public int getTabIndex(String path) {
        int i = 0;
        boolean found = false;
        for (; i < mFileEditors.size(); i++) {
            if (mFileEditors.get(i).getFilePath().equals(path)) {
                found = true;
                break;
            }
        }
        return found ? i : -1;
    }

    @Override
    public void setSelectedTabIndex(int index) {
        mTabControl.setSelectedIndex(index);
    }

    @Override
    public void openTab(String filename) {
        File file = new File(mCurrentDirectory, filename);

        System.out.println("Open tab: " + filename);
        System.out.println("Path: " + file.getAbsolutePath());

        MainWindow.this.addTab(FileEditor.open(file, MainWindow.this, mLinkGenerator));

        mTabControl.setSelectedIndex(mTabControl.getTabCount() - 1);
    }

    @Override
    public void useAppDirectory() {
        mFiles.setParentDirectory(mAppDirectory);
    }

    @Override
    public void useCurrentDirectory() {
        mFiles.setParentDirectory(mCurrentDirectory);
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

    private void actionRun() {

//                libraryPath
        //TODO fix run

        if (mMode == ApMode.AP_STOPPED) {

            // Compile and run program from start
            Library builder = mLibraries.get(mBuilders.get(mCurrentBuilder));
            RunHandler handler = new RunHandler(this, mComp);
            handler.launchRemote(builder, getCurrentDirectory(), libraryPath); //12/2020 testing new continue()

        } else {
            // Stop program completely.
            StopHandler handler = new StopHandler();
            handler.stop();
        }
    }


    private void actionPlayPause() {
        switch (mMode) {
            case AP_RUNNING:
                // Pause program
                PauseHandler pauseHandler = new PauseHandler(mVM);
                pauseHandler.pause();
                break;

            case AP_STOPPED:
                // When stopped, Play is exactly the same as Run
                Library builder = mLibraries.get(mBuilders.get(mCurrentBuilder));
                RunHandler handler = new RunHandler(this, mComp);
                handler.launchRemote(builder, getCurrentDirectory(), libraryPath); //12/2020 testing new continue()

                break;

            case AP_PAUSED:
                // When paused, play continues from where program was halted.
                ResumeHandler resumeHandler = new ResumeHandler();
                resumeHandler.resume();

                break;
        }
    }

    private void actionStep() {
        StepHandler handler = new StepHandler(this, mVM);
        handler.DoStep(1);
    }

    private void actionStepInto() {
        StepHandler handler = new StepHandler(this, mVM);
        handler.DoStep(2);
    }

    private void actionStepOutOf() {
        StepHandler handler = new StepHandler(this, mVM);
        handler.DoStep(3);
    }

    private void actionDebugMode() {
        // Toggle debug mode
        mDebugMode = !mDebugMode;
        mDebugMenuItem.setSelected(mDebugMode);
        mDebugButton.setSelected(mDebugMode);

        RefreshDebugDisplays();
    }

    public void closeAll() {
        for (int i = mTabControl.getTabCount() - 1; i >= 0; i--) {
            closeTab(i);
        }

        // Reset default run directory to programs folder
        mRunDirectory = mAppDirectory + "\\Programs";

        // Clear DLLs, breakpoints, bookmarks etc
        //m_dlls.Clear();
        mDebugger.ClearUserBreakPts();

        // Refresh UI
        RefreshActions();
    }

    public void closeTab(int index) {
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

                        for (Integer line : breakpoints) {
                            MainWindow.this.toggleBreakpt(file, line);
                        }

                        //Remove tab
                        mTabControl.remove(i);
                        mFileEditors.remove(i);

                        //Refresh controls if no files open
                        if (mFileEditors.size() == 0) {
                            SetMode(ApMode.AP_CLOSED);
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
        boolean readOnly = mMode != ApMode.AP_STOPPED;
        editor.editorPane.setEditable(!readOnly);

        //TODO set syntax highlight colors

        //Refresh interface if there was previously no tabs open
        if (count == 0) {
            SetMode(ApMode.AP_STOPPED);
        }
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
            if (index == -1) {
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
        mLibraries.add(new com.basic4gl.library.standard.Standard());
        mLibraries.add(new com.basic4gl.library.standard.TrigBasicLib());
        mLibraries.add(new com.basic4gl.library.standard.FileIOBasicLib());
        mLibraries.add(new com.basic4gl.library.standard.WindowsBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.JoystickBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.TextBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.OpenGLBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.GLUBasicLib());
        mLibraries.add(new com.basic4gl.library.desktopgl.GLBasicLib_gl());
        mLibraries.add(new com.basic4gl.library.desktopgl.TomCompilerBasicLib());
//        mLibraries.add(new com.basic4gl.library.desktopgl.SoundBasicLib());
        mLibraries.add(GLTextGridWindow.getInstance(mComp));
        mLibraries.add(BuilderDesktopGL.getInstance(mComp));

        mFiles = new FileOpener(mCurrentDirectory);
        //TODO Add more libraries
        int i = 0;
        for (Library lib : mLibraries) {
            lib.init(mComp); //Allow libraries to register function overloads
            if (lib instanceof IFileAccess) {
                //Allows libraries to read from directories
                ((IFileAccess) lib).init(mFiles);
            }
            if (lib instanceof FunctionLibrary) {
                mComp.AddConstants(((FunctionLibrary) lib).constants());
                mComp.AddFunctions(lib, ((FunctionLibrary) lib).specs());
            }
            if (lib instanceof Builder) {
                mBuilders.add(i);
            }
            i++;
        }
        //Set default target
        if (mBuilders.size() > 0) {
            mCurrentBuilder = 0;
        }

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

    private void onPause() {

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
            } else if (mode == ApMode.AP_STOPPED
                    && mMode != ApMode.AP_STOPPED
                    && mMode != ApMode.AP_CLOSED) {
                if (mVM.Done() && !mVM.hasError()) {
                    statusMsg = "Program completed";
                } else if (mVM.hasError()) {
                    statusMsg = mVM.getError();
                } else {
                    statusMsg = "Program stopped";
                }
                //mDLLs.ProgramEnd();
                mVM.ClearResources();

                // Inform libraries
                //StopTomSoundBasicLib();
            } else if (mode == ApMode.AP_PAUSED && mMode == ApMode.AP_RUNNING) {
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

    private void SetReadOnly(boolean readOnly) {
        for (int i = 0; i < mFileEditors.size(); i++) {
            mFileEditors.get(i).editorPane.setEditable(!readOnly);
        }
    }

    private void RefreshActions() {
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

                SetReadOnly(false);
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

                SetReadOnly(true);
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

                SetReadOnly(true);
                mRunMenuItem.setText("Stop Program");
                mRunButton.setIcon(createImageIcon(ICON_STOP_APP));
                break;
        }
    }

    private void RefreshDebugDisplays() {

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
        if (!mDebugMode) {
            return;
        }

        // Clear debug controls
        mWatchListModel.clear();
        mGosubListModel.clear();

        for (String watch : mWatches) {
            EvaluateWatchHandler handler = new EvaluateWatchHandler(this, mComp, mVM);
            mWatchListModel.addElement(watch + ": " + handler.EvaluateWatch(watch, true));
        }
        mWatchListModel.addElement(" ");              // Last line is blank, and can be clicked on to add new watch

        if (mMode != ApMode.AP_PAUSED) {
            return;
        }

        // Update call stack
        mGosubListModel.addElement("IP");
        Vector<UserFuncStackFrame> callStack = mVM.UserCallStack();
        for (int i2 = 0; i2 < callStack.size(); i2++) {
            UserFuncStackFrame frame = callStack.get(callStack.size() - i2 - 1);

            // User functions have positive indices
            if (frame.userFuncIndex >= 0) {
                mGosubListModel.addElement(mComp.GetUserFunctionName(frame.userFuncIndex) + "()");

                // Otherwise must be a gosub
            } else {
                mGosubListModel.addElement("gosub " + mComp.DescribeStackCall(frame.returnAddr));
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
        RefreshDebugDisplays();
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

        RefreshDebugDisplays();
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

    public void setCurrentDirectory(String path) {
        mCurrentDirectory = path;
    }

    public String getCurrentDirectory() {
        return mCurrentDirectory;
    }

    @Override
    public boolean isVMRunning() {
        return mMode == ApMode.AP_RUNNING;
    }

    @Override
    public int getVMRow(String filename) {
        if (mMode == ApMode.AP_RUNNING || !mVM.IPValid()) {
            return -1;
        }

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
        ToggleBreakPointHandler handler = new ToggleBreakPointHandler(this, mDebugger, mVM);
        boolean isBreakpoint = handler.toggleBreakPoint(filename, line);
        return isBreakpoint;
    }

    @Override
    public String getVariableAt(String line, int x) {
        return EditorUtil.getVariableAt(line, x);
    }

    @Override
    public String evaluateVariable(String variable) {
        EvaluateWatchHandler handler = new EvaluateWatchHandler(this, mComp, mVM);
        return handler.EvaluateWatch(variable, false);
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
    private boolean LoadProgramIntoCompiler() {
        //TODO Get editor assigned as main file
        return mPreprocessor.Preprocess(
                new EditorSourceFile(getEditor(0), getFilename(0)),
                mComp.Parser());
    }

    @Override
    public boolean Compile() {
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
        mComp.Compile();

        // Inform virtual machine view that code has changed
        //TODO add VM viewer
        //VMView().RefreshVMView();

        if (mComp.hasError()) {
            PlaceCursorAtProcessed((int) mComp.Line(), (int) mComp.Col());
            mCompStatusLabel.setText(mComp.getError());
            return false;
        }

        // Reset Virtual machine
        mVM.Reset();

        // TODO Reset OpenGL state
        //mGLWin.ResetGL ();
        //mTarget.reset();
        //mTarget.activate();
        //mGLWin.OpenGLDefaults ();

        //TODO Reset file directory
        //SetCurrentDir(mRunDirectory);

        return true;
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
            RefreshDebugDisplays();
        }
    }





    class DesktopDebuggerCallbacks extends DebuggerCallbacks {
        DesktopDebuggerCallbacks(TaskCallback callback, CallbackMessage message) {
            super(callback, message);
        }

        @Override
        public void onPreLoad() {
            mFiles.setParentDirectory(mRunDirectory);
        }

        @Override
        public void onPostLoad() {
            mFiles.setParentDirectory(mRunDirectory);
        }
    }

    public void reset() {
        mVM.Pause();
        if (mWorker != null) {
            mWorker.cancel(true);
            //TODO confirm there is no overlap with this thread stopping and starting a new one to avoid GL errors
            try {
                mWorker.getCompletionLatch().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mWorker = new VmWorker(
            mBuilder,
            mComp,
            this,
            mVM,
            mMessage);

        mWorker.setCompletionLatch(new CountDownLatch(1));
        mVM.Reset();
    }

    public void show(TaskCallback callbacks) {
        mWorker.setCallbacks(callbacks);
        mWorker.execute();
    }

    public void hide() {
        mBuilder.getVMDriver().hide();
        mWorker.cancel(true);
    }

    public void stop() {
        mBuilder.getVMDriver().stop();
        mWorker.cancel(true);
    }


    //TODO Reimplement callbacks

    public class DebugCallback implements TaskCallback {

        @Override
        public void message(CallbackMessage message) {
            if (message == null)
                return;
            mMessage.setMessage(message);
            if (message.status == CallbackMessage.WORKING)
                return;
            //TODO Pause
            if (message.status == CallbackMessage.PAUSED) {
                onPause();
            }

            //TODO determine if if-block is needed
            // Determine whether we are paused or stopped. (If we are paused, we can
            // resume from the current position. If we are stopped, we cannot.)
            if (mVM.Paused() && !mVM.hasError() && !mVM.Done() && !mBuilder.getVMDriver().isClosing())
                onPause();
            else {
                SetMode(MainWindow.ApMode.AP_STOPPED);
                //Program completed
            }
            RefreshActions();
            RefreshDebugDisplays();
            mVM.ClearTempBreakPts();

            // Handle GL window
            if (mBuilder.getVMDriver().isClosing())                // Explicitly closed
                hide();                   // Hide it


            //mTarget.setClosing(false);
            //if (!mBuilder.getTarget().isVisible())
            //    mBuilder.getTarget().reset();

            // Get focus back
            if (!(mBuilder.getVMDriver().isVisible() && !mBuilder.getVMDriver().isFullscreen() && mVM.Done())) {  // If program ended cleanly in windowed mode, leave focus on OpenGL window
                mFrame.requestFocus();
                if (!mFileEditors.isEmpty() && mTabControl.getTabCount() != 0) {
                    //TODO set tab to file that error occurred in
                    mFileEditors.get(mTabControl.getSelectedIndex()).editorPane.grabFocus();
                }
            }

            // Place cursor on current instruction
            //TODO Set as callbacks
            if (mVM.hasError() || mMode == MainWindow.ApMode.AP_PAUSED && mVM.IPValid()) {
                Mutable<Integer> line = new Mutable<Integer>(0), col = new Mutable<Integer>(0);

                mVM.GetIPInSourceCode(line, col);
                PlaceCursorAtProcessed(line.get(), col.get());
            }
        }
    }
}