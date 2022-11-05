package com.basic4gl.desktop;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

import com.basic4gl.desktop.util.MainEditor;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

public class FileEditor {
    public static final String DEFAULT_NAME = "[Unnamed]";

    private static final int HEADER_BOOKMARK = 0;
    private static final int HEADER_BREAK_PT = 1;

    private static final String IMAGE_DIRECTORY = "images/";
    private static final String THEME_DIRECTORY = IMAGE_DIRECTORY + "programmer-art/";
    private static final String ICON_BOOKMARK = THEME_DIRECTORY + "bookmark.png";
    private static final String ICON_BREAK_PT = THEME_DIRECTORY + "BreakPt.png";

    private final MainEditor mMainEditor;

    public RSyntaxTextArea editorPane;
    public RMultiHeaderScrollPane pane;

    //private Map<Integer, Object> mLineHighlights; //Highlight lines with breakpoints

    private String mFilename;    //Filename without path
    private String mFilePath;    //Full path including name
    private boolean mIsModified;
    private boolean mSaved;      //File exists on system

    public FileEditor(MainEditor mainEditor, LinkGenerator linkGenerator) {
        mMainEditor = mainEditor;

        int i, t;
        SyntaxScheme scheme;

        mFilename = "";
        mFilePath = "";
        mIsModified = false;
        mSaved      = false;

        editorPane = new RSyntaxTextArea(20, 60);
        editorPane.setSyntaxEditingStyle("text/basic4gl");
        if (linkGenerator != null) {
            editorPane.setHyperlinksEnabled(true);
            editorPane.setLinkGenerator(linkGenerator);
        }

        pane = new RMultiHeaderScrollPane(editorPane);

        //Add shortcut keys for breakpoints
        InputMap inputMap = editorPane.getInputMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                RTextAreaEditorKit.rtaNextBookmarkAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.SHIFT_MASK),
                RTextAreaEditorKit.rtaPrevBookmarkAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK),
                RTextAreaEditorKit.rtaToggleBookmarkAction);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),
                "RTA.NextBreakpointAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.SHIFT_MASK),
                "RTA.PrevBreakpointAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK),
                "RTA.ToggleBreakpointAction");

        //Update action map for use with the multi-header scroll pane
        ActionMap actionMap = editorPane.getActionMap();
        actionMap.put(RTextAreaEditorKit.rtaNextBookmarkAction,
                new MultiHeaderBookmarkActions.MultiHeaderNextBookmarkAction
                        (RTextAreaEditorKit.rtaNextBookmarkAction,true, HEADER_BOOKMARK));
        actionMap.put(RTextAreaEditorKit.rtaPrevBookmarkAction,
                new MultiHeaderBookmarkActions.MultiHeaderNextBookmarkAction
                        (RTextAreaEditorKit.rtaPrevBookmarkAction,false, HEADER_BOOKMARK));
        actionMap.put(RTextAreaEditorKit.rtaToggleBookmarkAction,
                new MultiHeaderBookmarkActions.MultiHeaderToggleBookmarkAction
                        (RTextAreaEditorKit.rtaToggleBookmarkAction, HEADER_BOOKMARK));

        actionMap.put("RTA.NextBreakpointAction",
                new MultiHeaderBookmarkActions.MultiHeaderNextBookmarkAction
                        ("RTA.NextBreakpointAction",true, HEADER_BREAK_PT));
        actionMap.put("RTA.PrevBreakpointAction",
                new MultiHeaderBookmarkActions.MultiHeaderNextBookmarkAction
                        ("RTA.PrevBreakpointAction",false, HEADER_BREAK_PT));
        actionMap.put("RTA.ToggleBreakpointAction",
                new MultiHeaderToggleBreakPointAction
                        ("RTA.ToggleBreakpointAction",this));

        //Enable bookmarks
        final MultiHeaderGutter gutter = pane.getGutter();
        gutter.addIconRowHeader();
        gutter.addIconRowHeader();

        gutter.setAutohideIconRowHeader(HEADER_BOOKMARK, true);

        gutter.getIconRowHeader(HEADER_BOOKMARK).addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (gutter.getBookmarks(HEADER_BOOKMARK).length == 0)
                    pane.setIconRowHeaderEnabled(HEADER_BOOKMARK, false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        gutter.getIconRowHeader(HEADER_BREAK_PT).addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

                        try {
                            //boolean highlight = false;
                            int offs = editorPane.viewToModel(e.getPoint());
                            int ble = offs > -1 ? editorPane.getLineOfOffset(offs):-1;
                            if(ble > -1) {
                                mMainEditor.toggleBreakpt(getFilePath(), ble);
                                //highlight = mMainEditor.toggleBreakpt(getFilename(), ble);
                            }
                            //TODO toggle highlighting breakpoints
                            /*
                            if (highlight)
                                mLineHighlights.put(offs, editorPane.addLineHighlight(ble, new Color(255,0,0,128)));
                            else
                                editorPane.removeLineHighlight(mLineHighlights.get(ble));
                            */
                        } catch (BadLocationException var3) {
                            var3.printStackTrace();
                        }

            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        gutter.setBookmarkIcon(HEADER_BOOKMARK, MainWindow.createImageIcon(ICON_BOOKMARK));
        gutter.setBookmarkIcon(HEADER_BREAK_PT, MainWindow.createImageIcon(ICON_BREAK_PT));

        gutter.setBookmarkingEnabled(HEADER_BOOKMARK, true);
        gutter.setBookmarkingEnabled(HEADER_BREAK_PT, true);

        pane.setIconRowHeaderEnabled(HEADER_BOOKMARK, false);
        pane.setIconRowHeaderEnabled(HEADER_BREAK_PT, true);

        pane.setFoldIndicatorEnabled(false);

        // Configure popup context menu
        JPopupMenu popup = editorPane.getPopupMenu();
        popup.remove(popup.getComponents().length - 1);    //Remove folding option
        popup.remove(popup.getComponents().length - 1);    //Remove separator

        //Set default color scheme
        scheme = editorPane.getSyntaxScheme();
        scheme.setStyle(TokenTypes.IDENTIFIER, new Style(new Color(0, 0, 128)));    //Normal text
        scheme.setStyle(TokenTypes.LITERAL_NUMBER_DECIMAL_INT, new Style(new Color(0, 0, 128)));

        scheme.setStyle(TokenTypes.COMMENT_EOL, new Style(new Color(101, 124, 167))); //Comment
        scheme.setStyle(TokenTypes.RESERVED_WORD, new Style(new Color(0, 0, 255)));    //Keyword
        scheme.setStyle(TokenTypes.RESERVED_WORD_2, new Style(new Color(0, 128, 255)));    //Constants
        scheme.setStyle(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, new Style(new Color(0, 128, 0)));    //String
        scheme.setStyle(TokenTypes.FUNCTION, new Style(new Color(255, 0, 0))); //Function
        scheme.setStyle(TokenTypes.OPERATOR, new Style(new Color(128, 0, 128))); //Operator
    }

    public String getTitle() {
        String result;
        result  = (mFilename.equals("") ? DEFAULT_NAME : mFilename).toLowerCase();

        // Append asterisk if modified
        if (mIsModified)
            result = result + " *";

        return result;
    }

    public void setFileName(String filename) {
        mFilename = filename;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public String getShortFilename() {
        return !mFilename.equals("")
                ? new File(mFilename).getName()
                : DEFAULT_NAME.toLowerCase();
    }

    public boolean isModified() {
        return mIsModified;
    }

    public void setModified() {
        mIsModified = true;
    }

    public boolean save(boolean saveAs, String parentDirectory) {
        boolean save = true;
        if (saveAs || !mSaved || mFilePath.equals("")) {
            JFileChooser dialog = new JFileChooser();
            dialog.setAcceptAllFileFilterUsed(false);
            dialog.addChoosableFileFilter(new FileNameExtensionFilter("GLBasic Program (*.gb)", "gb"));
            dialog.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
            dialog.setAcceptAllFileFilterUsed(true);    //Move "All Files" to bottom of filter list
            dialog.setCurrentDirectory(new File(mMainEditor.getCurrentDirectory()));
            dialog.setSelectedFile(new File(mFilename));
            int result = dialog.showSaveDialog(pane);

            if (result == JFileChooser.APPROVE_OPTION) {
                String path = dialog.getSelectedFile().getAbsolutePath();
                if (dialog.getFileFilter() instanceof FileNameExtensionFilter) {
                    //Append extension if needed
                    if(((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions().length > 0){
                        String extension = ((FileNameExtensionFilter) dialog.getFileFilter()).getExtensions()[0];
                        if (!path.endsWith("." + extension))
                            path += "." + extension;
                    }
                }
                mFilePath = path;
                mFilename = new File(path).getName();
            } else {
                save = false;
            }
        }
        if (save)
            try {
                FileWriter fw = new FileWriter(mFilePath, false);
                editorPane.write(fw);
                fw.close();
                mIsModified = false;
                mSaved = true;
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

        return false;
    }

    public static FileEditor open(Frame parent, MainEditor mainEditor, LinkGenerator linkGenerator) {
        FileEditor editor = null;
        JFileChooser dialog = new JFileChooser();
        dialog.setAcceptAllFileFilterUsed(false);
        dialog.addChoosableFileFilter(new FileNameExtensionFilter("GLBasic Program (*.gb)", "gb"));
        dialog.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
        dialog.setAcceptAllFileFilterUsed(true);    //Move "All Files" to bottom of filter list
        dialog.setCurrentDirectory(new File(mainEditor.getCurrentDirectory()));
        int result = dialog.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            editor = new FileEditor(mainEditor, linkGenerator);
            try {
                FileReader fr = new FileReader(dialog.getSelectedFile().getAbsolutePath());
                editor.mFilePath = dialog.getSelectedFile().getAbsolutePath();
                editor.mFilename = dialog.getSelectedFile().getName();
                editor.editorPane.read(fr, null);
                fr.close();
                editor.mSaved = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            editor.editorPane.discardAllEdits();    //Otherwise 'undo' will clear the text area after loading
        }

        return editor;
    }
    public static FileEditor open(File file, MainEditor mainEditor, LinkGenerator linkGenerator) {
        FileEditor editor = null;
        editor = new FileEditor(mainEditor, linkGenerator);
        if (file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                editor.mFilePath = file.getAbsolutePath();
                editor.mFilename = file.getName();
                editor.editorPane.read(fr, null);
                fr.close();
                editor.mSaved = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else {
            editor.mFilePath = file.getAbsolutePath();
            editor.mFilename = file.getName();
            editor.mSaved = false;
        }
        editor.editorPane.discardAllEdits();    //Otherwise 'undo' will clear the text area after loading

        return editor;
    }

    public void gotoNextBookmark(boolean forward) {
        //Copied from org.fife.ui.rtextarea.RTextAreaEditorKit.NextBookmarkAction
        MultiHeaderGutter gutter = pane.getGutter();
        if (gutter != null) {

            try {

                GutterIconInfo[] bookmarks = gutter.getBookmarks(HEADER_BOOKMARK);
                if (bookmarks.length == 0) {
                    UIManager.getLookAndFeel().
                            provideErrorFeedback(editorPane);
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
                        rsta.getFoldManager().
                                ensureOffsetNotInClosedFold(offs);
                    }
                }
                int line = editorPane.getLineOfOffset(offs);
                offs = editorPane.getLineStartOffset(line);
                editorPane.setCaretPosition(offs);

            } catch (BadLocationException ble) { // Never happens
                UIManager.getLookAndFeel().
                        provideErrorFeedback(editorPane);
                ble.printStackTrace();
            }
        }

    }
    public void toggleBookmark(){
        int line;
        try {
            line = editorPane.getLineOfOffset(editorPane.getCaretPosition());
            pane.getGutter().toggleBookmark(HEADER_BOOKMARK, line);

        } catch (BadLocationException ex) {
            line = -1;
            ex.printStackTrace();
            System.out.println(editorPane.getCaretPosition());
        }
    }

    public void gotoNextBreakpoint(boolean forward) {
        //Copied from org.fife.ui.rtextarea.RTextAreaEditorKit.NextBookmarkAction
        MultiHeaderGutter gutter = pane.getGutter();
        if (gutter != null) {

            try {

                GutterIconInfo[] bookmarks = gutter.getBookmarks(HEADER_BREAK_PT);
                if (bookmarks.length == 0) {
                    UIManager.getLookAndFeel().
                            provideErrorFeedback(editorPane);
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
                        rsta.getFoldManager().
                                ensureOffsetNotInClosedFold(offs);
                    }
                }
                int line = editorPane.getLineOfOffset(offs);
                offs = editorPane.getLineStartOffset(line);
                editorPane.setCaretPosition(offs);

            } catch (BadLocationException ble) { // Never happens
                UIManager.getLookAndFeel().
                        provideErrorFeedback(editorPane);
                ble.printStackTrace();
            }
        }

    }
    public void toggleBreakpoint(){
        int line;
        //boolean highlight = false;
        GutterIconInfo tag;
        try {
            line = editorPane.getLineOfOffset(editorPane.getCaretPosition());

            pane.getGutter().toggleBookmark(HEADER_BREAK_PT, line);

            //TODO toggle highlighting row of breakpoint
            /*
            highlight = pane.getGutter().toggleBookmark(HEADER_BREAK_PT, line);
            if (highlight) {
                mLineHighlights.put(line,
                        editorPane.addLineHighlight(line, new Color(255, 0, 0, 128)));
            }
            else {
                editorPane.removeLineHighlight(mLineHighlights.get(line));
                mLineHighlights.remove(line);
            }
            */
            mMainEditor.toggleBreakpt(getFilePath(), line);
        } catch (BadLocationException ex) {
            line = -1;
            ex.printStackTrace();
            System.out.println(editorPane.getCaretPosition());
        }
    }

    public ArrayList<Integer> getBreakpoints()
    {
        ArrayList<Integer> points = new ArrayList<Integer>();

        MultiHeaderGutter gutter = pane.getGutter();
        if (gutter != null) {
            GutterIconInfo[] bookmarks = gutter.getBookmarks(HEADER_BREAK_PT);
            for (GutterIconInfo info: bookmarks) {
                try {
                    int line = editorPane.getLineOfOffset(info.getMarkedOffset());
                    points.add(line);
                    System.out.println("Breakpoint at line: " + info.getMarkedOffset());
                } catch (BadLocationException ex){
                    ex.printStackTrace();
                }
            }
        }
        return points;
    }

    /**
     * Toggles whether the current line has a bookmark.
     */
    public static class MultiHeaderToggleBreakPointAction extends RecordableTextAction {
        private FileEditor mFileEditor;
        public MultiHeaderToggleBreakPointAction(String name, FileEditor fileEditor) {
            super(name);
            mFileEditor = fileEditor;
        }

        @Override
        public void actionPerformedImpl(ActionEvent e, RTextArea textArea) {
            mFileEditor.toggleBreakpoint();
        }

        @Override
        public final String getMacroID() {
            return getName();
        }

    }
}
