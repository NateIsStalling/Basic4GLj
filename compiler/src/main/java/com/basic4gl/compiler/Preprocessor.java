package com.basic4gl.compiler;

/*  Created 2-Jun-2007: Thomas Mulgrew (tmulgrew@slingshot.co.nz)

	Basic4GL compiler pre-processor.
*/

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.compiler.util.ISourceFile;
import com.basic4gl.compiler.util.ISourceFileServer;
import com.basic4gl.runtime.HasErrorState;
import java.io.File;
import java.util.*;

/**
 * Preprocessor
 *
 * Basic4GL compiler preprocessor.
 * Note: Basic4GL doesn't do a lot of preprocessing. But we do implement an
 * #include file mechanism. The preprocessor has the task of transparently
 * expanding #includes into a single large source file.
 */
public class Preprocessor extends HasErrorState {

// Registered source file servers
private List<ISourceFileServer> fileServers = new ArrayList<>();

// Stack of currently opened files.
// openFiles.back() is the current file being parsed
private Vector<ISourceFile> openFiles = new Vector<>();

// Filenames of visited source files. (To prevent circular includes)
private List<String> visitedFiles = new ArrayList<>();

// Source file <=> Processed file mapping
private LineNumberMapping lineNumberMap = new LineNumberMapping();

void closeAll() {

	// Close all open files
	for (int i = 0; i < openFiles.size(); i++) {
	openFiles.get(i).release();
	}
	openFiles.clear();
}

ISourceFile openFile(String filename) {
	System.out.println("Preprocessing include file: \n" + filename);
	// Query file servers in order until one returns an open file.
	for (ISourceFileServer server : fileServers) {
	ISourceFile file = server.openSourceFile(filename);
	if (file != null) {
		return file;
	}
	}

	// Unable to open file
	return null;
}

/**
* Construct the preprocessor. Pass in 0 or more file servers to initialise.
*/
public Preprocessor(int serverCount, ISourceFileServer... server) {
	// Register source file servers
	for (int i = 0; i < serverCount; i++) {
	fileServers.add(server[i]);
	}
}

protected void finalize() // virtual ~Preprocessor();
	{

	// Ensure no source files are still open
	closeAll();

	// Delete source file servers
	fileServers.clear();
	fileServers = null;
}

/**
* Process source file into one large file.
* Parser is initialised with the expanded file.
*/
public boolean preprocess(ISourceFile mainFile, Parser parser) {
	assertTrue(mainFile != null);

	// Reset
	closeAll();
	visitedFiles.clear();
	lineNumberMap.clear();
	clearError();

	// Clear the parser
	parser.getSourceCode().clear();

	// Load the main file
	openFiles.add(mainFile);

	// Process files
	while (!openFiles.isEmpty() && !hasError()) {
	// Check for Eof
	if (openFiles.lastElement().isEof()) {

		// Close innermost file
		openFiles.lastElement().release();
		openFiles.remove(openFiles.size() - 1);
	} else {

		// Read a line from the source file
		int lineNo = openFiles.lastElement().getLineNumber();
		String line = openFiles.lastElement().getNextLine();

		// Check for #include
		boolean include =
			(line.length() >= 8 && line.substring(0, 8).toLowerCase().equals("include "));
		if (include) {

		// Get filename
		String includeName = separatorsToSystem(line.substring(8, line.length()).trim());
		String parent = new File(mainFile.getFilename()).getParent(); // Parent directory
		String filename = new File(parent, includeName).getAbsolutePath();

		// Check this file hasn't been included already
		if (!visitedFiles.contains(filename)) {

			// Open next file
			ISourceFile file = openFile(filename);
			if (file == null) {
			setError("Unable to open file: " + includeName);
			} else {
			// This becomes the new innermost file
			openFiles.add(file);

			// Add to visited files list
			visitedFiles.add(0, filename);
			}
		} else {
			setError("File already included: " + includeName);
		}
		} else {
		// Not an #include line
		// Add to parser, and line number map
		lineNumberMap.addLine(openFiles.lastElement().getFilename(), lineNo);
		parser.getSourceCode().add(line);
		}
	}
	}

	// Return true if no error encountered
	return !hasError();
}

public LineNumberMapping getLineNumberMap() {
	return lineNumberMap;
}

String separatorsToSystem(String res) {
	if (res == null) {
	return null;
	}
	if (File.separatorChar == '\\') {
	// From Windows to Linux/Mac
	return res.replace('/', File.separatorChar);
	} else {
	// From Linux/Mac to Windows
	return res.replace('\\', File.separatorChar);
	}
}
}
