package com.basic4gl.lib.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// EmbeddedFiles
//
// A set of embedded files, keyed by relative filename
public class EmbeddedFiles {
    private Map<String,EmbeddedFile> m_files = new HashMap<>();

    public EmbeddedFiles () { ; };
    public EmbeddedFiles (ByteBuffer rawData){
        this(rawData, IntBuffer.allocate(0).put(0, 0));
    }
    public EmbeddedFiles (ByteBuffer rawData, IntBuffer offset){
        AddFiles (rawData, offset);
    }
    public void AddFiles (ByteBuffer rawData, IntBuffer offset){
        assert (rawData != null);

        // Read # of files
        rawData.position(rawData.position() + offset.get(0));//*((int *) (rawData + offset));
        int count = rawData.getInt();
        offset.put(0,offset.get(0) + (Integer.SIZE / Byte.SIZE));

        // Read in each file
        for (int i = 0; i < count; i++) {
            EmbeddedFile f = new EmbeddedFile(rawData, offset);
            m_files.put(f.Filename (), f);
        }
    }

    public boolean IsStored (String filename){
        String processedName = new File(filename).getAbsolutePath();
        return m_files.containsKey (processedName);
    }

    // Find stream.
    // Caller must free
    public FileInputStream Open        (String filename)		// Opens file. Returns NULL if not present.
    {
        return IsStored (filename)
                ? m_files.get(new File(filename).getAbsolutePath()).AsStream()
                : null;
    }
    public FileInputStream Open        (String filename, IntBuffer length)
    {
        if (IsStored(filename)) {
            EmbeddedFile file = m_files.get( new File(filename).getAbsolutePath());
            length.put(0, file.Length());
            return file.AsStream();
        }
        else {
            length.put(0, 0);
            return null;
        }
    }
    public FileInputStream OpenOrLoad  (String filename)		// Opens file. Falls back to disk if not present. Returns NULL if not present OR on disk
    {

        // Try embedded files first
        FileInputStream result = Open (filename);
        if (result == null) {

            // Otherwise try to load from file
            FileInputStream diskFile = null;
            try {
                diskFile = new FileInputStream(new File(filename));
                result = diskFile;
            } catch (FileNotFoundException e) {
                result = null;
            }
        }
        return result;
    }
    // Routines

    // Copy a GenericIStream into a GenericOStream
    public static void CopyStream (InputStream src, OutputStream dst) { CopyStream(src, dst, -1);}
    public static void CopyStream (InputStream src, OutputStream dst, long len)
    {

        // Copy stream to stream
        ByteBuffer buffer = ByteBuffer.allocate(0x4000);
        while (len > 0x4000 || len < 0) {
            buffer.mark();
            try {
                src.read (buffer.array(),buffer.arrayOffset(), 0x4000);
                buffer.reset();
                dst.write (buffer.array(),buffer.arrayOffset(), 0x4000);
                len -= 0x4000;
            } catch (IOException e) {
                len = 0;
                e.printStackTrace();
                break;
            }
        }
        if (len > 0) {
            try {
                src.read (buffer.array(),buffer.arrayOffset(), (int)len);
                dst.write (buffer.array(),buffer.arrayOffset(), (int)len);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Create embedded representation of stream
    static boolean EmbedFile (String filename, OutputStream stream)
    {

        // Open file
        File file;
        FileInputStream fileStream = null;
        try {
            file = new File(filename);
            fileStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return false;
        }

        // Convert filename to relative
        String relName =  new File(filename).getAbsolutePath();

        // Calculate lengths
        int nameLen = relName.length () + 1;		// +1 for 0 terminator
        LongBuffer fileLen = LongBuffer.allocate(1).put(0, file.length());

        // Write data to stream
        try {
            stream.write(ByteBuffer.allocate(4).putInt(nameLen).array());
            stream.write(relName.getBytes(StandardCharsets.UTF_8));
            stream.write(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(fileLen.get()).array());
            CopyStream(new FileInputStream(file), stream, fileLen.get());
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
