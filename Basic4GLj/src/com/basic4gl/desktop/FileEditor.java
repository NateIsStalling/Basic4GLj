package com.basic4gl.desktop;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.GutterIconInfo;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

public class FileEditor {
	public static final String DEFAULT_NAME = "[Unnamed]";
	public RSyntaxTextArea editorPane;
	public RTextScrollPane pane;	//Alternatively RTextScrollPane

	private String mFilename;	//Filename without path
	private String mFilePath;	//Full path including name
	private boolean mIsModified;

	public FileEditor(){
		int i, t;
		SyntaxScheme scheme;

		mFilename = "";
		mFilePath = "";
		mIsModified = false;

		editorPane = new RSyntaxTextArea(20, 60);
		editorPane.setSyntaxEditingStyle("text/basic4gl");
		pane = new RTextScrollPane(editorPane);

		pane.setIconRowHeaderEnabled(true);
		pane.setFoldIndicatorEnabled(false);

		//Enable bookmarks
		Gutter gutter = pane.getGutter();
		gutter.setBookmarkIcon(MainWindow.createImageIcon("images/bookmark.png"));
		gutter.setBookmarkingEnabled(true);

		// Configure popup context menu
		JPopupMenu popup = editorPane.getPopupMenu();
		popup.remove(popup.getComponents().length-1);	//Remove folding option
		popup.remove(popup.getComponents().length-1);	//Remove separator

		//Set default color scheme
		scheme = editorPane.getSyntaxScheme();
		scheme.setStyle(TokenTypes.IDENTIFIER,  new Style(new Color(0,0,128)));	//Normal text
		scheme.setStyle(TokenTypes.LITERAL_NUMBER_DECIMAL_INT, new Style(new Color(0,0,128)));

		scheme.setStyle(TokenTypes.COMMENT_EOL, new Style(new Color(101,124,167))); //Comment
		scheme.setStyle(TokenTypes.RESERVED_WORD, new Style(new Color(0,0,255)));	//Keyword
		scheme.setStyle(TokenTypes.RESERVED_WORD_2, new Style(new Color(0,128,255)));	//Constants
		scheme.setStyle(TokenTypes.LITERAL_STRING_DOUBLE_QUOTE, new Style(new Color(0,128,0)));	//String
		scheme.setStyle(TokenTypes.FUNCTION, new Style(new Color (255,0,0))); //Function
		scheme.setStyle(TokenTypes.OPERATOR, new Style(new Color (128,0,128))); //Operator
	}

	public String getTitle(){
		String result = mFilename.equals("") ? DEFAULT_NAME: mFilename;
		
		// Append asterisk if modified
		if (mIsModified)
			result = result + " *";

		return result;
	}
	public void setFileName(String filename){
		mFilename = filename;
	}
	public String getFilename(){
		return mFilename;
	}

	public boolean isModified(){return mIsModified;}
	public void setModified(){mIsModified = true;}

	public void save(boolean saveAs){
		boolean save = true;
		if (saveAs || mFilePath.equals("")){
			JFileChooser dialog = new JFileChooser();
			dialog.setAcceptAllFileFilterUsed(false);
			dialog.addChoosableFileFilter(new FileNameExtensionFilter("GLBasic Program (*.gb)", "gb"));
			dialog.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
			dialog.setAcceptAllFileFilterUsed(true);	//Move "All Files" to bottom of filter list
			dialog.setCurrentDirectory(new File(".gb"));
			int result = dialog.showSaveDialog(pane);

			if (result == JFileChooser.APPROVE_OPTION){
				mFilePath = dialog.getSelectedFile().getAbsolutePath();
				mFilename = dialog.getSelectedFile().getName();
			} else {
				save = false;
			}
		}
		if (save)
			try {
				FileWriter fw = new FileWriter(mFilePath,false);
				editorPane.write(fw);
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public static FileEditor open(Frame parent){
		FileEditor editor = null;
		JFileChooser dialog = new JFileChooser();
		dialog.setAcceptAllFileFilterUsed(false);
		dialog.addChoosableFileFilter(new FileNameExtensionFilter("GLBasic Program (*.gb)", "gb"));
		dialog.addChoosableFileFilter(new FileNameExtensionFilter("Text File (*.txt)", "txt"));
		dialog.setAcceptAllFileFilterUsed(true);	//Move "All Files" to bottom of filter list
		
		int result = dialog.showOpenDialog(parent);
		
		if (result == JFileChooser.APPROVE_OPTION){
			editor = new FileEditor();
			try {
				FileReader fr = new FileReader(dialog.getSelectedFile().getAbsolutePath());
				editor.mFilePath = dialog.getSelectedFile().getAbsolutePath();
				editor.mFilename = dialog.getSelectedFile().getName();
				editor.editorPane.read(fr, null);
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			editor.editorPane.discardAllEdits();	//Otherwise 'undo' will clear the text area after loading
		}

		return editor;
	}


	public void gotoNextBookmark(boolean forward) {
		//Copied from org.fife.ui.rtextarea.RTextAreaEditorKit.NextBookmarkAction
		Gutter gutter = RSyntaxUtilities.getGutter(editorPane);
		if (gutter!=null) {

			try {

				GutterIconInfo[] bookmarks = gutter.getBookmarks();
				if (bookmarks.length==0) {
					UIManager.getLookAndFeel().
							provideErrorFeedback(editorPane);
					return;
				}

				GutterIconInfo moveTo = null;
				int curLine = editorPane.getCaretLineNumber();

				if (forward) {
					for (int i=0; i<bookmarks.length; i++) {
						GutterIconInfo bookmark = bookmarks[i];
						int offs = bookmark.getMarkedOffset();
						int line = editorPane.getLineOfOffset(offs);
						if (line>curLine) {
							moveTo = bookmark;
							break;
						}
					}
					if (moveTo==null) { // Loop back to beginning
						moveTo = bookmarks[0];
					}
				}
				else {
					for (int i=bookmarks.length-1; i>=0; i--) {
						GutterIconInfo bookmark = bookmarks[i];
						int offs = bookmark.getMarkedOffset();
						int line = editorPane.getLineOfOffset(offs);
						if (line<curLine) {
							moveTo = bookmark;
							break;
						}
					}
					if (moveTo==null) { // Loop back to end
						moveTo = bookmarks[bookmarks.length-1];
					}
				}

				int offs = moveTo.getMarkedOffset();
				if (editorPane instanceof RSyntaxTextArea) {
					RSyntaxTextArea rsta = (RSyntaxTextArea)editorPane;
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
}
