package com.basic4gl.library.desktopgl.soundengine;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.library.desktopgl.soundengine.util.ALUtil;
import com.basic4gl.runtime.HasErrorState;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.openal.*;

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
    private List<Integer> queue = new ArrayList<>(); // Queued voices. Each entry indexes into voices array.

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
            setError(ALUtil.getALErrorString(error));
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
}
