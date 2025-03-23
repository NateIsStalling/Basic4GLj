package com.basic4gl.compiler.util;

import com.basic4gl.runtime.types.ValType;

/**
 * Compile time parameter validation callback.
 */
public interface ParamValidationCallback {
  /**
   * Called at compile time to validate any parameters whose types are specified
   * as VTP_UNDEFINED.
   * @param index Index is 0 for the leftmost parameter and so on (unlike at run time)
   * @param type
   * @return Callback should return true if parameter type is valid.
   */
  boolean run(int index, ValType type);
}
