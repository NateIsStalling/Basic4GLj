package com.basic4gl.compiler.util;

import com.basic4gl.vm.types.ValType;

public interface compParamValidationCallback {
	public abstract boolean run(int index, ValType type);
}
