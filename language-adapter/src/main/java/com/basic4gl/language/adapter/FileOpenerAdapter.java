package com.basic4gl.language.adapter;

import com.basic4gl.language.core.runtime.HasErrorState;
import com.basic4gl.language.core.runtime.IFileOpener;
import com.basic4gl.library.desktopgl.content.FileOpener;

import java.io.*;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Nate on 11/1/2015.
 */
public class FileOpenerAdapter extends HasErrorState implements IFileOpener {
    private static final String DEFAULT_APP_DATA_FOLDER_NAME = "Basic4GL";

    public static final String ERROR_DIRECTORY_ALREADY_EXISTS = "Directory already exists";

    com.basic4gl.desktop.spi.FileOpener parent;

    private String parentDirectory;

    public FileOpenerAdapter(com.basic4gl.desktop.spi.FileOpener parent) {
        this.parent = parent;
    }

    String extractStoredFile(String filename) {
        return parent.extractStoredFile(filename);
    }

    /*
    	void deleteTempFile(String filename){
    		if (tempFiles.find(filename) != tempFiles.end()) {
    			DeleteFile(filename.c_str());
    			tempFiles.erase(filename);
    		}
    	}

    */
    // Returns true if file is in the current directory
    public boolean checkFilesFolder(String filename) {
        return parent.checkFilesFolder(filename);
    }

    // Both functions return a newly allocated stream if successful, or NULL if not (use the Error()
    // and GetError() methods to find what the problem was)
    // If files folder is true then the file will only be opened if it is in the "files\" subfolder in
    // the current directory.
    public FileInputStream openRead(String filename) {
        return openRead(filename, true);
    }

    public FileInputStream openRead(String filename, boolean filesFolder) {
        return parent.openRead(filename, filesFolder);
    }

    public FileInputStream openRead(String filename, boolean filesFolder, IntBuffer length) {
        return parent.openRead(filename, filesFolder, length);
    }

    public FileOutputStream openWrite(String filename) {
        return openWrite(filename, true);
    }

    public FileOutputStream openWrite(String filename, boolean filesFolder) {
        return parent.openWrite(filename, filesFolder);
    }


    // The following function returns a filename that can be opened in read mode.
    // If the input filename corresponds to an embedded file, the embedded file
    // is copied to a temporary file on the drive, and that filename is returned
    // instead.
    // (Use this when the file opening code expects to see a real disk file and
    // cannot be rewritten to work from a memory stream.)
    // Returns a blank string if not successful (use Error() and GetError() to retrieve the error
    // description.)
    public String getFilenameForRead(String filename) {
        return getFilenameForRead(filename, true);
    }

    public String getFilenameForRead(String filename, boolean filesFolder) {
        return parent.getFilenameForRead(filename, filesFolder);
    }

    public String getFileAbsolutePath(String filename) {
        return parent.getFileAbsolutePath(filename);
    }

    // Delete a disk file
    public boolean delete(String filename, boolean isSandboxMode) {
        return parent.delete(filename, isSandboxMode);
    }

    public void setParentDirectory(String parent) {
        this.parent.setParentDirectory(parent);
    }

    public String getParentDirectory() {
        return parent.getParentDirectory();
    }

    public String getAppDataFolder(boolean allUsers) {
        return parent.getAppDataFolder(allUsers);
    }

    public String getAppDataFolderName() {
        return DEFAULT_APP_DATA_FOLDER_NAME;
    }

    public boolean createDirectory(String pathname) {
        return parent.createDirectory(pathname);
    }
}
