package com.basic4gl.language.core.runtime;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.IntBuffer;

public interface IFileOpener {
    FileInputStream openRead(String filename, boolean filesFolder);

    FileInputStream openRead(String filename, boolean filesFolder, IntBuffer length);

    FileOutputStream openWrite(String filename);

    FileOutputStream openWrite(String filename, boolean filesFolder);

    String getFilenameForRead(String filename);

    String getFilenameForRead(String filename, boolean filesFolder);

    String getFileAbsolutePath(String filename);

    boolean delete(String filename, boolean isSandboxMode);

    void setParentDirectory(String parent);

    String getParentDirectory();

    String getAppDataFolder(boolean allUsers);

    String getAppDataFolderName();

    boolean createDirectory(String pathname);
}
