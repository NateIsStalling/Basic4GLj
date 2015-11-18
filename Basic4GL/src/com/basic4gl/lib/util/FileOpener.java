package com.basic4gl.lib.util;

import com.basic4gl.vm.HasErrorState;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Created by Nate on 11/1/2015.
 */
public class FileOpener extends HasErrorState {
    EmbeddedFiles m_embeddedFiles;
    Set<String> tempFiles;

    String ExtractStoredFile(String filename){
        Exception exception = null;
        File file = null;
        try {
            URL resource = FileOpener.class.getResource(filename);
            file = Paths.get(resource.toURI()).toFile();
        } catch (Exception e){
            e.printStackTrace();
            exception = e;
        }
        if (file == null || exception != null)
        {
            setError("Error opening embedded file");
            return "";
        }
        // Return new filename
        return file.getAbsolutePath();
    }
    /*
    void DeleteTempFile(String filename){
        if (tempFiles.find(filename) != tempFiles.end()) {
            DeleteFile(filename.c_str());
            tempFiles.erase(filename);
        }
    }

*/
    public FileOpener (){
        m_embeddedFiles = new EmbeddedFiles();
    }
    public FileOpener (ByteBuffer rawData){
        m_embeddedFiles = new EmbeddedFiles(rawData);
    }
    public void AddFiles (ByteBuffer rawData){
        IntBuffer offset = IntBuffer.allocate(1).put(0, 0);
        m_embeddedFiles.AddFiles (rawData, offset);
    }
    public void AddFiles (ByteBuffer rawData, IntBuffer offset){
        m_embeddedFiles.AddFiles (rawData, offset);
    }

    // Returns true if file is in the current directory
    public boolean CheckFilesFolder (String filename)
    {
        File file = new File(filename);/*
        GetFullPathName(filename.c_str (), 1024, fullPath, &fileBit);
        GetCurrentDirectory(1024, currentDir);
        PrepPathForComp(fullPath);
        PrepPathForComp(currentDir);

        // Truncate
        fullPath[strlen(currentDir)] = 0;
        if (strcmp(fullPath, currentDir) == 0)
            return true;
        else {
            setError("You can only open files in the current directory or below.");
            return false;
        }
        */
        if (file.exists()){
            return true;
        } else {
            setError("You can only open files in the current directory or below.");
            return false;
        }
    }
    // Both functions return a newly allocated stream if successful, or NULL if not (use the Error() and GetError() methods to find what the problem was)
    // If files folder is true then the file will only be opened if it is in the "files\" subfolder in the current directory.
    public FileInputStream OpenRead(String filename) { return OpenRead(filename, true);}
    public FileInputStream OpenRead(String filename, boolean filesFolder){
        clearError();
        if (filesFolder && !CheckFilesFolder (filename))
            return null;
        else {
            FileInputStream result = m_embeddedFiles.OpenOrLoad (filename);
            if (result == null)
                setError("Failed to open " + filename);
            return result;
        }
    }
    public FileOutputStream OpenWrite(String filename) { return OpenWrite(filename, true);}
    public FileOutputStream OpenWrite(String filename, boolean filesFolder){
        clearError();
        if (filesFolder && !CheckFilesFolder (filename))
            return null;
        else {
            File file = new File(filename);
            FileOutputStream stream = null;
            Exception exception = null;

            try {
                if (file.exists())
                    stream = new FileOutputStream(file);
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
            if (exception != null) {
                file = null;
                setError ("Failed to open " + filename);
                return null;
            }
            else
                return stream;
        }
    }

    public FileInputStream OpenRead(String filename, boolean filesFolder, IntBuffer length)
    {
        clearError();
        if (filesFolder && !CheckFilesFolder (filename)) {
            length.put(0, 0);
            return null;
        }
        else {
            FileInputStream result = m_embeddedFiles.OpenOrLoad(filename);
            if (result == null)
                setError("Failed to open " + filename);
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
    public String FilenameForRead(String filename){
        return FilenameForRead(filename, true);
    }
    public String FilenameForRead(String filename, boolean filesFolder){
        clearError ();
        if (filesFolder && !CheckFilesFolder (filename))
            return "";
        else {

            // Stored in embedded file?
            if (m_embeddedFiles.IsStored(filename))
            {
                return ExtractStoredFile(filename);
            }
            else {

                // Not embedded. Return input filename.
                return filename;
            }
        }
    }

    // Delete a disk file
    public boolean Delete(String filename, boolean isSandboxMode) {

        clearError();

        if (isSandboxMode) {
            setError("Deleting files is not allowed when safe mode is switched on");
            return false;
        }

        if (new File(filename).delete())
            return true;
        else {
            setError("Delete failed");
            return false;
        }
    }
}
