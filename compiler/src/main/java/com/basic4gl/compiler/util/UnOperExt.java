package com.basic4gl.compiler.util;

import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.types.ValType;

//Language extension: Operator overloading
public interface UnOperExt {
	abstract boolean run (  Mutable<ValType> regType,     // IN: Current type in register.                                                        OUT: Required type cast before calling function
            short opCode,          // IN: Operator being applied
            Mutable<Integer> operFunction,      // OUT: Index of VM_CALL_OPERATOR_FUNC function to call
            Mutable<ValType> resultType,  // OUT: Resulting value type
            Mutable<Boolean> freeTempData);    // OUT: Set to true if temp data needs to be freed
}
