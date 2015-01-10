package com.basic4gl.vm;
import java.util.*;

import com.basic4gl.compiler.util.ILineNumberMapping;
import com.basic4gl.vm.util.IVMDebugger;
import com.basic4gl.vm.util.SourceUserBreakPt;

public class Debugger extends IVMDebugger {
	Vector<SourceUserBreakPt> userBreakPts;
    ILineNumberMapping mapping;

    // Return breakpoint position or userBreakPts.end() if not found.
    SourceUserBreakPt FindBreakPt(String filename, int lineNo){

        // Search for matching breakpt
        // Stop when end is reached or breakpt found
        for (SourceUserBreakPt pt: userBreakPts){
        	if (pt.sourceFile.equals(filename) && pt.lineNo == lineNo){
        		return pt;
        	}
        }
        return null;
    }

    public Debugger(ILineNumberMapping _mapping){
    	mapping = _mapping;
    }

    // UI interface
    public void ClearUserBreakPts()
    {
        userBreakPts.clear();
    }
    public void AddUserBreakPt(String filename, int lineNo)
    {
        if (!IsUserBreakPt(filename, lineNo))
            userBreakPts.add(new SourceUserBreakPt(filename, lineNo));
    }
    public void RemoveUserBreakPt(String filename, int lineNo)
    {
       userBreakPts.remove(FindBreakPt(filename, lineNo));
    }
    public void ToggleUserBreakPt(String filename, int lineNo)
    {
        if (IsUserBreakPt(filename, lineNo))
            RemoveUserBreakPt(filename, lineNo);
        else
            AddUserBreakPt(filename, lineNo);
    }
    public boolean IsUserBreakPt(String filename, int lineNo)
    {
        return FindBreakPt(filename, lineNo) != userBreakPts.lastElement();
    }

    /// Adjust debugging information in response to an insertion/deletion of
    /// one or more lines.
    /// If delta is positive, |delta| lines will be inserted.
    /// If delta is negative, |delta| lines will be deleted.
    /// Breakpoints will be moved (or deleted) appropriately.
    public void InsertDeleteLines(String filename, int fileLineNo, int delta)
    {

        // Move or remove user breakpoints
        for(SourceUserBreakPt i : userBreakPts)
        {
            if (i.sourceFile.equals(filename)) {

                // Breakpoints before modified line(s) are unaffected
                if (i.lineNo >= fileLineNo) {
                    if (delta >= 0) {
                        // Insert
                        i.lineNo += delta;
                        continue;
                    }
                    else {
                        // Delete
                        // Is breakpoint on a deleted line?
                        if (i.lineNo < fileLineNo - delta){
                            userBreakPts.remove(i);
                            continue;
                        }
                        else {
                            i.lineNo += delta;
                            continue;
                        }
                    }
                }
                else
                	continue;
            }
            else
            	continue;
        }
    
    }

    // IVMDebugger methods
    public int UserBreakPtCount(){
        return userBreakPts.size();
    }
    public int UserBreakPtLine(int index){
        assert(index >= 0);
        assert(index < UserBreakPtCount());

        // Find breakpoint and convert to main line number
        return mapping.MainFromSource(
            userBreakPts.get(index).sourceFile,
            userBreakPts.get(index).lineNo);
    }
}
