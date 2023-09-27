package com.basic4gl.lib.util;

import com.basic4gl.runtime.HasErrorState;

import java.io.*;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Created by Nate on 11/1/2015.
 */
public class FileOpener extends HasErrorState {
    private String parentDirectory;
    private EmbeddedFiles embeddedFiles;
    Set<String> tempFiles;

    String extractStoredFile(String filename){
        filename = separatorsToSystem(filename);
        Exception exception = null;
        File file = null;

        InputStream in = null;
        OutputStream out = null;
        try {
            // TODO keep cache of temp filenames
            URL resource = embeddedFiles.getResource(filename);
            File tempDirectory = parentDirectory != null
                ? new File(parentDirectory)
                : null;
            file = File.createTempFile("temp", new File(filename).getName(), tempDirectory);
            System.out.println("file: " + filename);
            System.out.println("fn: " +  new File(filename).getName());
            System.out.println("Created temp file: " + file.getAbsolutePath());
            file.deleteOnExit();

            in = resource.openStream();
            out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

        } catch (IOException e){
            e.printStackTrace();
            exception = e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (file == null || exception != null)
        {
            setError("Error opening embedded file");
            return "";
        }
        // Return new filename
        File workingDirectory = parentDirectory != null
            ? new File(parentDirectory)
            : Paths.get("").toFile();
        return workingDirectory.toURI().relativize(file.toURI()).getPath();
    }
    /*
    void DeleteTempFile(String filename){
        if (tempFiles.find(filename) != tempFiles.end()) {
            DeleteFile(filename.c_str());
            tempFiles.erase(filename);
        }
    }

*/
    public FileOpener (String parent){
        parent = separatorsToSystem(parent);
        parentDirectory = parent;
        embeddedFiles = new EmbeddedFiles();
        embeddedFiles.setParentDirectory(parent);
    }

    private static boolean isChild(Path child, String parentText) {
        Path parent = Paths.get(parentText).normalize().toAbsolutePath();
        return child.startsWith(parent);
    }

    // Returns true if file is in the current directory
    public boolean checkFilesFolder(String filename)
    {
        filename = separatorsToSystem(filename);

        File file = new File(filename);
        if (file.isAbsolute()) {
            Path path = Paths.get(file.getAbsolutePath()).normalize();
            if (!isChild(path, parentDirectory)) {
                setError("You can only open files in the current directory or below.");
                return false;
            }
        } else {
            file = new File(parentDirectory, filename);
        }

        if (file.exists()){
            System.out.println("file found! " + filename);
            return true;
        } else {
            System.out.println("parent " + parentDirectory);
            setError("You can only open files in the current directory or below.");
            return false;
        }
    }

    // Both functions return a newly allocated stream if successful, or NULL if not (use the Error() and GetError() methods to find what the problem was)
    // If files folder is true then the file will only be opened if it is in the "files\" subfolder in the current directory.
    public FileInputStream openRead(String filename) { return openRead(filename, true);}
    public FileInputStream openRead(String filename, boolean filesFolder){
        filename = separatorsToSystem(filename);

        clearError();
        if (filesFolder && !checkFilesFolder(filename)) {
            return null;
        } else {
            System.out.println("file embedded! " + filename + " : " + filesFolder);
            FileInputStream result = embeddedFiles.OpenOrLoad (filename);
            if (result == null) {
                setError("Failed to open " + filename);
            }
            return result;
        }
    }
    public FileOutputStream openWrite(String filename) { return openWrite(filename, true);}
    public FileOutputStream openWrite(String filename, boolean filesFolder){
        filename = separatorsToSystem(filename);

        clearError();

        if (filesFolder && !checkFilesFolder(filename)) {
            return null;
        } else {
            File file = new File(parentDirectory, filename);
            FileOutputStream stream = null;
            Exception exception = null;

            try {
                if (file.exists()) {
                    stream = new FileOutputStream(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (exception != null) {
                file = null;
                exception.printStackTrace();
                setError ("Failed to open " + filename);
                return null;
            }
            else {
                return stream;
            }
        }
    }

    public FileInputStream openRead(String filename, boolean filesFolder, IntBuffer length)
    {
        filename = separatorsToSystem(filename);

        clearError();

        if (filesFolder && !checkFilesFolder(filename)) {
            length.put(0, 0);
            return null;
        }
        else {
            FileInputStream result = embeddedFiles.OpenOrLoad(filename);
            if (result == null) {
                setError("Failed to open " + filename);
            }
            return result;
        }
    }
    // The following function returns a filename that can be opened in read mode.
    // If the input filename corresponds to an embedded file, the embedded file
    // is copied to a temporary file on the drive, and that filename is returned
    // instead.
    // (Use this when the file opening code expects to see a real disk file and
    // cannot be rewritten to work from a memory stream.)
    // Returns a blank string if not successful (use Error() and GetError() to retrieve the error description.)
    public String getFilenameForRead(String filename){
        return getFilenameForRead(filename, true);
    }
    public String getFilenameForRead(String filename, boolean filesFolder){
        filename = separatorsToSystem(filename);

        clearError();

        if (filesFolder && !checkFilesFolder(filename)) {
            return "";
        } else {

            // Stored in embedded file?
            if (embeddedFiles.IsStored(filename))
            {
                return extractStoredFile(filename);
            }
            else {

                // Not embedded. Return input filename.
                return filename;
            }
        }
    }

    public String getFileAbsolutePath(String filename) {
        filename = getFilenameForRead(filename, false);

        File file;
        if (filename != null && !filename.equals("") && getError().equals("") &&
                (file = new File(getParentDirectory(), filename)).exists() && !file.isDirectory()) {
            filename = file.getAbsolutePath();
        }

        return filename;
    }

    // Delete a disk file
    public boolean delete(String filename, boolean isSandboxMode) {

        filename = separatorsToSystem(filename);

        clearError();

        if (isSandboxMode) {
            setError("Deleting files is not allowed when safe mode is switched on");
            return false;
        }

        if (new File(parentDirectory, filename).delete()) {
            return true;
        } else {
            setError("Delete failed");
            return false;
        }
    }

    public void setParentDirectory(String parent){
        String path = separatorsToSystem(parent);

        parentDirectory = path;
        embeddedFiles.setParentDirectory(path);
    }
    public String getParentDirectory(){return parentDirectory;}

    String separatorsToSystem(String res) {
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
