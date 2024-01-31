package com.basic4gl.lib.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUtil {
    private FileUtil() {}


    public static File getTempDirectory(String parentDirectory) {
        File tempDirectory = null;

        // default to system temp dir; installation dir may be read-only
        String tmpdir = System.getProperty("java.io.tmpdir");
        if (tmpdir != null) {
            Path tempFolderPath = Paths.get(tmpdir);
            tempDirectory = tempFolderPath.toFile();
        }

        // fallback to parent directory if tmpdir is not available for some reason - parent may be same as installation dir
        if ((tempDirectory == null || !tempDirectory.canWrite()) && parentDirectory != null) {
            tempDirectory = new File(parentDirectory);
        }

        // do not return read-only directories
        if (tempDirectory != null && !tempDirectory.canWrite()) {
            tempDirectory = null;
        }

        return tempDirectory;
    }

    public static String separatorsToSystem(String res) {
        if (res == null) {
            return null;
        }
        if (File.separatorChar=='\\') {
            // From Linux/Mac to Windows
            return res.replace('/', File.separatorChar);
        } else {
            // From Windows to Linux/Mac
            return res.replace('\\', File.separatorChar);
        }
    }
}
