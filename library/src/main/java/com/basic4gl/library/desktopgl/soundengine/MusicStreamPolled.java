package com.basic4gl.library.desktopgl.soundengine;

import static com.basic4gl.library.desktopgl.soundengine.util.STBVorbisUtil.getVorbisFileErrorString;
import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.stb.STBVorbis.*;

import com.basic4gl.library.desktopgl.soundengine.util.ALUtil;
import com.basic4gl.runtime.HasErrorState;
import java.io.File;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.stb.STBVorbisInfo;

/**
 * A polled implementation of a streaming music file.
 * Plays an Ogg Vorbis file using OpenAL.
 * Calling code must continually call ::Update() to ensure the
 * music keeps playing.
 *
 * Note: For a version that doesn't require continuous polling,
 * use class "MusicStream" instead. ("MusicStream" uses
 * "MusicStreamPolled internally, but automates polling itself.)
 */
public class MusicStreamPolled extends HasErrorState {

    // Note: This is about enough space for 1 second of
    // stereo 44KHz 16bit music.
    // Our update frequency is 10 times per second, which
    // should be plenty enough to ensure that the stream
    // won't be interrupted.
    static final int MUSIC_STREAM_BUFFERS = 64;
    static final int STREAM_BLOCK_SIZE = 4096;

    // File access
    private File file;
    // File handle
    private long ogg;

    // Used by VorbisFile to access the file data.

    // File info
    private int freq;
    private int format;
    private int channels;

    // OpenAL objects

    // OpenAL source. Music is streamed through this source
    private IntBuffer source;
    // OpenAL buffers. Filled with music data and streamed into the source
    private IntBuffer buffers;
    // # used buffers from buffers array
    private int usedBufCount;

    // State
    private boolean initialised;
    private boolean looping;

    // Temporary buffer
    private ShortBuffer data;

    void fillBuffer(int buffer) {
        int size = readData();
        data.position(0);
        data.limit(size);
        AL10.alBufferData(buffer, format, data, freq);
    }

    /**
     * Read ogg data into buffer and return size of buffer
     * @return number of bytes read
     */
    private int readData() {
        data.rewind();
        data.limit(STREAM_BLOCK_SIZE);
        int offset = 0;
        boolean justLooped = false;

        // Request data from Vorbisfile.
        // Vorbisfile doesn't always return as many bytes as we ask for, so we simply keep asking
        // until our data block is full, or the end of the file is reached.

        while (offset < STREAM_BLOCK_SIZE && file != null) {
            data.position(offset);
            int read = stb_vorbis_get_samples_short_interleaved(ogg, channels, data) * channels;
            if (read <= 0) {
                // Error or end of file.

                // Set error status (if any)
                if (read < 0) {
                    setError(getVorbisFileErrorString(read));
                } else {
                    clearError();
                }
                // If EOF and we are looping, then loop
                if (read == 0 && looping && !justLooped) {
                    stb_vorbis_seek_start(ogg);
                    int error = stb_vorbis_get_error(ogg);
                    if (error != 0) {
                        setError(getVorbisFileErrorString(error));
                        doClose();
                    } else {
                        // This detects 0 length files. Otherwise we would end up with an infinite loop here!
                        justLooped = true;
                    }
                } else {
                    // Error or EOF (and not looping)
                    doClose();
                }
            } else {
                offset += read;
                justLooped = false;
            }
        }

        // Return number of bytes read
        return offset;
    }

    void doClose() {

        // Cleanup and close file
        // Note: Unlike the public closeFile() method, we don't stop the music
        // playing. We simply close the file.
        if (file != null) {
            if (ogg != 0) {
                stb_vorbis_close(ogg);
            }
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
        source = BufferUtils.createIntBuffer(1);
        source.rewind();
        AL10.alGenSources(source);
        int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            setError(ALUtil.getALErrorString(error));
            return;
        }

        // Allocate buffers
        buffers = BufferUtils.createIntBuffer(MUSIC_STREAM_BUFFERS);
        AL10.alGenBuffers(buffers);
        error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            setError(ALUtil.getALErrorString(error));
            return;
        }

        // Allocate temp data block
        data = BufferUtils.createShortBuffer(STREAM_BLOCK_SIZE);

        initialised = true;
    }

    public void dispose() {

        // Close any open file
        closeFile();

        // Free buffers
        if (buffers != null) {
            buffers.rewind();
            AL10.alDeleteBuffers(buffers);
            buffers.clear();
        }

        // Close source
        source.rewind();
        AL10.alDeleteSources(source);

        // Free data
        if (data != null) {
            data = null;
        }
    }

    // Control interface
    public void openFile(String filename) {
        openFile(filename, 1f, false);
    }

    public void openFile(String filename, float gain) {
        openFile(filename, gain, false);
    }

    public void openFile(String filename, float gain, boolean looping) // Open file and start playing
            {
        if (!initialised) {
            return;
        }

        this.looping = looping;

        // Close any existing open file
        closeFile();

        // Open file
        file = new File(filename);
        if (!file.exists()) {
            setError("Could not open file: " + filename);
            return;
        }

        // Open vorbis
        IntBuffer error = BufferUtils.createIntBuffer(1);
        ogg = stb_vorbis_open_filename(file.getAbsolutePath(), error, null);
        if (error.get(0) != 0) {
            String errorMessage = getVorbisFileErrorString(error.get(0));
            setError(errorMessage);
            return;
        }

        // Extract file parameters
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            stb_vorbis_get_info(ogg, info);
            freq = info.sample_rate();
            channels = info.channels();
            if (channels == 1) {
                format = AL10.AL_FORMAT_MONO16;
            } else {
                format = AL10.AL_FORMAT_STEREO16;
            }
        } catch (Exception e) {
            e.printStackTrace();
            setError("Unable to extract audio info from file");
            doClose();
            return;
        }

        // Fill sound buffers
        data.rewind();
        while (file != null && usedBufCount < MUSIC_STREAM_BUFFERS) {
            fillBuffer(buffers.get(usedBufCount));
            usedBufCount++;
        }

        // Queue them into source
        AL10.alGetError();
        buffers.rewind();
        buffers.limit(usedBufCount);
        AL10.alSourceQueueBuffers(source.get(0), buffers);

        // Set the gain
        AL10.alSourcef(source.get(0), AL10.AL_GAIN, gain);

        // Play the source
        AL10.alSourcePlay(source.get(0));

        // Check for OpenAL errors
        error.put(0, AL10.alGetError());
        if (error.get(0) != AL10.AL_NO_ERROR) {
            setError(ALUtil.getALErrorString(error.get(0)));
            doClose();
            return;
        }

        clearError();
    }

    public void closeFile() {
        if (!initialised) {
            return;
        }

        // Stop playing
        if (isPlaying()) {
            AL10.alSourceStop(source.get(0));
        }

        // Close file
        doClose();

        // Clear error status
        clearError();
    }

    public void setGain(float gain) {
        if (!initialised) {
            return;
        }

        // Set the gain
        AL10.alSourcef(source.get(0), AL10.AL_GAIN, gain);
    }

    public boolean isPlaying() {

        // If file is still being played, return true
        if (file != null) {
            return true;
        } else {

            // Otherwise, it's possible the file has run out, but the
            // last buffers are still being played, so we must check
            // for this also
            IntBuffer state = BufferUtils.createIntBuffer(1);
            AL10.alGetSourcei(source.get(0), AL10.AL_SOURCE_STATE, state);
            return state.get(0) == AL10.AL_PLAYING;
        }
    }

    // Must be called periodically to keep stream playing
    public void update() {
        if (!initialised) {
            return;
        }

        // Check state is streaming state
        IntBuffer sourceType = BufferUtils.createIntBuffer(1);
        AL10.alGetSourcei(source.get(0), AL10.AL_SOURCE_TYPE, sourceType);
        if (sourceType.get(0) != AL11.AL_STREAMING) {
            return;
        }

        // Look for processed buffers
        IntBuffer processedBuffer = BufferUtils.createIntBuffer(1);
        AL10.alGetSourcei(source.get(0), AL10.AL_BUFFERS_PROCESSED, processedBuffer);
        int processed = processedBuffer.get(0);

        // Remove them
        if (processed > 0) {
            assertTrue(processed <= MUSIC_STREAM_BUFFERS);
            IntBuffer processedBuffers = BufferUtils.createIntBuffer(MUSIC_STREAM_BUFFERS);
            processedBuffers.limit(processed);
            AL10.alSourceUnqueueBuffers(source.get(0), processedBuffers);

            // Refill
            int count = 0;
            data.rewind();
            while (count < processed && file != null) {
                fillBuffer(processedBuffers.get(count));
                count++;
            }

            // Requeue
            if (count > 0) {
                AL10.alGetError();
                processedBuffers.rewind();
                processedBuffers.limit(count);
                AL10.alSourceQueueBuffers(source.get(0), processedBuffers);

                // Make sure the source keeps playing.
                // (This prevents lag hicups from stopping the music.
                // Otherwise the source could stop if the buffers run out.)
                IntBuffer state = BufferUtils.createIntBuffer(1);
                AL10.alGetSourcei(source.get(0), AL10.AL_SOURCE_STATE, state);
                if (state.get(0) != AL10.AL_PLAYING) {
                    AL10.alSourcePlay(source.get(0));
                }

                // Check for OpenAL error
                int error = AL10.alGetError();
                if (error != AL10.AL_NO_ERROR) {
                    setError(ALUtil.getALErrorString(error));
                    return;
                }
            }
        }
    }
}
