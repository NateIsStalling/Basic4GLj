package com.basic4gl.library.desktopgl.soundengine;

import com.basic4gl.runtime.HasErrorState;
import org.lwjgl.openal.AL10;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecIBXM;


/**
 * Represents a sound effect. Wrapper for paulscode Source
 * Original source: SoundEngine.h
 */
public class Sound extends HasErrorState {

    public static SoundSystem system;

    private String sourceName;
    private boolean toLoop;

    public static void init(){
        // Load some library and codec pluggins:
        try
        {
            SoundSystemConfig.setSoundFilesPackage("");
            SoundSystemConfig.addLibrary(LibraryLWJGL3OpenAL.class);
            SoundSystemConfig.setCodec( "wav", CodecWav.class );
            SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
            SoundSystemConfig.setCodec("xm",CodecIBXM.class);
            SoundSystemConfig.setCodec("s3m", CodecIBXM.class);
            SoundSystemConfig.setCodec("mod", CodecIBXM.class);
        }
        catch( SoundSystemException e )
        {
            System.out.println("error linking with the SoundSystem plugins" );
        }

        // Instantiate the SoundSystem:
        try
        {
            system = new SoundSystem( LibraryLWJGL3OpenAL.class);
        }
        catch( SoundSystemException e )
        {
            System.out.println( "JavaSound library is not compatible on " +
                    "this computer" );
//            System.out.println( "LWJGL OpenAL library is not compatible on " +
//                                "this computer" );
            e.printStackTrace();
            return;
        }
    }

    public static void cleanup(){
        system.cleanup();
        system = null;
    }

    public Sound(String filename){
        // Open file
        this.sourceName = filename;

        File file = new File(filename);
        // debugging..
//        if (file.exists()) {
//            System.out.println(filename + " exists!");
//        } else {
//            System.out.println(filename + " missing!");
//        }
        URL url;

        try {
            url = file.toURI().toURL();

            system.newStreamingSource(true, filename, url, filename,
                    false, 0, 0, 0,
                    SoundSystemConfig.ATTENUATION_NONE,
                    0);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            setError(e.getMessage());
        }

        // Check for errors
        int error = AL10.alGetError();
        if (error != AL10.AL_NO_ERROR) {
            setError(SoundEngine.getALErrorString(error));
        } else {
            clearError();
        }
    }

    public void dispose(){
        //        system.stop(sourceName);
    }
    // Member access
    public void setGain(float gain) {
        system.setVolume(sourceName, gain);
    }
    public void setLooping(boolean looping) {
        this.toLoop = looping;
        system.setLooping(sourceName, looping);
    }

    public boolean isLooping() {
        return this.toLoop;
    }

    public boolean isPlaying() {
        return system.playing(sourceName);
    }
    public void play() {
        system.play(sourceName);
    }

    public void stop() {
        system.stop(sourceName);
    }
}
