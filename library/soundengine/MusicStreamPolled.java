package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.runtime.vm.HasErrorState;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.stb.STBVorbisInfo;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBVorbis.*;

///////////////////////////////////////////////////////////////////////////////
//	MusicStreamPolled
//
///	A polled implementation of a streaming music file.
/// Plays an Ogg Vorbis file using OpenAL.
/// Calling code must continually call ::Update() to ensure the
/// music keeps playing.
///
/// Note: For a version that doesn't require continuous polling,
/// use class "MusicStream" instead. ("MusicStream" uses
/// "MusicStreamPolled internally, but automates polling itself.)
public class MusicStreamPolled extends HasErrorState {
    static final int MUSICSTREAMBUFFERS = 64;
    static final int STREAMBLOCKSIZE = 4096;
    // Note: This is about enough space for 1 second of
    // stereo 44KHz 16bit music.
    // Our update frequency is 10 times per second, which
    // should be plenty enough to ensure that the stream
    // won't be interrupted.

    // File access
    File file;
    long ogg;
    //ov_callbacks	callbacks;
    // Used by VorbisFile to access the file data.

    // File info
    int freq;
    int format;
    int channels;

    // OpenAL objects
    ByteBuffer source;                // OpenAL source. Music is streamed through this source
    ByteBuffer buffers;            // OpenAL buffers. Filled with music data and streamed into the source
    int usedBufCount;        // # used buffers from buffers array

    // State
    boolean initialised;
    boolean looping;

    // Temporary buffer
    ByteBuffer data;

    void FillBuffer(int buffer) {
        int size = ReadData();
        AL10.alBufferData(buffer, format, data, size, freq);
    }

    int ReadData() {
        int offset = 0;
        boolean justLooped = false;

        stb_vorbis_get_frame_short(ogg, channels, data, STREAMBLOCKSIZE);
        return stb_vorbis_get_file_offset(ogg);
        // Request data from Vorbisfile.
        // Vorbisfile doesn't always return as many bytes as we ask for, so we simply keep asking
        // until our data block is full, or the end of the file is reached.
        /*
        while (offset < STREAMBLOCKSIZE && file != null) {
            int read = ov_read( & ogg,&data[offset], STREAMBLOCKSIZE - offset, 0, 2, 1);
            if (read <= 0) {
                // Error or end of file.

                // Set error status (if any)
                if (read < 0)
                    setError(GetVorbisFileErrorString(read));
                else
                    clearError();

                // If EOF and we are looping, then loop
                if (read == 0 && looping && !justLooped) {
                    int error = ov_raw_seek( & ogg, 0);
                    if (error != 0) {
                        setError(GetVorbisFileErrorString(error));
                        DoClose();
                    } else
                        justLooped = true;  // This detects 0 length files. Otherwise we would end up with an infinite loop here!
                } else
                    DoClose();              // Error or EOF (and not looping)
            } else {
                offset += read;
                justLooped = false;
            }
        }*/

        // Return number of bytes read
        //return offset;
    }

    void DoClose() {

        // Cleanup and close file
        // Note: Unlike the public CloseFile() method, we don't stop the music
        // playing. We simply close the file.
        if (file != null) {
            stb_vorbis_close( ogg);
            file = null;
            usedBufCount = 0;
        }
    }


    public MusicStreamPolled() {
        file = null;
        buffers = null;
        usedBufCount = 0;
        initialised = false;
        data = null;
        // Allocate source
        AL10.alGetError();
        AL10.alGenSources(1, source);
        int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            setError(SoundEngine.GetALErrorString(error));
            return;
        }

        // Allocate buffers
        buffers = BufferUtils.createByteBuffer(MUSICSTREAMBUFFERS * Integer.SIZE / Byte.SIZE);
        AL10.alGenBuffers(MUSICSTREAMBUFFERS, buffers);
        error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            setError(SoundEngine.GetALErrorString(error));
            return;
        }

        // Allocate temp data block
        data = BufferUtils.createByteBuffer(STREAMBLOCKSIZE * Integer.SIZE / Byte.SIZE);

        initialised = true;
    }

    public void dispose() {

        // Close any open file
        CloseFile();

        // Free buffers
        if (buffers != null) {
            AL10.alDeleteBuffers(MUSICSTREAMBUFFERS, buffers);
            buffers.clear();
        }

        // Close source
        AL10.alDeleteSources(1, source);

        // Free data
        if (data != null)
            data = null;
    }

    // Control interface
    public void OpenFile(String filename) {
        OpenFile(filename, 1f, false);
    }

    public void OpenFile(String filename, float gain) {
        OpenFile(filename, gain, false);
    }

    public void OpenFile(String filename, float gain, boolean looping)    // Open file and start playing
    {
        if (!initialised)
            return;

        this.looping = looping;

        // Close any existing open file
        CloseFile();

        // Open file
        file = new File(filename);
        if (!file.exists()) {
            setError("Could not open file: " + filename);
            return;
        }

        // Build callbacks structure
        //callbacks.read_func = ovCB_read;
        //callbacks.close_func = ovCB_close;
        //callbacks.seek_func = ovCB_seek;
        //callbacks.tell_func = ovCB_tell;

        // Open vorbis
        IntBuffer error = BufferUtils.createIntBuffer(1);
        ogg = stb_vorbis_open_filename(file.getAbsolutePath(), error, null);
        if (error.get(0) != 0) {
            setError(SoundEngine.GetVorbisFileErrorString(error.get(0)));
            return;
        }

        // Extract file parameters
        STBVorbisInfo info = null;
        stb_vorbis_get_info(ogg, info);
        if (info == null) {
            setError("Unable to extract audio info from file");
            DoClose();
            return;
        }
        freq = info.sample_rate();
        channels = info.channels();
        if (channels == 1)
            format = AL10.AL_FORMAT_MONO16;
        else
            format = AL10.AL_FORMAT_STEREO16;

        // Fill sound buffers
        while (file != null && usedBufCount < MUSICSTREAMBUFFERS) {
            FillBuffer(buffers.asIntBuffer().get(usedBufCount));
            usedBufCount++;
        }

        // Queue them into source
        AL10.alGetError();
        AL10.alSourceQueueBuffers(source.asIntBuffer().get(0), usedBufCount, buffers);

        // Set the gain
        AL10.alSourcef(source.asIntBuffer().get(0), AL10.AL_GAIN, gain);

        // Play the source
        AL10.alSourcePlay(source.asIntBuffer().get(0));

        // Check for OpenAL errors
        error.put(0, AL10.alGetError());
        if (error.get(0) != AL10.AL_NO_ERROR) {
            setError(SoundEngine.GetALErrorString(error.get(0)));
            DoClose();
            return;
        }

        clearError();
    }

    public void CloseFile() {
        if (!initialised)
            return;

        // Stop playing
        if (Playing())
            AL10.alSourceStop(source.asIntBuffer().get(0));

        // Close file
        DoClose();

        // Clear error status
        clearError();
    }

    public void SetGain(float gain) {
        if (!initialised)
            return;

        // Set the gain
        AL10.alSourcef(source.asIntBuffer().get(0), AL10.AL_GAIN, gain);
    }

    public boolean Playing() {

        // If file is still being played, return true
        if (file != null)
            return true;
        else {

            // Otherwise, it's possible the file has run out, but the
            // last buffers are still being played, so we must check
            // for this also
            IntBuffer state = BufferUtils.createIntBuffer(1);
            AL10.alGetSourcei(source.asIntBuffer().get(0), AL10.AL_SOURCE_STATE, state);
            return state.get(0) == AL10.AL_PLAYING;
        }
    }


    // Must be called periodically to keep stream playing
    public void Update() {
        if (!initialised)
            return;

        // Check state is streaming state
        IntBuffer sourceType = BufferUtils.createIntBuffer(1);
        AL10.alGetSourcei(source.asIntBuffer().get(0), AL10.AL_SOURCE_TYPE,  sourceType);
        if (sourceType.get(0) != AL11.AL_STREAMING)
            return;

        // Look for processed buffers
        IntBuffer processed = BufferUtils.createIntBuffer(1);
        AL10.alGetSourcei(source.asIntBuffer().get(0), AL10.AL_BUFFERS_PROCESSED,  processed);

        // Remove them
        if (processed.get(0) > 0) {
            assertTrue(processed.get(0) <= MUSICSTREAMBUFFERS);
            ByteBuffer processedBuffers = BufferUtils.createByteBuffer(MUSICSTREAMBUFFERS * Integer.SIZE / Byte.SIZE);
            AL10.alSourceUnqueueBuffers(source.asIntBuffer().get(0), processed.get(0), processedBuffers);

            // Refill
            int count = 0;
            while (count < processed.get(0) && file != null) {
                FillBuffer(processedBuffers.asIntBuffer().get(count));
                count++;
            }

            // Requeue
            if (count > 0) {
                AL10.alGetError();
                processedBuffers.rewind();
                AL10.alSourceQueueBuffers(source.asIntBuffer().get(0), count, processedBuffers);

                // Make sure the source keeps playing.
                // (This prevents lag hicups from stopping the music.
                // Otherwise the source could stop if the buffers run out.)
                IntBuffer state = BufferUtils.createIntBuffer(1);
                AL10.alGetSourcei(source.asIntBuffer().get(0), AL10.AL_SOURCE_STATE, state);
                if (state.get(0) != AL10.AL_PLAYING)
                    AL10.alSourcePlay(source.asIntBuffer().get(0));

                // Check for OpenAL error
                int error = AL10.alGetError();
                if (error != AL10.AL_NO_ERROR) {
                    setError(SoundEngine.GetALErrorString(error));
                    return;
                }
            }
        }
    }
}
