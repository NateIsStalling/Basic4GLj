package com.basic4gl.desktop.util;

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.event.InputEvent;

public class EditorUtil {
  public static String getVariableAt(String line, int x) {
    char[] l = line.toCharArray();
    // Find character
    if (x < 1 || x > l.length || l[x] <= ' ') {
      return "";
    }

    // Scan to right of word
    int right = x + 1;
    while (right <= l.length
        && ((l[right] >= 'a' && l[right] <= 'z')
            || (l[right] >= 'A' && l[right] <= 'Z')
            || (l[right] >= '0' && l[right] <= '9')
            || l[right] == '_'
            || l[right] == '#'
            || l[right] == '$')) {
      right++;
    }

    // Scan left
    int left = x;
    while (left > 0
        && ((l[left] >= 'a' && l[left] <= 'z')
            || (l[left] >= 'A' && l[left] <= 'Z')
            || (l[left] >= '0' && l[left] <= '9')
            || l[left] == '.'
            || l[left] == '_'
            || l[left] == '#'
            || l[left] == '$'
            || l[left] == ')')) {

      // Skip over brackets
      if (l[left] == ')') {
        int level = 1;

        left--;

        while (level > 0 && left > 0) {
          if (l[left] == ')') {
            level++;
          } else if (l[left] == '(') {
            level--;
          }
          left--;
        }
        while (left > 0 && l[left] <= ' ') {
          left--;
        }
      } else {
        left--;
      }
    }
    left++;

    // Trim whitespace from left
    while (left < right && l[left] <= ' ') {
      left++;
    }

    // Return result
    if (left < right) {
      // TODO Possibly wrong second parameter
      return line.substring(left, right - left);
    } else {
      return "";
    }
  }

  public static int getLinkScanningMask() {
    if (SystemInfo.isMacOS) {
      return InputEvent.META_DOWN_MASK;
    }

    return InputEvent.CTRL_DOWN_MASK;
  }
}
