package com.basic4gl.lib.util;

// EmbeddedFile
//
// A single file, embedded into the executable
public class EmbeddedFile {
    /*String m_filename;
    int			m_length;
    char		*m_data;

    public EmbeddedFile ()
    {
        m_filename = "";
        m_length = 0;
        m_data = null;
    }
    public EmbeddedFile (char *rawData){
        int offset = 0;
        this(rawData, offset);
    }
    public EmbeddedFile (char *rawData, int& offset){
        assert (rawData != null);

        // Read filename length
        int nameLength = *((int *) (rawData + offset));
        offset += sizeof (nameLength);

        // Read filename
        m_filename = rawData + offset;
        offset += nameLength;

        // Read length
        m_length = *((int *) (rawData + offset));
        offset += sizeof (m_length);

        // Save pointer to data
        m_data = rawData + offset;
        offset += m_length;
    }
    public String Filename () { return m_filename; }
    public GenericIStream *AsStream ()		// Return file as a generic input stream
    {
        std::stringstream *result = new std::stringstream;		// Use a string stream as temp buffer

        result->write (m_data, m_length);						// Copy file data into it
        result->seekg (0, std::ios::beg);						// Reset to start
        return result;
    }
    public int Length() { return m_length; }*/
}
