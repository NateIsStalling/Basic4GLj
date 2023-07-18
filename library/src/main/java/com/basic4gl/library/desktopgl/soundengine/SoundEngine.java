package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.runtime.HasErrorState;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Main interface to the sound engine.
 * Original source: SoundEngine.h
 */
public class SoundEngine extends HasErrorState {

    static long device = -1;
    static long contextAL = -1;

    static ALCCapabilities deviceCaps;
    static ALCapabilities caps;
    private boolean			initialised;		        // True if OpenAL successfully initialised.
    // (If false, we won't try to shut it down when we are destroyed!)

    private int				voiceCount;
    private IntBuffer      voices;			            // OpenAL voices.
    private List<Integer>   queue = new ArrayList<>();	// Queued voices. Each entry indexes into voices array.
    private boolean useTLC;

    public SoundEngine(int voiceCount) {
        this.voiceCount = voiceCount;
        assertTrue(this.voiceCount > 0);
        assertTrue(this.voiceCount <= 1000);

        // Initialise OpenAL
// Can call "alc" functions at any time
        device = alcOpenDevice((ByteBuffer)null);
        deviceCaps = ALC.createCapabilities(device);

        contextAL = alcCreateContext(device, (IntBuffer) null);
        alcMakeContextCurrent(contextAL);

        useTLC = deviceCaps.ALC_EXT_thread_local_context && alcSetThreadContext(contextAL);
        if (!useTLC) {
            if (!alcMakeContextCurrent(contextAL)) {
                throw new IllegalStateException();
            }
        }

        caps = AL.createCapabilities(deviceCaps);
        AL10.alGetError();


        int error;
        if ((error = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            setError(SoundEngine.getALErrorString(error));
            voices = null;
            initialised = false;
            return;
        }

        //Init Sound System libraries
        Sound.init();

        // Allocate voices
        voices = BufferUtils.createIntBuffer(this.voiceCount);
        AL10.alGetError();
        AL10.alGenSources(voices);
        error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            setError(getALErrorString(error));
            voices.clear();
            voices = null;
            return;
        }

        // Setup voice queue
        rebuildQueue();

        initialised = true;
    }
    public void dispose() {

        alcDestroyContext(contextAL);
        alcMakeContextCurrent(NULL);
        if (useTLC) {
            AL.setCurrentThread(null);
        } else {
            AL.setCurrentProcess(null);
        }
        memFree(caps.getAddressBuffer());

        alcDestroyContext(contextAL);
        alcCloseDevice(device);

            // Stop voices playing
            if (initialised) {
                stopAll();

                // Delete voices
                voices.rewind();
                AL10.alDeleteSources(voices);
                voices.clear();

                // Shut down OpenAL
                ALC.destroy();
            }
            Sound.cleanup();

    }

    private void rebuildQueue() {

        // Rebuild queue of voices
        queue.clear();
        for (int i = 0; i < voiceCount; i++) {
            queue.add(i);
        }
    }

    private int findFreeVoice() {
        int index = -1;
        boolean found = false;

        List<Integer> temp = new ArrayList<>();
        // Find a free voice

        // First look for a free voice that isn't playing
        Iterator<Integer> i = queue.iterator();
        while (i.hasNext()) {
            // Find source
            index = i.next();
            int source = voices.get(index);

            // Check if it's playing
            IntBuffer state = BufferUtils.createIntBuffer(1);
            AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE, state);
            if (state.get(0) != AL10.AL_PLAYING) {

                // Move source to back of queue
                i.remove();
                temp.add(index);

                // Use this source as our "voice"
                found = true;
                break;
            }
        }
        queue.addAll(temp);
        temp.clear();
        if (found) {
            return index;
        }

        // No non-playing source found.
        // Now we look for the oldest playing source that isn't looping
        i = queue.iterator();
        while (i.hasNext()) {

            // Find source
            index = i.next();
            int source = voices.get(index);

            // Check if it's looping
            IntBuffer looping = BufferUtils.createIntBuffer(1);
            AL10.alGetSourcei(source, AL10.AL_LOOPING, looping);
            if (looping.get(0) != AL10.AL_FALSE) {

                // Stop old sound
                AL10.alSourceStop(source);

                // Move source to back of queue
                i.remove();
                temp.add(index);

                // Use this source as our "voice"
                found = true;
                break;
            }
        }
        queue.addAll(temp);
        temp.clear();
        if (found) {
            return index;
        }

        // All sounds are playing and looping.
        // Just use oldest voice
        index = queue.get(0);
        int source = voices.get(index);

        // Stop old sound
        AL10.alSourceStop(source);

        // Move source to back of queue
        queue.remove(0);
        queue.add(index);

        return index;
    }

    public int playSound(Sound sound, float gain, boolean looped) {
        if (!initialised) {
            return -1;
        }

        if (sound.hasError()) {
            setError("Error opening sound: " + sound.getError());
            return -1;
        }

        // Find a suitable voice
        int index = findFreeVoice();
        int source = voices.get(index);

        // Set looping state
        AL10.alSourcei(source, AL10.AL_LOOPING, looped ? AL10.AL_TRUE : AL10.AL_FALSE);

        // Set gain
        AL10.alSourcef(source, AL10.AL_GAIN, gain);

        // Connect the sound buffer
        AL10.alSourcei(source, AL10.AL_BUFFER, sound.getBuffer());

        // Play it
        AL10.alSourcePlay(source);

        clearError();
        return index;
    }

    public void stopVoice(int index) {
        if (!initialised) {
            return;
        }

        if (index >= 0 && index < voiceCount) {
            AL10.alSourceStop(voices.get(index));
        }
    }

    public void stopAll() {
        if (!initialised) {
            return;
        }

        for (int i = 0; i < voiceCount; i++) {
            AL10.alSourceStop(voices.get(i));
        }
        rebuildQueue();
    }

    boolean isVoicePlaying(int index) {
        if (!initialised) {
            return false;
        }

        if (index >= 0 && index < voiceCount) {
            int source = voices.get(index);
            IntBuffer state = BufferUtils.createIntBuffer(1);
            AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE, state);
            return state.get(0) == AL10.AL_PLAYING;
        }
        else {
            return false;
        }
    }

    //	Helper functions

    static String getALErrorString(int error) {
        switch (error) {
            case AL10.AL_INVALID_NAME:       return "AL_INVALID_NAME: Invalid name";
            case AL10.AL_INVALID_ENUM:       return "AL_INVALID_ENUM: Invalid enumeration";
            case AL10.AL_INVALID_VALUE:		return "AL_INVALID_VALUE: Invalid parameter value";
            case AL10.AL_INVALID_OPERATION:	return "AL_INVALID_OPERATION: Invalid operation";
            case AL10.AL_OUT_OF_MEMORY:		return "AL_OUT_OF_MEMORY: Out of memory";
            default:					return "OpenAL error";
        }
    }


    static String getVorbisFileErrorString(int error) {
        switch (error) {
            case VORBIS__no_error: return "STBVorbis: No error";

            case VORBIS_bad_packet_type: return "STBVorbis: Bad packet type";

            case VORBIS_cant_find_last_page: return "STBVorbis: Can't find last page";

            case VORBIS_continued_packet_flag_invalid: return "STBVorbis: Continued packet flag invalid";

            case VORBIS_feature_not_supported: return "STBVorbis: Feature not supported";
            case VORBIS_file_open_failure: return "STBVorbis: File open failure";
            case VORBIS_incorrect_stream_serial_number: return "STBVorbis: Incorrect stream serial number";
            case VORBIS_invalid_api_mixing: return "STBVorbis: Invalid api mixing";
            case VORBIS_invalid_first_page: return "STBVorbis: Invalid first page";
            case VORBIS_invalid_setup: return "STBVorbis: Invalid setup";
            case VORBIS_invalid_stream: return "STBVorbis: Invalid stream";
            case VORBIS_invalid_stream_structure_version: return "STBVorbis: Invalid stream structure version";
            case VORBIS_missing_capture_pattern: return "STBVorbis: Missing capture pattern";
            case VORBIS_need_more_data: return "STBVorbis: Need more data";
            case VORBIS_outofmem: return "STBVorbis: Out of memory";
            case VORBIS_seek_failed: return "STBVorbis: Seek failed";
            case VORBIS_seek_invalid: return "STBVorbis: Seek invalid";
            case VORBIS_seek_without_length: return "STBVorbis: Seek without length";
            case VORBIS_too_many_channels: return "STBVorbis: Too many channels";
            case VORBIS_unexpected_eof: return "STBVorbis: Unexpected eof";
            default:                    return "STBVorbis error";
        }
    }
    /*
    // Vorbisfile callbacks
    size_t ovCB_read(void *buf,unsigned int a,unsigned int b,void * fp) {
        return fread(buf,a,b,(FILE *)fp);
    }

    int ovCB_close(void * fp) {
        return fclose((FILE *)fp);
    }

    int ovCB_seek(void *fp,__int64 a,int b) {
        return fseek((FILE *)fp,(long)a,b);
    }

    long ovCB_tell(void *fp) {
        return ftell((FILE *)fp);
    }*/
}
