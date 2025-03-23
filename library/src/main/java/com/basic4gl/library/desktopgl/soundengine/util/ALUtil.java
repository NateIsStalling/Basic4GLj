package com.basic4gl.library.desktopgl.soundengine.util;

import org.lwjgl.openal.AL10;

public final class ALUtil {
  public static String getALErrorString(int error) {
    switch (error) {
      case AL10.AL_INVALID_NAME:
        return "AL_INVALID_NAME: Invalid name";
      case AL10.AL_INVALID_ENUM:
        return "AL_INVALID_ENUM: Invalid enumeration";
      case AL10.AL_INVALID_VALUE:
        return "AL_INVALID_VALUE: Invalid parameter value";
      case AL10.AL_INVALID_OPERATION:
        return "AL_INVALID_OPERATION: Invalid operation";
      case AL10.AL_OUT_OF_MEMORY:
        return "AL_OUT_OF_MEMORY: Out of memory";
      default:
        return "OpenAL error";
    }
  }
}
