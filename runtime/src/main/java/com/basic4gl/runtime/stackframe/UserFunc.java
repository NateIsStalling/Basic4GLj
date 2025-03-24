package com.basic4gl.runtime.stackframe;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UserFunc implements Streamable {
    public int prototypeIndex;
    public boolean implemented;
    public int programOffset;

    public UserFunc() {}

    public UserFunc(int prototypeIndex, boolean implemented) {
        this(prototypeIndex, implemented, -1);
    }

    public UserFunc(int prototypeIndex, boolean implemented, int programOffset) {
        this.prototypeIndex = prototypeIndex;
        this.implemented = implemented;
        this.programOffset = programOffset;
    }

    public void streamOut(DataOutputStream stream) throws IOException {
        // Assume program is complete, i.e all functions are implemented, before
        // streaming occurs.
        assertTrue(implemented);

        Streaming.writeLong(stream, prototypeIndex);
        Streaming.writeLong(stream, programOffset);
    }

    public boolean streamIn(DataInputStream stream) throws IOException {
        prototypeIndex = (int) Streaming.readLong(stream);
        programOffset = (int) Streaming.readLong(stream);

        implemented = true;

        return true;
    }
}
