package com.basic4gl.debug.protocol.callbacks;

import com.google.gson.Gson;

public class DebugCallback {
  public String message;

  public static DebugCallback fromJson(String message) {
    Gson gson = new Gson();
    try {
      return gson.fromJson(message, DebugCallback.class);
    } catch (Exception e) {
      return null;
    }
  }
}
