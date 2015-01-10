package com.basic4gl.compiler;

/*  Created 2-Jun-2007: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

    Basic4GL compiler pre-processor.
*/

import java.io.File;
import java.util.*;

import com.basic4gl.compiler.util.ISourceFile;
import com.basic4gl.compiler.util.ISourceFileServer;
import com.basic4gl.vm.HasErrorState;







////////////////////////////////////////////////////////////////////////////////
//  Preprocessor
//
/// Basic4GL compiler preprocessor.
/// Note: Basic4GL doesn't do a lot of preprocessing. But we do implement an
///     #include file mechanism. The preprocessor has the task of transparently
///     expanding #includes into a single large source file.
public class Preprocessor extends HasErrorState {

    // Registered source file servers
    ArrayList<ISourceFileServer> fileServers;

    // Stack of currently opened files.
    // openFiles.back() is the current file being parsed
    Vector<ISourceFile> openFiles;

    // Filenames of visited source files. (To prevent circular includes)
    ArrayList<String> visitedFiles;

    // Source file <=> Processed file mapping
    LineNumberMapping lineNumberMap;

    void CloseAll()
    {

        // Close all open files
        for (int i = 0; i < openFiles.size(); i++)
            openFiles.get(i).Release();
        openFiles.clear();
    }
    ISourceFile OpenFile(String filename)
    {

        // Query file servers in order until one returns an open file.
    	for(ISourceFileServer server: fileServers){
            ISourceFile file = server.OpenSourceFile(filename);
            if (file != null)
                return file;
        }

        // Unable to open file
        return null;
    }


    /// Construct the preprocessor. Pass in 0 or more file servers to initialise.
    public Preprocessor(int serverCount, ISourceFileServer... server){
        // Register source file servers
        for (int i = 0; i < serverCount; i++)
            fileServers.add(server[i]);
    }
    protected void finalize() //virtual ~Preprocessor();
    {

        // Ensure no source files are still open
        CloseAll();
        
     // Delete source file servers
        fileServers.clear();
        fileServers = null;
    }
    /// Process source file into one large file.
    /// Parser is initialised with the expanded file.
    public boolean Preprocess(ISourceFile mainFile, Parser parser)
    {
        assert(mainFile != null);

        // Reset
        CloseAll();
        visitedFiles.clear();
        lineNumberMap.Clear();
        ClearError();

        // Clear the parser
        parser.SourceCode().clear();

        // Load the main file
        openFiles.add(mainFile);

        // Process files
        while (!openFiles.isEmpty() && !Error()) {
            // Check for Eof
            if (openFiles.lastElement().Eof()) {

                // Close innermost file
                openFiles.lastElement().Release();
                openFiles.remove(openFiles.size()-1);
            }
            else {

                // Read a line from the source file
                int lineNo = openFiles.lastElement().LineNumber();
                String line = openFiles.lastElement().GetNextLine();

                // Check for #include
                if (line.substring(0, 8).toLowerCase().equals("include ")) {

                    // Get filename
                    String filename = new File(line.substring(8, line.length())).getAbsolutePath();

                    // Check this file hasn't been included already
                    if (!visitedFiles.contains(filename)) {

                        // Open next file
                        ISourceFile file = OpenFile(filename);
                        if (file == null)
                            SetError("Unable to open file: " + line.substring(8, line.length()));
                        else {
                            // This becomes the new innermost file
                            openFiles.add(file);

                            // Add to visited files list
                            visitedFiles.add(0,filename);
                        }
                    }
                }
                else {
                    // Not an #include line
                    // Add to parser, and line number map
                    lineNumberMap.AddLine(openFiles.lastElement().Filename(), lineNo);
                    parser.SourceCode().add(line);
                }
            }
        }

        // Return true if no error encountered
        return !Error();     
    }
    /// Member access
    public LineNumberMapping LineNumberMap() { return lineNumberMap; }
}
