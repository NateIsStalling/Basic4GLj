package com.basic4gl.compiler;

import com.basic4gl.language.core.streaming.Streamable;
import com.basic4gl.language.core.streaming.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A program label, i.e. a named destination for "goto" and "gosub"s
 */
class Label implements Streamable {
    private int offset;

    private int programDataOffset;

    Label(int offset, int dataOffset) {
        this.offset = offset;
        programDataOffset = dataOffset;
    }

    Label() {
        offset = 0;
        programDataOffset = 0;
    }

    public void streamOut(DataOutputStream stream) throws IOException {
        Streaming.writeLong(stream, offset);
        Streaming.writeLong(stream, programDataOffset);
    }

    public boolean streamIn(DataInputStream stream) throws IOException {
        offset = (int) Streaming.readLong(stream);
        programDataOffset = (int) Streaming.readLong(stream);

        return true;
    }

    /**
     * Instruction index in code
     */
    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Program data offset. (For use with "RESET labelname" command.)
     */
    public int getProgramDataOffset() {
        return programDataOffset;
    }

    public void setProgramDataOffset(int programDataOffset) {
        this.programDataOffset = programDataOffset;
    }
}
