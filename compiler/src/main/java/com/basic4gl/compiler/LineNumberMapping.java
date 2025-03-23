package com.basic4gl.compiler;

import com.basic4gl.compiler.util.SourcePosition;
import com.basic4gl.runtime.util.ILineNumberMapping;
import com.basic4gl.runtime.util.Mutable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Compiler line number mapping.
 *
 * Maps line numbers of the expanded file back to their corresponding source file.
 * Used by debugging code so that lines correctly match up with program addresses.
 */
public class LineNumberMapping extends ILineNumberMapping implements Serializable {
    private final Vector<String> filenames = new Vector<>();
    private final Map<String, Integer> filenameLookup = new HashMap<>();
    private final Vector<SourcePosition> mapping = new Vector<>();
    private final Vector<Vector<Integer>> reverseMapping = new Vector<>();

    int getFileIndex(String filename) {

        // Is file already in list?
        if (filenameLookup.containsKey(filename)) {
            return filenameLookup.get(filename);
        } else {
            // Otherwise, add to list
            int index = filenames.size();
            filenameLookup.put(filename, index);
            filenames.add(filename);
            reverseMapping.add(new Vector<>());
            return index;
        }
    }

    int getSourceFromMain(int fileIndex, int lineNo) {
        // Is source line valid and does it correspond to a line inside the
        // specified file?
        if (lineNo >= 0 && lineNo < mapping.size() && mapping.get(lineNo).getFileIndex() == fileIndex) {
            return mapping.get(lineNo).getFileLineNo();
        } else {
            return -1;
        }
    }

    int getMainFromSource(int fileIndex, int fileLineNo) {
        // Check line is valid
        if (fileLineNo >= 0 && fileLineNo < reverseMapping.get(fileIndex).size()) {
            return reverseMapping.get(fileIndex).get(fileLineNo);
        } else {
            return -1;
        }
    }

    // Mapping building
    public void clear() {
        filenames.clear();
        filenameLookup.clear();
        mapping.clear();
        reverseMapping.clear();
    }

    public void addLine(String filename, int fileLineNo) {

        // Append mapping entry
        int fileIndex = getFileIndex(filename);
        int mainLineNo = mapping.size();
        mapping.add(new SourcePosition(fileIndex, fileLineNo));

        // Append reverse-mapping entry
        while (reverseMapping.get(fileIndex).size() <= fileLineNo) {
            reverseMapping.get(fileIndex).add(-1);
        }
        reverseMapping.get(fileIndex).set(fileLineNo, mainLineNo);
    }

    // ILineNumberMapping methods
    @Override
    public void getSourceFromMain(Mutable<String> filename, Mutable<Integer> fileLineNo, int lineNo) {

        // Is source line valid
        if (lineNo >= 0 && lineNo < mapping.size()) {

            // Return filename and line number
            filename.set(filenames.get(mapping.get(lineNo).getFileIndex()));
            fileLineNo.set(mapping.get(lineNo).getFileLineNo());
        } else {

            // Invalid source line
            filename.set("?");
            fileLineNo.set(-1);
        }
    }

    @Override
    public int getSourceFromMain(String filename, int lineNo) {
        return getSourceFromMain(getFileIndex(filename), lineNo);
    }

    @Override
    public int getMainFromSource(String filename, int fileLineNo) {
        return getMainFromSource(getFileIndex(filename), fileLineNo);
    }
}
