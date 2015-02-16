package com.basic4gl.desktop;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.targets.desktopgl.DesktopGL;
import com.basic4gl.lib.util.Library;
import com.basic4gl.lib.util.Target;
import com.basic4gl.lib.util.TaskCallback;
import com.basic4gl.util.Mutable;
import com.basic4gl.vm.TomVM;
import org.fife.ui.rsyntaxtextarea.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MainWindow {

    //Window Constants
    public static final String APPLICATION_NAME = "Basic4GLj";
    public static final String APPLICATION_VERSION = "Alpha 0.2.0";
    public static final String APPLICATION_BUILD_DATE = "1/24/2015";
    public static final String APPLICATION_COPYRIGHT = "(c) 2015, Nathaniel Nielsen";
    public static final String APPLICATION_DESCRIPTION = "Basic4GL for java";
    public static final String APPLICATION_WEBSITE = "blog.crazynatestudios.com";
    public static final String APPLICATION_CONTACT = "support@crazynatestudios.com";

    public static final String ICON_LOGO_SMALL = "images/logox32.png";
    public static final String ICON_LOGO_LARGE = "images/logox128.png";

    private static final String ICON_RUN_APP = "images/icon_play.png";
    private static final String ICON_STOP_APP = "images/icon_stop.png";
    private static final String ICON_NEW = "images/icon_new.png";
    private static final String ICON_OPEN = "images/icon_open.png";
    private static final String ICON_SAVE = "images/icon_save.png";

    private static final boolean DISPLAY_VERSION_INFO = true;

    enum RunMode {
        RM_STOPPED, RM_PAUSED, RM_RUNNING
    }

    ;

    // Window
    JFrame mFrame;
    JPanel mMainPanel;
    JPanel mStatusPanel;
    JMenuBar mMenuBar;
    JToolBar mToolBar;
    JTabbedPane mTabControl;
    LayoutManager mManager;
    // Buttons
    JButton mButtonSave;
    JButton mButtonNew;
    JButton mButtonOpen;
    JButton mButtonRun;
    // Labels
    JLabel mLabelStatusInfo; // Compiler/VM Status
    JLabel mLabelStatusCursor; // Cursor Position

    //TODO add library viewer to display documentation
    HashMap<String, String> mKeywordTips;

    // Compiler and VM
    private TomBasicCompiler mComp;
    private TomVM mVm;

    // Editors
    Vector<FileEditor> mFileEditors;

    // State
    private RunMode mRunMode;

    //Window to run Basic4GL virtual machine in
    private Target mTarget;

    //Libraries
    private java.util.List<Library> mLibraries;
    private java.util.List<Integer> mTargets;        //Indexes of libraries that can be launch targets
    private int mCurrentTarget;            //Index value of target in mTargets

    public static void main(String[] args) {
        new MainWindow();
    }

    public MainWindow() {
        mRunMode = RunMode.RM_STOPPED;
        mVm = new TomVM(null);
        mComp = new TomBasicCompiler(mVm);

        mLibraries = new ArrayList<Library>();
        mTargets = new ArrayList<Integer>();
        mCurrentTarget = -1;

        mFileEditors = new Vector<FileEditor>();

        // Create and set up the window.
        mFrame = new JFrame(APPLICATION_NAME);
        // Configure window
        mFrame.setIconImage(createImageIcon(ICON_LOGO_SMALL).getImage());
        mFrame.setPreferredSize(new Dimension(696, 480));
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Initialize menu bar
        mMenuBar = setupMenuBar();

        //Initialize other components
        mStatusPanel = new JPanel();
        mLabelStatusInfo = new JLabel("");
        mLabelStatusCursor = new JLabel("0:0");
        JPanel panelStatusInfo = new JPanel(new BorderLayout());
        JPanel panelStatusCursor = new JPanel();

        mStatusPanel.setLayout(new BoxLayout(mStatusPanel, BoxLayout.LINE_AXIS));

        //Add components to Status Panel
        mFrame.add(mStatusPanel, BorderLayout.SOUTH);
        mStatusPanel.add(panelStatusCursor);
        mStatusPanel.add(new JSeparator(JSeparator.VERTICAL));
        mStatusPanel.add(panelStatusInfo);
        panelStatusInfo.add(mLabelStatusInfo, BorderLayout.LINE_START);
        panelStatusCursor.add(mLabelStatusCursor, BorderLayout.CENTER);

        // Setup Status Panel
        mStatusPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
        panelStatusInfo.setBorder(new EmptyBorder(0, 5, 0, 5));
        panelStatusInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        panelStatusCursor.setBorder(new EmptyBorder(0, 5, 0, 5));
        panelStatusCursor.setMaximumSize(new Dimension(96, 24));

        // Toolbar
        mToolBar = new JToolBar();
        mToolBar.setFloatable(false);
        mButtonNew = new JButton(createImageIcon(ICON_NEW));
        mButtonNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewActionExecute();
            }
        });
        mToolBar.add(mButtonNew);
        mButtonOpen = new JButton(createImageIcon(ICON_OPEN));
        mButtonOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenActionExecute();
            }
        });
        mToolBar.add(mButtonOpen);
        mButtonSave = new JButton(createImageIcon(ICON_SAVE));
        mButtonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SaveActionExecute();
            }
        });
        mToolBar.add(mButtonSave);
        mToolBar.addSeparator();
        mButtonRun = new JButton(createImageIcon(ICON_RUN_APP));
        mButtonRun.setToolTipText("Run the program!");
        mButtonRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GoStopActionExecute();
            }
        });
        mToolBar.add(mButtonRun);
        mToolBar.setAlignmentY(1);

        //Initialize syntax highlighting and folding
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/basic4gl", "com.basic4gl.desktop.BasicTokenMaker");

        // Tabs
        mTabControl = new JTabbedPane();
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

        // Add controls to window
        mFrame.add(mToolBar, BorderLayout.NORTH);
        mFrame.add(mTabControl, BorderLayout.CENTER);
        mFrame.setJMenuBar(mMenuBar);

        // Add Window listeners
        mFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // TODO Check if changes should be saved
                ShutDownLibraries();
            }

        });

        // Initialize project and libraries
        InitLibraries();
        ResetProject();

        // Display the window.
        mFrame.pack();
        mFrame.setLocationRelativeTo(null);
        mFrame.setVisible(true);
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

                mLabelStatusCursor.setText((column + 1) + ":" + (row + 1));
            } catch (BadLocationException ex) {
                mLabelStatusCursor.setText(0 + ":" + 0);
                ex.printStackTrace();

            }

        }
    };

    void GoStopActionExecute() {
        switch (mRunMode) {
            case RM_STOPPED:
                if (Compile())
                    Run();
                break;
            case RM_RUNNING:
            case RM_PAUSED:
                Stop();
                break;
        }
    }

    // void __fastcall ExitActionExecute(TObject *Sender);

    void NewActionExecute() {
        //TODO Check if current files are saved

        //Clear file editors
        this.mTabControl.removeAll();
        this.mFileEditors.clear();

        this.addTab();
        this.SynchroniseTabs();
    }

    void OpenActionExecute() {
        FileEditor editor = FileEditor.open(mFrame);
        if (editor != null) {
            //TODO Check if file should open as new tab or project
            //For now just open as new project
            //So... close all current tabs
            this.mTabControl.removeAll();
            this.mFileEditors.clear();

            //And add a new one
            addTab(editor);
        }

        this.SynchroniseTabs();
    }

    void SaveActionExecute() {
        //Save content of current tab
        if (this.mFileEditors.isEmpty() && this.mTabControl.getSelectedIndex() != -1)
            return;
        mFileEditors.get(mTabControl.getSelectedIndex()).save(false);
        this.SynchroniseTabs();

    }

    void SaveAsActionExecute() {
        //Save content of current tab as new file
        if (this.mFileEditors.isEmpty() && this.mTabControl.getSelectedIndex() != -1)
            return;

        mFileEditors.get(mTabControl.getSelectedIndex()).save(true);
        this.SynchroniseTabs();
    }

    public String getVersionInfo() {
        if (DISPLAY_VERSION_INFO)
            return APPLICATION_VERSION;
        return "";
    }


    public void addTab() {
        final FileEditor editor = new FileEditor();
        mFileEditors.add(editor);
        mTabControl.addTab(editor.getTitle(), editor.pane);

        //Allow user to see cursor position
        editor.editorPane.addCaretListener(TrackCaretPosition);
        mLabelStatusCursor.setText(0 + ":" + 0); //Reset label

        //TODO get current editor
        //TODO set colors
        //mFileEditors.get(0).editorPane.setKeywordColor(mKeywords);
    }

    public void addTab(FileEditor editor) {
        mFileEditors.add(editor);
        mTabControl.addTab(editor.getTitle(), editor.pane);

        //Allow user to see cursor position
        editor.editorPane.addCaretListener(TrackCaretPosition);
        mLabelStatusCursor.setText(0 + ":" + 0); //Reset label

        //TODO get current editor
        //TODO set colors
        //mFileEditors.get(0).editorPane.setKeywordColor(mKeywords);
    }

    /**
     * Configure menu bar to display at the top of the window
     */
    private JMenuBar setupMenuBar() {
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;

        // Create the menu bar.
        menuBar = new JMenuBar();

        //File menu
        menu = new JMenu("File");
        menuBar.add(menu);

        // a group of JMenuItems
        menuItem = new JMenuItem("New Program", KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewActionExecute();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("Open Program...", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenActionExecute();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("Save", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SaveActionExecute();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("Save As...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SaveAsActionExecute();
            }
        });
        menu.add(menuItem);
        menu.add(new JSeparator());

        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(menuItem);
        //TODO Implement save all
        /*menu.add(menuItem);
        menuItem = new JMenuItem("Save All");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SaveAllActionExecute();
			}
		});
		menu.add(menuItem);*/
        //TODO Implement export
		/*menuItem = new JMenuItem("Export...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new Exporter().run(mTarget, mLibraries);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		menu.add(menuItem);*/
        //Edit menu
        menu = new JMenu("Edit");
        menuBar.add(menu);

        menuItem = new JMenuItem("Undo");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    if (mFileEditors.get(i).editorPane.canUndo())
                        mFileEditors.get(i).editorPane.undoLastAction();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("Redo");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    if (mFileEditors.get(i).editorPane.canRedo())
                        mFileEditors.get(i).editorPane.redoLastAction();
            }
        });
        menu.add(menuItem);
        menu.add(new JSeparator());
        menuItem = new JMenuItem("Cut");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    mFileEditors.get(i).editorPane.cut();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("Copy");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    mFileEditors.get(i).editorPane.copy();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem("Paste");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    mFileEditors.get(i).editorPane.paste();
            }
        });
        menu.add(menuItem);

        menu.add(new JSeparator());
        menuItem = new JMenuItem("Select All");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size())
                    mFileEditors.get(i).editorPane.selectAll();
            }
        });
        menu.add(menuItem);

        //View menu
        menu = new JMenu("View");
        menuBar.add(menu);
        submenu = new JMenu("Bookmarks");
        menu.add(submenu);
        menuItem = new JMenuItem("Next");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea component;
                int line = -1;
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    mFileEditors.get(i).gotoNextBookmark(true);
                }
            }
        });
        submenu.add(menuItem);
        menuItem = new JMenuItem("Previous");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.SHIFT_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea component;
                int line = -1;
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    mFileEditors.get(i).gotoNextBookmark(false);
                }
            }
        });
        submenu.add(menuItem);
        submenu.add(new JSeparator());
        menuItem = new JMenuItem("Toggle Bookmark");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextArea component;
                int line = -1;
                int i = mTabControl.getSelectedIndex();
                if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
                    component = mFileEditors.get(i).editorPane;
                    try {
                        line = component.getLineOfOffset(component.getCaretPosition());
                        mFileEditors.get(i).pane.getGutter().toggleBookmark(line);

                    } catch (BadLocationException ex) {
                        line = -1;
                        ex.printStackTrace();
                        System.out.println(component.getCaretPosition());
                    }
                }
            }
        });
        submenu.add(menuItem);
        //Application menu
        menu = new JMenu("Application");
        menuBar.add(menu);
        menuItem = new JMenuItem("Project Settings"); //Configure launch and build settings
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectSettingsDialog dialog = new ProjectSettingsDialog(mFrame);
                dialog.setLibraries(mLibraries, mTargets, mCurrentTarget);
                dialog.setVisible(true);
                mCurrentTarget = dialog.getCurrentTarget();
            }
        });
        menu.add(menuItem);

        //Help menu
        menu = new JMenu("Help");
        menuBar.add(menu);
        //menuItem = new JMenuItem("Libraries...");	//List available libraries and functions/constants
        //menuItem.addActionListener(this);
        //menu.add(menuItem);
        menuItem = new JMenuItem("Function List");    //List available functions/constants; replace with Libraries later
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReferenceWindow window = new ReferenceWindow(mFrame);
                window.populate(mComp);
                window.setVisible(true);
            }
        });
        menu.add(menuItem);
        menu.add(new JSeparator());
        menuItem = new JMenuItem("About");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog(mFrame);
            }
        });
        menu.add(menuItem);


        return menuBar;
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

    private void ResetProject() {
        // Clear out the current project and setup a new basic one with a single
        // source-file.

        // Close existing editors
        mTabControl.removeAll();
        this.mFileEditors.clear();

        // Create a default tab
        addTab();

        // Setup tabs
        SynchroniseTabs();

        //Display the editor
        ShowEditor(0);

    }

    private void ShowEditor(int index) {
        // TODO Switch to tab at index
        if (index > -1 && index < mTabControl.getTabCount())
            mTabControl.setSelectedIndex(index);
    }

    private void LoadParser(RSyntaxTextArea editorPane) // Load editor text into parser
    {
        int start, stop; // line offsets
        String line; // line to add
        // Load editor text into parser (appended to bottom)
        try {
            for (int i = 0; i < editorPane.getLineCount(); i++) {
                start = editorPane.getLineStartOffset(i);
                stop = editorPane.getLineEndOffset(i);

                line = editorPane.getText(start, stop - start);

                mComp.Parser().SourceCode().add(line);

            }
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void PutCursor(int line, int col) {
        int offset;
        // CODE HERE!!!
        //Temporary
        this.mLabelStatusInfo.setText("Line: " + (line + 1) + " - " + mLabelStatusInfo.getText());

        if (!(this.mFileEditors.isEmpty() && this.mTabControl.getSelectedIndex() != -1)) {
            try {
                offset = mFileEditors.get(mTabControl.getSelectedIndex()).editorPane.getLineStartOffset(line);
                offset += col;
                mFileEditors.get(mTabControl.getSelectedIndex()).editorPane.grabFocus();
                mFileEditors.get(mTabControl.getSelectedIndex()).editorPane.setCaretPosition(offset);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        // TODO note was not implemented in original source
    }

    private void PutCursorAtIP() {
        // TODO Possibly remove
        // Find IP
        Mutable<Integer> line = new Mutable<Integer>(0), col = new Mutable<Integer>(0);
        mVm.GetIPInSourceCode(line, col);

        // Put cursor there
        PutCursor(line.get(), col.get());
    }

    private void SynchroniseTabs() {
        int i;
        // We always keep at least one editor active
        assert (mFileEditors.size() > 0);

        // Synchronise tabs with currently open files
		/*
		 *
		 * // Remove unused tabs while (Tabs.Tabs.Count > (int)
		 * m_fileEditors.size()) Tabs.Tabs.Delete(Tabs.Tabs.Count - 1);
		 *
		 * // Add new tabs while (Tabs.Tabs.Count < (int) m_fileEditors.size())
		 * Tabs.Tabs.Add("");
		 *
		 * // Set tab text for (int i = 0; i < m_fileEditors.size(); i++)
		 * Tabs.Tabs.operator [](i) = m_fileEditors[i].TabCaption();
		 */
        i = 0;
        for (FileEditor editor : mFileEditors) {

            mTabControl.setTitleAt(i, editor.getTitle());
            i++;
        }
    }

    private boolean CheckModified() {
        // TODO Check if files in editor have changed
        return true;
    }

    // Program control
    private boolean Compile() {

        // Clear source code from parser
        mComp.Parser().SourceCode().clear();

        // Reload in from editors
        // Load included files first (if any)
        // int i;
        for (int i = 1; i < mFileEditors.size(); i++)
            LoadParser(mFileEditors.get(i).editorPane);

        // Load main file last.
        LoadParser(mFileEditors.get(0).editorPane);

        // Compile
        mComp.ClearError();
        mComp.Compile();

        // Return result
        if (mComp.Error()) {

            // Show error
            mLabelStatusInfo.setText(mComp.GetError());
            PutCursor((int) mComp.Line(), (int) mComp.Col());
            return false;
        }
        return true;
    }

    private void Run() {

        // Run from start

        // Reset virtual machine
        mVm.Reset();

        // Setup to start program from start
        SetupForRun();

        // Start running
        Continue();
    }

    private void Stop() {

        // Stop program
        mRunMode = RunMode.RM_STOPPED;
        DeactivateForStop();
        mLabelStatusInfo.setText("Program stopped");

        // Update UI
        mButtonRun.setIcon(createImageIcon(ICON_RUN_APP));
        mButtonRun.setToolTipText("Run the program!");
        mButtonRun.setEnabled(true);

        mButtonNew.setEnabled(true);
        mButtonOpen.setEnabled(true);
        mButtonSave.setEnabled(CheckModified());
        // TODO Implement Save as
        // SaveAsAction.Enabled = true;
    }

    private void Continue() {
        mRunMode = RunMode.RM_RUNNING;
        ActivateForContinue();
        mLabelStatusInfo.setText("Running...");

        // Update UI
        mButtonRun.setIcon(createImageIcon(ICON_STOP_APP));
        mButtonRun.setToolTipText("Stop the program!");
        mButtonRun.setEnabled(true);

        mButtonNew.setEnabled(false);
        mButtonOpen.setEnabled(false);
        mButtonSave.setEnabled(false);
        // TODO Implement Save as
        // SaveAsAction.Enabled = true;
    }

    private void Pause() {
        mRunMode = RunMode.RM_PAUSED;
        DeactivateForStop();
        mLabelStatusInfo.setText("Program paused");

        // Update UI
        mButtonRun.setIcon(createImageIcon(ICON_STOP_APP));
        mButtonRun.setToolTipText("Stop the program!");
        mButtonRun.setEnabled(true);

        mButtonNew.setEnabled(false);
        mButtonOpen.setEnabled(false);
        mButtonSave.setEnabled(false);
        // TODO Implement Save as
        // SaveAsAction.Enabled = true;
    }

    private void SetupForRun() {

        // Setup to run the program from the start.

        // Reset the OpenGL state
        // TODO Implement OpenGL
        // m_glWin.ResetGL();
        // m_glWin.Activate();
        // m_glWin.OpenGLDefaults();
        // m_glWin.SetClosing(false);
        if ((mCurrentTarget > -1 && mCurrentTarget < mTargets.size()) &&
                (mTargets.get(mCurrentTarget) > -1 && mTargets.get(mCurrentTarget) < mLibraries.size()) &&
                mLibraries.get(mTargets.get(mCurrentTarget)) instanceof Target)
            mTarget = (Target) mLibraries.get(mTargets.get(mCurrentTarget));
        else
            mTarget = null;

        if (mTarget != null) {
            mTarget.reset();
            if (!mTarget.isVisible())
                mTarget.activate();
        }
    }

    private void ActivateForContinue() {
        // TODO Implement OpenGL
        // Re-activate to continue the program
        // m_glWin.Show();
        // m_glWin.Activate();
        //mTarget.activate();
        if (mTarget != null) {
            mTarget.reset();
            mTarget.show(new DebugCallback());
        }

    }

    private void DeactivateForStop() {
        // TODO Implement OpenGL
        // Deactivate to stop the program

        if (mTarget != null) {
            //if (mTarget.isFullscreen())
            mTarget.hide();
        }
    }

    private void InitLibraries() {

        // Create OpenGL window
        // TODO Implement OpenGL
        //TODO Move settings to target
        // m_glWin = null;
        // m_glText = null;

        // Default settings
        // boolean fullScreen = false, border = true;
        // int width = 640, height = 480, bpp = 0;
        // ResetGLModeType resetGLMode = RGM_RESETSTATE;

        // Create window
		/*
		 * m_glWin = new glTextGridWindow ( fullScreen, border, width, height,
		 * bpp, "Basic4GL", resetGLMode);
		 *
		 * // Check for errors if (m_glWin.Error ()) { MessageDlg ( (AnsiString)
		 * m_glWin.GetError().c_str(), mtError, TMsgDlgButtons() << mbOK, 0);
		 * Application.Terminate (); return; } m_glWin.Hide ();
		 *
		 * // Create OpenGL text grid m_glText = new glSpriteEngine (
		 * (ExtractFilePath (Application.ExeName) + "charset.png").c_str (),
		 * &m_files, 25, 40, 16, 16);
		 *
		 * // Check for errors if (m_glText.Error ()) { MessageDlg (
		 * (AnsiString) + m_glText.GetError ().c_str (), mtError,
		 * TMsgDlgButtons() << mbOK, 0); Application.Terminate (); return; }
		 * m_glWin.SetTextGrid (m_glText);
		 */

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
        mLibraries.add(new DesktopGL(mVm));

        //TODO Add more libraries
        int i = 0;
        for (Library lib : mLibraries) {
            mComp.AddConstants(lib.constants());
            mComp.AddFunctions(lib.functions(), lib.specs());
            if (lib.isTarget()) {
                mTargets.add(i);
            }
            i++;
        }
        //Set default target
        if (mTargets.size() > 0)
            mCurrentTarget = 0;

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

    private void ShutDownLibraries() {

        // Clear virtual machine state
        mVm.Clr();

        // TODO Implement OpenGL
        // Free text display
        // if (m_glText != NULL)
        // delete m_glText;

        // Free the openGL window
        // if (m_glWin != NULL)
        // delete m_glWin;

        //Clear Libraries
        mLibraries.clear();
        mTargets.clear();
        mCurrentTarget = -1;
    }


    public class DebugCallback implements TaskCallback {

        @Override
        public void complete(boolean success, String message) {
            Stop();
            if (success) {
                mLabelStatusInfo.setText(message);
            } else {
                mLabelStatusInfo.setText(mVm.GetError());
                PutCursorAtIP();
            }
        }

    }

}
