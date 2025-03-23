package com.basic4gl.runtime;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.util.ILineNumberMapping;
import com.basic4gl.runtime.util.IVMDebugger;
import com.basic4gl.runtime.util.SourceUserBreakPt;
import java.util.*;

public class Debugger extends IVMDebugger {
  private Vector<SourceUserBreakPt> mUserBreakPts = new Vector<>();
  private ILineNumberMapping mMapping;

  // Return breakpoint position or mUserBreakPts.end() if not found.
  SourceUserBreakPt findBreakPoint(String filename, int lineNo) {

    // Search for matching breakpt
    // Stop when end is reached or breakpt found
    for (SourceUserBreakPt pt : mUserBreakPts) {
      if (pt.sourceFile.equals(filename) && pt.lineNo == lineNo) {
        return pt;
      }
    }
    return null;
  }

  public Debugger(ILineNumberMapping mapping) {
    mMapping = mapping;
  }

  // UI interface
  public void clearUserBreakPoints() {
    mUserBreakPts.clear();
  }

  public void clearUserBreakPoints(String filename) {
    Iterator<SourceUserBreakPt> i = mUserBreakPts.iterator();
    while (i.hasNext()) {
      SourceUserBreakPt breakPt = i.next();
      if (breakPt.sourceFile.equals(filename)) {
        i.remove();
      }
    }
  }

  public void addUserBreakPoint(String filename, int lineNo) {
    if (!isUserBreakPoint(filename, lineNo)) {
      mUserBreakPts.add(new SourceUserBreakPt(filename, lineNo));
    }
  }

  public void removeUserBreakPoint(String filename, int lineNo) {
    mUserBreakPts.remove(findBreakPoint(filename, lineNo));
  }

  public boolean toggleUserBreakPoint(String filename, int lineNo) {
    if (isUserBreakPoint(filename, lineNo)) {
      removeUserBreakPoint(filename, lineNo);
      return false;
    } else {
      addUserBreakPoint(filename, lineNo);
      return true;
    }
  }

  public boolean isUserBreakPoint(String filename, int lineNo) {
    return findBreakPoint(filename, lineNo) != null;
  }

  /**
   * Adjust debugging information in response to an insertion/deletion of
   * one or more lines.
   * If delta is positive, |delta| lines will be inserted.
   * If delta is negative, |delta| lines will be deleted.
   * Breakpoints will be moved (or deleted) appropriately.
   */
  public void insertDeleteLines(String filename, int fileLineNo, int delta) {

    // Move or remove user breakpoints
    for (SourceUserBreakPt i : mUserBreakPts) {
      if (i.sourceFile.equals(filename)) {

        // Breakpoints before modified line(s) are unaffected
        if (i.lineNo >= fileLineNo) {
          if (delta >= 0) {
            // Insert
            i.lineNo += delta;
          } else {
            // Delete
            // Is breakpoint on a deleted line?
            if (i.lineNo < fileLineNo - delta) {
              mUserBreakPts.remove(i);
            } else {
              i.lineNo += delta;
            }
          }
        }
      }
    }
  }

  // IVMDebugger methods
  public int getUserBreakPointCount() {
    return mUserBreakPts.size();
  }

  public int getUserBreakPointLine(int index) {
    assertTrue(index >= 0);
    assertTrue(index < getUserBreakPointCount());

    // Find breakpoint and convert to main line number
    return mMapping.getMainFromSource(
        mUserBreakPts.get(index).sourceFile, mUserBreakPts.get(index).lineNo);
  }
}
