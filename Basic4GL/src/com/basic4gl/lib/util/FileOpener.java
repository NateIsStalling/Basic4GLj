package com.basic4gl.lib.util;

import com.basic4gl.vm.HasErrorState;

import java.util.Set;

/**
 * Created by Nate on 11/1/2015.
 */
public class FileOpener extends HasErrorState {
    /*EmbeddedFiles m_embeddedFiles;
    Set<String> tempFiles;

    String ExtractStoredFile(String filename);
    void DeleteTempFile(String filename);


    public FileOpener ();
    public FileOpener (char *rawData);
    public ~FileOpener();
    public void AddFiles (char *rawData);
    public void AddFiles (char *rawData, int& offset);

    public boolean CheckFilesFolder (String filename);			// Returns true if file is in the "files\" subfolder in the current directory

    // Both functions return a newly allocated stream if successful, or NULL if not (use the Error() and GetError() methods to find what the problem was)
    // If files folder is true then the file will only be opened if it is in the "files\" subfolder in the current directory.
    public GenericIStream *OpenRead(String filename, boolean filesFolder = true);
    public GenericOStream *OpenWrite(String filename, boolean filesFolder = true);
    public GenericIStream *OpenRead(String filename, boolean filesFolder, int& length);

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
        
    }

    // Delete a disk file
    public boolean Delete(String filename, boolean isSandboxMode);*/
}
