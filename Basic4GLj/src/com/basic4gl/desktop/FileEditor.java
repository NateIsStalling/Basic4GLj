package com.basic4gl.desktop;

import java.awt.Container;
import java.awt.Frame;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.CodeEditorPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileEditor {
	public static final String DEFAULT_NAME = "[Unnamed]";
	public CodeEditorPane editorPane;
	public Container pane;

	private String mFilename;	//Filename without path
	private String mFilePath;	//Full path including name
	private boolean mIsModified;

	public FileEditor(){
		editorPane = new CodeEditorPane();
		pane = editorPane.getContainerWithLines();
		
		//editorPane.syntaxDocumentFilter= null;
		//editorPane.setContentType("text/basic4gl");
		mFilename = "";
		mFilePath = "";
		mIsModified = false;

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

	public void save(boolean saveas){
		boolean save = true;
		if (saveas || mFilePath.equals("")){
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
				//TODO Change tabs when opening file
				FileReader fr = new FileReader(dialog.getSelectedFile().getAbsolutePath());
				editor.mFilePath = dialog.getSelectedFile().getAbsolutePath();
				editor.mFilename = dialog.getSelectedFile().getName();
				editor.editorPane.read(fr, null);
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			editor.editorPane.invalidate();
		}
		return editor;
	}

}
