package com.basic4gl.library.desktopgl.soundengine;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import paulscode.sound.libraries.LWJGLException;

import static org.lwjgl.openal.ALC10.*;


import com.basic4gl.runtime.HasErrorState;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC11.ALC_ALL_DEVICES_SPECIFIER;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;

public class ALWrapper {

    public static long device = -1;
    public static long contextAL = -1;


    static ALCCapabilities deviceCaps;
    static ALCapabilities caps;

    private static boolean useTLC;

    static long alContext = -1;
    static ALCdevice alcDevice;
    private static boolean created = false;

    public ALWrapper() {
    }

    public static void create() throws LWJGLException {
        // Initialise OpenAL
// Can call "alc" functions at any time
        List<String> devices = ALUtil.getStringList(NULL, ALC_ALL_DEVICES_SPECIFIER);

        System.out.println(String.join(", ", devices));
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

        // TODO consolidate code below with above - originally separate OpenAL init functions

        if (alContext == -1) {
//
//            long device = -1;
//            long contextAL = -1;
//
//            ALCCapabilities deviceCaps;
//            ALCapabilities caps;
//
//
//            device = alcOpenDevice((ByteBuffer)null);
//            System.out.println("device: " + device);
////            ALDevice alDevice = ALDevice.create();
//            IntBuffer attribs = BufferUtils.createIntBuffer(16);
//            attribs.put(4103);
//            attribs.put(44100);
//            attribs.put(4104);
//            attribs.put(60);
//            attribs.put(4105);
//            attribs.put(0);
//            attribs.put(0);
//            attribs.flip();

//            long contextHandle = ALC10.alcCreateContext(device, attribs);
//            System.out.println("contextHandle: " + contextHandle);
            alContext = contextAL;//alcCreateContext(device, (IntBuffer) attribs);
            alcDevice = new ALCdevice(alContext);
            created = true;
        }

    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
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

        // TODO consolidate cleanup above and below.. above is from previous SoundEngine implementation, below was initial ALWrapper implementation

        alcDestroyContext(alContext);
        alContext = -1;
        alcDevice = null;
        created = false;
    }

    public static ALCdevice getDevice() {
        return alcDevice;
    }

    static {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize glfw");
        }
    }
}
