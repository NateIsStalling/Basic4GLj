package com.basic4gl.language.core.runtime;

import com.basic4gl.language.core.streaming.Streamable;
import com.basic4gl.language.core.streaming.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * General purpose program data (as allocated with "DATA" statement in BASIC).
 */
public class ProgramDataElement implements Streamable {
    private int basicType;
    private Value value;

    public int getType() {
        return basicType;
    }

    public Value getValue() {
        return value;
    }

    public void setType(int type) {
        basicType = type;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    // Streaming
    public void streamOut(DataOutputStream stream) throws IOException {
        Streaming.writeLong(stream, basicType);
        value.streamOut(stream);
    }

    public boolean streamIn(DataInputStream stream) throws IOException {
        basicType = (int) Streaming.readLong(stream);
        value = new Value();
        value.streamIn(stream);
        return true;
    }
}
