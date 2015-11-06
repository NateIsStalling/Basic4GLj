package com.basic4gl.lib.util;

import java.util.Map;

// EmbeddedFiles
//
// A set of embedded files, keyed by relative filename
public class EmbeddedFiles {
    /*private Map<String,EmbeddedFile> m_files;

    public EmbeddedFiles () { ; };
    public EmbeddedFiles (char *rawData){
        int offset = 0;
        this(rawData, offset);
    }
    public EmbeddedFiles (char *rawData, int& offset){
        AddFiles (rawData, offset);
    }
    public void AddFiles (char *rawData, int& offset){
        assert (rawData != null);

        // Read # of files
        int count = *((int *) (rawData + offset));
        offset += sizeof (count);

        // Read in each file
        for (int i = 0; i < count; i++) {
            EmbeddedFile f = new EmbeddedFile(rawData, offset);
            m_files [f.Filename ()] = f;
        }
    }

    public boolean IsStored (String filename){
        String processedName = ProcessPath (filename);
        return m_files.find (processedName) != m_files.end ();
    }

    // Find stream.
    // Caller must free
    public GenericIStream *Open        (String filename)		// Opens file. Returns NULL if not present.
    {
        return IsStored (filename)
                ? m_files [ProcessPath (filename)].AsStream ()
                : null;
    }
    public GenericIStream *Open        (String filename, int& length)
    {
        return IsStored (filename)
                ? m_files [ProcessPath (filename)].AsStream ()
                : null;
    }
    public GenericIStream *OpenOrLoad  (String filename);		// Opens file. Falls back to disk if not present. Returns NULL if not present OR on disk
    {

        // Try embedded files first
        GenericIStream *result = Open (filename);
        if (result == null) {

            // Otherwise try to load from file
            std::ifstream *diskFile = new std::ifstream (filename.c_str (), std::ios::binary | std::ios::in);
            if (!diskFile->fail ())
                result = diskFile;
            else
                delete diskFile;
        }
        return result;
    }
    public GenericIStream *OpenOrLoad  (String filename, int& length);
    {

        // 17-Apr-06:
        // There's an issue with Borland C++ v5.5's implementation of stringstream
        // in that:
        //      seekg(0, std::ios::end);
        //      length = tellg();
        // always sets length to 0.
        //
        // Therefore we have this method for returning a file stream AND its length.
        // If the file is stored as an embedded file, we simply return the length
        // from the embedded file.
        // If the file is loaded from disk we use seekg and tellg to get the file
        // length (which still works fine for ifstream streams).

        GenericIStream *result = Open(filename, length);
        if (result == null) {
            // Otherwise try to load from file
            std::ifstream *diskFile = new std::ifstream (filename.c_str (), std::ios::binary | std::ios::in);
            if (!diskFile->fail ()) {

                // Get file length
                diskFile->seekg(0, std::ios::end);      // Seek to end
                length = diskFile->tellg();             // Return length
                diskFile->seekg(0, std::ios::beg);      // Seek back to beginning

                result = diskFile;
            }
            else
                delete diskFile;
        }
        return result;
    }
    // Routines

    // Copy a GenericIStream into a GenericOStream
    static void CopyStream (GenericIStream& src, GenericOStream& dst, int len = -1)
    {

        // Copy stream to stream
        char buffer [0x4000];
        while (len > 0x4000 || len < 0 && !src.fail()) {
            src.read (buffer, 0x4000);
            dst.write (buffer, 0x4000);
            len -= 0x4000;
        }
        if (len > 0) {
            src.read (buffer, len);
            dst.write (buffer, len);
        }
    }

    // Create embedded representation of stream
    static boolean EmbedFile (String filename, GenericOStream& stream)
    {

        // Open file
        std::ifstream file (filename.c_str (), std::ios::binary | std::ios::in);
        if (file.fail ())
            return false;

        // Convert filename to relative
        std::string relName = ProcessPath (filename);

        // Calculate lengths
        int nameLen = relName.length () + 1;		// +1 for 0 terminator
        file.seekg (0, std::ios::end);
        int fileLen = file.tellg ();
        file.seekg (0, std::ios::beg);

        // Write data to stream
        stream.write ((char *) &nameLen, sizeof (nameLen));
        stream.write (relName.c_str (), nameLen);
        stream.write ((char *) &fileLen, sizeof (fileLen));
        CopyStream (file, stream, fileLen);
        return true;
    }*/
}
