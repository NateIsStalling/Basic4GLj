package com.basic4gl.runtime.stackframe;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Compiler Runtime Function
 */
public class RuntimeFunction implements Streamable {

    /**
     * Index of implementing function, or -1 if none.
     */
    public int functionIndex;

    public RuntimeFunction() {
        functionIndex = -1;
    }

    public RuntimeFunction(int functionIndex) {
        this.functionIndex = functionIndex;
    }

    @Override
    public void streamOut(DataOutputStream stream) throws IOException {
        Streaming.writeLong(stream, functionIndex);
    }

    @Override
    public boolean streamIn(DataInputStream stream) throws IOException {
        functionIndex = (int) Streaming.readLong(stream);

        return true;
    }
}
