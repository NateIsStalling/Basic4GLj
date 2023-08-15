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

public class ALWrapper {
    static long alContext = -1;
    static ALCdevice alcDevice;
    private static boolean created = false;

    public ALWrapper() {
    }

    public static void create() throws LWJGLException {
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
            alContext = SoundEngine.contextAL;//alcCreateContext(device, (IntBuffer) attribs);
            alcDevice = new ALCdevice(alContext);
            created = true;
        }

    }

    public static boolean isCreated() {
        return created;
    }

    public static void destroy() {
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
