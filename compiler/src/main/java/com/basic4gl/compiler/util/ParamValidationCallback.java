package com.basic4gl.compiler.util;

import com.basic4gl.runtime.types.ValType;

public interface ParamValidationCallback {
	public abstract boolean run(int index, ValType type);
}