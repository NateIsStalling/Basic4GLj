package com.basic4gl.runtime.types;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static com.basic4gl.runtime.util.Assert.assertTrue;

public class Structure implements Streamable {
    /**
     * Type name
     */
    public String name;

    /**
     * Has structure been defined. False if being defined, or has been forward declared.
     */
    public boolean isDefined;

    /**
     * Index of first field
     */
    public int firstFieldIndex;

    /**
     * # of fields in structure
     */
    public int fieldCount;

    /**
     * Size of data
     */
    public int dataSize;

    /**
     * Contains one or more strings.
     * (Requires special handling when copying data.)
     */
    public boolean containsString;

    /**
     * Contains one or more arrays.
     * (Requires special handling when allocating data.)
     */
    public boolean containsArray;

    /**
     * Contains one or more pointers.
     * (Requires pointer validity checking when copying data.)
     */
    public boolean containsPointer;

    public Structure() {
        this("", 0);
    }

    public Structure(String name, int firstField) {
        this.name = name.toLowerCase();
        isDefined = false;
        firstFieldIndex = firstField;
        fieldCount = 0;
        dataSize = 0;
        containsString = false;
        containsArray = false;
        containsPointer = false;
    }

    // Streaming
    @Override
    public void streamOut(DataOutputStream stream) throws IOException {
        assertTrue(isDefined); // Definition should have been completed
        Streaming.writeString(stream, name);

        Streaming.writeLong(stream, firstFieldIndex);
        Streaming.writeLong(stream, fieldCount);
        Streaming.writeLong(stream, dataSize);
        Streaming.writeByte(stream, (byte) (containsString ? 1 : 0));
        Streaming.writeByte(stream, (byte) (containsArray ? 1 : 0));
    }

    @Override
    public boolean streamIn(DataInputStream stream) throws IOException {
        name = Streaming.readString(stream);

        firstFieldIndex = (int) Streaming.readLong(stream);
        fieldCount = (int) Streaming.readLong(stream);
        dataSize = (int) Streaming.readLong(stream);
        containsString = (Streaming.readByte(stream) == 1);
        containsArray = (Streaming.readByte(stream) == 1);

        // Only defined structures should be streamed.
        isDefined = true;

        return true;
    }
}
