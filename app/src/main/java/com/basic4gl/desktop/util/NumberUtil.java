package com.basic4gl.desktop.util;

public final class NumberUtil {
  public static Integer parseIntOrNull(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
