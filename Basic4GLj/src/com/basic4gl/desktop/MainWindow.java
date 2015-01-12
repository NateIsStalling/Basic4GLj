package com.basic4gl.desktop;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.targets.desktopgl.DesktopGL;
import com.basic4gl.lib.util.Library;
import com.basic4gl.lib.util.Target;
import com.basic4gl.lib.util.TaskCallback;
import com.basic4gl.vm.TomVM;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;

public class MainWindow {

	//Window Constants
	private static final boolean DISPLAY_VERSION_INFO = true;
	private static final String VERSION_NAME =  "Alpha 0.1.2";

	enum RunMode {
		RM_STOPPED, RM_PAUSED, RM_RUNNING
	};

	// Window
	JFrame mFrame;
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
	JLabel mLabelRow; // Cursor Row
	JLabel mLabelCol; // Cursor Column
	JLabel mLabelStatus; // Compiler/VM Status

	//TODO add documentation when mouse hovers over text
	HashMap<String, String> mKeywordTips;

	// Compiler and VM
	private TomBasicCompiler m_comp;
	private TomVM m_vm;

	// Editors
	Vector<FileEditor> mFileEditors;

	// State
	private RunMode m_runMode;

	//Window to run Basic4GL virtual machine in
	private Target mTarget;

	//Libraries
	private java.util.List<Library> mLibraries;
	private java.util.List<Integer> mTargets;		//Indexes of libraries that can be launch targets
	private int mCurrentTarget;			//Index value of target in mTargets

	public static void main(String [] args)
	{
		new MainWindow();
	}

	public MainWindow() {
		m_runMode = RunMode.RM_STOPPED;
		m_vm = new TomVM(null);
		m_comp = new TomBasicCompiler(m_vm);

		mLibraries = new ArrayList<Library>();
		mTargets = new ArrayList<Integer>();
		mCurrentTarget = -1;

		mFileEditors = new Vector<FileEditor>();

		// Create and set up the window.
		mFrame = new JFrame("Basic4GLj" + (!getVersionInfo().equals("") ? " - " + getVersionInfo(): ""));
		// Configure window
		mFrame.setPreferredSize(new Dimension(696, 480));
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mMenuBar = setupMenuBar();

		// Setup Status Panel
		mStatusPanel = new JPanel();
		mStatusPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		mFrame.add(mStatusPanel, BorderLayout.SOUTH);
		mStatusPanel.setPreferredSize(new Dimension(mFrame.getWidth(), 24));
		mStatusPanel.setLayout(new BoxLayout(mStatusPanel, BoxLayout.X_AXIS));
		// TODO Add mLabelRow and mLabelCol to status bar
		mLabelStatus = new JLabel("");
		mLabelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
		mStatusPanel.add(mLabelStatus, BorderLayout.CENTER);
		// Add controls

		// Toolbar
		mToolBar = new JToolBar();
		mToolBar.setFloatable(false);
		mButtonNew = new JButton(createImageIcon("images/ImgNew.png"));
		mButtonNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NewActionExecute();
			}
		});
		mToolBar.add(mButtonNew);
		mButtonOpen = new JButton(createImageIcon("images/ImgOpen.png"));
		mButtonOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OpenActionExecute();
			}
		});
		mToolBar.add(mButtonOpen);
		mButtonSave = new JButton(createImageIcon("images/ImgSave.png"));
		mButtonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SaveActionExecute();
			}
		});
		mToolBar.add(mButtonSave);
		mToolBar.addSeparator();
		mButtonRun = new JButton(createImageIcon("images/ImgGo.png"));
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
	void GoStopActionExecute() {
		switch (m_runMode) {
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
		if (editor != null){
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

	public String getVersionInfo(){
		if (DISPLAY_VERSION_INFO)
			return VERSION_NAME;
		return "";
	}



	public void addTab() {
		FileEditor editor = new FileEditor();
		mFileEditors.add(editor);
		mTabControl.addTab(editor.getTitle(), editor.pane);

		//TODO get current editor
		//TODO set colors
		//mFileEditors.get(0).editorPane.setKeywordColor(mKeywords);
	}
	public void addTab(FileEditor editor) {
		mFileEditors.add(editor);
		mTabControl.addTab(editor.getTitle(), editor.pane);

		//TODO get current editor
		//TODO set colors
		//mFileEditors.get(0).editorPane.setKeywordColor(mKeywords);
	}
	/**
	 * Configure menu bar to display at the top of the window
	 */
	private JMenuBar setupMenuBar() {
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;

		// Create the menu bar.
		menuBar = new JMenuBar();

		//File menu
		menu = new JMenu("File");
		menuBar.add(menu);

		// a group of JMenuItems
		menuItem = new JMenuItem("New Program",KeyEvent.VK_N);
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

		/*
		menu = new JMenu("Help");
		menuBar.add(menu);
		menuItem = new JMenuItem("Libraries...");	//List available libraries and functions/constants
		menuItem.addActionListener(this);
		menu.add(menuItem);
		 */


		return menuBar;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
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
		// TODO read JTextPane line by line
		// Load editor text into parser (appended to bottom)
		try {
			for (int i = 0; i < editorPane.getLineCount(); i++) {
				start = editorPane.getLineStartOffset(i);
				stop = editorPane.getLineEndOffset(i);

				line = editorPane.getText(start, stop - start);

				m_comp.Parser().SourceCode().add(line);

			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void PutCursor(int line, int col) {
		// CODE HERE!!!
		//Temporary
		this.mLabelStatus.setText(mLabelStatus.getText() + " - Line: " + (line + 1));
		// TODO was not implemented in original source
	}

	private void PutCursorAtIP() {
		// TODO Possibly remove
		// Find IP
		Integer line = 0, col = 0;
		m_vm.GetIPInSourceCode(line, col);

		// Put cursor there
		PutCursor(line, col);
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
		for (FileEditor editor: mFileEditors){

			mTabControl.setTitleAt(i, editor.getTitle());
			i++;
		}

		// Set the application caption
		if (mFileEditors.get(0).getTitle().equals(FileEditor.DEFAULT_NAME) ||mFileEditors.get(0).getTitle().equals(""))
			mFrame.setTitle("Basic4GLj" + (!getVersionInfo().equals("") ? " - " + getVersionInfo(): ""));
		else
			mFrame.setTitle("Basic4GLj - " + mFileEditors.get(0).getTitle() );
	}

	private boolean CheckModified() {
		// TODO Check if files in editor have changed
		return true;
	}

	// Program control
	private boolean Compile() {

		// Clear source code from parser
		m_comp.Parser().SourceCode().clear();

		// Reload in from editors
		// Load included files first (if any)
		// int i;
		for (int i = 1; i < mFileEditors.size(); i++)
			LoadParser(mFileEditors.get(i).editorPane);

		// Load main file last.
		LoadParser(mFileEditors.get(0).editorPane);

		// Compile
		m_comp.ClearError();
		m_comp.Compile();

		// Return result
		if (m_comp.Error()) {

			// Show error
			mLabelStatus.setText(m_comp.GetError());
			PutCursor((int) m_comp.Line(), (int) m_comp.Col());
			return false;
		}
		return true;
	}

	private void Run() {

		// Run from start

		// Reset virtual machine
		m_vm.Reset();

		// Setup to start program from start
		SetupForRun();

		// Start running
		Continue();
	}

	private void Stop() {

		// Stop program
		m_runMode = RunMode.RM_STOPPED;
		DeactivateForStop();
		mLabelStatus.setText("Program stopped");

		// Update UI
		mButtonRun.setIcon(createImageIcon("images/ImgGo.png"));
		mButtonRun.setToolTipText("Run the program!");
		mButtonRun.setEnabled(true);

		mButtonNew.setEnabled(true);
		mButtonOpen.setEnabled(true);
		mButtonSave.setEnabled(CheckModified());
		// TODO Implement Save as
		// SaveAsAction.Enabled = true;
	}

	private void Continue() {
		m_runMode = RunMode.RM_RUNNING;
		ActivateForContinue();
		mLabelStatus.setText("Running...");

		// Update UI
		mButtonRun.setIcon(createImageIcon("images/ImgStop.png"));
		mButtonRun.setToolTipText("Stop the program!");
		mButtonRun.setEnabled(true);

		mButtonNew.setEnabled(false);
		mButtonOpen.setEnabled(false);
		mButtonSave.setEnabled(false);
		// TODO Implement Save as
		// SaveAsAction.Enabled = true;
	}

	private void Pause() {
		m_runMode = RunMode.RM_PAUSED;
		DeactivateForStop();
		mLabelStatus.setText("Program paused");

		// Update UI
		mButtonRun.setIcon(createImageIcon("images/ImgStop.png"));
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
			mTarget  =  (Target)mLibraries.get(mTargets.get(mCurrentTarget));
		else
			mTarget = null;

		if (mTarget != null){
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
		if (mTarget != null){
			mTarget.reset();
			mTarget.show(new DebugCallback());
		}

	}

	private void DeactivateForStop() {
		// TODO Implement OpenGL
		// Deactivate to stop the program

		if (mTarget != null){
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
		 * InitTomStdBasicLib (m_comp); // Standard library
		 * InitTomWindowsBasicLib (m_comp, &m_files); // Windows specific
		 * library InitTomOpenGLBasicLib (m_comp, m_glWin, &m_files); // OpenGL
		 * InitTomTextBasicLib (m_comp, m_glWin, m_glText); // Basic
		 * text/sprites InitGLBasicLib_gl (m_comp); InitGLBasicLib_glu (m_comp);
		 * InitTomJoystickBasicLib (m_comp, m_glWin); // Joystick support
		 * InitTomTrigBasicLib (m_comp); // Trigonometry library
		 * InitTomFileIOBasicLib (m_comp, &m_files); // File I/O library
		 * InitTomNetBasicLib (m_comp); // Networking
		 */

		//TODO Load libraries dynamically
		mLibraries.add(new com.basic4gl.lib.standard.Standard());
		mLibraries.add(new DesktopGL(m_vm));

		//TODO Add more libraries
		int i = 0;
		for (Library lib: mLibraries){
			m_comp.AddConstants(lib.constants());
			m_comp.AddFunctions(lib.functions(), lib.specs());
			if (lib.isTarget()){
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
		for (String s: m_comp.m_reservedWords)
			BasicTokenMaker.mReservedWords.add(s);

		for (String s: m_comp.Constants().keySet())
			BasicTokenMaker.mConstants.add(s);

		for (String s: m_comp.m_functionIndex.keySet())
			BasicTokenMaker.mFunctions.add(s);

		for (String s: m_comp.getBinaryOperators())
			BasicTokenMaker.mOperators.add(s);
		for (String s: m_comp.getUnaryOperators())
			BasicTokenMaker.mOperators.add(s);

	}

	private void ShutDownLibraries() {

		// Clear virtual machine state
		m_vm.Clr();

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
			if (success){
				mLabelStatus.setText(message);
			} else {
				mLabelStatus.setText(m_vm.GetError());
				PutCursorAtIP();
			}
		}

	}

}
