package com.basic4gl.runtime.stackframe;

import com.basic4gl.runtime.util.CollectionUtil;
import java.util.ArrayList;

/**
 * Stack frame created when a user function is called.
 */
public class UserFuncStackFrame {

    // Corresponding function definition. -1 if is a simple GOSUB
    public int userFuncIndex;

    // Return address
    public int returnAddr;

    // Previous stack frame info
    // Ignored for GOSUBs
    public int prevStackTop;
    public int prevTempDataLock;
    public int prevCurrentFrame;

    // Local variables and parameters
    // Stores offset of each variable in data array (0 = unallocated).
    public ArrayList<Integer> localVarDataOffsets;

    public UserFuncStackFrame() {
        localVarDataOffsets = new ArrayList<>();
        resetForReuse();
    }

    public void resetForReuse() {
        userFuncIndex = -1;
        returnAddr = -1;
        prevStackTop = 0;
        prevTempDataLock = 0;
        prevCurrentFrame = -1;
        localVarDataOffsets.clear();
    }

    public void initForGosub(int returnAddress) {
        resetForReuse();
        userFuncIndex = -1;
        returnAddr = returnAddress;
    }

    public void initForUserFunction(UserFuncPrototype prototype, int userFuncIndex) {
        resetForReuse();

        this.userFuncIndex = userFuncIndex;

        int newSize = prototype.localVarTypes.size();
        CollectionUtil.resize(localVarDataOffsets, newSize);

        for (int i = 0; i < newSize; i++) {
            localVarDataOffsets.set(i, 0);
        }
    }
}