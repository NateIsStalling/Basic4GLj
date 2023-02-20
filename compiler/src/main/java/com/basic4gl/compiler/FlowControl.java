package com.basic4gl.compiler;

import com.basic4gl.runtime.InstructionPosition;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to maintain a stack of open flow control structures, so that when an
 * "endif", "else", "next" or "wend" is found we know what it corresponds to.
 */
class FlowControl {

	public enum FlowControlType {
		FCT_IF(0),
		FCT_ELSE(1),
		FCT_FOR(2),
		FCT_WHILE(3),
		/**
		 * Do with a pre-condition
		 */
		FCT_DO_PRE(4),
		/**
		 * Do with a post-condition
		 */
		FCT_DO_POST(5);

		private static final Map<Integer, FlowControlType> values = new HashMap<Integer, FlowControlType>();
		private final int type;

		FlowControlType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

		public static FlowControlType getType(int type) {
			return values.get(type);
		}

		static {
			for (FlowControlType t : FlowControlType.values()) {
				values.put(t.getType(), t);
			}
		}
	}


	/**
	 * Type of flow control construct
	 */
	FlowControlType controlType;

	/**
	 * Index of instruction that jumps past (out) of flow control construct
	 */
	int jumpOut;

	/**
	 * Instruction to jump to to loop
	 */
	int jumpLoop;

	InstructionPosition sourcePos;

	/**
	 * Misc data
	 */
	String data;

	/**
	 * If/elseif/else only.
	 * True if there is an implied endif after the explicit endif
	 */
	boolean impliedEndif;

	/**
	 * True if is a block if. Block ifs require "endifs".
	 * Non-block ifs have an implicit endif at the end of the line
	 */
	boolean blockIf;

	FlowControl(FlowControlType type, int jumpOut, int jumpLoop,
				int line, int col) {
		this(type, jumpOut, jumpLoop, line, col, false, "", false);
	}

	FlowControl(FlowControlType type, int jumpOut, int jumpLoop,
				int line, int col, boolean impliedEndif, String data,
				boolean blockIf) {
		controlType = type;
		this.jumpOut = jumpOut;
		this.jumpLoop = jumpLoop;
		sourcePos = new InstructionPosition(line, col);
		this.impliedEndif = impliedEndif;
		this.data = data;
		this.blockIf = blockIf;
	}

	FlowControl() {
		controlType = FlowControlType.getType(0);
		jumpOut = 0;
		jumpLoop = 0;
		impliedEndif = false;
		data = "";
	}
}