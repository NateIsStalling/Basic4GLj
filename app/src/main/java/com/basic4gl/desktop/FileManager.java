package com.basic4gl.desktop;

import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.desktop.util.IFileManager;
import com.basic4gl.runtime.util.Mutable;

import javax.swing.*;
import java.io.File;
import java.util.Vector;

public class FileManager implements IFileManager {

    public String mCurrentDirectory;   //Current working directory


    public String mAppDirectory,  // Application directory (where basic4gl.exe is)
            mFileDirectory, // File I/O in this directory
            mRunDirectory;  // Basic4GL program are run in this directory

    public Vector<FileEditor> mFileEditors = new Vector<FileEditor>();

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
    public String getAppDirectory() {
        return mAppDirectory;
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

    public void SelectPreviousBreakpoint(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).gotoNextBreakpoint(false);
        }
    }

    public void SelectNextBreakpoint(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).gotoNextBreakpoint(true);
        }
    }

    public void ToggleBookmark(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).toggleBookmark();
        }
    }

    public void SelectPreviousBookmark(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).gotoNextBookmark(false);
        }
    }

    public void SelectNextBookmark(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).gotoNextBookmark(true);
        }
    }

    public void SelectAll(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).editorPane.selectAll();
        }
    }

    public void Paste(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).editorPane.paste();
        }
    }

    public void copy(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).editorPane.copy();
        }
    }

    public void cut(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).editorPane.cut();
        }
    }

    public void redo(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            if (mFileEditors.get(i).editorPane.canRedo()) {
                mFileEditors.get(i).editorPane.redoLastAction();
            }
        }
    }

    public void undo(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            if (mFileEditors.get(i).editorPane.canUndo()) {
                mFileEditors.get(i).editorPane.undoLastAction();
            }
        }
    }

    public void toggleBreakpoint(int i) {
        if (mFileEditors != null && i > -1 && i < mFileEditors.size()) {
            mFileEditors.get(i).toggleBreakpoint();
        }
    }

    public void setReadOnly(boolean readOnly) {
        for (int i = 0; i < mFileEditors.size(); i++) {
            mFileEditors.get(i).editorPane.setEditable(!readOnly);
        }
    }

    public boolean MultifileModified(Mutable<String> description) {
        boolean result = false;
        String desc = "";

        for (int i = 0; i < mFileEditors.size(); i++) {
            if (mFileEditors.get(i).isModified()) {
                result = true;
                if (!desc.equals("")) {
                    desc += ", ";
                }
                String filename = mFileEditors.get(i).getShortFilename();

                desc += filename;
            }
        }
        description.set(desc);
        return result;
    }
}
