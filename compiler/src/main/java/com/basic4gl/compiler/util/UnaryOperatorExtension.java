package com.basic4gl.compiler.util;

import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.types.ValType;

/**
 * Language extension: Operator overloading
 */
public interface UnaryOperatorExtension {
	/**
	 *
	 * @param regType IN: Current type in register. OUT: Required type cast before calling function
	 * @param opCode IN: Operator being applied
	 * @param operFunction OUT: Index of VM_CALL_OPERATOR_FUNC function to call
	 * @param resultType OUT: Resulting value type
	 * @param freeTempData OUT: Set to true if temp data needs to be freed
	 * @return Operation result
	 */
	boolean run (Mutable<ValType> regType,
            short opCode,
            Mutable<Integer> operFunction,
            Mutable<ValType> resultType,
            Mutable<Boolean> freeTempData);
}
