package com.basic4gl.compiler;

import java.util.Map;
import java.util.Vector;

import com.basic4gl.compiler.util.CompSourcePos;
import com.basic4gl.compiler.util.ILineNumberMapping;
import com.basic4gl.util.Mutable;

////////////////////////////////////////////////////////////////////////////////
//compLineNumberMapping
//
/// Maps line numbers of the expanded file back to their corresponding source file.
/// Used by debugging code so that lines correctly match up with program addresses.
public class CompLineNumberMapping extends ILineNumberMapping {
	Vector<String> filenames;
	Map<String,Integer> filenameLookup;
	Vector<CompSourcePos> mapping;
	Vector<Vector<Integer>> reverseMapping;

	int GetFileIndex(String filename)
	{

		// Is file already in list?
		if (filenameLookup.containsKey(filename))
			return filenameLookup.get(filename);
		else {
			// Otherwise, add to list
			int index = filenames.size();
			filenameLookup.put(filename, index);
			filenames.add(filename);
			reverseMapping.add(new Vector<Integer>());
			return index;
		}
	}
	int SourceFromMain(int fileIndex, int lineNo)
	{
		// Is source line valid and does it correspond to a line inside the
		// specified file?
		if (lineNo >= 0 && lineNo < mapping.size() &&
				mapping.get(lineNo).getFileIndex() == fileIndex)
			return mapping.get(lineNo).getFileLineNo();
		else
			return -1;
	}
	int MainFromSource(int fileIndex, int fileLineNo)
	{
		// Check line is valid
		if (fileLineNo >= 0 && fileLineNo < reverseMapping.get(fileIndex).size())
			return reverseMapping.get(fileIndex).get(fileLineNo);
		else
			return -1;
	}

	// Mapping building
	public void Clear()
	{
		filenames.clear();
		filenameLookup.clear();
		mapping.clear();
		reverseMapping.clear();
	}
	public void AddLine(String filename, int fileLineNo)
	{

		// Append mapping entry
		int fileIndex = GetFileIndex(filename);
		int mainLineNo = mapping.size();
		mapping.add(new CompSourcePos(fileIndex, fileLineNo));

		// Append reverse-mapping entry
		while (reverseMapping.get(fileIndex).size() <= fileLineNo)
			reverseMapping.get(fileIndex).add(-1);
		reverseMapping.get(fileIndex).set(fileLineNo, mainLineNo);
	}

	// ILineNumberMapping methods
	@Override
	public void SourceFromMain(String filename, Mutable<Integer> fileLineNo, int lineNo)
	{

		// Is source line valid
		if (lineNo >= 0 && lineNo < mapping.size()) {

			// Return filename and line number
			filename = filenames.get(mapping.get(lineNo).getFileIndex());
			fileLineNo.set( mapping.get(lineNo).getFileLineNo());
		}
		else {

			// Invalid source line
			filename = "?";
			fileLineNo.set(-1);
		}
	}
	@Override
	public int SourceFromMain(String filename, int lineNo)
	{
		return SourceFromMain(GetFileIndex(filename), lineNo);
	}
	@Override
	public int MainFromSource(String filename, int fileLineNo)
	{
		return MainFromSource(GetFileIndex(filename), fileLineNo);
	}
}