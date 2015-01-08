package com.basic4gl.vm;

import com.basic4gl.vm.types.OpCode;

public class vmPatchedBreakPt {
	int m_offset; // Op-Code offset in program
	short m_replacedOpCode; // For active breakpoints: The op-code that
								// has been replaced with the OP_BREAKPT.
	public vmPatchedBreakPt(){
		m_offset = 0xffff;
		m_replacedOpCode = OpCode.OP_NOP;
	}
}
