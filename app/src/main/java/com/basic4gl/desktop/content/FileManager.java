package com.basic4gl.desktop.content;

import com.basic4gl.desktop.editor.FileEditor;
import com.basic4gl.desktop.util.IFileManager;
import com.basic4gl.language.core.internal.Mutable;
import java.io.File;
import java.util.Vector;
import javax.swing.*;

public class FileManager implements IFileManager {

    private final Vector<FileEditor> fileEditors = new Vector<>();
    private String runnableFilePath;

    private String currentDirectory; // Current working directory

    private String appDirectory; // Application directory (where the basic4glj executable is)
    private String fileDirectory; // File I/O in this directory
    private String runDirectory; // Basic4GL programs are run in this directory

    private final IFileManagerListener listener;

    public FileManager(IFileManagerListener listener) {
        this.listener = listener;
    }

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

    private FileEditor getEditorOrNull(int index) {
        if (index < 0 || index >= fileEditors.size()) {
            return null;
        }
        return fileEditors.get(index);
    }

    public void setCurrentDirectory(String path) {
        currentDirectory = path;
        if (listener != null) {
            listener.onCurrentDirectoryChanged(path);
        }
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
            FileEditor editor = fileEditors.get(i);
            if (editor != null && editor.getFilePath().equals(path)) {
                found = true;
                break;
            }
        }
        return found ? i : -1;
    }

    public void selectPreviousBreakpoint(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.gotoNextBreakpoint(false);
        }
    }

    public void selectNextBreakpoint(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.gotoNextBreakpoint(true);
        }
    }

    public void toggleBookmark(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.toggleBookmark();
        }
    }

    public void selectPreviousBookmark(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.gotoNextBookmark(false);
        }
    }

    public void selectNextBookmark(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.gotoNextBookmark(true);
        }
    }

    public void selectAll(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.getEditorPane().selectAll();
        }
    }

    public void paste(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.getEditorPane().paste();
        }
    }

    public void copy(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.getEditorPane().copy();
        }
    }

    public void cut(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.getEditorPane().cut();
        }
    }

    public void redo(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null && editor.canRedo()) {
            editor.redoLastAction();
        }
    }

    public void undo(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null && editor.canUndo()) {
            editor.undoLastAction();
        }
    }

    public void toggleBreakpoint(int i) {
        FileEditor editor = getEditorOrNull(i);
        if (editor != null) {
            editor.toggleBreakpoint();
        }
    }

    public void setReadOnly(boolean readOnly) {
        for (int i = 0; i < fileEditors.size(); i++) {
            FileEditor editor = fileEditors.get(i);
            if (editor != null) {
                editor.getEditorPane().setEditable(!readOnly);
            }
        }
    }

    public boolean isMultifileModified(Mutable<String> description) {
        boolean result = false;
        String desc = "";

        for (int i = 0; i < fileEditors.size(); i++) {
            FileEditor editor = fileEditors.get(i);
            if (editor != null && editor.isModified()) {
                result = true;
                if (!desc.isEmpty()) {
                    desc += ", ";
                }
                String filename = editor.getShortFilename();

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

    public String getRunnableFilePath() {
        ensureRunnableFileValid();
        return runnableFilePath;
    }

    public void setRunnableFilePath(String runnableFilePath) {
        this.runnableFilePath = runnableFilePath;
        ensureRunnableFileValid();
    }

    public int getRunnableFileIndex() {
        ensureRunnableFileValid();
        if (runnableFilePath == null || runnableFilePath.isBlank()) {
            return -1;
        }
        return getTabIndex(runnableFilePath);
    }

    public void ensureRunnableFileValid() {
        if (fileEditors.isEmpty()) {
            runnableFilePath = null;
            return;
        }

        if (runnableFilePath != null && !runnableFilePath.isBlank() && getTabIndex(runnableFilePath) != -1) {
            return;
        }

        for (FileEditor editor : fileEditors) {
            if (editor != null) {
                runnableFilePath = editor.getFilePath();
                return;
            }
        }
        runnableFilePath = null;
    }
}
