package com.basic4gl.lib.util;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import static com.basic4gl.runtime.util.Assert.assertTrue;

// EmbeddedFile
//
// A single file, embedded into the executable
public class EmbeddedFile {
    String m_filename;
    int			m_length;
    FileInputStream  m_data;

    public EmbeddedFile ()
    {
        m_filename = "";
        m_length = 0;
        m_data = null;
    }
    public EmbeddedFile (ByteBuffer rawData){
        this(rawData, IntBuffer.allocate(1).put(0, 0));
    }
    public EmbeddedFile (ByteBuffer rawData, IntBuffer offset){
        assertTrue(rawData != null);

        // Read filename length
        rawData.position(rawData.position() + offset.get());
        int nameLength = rawData.asIntBuffer().get(); //*((int *) (rawData + offset));
        offset.put(0, offset.get(0) + Integer.SIZE/Byte.SIZE);

        // Read filename
        byte[] name = new byte[nameLength];
        rawData.position(rawData.position() + offset.get());
        rawData.get(name);
        m_filename = new String(name, Charset.forName("UTF-8"));
        offset.put(0, offset.get(0) + nameLength);

        // Read length
        rawData.position(rawData.position() + offset.get());
        m_length = rawData.asIntBuffer().get(); //*((int *) (rawData + offset));
        offset.put(0, offset.get(0) + Integer.SIZE/Byte.SIZE);

        // Save pointer to data
        rawData.position(rawData.position() + offset.get());
        //m_data = rawData.position() + offset.get();
        offset.put(0, offset.get(0) + m_length);
    }
    public String Filename () { return m_filename; }
    public FileInputStream AsStream ()		// Return file as a generic input stream
    {
        return m_data;
    }
    public int Length() { return m_length; }
}
