package com.basic4gl.desktop;

import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.desktop.util.IFileManager;
import com.basic4gl.runtime.util.Mutable;
import java.io.File;
import java.util.Vector;
import javax.swing.*;

public class FileManager implements IFileManager {

    private final Vector<FileEditor> fileEditors = new Vector<>();

    private String currentDirectory; // Current working directory

    private String appDirectory; // Application directory (where the basic4glj executable is)
    private String fileDirectory; // File I/O in this directory
    private String runDirectory; // Basic4GL programs are run in this directory

    @Override
    public int editorCount() {
        return fileEditors.size();
    }

    @Override
    public JTextArea getEditor(int index) {
        return fileEditors.get(index).getEditorPane();
    }

    public String getFilename(int index) {
        return fileEditors.get(index).getFilePath();
    }

    public void setCurrentDirectory(String path) {
        currentDirectory = path;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    @Override
    public String getAppDirectory() {
        return appDirectory;
    }

    public int getFileTabIndex(String filename) {
        File file = new File(getCurrentDirectory(), filename);
        return getTabIndex(file.getAbsolutePath());
    }

    public int getTabIndex(String path) {
        int i = 0;
        boolean found = false;
        for (; i < fileEditors.size(); i++) {
            if (fileEditors.get(i).getFilePath().equals(path)) {
                found = true;
                break;
            }
        }
        return found ? i : -1;
    }

    public void selectPreviousBreakpoint(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).gotoNextBreakpoint(false);
        }
    }

    public void selectNextBreakpoint(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).gotoNextBreakpoint(true);
        }
    }

    public void toggleBookmark(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).toggleBookmark();
        }
    }

    public void selectPreviousBookmark(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).gotoNextBookmark(false);
        }
    }

    public void selectNextBookmark(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).gotoNextBookmark(true);
        }
    }

    public void selectAll(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).getEditorPane().selectAll();
        }
    }

    public void paste(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).getEditorPane().paste();
        }
    }

    public void copy(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).getEditorPane().copy();
        }
    }

    public void cut(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).getEditorPane().cut();
        }
    }

    public void redo(int i) {
        if (i > -1 && i < fileEditors.size()) {
            if (fileEditors.get(i).canRedo()) {
                fileEditors.get(i).redoLastAction();
            }
        }
    }

    public void undo(int i) {
        if (i > -1 && i < fileEditors.size()) {
            if (fileEditors.get(i).canUndo()) {
                fileEditors.get(i).undoLastAction();
            }
        }
    }

    public void toggleBreakpoint(int i) {
        if (i > -1 && i < fileEditors.size()) {
            fileEditors.get(i).toggleBreakpoint();
        }
    }

    public void setReadOnly(boolean readOnly) {
        for (int i = 0; i < fileEditors.size(); i++) {
            fileEditors.get(i).getEditorPane().setEditable(!readOnly);
        }
    }

    public boolean isMultifileModified(Mutable<String> description) {
        boolean result = false;
        String desc = "";

        for (int i = 0; i < fileEditors.size(); i++) {
            if (fileEditors.get(i).isModified()) {
                result = true;
                if (!desc.isEmpty()) {
                    desc += ", ";
                }
                String filename = fileEditors.get(i).getShortFilename();

                desc += filename;
            }
        }
        description.set(desc);
        return result;
    }

    public void setAppDirectory(String appDirectory) {
        this.appDirectory = appDirectory;
    }

    public String getFileDirectory() {
        return fileDirectory;
    }

    public void setFileDirectory(String fileDirectory) {
        this.fileDirectory = fileDirectory;
    }

    public String getRunDirectory() {
        return runDirectory;
    }

    public void setRunDirectory(String runDirectory) {
        this.runDirectory = runDirectory;
    }

    public Vector<FileEditor> getFileEditors() {
        return fileEditors;
    }
}
