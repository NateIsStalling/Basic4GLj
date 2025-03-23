package com.basic4gl.compiler.util;

import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Mutable;

/**
 * Language extension: Operator overloading
 */
public interface BinaryOperatorExtension {
/**
*
* @param regType IN: Current type in register. OUT: Required type cast before calling function
* @param reg2Type IN: Current type in second register (operation is reg2 OP reg1, e.g reg2 + reg1): OUT: Required type cast before calling function
* @param opCode IN: Operator being applied
* @param operFunction OUT: Index of VM_CALL_OPERATOR_FUNC function to call
* @param resultType OUT: Resulting value type
* @param freeTempData OUT: Set to true if temp data needs to be freed
* @return Operation result
*/
boolean run(
	Mutable<ValType> regType,
	Mutable<ValType> reg2Type,
	short opCode,
	Mutable<Integer> operFunction,
	Mutable<ValType> resultType,
	Mutable<Boolean> freeTempData);
}
