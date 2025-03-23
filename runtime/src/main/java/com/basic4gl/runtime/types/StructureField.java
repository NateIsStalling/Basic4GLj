package com.basic4gl.runtime.types;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 1 or more VmValTypes combined into a structure type.
 *
 * E.g.
 * Struc
 * dim angle#, names$ (100), x, y
 * EndStruc
 */
public class StructureField implements Streamable {

    /**
     * Field name
     */
    public String name;

    /**
     * Data type
     */
    public ValType type;

    /**
     * Data offset from top of structure
     */
    public int dataOffset;

    public StructureField(String name, ValType type) {
        this(name, type, 0);
    }

    public StructureField(String name, ValType type, int dataOffset) {
        this.name = name.toLowerCase();
        this.type = type;
        this.dataOffset = dataOffset;
    }

    public StructureField() {
        name = "";
        type = new ValType(BasicValType.VTP_INT);
        dataOffset = 0;
    }

    // Streaming
    public void streamOut(DataOutputStream stream) throws IOException {

        Streaming.writeString(stream, name);

        type.streamOut(stream);
        Streaming.writeLong(stream, dataOffset);
    }

    public boolean streamIn(DataInputStream stream) throws IOException {

        name = Streaming.readString(stream);

        type.streamIn(stream);
        dataOffset = (int) Streaming.readLong(stream);

        return true;
    }
}
