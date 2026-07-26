package com.basic4gl.desktop.editor;

import com.basic4gl.desktop.language.Basic4GLFoldParser;
import com.basic4gl.desktop.util.EditorUtil;
import com.basic4gl.desktop.util.IFileManager;
import com.basic4gl.desktop.util.SwingIconUtil;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.search.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.*;

public class FileEditor implements SearchListener {
    public static final String DEFAULT_NAME = "[Unnamed]";

    private static final String ACTION_NEXT_BOOKMARK = "RTA.NextBookmarkAction";
    private static final String ACTION_PREV_BOOKMARK = "RTA.PrevBookmarkAction";
    private static final String ACTION_TOGGLE_BOOKMARK = "RTA.ToggleBookmarkAction";

    private static final String ACTION_NEXT_BREAKPOINT = "RTA.NextBreakpointAction";
    private static final String ACTION_PREV_BREAKPOINT = "RTA.PrevBreakpointAction";
    private static final String ACTION_TOGGLE_BREAKPOINT = "RTA.ToggleBreakpointAction";

    private static final String IMAGE_DIRECTORY = "images/";
    private static final String THEME_DIRECTORY = IMAGE_DIRECTORY + "material/";
    private static final String ICON_BOOKMARK = THEME_DIRECTORY + "bookmark.png";
    private static final String ICON_BREAK_PT = THEME_DIRECTORY + "BreakPt.png";

    static {
        FoldParserManager.get().addFoldParserMapping("text/basic4gl", new Basic4GLFoldParser());
    }

    private final IFileManager fileManager;
    private final IToggleBreakpointListener toggleBreakpointListener;

    private final IFileEditorActionListener actionListener;
    private final FindToolBar findToolBar;
    private final ReplaceToolBar replaceToolBar;
    private final CollapsibleSectionPanel csp;
    private final DualGutterScrollPane scrollPane;
    private final RSyntaxTextArea editorPane;

    private final JPopupMenu gutterPopup;
    private int gutterPopupLine = -1;

    // private Map<Integer, Object> lineHighlights; //Highlight lines with breakpoints

    private String fileName; // Filename without path
    private String filePath; // Full path including name
    private boolean isModified;
    private boolean isSaved; // File exists on system

    public FileEditor(
            IFileEditorActionListener actionListener,
            IFileManager fileManager,
            IToggleBreakpointListener toggleBreakpointListener,
            LinkGenerator linkGenerator,
            SearchContext searchContext) {

        SyntaxScheme scheme;

        this.actionListener = actionListener;
        this.fileManager = fileManager;
        this.toggleBreakpointListener = toggleBreakpointListener;

        fileName = "";
        filePath = "";
        isModified = false;
        isSaved = false;

        editorPane = new RSyntaxTextArea(20, 60);
        editorPane.setSyntaxEditingStyle("text/basic4gl");
        // Enable code folding
        editorPane.setCodeFoldingEnabled(true);
        if (linkGenerator != null) {
            editorPane.setHyperlinksEnabled(true);
            editorPane.setLinkScanningMask(EditorUtil.getLinkScanningMask());
            editorPane.setLinkGenerator(linkGenerator);
        }

        scrollPane = new DualGutterScrollPane(editorPane);

        // Add shortcut keys for bookmarks and breakpoints

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        InputMap inputMap = editorPane.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), ACTION_NEXT_BOOKMARK);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_MASK), ACTION_PREV_BOOKMARK);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, toolkit.getMenuShortcutKeyMask()), ACTION_TOGGLE_BOOKMARK);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), ACTION_NEXT_BREAKPOINT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK), ACTION_PREV_BREAKPOINT);
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, toolkit.getMenuShortcutKeyMask()), ACTION_TOGGLE_BREAKPOINT);

        // Bookmarks and breakpoints live in dedicated icon columns rather than
        // the gutter's built-in bookmark header, so wire up custom actions that
        // delegate to this editor's handling for each.
        ActionMap actionMap = editorPane.getActionMap();
        actionMap.put(ACTION_NEXT_BOOKMARK, new NextBookmarkAction(ACTION_NEXT_BOOKMARK, this, true));
        actionMap.put(ACTION_PREV_BOOKMARK, new NextBookmarkAction(ACTION_PREV_BOOKMARK, this, false));
        actionMap.put(ACTION_TOGGLE_BOOKMARK, new ToggleBookmarkAction(ACTION_TOGGLE_BOOKMARK, this));
        actionMap.put(ACTION_NEXT_BREAKPOINT, new NextBreakpointAction(ACTION_NEXT_BREAKPOINT, this, true));
        actionMap.put(ACTION_PREV_BREAKPOINT, new NextBreakpointAction(ACTION_PREV_BREAKPOINT, this, false));
        actionMap.put(ACTION_TOGGLE_BREAKPOINT, new ToggleBreakpointAction(ACTION_TOGGLE_BREAKPOINT, this));

        // Bookmark column: leftmost dedicated icon row header. A left click
        // toggles the bookmark; the column auto-hides when it holds no
        // bookmarks.
        final HoverIconRowHeader bookmarkHeader = scrollPane.getBookmarkHeader();
        bookmarkHeader.setBookmarkIcon(SwingIconUtil.createImageIcon(ICON_BOOKMARK));
        bookmarkHeader.setBookmarkingEnabled(true);
        bookmarkHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                refreshBookmarkColumn();
            }
        });
        scrollPane.setBookmarkColumnVisible(false);

        // Breakpoint column: the second dedicated icon row header. Enabling
        // bookmarking lets a left click toggle the breakpoint icon; the mouse
        // listener notifies the debugger of the change.
        final HoverIconRowHeader breakpointHeader = scrollPane.getBreakpointHeader();
        breakpointHeader.setBookmarkIcon(SwingIconUtil.createImageIcon(ICON_BREAK_PT));
        breakpointHeader.setBookmarkingEnabled(true);
        breakpointHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                try {
                    int offs = editorPane.viewToModel(e.getPoint());
                    int line = offs > -1 ? editorPane.getLineOfOffset(offs) : -1;
                    if (line > -1) {
                        FileEditor.this.toggleBreakpointListener.onToggleBreakpoint(getFilePath(), line);
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Right-click context menu (shared by both gutter columns) to toggle
        // bookmarks and breakpoints on the clicked line.
        gutterPopup = new JPopupMenu();
        JMenuItem toggleBreakpointItem = new JMenuItem("Toggle Breakpoint");
        toggleBreakpointItem.addActionListener(e -> {
            if (gutterPopupLine > -1) {
                toggleBreakpointAtLine(gutterPopupLine);
            }
        });
        JMenuItem toggleBookmarkItem = new JMenuItem("Toggle Bookmark");
        toggleBookmarkItem.addActionListener(e -> {
            if (gutterPopupLine > -1) {
                toggleBookmarkAtLine(gutterPopupLine);
            }
        });
        gutterPopup.add(toggleBreakpointItem);
        gutterPopup.add(toggleBookmarkItem);

        MouseAdapter gutterPopupListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowGutterPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowGutterPopup(e);
            }
        };
        bookmarkHeader.addMouseListener(gutterPopupListener);
        breakpointHeader.addMouseListener(gutterPopupListener);

        // Enable code folding for Basic4GL syntax
        final Gutter gutter = scrollPane.getGutter();
        gutter.setFoldIndicatorEnabled(true);
        scrollPane.setFoldIndicatorEnabled(true);
        // Modern fold visuals: show collapsed markers on hover and subtle armed highlight.
        gutter.setFoldIndicatorStyle(FoldIndicatorStyle.MODERN);
        gutter.setExpandedFoldRenderStrategy(ExpandedFoldRenderStrategy.ON_HOVER);
        gutter.setShowCollapsedRegionToolTips(true);
        gutter.setSpacingBetweenLineNumbersAndFoldIndicator(4);
        gutter.setArmedFoldBackground(new Color(232, 244, 255));
        gutter.setFoldIndicatorArmedForeground(new Color(60, 90, 160));

        // Force gutter visibility update
        editorPane.setCodeFoldingEnabled(true);
        scrollPane.revalidate();
        scrollPane.repaint();

        // Create toolbars and tie their search contexts together also.
        findToolBar = new FindToolBar(this);
        findToolBar.setSearchContext(searchContext);
        replaceToolBar = new ReplaceToolBar(this);
        replaceToolBar.setSearchContext(searchContext);

        csp = new CollapsibleSectionPanel();
        csp.add(scrollPane);

        csp.addBottomComponent(findToolBar);
        csp.addBottomComponent(replaceToolBar);

        // Configure popup context menu
        JPopupMenu popup = editorPane.getPopupMenu();
        popup.remove(popup.getComponents().length - 1); // Remove folding option
        popup.remove(popup.getComponents().length - 1); // Remove separator

        // Set default color scheme
        scheme = editorPane.getSyntaxScheme();
        scheme.setStyle(TokenTypes.IDENTIFIER, new Style(new Color(0, 0, 128))); // Normal text
        scheme.setStyle(TokenTypes.LITERAL_NUMBER_DECIMAL_INT, new Style(new Color(0, 0, 128)));

        scheme.setStyle(TokenTypes.COMMENT_EOL, new Style(new Color(101, 124, 167))); // Comment
        scheme.setStyle(TokenTypes.RESERVED_WORD, new Style(new Color(0, 0, 255))); // Keyword
        scheme.setStyle(TokenTypes.RESERVED_WORD_2, new Style(new Color(0, 128, 255))); // Constants
        scheme.setStyle(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, new Style(new Color(0, 128, 0))); // String
        scheme.setStyle(TokenTypes.FUNCTION, new Style(new Color(255, 0, 0))); // Function
        scheme.setStyle(TokenTypes.OPERATOR, new Style(new Color(128, 0, 128))); // Operator
    }

    public JPanel getContentPane() {
        return csp;
    }

    @Override
    public String getSelectedText() {
        return editorPane.getSelectedText();
    }

    /**
     * Listens for events from our search dialogs and actually does the dirty
     * work.
     */
    @Override
    public void searchEvent(SearchEvent e) {
        RTextArea textArea = editorPane;
        SearchContext context = e.getSearchContext();
        SearchEvent.Type type = e.getType();
        SearchResult result = null;
        String text = "";

        try {
            switch (type) {
                default: // Prevent FindBugs warning later
                case MARK_ALL:
                    result = SearchEngine.markAll(textArea, context);
                    break;
                case FIND:
                    result = SearchEngine.find(textArea, context);
                    if (!result.wasFound() || result.isWrapped()) {
                        UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    }
                    break;
                case REPLACE:
                    if (textArea.isEditable()) {
                        result = SearchEngine.replace(textArea, context);
                        if (!result.wasFound() || result.isWrapped()) {
                            UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "File is read-only.");
                    }
                    break;
                case REPLACE_ALL:
                    if (textArea.isEditable()) {
                        result = SearchEngine.replaceAll(textArea, context);
                        JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
                    } else {
                        JOptionPane.showMessageDialog(null, "File is read-only.");
                    }
                    break;
            }

            if (result != null) {
                if (result.wasFound()) {
                    text = "Text found; occurrences marked: " + result.getMarkedCount();
                } else if (type == SearchEvent.Type.MARK_ALL) {
                    if (result.getMarkedCount() > 0) {
                        text = "Occurrences marked: " + result.getMarkedCount();
                    } else {
                        text = "";
                    }
                } else {
                    text = "Text not found";
                }
            }
            actionListener.onSearchResult(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getTitle() {
        String result;
        result = (fileName.isEmpty() ? DEFAULT_NAME : fileName).toLowerCase();

        // Append asterisk if modified
        if (isModified) {
            result += " *";
        }

        return result;
    }

    public void setFileName(String filename) {
        if (filename != null && !filename.isBlank()) {
            this.fileName = filename;
        } else {
            this.fileName = "";
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFile() {
        return !filePath.isEmpty() ? new File(filePath) : null;
    }

    public String getShortFilename() {
        return !fileName.isEmpty() ? new File(fileName).getName() : DEFAULT_NAME.toLowerCase();
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified() {
        isModified = true;
    }

    public boolean save(boolean saveAs, String parentDirectory) {
        boolean save = true;
        if (saveAs || !isSaved || filePath.isEmpty()) {
            JFileChooser dialog = new JFileChooser();
            dialog.setAcceptAllFileFilterUsed(false);
            dialog.addChoosableFileFilter(new FileNameExtensionFilter("GLBasic Program (*.gb)", "gb"));
            dialog.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
            dialog.setAcceptAllFileFilterUsed(true); // Move "All Files" to bottom of filter list
            dialog.setCurrentDirectory(new File(fileManager.getCurrentDirectory()));
            dialog.setSelectedFile(new File(fileName));
            int result = dialog.showSaveDialog(scrollPane);

            if (result == JFileChooser.APPROVE_OPTION) {
                String path = dialog.getSelectedFile().getAbsolutePath();
                if (dialog.getFileFilter() instanceof FileNameExtensionFilter) {
                    // Append extension if needed
                    if (((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions().length > 0) {
                        String extension = ((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions()[0];
                        if (!path.endsWith("." + extension)) {
                            path += "." + extension;
                        }
                    }
                }
                filePath = path;
                fileName = new File(path).getName();
            } else {
                save = false;
            }
        }
        if (save) {
            try {
                FileWriter fw = new FileWriter(filePath, false);
                editorPane.write(fw);
                fw.close();
                isModified = false;
                isSaved = true;
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public static FileEditor open(
            Frame parent,
            IFileEditorActionListener actionListener,
            IFileManager fileManager,
            IToggleBreakpointListener listener,
            LinkGenerator linkGenerator,
            SearchContext searchContext) {

        FileEditor editor = null;
        JFileChooser dialog = new JFileChooser();
        dialog.setAcceptAllFileFilterUsed(false);
        dialog.addChoosableFileFilter(new FileNameExtensionFilter("GLBasic Program (*.gb)", "gb"));
        dialog.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
        dialog.setAcceptAllFileFilterUsed(true); // Move "All Files" to bottom of filter list
        dialog.setCurrentDirectory(new File(fileManager.getCurrentDirectory()));
        int result = dialog.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            editor = new FileEditor(actionListener, fileManager, listener, linkGenerator, searchContext);
            try {
                FileReader fr = new FileReader(dialog.getSelectedFile().getAbsolutePath());
                editor.filePath = dialog.getSelectedFile().getAbsolutePath();
                editor.fileName = dialog.getSelectedFile().getName();
                editor.editorPane.read(fr, null);
                fr.close();
                editor.isSaved = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            editor.editorPane.discardAllEdits(); // Otherwise 'undo' will clear the text area after loading
        }

        return editor;
    }

    public static FileEditor open(
            File file,
            IFileEditorActionListener actionListener,
            IFileManager fileManager,
            IToggleBreakpointListener listener,
            LinkGenerator linkGenerator,
            SearchContext searchContext) {

        FileEditor editor = null;
        editor = new FileEditor(actionListener, fileManager, listener, linkGenerator, searchContext);
        if (file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                editor.filePath = file.getAbsolutePath();
                editor.fileName = file.getName();
                editor.editorPane.read(fr, null);
                fr.close();
                editor.isSaved = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            editor.filePath = file.getAbsolutePath();
            editor.fileName = file.getName();
            editor.isSaved = false;
        }
        editor.editorPane.discardAllEdits(); // Otherwise 'undo' will clear the text area after loading

        return editor;
    }

    public void gotoNextBookmark(boolean forward) {
        // Copied from org.fife.ui.rtextarea.RTextAreaEditorKit.NextBookmarkAction
        IconRowHeader gutter = scrollPane.getBookmarkHeader();
        if (gutter != null) {

            try {

                GutterIconInfo[] bookmarks = gutter.getBookmarks();
                if (bookmarks.length == 0) {
                    UIManager.getLookAndFeel().provideErrorFeedback(editorPane);
                    return;
                }

                GutterIconInfo moveTo = null;
                int curLine = editorPane.getCaretLineNumber();

                if (forward) {
                    for (int i = 0; i < bookmarks.length; i++) {
                        GutterIconInfo bookmark = bookmarks[i];
                        int offs = bookmark.getMarkedOffset();
                        int line = editorPane.getLineOfOffset(offs);
                        if (line > curLine) {
                            moveTo = bookmark;
                            break;
                        }
                    }
                    if (moveTo == null) { // Loop back to beginning
                        moveTo = bookmarks[0];
                    }
                } else {
                    for (int i = bookmarks.length - 1; i >= 0; i--) {
                        GutterIconInfo bookmark = bookmarks[i];
                        int offs = bookmark.getMarkedOffset();
                        int line = editorPane.getLineOfOffset(offs);
                        if (line < curLine) {
                            moveTo = bookmark;
                            break;
                        }
                    }
                    if (moveTo == null) { // Loop back to end
                        moveTo = bookmarks[bookmarks.length - 1];
                    }
                }

                int offs = moveTo.getMarkedOffset();
                if (editorPane instanceof RSyntaxTextArea) {
                    RSyntaxTextArea rsta = (RSyntaxTextArea) editorPane;
                    if (rsta.isCodeFoldingEnabled()) {
                        rsta.getFoldManager().ensureOffsetNotInClosedFold(offs);
                    }
                }
                int line = editorPane.getLineOfOffset(offs);
                offs = editorPane.getLineStartOffset(line);
                editorPane.setCaretPosition(offs);

            } catch (BadLocationException ble) { // Never happens
                UIManager.getLookAndFeel().provideErrorFeedback(editorPane);
                ble.printStackTrace();
            }
        }
    }

    public void toggleFindToolBar(boolean replace) {
        FindToolBar findToolBar = this.findToolBar;
        if (replace) {
            findToolBar = this.replaceToolBar;
        }

        // Toggle search toolbar
        if (csp.getDisplayedBottomComponent() == findToolBar) {
            csp.hideBottomComponent();
        } else {
            csp.showBottomComponent(findToolBar);
        }
    }

    public void toggleBookmark() {
        try {
            int line = editorPane.getLineOfOffset(editorPane.getCaretPosition());
            toggleBookmarkAtLine(line);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
            System.out.println(editorPane.getCaretPosition());
        }
    }

    /**
     * Toggles whether the given line has a bookmark and keeps the bookmark
     * column's visibility in sync.
     *
     * @param line The zero-based line to toggle.
     */
    public void toggleBookmarkAtLine(int line) {
        try {
            scrollPane.getBookmarkHeader().toggleBookmark(line);
            refreshBookmarkColumn();
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Shows the shared gutter context menu on a popup-trigger event, targeting
     * the line under the cursor.
     */
    private void maybeShowGutterPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        try {
            int offs = editorPane.viewToModel(e.getPoint());
            gutterPopupLine = offs > -1 ? editorPane.getLineOfOffset(offs) : -1;
        } catch (BadLocationException ex) {
            gutterPopupLine = -1;
        }
        if (gutterPopupLine > -1) {
            gutterPopup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * Shows the bookmark column while it holds at least one bookmark, and hides
     * it otherwise so it doesn't take up gutter space when empty.
     */
    private void refreshBookmarkColumn() {
        boolean hasBookmarks = scrollPane.getBookmarkHeader().getBookmarks().length > 0;
        scrollPane.setBookmarkColumnVisible(hasBookmarks);
    }

    public void gotoNextBreakpoint(boolean forward) {
        // Copied from org.fife.ui.rtextarea.RTextAreaEditorKit.NextBookmarkAction
        IconRowHeader gutter = scrollPane.getBreakpointHeader();
        if (gutter != null) {

            try {

                GutterIconInfo[] bookmarks = gutter.getBookmarks();
                if (bookmarks.length == 0) {
                    UIManager.getLookAndFeel().provideErrorFeedback(editorPane);
                    return;
                }

                GutterIconInfo moveTo = null;
                int curLine = editorPane.getCaretLineNumber();

                if (forward) {
                    for (int i = 0; i < bookmarks.length; i++) {
                        GutterIconInfo bookmark = bookmarks[i];
                        int offs = bookmark.getMarkedOffset();
                        int line = editorPane.getLineOfOffset(offs);
                        if (line > curLine) {
                            moveTo = bookmark;
                            break;
                        }
                    }
                    if (moveTo == null) { // Loop back to beginning
                        moveTo = bookmarks[0];
                    }
                } else {
                    for (int i = bookmarks.length - 1; i >= 0; i--) {
                        GutterIconInfo bookmark = bookmarks[i];
                        int offs = bookmark.getMarkedOffset();
                        int line = editorPane.getLineOfOffset(offs);
                        if (line < curLine) {
                            moveTo = bookmark;
                            break;
                        }
                    }
                    if (moveTo == null) { // Loop back to end
                        moveTo = bookmarks[bookmarks.length - 1];
                    }
                }

                int offs = moveTo.getMarkedOffset();
                if (editorPane instanceof RSyntaxTextArea) {
                    RSyntaxTextArea rsta = (RSyntaxTextArea) editorPane;
                    if (rsta.isCodeFoldingEnabled()) {
                        rsta.getFoldManager().ensureOffsetNotInClosedFold(offs);
                    }
                }
                int line = editorPane.getLineOfOffset(offs);
                offs = editorPane.getLineStartOffset(line);
                editorPane.setCaretPosition(offs);

            } catch (BadLocationException ble) { // Never happens
                UIManager.getLookAndFeel().provideErrorFeedback(editorPane);
                ble.printStackTrace();
            }
        }
    }

    public void toggleBreakpoint() {
        try {
            int line = editorPane.getLineOfOffset(editorPane.getCaretPosition());
            toggleBreakpointAtLine(line);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
            System.out.println(editorPane.getCaretPosition());
        }
    }

    /**
     * Toggles whether the given line has a breakpoint, updating both the
     * breakpoint icon and the debugger.
     *
     * @param line The zero-based line to toggle.
     */
    public void toggleBreakpointAtLine(int line) {
        try {
            scrollPane.getBreakpointHeader().toggleBookmark(line);
            toggleBreakpointListener.onToggleBreakpoint(getFilePath(), line);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<Integer> getBreakpoints() {
        ArrayList<Integer> points = new ArrayList<>();

        IconRowHeader gutter = scrollPane.getBreakpointHeader();
        if (gutter != null) {
            GutterIconInfo[] bookmarks = gutter.getBookmarks();
            for (GutterIconInfo info : bookmarks) {
                try {
                    int line = editorPane.getLineOfOffset(info.getMarkedOffset());
                    points.add(line);
                    System.out.println("Breakpoint at line: " + info.getMarkedOffset());
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return points;
    }

    public JTextArea getEditorPane() {
        return editorPane;
    }

    public void refreshSyntaxHighlighting() {
        // RSyntaxTextArea does not expose a direct TokenMaker invalidation API.
        // Flip style away and back to force a fresh token maker/tokenization pass.
        editorPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        editorPane.setSyntaxEditingStyle("text/basic4gl");

        editorPane.invalidate();
        editorPane.revalidate();
        editorPane.repaint();
    }

    public boolean canRedo() {
        return editorPane.canRedo();
    }

    public boolean canUndo() {
        return editorPane.canUndo();
    }

    public void redoLastAction() {
        editorPane.redoLastAction();
    }

    public void undoLastAction() {
        editorPane.undoLastAction();
    }

    /**
     * Moves the caret to the next (or previous) bookmark.
     */
    public static class NextBookmarkAction extends RecordableTextAction {
        private final FileEditor fileEditor;
        private final boolean forward;

        public NextBookmarkAction(String name, FileEditor fileEditor, boolean forward) {
            super(name);
            this.fileEditor = fileEditor;
            this.forward = forward;
        }

        @Override
        public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
            fileEditor.gotoNextBookmark(forward);
        }

        @Override
        public final String getMacroID() {
            return getName();
        }
    }

    /**
     * Toggles whether the current line has a bookmark.
     */
    public static class ToggleBookmarkAction extends RecordableTextAction {
        private final FileEditor fileEditor;

        public ToggleBookmarkAction(String name, FileEditor fileEditor) {
            super(name);
            this.fileEditor = fileEditor;
        }

        @Override
        public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
            fileEditor.toggleBookmark();
        }

        @Override
        public final String getMacroID() {
            return getName();
        }
    }

    /**
     * Moves the caret to the next (or previous) breakpoint.
     */
    public static class NextBreakpointAction extends RecordableTextAction {
        private final FileEditor fileEditor;
        private final boolean forward;

        public NextBreakpointAction(String name, FileEditor fileEditor, boolean forward) {
            super(name);
            this.fileEditor = fileEditor;
            this.forward = forward;
        }

        @Override
        public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
            fileEditor.gotoNextBreakpoint(forward);
        }

        @Override
        public final String getMacroID() {
            return getName();
        }
    }

    /**
     * Toggles whether the current line has a breakpoint.
     */
    public static class ToggleBreakpointAction extends RecordableTextAction {
        private final FileEditor fileEditor;

        public ToggleBreakpointAction(String name, FileEditor fileEditor) {
            super(name);
            this.fileEditor = fileEditor;
        }

        @Override
        public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
            fileEditor.toggleBreakpoint();
        }

        @Override
        public final String getMacroID() {
            return getName();
        }
    }
}
