package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.runtime.HasErrorState;
import org.lwjgl.openal.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.stb.STBVorbis.*;

/**
 * Main interface to the sound engine.
 * Original source: SoundEngine.h
 */
public class SoundEngine extends HasErrorState {

    private boolean initialized = false;
    private int voiceCount;

    // original source used an uint collection to track AL voice handles;
    // track Sound wrapper objects instead here
    private Sound[] voiceSources;
    private List<Integer> queue = new ArrayList<>();	// Queued voices. Each entry indexes into voices array.

    public SoundEngine(int voiceCount) {
        assertTrue(voiceCount > 0);
        assertTrue(voiceCount <= 1000);

        this.voiceCount = voiceCount;
        this.voiceSources = new Sound[voiceCount];

        boolean hasError = false;
        try {
            Sound.init();
        } catch (Exception e) {
            e.printStackTrace();
            setError(e.getMessage());
            hasError = true;
        }

        int error;
        if ((error = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            setError(SoundEngine.getALErrorString(error));
            hasError = true;
        }

        if (hasError) {
            voiceSources = null;
            initialized = false;
            return;
        }

        // setup voice queue
        rebuildQueue();

        initialized = true;
    }
    public void dispose() {
        Sound.cleanup();
        voiceSources = null;
        initialized = false;
    }

    private void rebuildQueue() {

        // Rebuild queue of voices
        queue.clear();
        for (int i = 0; i < voiceCount; i++) {
            queue.add(i);
        }
    }

    private int findFreeVoice(Sound forSound) {
        int index = -1;
        boolean found = false;

        List<Integer> temp = new ArrayList<>();
        // Find a free voice

        // First look for a free voice that isn't playing
        Iterator<Integer> i = queue.iterator();
        while (i.hasNext()) {
            // Find source
            index = i.next();
            Sound source = voiceSources[index];

            // Check if it's playing
            boolean isPlaying = source != null && source.isPlaying();

            if (!isPlaying) {

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
            voiceSources[index] = forSound;
            return index;
        }

        // No non-playing source found.
        // Now we look for the oldest playing source that isn't looping
        i = queue.iterator();
        while (i.hasNext()) {

            // Find source
            index = i.next();
            Sound source = voiceSources[index];

            // Check if it's looping
            if (source == null || !source.isLooping()) {

                // Stop old sound
                if (source != null) {
                    source.stop();
                }

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
            voiceSources[index] = forSound;
            return index;
        }

        // All sounds are playing and looping.
        // Just use oldest voice
        index = queue.get(0);
        Sound source = voiceSources[index];

        // Stop old sound
        if (source != null) {
            source.stop();
        }

        // Move source to back of queue
        queue.remove(0);
        queue.add(index);

        voiceSources[index] = forSound;
        return index;
    }

    public int playSound(Sound sound, float gain, boolean looped) {
        if (!initialized) {
            return -1;
        }
        if (sound.hasError()) {
            setError("Error opening sound: " + sound.getError());
            return -1;
        }

        // Find a suitable voice
        int index = findFreeVoice(sound);

        // Set looping state
        sound.setLooping(looped);

        // Set gain
        sound.setGain(gain);

        sound.play();

        clearError();
        return index;
    }

    public void stopVoice(int index) {
        if (!initialized) {
            return;
        }

        if (index >= 0 && index < voiceCount) {
            Sound source = voiceSources[index];
            source.stop();
        }
    }

    public void stopAll() {
        if (!initialized) {
            return;
        }

        for (int i = 0; i < voiceCount; i++) {
            Sound source = voiceSources[i];
            source.stop();
        }
        rebuildQueue();
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
}
