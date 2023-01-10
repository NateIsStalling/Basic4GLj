package com.basic4gl.runtime;

import com.basic4gl.runtime.types.OpCode;

public class PatchedBreakPt {
	int m_offset; // Op-Code offset in program
	short m_replacedOpCode; // For active breakpoints: The op-code that
								// has been replaced with the OP_BREAKPT.
	public PatchedBreakPt(){
		m_offset = 0xffff;
		m_replacedOpCode = OpCode.OP_NOP;
	}
}