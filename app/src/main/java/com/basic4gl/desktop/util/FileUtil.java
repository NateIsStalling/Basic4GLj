package com.basic4gl.desktop.util;

public class FileUtil {
  public static String fromUserHome(String absolutePath) {
    String userHome = System.getProperty("user.home");

    if (absolutePath.startsWith(userHome)) {
      return absolutePath.replaceFirst(userHome, "~");
    }
    return absolutePath; // Return as-is if not in home directory
  }
}
