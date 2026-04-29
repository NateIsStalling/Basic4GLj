package com.basic4gl.runtime;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.stackframe.RuntimeFunction;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Represents a block of code.
 * The program is code block 0. Any other files/strings compiled at run time
 * are also separate code blocks.
 */
public class CodeBlock implements Streamable {
    /**
     * -1 if code block is invalid (e.g. because of compile error)
     */
    public int programOffset;

    private String filename = "";

    public Vector<RuntimeFunction> runtimeFunctions = new Vector<>();
    public HashMap<String, Integer> userFunctions = new HashMap<>();

    public CodeBlock() {
        programOffset = -1;
    }

    public void setLengthAtLeast(int length) {
        int size = runtimeFunctions.size();
        if (size < length) {
            runtimeFunctions.setSize(length);
            for (int i = size; i < length; i++) {
                runtimeFunctions.set(i, new RuntimeFunction());
            }
        }
    }

    public RuntimeFunction getRuntimeFunction(int index) {
        assertTrue(index >= 0);
        setLengthAtLeast(index + 1);
        return runtimeFunctions.get(index);
    }

    public void streamOut(DataOutputStream stream) throws IOException {

        Streaming.writeString(stream, filename);

        Streaming.writeLong(stream, programOffset);
        Streaming.writeLong(stream, runtimeFunctions.size());

        for (RuntimeFunction f : runtimeFunctions) {
            f.streamOut(stream);
        }

        // User functions
        Streaming.writeLong(stream, userFunctions.size());
        for (Map.Entry<String, Integer> fn : userFunctions.entrySet())
        {
            Streaming.writeString(stream, fn.getKey());
            Streaming.writeLong(stream, fn.getValue());
        }
    }

    public boolean streamIn(DataInputStream stream) throws IOException {

        filename = Streaming.readString(stream);

        programOffset = (int) Streaming.readLong(stream);
        int count = (int) Streaming.readLong(stream);
        runtimeFunctions.setSize(count);

        for (int i = 0; i < count; i++) {
            runtimeFunctions.set(i, new RuntimeFunction());
            runtimeFunctions.get(i).streamIn(stream);
        }

        // User functions
        userFunctions.clear();
        count = (int) Streaming.readLong(stream);
        for (int i = 0; i < count; i++)
        {
            String name = Streaming.readString(stream);
            int index = (int) Streaming.readLong(stream);
            userFunctions.put(name, index);
        }
        return true;
    }

    public void setFilename(String filename) {
        this.filename = prepCodeBlockFilename(filename);
    }

    public String getFilename() {
        return filename;
    }

    public boolean filenameEquals(String otherFilename) {
        return prepCodeBlockFilename(otherFilename).equals(filename);
    }

    private String prepCodeBlockFilename(String filename)
    {
        if (filename == null) {
            return "";
        }
        // Convert to lowercase
        String result = filename.toLowerCase();

        result = separatorsToSystem(result);

        return result;
    }

    String separatorsToSystem(String res) {
        if (res == null) {
            return null;
        }
        if (File.separatorChar == '\\') {
            // From Windows to Linux/Mac
            return res.replace('/', File.separatorChar);
        } else {
            // From Linux/Mac to Windows
            return res.replace('\\', File.separatorChar);
        }
    }
}
