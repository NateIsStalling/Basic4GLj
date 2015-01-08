package com.basic4gl.vm.types;

import java.util.HashMap;
import java.util.Map;

import com.basic4gl.vm.types.ValType.BasicValType;

public class OpCode {
	// Enumerated opcodes


	// General
	public static final short OP_NOP = 0x0; 				// No operation
	public static final short OP_END = 0x01; 				// End program
	public static final short OP_LOAD_CONST = 0x02; 		// Load constant into reg
	public static final short OP_LOAD_VAR = 0x03; 			// Load address of variable into reg
	public static final short OP_LOAD_LOCAL_VAR = 0x04; 	// Load address of local variable into reg
	public static final short OP_DEREF = 0x05; 			// Dereference reg (load value at [reg] into reg)
	public static final short OP_ADD_CONST = 0x06; 		// Add constant to reg. Used to address into a
	// structure
	public static final short OP_ARRAY_INDEX = 0x07; 		// IN: reg = array index, reg2 = array address.
	// OUT: reg = element address.
	public static final short OP_PUSH = 0x08; 				// Push reg to stack
	public static final short OP_POP = 0x09; 				// Pop stack into reg2
	public static final short OP_SAVE = 0x0A; 				// Save int, real or string in reg into [reg2]
	public static final short OP_COPY = 0x0B; 				// Copy structure at [reg2] into [reg]. Instruction value
	// points to data type.
	public static final short OP_DECLARE = 0x0C; 			// Allocate a variable
	public static final short OP_DECLARE_LOCAL = 0x0D; 	// Allocate a local variable
	public static final short OP_TIMESHARE = 0x0E; 		// Perform a timesharing break
	public static final short OP_FREE_TEMP = 0x0F; 		// Free temporary data
	public static final short OP_ALLOC = 0x10; 			// Allocate variable memory
	public static final short OP_DATA_READ = 0x11; 		// Read program data into data at [reg]. Instruction
	// contains target data type.
	public static final short OP_DATA_RESET = 0x12; 		// Reset program data pointer
	public static final short OP_SAVE_PARAM = 0x13; 		// Save int, real or string in reg into parameter
	// in pending stack frame. Instruction contains
	// data type. Parameter # is however many
	// parameters have been set since
	// OP_CREATE_USER_FRAME was executed.
	public static final short OP_SAVE_PARAM_PTR = 0x14; 	// Save pointer to data in [reg] into parameter
	// pointer.
	public static final short OP_COPY_USER_STACK = 0x15; 	// Copy data at [reg] to top of user stack.
	// reg is then adjusted to point to the data
	// in the user stack.
	public static final short OP_MOVE_TEMP = 0x16; 		// Free temp data and move data at [reg] into temp
	// data. (Also handles if [reg] points to temp
	// data).
	public static final short OP_CHECK_PTR = 0x17; 		// Check pointer scope before saving to variable.
	// (Ptr is in reg, variable is [reg2])
	public static final short OP_CHECK_PTRS = 0x18; 		// Check all pointers in block at [reg] before
	// copying to [reg2]. Instruction contains data
	// type.
	public static final short OP_REG_DESTRUCTOR = 0x19; 	// Register string destruction block at [reg]
	// (will be temp or stack depending on reg)

	// Flow control
	public static final short OP_JUMP = 0x40; 					// Unconditional jump
	public static final short OP_JUMP_TRUE = 0x41; 			// Jump if reg <> 0
	public static final short OP_JUMP_FALSE = 0x42; 			// Jump if reg == 0
	public static final short OP_CALL_FUNC = 0x43; 			// Call external function
	public static final short OP_CALL_OPERATOR_FUNC = 0x44; 	// Call external operator function
	public static final short OP_CALL_DLL = 0x45; 				// Call DLL function
	public static final short OP_CALL = 0x46; 					// Call VM function
	public static final short OP_CREATE_USER_FRAME = 0x47; 	// Create user stack frame in preparation
	// for a call
	public static final short OP_CALL_USER_FUNC = 0x48; 		// Call user defined function
	public static final short OP_RETURN = 0x49; 				// Return from VM function
	public static final short OP_RETURN_USER_FUNC = 0x4A; 		// Return from user defined function
	public static final short OP_NO_VALUE_RETURNED = 0x4B; 	// Generates a runtime error if executed

	//0x4C - 0x50 added after version 2.5.0
	public static final short OP_BINDCODE = 0x4C;            // Bind a runtime code block to be executed
	public static final short OP_EXEC = 0x4D;                // Execute runtime code block
	public static final short OP_CREATE_RUNTIME_FRAME = 0x4F;// Create a stack frame to call a function/sub in runtime code block
	public static final short OP_END_CALLBACK = 0x50;        // End callback initiated by built-in function or DLL function, and return control to that function.

	// Operations
	// Mathematical
	public static final short OP_OP_NEG = 0x60;
	public static final short OP_OP_PLUS = 0x61; // (Doubles as string concatenation)
	public static final short OP_OP_MINUS = 0x62;
	public static final short OP_OP_TIMES = 0x63;
	public static final short OP_OP_DIV = 0x64;
	public static final short OP_OP_MOD = 0x65;

	// Logical
	public static final short OP_OP_NOT = 0x80;
	public static final short OP_OP_EQUAL = 0x81;
	public static final short OP_OP_NOT_EQUAL = 0x82;
	public static final short OP_OP_GREATER = 0x83;
	public static final short OP_OP_GREATER_EQUAL = 0x84;
	public static final short OP_OP_LESS = 0x85;
	public static final short OP_OP_LESS_EQUAL = 0x86;
	public static final short OP_OP_AND = 0x87;
	public static final short OP_OP_OR = 0x88;
	public static final short OP_OP_XOR = 0x89;

	// Conversion
	public static final short OP_CONV_INT_REAL = 0xA0; 	// Convert integer in reg to real
	public static final short OP_CONV_INT_STRING = 0xA1; 	// Convert integer in reg to string
	public static final short OP_CONV_REAL_STRING = 0xA2; 	// Convert real in reg to string
	public static final short OP_CONV_REAL_INT = 0xA3;
	public static final short OP_CONV_INT_REAL2 = 0xA4; 	// Convert integer in
	// reg2 to real
	public static final short OP_CONV_INT_STRING2 = 0xA5;	// Convert integer in reg2 to string
	public static final short OP_CONV_REAL_STRING2 = 0xA6; // Convert real in reg2 to string
	public static final short OP_CONV_REAL_INT2 = 0xA7;

	// Misc routine
	public static final short OP_RUN = 0xc0; 	// Restart program. Reinitialises variables, display,
	// state e.t.c

	// Debugging
	public static final short OP_BREAKPT = 0xe0; // Breakpoint


	public static String vmOpCodeName (short code){
		switch (code) {
			case OP_NOP:                return  "NOP";
			case OP_END:                return  "END";
			case OP_LOAD_CONST:         return  "LOAD_CONST";
			case OP_LOAD_VAR:           return  "LOAD_VAR";
			case OP_LOAD_LOCAL_VAR:     return  "LOAD_LOCAL_VAR";
			case OP_DEREF:              return  "DEREF";
			case OP_ADD_CONST:          return  "ADD_CONST";
			case OP_ARRAY_INDEX:        return  "ARRAY_INDEX";
			case OP_PUSH:               return  "PUSH";
			case OP_POP:                return  "POP";
			case OP_SAVE:               return  "SAVE";
			case OP_COPY:               return  "COPY";
			case OP_DECLARE:            return  "DECLARE";
			case OP_DECLARE_LOCAL:      return  "DECLARE_LOCAL";
			case OP_TIMESHARE:          return  "TIMESHARE";
			case OP_FREE_TEMP:          return  "FREE_TEMP";
			case OP_ALLOC:              return  "ALLOC";
			case OP_DATA_READ:          return  "DATA_READ";
			case OP_DATA_RESET:         return  "DATA_RESET";
			case OP_SAVE_PARAM:         return  "SAVE_PARAM";
			case OP_SAVE_PARAM_PTR:     return  "SAVE_PARAM_PTR";
			case OP_COPY_USER_STACK:    return  "COPY_USER_STACK";
			case OP_MOVE_TEMP:          return  "MOVE_TEMP";
			case OP_CHECK_PTR:          return  "CHECK_PTR";
			case OP_CHECK_PTRS:         return  "CHECK_PTRS";
			case OP_REG_DESTRUCTOR:     return  "REG_DESTRUCTOR";
			case OP_JUMP:               return  "JUMP";
			case OP_JUMP_TRUE:          return  "JUMP_TRUE";
			case OP_JUMP_FALSE:         return  "JUMP_FALSE";
			case OP_CALL_FUNC:          return  "CALL_FUNC";
			case OP_CALL_OPERATOR_FUNC: return  "CALL_OPERATOR_FUNC";
			case OP_CALL:               return  "CALL";
			case OP_RETURN:             return  "RETURN";
			case OP_CALL_DLL:           return  "CALL_DLL";
			case OP_CREATE_USER_FRAME:  return  "CREATE_USER_FRAME";
			case OP_CALL_USER_FUNC:     return  "CALL_USER_FUNC";
			case OP_RETURN_USER_FUNC:   return  "RETURN_USER_FUNC";
			case OP_NO_VALUE_RETURNED:  return  "NO_VALUE_RETURNED";
			case OP_BINDCODE:           return  "BINDCODE";
			case OP_EXEC:               return  "EXEC";
			case OP_CREATE_RUNTIME_FRAME: return "CREATE_RUNTIME_FRAME";
			case OP_END_CALLBACK:       return  "END_CALLBACK";
			case OP_OP_NEG:             return  "OP_NEG";
			case OP_OP_PLUS:            return  "OP_PLUS";
			case OP_OP_MINUS:           return  "OP_MINUS";
			case OP_OP_TIMES:           return  "OP_TIMES";
			case OP_OP_DIV:             return  "OP_DIV";
			case OP_OP_MOD:             return  "OP_MOD";
			case OP_OP_NOT:             return  "OP_NOT";
			case OP_OP_EQUAL:           return  "OP_EQUAL";
			case OP_OP_NOT_EQUAL:       return  "OP_NOT_EQUAL";
			case OP_OP_GREATER:         return  "OP_GREATER";
			case OP_OP_GREATER_EQUAL:   return  "OP_GREATER_EQUAL";
			case OP_OP_LESS:            return  "OP_LESS";
			case OP_OP_LESS_EQUAL:      return  "OP_LESS_EQUAL";
			case OP_CONV_INT_REAL:      return  "CONV_INT_REAL";
			case OP_CONV_INT_STRING:    return  "CONV_INT_STRING";
			case OP_CONV_REAL_STRING:   return  "CONV_REAL_STRING";
			case OP_CONV_INT_REAL2:     return  "CONV_INT_REAL2";
			case OP_CONV_INT_STRING2:   return  "CONV_INT_STRING2";
			case OP_CONV_REAL_STRING2:  return  "CONV_REAL_STRING2";
			case OP_OP_OR:              return  "OP_OR";
			case OP_OP_AND:             return  "OP_AND";
			case OP_OP_XOR:             return  "OP_XOR";
			case OP_CONV_REAL_INT:      return  "CONV_REAL_INT";
			case OP_CONV_REAL_INT2:     return  "CONV_REAL_INT2";
			case OP_RUN:                return  "OP_RUN";
			case OP_BREAKPT:            return  "OP_BREAKPT";
			default:                    return  "???";
		}
	}
}
