package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.runtime.HasErrorState;

/**
 * Basic4GL Sound Engine Library
 */
public class Basic4GLSoundLibrary implements SoundLibrary {

    // Error state
    protected String errorMessage = "";

    private SoundEngine engine = null;
    private MusicStream music = null;

    boolean checkError(HasErrorState obj) {
        if (obj.hasError()) {
            errorMessage = obj.getError();
            return false;
        } else {
            errorMessage = "";
            return true;
        }
    }

    @Override
    public void init(int voiceCount) {
        if (engine == null) {
            // NOTE: the ALC device context for OpenAL is initialized by
            // the SoundEngine constructor initializing the SoundSystem library
            // which configures OpenAL internally.
            engine = new SoundEngine(voiceCount);
            music = new MusicStream();
        }
    }

    @Override
    public void dispose() {
        if (engine != null) {
            engine.dispose();
        }

        if (music != null) {
            music.dispose();
        }
    }

    @Override
    public void reset() {
        if (engine != null) {
            engine.stopAll();
        }
        if (music != null) {
            music.closeFile();
        }

        errorMessage = "";
    }

    @Override
    public Sound loadSound(String filename) {
        Sound s = new Sound(filename);
        if (checkError(s)) {
            return s;
        } else {
            s.dispose();
            return null;
        }
    }

    @Override
    public void deleteSound(Sound sound) {
        if (sound != null) {
            sound.dispose();
        }
    }

    @Override
    public int playSound(Sound sound, float volume, boolean looped) {
        if (sound != null) {
            int voice = engine.playSound(sound, volume, looped);
            checkError(sound);
            return voice;
        } else {
            return -1;
        }
    }

    @Override
    public void stopSounds() {
        engine.stopAll();
    }

    @Override
    public void playMusic(String filename, float volume, boolean looped) {
        music.openFile(filename, volume, looped);
        music.updateErrorState();
        checkError(music);
    }

    @Override
    public void stopMusic() {
        music.closeFile();
    }

    @Override
    public boolean isMusicPlaying() {
        return music.isPlaying();
    }

    @Override
    public void setMusicVolume(float volume) {
        music.setGain(volume);
    }

    @Override
    public void stopSoundVoice(int voice) {
        engine.stopVoice(voice);
    }

    @Override
    public void getError(StringBuilder message) {
        message.append(errorMessage);
    }
}
