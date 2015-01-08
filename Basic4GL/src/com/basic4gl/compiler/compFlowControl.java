package com.basic4gl.compiler;

import java.util.HashMap;
import java.util.Map;

class compFlowControl {
	// compFlowControl
		// Used to maintain a stack of open flow control structures, so that when an
		// "endif", "else", "next" or "wend" is found we know what it corresponds
		// to.
		public enum compFlowControlType {
			FCT_IF(0), FCT_ELSE(1), FCT_FOR(2), FCT_WHILE(3), FCT_DO_PRE(4), // Do
			// with
			// a
			// pre-condition
			FCT_DO_POST(5); // Do with a post-condition

			private static Map<Integer, compFlowControlType> mValues = new HashMap<Integer, compFlowControlType>();
			private int mType;

			compFlowControlType(int type) {
				mType = type;
			}

			public int getType() {
				return mType;
			}

			public static compFlowControlType getType(int type) {
				return mValues.get(type);
			}

			static {
				for (compFlowControlType t : compFlowControlType.values()) {
					mValues.put(t.getType(), t);
				}
			}
		}
		
	compFlowControlType m_type; // Type of flow control construct
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

	compFlowControl(compFlowControlType type, int jumpOut, int jumpLoop,
			int line, int col) {
		this(type, jumpOut, jumpLoop, line, col, false, "", false);
	}

	compFlowControl(compFlowControlType type, int jumpOut, int jumpLoop,
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

	compFlowControl() {
		m_type = compFlowControlType.getType(0);
		m_jumpOut = 0;
		m_jumpLoop = 0;
		m_impliedEndif = false;
		m_data = "";
	}
}