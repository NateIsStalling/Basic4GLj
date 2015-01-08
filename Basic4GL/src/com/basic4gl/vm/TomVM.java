package com.basic4gl.vm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//TODO Reimplement libraries
//import com.basic4gl.nate.plugins.PluginDLLFile;
//import com.basic4gl.nate.plugins.PluginDLLManager;
//import com.basic4gl.nate.plugins.TomVMDLLAdapter;
//import com.basic4gl.nate.plugins.util.IDLL_Basic4GL_Runtime;






import com.basic4gl.util.Mutable;
import com.basic4gl.util.Streaming;
import com.basic4gl.vm.stackframe.vmProtectedStackRange;
import com.basic4gl.vm.stackframe.vmStackDestructor;
import com.basic4gl.vm.stackframe.vmUserFunc;
import com.basic4gl.vm.stackframe.vmUserFuncPrototype;
import com.basic4gl.vm.stackframe.vmUserFuncStackFrame;
import com.basic4gl.vm.types.Structure;
import com.basic4gl.vm.types.StructureField;
import com.basic4gl.vm.types.TypeLibrary;
import com.basic4gl.vm.types.ValType;
import com.basic4gl.vm.types.ValTypeSet;
import com.basic4gl.vm.types.ValType.BasicValType;
import com.basic4gl.vm.util.Function;
import com.basic4gl.vm.util.IVMDebugger;
import com.basic4gl.vm.util.Resources;
import com.basic4gl.vm.types.OpCode;
import com.basic4gl.vm.vmVariables.vmVariable;

////////////////////////////////////////////////////////////////////////////////
// TomVM
//
// Virtual machine
public class TomVM extends HasErrorState {
	// Constants
	
		public static final int VM_STEPS = 1000;
	// 10,000 user function stack calls
		public static final int VM_MAXUSERSTACKCALLS = 10000;
	// 100,000,000 variables (.4 gig of memory)
		public static final int VM_MAXDATA = 100000000;
	// First 250,000 (1 meg) reserved for stack/temp data space
		public static final int VM_MAXSTACK = 250000;

		public static final int VM_DATATOSTRINGMAXCHARS = 800; // Any more gets annoying...

	public static final String 		STREAM_HEADER = "Basic4GL stream";
	public static final int         STREAM_VERSION = 2;

	// Error Messages
		public static final String ERR_NOT_IMPLEMENTED = "Opcode not implemented",
				ERR_INVALID = "Invalid opcode",
				ERR_UNDIMMED_VARIABLE = "UnDIMmed variable",
				ERR_BAD_ARRAY_INDEX = "Array index out of range",
				ERR_REDIMMED_VARIABLE = "ReDIMmed variable",
				ERR_BAD_DECLARATION = "Variable declaration error",
				ERR_VARIABLE_TOO_BIG = "Variable is too big",
				ERR_OUT_OF_MEMORY = "Ran out of variable memory",
				ERR_BAD_OPERATOR = "Operator cannot be applied to data type",
				ERR_RETURN_WITHOUT_GOSUB = "Return without gosub",
				ERR_STACK_ERROR = "Stack error",
				ERR_UNSET_POINTER = "Unset pointer",
				ERR_STACK_OVERFLOW = "Stack overflow",
				ERR_ARRAY_SIZE_MISMATCH = "Array sizes are different",
				ERR_ZERO_LENGTH_ARRAY = "Array size must be 0 or greater",
				ERR_OUT_OF_DATA = "Out of DATA",
				ERR_DATA_IS_STRING = "Expected to READ a number, got a text string instead",
				ERR_RUN_CALLED_INSIDE_EXECUTE = "Cannot execute RUN in dynamic code",
				ERR_USER_FUNC_STACK_OVERFLOW = "Ran out of function variable memory",
				ERR_NO_VALUE_RETURNED = "Function did not return a value",
				ERR_POINTER_SCOPE_ERROR = "Pointer scope error",
				ERR_NO_RUNTIME_FUNCTION = "Runtime function not implemented",
				ERR_INVALID_CODE_BLOCK = "Could not find runtime code to execute",
				ERR_DLL_NOT_IMPLEMENTED = "DLL plugins are not implemented in this version of Basic4GL";

			// External functions
		public Vector<Function> mFunctions;
		public Vector<Function> mOperatorFunctions;
		// mFunctions are standard functions where the parameters are pushed
		// to the stack.
		// mOperatorFunctions are generally used for language extension, and
		// perform the job of either a unary or binary operator.
		// That is, they perform Reg2 operator Reg1, and place the result in
		// Reg1.

		
		// Registers
		VMValue mReg, // Register values (when int or float)
				mReg2;
		String mRegString, // Register values when string
				mReg2String;
		int mCurrentUserFrame;
		// The current active frame.
		// Note that this is often but NOT ALWAYS the top of the stack.
		// (When evaluating parameters for a impending function call, the pending
		// function stack frame is the top of the stack, but is not yet active).

		// Runtime stacks
		VmValueStack mStack; // Used for expression evaluation
		Vector<vmUserFuncStackFrame> mUserCallStack; // Call stack for user functions

		// Individual code blocks
	    Vector<vmCodeBlock> mCodeBlocks;
	    int mBoundCodeBlock;
	    
		// Data destruction
		Vector<vmStackDestructor> mStackDestructors;
		Vector<vmStackDestructor> mTempDestructors;

		// Plugin DLLs
		//TODO Reimplement libraries
		//PluginDLLManager m_plugins;
		//IDLL_Basic4GL_Runtime m_pluginRuntime;

		// Debugger
		IVMDebugger mDebugger;
		
		
		    ////////////////////////////////////
	    // Data

	    // Variables, data and data types
	    TypeLibrary mDataTypes;
	    vmData mData;
	    vmVariables mVariables;
		Vector<String> mStringConstants;  // Constant strings declared in program
	    VmStore<String> mStrings;
	    List<Resources> mResources;
	    Vector<vmUserFuncPrototype> mUserFunctionPrototypes;
	    Vector<vmUserFunc> mUserFunctions;

		public Vector<Function> mInitFunctions;	// Initialisation functions
	    
		// Program data
		Vector<VmProgramDataElement> mProgramData; // General purpose program data
													// (e.g declared with "DATA"
													// keyword in BASIC)
		int mProgramDataOffset;

		// //////////////////////////////////
		// Code

		// Instructions
		Vector<vmInstruction> mCode;
		ValTypeSet mTypeSet;

	/**
	 * Instruction pointer
	 */
		private int mIp;

		// Debugging
		ArrayList<vmPatchedBreakPt> mPatchedBreakPts; // Patched in breakpoints
		ArrayList<vmTempBreakPt> mTempBreakPts; // Temporary breakpoints, generated
													// for stepping over a line

		boolean m_paused, // Set to true when program hits a breakpoint. (Or can be
							// set by caller.)
				m_breakPtsPatched; // Set to true if breakpoints are patched and in
									// synchronisation with compiled code

									
		
	//TODO Reimplement libraries
		//public TomVM(PluginDLLManager plugins, IVMDebugger debugger) {
		public TomVM(IVMDebugger debugger) {
			this(debugger, VM_MAXDATA, VM_MAXSTACK);
		}
		//TODO Reimplement libraries
		//public TomVM(PluginDLLManager plugins, IVMDebugger debugger,
		//		int maxDataSize, int maxStackSize) {
		public TomVM(IVMDebugger debugger,
				int maxDataSize, int maxStackSize) {
			mDebugger = debugger;
			
			mData = new vmData(maxDataSize, maxStackSize);
			mDataTypes = new TypeLibrary();
			mVariables = new vmVariables(mData, mDataTypes);
			
			mReg = new VMValue();
			mReg2 = new VMValue();
			mResources = new ArrayList<Resources>();
			
			mStrings = new VmStore<String>("");
			mStack = new VmValueStack(mStrings);
			mUserCallStack = new Vector<vmUserFuncStackFrame>();
			mStackDestructors = new Vector<vmStackDestructor>();
			mTempDestructors = new Vector<vmStackDestructor>();
			
			mProgramData = new Vector<VmProgramDataElement>();
			mCodeBlocks = new Vector<vmCodeBlock>();
			mStringConstants = new Vector<String>();

			mTypeSet = new ValTypeSet();
			
			mCode = new Vector<vmInstruction>();
			mFunctions = new Vector<Function>();
			mUserFunctions = new Vector<vmUserFunc>();
			mUserFunctionPrototypes = new Vector<vmUserFuncPrototype>();
			mPatchedBreakPts = new ArrayList<vmPatchedBreakPt>();
			mTempBreakPts = new ArrayList<vmTempBreakPt>();

			mInitFunctions = new Vector<Function>();
			//TODO Reimplement libraries
			//m_plugins = plugins;
			// Create plugin runtime
			//m_pluginRuntime = new TomVMDLLAdapter(this,
			//		m_plugins.StructureManager());

			New();
		}
		
			public void New() // New program
		{

			// Clear variables, data and data types
			Clr();
			mVariables.Clear();
			mDataTypes.Clear();
			mProgramData.clear();
			mCodeBlocks.clear();

			// Clear string constants
			mStringConstants.clear();

			// Deallocate code
			mCode.clear();
			mTypeSet.Clear();
			mUserFunctions.clear();
			mUserFunctionPrototypes.clear();
			mIp = 0;
			m_paused = false;

			// Clear breakpoints
			mPatchedBreakPts.clear();
			mTempBreakPts.clear();
			m_breakPtsPatched = false;
		}
		
		public void Clr() // Clear variables
		{
			mVariables.Deallocate(); // Deallocate variables
			mData.Clear(); // Deallocate variable data
			mStrings.Clear(); // Clear strings
			mStack.Clear(); // Clear runtime stacks
			mUserCallStack.clear();
			mStackDestructors.clear();
			mTempDestructors.clear();
			mCurrentUserFrame = -1;
			mBoundCodeBlock = 0;
			
			// Clear resources
			ClearResources();

			// Init registers
			mReg.setIntVal(0);
			mReg2.setIntVal(0);
			mRegString = "";
			mReg2String = "";
			mProgramDataOffset = 0;
		}
		
		public void ClearResources() {
			// Clear resources
			for (Resources res : mResources)
				res.Clear();
		}
		
		public void Reset() {

			// Clear error state
			ClearError();

			// Deallocate variables
			Clr();

			// Call registered initialisation functions
			for (int i = 0; i < mInitFunctions.size(); i++)
				mInitFunctions.get(i).run(this);

			// Move to start of program
			mIp = 0;
			m_paused = false;
		}
		
		public void Continue() {
			// Reduced from 0xffffffff since Java doesn't support unsigned ints
			Continue(0x7fffffff);
		}

		public void Continue(int steps) // Continue execution from last position
		{
			
			ClearError();
			m_paused = false;

			// //////////////////////////////////////////////////////////////////////////
			// Virtual machine main loop
			vmInstruction instruction;
			int stepCount = 0;
			int tempI;

			step: while(true) {	//breaks on last line; taking advantage of loops having labels for continue statements 
				
				// Count steps
				if (++stepCount > steps)
					return;
				
				
				instruction = mCode.get(mIp);
				switch (instruction.mOpCode) {
				case OpCode.OP_NOP:
					mIp++; // Proceed to next instruction
					continue step;
				case OpCode.OP_END:
					break;
				case OpCode.OP_LOAD_CONST:

					// Load value
					if (instruction.mType == BasicValType.VTP_STRING.getType()) {
						assert (instruction.mValue.getIntVal() >= 0);
						assert (instruction.mValue.getIntVal() < mStringConstants.size());
						setRegString(mStringConstants.get(instruction.mValue
								.getIntVal()));
					} else {
						setReg(new VMValue(instruction.mValue));
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_LOAD_VAR: {

					// Load variable.
					// Instruction contains index of variable.
					assert (mVariables.IndexValid(instruction.mValue.getIntVal()));
					vmVariable var = mVariables.Variables().get(
							instruction.mValue.getIntVal());
					if (var.Allocated()) {
						// Load address of variable's data into register
						Reg().setIntVal(var.m_dataIndex);
						mIp++; // Proceed to next instruction
						continue step;
					}
					SetError(ERR_UNDIMMED_VARIABLE);
					break;
				}

				case OpCode.OP_LOAD_LOCAL_VAR: {

					// Find current stack frame
					assert (mCurrentUserFrame >= 0);
					assert (mCurrentUserFrame < mUserCallStack.size());
					vmUserFuncStackFrame currentFrame = mUserCallStack
							.get(mCurrentUserFrame);

					// Find variable
					int index = instruction.mValue.getIntVal();

					// Instruction contains index of variable.
					if (currentFrame.localVarDataOffsets.get(index) != 0) {
						// Load address of variable's data into register
						Reg().setIntVal(currentFrame.localVarDataOffsets.get(index));
						mIp++; // Proceed to next instruction
						continue step;
					}
					SetError(ERR_UNDIMMED_VARIABLE);
					break;
				}

				case OpCode.OP_DEREF: {

					// Dereference reg.
					if (Reg().getIntVal() != 0) {
						assert (mData.IndexValid(Reg().getIntVal()));
						// Find value that reg points to
						VMValue val = mData.Data().get(Reg().getIntVal());
						switch (BasicValType.getType(instruction.mType)) {
						case VTP_INT:
						case VTP_REAL:
							setReg(val);
							mIp++; // Proceed to next instruction
							continue step;
						case VTP_STRING:
							assert (mStrings.IndexValid(val.getIntVal()));
							setRegString(mStrings.Value(val.getIntVal()));
							mIp++; // Proceed to next instruction
							continue step;
						default:
							break;
						}
						assert (false);
					}
					SetError(ERR_UNSET_POINTER);
					break;
				}
				case OpCode.OP_ADD_CONST:
					// Check pointer
					if (Reg().getIntVal() != 0) {
						Reg().setIntVal(Reg().getIntVal()
								+ instruction.mValue.getIntVal());
						mIp++; // Proceed to next instruction
						continue step;
					}
					SetError(ERR_UNSET_POINTER);
					break;

				case OpCode.OP_ARRAY_INDEX:

					if (Reg2().getIntVal() != 0) {
						// Input: mReg2 = Array address
						// mReg = Array index
						// Output: mReg = Element address
						assert (mData.IndexValid(Reg2().getIntVal()));
						assert (mData.IndexValid(Reg2().getIntVal() + 1));

						// mReg2 points to array header (2 values)
						// First value is highest element (i.e number of elements +
						// 1)
						// Second value is size of array element.
						// Array data immediately follows header
						if (Reg().getIntVal() >= 0
								&& Reg().getIntVal() < mData.Data()
										.get(Reg2().getIntVal()).getIntVal()) {
							assert (mData.Data().get(Reg2().getIntVal() + 1)
									.getIntVal() >= 0);
							Reg().setIntVal(Reg2().getIntVal()
									+ 2
									+ Reg().getIntVal()
									* mData.Data().get(Reg2().getIntVal() + 1)
											.getIntVal());

							mIp++; // Proceed to next instruction
							continue step;
						}
						SetError(ERR_BAD_ARRAY_INDEX);
						break;
					}
					SetError(ERR_UNSET_POINTER);
					break;

				case OpCode.OP_PUSH:

					// Push register to stack
					if (instruction.mType == BasicValType.VTP_STRING.getType())
						mStack.PushString(RegString());
					else
						mStack.Push(Reg());

					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_POP:

					// Pop reg2 from stack
					if (instruction.mType == BasicValType.VTP_STRING.getType()){
						
						setReg2String(mStack.PopString());
					}else{
						Reg2().setVal(mStack.Pop());
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_SAVE: {

					// Save reg into [reg2]
					if (Reg2().getIntVal() > 0) {
						assert (mData.IndexValid(Reg2().getIntVal()));
						VMValue dest = mData.Data().get(Reg2().getIntVal());
						switch (BasicValType.getType(instruction.mType)) {
						case VTP_INT:
						case VTP_REAL:
							//mData.Data().set(mReg2.getIntVal(), new VMValue(mReg));
							dest.setVal(Reg());
							
							mIp++; // Proceed to next instruction
							continue step;
						case VTP_STRING:

							// Allocate string space if necessary
							if (dest.getIntVal() == 0)
								dest.setIntVal(mStrings.Alloc());

							// Copy string value
							mStrings.setValue(dest.getIntVal(), RegString());
							mIp++; // Proceed to next instruction
							continue step;
						default:
							break;
						}
						assert (false);
					}
					SetError(ERR_UNSET_POINTER);
					break;
				}

				case OpCode.OP_COPY: {

					// Copy data
					if (CopyData(Reg().getIntVal(), Reg2().getIntVal(),
							mTypeSet.GetValType(instruction.mValue.getIntVal()))) {
						mIp++; // Proceed to next instruction
						continue step;
					} else {
						break;
					}
				}
				case OpCode.OP_DECLARE: {

					// Allocate variable.
					assert (mVariables.IndexValid(instruction.mValue.getIntVal()));
					vmVariable var = mVariables.Variables().get(instruction.mValue.getIntVal());

					// Must not already be allocated
					if (var.Allocated()) {
						SetError(ERR_REDIMMED_VARIABLE);
						break;
					}

					// Pop and validate array dimensions sizes into type (if
					// applicable)
					if (var.m_type.PhysicalPointerLevel() == 0
							&& !PopArrayDimensions(var.m_type))
						break;

					// Validate type size
					if (!ValidateTypeSize(var.m_type))
						break;

					// Allocate variable
					var.Allocate(mData, mDataTypes);

					mIp++; // Proceed to next instruction
					continue step;
				}
				case OpCode.OP_DECLARE_LOCAL: {

					// Allocate local variable

					// Find current stack frame
					assert (mCurrentUserFrame >= 0);
					assert (mCurrentUserFrame < mUserCallStack.size());
					vmUserFuncStackFrame currentFrame = mUserCallStack.get(mCurrentUserFrame);
					vmUserFunc userFunc = mUserFunctions.get(currentFrame.userFuncIndex);
					vmUserFuncPrototype prototype = mUserFunctionPrototypes.get(userFunc.prototypeIndex);
					
					// Find variable type
					int index = instruction.mValue.getIntVal();
					assert (index >= 0);
					assert (index < prototype.localVarTypes.size());
					ValType type = prototype.localVarTypes.get(index);

					// Must not already be allocated
					if (currentFrame.localVarDataOffsets.get(index) != 0) {
						SetError(ERR_REDIMMED_VARIABLE);
						break;
					}

					// Pop and validate array dimensions sizes into type (if
					// applicable)
					if (type.PhysicalPointerLevel() == 0
							&& !PopArrayDimensions(type))
						break;

					// Validate type size
					if (!ValidateTypeSizeForStack(type))
						break;

					// Allocate new data
					int dataIndex = mData.AllocateStack(mDataTypes.DataSize(type));

					// Initialise it
					mData.InitData(dataIndex, type, mDataTypes);

					// Store data index in stack frame
					currentFrame.localVarDataOffsets.set(index, dataIndex);

					// Also store in register, so that OpCode.OP_REG_DESTRUCTOR can be used
					Reg().setIntVal(dataIndex);

					mIp++; // Proceed to next instruction
					continue step;

				}
				case OpCode.OP_JUMP:

					// Jump
					assert (instruction.mValue.getIntVal() >= 0);
					assert (instruction.mValue.getIntVal() < mCode.size());
					mIp = instruction.mValue.getIntVal();
					continue step; // Proceed without incrementing instruction

				case OpCode.OP_JUMP_TRUE:

					// Jump if reg != 0
					assert (instruction.mValue.getIntVal() >= 0);
					assert (instruction.mValue.getIntVal() < mCode.size());
					if (Reg().getIntVal() != 0) {
						mIp = instruction.mValue.getIntVal();
						continue step; // Proceed without incrementing instruction
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_JUMP_FALSE:

					// Jump if reg == 0
					assert (instruction.mValue.getIntVal() >= 0);
					assert (instruction.mValue.getIntVal() < mCode.size());
					if (Reg().getIntVal() == 0) {
						mIp = instruction.mValue.getIntVal();
						continue step; // Proceed without incrementing instruction
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_NEG:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(-Reg().getIntVal());
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setRealVal(-Reg().getRealVal());
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_PLUS:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(Reg().getIntVal() + Reg2().getIntVal());
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setRealVal(Reg().getRealVal() + Reg2().getRealVal());
					else if (instruction.mType == BasicValType.VTP_STRING.getType())
						setRegString(Reg2String() + RegString());
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_MINUS:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(Reg2().getIntVal() - Reg().getIntVal());
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setRealVal(Reg2().getRealVal() - Reg().getRealVal());
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_TIMES:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(Reg().getIntVal() * Reg2().getIntVal());
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setRealVal(Reg().getRealVal() * Reg2().getRealVal());
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_DIV:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(Reg2().getIntVal() / Reg().getIntVal());
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setRealVal(Reg2().getRealVal() / Reg().getRealVal());
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_MOD:
					if (instruction.mType == BasicValType.VTP_INT.getType()) {
						int i = Reg2().getIntVal() % Reg().getIntVal();
						if (i >= 0)
							Reg().setIntVal(i);
						else
							Reg().setIntVal(Reg().getIntVal() + i);
					} else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_NOT:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(Reg().getIntVal() == 0 ? -1 : 0);
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_EQUAL:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(
								Reg2().getIntVal() == Reg().getIntVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setIntVal(
								Reg2().getRealVal() == Reg().getRealVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_STRING.getType())
						Reg().setIntVal(Reg2String().equals(RegString()) ? -1 : 0);
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_NOT_EQUAL:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(
								Reg2().getIntVal() != Reg().getIntVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setIntVal(
								Reg2().getRealVal() != Reg().getRealVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_STRING.getType())
						Reg().setIntVal(!Reg2String().equals(RegString()) ? -1 : 0);
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_GREATER:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(
								Reg2().getIntVal() > Reg().getIntVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setIntVal(
								Reg2().getRealVal() > Reg().getRealVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_STRING.getType())
						Reg().setIntVal(
								(Reg2String().compareTo(RegString()) > 0) ? -1 : 0);
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_GREATER_EQUAL:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(
								Reg2().getIntVal() >= Reg().getIntVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setIntVal(
								Reg2().getRealVal() >= Reg().getRealVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_STRING.getType())
						Reg().setIntVal(
								(Reg2String().compareTo(RegString()) >= 0) ? -1 : 0);
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_LESS:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(
								Reg2().getIntVal() < Reg().getIntVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setIntVal(
								Reg2().getRealVal() < Reg().getRealVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_STRING.getType())
						Reg().setIntVal(
								(Reg2String().compareTo(RegString()) < 0) ? -1 : 0);
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_LESS_EQUAL:
					if (instruction.mType == BasicValType.VTP_INT.getType())
						Reg().setIntVal(
								Reg2().getIntVal() <= Reg().getIntVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_REAL.getType())
						Reg().setIntVal(
								Reg2().getRealVal() <= Reg().getRealVal() ? -1 : 0);
					else if (instruction.mType == BasicValType.VTP_STRING.getType())
						Reg().setIntVal(
								(Reg2String().compareTo(RegString()) <= 0) ? -1 : 0);
					else {
						SetError(ERR_BAD_OPERATOR);
						break;
					}
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CONV_INT_REAL:
					Reg().setRealVal((float)Reg().getIntVal());
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CONV_INT_REAL2:
					Reg2().setRealVal((float)Reg2().getIntVal());
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CONV_REAL_INT:
					Reg().setIntVal((int)Reg().getRealVal());
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CONV_REAL_INT2:
					Reg2().setIntVal((int)Reg2().getRealVal());
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CONV_INT_STRING:
					setRegString(String.valueOf(Reg().getIntVal()));
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CONV_REAL_STRING:
					setRegString(String.valueOf(Reg().getRealVal()));
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CONV_INT_STRING2:
					setReg2String(String.valueOf(Reg2().getIntVal()));
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CONV_REAL_STRING2:
					setReg2String(String.valueOf(Reg2().getRealVal()));
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_AND:
					Reg().setIntVal(Reg().getIntVal() & Reg2().getIntVal());
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_OR:
					Reg().setIntVal(Reg().getIntVal() | Reg2().getIntVal());
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_OP_XOR:
					Reg().setIntVal(Reg().getIntVal() ^ Reg2().getIntVal());
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_CALL_FUNC:

					assert (instruction.mValue.getIntVal() >= 0);
					assert (instruction.mValue.getIntVal() < mFunctions.size());

					// Call external function
					mFunctions.get(instruction.mValue.getIntVal()).run(this);
					if (!Error()) {
						mIp++; // Proceed to next instruction
						continue step;
					}
					break;

				case OpCode.OP_CALL_OPERATOR_FUNC:

					assert (instruction.mValue.getIntVal() >= 0);
					assert (instruction.mValue.getIntVal() < mOperatorFunctions.size());

					// Call external function
					mOperatorFunctions.get(instruction.mValue.getIntVal()).run(this);
					if (!Error()) {
						mIp++; // Proceed to next instruction
						continue step;
					}
					break;

				case OpCode.OP_TIMESHARE:
					mIp++; // Move on to next instruction
					break; // And return

				case OpCode.OP_FREE_TEMP:

					// Free temporary data
					UnwindTemp();
					mData.FreeTemp();
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_ALLOC: {

					// Extract type, and array dimensions
					ValType type = mTypeSet.GetValType(instruction.mValue
							.getIntVal());
					if (!PopArrayDimensions(type))
						break;

					// Validate type size
					if (!ValidateTypeSize(type))
						break;

					// Allocate and initialise new data
					Reg().setIntVal(mData.Allocate(mDataTypes.DataSize(type)));
					mData.InitData(Reg().getIntVal(), type, mDataTypes);

					mIp++; // Proceed to next instruction
					continue step;
				}

				case OpCode.OP_CALL: {

					// Call
					assert (instruction.mValue.getIntVal() >= 0);
					assert (instruction.mValue.getIntVal() < mCode.size());

					// Check for stack overflow
					if (mUserCallStack.size() >= VM_MAXUSERSTACKCALLS) {
						SetError(ERR_STACK_OVERFLOW);
						break;
					}

					// Push stack frame, with return address
					mUserCallStack.add(new vmUserFuncStackFrame());
					vmUserFuncStackFrame stackFrame = mUserCallStack.lastElement();
					stackFrame.InitForGosub(mIp + 1);

					// Jump to subroutine
					mIp = instruction.mValue.getIntVal();
					continue step; // Proceed without incrementing instruction
				}
				case OpCode.OP_RETURN:

					// Return from GOSUB
					
					// Pop and validate return address
					if (mUserCallStack.isEmpty()) {
						SetError(ERR_RETURN_WITHOUT_GOSUB);
						break;
					}
					// -1 means GOSUB. Should be impossible to execute 
					// an OpCode.OP_RETURN if stack top is not a GOSUB 
					assert(mUserCallStack.lastElement().userFuncIndex == -1);
					
					tempI = mUserCallStack.lastElement().returnAddr;
					mUserCallStack.remove(mUserCallStack.size() - 1);
					if (tempI >= mCode.size()) {
						SetError(ERR_STACK_ERROR);
						break;
					}
					
					// Jump to return address
					mIp = tempI;
					continue step; // Proceed without incrementing instruction

				case OpCode.OP_CALL_DLL: {

					// Call plugin DLL function
					//TODO Reimplement libraries
					//int index = instruction.mValue.getIntVal();
					//m_plugins.GetPluginDLL(index >> 24)
					//		.GetFunction(index & 0x00ffffff).Run(m_pluginRuntime);
					SetError(ERR_DLL_NOT_IMPLEMENTED); //Remove line when libraries are implemented
					if (!Error()) {
						mIp++; // Proceed to next instruction
						continue step;
					}
					break;
				}

				case OpCode.OP_CREATE_USER_FRAME: {

	        // Check for stack overflow
	        if (mUserCallStack.size () >= VM_MAXUSERSTACKCALLS) {
	            SetError(ERR_STACK_OVERFLOW);
	            break;
	        }

	        // Create and initialize stack frame
	        int funcIndex = instruction.mValue.getIntVal();
	        mUserCallStack.add(new vmUserFuncStackFrame());
	        vmUserFuncStackFrame stackFrame = mUserCallStack.lastElement();
	        stackFrame.InitForUserFunction(
	            mUserFunctionPrototypes.get(mUserFunctions.get(funcIndex).prototypeIndex),
	            funcIndex);
				
					// Save previous stack frame data
					mData.SaveState(stackFrame.prevStackTop,
							stackFrame.prevTempDataLock);

					mIp++; // Proceed to next instruction
					continue step;
				}
	case OpCode.OP_CREATE_RUNTIME_FRAME: {
	        assert(!mCodeBlocks.isEmpty());

	        // Find function index
	        int funcIndex = -1;

	        // Look for function in bound code block
	        int runtimeIndex = instruction.mValue.getIntVal();
	        if (mBoundCodeBlock > 0 && mBoundCodeBlock < mCodeBlocks.size()) {
	            vmCodeBlock codeBlock = mCodeBlocks.get(mBoundCodeBlock);
	            if (codeBlock.programOffset >= 0)
	                funcIndex = codeBlock.GetRuntimeFunction(runtimeIndex).functionIndex;
	        }

	        // If not found, look in main program
	        if (funcIndex < 0)
	            funcIndex = mCodeBlocks.get(0).GetRuntimeFunction(runtimeIndex).functionIndex;

	        // No function => Runtime error
	        if (funcIndex < 0) {
	            SetError(ERR_NO_RUNTIME_FUNCTION);
	            break;
	        }

	        // From here on the logic is the same as OpCode.OP_CREATE_USER_FRAME
	        // Check for stack overflow
	        if (mUserCallStack.size () >= VM_MAXUSERSTACKCALLS) {
	            SetError(ERR_STACK_OVERFLOW);
	            break;
	        }

	        // Create and initialize stack frame
	        mUserCallStack.add(new vmUserFuncStackFrame());
	        vmUserFuncStackFrame stackFrame = mUserCallStack.lastElement();
	        stackFrame.InitForUserFunction(
	            mUserFunctionPrototypes.get(mUserFunctions.get(funcIndex).prototypeIndex),
	            funcIndex);

	        // Save previous stack frame data
	        mData.SaveState(stackFrame.prevStackTop, stackFrame.prevTempDataLock);

	        mIp++; // Proceed to next instruction
			continue step;
	    }
				case OpCode.OP_CALL_USER_FUNC: {

					// Call user defined function
					vmUserFuncStackFrame stackFrame = mUserCallStack.lastElement();
					vmUserFunc userFunc = mUserFunctions
							.get(stackFrame.userFuncIndex);

					// Make active
					stackFrame.prevCurrentFrame = mCurrentUserFrame;
					mCurrentUserFrame = mUserCallStack.size() - 1;

					// Call function
					stackFrame.returnAddr = mIp + 1;
					mIp = userFunc.programOffset;
					continue step; // Proceed without incrementing instruction
				}

				case OpCode.OP_RETURN_USER_FUNC: {
					assert (mUserCallStack.size() > 0);

					// Find current stack frame
					vmUserFuncStackFrame stackFrame = mUserCallStack.lastElement();
					assert(stackFrame.userFuncIndex >= 0);
					
					// Restore previous stack frame data
					boolean doFreeTempData = instruction.mValue.getIntVal() == 1;
					if (doFreeTempData)
						UnwindTemp();
					UnwindStack(stackFrame.prevStackTop);
					mData.RestoreState(stackFrame.prevStackTop,
							stackFrame.prevTempDataLock, doFreeTempData);

					// Return to return address
					mIp = stackFrame.returnAddr;

					// Make previous frame active
					mCurrentUserFrame = stackFrame.prevCurrentFrame;

					// Remove stack frame
					mUserCallStack.remove(mUserCallStack.size() - 1);

					continue step; // Proceed without incrementing instruction
				}

				case OpCode.OP_NO_VALUE_RETURNED:
					SetError(ERR_NO_VALUE_RETURNED);
					break;
	case OpCode.OP_BINDCODE:
	        mBoundCodeBlock = Reg().getIntVal();
	        mIp++; // Proceed to next instruction
			continue step;

	    case OpCode.OP_EXEC:

	        // Call runtime compiled code block.
	        // Call is like a GOSUB.
	        // RETURN will return back to the next op-code
	        if (mBoundCodeBlock > 0 && mBoundCodeBlock < mCodeBlocks.size()) {
	            vmCodeBlock codeBlock = mCodeBlocks.get(mBoundCodeBlock);
	            if (codeBlock.programOffset >= 0) {

	                // From here the code is the same as OpCode.OP_CALL
	                assert(codeBlock.programOffset >= 0);
	                assert(codeBlock.programOffset < mCode.size ());

	                // Check for stack overflow
	                if (mUserCallStack.size () >= VM_MAXUSERSTACKCALLS) {
	                    SetError(ERR_STACK_OVERFLOW);
	                    break;
	                }

	                // Push stack frame, with return address
	                mUserCallStack.add(new vmUserFuncStackFrame());
	                vmUserFuncStackFrame stackFrame = mUserCallStack.lastElement();
	                stackFrame.InitForGosub(mIp + 1);

	                // Jump to subroutine
	                mIp = codeBlock.programOffset;
	                continue step;
	            }
	        }

	        SetError(ERR_INVALID_CODE_BLOCK);
	        break;

	    case OpCode.OP_END_CALLBACK:
	        break;          // Timeshare break. Calling code will then detect this op-code has been reached

				case OpCode.OP_DATA_READ:

					// Read program data into register
					if (ReadProgramData(instruction.mType)) {
						mIp++; // Proceed to next instruction
						continue step;
					} else {
						break;
					}
				case OpCode.OP_DATA_RESET:

					mProgramDataOffset = instruction.mValue.getIntVal();
					mIp++; // Proceed to next instruction
					continue step;

				case OpCode.OP_SAVE_PARAM: {

					// Allocate parameter data
					if (!mData.StackRoomFor(1)) {
						SetError(ERR_USER_FUNC_STACK_OVERFLOW);
						break;
					}
					int dataIndex = mData.AllocateStack(1);
					int paramIndex = instruction.mValue.getIntVal();

					// Initialize parameter
					assert (!mUserCallStack.isEmpty());
					mUserCallStack.lastElement().localVarDataOffsets.set(
							paramIndex, dataIndex);

					// Transfer register value to parameter
					VMValue dest = mData.Data().get(dataIndex);
					switch (BasicValType.getType(instruction.mType)) {
					case VTP_INT:
					case VTP_REAL:
						//TODO Confirm value is properly set
						//TODO Check other "dest" variables
						dest.setVal(Reg());
						break;
					case VTP_STRING:

						// Allocate string space
						dest.setIntVal(mStrings.Alloc());

						// Copy string value
						mStrings.setValue(dest.getIntVal(), RegString());
						break;
					default:
						assert (false);
					}

					// Save parameter offset in register (so that OpCode.OP_REG_DESTRUCTOR
					// will work)
					Reg().setIntVal(dataIndex);
					mIp++; // Proceed to next instruction
					continue step;
				}

				case OpCode.OP_COPY_USER_STACK: {

					// Copy data pointed to by mReg into next stack frame
					// parameter.
					// Instruction value points to the parameter data type.
					if (CopyToParam(Reg().getIntVal(),
							mTypeSet.GetValType(instruction.mValue.getIntVal()))) {
						mIp++; // Proceed to next instruction
						continue step;
					} else {
						break;
					}
				}

				case OpCode.OP_MOVE_TEMP: {

					if (MoveToTemp(Reg().getIntVal(),
							mTypeSet.GetValType(instruction.mValue.getIntVal()))) {
						mIp++; // Proceed to next instruction
						continue step;
					} else {
						break;
					}
				}

				case OpCode.OP_CHECK_PTR: {

					if (CheckPointer(Reg2().getIntVal(), Reg().getIntVal())) {
						mIp++; // Proceed to next instruction
						continue step;
					} else {
						SetError(ERR_POINTER_SCOPE_ERROR);
						break;
					}
				}

				case OpCode.OP_CHECK_PTRS: {
					if (CheckPointers(Reg().getIntVal(),
							mTypeSet.GetValType(instruction.mValue.getIntVal()),
							Reg2().getIntVal())) {
						mIp++; // Proceed to next instruction
						continue step;
					} else {
						SetError(ERR_POINTER_SCOPE_ERROR);
						break;
					}
				}

				case OpCode.OP_REG_DESTRUCTOR: {

					// Register destructor for data pointed to by mReg.
					int ptr = Reg().getIntVal();
					assert (ptr >= 0);
					if (ptr == 0)
						; // Do nothing
					else if (ptr < mData.TempData()) {

						// Pointer into temp data found
						assert (mTempDestructors.isEmpty() || mTempDestructors
								.lastElement().addr < ptr);
						mTempDestructors.add(new vmStackDestructor(ptr,
								instruction.mValue.getIntVal()));
					} else if (ptr >= mData.StackTop() && ptr < mData.Permanent()) {

						// Pointer into stack data found
						assert (mStackDestructors.isEmpty() || mStackDestructors
								.lastElement().addr > ptr);
						mStackDestructors.add(new vmStackDestructor(ptr,
								instruction.mValue.getIntVal()));
					}
					mIp++; // Proceed to next instruction
					continue step;
				}

				case OpCode.OP_SAVE_PARAM_PTR: {

					// Save register pointer into param pointer
					assert (!mUserCallStack.isEmpty());
					mUserCallStack.lastElement().localVarDataOffsets.set(
							instruction.mValue.getIntVal(), Reg().getIntVal());

					mIp++; // Proceed to next instruction
					continue step;
				}

				case OpCode.OP_RUN:

					// If the stack is not empty, it means we are inside an
					// Execute() call.
					// Resetting the program is not a good idea, as we will lose all
					// the
					// stacked state that the Execute() call is expecting to be
					// present when
					// it returns.
					if (!mStack.Empty())
						SetError(ERR_RUN_CALLED_INSIDE_EXECUTE);
					else
						Reset(); // Reset program
					break; // Timeshare break

				case OpCode.OP_BREAKPT:
					m_paused = true; // Pause program
					break; // Timeshare break

				default:
					SetError(ERR_INVALID);
				}
				break; //DO NOT LOOP
			}	
		}
		// Constant strings
		public int StoreStringConstant(String string) {
			int index = mStringConstants.size();
			mStringConstants.add(string);
			return index;
		}
		
			// Internal methods
		void BlockCopy(int sourceIndex, int destIndex, int size) {

			// Block copy data
			assert (mData.IndexValid(sourceIndex));
			assert (mData.IndexValid(sourceIndex + size - 1));
			assert (mData.IndexValid(destIndex));
			assert (mData.IndexValid(destIndex + size - 1));
			for (int i = 0; i < size; i++)
				mData.Data().set(destIndex + i, mData.Data().get(sourceIndex + i));
		}

		void CopyStructure(int sourceIndex, int destIndex, ValType type) {
			assert (mDataTypes.TypeValid(type));
			assert (type.VirtualPointerLevel() == 0);
			assert (type.m_arrayLevel == 0);
			assert (type.m_basicType >= 0);

			// Find structure definition
			Structure s = mDataTypes.Structures().get(type.m_basicType);

			// Copy fields in structure
			for (int i = 0; i < s.m_fieldCount; i++) {
				StructureField f = mDataTypes.Fields().get(s.m_firstField + i);
				CopyField(sourceIndex + f.m_dataOffset, destIndex + f.m_dataOffset,
						f.m_type);
			}
		}

		void CopyArray(int sourceIndex, int destIndex, ValType type) {
			assert (mDataTypes.TypeValid(type));
			assert (type.VirtualPointerLevel() == 0);
			assert (type.m_arrayLevel > 0);
			assert (mData.IndexValid(sourceIndex));
			assert (mData.IndexValid(destIndex));
			assert (mData.Data().get(sourceIndex).getIntVal() == mData.Data()
					.get(destIndex).getIntVal()); // Array sizes match
			assert (mData.Data().get(sourceIndex + 1).getIntVal() == mData.Data()
					.get(destIndex + 1).getIntVal()); // Element sizes match

			// Find element type and size
			ValType elementType = type;
			elementType.m_arrayLevel--;
			int elementSize = mData.Data().get(sourceIndex + 1).getIntVal();

			// Copy elements
			for (int i = 0; i < mData.Data().get(sourceIndex).getIntVal(); i++) {
				if (elementType.m_arrayLevel > 0)
					CopyArray(sourceIndex + 2 + i * elementSize, destIndex + 2 + i
							* elementSize, elementType);
				else
					CopyField(sourceIndex + 2 + i * elementSize, destIndex + 2 + i
							* elementSize, elementType);
			}
		}
		
		void CopyField(int sourceIndex, int destIndex, ValType type) {

			assert (mDataTypes.TypeValid(type));

			// If type is basic string, copy string value
			if (type.Equals(BasicValType.VTP_STRING)) {
				VMValue src = mData.Data().get(sourceIndex);
				VMValue dest = mData.Data().get(destIndex);
				if (src.getIntVal() > 0 || dest.getIntVal() > 0) {

					// Allocate string space if necessary
					if (dest.getIntVal() == 0)
						dest.setIntVal(mStrings.Alloc());

					// Copy string value
					mStrings.setValue(dest.getIntVal(), mStrings.Value(mData.Data().get(sourceIndex).getIntVal()));
				}
			}

			// If type is basic, or pointer then just copy value
			else if (type.IsBasic() || type.VirtualPointerLevel() > 0)
				mData.Data().set(destIndex, mData.Data().get(sourceIndex));

			// If contains no strings, can just block copy
			else if (!mDataTypes.ContainsString(type))
				BlockCopy(sourceIndex, destIndex, mDataTypes.DataSize(type));

			// Otherwise copy array or structure
			else if (type.m_arrayLevel > 0)
				CopyArray(sourceIndex, destIndex, type);
			else
				CopyStructure(sourceIndex, destIndex, type);
		}
		boolean CopyData(int sourceIndex, int destIndex, ValType type) {
			assert (mDataTypes.TypeValid(type));
			assert (type.VirtualPointerLevel() == 0);

			// If a referenced type (which it should always be), convert to regular
			// type.
			// (To facilitate comparisons against basic types such as VTP_STRING.)
			if (type.m_byRef)
				type.m_pointerLevel--;
			type.m_byRef = false;

			// Check pointers are valid
			if (!mData.IndexValid(sourceIndex) || !mData.IndexValid(destIndex)
					|| sourceIndex == 0 || destIndex == 0) {
				SetError(ERR_UNSET_POINTER);
				return false;
			}

			// Calculate element size
			int size = 1;
			if (type.m_basicType >= 0)
				size = mDataTypes.Structures().get(type.m_basicType).m_dataSize;

			// If the data types are arrays, then their sizes could be different.
			// If so, this is a run-time error.
			if (type.m_arrayLevel > 0) {
				int s = sourceIndex + (type.m_arrayLevel - 1) * 2, 
					d = destIndex + (type.m_arrayLevel - 1) * 2;
				for (int i = 0; i < type.m_arrayLevel; i++) {
					assert (mData.IndexValid(s));
					assert (mData.IndexValid(s + 1));
					assert (mData.IndexValid(d));
					assert (mData.IndexValid(d + 1));
					if (mData.Data().get(s).getIntVal() != mData.Data().get(d).getIntVal()) {
						SetError(ERR_ARRAY_SIZE_MISMATCH);
						return false;
					}

					// Update data size
					size *= mData.Data().get(s).getIntVal();
					size += 2;

					// Point to first element in array
					s -= 2;
					d -= 2;
				}
			}

			// If data type doesn't contain strings, can do a straight block copy
			if (!mDataTypes.ContainsString(type))
				BlockCopy(sourceIndex, destIndex, size);
			else
				CopyField(sourceIndex, destIndex, type);

			return true;
		}
		
		boolean CheckPointers(int index, ValType type, int destIndex) {

			// Check that pointers in data at "index" of type "type" can be stored
			// at
			// "destIndex" without any pointer scope errors.
			// (See CheckPointer for defn of "pointer scope error")
			assert (mData.IndexValid(index));
			assert (mDataTypes.TypeValid(type));

			// Type does not contain any pointers?
			if (!mDataTypes.ContainsPointer(type))
				return true;

			// Type is a pointer?
			if (type.m_pointerLevel > 0)
				return CheckPointer(destIndex, mData.Data().get(index).getIntVal());

			// Type is not a pointer, but contains one or more pointers.
			// Need to recursively break down object and check

			// Type is array?
			if (type.m_arrayLevel > 0) {

				// Find and check elements
				int elements = mData.Data().get(index).getIntVal();
				int elementSize = mData.Data().get(index + 1).getIntVal();
				int arrayStart = index + 2;

				// Calculate element type
				ValType elementType = type;
				elementType.m_arrayLevel--;

				// Check each element
				for (int i = 0; i < elements; i++)
					if (!CheckPointers(arrayStart + i * elementSize, elementType,
							destIndex))
						return false;

				return true;
			} else {

				// Must be a structure
				assert (type.m_basicType >= 0);

				// Find structure definition
				Structure s = mDataTypes.Structures().get(type.m_basicType);

				// Check each field in structure
				for (int i = 0; i < s.m_fieldCount; i++) {
					StructureField f = mDataTypes.Fields().get(
							s.m_firstField + i);
					if (!CheckPointers(index + f.m_dataOffset, f.m_type, destIndex))
						return false;
				}

				return true;
			}
		}
		
		boolean CheckPointer(int dest, int ptr) {

			// Check that pointer "ptr" can be stored at "dest" without a pointer
			// scope
			// error.
			// By "pointer scope error" we mean that a pointer of a longer lived
			// scope
			// is pointing to data of a shorter lived scope.
			// For example, a global pointer to a local variable.

			// We treat this as a runtime error to prevent system instability that
			// would
			// result if we allowed the program to continue to run.

			// Note that this approach is a bit of an experiment at the moment.
			assert (mData.IndexValid(dest));

			// Null pointer case
			if (ptr == 0)
				return true;

			// Check whether pointer points to temporary stack data
			if (ptr < mData.Permanent()) {

				// Such pointers can only be stored in variables in the current
				// stack frame.
				if (mUserCallStack.isEmpty()
						|| !(dest >= mData.StackTop() && dest < mUserCallStack.lastElement().prevStackTop))
					return false;
			}

			return true;
		}
		
		boolean PopArrayDimensions(ValType type) {
			assert (mDataTypes.TypeValid(type));
			assert (type.VirtualPointerLevel() == 0);

			// Pop and validate array indices from stack into type
			int i;
			VMValue v = new VMValue();
			for (i = 0; i < type.m_arrayLevel; i++) {
				v = mStack.Pop();
				int size = v.getIntVal() + 1;
				if (size < 1) {
					SetError(ERR_ZERO_LENGTH_ARRAY);
					return false;
				}
				type.m_arrayDims[i] = size;
			}

			return true;
		}
		
			boolean ValidateTypeSize(ValType type) {

			// Enforce variable size limitations.
			// This prevents rogue programs from trying to allocate unrealistic
			// amounts
			// of data.
			if (mDataTypes.DataSizeBiggerThan(type, mData.MaxDataSize())) {
				SetError(ERR_VARIABLE_TOO_BIG);
				return false;
			}

			if (!mData.RoomFor(mDataTypes.DataSize(type))) {
				SetError(ERR_OUT_OF_MEMORY);
				return false;
			}

			return true;
		}
		
		boolean ValidateTypeSizeForStack(ValType type) {
			// Enforce variable size limitations.
			// This prevents rogue programs from trying to allocate unrealistic
			// amounts
			// of data.
			if (mDataTypes.DataSizeBiggerThan(type, mData.MaxDataSize())) {
				SetError(ERR_VARIABLE_TOO_BIG);
				return false;
			}

			if (!mData.StackRoomFor(mDataTypes.DataSize(type))) {
				SetError(ERR_USER_FUNC_STACK_OVERFLOW);
				return false;
			}

			return true;
		}
		
		void PatchInBreakPt(int offset) {

			// Only patch if offset is valid and there is no breakpoint there
			// already.
			// Note: Don't patch in breakpoint to last instruction of program as
			// this is
			// always OpCode.OP_END anyway.
			if (offset < mCode.size() - 1 && mCode.get(offset).mOpCode != OpCode.OP_BREAKPT) {

				// Record previous op-code
				vmPatchedBreakPt bp = new vmPatchedBreakPt();
				bp.m_offset = offset;
				bp.m_replacedOpCode = mCode.get(offset).mOpCode;
				mPatchedBreakPts.add(bp);

				// Patch in breakpoint
				mCode.get(offset).mOpCode = OpCode.OP_BREAKPT;
			}
		}
		
		void InternalPatchOut() {

			// Patch out breakpoints and restore program to its no breakpoint state.
			for (vmPatchedBreakPt pt : mPatchedBreakPts)
				if (pt.m_offset < mCode.size())
					mCode.get(pt.m_offset).mOpCode = pt.m_replacedOpCode;
			mPatchedBreakPts.clear();
			m_breakPtsPatched = false;
		}
		
		void InternalPatchIn() {

			// Patch breakpoint instructions into the virtual machine code program.
			// This consists of swapping the virtual machine op-codes with
			// OpCode.OP_BREAKPT
			// codes.
			// We record the old op-code in the mPatchedBreakPts list, so we can
			// restore
			// the program when we've finished.

			// User breakpts
			for (int i = 0; i < mDebugger.UserBreakPtCount(); i++) {

				// Find line number
				int line = mDebugger.UserBreakPtLine(i);

				// Convert to offset
				int offset = 0;
				while (offset < mCode.size()
						&& mCode.get(offset).mSourceLine < line)
					offset++;

				// Patch in breakpt
				if (offset < mCode.size())
					PatchInBreakPt(offset);
			}

			// Patch in temp breakpts
			for (vmTempBreakPt pt : mTempBreakPts)
				PatchInBreakPt(pt.m_offset);

			m_breakPtsPatched = true;
		}
		
		vmTempBreakPt MakeTempBreakPt(int offset) {
			vmTempBreakPt breakPt = new vmTempBreakPt();
			breakPt.m_offset = offset;
			return breakPt;
		}
		
		int CalcBreakPtOffset(int line) {
			int offset = 0;
			while (offset < mCode.size() && mCode.get(offset).mSourceLine < line)
				offset++;
			// Is breakpoint line valid?
			if (offset < mCode.size() && mCode.get(offset).mSourceLine == line)
				return offset;
			else
				return 0xffff; // 0xffff means line invalid
				//TODO Value is meant to be unsigned; confirm this doesn't cause issues
		}
		
		public void AddStepBreakPts(boolean stepInto) 
		{
			// Add temporary breakpoints to catch execution after stepping over the
			// current line
			if (mIp >= mCode.size())
				return;
			PatchOut();

			// Calculate op-code range that corresponds to the current line.
			int line, startOffset, endOffset;
			startOffset = mIp;
			line = mCode.get(startOffset).mSourceLine;

			// Search for start of line
			while (startOffset > 0 && mCode.get(startOffset - 1).mSourceLine == line)
				startOffset--;

			// Search for start of next line
			endOffset = mIp + 1;
			while (endOffset < mCode.size() && mCode.get(endOffset).mSourceLine == line)
				endOffset++;

			// Create breakpoint on next line
			mTempBreakPts.add(MakeTempBreakPt(endOffset));

			// Scan for jumps, and place breakpoints at destination addresses
			for (int i = startOffset; i < endOffset; i++) {
				// TODO had to reduce dest from 0xffffffff since Java does not like unsigned values
				int dest = 0x7fffffff;
				switch (mCode.get(i).mOpCode) {
				case OpCode.OP_CALL:
					if (!stepInto) // If stepInto then fall through to JUMP
									// handling.
						break; // Otherwise break out, and no BP will be set.
				case OpCode.OP_JUMP:
				case OpCode.OP_JUMP_TRUE:
				case OpCode.OP_JUMP_FALSE:
					dest = mCode.get(i).mValue.getIntVal(); // Destination jump
																// address
					break;
				case OpCode.OP_RETURN:
				case OpCode.OP_RETURN_USER_FUNC:
					if (!mUserCallStack.isEmpty()) // Look at call stack and place
												// breakpoint on return
						dest = mUserCallStack.lastElement().returnAddr;
					break;
				case OpCode.OP_CREATE_USER_FRAME:
					if (stepInto)
						dest = mUserFunctions.get(mCode.get(i).mValue
								.getIntVal()).programOffset;
					break;
				default:
					break;
				}

				if (dest < mCode.size() // Destination valid?
						&& (dest < startOffset || dest >= endOffset)) // Destination outside line we are stepping over?
					mTempBreakPts.add(MakeTempBreakPt(dest));// Add breakpoint
			}
		}
		
		public boolean AddStepOutBreakPt() // Add breakpoint to step out of gosub
		{

			// Call stack must contain at least 1 return
			if (!mUserCallStack.isEmpty()) {
				int returnAddr = mUserCallStack.lastElement().returnAddr;
				if (returnAddr < mCode.size()) { // Validate it
					//Place breakpoint
					mTempBreakPts.add(MakeTempBreakPt(returnAddr));
					return true;
				}
			}
			return false;
		}
		
		public vmState GetState() {
			vmState s = new vmState();

			// Instruction pointer
			s.ip = mIp;

			// Registers
			s.reg = mReg;
			s.reg2 = mReg2;
			s.regString = mRegString;
			s.reg2String = mReg2String;

			// Stacks
			s.stackTop = mStack.Size();
			s.userFuncStackTop = mUserCallStack.size();
			s.currentUserFrame = mCurrentUserFrame;

			// Top of program
			s.codeSize = InstructionCount();
		    s.codeBlockCount    = mCodeBlocks.size();
		    
			// Variable data
			mData.SaveState(s.stackDataTop, s.tempDataLock);

			// Error state
			s.error = Error();
			if (s.error)
				s.errorString = GetError();
			else
				s.errorString = "";

			// Other state
			s.paused = m_paused;

			return s;
		}
		
		public void SetState(vmState state) {

			// Instruction pointer
			mIp = state.ip;

			// Registers
			mReg = state.reg;
			mReg2 = state.reg2;
			mRegString = state.regString;
			mReg2String = state.reg2String;

			// Stacks
			if (state.stackTop < mStack.Size())
				mStack.Resize(state.stackTop);
			if (state.userFuncStackTop < mUserCallStack.size())
				mUserCallStack.setSize(state.userFuncStackTop);
			mCurrentUserFrame = state.currentUserFrame;

			// Top of program
			if (state.codeSize < mCode.size())
				mCode.setSize(state.codeSize);
		    if (state.codeBlockCount < mCodeBlocks.size())
		        mCodeBlocks.setSize(state.codeBlockCount);

			// Variable data
			UnwindTemp();
			UnwindStack(state.stackDataTop);
			mData.RestoreState(state.stackDataTop, state.tempDataLock, true);

			// Error state
			if (state.error)
				SetError(state.errorString);
			else
				ClearError();

			// Other state
			m_paused = state.paused;
		}
		
			// Displaying data
		public String BasicValToString(VMValue val, int type,
				boolean constant) {
			switch (BasicValType.getType(type)) {
			case VTP_INT:
				return String.valueOf(val.getIntVal());
			case VTP_REAL:
				return String.valueOf(val.getRealVal());
			case VTP_STRING:
				if (constant) {
					if (val.getIntVal() >= 0
							&& val.getIntVal() < mStringConstants.size())
						return "\"" + mStringConstants.get(val.getIntVal()) + "\"";
					else
						return "???";
				} else
					return mStrings.IndexValid(val.getIntVal()) ? "\""
							+ mStrings.Value(val.getIntVal()) + "\""
							: "???";
			default:
				break;
			}
			return "???";
		}
		
		//TODO Move to utility class
		static String TrimToLength(String str, Mutable<Integer> length) {
			if (str.length() > length.get()) {
				length.set(0);
				return str.substring(0, length.get());
			} else {
				return str;
			}
		}
		
		void Deref(VMValue val, ValType type) {
			type.m_pointerLevel--;
			if (type.m_pointerLevel == 0 && !type.IsBasic()) {

				// Can't follow pointer, as type is not basic (and therefore cannot
				// be loaded into a register)
				// Use value by reference instead
				type.m_pointerLevel = 1;
				type.m_byRef = true;
			} else {

				// Follow pointer
				if (!mData.IndexValid(val.getIntVal())) // DEBUGGING!!!
					assert (mData.IndexValid(val.getIntVal()));
				val.setVal(mData.Data().get(val.getIntVal()));
			}
		}
		
		public String ValToString(VMValue val, ValType type, Mutable<Integer> maxChars) {
			assert (mDataTypes.TypeValid(type));
			assert (type.PhysicalPointerLevel() > 0 || type.IsBasic());

			if (maxChars.get().intValue() <= 0)
				return "";

			// Basic types
			if (type.IsBasic())
				return TrimToLength(BasicValToString(val, type.m_basicType, false),	maxChars);

			// Pointer types
			if (type.VirtualPointerLevel() > 0) {

				// Follow pointer down
				if (val.getIntVal() == 0)
					return TrimToLength("[UNSET POINTER]", maxChars);
				Deref(val, type);
				return TrimToLength("&", maxChars)
						+ ValToString(val, type, maxChars);
			}

			// Type is not basic, or a pointer. Must be a value by reference. Either
			// a structure or an array
			assert (type.m_pointerLevel == 1);
			assert (type.m_byRef);
			int dataIndex = val.getIntVal(); // Points to data
			if (dataIndex == 0)
				return TrimToLength("[UNSET]", maxChars);
			String result = "";

			// Arrays
			if (type.m_arrayLevel > 0) {
				assert (mData.IndexValid(dataIndex));
				assert (mData.IndexValid(dataIndex + 1));

				// Read array header
				int elements = mData.Data().get(dataIndex).getIntVal();
				int elementSize = mData.Data().get(dataIndex + 1).getIntVal();
				int arrayStart = dataIndex + 2;

				// Enumerate elements
				result = TrimToLength("{", maxChars);
				for (int i = 0; i < elements && maxChars.get() > 0; i++) {
					VMValue element = new VMValue(arrayStart + i * elementSize); // Address
																					// of
																					// element
					ValType elementType = type; // Element type.
					elementType.m_arrayLevel--; // Less one array level.
					elementType.m_pointerLevel = 1; // Currently have a pointer
					elementType.m_byRef = false;

					// Deref to reach data
					Deref(element, elementType);

					// Describe element
					result += ValToString(element, elementType, maxChars);
					if (i < elements - 1)
						result += TrimToLength(", ", maxChars);
				}
				result += TrimToLength("}", maxChars);
				return result;
			}

			// Structures
			if (type.m_basicType >= 0) {
				result = TrimToLength("{", maxChars);
				Structure structure = mDataTypes.Structures().get(type.m_basicType);
				for (int i = 0; i < structure.m_fieldCount && maxChars.get() > 0; i++) {
					StructureField field = mDataTypes.Fields().get(
							structure.m_firstField + i);
					VMValue fieldVal = new VMValue(dataIndex + field.m_dataOffset);
					ValType fieldType = field.m_type;
					fieldType.m_pointerLevel++;
					Deref(fieldVal, fieldType);
					result += TrimToLength(field.m_name + "=", maxChars)
							+ ValToString(fieldVal, fieldType, maxChars);
					if (i < structure.m_fieldCount - 1)
						result += TrimToLength(", ", maxChars);
				}
				result += TrimToLength("}", maxChars);
				return result;
			}

			return "???";
		}
		
			public String VarToString(vmVariable v, int maxChars) {
			return DataToString(v.m_dataIndex, v.m_type, maxChars);
		}

		public String DataToString(int dataIndex, ValType type, int maxChars) {
			VMValue val = new VMValue(dataIndex);
			type.m_pointerLevel++;
			Deref(val, type);
			return ValToString(val, type, new Mutable<Integer>(maxChars));
		}
		
		boolean ReadProgramData(byte basictype) {

			// Read program data into register.

			// Check for out-of-data.
			if (mProgramDataOffset >= mProgramData.size()) {
				SetError(ERR_OUT_OF_DATA);
				return false;
			}

			// Find program data
			VmProgramDataElement e = mProgramData.get(mProgramDataOffset++);

			// Convert to requested type
			switch (BasicValType.getType(basictype)) {
			case VTP_STRING:

				// Convert type to int.
				switch (e.getType()) {
				case VTP_STRING:
					assert (e.getValue().getIntVal() >= 0);
					assert (e.getValue().getIntVal() < mStringConstants.size());
					setRegString(mStringConstants.get(e.getValue().getIntVal()));
					return true;
				case VTP_INT:
					setRegString(String.valueOf(e.getValue().getIntVal()));
					return true;
				case VTP_REAL:
					setRegString(String.valueOf(e.getValue().getRealVal()));
					return true;
				default:
					break;
				}
				break;

			case VTP_INT:
				switch (e.getType()) {
				case VTP_STRING:
					SetError(ERR_DATA_IS_STRING);
					return false;
				case VTP_INT:
					Reg().setIntVal(e.getValue().getIntVal());
					return true;
				case VTP_REAL:
					Reg().setIntVal((int)e.getValue().getRealVal());
					return true;
				default:
					break;
				}
				break;

			case VTP_REAL:
				switch (e.getType()) {
				case VTP_STRING:
					SetError(ERR_DATA_IS_STRING);
					return false;
				case VTP_INT:
					Reg().setRealVal((float)e.getValue().getIntVal());
					return true;
				case VTP_REAL:
					Reg().setRealVal(e.getValue().getRealVal());
					return true;
				default:
					break;
				}
				break;
			default:
				break;
			}
			assert (false);
			return false;
		}
		
		int StoredDataSize(int sourceIndex, ValType type) {
			assert (!type.m_byRef);

			if (type.m_pointerLevel == 0 && type.m_arrayLevel > 0) {
				// Calculate actual array size
				// Array is prefixed by element count and element size.
				return mData.Data().get(sourceIndex).getIntVal()
						* mData.Data().get(sourceIndex + 1).getIntVal() + 2;
			} else
				return mDataTypes.DataSize(type);
		}
		
			boolean CopyToParam(int sourceIndex, ValType type) {

			// Check source index is valid
			if (!mData.IndexValid(sourceIndex) || sourceIndex == 0) {
				SetError(ERR_UNSET_POINTER);
				return false;
			}

			// Calculate data size.
			// Note that the "type" does not specify array dimensions (if type is an
			// array),
			// so they must be read from the source array.
			int size = StoredDataSize(sourceIndex, type);

			// Allocate data for parameter on stack
			if (!mData.StackRoomFor(size)) {
				SetError(ERR_USER_FUNC_STACK_OVERFLOW);
				return false;
			}
			int dataIndex = mData.AllocateStack(size);

			// Block copy the data
			BlockCopy(sourceIndex, dataIndex, size);

			// Duplicate any contained strings
			if (mDataTypes.ContainsString(type))
				DuplicateStrings(dataIndex, type);

			// Store pointer in register
			Reg().setIntVal(dataIndex);

			return true;
		}
		
		void DuplicateStrings(int dataIndex, ValType type) {

			// Called after data is block copied.
			// Strings are stored as pointers to string objects. After a block copy,
			// the source and destination blocks end up pointing to the same string
			// objects.
			// This method traverses the destination block, creates duplicate copies
			// of
			// any contained strings and fixes up the pointers to point to these new
			// string objects.
			assert (mDataTypes.ContainsString(type));
			assert (!type.m_byRef);
			assert (type.m_pointerLevel == 0);

			// Type IS string case
			if (type.Equals(BasicValType.VTP_STRING)) {

				VMValue val = mData.Data().get(dataIndex);
				// Empty strings (index 0) can be ignored
				if (val.getIntVal() != 0) {

					// Allocate new string
					int newStringIndex = mStrings.Alloc();

					// Copy previous string
					mStrings.setValue(newStringIndex,
							mStrings.Value(val.getIntVal()));

					// Point to new string
					val.setIntVal(newStringIndex);
				}
			}

			// Array case
			else if (type.m_arrayLevel > 0) {

				// Read array header
				int elements = mData.Data().get(dataIndex).getIntVal();
				int elementSize = mData.Data().get(dataIndex + 1).getIntVal();
				int arrayStart = dataIndex + 2;

				// Calculate element type
				ValType elementType = type;
				elementType.m_arrayLevel--;

				// Duplicate strings in each array element
				for (int i = 0; i < elements; i++)
					DuplicateStrings(arrayStart + i * elementSize, elementType);
			}

			// Otherwise must be a structure
			else {
				assert (type.m_basicType >= 0);

				// Find structure definition
				Structure s = mDataTypes.Structures().get(type.m_basicType);

				// Duplicate strings for each field in structure
				for (int i = 0; i < s.m_fieldCount; i++) {
					StructureField f = mDataTypes.Fields().get(s.m_firstField + i);
					if (mDataTypes.ContainsString(f.m_type))
						DuplicateStrings(dataIndex + f.m_dataOffset, f.m_type);
				}
			}
		}
		
		boolean MoveToTemp(int sourceIndex, ValType type) {

			// Free temp data
			// Note: We can do this because we know that the data isn't really
			// freed,
			// just marked as free. This is significant, because the data we are
			// moving
			// into the temp data area may be in temp-data already.

			// Special handling is required if data being copied is already in the
			// temp region
			boolean sourceIsTemp = sourceIndex > 0
					&& sourceIndex < mData.TempData();

			// Destroy temp data.
			// However, if the data being copied is in temp data, we use a
			// protected-stack-range to prevent it being destroyed.
			if (sourceIsTemp)
				UnwindTemp(new vmProtectedStackRange(sourceIndex, sourceIndex
						+ StoredDataSize(sourceIndex, type)));
			else
				UnwindTemp();

			// Free the data
			mData.FreeTemp();

			// Calculate data size.
			// Note that the "type" does not specify array dimensions (if type is an
			// array),
			// so they must be read from the source array.
			int size = StoredDataSize(sourceIndex, type);

			// Allocate data for parameter on stack
			if (!mData.StackRoomFor(size)) {
				SetError(ERR_USER_FUNC_STACK_OVERFLOW);
				return false;
			}
			int dataIndex = mData.AllocateTemp(size, false);

			// Block copy the data
			BlockCopy(sourceIndex, dataIndex, size);

			// Extra logic required to manage strings
			if (mDataTypes.ContainsString(type)) {

				// If source was NOT temp data, then the object has been copied,
				// rather
				// than moved, and we must duplicate all the contained string
				// objects.
				if (!sourceIsTemp)
					DuplicateStrings(dataIndex, type);
			}

			// Store pointer in register
			Reg().setIntVal(dataIndex);

			return true;
		}

		void UnwindTemp() {
			UnwindTemp(new vmProtectedStackRange());
		}

		void UnwindTemp(vmProtectedStackRange protect) {
			int newTop = mData.TempDataLock();

			// Run destrution logic over data that is about to be deallocated.
			while (!mTempDestructors.isEmpty()
					&& mTempDestructors.lastElement().addr >= newTop) {
				DestroyData(mTempDestructors.lastElement(), protect);
				mTempDestructors.remove(mTempDestructors.size() - 1);
			}

			// Note: We don't actually remove the data from the stack. Calling code
			// must
			// handle that instead.
		}

		void UnwindStack(int newTop) {

			// Run destruction logic over data that is about to be deallocated.
			while (!mStackDestructors.isEmpty()
					&& mStackDestructors.lastElement().addr < newTop) {
				DestroyData(mStackDestructors.lastElement(),
						new vmProtectedStackRange());
				mStackDestructors.remove(mStackDestructors.size() - 1);
			}

			// Note: We don't actually remove the data from the stack. Calling code
			// must
			// handle that instead.
		}

		void DestroyData(vmStackDestructor d, vmProtectedStackRange protect) {
			// Apply destructor logic to data block.
			DestroyData(d.addr, mTypeSet.GetValType(d.dataTypeIndex), protect);
		}

		void DestroyData(int index, ValType type, vmProtectedStackRange protect) {
			assert (mDataTypes.ContainsString(type));
			assert (!type.m_byRef);

			// Note: Current "destruction" logic involves deallocating strings
			// stored
			// in the data. (But could later be extended to something more general
			// purpose.)
			if (type.Equals(BasicValType.VTP_STRING)) {

				// Don't destroy if in protected range
				if (protect.ContainsAddr(index))
					return;

				// Type IS string case

				// Deallocate the string (if allocated)
				int stringIndex = mData.Data().get(index).getIntVal();
				if (stringIndex != 0)
					mStrings.Free(stringIndex);
			} else if (type.m_arrayLevel > 0) {

				// Array case
				ValType elementType = type;
				elementType.m_arrayLevel--;
				int count = mData.Data().get(index).getIntVal();
				int elementSize = mData.Data().get(index + 1).getIntVal();
				int arrayStart = index + 2;

				// Don't destroy if in protected range
				if (protect.ContainsRange(index, arrayStart + count * elementSize))
					return;

				// Recursively destroy each element
				for (int i = 0; i < count; i++)
					DestroyData(arrayStart + i * elementSize, elementType, protect);
			} else {

				// At this point we know the type contains a string and is not a
				// string
				// or array.
				// Can only be a structure.
				assert (type.m_pointerLevel == 0);
				assert (type.m_basicType >= 0);

				// Recursively destroy each structure field (that contains a string)
				Structure s = mDataTypes.Structures().get(type.m_basicType);

				// Don't destroy if in protected range
				if (protect.ContainsRange(index, index + s.m_dataSize))
					return;

				for (int i = 0; i < s.m_fieldCount; i++) {

					// Get field info
					StructureField f = mDataTypes.Fields().get(
							s.m_firstField + i);

					// Destroy if contains string(s)
					if (mDataTypes.ContainsString(f.m_type))
						DestroyData(index + f.m_dataOffset, f.m_type, protect);
				}
			}
		}
		
		public int NewCodeBlock() {
	    mCodeBlocks.add(new vmCodeBlock());

	    // Set pointer to code
	    CurrentCodeBlock().programOffset = mCode.size();

	    // Bind code block
	    mBoundCodeBlock = mCodeBlocks.size() - 1;

	    // Return index of new code block
	    return mBoundCodeBlock;
	}

	public vmCodeBlock CurrentCodeBlock() {
	    assert(!mCodeBlocks.isEmpty());
	    return mCodeBlocks.lastElement();
	}

	int CurrentCodeBlockIndex() {
	    assert(!mCodeBlocks.isEmpty());
	    return mCodeBlocks.size() - 1;
	}

	boolean IsCodeBlockValid(int index) {
	    return index >= 0 && index < mCodeBlocks.size();
	}

	int GetCodeBlockOffset(int index) {
	    assert(IsCodeBlockValid(index));
	    return mCodeBlocks.get(index).programOffset;
	}

	public vmRollbackPoint GetRollbackPoint() {
	    vmRollbackPoint r = new vmRollbackPoint();

	    r.codeBlockCount            = mCodeBlocks.size();
	    r.boundCodeBlock            = mBoundCodeBlock;
	    r.functionPrototypeCount    = mUserFunctionPrototypes.size();
	    r.functionCount             = mUserFunctions.size();
	    r.dataCount                 = mProgramData.size();
	    r.instructionCount          = mCode.size();

	    return r;
	}

	public void Rollback(vmRollbackPoint rollbackPoint) {

	    // Rollback virtual machine
	    mCodeBlocks.setSize(rollbackPoint.codeBlockCount);
	    mBoundCodeBlock = rollbackPoint.boundCodeBlock;
	    mUserFunctionPrototypes.setSize(rollbackPoint.functionPrototypeCount);
	    mUserFunctions.setSize(rollbackPoint.functionCount);
	    mProgramData.setSize(rollbackPoint.dataCount);
	    mCode.setSize(rollbackPoint.instructionCount);
	}

	// Streaming
		public void StreamOut(ByteBuffer buffer) {
			int i;
			try {
				// Stream header
				Streaming.WriteString(buffer, STREAM_HEADER);
				Streaming.WriteLong(buffer, STREAM_VERSION);

				// Plugin DLLs
				//TODO Reimplement libraries
				//m_plugins.StreamOut(buffer);

				// Variables
				mVariables.StreamOut(buffer); // Note: mVariables automatically
												// streams out mDataTypes

				// String constants
				Streaming.WriteLong(buffer, mStringConstants.size());
				for (i = 0; i < mStringConstants.size(); i++)
					Streaming.WriteString(buffer, mStringConstants.get(i));

				// Data type lookup table
				mTypeSet.StreamOut(buffer);

				// Program code
				Streaming.WriteLong(buffer, mCode.size());
				for (i = 0; i < mCode.size(); i++)
					mCode.get(i).StreamOut(buffer);

				// Program data (for "DATA" statements)
				Streaming.WriteLong(buffer, mProgramData.size());
				for (i = 0; i < mProgramData.size(); i++)
					mProgramData.get(i).StreamOut(buffer);

			    // User function prototypes
				Streaming.WriteLong(buffer, mUserFunctionPrototypes.size());
			    for (i = 0; i < mUserFunctionPrototypes.size(); i++)
			        mUserFunctionPrototypes.get(i).StreamOut(buffer);

			    // User functions
			    Streaming.WriteLong(buffer, mUserFunctions.size());
			    for (i = 0; i < mUserFunctions.size(); i++)
			        mUserFunctions.get(i).StreamOut(buffer);

			    // Code blocks
			    Streaming.WriteLong(buffer, mCodeBlocks.size());
			    for (i = 0; i < mCodeBlocks.size(); i++)
			        mCodeBlocks.get(i).StreamOut(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public boolean StreamIn(ByteBuffer buffer) {
			try {
				// Read and validate stream header
			    if (!Streaming.ReadString(buffer).equals(STREAM_HEADER))
			        return false;
			    if (Streaming.ReadLong(buffer) != STREAM_VERSION)
			        return false;

			    // Plugin DLLs
			    //TODO Reimplement libraries
			    /*
			    if (!m_plugins.StreamIn(buffer)) {
			        SetError(m_plugins.Error());
			        return false;
			    }

			    // Register plugin structures and functions in VM
			    m_plugins.StructureManager().AddVMStructures(DataTypes());
			    m_plugins.CreateVMFunctionSpecs();
			    */
			    		    
			    // Variables
			    mVariables.StreamIn(buffer);

			    // String constants
			    int count, i;
			    count = (int)Streaming.ReadLong (buffer);
			    mStringConstants.setSize(count);
			    for (i = 0; i < count; i++)
			        mStringConstants.set(i, Streaming.ReadString (buffer));

			    // Data type lookup table
			    mTypeSet.StreamIn(buffer);

			    // Program code
			    count = (int)Streaming.ReadLong(buffer);
			    mCode.setSize(count);
			    for (i = 0; i < count; i++)
			        mCode.get(i).StreamIn(buffer);

			    // Program data (for "DATA" statements)
			    count = (int)Streaming.ReadLong(buffer);
			    mProgramData.setSize(count);
			    for (i = 0; i < count; i++)
			        mProgramData.get(i).StreamIn(buffer);

			    // User function prototypes
			    count = (int)Streaming.ReadLong(buffer);
			    mUserFunctionPrototypes.setSize(count);
			    for (i = 0; i < count; i++)
			        mUserFunctionPrototypes.get(i).StreamIn(buffer);

			    // User functions
			    count = (int)Streaming.ReadLong(buffer);
			    mUserFunctions.setSize(count);
			    for (i = 0; i < count; i++)
			        mUserFunctions.get(i).StreamIn(buffer);

			    // Code blocks
			    count = (int)Streaming.ReadLong(buffer);
			    mCodeBlocks.setSize(count);
			    for (i = 0; i < count; i++)
			        mCodeBlocks.get(i).StreamIn(buffer);
			    
			    return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		
			void PatchOut() {
			if (m_breakPtsPatched)
				InternalPatchOut();
		}
		
		// General
		public boolean Done() {
			assert (IPValid());
			return mCode.get(mIp).mOpCode == OpCode.OP_END; // Reached end of
																	// program?
		}
		
		public void GetIPInSourceCode(Integer line, Integer col) {
			assert (IPValid());
			line = mCode.get(mIp).mSourceLine;
			col = (int) mCode.get(mIp).mSourceChar;
		}
		
		public void BindCodeBlock(int index) { mBoundCodeBlock = index; }
	    public int GetBoundCodeBlock() { return mBoundCodeBlock; }
		
		
		// IP and registers
		public int IP() {
			return mIp;
		}

		public VMValue Reg() {
			return mReg;
		}

		public VMValue Reg2() {
			return mReg2;
		}

		public void setReg(VMValue value) {
			mReg.setVal(value);
		}

		public void setReg2(VMValue value) {
			mReg2.setVal(value);
		}

		public String RegString() {
			return mRegString;
		}

		public String Reg2String() {
			return mReg2String;
		}

		public void setRegString(String string) {
			mRegString = string;
		}

		public void setReg2String(String string) {
			mReg2String = string;
		}

		public VmValueStack Stack() {
			return mStack;
		}
		
		public void setStack(VmValueStack stack) {
			mStack = stack;
		}
		
		
		// Variables, data and data types
		public TypeLibrary DataTypes() {
			return mDataTypes;
		}

		public vmData Data() {
			return mData;
		}

		public vmVariables Variables() {
			return mVariables;
		}

		public Vector<VmProgramDataElement> ProgramData() {
			return mProgramData;
		}
		
		// User functions
		public Vector<vmUserFuncPrototype> UserFunctionPrototypes() {
			return mUserFunctionPrototypes;
		}

		public Vector<vmUserFunc> UserFunctions() {
			return mUserFunctions;
		}

		public Vector<vmUserFuncStackFrame> UserCallStack() {
			return mUserCallStack;
		}

		public int CurrentUserFrame() {
			return mCurrentUserFrame;
		}
		
		// Debugging
		public boolean Paused() {
			return m_paused;
		}

		public void Pause() {
			m_paused = true;
		}

		public boolean BreakPtsPatched() {
			return m_breakPtsPatched;
		}
		
		
		public void ClearTempBreakPts() {
			PatchOut();
			mTempBreakPts.clear();
		}

		public void PatchIn() {
			if (!m_breakPtsPatched)
				InternalPatchIn();
		}

		public void RepatchBreakpts() {
			PatchOut();
			PatchIn();
		}
		
		public void GotoInstruction(int offset) {
			assert (OffsetValid(offset));
			mIp = offset;
		}

		public boolean SkipInstruction() { // USE WITH CARE!!!
			if (mIp < InstructionCount() + 1) {
				mIp++; // Proceed to next instruction
				return true;
			} else
				return false;
		}
		
		public boolean OffsetValid(int offset) {
			return offset >= 0 && offset < InstructionCount();
		}

		public boolean IPValid() {
			return OffsetValid(mIp);
		}
		
		// Building raw VM instructions
		public int InstructionCount() {
			return mCode.size();
		}

		public void AddInstruction(vmInstruction i) {
			PatchOut();
			mCode.add(i);
		}

		public void RollbackProgram(int size) {
			assert (size >= 0);
			assert (size <= InstructionCount());
			while (size < InstructionCount())
				mCode.remove(mCode.size() - 1);
		}
		
		public vmInstruction Instruction(int index) {
			assert (index < mCode.size());
			PatchOut();
			return mCode.get(index);
		}

		public void RemoveLastInstruction() {
			mCode.remove(mCode.size() - 1);
		}

		public int StoreType(ValType type) {
			return mTypeSet.GetIndex(type);
		}

		public ValType GetStoredType(int index) {
			return mTypeSet.GetValType(index);
		}

		// Program data
		public void StoreProgramData(BasicValType t, VMValue v) {
			VmProgramDataElement d = new VmProgramDataElement();
			d.setType(t);
			d.setValue(v);
			mProgramData.add(d);
		}

		public Vector<String> StringConstants() {
			return mStringConstants;
		}

		// External functions
		public int AddFunction(Function func) {
			int result = FunctionCount();
			mFunctions.add(func);
			return result;
		}

		public int FunctionCount() {
			return mFunctions.size();
		}

		public int AddOperatorFunction(Function func) {
			int result = OperatorFunctionCount();
			mOperatorFunctions.add(func);
			return result;
		}

		public int OperatorFunctionCount() {
			return mOperatorFunctions.size();
		}
		
		// Called by external functions
		public VMValue GetParam(int index) {
			// Read param from param stack.
			// Index 1 is TOS
			// Index 2 is TOS - 1
			// ...
			assert (index > 0);
			assert (index <= mStack.Size());
			return mStack.get(mStack.Size() - index);
		}

		public Integer GetIntParam(int index) {
			return GetParam(index).getIntVal();
		}

		public Float GetRealParam(int index) {
			return GetParam(index).getRealVal();
		}

		public String GetStringParam(int index) {
			return mStrings.Value(GetIntParam(index));
		}
		public void setStringParam(int index, String string) {
			mStrings.setValue(GetIntParam(index), string);
		}
		public String GetString(int index) {
			return mStrings.Value(index);
		}

		public void setString(int index, String string) {
			mStrings.setValue(index, string);
		}

		public int AllocString() {
			return mStrings.Alloc();
		}

		public int StoredStrings() {
			return mStrings.StoredElements();
		}

		public VmStore<String> Strings() {
			return mStrings;
		}
		
		// Reference params (called by external functions)
		public boolean CheckNullRefParam(int index) {

			// Return true if param is not a null reference
			// Otherwise return false and set a virtual machine error
			boolean result = GetIntParam(index) > 0;
			if (!result)
				FunctionError("Unset pointer");
			return result;
		}

		public VMValue GetRefParam(int index) {

			// Get reference parameter.
			// Returns a reference to the actual VMValue object
			int ptr = GetIntParam(index);
			assert (ptr > 0);
			assert (mData.IndexValid(ptr));
			return mData.Data().get(ptr);
		}

		public void FunctionError(String name) {
			SetError((String) "Function error: " + name);
		}

		public void MiscError(String name) {
			SetError(name);
		}
		
		
		// Initialisation functions
		public void AddInitFunc(Function func) {
			mInitFunctions.add(func);
		}

		// Resources
		public void AddResources(Resources resources) {
			mResources.add(resources);
		}
		
		
	    // Plugin DLLs
		//TODO Reimplement libraries
	    /*PluginDLLManager& Plugins() { return m_plugins; }*/
		    // Builtin/plugin function callback support
	    boolean IsEndCallback() {
	        assert(IPValid());
	        return mCode.get(mIp).mOpCode == OpCode.OP_END_CALLBACK;        // Reached end callback opcode?
	    }
}
