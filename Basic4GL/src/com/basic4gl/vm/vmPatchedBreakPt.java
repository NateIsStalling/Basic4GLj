package com.basic4gl.vm;

import com.basic4gl.vm.vmCode.vmOpCode;

public class vmPatchedBreakPt {
	int m_offset; // Op-Code offset in program
	vmOpCode m_replacedOpCode; // For active breakpoints: The op-code that
								// has been replaced with the OP_BREAKPT.
	public vmPatchedBreakPt(){
		m_offset = 0xffff;
		m_replacedOpCode = vmOpCode.OP_NOP;
	}
}
