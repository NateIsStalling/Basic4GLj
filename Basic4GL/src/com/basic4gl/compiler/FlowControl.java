package com.basic4gl.compiler;

import java.util.HashMap;
import java.util.Map;

class FlowControl {
	// FlowControl
		// Used to maintain a stack of open flow control structures, so that when an
		// "endif", "else", "next" or "wend" is found we know what it corresponds
		// to.
		public enum FlowControlType {
			FCT_IF(0), FCT_ELSE(1), FCT_FOR(2), FCT_WHILE(3), FCT_DO_PRE(4), // Do
			// with
			// a
			// pre-condition
			FCT_DO_POST(5); // Do with a post-condition

			private static Map<Integer, FlowControlType> mValues = new HashMap<Integer, FlowControlType>();
			private int mType;

			FlowControlType(int type) {
				mType = type;
			}

			public int getType() {
				return mType;
			}

			public static FlowControlType getType(int type) {
				return mValues.get(type);
			}

			static {
				for (FlowControlType t : FlowControlType.values()) {
					mValues.put(t.getType(), t);
				}
			}
		}
		
	FlowControlType m_type; // Type of flow control construct
	int m_jumpOut; // Index of instruction that jumps past (out) of flow
					// control construct
	int m_jumpLoop; // Instruction to jump to to loop
	InstructionPos m_sourcePos;
	String m_data; // Misc data
	boolean m_impliedEndif; // If/elseif/else only. True if there is an
							// implied endif after the explict endif
	boolean m_blockIf; // True if is a block if. Block ifs require "endifs".
						// Non-block ifs have an implicit endif at the end
						// of the line

	FlowControl(FlowControlType type, int jumpOut, int jumpLoop,
				int line, int col) {
		this(type, jumpOut, jumpLoop, line, col, false, "", false);
	}

	FlowControl(FlowControlType type, int jumpOut, int jumpLoop,
				int line, int col, boolean impliedEndif, String data,
				boolean blockIf) {
		m_type = type;
		m_jumpOut = jumpOut;
		m_jumpLoop = jumpLoop;
		m_sourcePos = new InstructionPos(line, col);
		m_impliedEndif = impliedEndif;
		m_data = data;
		m_blockIf = blockIf;
	}

	FlowControl() {
		m_type = FlowControlType.getType(0);
		m_jumpOut = 0;
		m_jumpLoop = 0;
		m_impliedEndif = false;
		m_data = "";
	}
}