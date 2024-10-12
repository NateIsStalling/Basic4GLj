package com.basic4gl.runtime;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//TODO Reimplement libraries
//import com.basic4gl.nate.plugins.PluginDLLFile;
//import com.basic4gl.nate.plugins.PluginDLLManager;
//import com.basic4gl.nate.plugins.TomVMDLLAdapter;
//import com.basic4gl.nate.plugins.util.IDLL_Basic4GL_Runtime;


import com.basic4gl.runtime.types.*;
import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import com.basic4gl.runtime.stackframe.*;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.IVMDebugger;
import com.basic4gl.runtime.util.Resources;
import com.basic4gl.runtime.VariableCollection.Variable;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Virtual machine for Basic4GL
 */
public class TomVM extends HasErrorState implements Streamable {
    // Constants

    public static final int VM_STEPS = 1000;
    // 10,000 user function stack calls
    public static final int MAX_USER_STACK_CALLS = 10000;
    // 100,000,000 variables (.4 gig of memory)
    public static final int MAX_DATA = 100000000;
    // First 250,000 (1 meg) reserved for stack/temp data space
    public static final int MAX_STACK = 250000;

    public static final int DATA_TO_STRING_MAX_CHARS = 800; // Any more gets annoying...
    public static final int ARRAY_MAX_DIMENSIONS = 10; // Maximum dimensions in an array

    public static final String STREAM_HEADER = "Basic4GL stream";
    public static final int STREAM_VERSION = 3;

    // Error Messages
    public static final String ERR_NOT_IMPLEMENTED = "Opcode not implemented",
            ERR_INVALID = "Invalid opcode",
            ERR_UNDIMMED_VARIABLE = "UnDIMmed variable",
            ERR_BAD_ARRAY_INDEX = "Array index out of range",
            ERR_REDIMMED_VARIABLE = "ReDIMmed variable",
            ERR_BAD_DECLARATION = "Var declaration error",
            ERR_VARIABLE_TOO_BIG = "Var is too big",
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
    Value mReg, // Register values (when int or float)
            mReg2;
    String mRegString, // Register values when string
            mReg2String;
    int mCurrentUserFrame;
    // The current active frame.
    // Note that this is often but NOT ALWAYS the top of the stack.
    // (When evaluating parameters for a impending function call, the pending
    // function stack frame is the top of the stack, but is not yet active).

    // Runtime stacks
    private ValueStack mStack; // Used for expression evaluation
    private Vector<UserFuncStackFrame> mUserCallStack; // Call stack for user functions

    // Individual code blocks
    private Vector<CodeBlock> mCodeBlocks;
    private int mBoundCodeBlock;

    // Data destruction
    private Vector<StackDestructor> mStackDestructors;
    private Vector<StackDestructor> mTempDestructors;

    // Plugin DLLs
    //TODO Reimplement libraries
    //PluginDLLManager m_plugins;
    //IDLL_Basic4GL_Runtime m_pluginRuntime;

    // Debugger
    IVMDebugger mDebugger;

    // Variables, data and data types
    TypeLibrary mDataTypes;
    Data mData;
    VariableCollection mVariables;
    Vector<String> mStringConstants;  // Constant strings declared in program
    Store<String> mStrings;
    List<Resources> mResources;
    Vector<UserFuncPrototype> mUserFunctionPrototypes;
    Vector<UserFunc> mUserFunctions;

    public Vector<Function> mInitFunctions;    // Initialisation functions

    // Program data
    Vector<ProgramDataElement> mProgramData; // General purpose program data
    // (e.g declared with "DATA"
    // keyword in BASIC)
    int mProgramDataOffset;

    // Instructions
    //TODO make private
    public Vector<Instruction> mCode;
    ValTypeSet mTypeSet;

    /**
     * Instruction pointer
     */
    private int mIp;

    // Debugging
    ArrayList<PatchedBreakPt> mPatchedBreakPts; // Patched in breakpoints
    ArrayList<TempBreakPt> mTempBreakPts; // Temporary breakpoints, generated
    // for stepping over a line

    boolean m_paused, // Set to true when program hits a breakpoint. (Or can be
    // set by caller.)
    m_breakPtsPatched; // Set to true if breakpoints are patched and in
    // synchronisation with compiled code

    boolean mStopped;

    //TODO Reimplement libraries
    //public TomVM(PluginDLLManager plugins, IVMDebugger debugger) {
    public TomVM(IVMDebugger debugger) {
        this(debugger, MAX_DATA, MAX_STACK);
    }

    //TODO Reimplement libraries
    //public TomVM(PluginDLLManager plugins, IVMDebugger debugger,
    //		int maxDataSize, int maxStackSize) {
    public TomVM(IVMDebugger debugger,
                 int maxDataSize, int maxStackSize) {
        mDebugger = debugger;

        mData = new Data(maxDataSize, maxStackSize);
        mDataTypes = new TypeLibrary();
        mVariables = new VariableCollection(mData, mDataTypes);

        mReg = new Value();
        mReg2 = new Value();
        mResources = new ArrayList<Resources>();

        mStrings = new Store<String>("");
        mStack = new ValueStack(mStrings);
        mUserCallStack = new Vector<UserFuncStackFrame>();
        mStackDestructors = new Vector<StackDestructor>();
        mTempDestructors = new Vector<StackDestructor>();

        mProgramData = new Vector<ProgramDataElement>();
        mCodeBlocks = new Vector<CodeBlock>();
        mStringConstants = new Vector<String>();

        mTypeSet = new ValTypeSet();

        mCode = new Vector<Instruction>();
        mFunctions = new Vector<Function>();
        mOperatorFunctions = new Vector<>();
        mUserFunctions = new Vector<UserFunc>();
        mUserFunctionPrototypes = new Vector<UserFuncPrototype>();
        mPatchedBreakPts = new ArrayList<PatchedBreakPt>();
        mTempBreakPts = new ArrayList<TempBreakPt>();

        mInitFunctions = new Vector<Function>();
        //TODO Reimplement libraries
        //m_plugins = plugins;
        // Create plugin runtime
        //m_pluginRuntime = new TomVMDLLAdapter(this,
        //		m_plugins.StructureManager());

        clearProgram();
    }

    /**
     * New program
     */
    public void clearProgram()
    {
        // Clear variables, data and data types
        clearVariables();
        mVariables.clear();
        mDataTypes.clear();
        mProgramData.clear();
        mCodeBlocks.clear();

        // Clear string constants
        mStringConstants.clear();

        // Deallocate code
        mCode.clear();
        mTypeSet.clear();
        mUserFunctions.clear();
        mUserFunctionPrototypes.clear();
        mIp = 0;
        m_paused = false;

        // Clear breakpoints
        mPatchedBreakPts.clear();
        mTempBreakPts.clear();
        m_breakPtsPatched = false;
    }

    /**
     * Clear variables
     */
    public void clearVariables()
    {
        mVariables.deallocate(); // Deallocate variables
        mData.clear(); // Deallocate variable data
        mStrings.clear(); // Clear strings
        mStack.clear(); // Clear runtime stacks
        mUserCallStack.clear();
        mStackDestructors.clear();
        mTempDestructors.clear();
        mCurrentUserFrame = -1;
        mBoundCodeBlock = 0;

        // Clear resources
        clearResources();

        // Init registers
        mReg.setIntVal(0);
        mReg2.setIntVal(0);
        mRegString = "";
        mReg2String = "";
        mProgramDataOffset = 0;
    }

    public IVMDebugger getDebugger(){
        return mDebugger;
    }
    public void clearResources() {
        // Clear resources
        for (Resources res : mResources) {
            res.clear();
        }
    }

    public void resetVM() {

        // Clear error state
        clearError();

        // Deallocate variables
        clearVariables();

        // Call registered initialisation functions
        for (int i = 0; i < mInitFunctions.size(); i++) {
            mInitFunctions.get(i).run(this);
        }

        // Move to start of program
        mIp = 0;
        m_paused = false;
    }

    public void continueVM() {
        // Reduced from 0xffffffff since Java doesn't support unsigned ints
        continueVM(0x7fffffff);
    }

    public void continueVM(int steps) // Continue execution from last position
    {

        clearError();
        m_paused = false;

        Instruction instruction;
        int stepCount = 0;
        int tempI;

        // Virtual machine main loop
        step:
        while (true) {    //breaks on last line; taking advantage of loops having labels for continue statements to replicate GOTO

            if (mStopped) {
                return;
            }

            // Count steps
            if (++stepCount > steps) {
                return;
            }


            instruction = mCode.get(mIp);
            switch (instruction.opCode) {
                case OpCode.OP_NOP:
                    mIp++; // Proceed to next instruction
                    continue step;
                case OpCode.OP_END:
                    break;
                case OpCode.OP_LOAD_CONST:

                    // Load value
                    if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        assertTrue(instruction.value.getIntVal() >= 0);
                        assertTrue(instruction.value.getIntVal() < mStringConstants.size());
                        setRegString(mStringConstants.get(instruction.value
                                .getIntVal()));
                    } else {
                        setReg(new Value(instruction.value));
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_LOAD_VAR: {

                    // Load variable.
                    // Instruction contains index of variable.
                    assertTrue(mVariables.isIndexValid(instruction.value.getIntVal()));
                    Variable var = mVariables.getVariables().get(
                            instruction.value.getIntVal());
                    if (var.allocated()) {
                        // Load address of variable's data into register
                        getReg().setIntVal(var.dataIndex);
                        mIp++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNDIMMED_VARIABLE);
                    break;
                }

                case OpCode.OP_LOAD_LOCAL_VAR: {

                    // Find current stack frame
                    assertTrue(mCurrentUserFrame >= 0);
                    assertTrue(mCurrentUserFrame < mUserCallStack.size());
                    UserFuncStackFrame currentFrame = mUserCallStack
                            .get(mCurrentUserFrame);

                    // Find variable
                    int index = instruction.value.getIntVal();

                    // Instruction contains index of variable.
                    if (currentFrame.localVarDataOffsets.get(index) != 0) {
                        // Load address of variable's data into register
                        getReg().setIntVal(currentFrame.localVarDataOffsets.get(index));
                        mIp++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNDIMMED_VARIABLE);
                    break;
                }

                case OpCode.OP_DEREF: {

                    // Dereference reg.
                    if (getReg().getIntVal() != 0) {
                        assertTrue(mData.isIndexValid(getReg().getIntVal()));
                        // Find value that reg points to
                        Value val = mData.data().get(getReg().getIntVal());
                        switch (instruction.basicVarType) {
                            case BasicValType.VTP_INT:
                            case BasicValType.VTP_REAL:
                                setReg(val);
                                mIp++; // Proceed to next instruction
                                continue step;
                            case BasicValType.VTP_STRING:
                                assertTrue(mStrings.isIndexValid(val.getIntVal()));
                                setRegString(mStrings.getValueAt(val.getIntVal()));
                                mIp++; // Proceed to next instruction
                                continue step;
                            default:
                                break;
                        }
                        assertTrue(false);
                    }
                    setError(ERR_UNSET_POINTER);
                    break;
                }
                case OpCode.OP_ADD_CONST:
                    // Check pointer
                    if (getReg().getIntVal() != 0) {
                        getReg().setIntVal(getReg().getIntVal()
                                + instruction.value.getIntVal());
                        mIp++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNSET_POINTER);
                    break;

                case OpCode.OP_ARRAY_INDEX:

                    if (getReg2().getIntVal() != 0) {
                        // Input: mReg2 = Array address
                        // mReg = Array index
                        // Output: mReg = Element address
                        assertTrue(mData.isIndexValid(getReg2().getIntVal()));
                        assertTrue(mData.isIndexValid(getReg2().getIntVal() + 1));

                        // mReg2 points to array header (2 values)
                        // First value is highest element (i.e number of elements +
                        // 1)
                        // Second value is size of array element.
                        // Array data immediately follows header
                        if (getReg().getIntVal() >= 0
                                && getReg().getIntVal() < mData.data()
                                .get(getReg2().getIntVal()).getIntVal()) {
                            assertTrue(mData.data().get(getReg2().getIntVal() + 1)
                                    .getIntVal() >= 0);
                            getReg().setIntVal(getReg2().getIntVal()
                                    + 2
                                    + getReg().getIntVal()
                                    * mData.data().get(getReg2().getIntVal() + 1)
                                    .getIntVal());

                            mIp++; // Proceed to next instruction
                            continue step;
                        }
                        setError(ERR_BAD_ARRAY_INDEX);
                        break;
                    }
                    setError(ERR_UNSET_POINTER);
                    break;

                case OpCode.OP_PUSH:

                    // Push register to stack
                    if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        mStack.pushString(getRegString());
                    } else {
                        mStack.push(getReg());
                    }

                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_POP:

                    // Pop reg2 from stack
                    if (instruction.basicVarType == BasicValType.VTP_STRING) {

                        setReg2String(mStack.popString());
                    } else {
                        getReg2().setVal(mStack.pop());
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_SAVE: {

                    // Save reg into [reg2]
                    if (getReg2().getIntVal() > 0) {
                        assertTrue(mData.isIndexValid(getReg2().getIntVal()));
                        Value dest = mData.data().get(getReg2().getIntVal());
                        switch (instruction.basicVarType) {
                            case BasicValType.VTP_INT:
                            case BasicValType.VTP_REAL:
                                //mData.Data().set(mReg2.getIntVal(), new Value(mReg));
                                dest.setVal(getReg());

                                mIp++; // Proceed to next instruction
                                continue step;
                            case BasicValType.VTP_STRING:

                                // Allocate string space if necessary
                                if (dest.getIntVal() == 0) {
                                    dest.setIntVal(mStrings.alloc());
                                }

                                // Copy string value
                                mStrings.setValue(dest.getIntVal(), getRegString());
                                mIp++; // Proceed to next instruction
                                continue step;
                            default:
                                break;
                        }
                        assertTrue(false);
                    }
                    setError(ERR_UNSET_POINTER);
                    break;
                }

                case OpCode.OP_COPY: {

                    // Copy data
                    if (CopyData(getReg().getIntVal(), getReg2().getIntVal(),
                            mTypeSet.getValType(instruction.value.getIntVal()))) {
                        mIp++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }
                case OpCode.OP_DECLARE: {

                    // Allocate variable.
                    assertTrue(mVariables.isIndexValid(instruction.value.getIntVal()));
                    Variable var = mVariables.getVariables().get(instruction.value.getIntVal());

                    // Must not already be allocated
                    if (var.allocated()) {
                        setError(ERR_REDIMMED_VARIABLE);
                        break;
                    }

                    // Pop and validate array dimensions sizes into type (if
                    // applicable)
                    if (var.type.getPhysicalPointerLevel() == 0
                            && !PopArrayDimensions(var.type)) {
                        break;
                    }

                    // Validate type size
                    if (!ValidateTypeSize(var.type)) {
                        break;
                    }

                    // Allocate variable
                    var.allocate(mData, mDataTypes);

                    mIp++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_DECLARE_LOCAL: {

                    // Allocate local variable

                    // Find current stack frame
                    assertTrue(mCurrentUserFrame >= 0);
                    assertTrue(mCurrentUserFrame < mUserCallStack.size());
                    UserFuncStackFrame currentFrame = mUserCallStack.get(mCurrentUserFrame);
                    UserFunc userFunc = mUserFunctions.get(currentFrame.userFuncIndex);
                    UserFuncPrototype prototype = mUserFunctionPrototypes.get(userFunc.mPrototypeIndex);

                    // Find variable type
                    int index = instruction.value.getIntVal();
                    assertTrue(index >= 0);
                    assertTrue(index < prototype.localVarTypes.size());
                    ValType type = new ValType(prototype.localVarTypes.get(index));

                    // Must not already be allocated
                    if (currentFrame.localVarDataOffsets.get(index) != 0) {
                        setError(ERR_REDIMMED_VARIABLE);
                        break;
                    }

                    // Pop and validate array dimensions sizes into type (if
                    // applicable)
                    if (type.getPhysicalPointerLevel() == 0
                            && !PopArrayDimensions(type)) {
                        break;
                    }

                    // Validate type size
                    if (!ValidateTypeSizeForStack(type)) {
                        break;
                    }

                    // Allocate new data
                    int dataIndex = mData.allocateStack(mDataTypes.getDataSize(type));

                    // Initialise it
                    mData.initData(dataIndex, type, mDataTypes);

                    // Store data index in stack frame
                    currentFrame.localVarDataOffsets.set(index, dataIndex);

                    // Also store in register, so that OpCode.OP_REG_DESTRUCTOR can be used
                    getReg().setIntVal(dataIndex);

                    mIp++; // Proceed to next instruction
                    continue step;

                }
                case OpCode.OP_JUMP:

                    // Jump
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < mCode.size());
                    mIp = instruction.value.getIntVal();
                    continue step; // Proceed without incrementing instruction

                case OpCode.OP_JUMP_TRUE:

                    // Jump if reg != 0
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < mCode.size());
                    if (getReg().getIntVal() != 0) {
                        mIp = instruction.value.getIntVal();
                        continue step; // Proceed without incrementing instruction
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_JUMP_FALSE:

                    // Jump if reg == 0
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < mCode.size());
                    if (getReg().getIntVal() == 0) {
                        mIp = instruction.value.getIntVal();
                        continue step; // Proceed without incrementing instruction
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_NEG:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(-getReg().getIntVal());
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setRealVal(-getReg().getRealVal());
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_PLUS:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg().getIntVal() + getReg2().getIntVal());
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setRealVal(getReg().getRealVal() + getReg2().getRealVal());
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        setRegString(getReg2String() + getRegString());
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_MINUS:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg2().getIntVal() - getReg().getIntVal());
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setRealVal(getReg2().getRealVal() - getReg().getRealVal());
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_TIMES:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg().getIntVal() * getReg2().getIntVal());
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setRealVal(getReg().getRealVal() * getReg2().getRealVal());
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_DIV:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg2().getIntVal() / getReg().getIntVal());
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setRealVal(getReg2().getRealVal() / getReg().getRealVal());
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_MOD:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        int i = getReg2().getIntVal() % getReg().getIntVal();
                        if (i >= 0) {
                            getReg().setIntVal(i);
                        } else {
                            getReg().setIntVal(getReg().getIntVal() + i);
                        }
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_NOT:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg().getIntVal() == 0 ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_EQUAL:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(
                                getReg2().getIntVal() == getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(
                                getReg2().getRealVal() == getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal(getReg2String().equals(getRegString()) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_NOT_EQUAL:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(
                                getReg2().getIntVal() != getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(
                                getReg2().getRealVal() != getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal(!getReg2String().equals(getRegString()) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_GREATER:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(
                                getReg2().getIntVal() > getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(
                                getReg2().getRealVal() > getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal(
                                (getReg2String().compareTo(getRegString()) > 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_GREATER_EQUAL:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(
                                getReg2().getIntVal() >= getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(
                                getReg2().getRealVal() >= getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal(
                                (getReg2String().compareTo(getRegString()) >= 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_LESS:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(
                                getReg2().getIntVal() < getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(
                                getReg2().getRealVal() < getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal(
                                (getReg2String().compareTo(getRegString()) < 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_LESS_EQUAL:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(
                                getReg2().getIntVal() <= getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(
                                getReg2().getRealVal() <= getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal(
                                (getReg2String().compareTo(getRegString()) <= 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_REAL:
                    getReg().setRealVal((float) getReg().getIntVal());
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_REAL2:
                    getReg2().setRealVal((float) getReg2().getIntVal());
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_INT:
                    getReg().setIntVal((int) getReg().getRealVal());
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_INT2:
                    getReg2().setIntVal((int) getReg2().getRealVal());
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_STRING:
                    setRegString(String.valueOf(getReg().getIntVal()));
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_STRING:
                    setRegString(String.valueOf(getReg().getRealVal()));
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_STRING2:
                    setReg2String(String.valueOf(getReg2().getIntVal()));
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_STRING2:
                    setReg2String(String.valueOf(getReg2().getRealVal()));
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_AND:
                    getReg().setIntVal(getReg().getIntVal() & getReg2().getIntVal());
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_OR:
                    getReg().setIntVal(getReg().getIntVal() | getReg2().getIntVal());
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_XOR:
                    getReg().setIntVal(getReg().getIntVal() ^ getReg2().getIntVal());
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CALL_FUNC:

                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < mFunctions.size());

                    // Call external function
                    mFunctions.get(instruction.value.getIntVal()).run(this);

                    if (!hasError()) {
                        mIp++; // Proceed to next instruction
                        continue step;
                    }
                    break;

                case OpCode.OP_CALL_OPERATOR_FUNC:

                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < mOperatorFunctions.size());

                    // Call external function
                    mOperatorFunctions.get(instruction.value.getIntVal()).run(this);
                    if (!hasError()) {
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
                    mData.freeTempData();
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_ALLOC: {

                    // Extract type, and array dimensions
                    ValType type = new ValType(mTypeSet.getValType(instruction.value
                            .getIntVal()));
                    if (!PopArrayDimensions(type)) {
                        break;
                    }

                    // Validate type size
                    if (!ValidateTypeSize(type)) {
                        break;
                    }

                    // Allocate and initialise new data
                    getReg().setIntVal(mData.allocate(mDataTypes.getDataSize(type)));
                    mData.initData(getReg().getIntVal(), type, mDataTypes);

                    mIp++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_CALL: {

                    // Call
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < mCode.size());

                    // Check for stack overflow
                    if (mUserCallStack.size() >= MAX_USER_STACK_CALLS) {
                        setError(ERR_STACK_OVERFLOW);
                        break;
                    }

                    // Push stack frame, with return address
                    mUserCallStack.add(new UserFuncStackFrame());
                    UserFuncStackFrame stackFrame = mUserCallStack.lastElement();
                    stackFrame.InitForGosub(mIp + 1);

                    // Jump to subroutine
                    mIp = instruction.value.getIntVal();
                    continue step; // Proceed without incrementing instruction
                }
                case OpCode.OP_RETURN:

                    // Return from GOSUB

                    // Pop and validate return address
                    if (mUserCallStack.isEmpty()) {
                        setError(ERR_RETURN_WITHOUT_GOSUB);
                        break;
                    }
                    // -1 means GOSUB. Should be impossible to execute
                    // an OpCode.OP_RETURN if stack top is not a GOSUB
                    assertTrue(mUserCallStack.lastElement().userFuncIndex == -1);

                    tempI = mUserCallStack.lastElement().returnAddr;
                    mUserCallStack.remove(mUserCallStack.size() - 1);
                    if (tempI >= mCode.size()) {
                        setError(ERR_STACK_ERROR);
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
                    setError(ERR_DLL_NOT_IMPLEMENTED); //Remove line when libraries are implemented
                    if (!hasError()) {
                        mIp++; // Proceed to next instruction
                        continue step;
                    }
                    break;
                }

                case OpCode.OP_CREATE_USER_FRAME: {

                    // Check for stack overflow
                    if (mUserCallStack.size() >= MAX_USER_STACK_CALLS) {
                        setError(ERR_STACK_OVERFLOW);
                        break;
                    }

                    // Create and initialize stack frame
                    int funcIndex = instruction.value.getIntVal();
                    mUserCallStack.add(new UserFuncStackFrame());
                    UserFuncStackFrame stackFrame = mUserCallStack.lastElement();
                    stackFrame.InitForUserFunction(
                            mUserFunctionPrototypes.get(mUserFunctions.get(funcIndex).mPrototypeIndex),
                            funcIndex);

                    // Save previous stack frame data
                    Mutable<Integer> tempTop = new Mutable<>(0), tempLock = new Mutable<>(0);
                    mData.saveState(tempTop, tempLock);
                    stackFrame.prevStackTop = tempTop.get();
                    stackFrame.prevTempDataLock = tempLock.get();

                    mIp++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_CREATE_RUNTIME_FRAME: {
                    assertTrue(!mCodeBlocks.isEmpty());

                    // Find function index
                    int funcIndex = -1;

                    // Look for function in bound code block
                    int runtimeIndex = instruction.value.getIntVal();
                    if (mBoundCodeBlock > 0 && mBoundCodeBlock < mCodeBlocks.size()) {
                        CodeBlock codeBlock = mCodeBlocks.get(mBoundCodeBlock);
                        if (codeBlock.programOffset >= 0) {
                            funcIndex = codeBlock.getRuntimeFunction(runtimeIndex).functionIndex;
                        }
                    }

                    // If not found, look in main program
                    if (funcIndex < 0) {
                        funcIndex = mCodeBlocks.get(0).getRuntimeFunction(runtimeIndex).functionIndex;
                    }

                    // No function => Runtime error
                    if (funcIndex < 0) {
                        setError(ERR_NO_RUNTIME_FUNCTION);
                        break;
                    }

                    // From here on the logic is the same as OpCode.OP_CREATE_USER_FRAME
                    // Check for stack overflow
                    if (mUserCallStack.size() >= MAX_USER_STACK_CALLS) {
                        setError(ERR_STACK_OVERFLOW);
                        break;
                    }

                    // Create and initialize stack frame
                    mUserCallStack.add(new UserFuncStackFrame());
                    UserFuncStackFrame stackFrame = mUserCallStack.lastElement();
                    stackFrame.InitForUserFunction(
                            mUserFunctionPrototypes.get(mUserFunctions.get(funcIndex).mPrototypeIndex),
                            funcIndex);

                    // Save previous stack frame data
                    Mutable<Integer> tempTop = new Mutable<>(0), tempLock = new Mutable<>(0);
                    mData.saveState(tempTop, tempLock);
                    stackFrame.prevStackTop = tempTop.get();
                    stackFrame.prevTempDataLock = tempLock.get();

                    mIp++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_CALL_USER_FUNC: {

                    // Call user defined function
                    UserFuncStackFrame stackFrame = mUserCallStack.lastElement();
                    UserFunc userFunc = mUserFunctions
                            .get(stackFrame.userFuncIndex);

                    // Make active
                    stackFrame.prevCurrentFrame = mCurrentUserFrame;
                    mCurrentUserFrame = mUserCallStack.size() - 1;

                    // Call function
                    stackFrame.returnAddr = mIp + 1;
                    mIp = userFunc.mProgramOffset;
                    continue step; // Proceed without incrementing instruction
                }

                case OpCode.OP_RETURN_USER_FUNC: {
                    assertTrue(mUserCallStack.size() > 0);

                    // Find current stack frame
                    UserFuncStackFrame stackFrame = mUserCallStack.lastElement();
                    assertTrue(stackFrame.userFuncIndex >= 0);

                    // Restore previous stack frame data
                    boolean doFreeTempData = instruction.value.getIntVal() == 1;
                    if (doFreeTempData) {
                        UnwindTemp();
                    }
                    UnwindStack(stackFrame.prevStackTop);
                    mData.restoreState(stackFrame.prevStackTop,
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
                    setError(ERR_NO_VALUE_RETURNED);
                    break;
                case OpCode.OP_BINDCODE:
                    mBoundCodeBlock = getReg().getIntVal();
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_EXEC:

                    // Call runtime compiled code block.
                    // Call is like a GOSUB.
                    // RETURN will return back to the next op-code
                    if (mBoundCodeBlock > 0 && mBoundCodeBlock < mCodeBlocks.size()) {
                        CodeBlock codeBlock = mCodeBlocks.get(mBoundCodeBlock);
                        if (codeBlock.programOffset >= 0) {

                            // From here the code is the same as OpCode.OP_CALL
                            assertTrue(codeBlock.programOffset >= 0);
                            assertTrue(codeBlock.programOffset < mCode.size());

                            // Check for stack overflow
                            if (mUserCallStack.size() >= MAX_USER_STACK_CALLS) {
                                setError(ERR_STACK_OVERFLOW);
                                break;
                            }

                            // Push stack frame, with return address
                            mUserCallStack.add(new UserFuncStackFrame());
                            UserFuncStackFrame stackFrame = mUserCallStack.lastElement();
                            stackFrame.InitForGosub(mIp + 1);

                            // Jump to subroutine
                            mIp = codeBlock.programOffset;
                            continue step;
                        }
                    }

                    setError(ERR_INVALID_CODE_BLOCK);
                    break;

                case OpCode.OP_END_CALLBACK:
                    break;          // Timeshare break. Calling code will then detect this op-code has been reached

                case OpCode.OP_DATA_READ:

                    // Read program data into register
                    if (ReadProgramData(instruction.basicVarType)) {
                        mIp++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                case OpCode.OP_DATA_RESET:

                    mProgramDataOffset = instruction.value.getIntVal();
                    mIp++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_SAVE_PARAM: {

                    // Allocate parameter data
                    if (!mData.hasStackRoomFor(1)) {
                        setError(ERR_USER_FUNC_STACK_OVERFLOW);
                        break;
                    }
                    int dataIndex = mData.allocateStack(1);
                    int paramIndex = instruction.value.getIntVal();

                    // Initialize parameter
                    assertTrue(!mUserCallStack.isEmpty());
                    mUserCallStack.lastElement().localVarDataOffsets.set(
                            paramIndex, dataIndex);

                    // Transfer register value to parameter
                    Value dest = mData.data().get(dataIndex);
                    switch (instruction.basicVarType) {
                        case BasicValType.VTP_INT:
                        case BasicValType.VTP_REAL:
                            //TODO Confirm value is properly set
                            //TODO Check other "dest" variables
                            dest.setVal(getReg());
                            break;
                        case BasicValType.VTP_STRING:

                            // Allocate string space
                            dest.setIntVal(mStrings.alloc());

                            // Copy string value
                            mStrings.setValue(dest.getIntVal(), getRegString());
                            break;
                        default:
                            assertTrue(false);
                    }

                    // Save parameter offset in register (so that OpCode.OP_REG_DESTRUCTOR
                    // will work)
                    getReg().setIntVal(dataIndex);
                    mIp++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_COPY_USER_STACK: {

                    // Copy data pointed to by mReg into next stack frame
                    // parameter.
                    // Instruction value points to the parameter data type.
                    if (CopyToParam(getReg().getIntVal(),
                            mTypeSet.getValType(instruction.value.getIntVal()))) {
                        mIp++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }

                case OpCode.OP_MOVE_TEMP: {

                    if (MoveToTemp(getReg().getIntVal(),
                            mTypeSet.getValType(instruction.value.getIntVal()))) {
                        mIp++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }

                case OpCode.OP_CHECK_PTR: {

                    if (CheckPointer(getReg2().getIntVal(), getReg().getIntVal())) {
                        mIp++; // Proceed to next instruction
                        continue step;
                    } else {
                        setError(ERR_POINTER_SCOPE_ERROR);
                        break;
                    }
                }

                case OpCode.OP_CHECK_PTRS: {
                    if (CheckPointers(getReg().getIntVal(),
                            mTypeSet.getValType(instruction.value.getIntVal()),
                            getReg2().getIntVal())) {
                        mIp++; // Proceed to next instruction
                        continue step;
                    } else {
                        setError(ERR_POINTER_SCOPE_ERROR);
                        break;
                    }
                }

                case OpCode.OP_REG_DESTRUCTOR: {

                    // Register destructor for data pointed to by mReg.
                    int ptr = getReg().getIntVal();
                    assertTrue(ptr >= 0);
                    if (ptr == 0) {
                        // Do nothing
                    } else if (ptr < mData.TempData()) {

                        // Pointer into temp data found
                        assertTrue(mTempDestructors.isEmpty() || mTempDestructors
                                .lastElement().addr < ptr);
                        mTempDestructors.add(new StackDestructor(ptr,
                                instruction.value.getIntVal()));
                    } else if (ptr >= mData.StackTop() && ptr < mData.Permanent()) {

                        // Pointer into stack data found
                        assertTrue(mStackDestructors.isEmpty() || mStackDestructors
                                .lastElement().addr > ptr);
                        mStackDestructors.add(new StackDestructor(ptr,
                                instruction.value.getIntVal()));
                    }
                    mIp++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_SAVE_PARAM_PTR: {

                    // Save register pointer into param pointer
                    assertTrue(!mUserCallStack.isEmpty());
                    mUserCallStack.lastElement().localVarDataOffsets.set(
                            instruction.value.getIntVal(), getReg().getIntVal());

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
                    if (!mStack.isEmpty()) {
                        setError(ERR_RUN_CALLED_INSIDE_EXECUTE);
                    } else {
                        resetVM(); // Reset program
                    }
                    break; // Timeshare break

                case OpCode.OP_BREAKPT:
                    m_paused = true; // Pause program
                    break; // Timeshare break

                default:
                    setError(ERR_INVALID);
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
        assertTrue(mData.isIndexValid(sourceIndex));
        assertTrue(mData.isIndexValid(sourceIndex + size - 1));
        assertTrue(mData.isIndexValid(destIndex));
        assertTrue(mData.isIndexValid(destIndex + size - 1));
        for (int i = 0; i < size; i++) {
            mData.data().set(destIndex + i, mData.data().get(sourceIndex + i));
        }
    }

    void CopyStructure(int sourceIndex, int destIndex, ValType type) {
        assertTrue(mDataTypes.isTypeValid(type));
        assertTrue(type.getVirtualPointerLevel() == 0);
        assertTrue(type.arrayLevel == 0);
        assertTrue(type.basicType >= 0);

        // Find structure definition
        Structure s = mDataTypes.getStructures().get(type.basicType);

        // Copy fields in structure
        for (int i = 0; i < s.fieldCount; i++) {
            StructureField f = mDataTypes.getFields().get(s.firstFieldIndex + i);
            CopyField(sourceIndex + f.dataOffset, destIndex + f.dataOffset,
                    f.type);
        }
    }

    void CopyArray(int sourceIndex, int destIndex, ValType type) {
        assertTrue(mDataTypes.isTypeValid(type));
        assertTrue(type.getVirtualPointerLevel() == 0);
        assertTrue(type.arrayLevel > 0);
        assertTrue(mData.isIndexValid(sourceIndex));
        assertTrue(mData.isIndexValid(destIndex));
        assertTrue(mData.data().get(sourceIndex).getIntVal() == mData.data()
                .get(destIndex).getIntVal()); // Array sizes match
        assertTrue(mData.data().get(sourceIndex + 1).getIntVal() == mData.data()
                .get(destIndex + 1).getIntVal()); // Element sizes match

        // Find element type and size
        ValType elementType = new ValType(type);
        elementType.arrayLevel--;
        int elementSize = mData.data().get(sourceIndex + 1).getIntVal();

        // Copy elements
        for (int i = 0; i < mData.data().get(sourceIndex).getIntVal(); i++) {
            if (elementType.arrayLevel > 0) {
                CopyArray(sourceIndex + 2 + i * elementSize, destIndex + 2 + i
                        * elementSize, elementType);
            } else {
                CopyField(sourceIndex + 2 + i * elementSize, destIndex + 2 + i
                        * elementSize, elementType);
            }
        }
    }

    void CopyField(int sourceIndex, int destIndex, ValType type) {

        assertTrue(mDataTypes.isTypeValid(type));

        // If type is basic string, copy string value
        if (type.matchesType(BasicValType.VTP_STRING)) {
            Value src = mData.data().get(sourceIndex);
            Value dest = mData.data().get(destIndex);
            if (src.getIntVal() > 0 || dest.getIntVal() > 0) {

                // Allocate string space if necessary
                if (dest.getIntVal() == 0) {
                    dest.setIntVal(mStrings.alloc());
                }

                // Copy string value
                mStrings.setValue(dest.getIntVal(), mStrings.getValueAt(mData.data().get(sourceIndex).getIntVal()));
            }
        }

        // If type is basic, or pointer then just copy value
        else if (type.isBasicType() || type.getVirtualPointerLevel() > 0) {
            mData.data().set(destIndex, mData.data().get(sourceIndex));
        }

            // If contains no strings, can just block copy
        else if (!mDataTypes.containsString(type)) {
            BlockCopy(sourceIndex, destIndex, mDataTypes.getDataSize(type));
        }

            // Otherwise copy array or structure
        else if (type.arrayLevel > 0) {
            CopyArray(sourceIndex, destIndex, type);
        } else {
            CopyStructure(sourceIndex, destIndex, type);
        }
    }

    boolean CopyData(int sourceIndex, int destIndex, ValType type) {
        assertTrue(mDataTypes.isTypeValid(type));
        assertTrue(type.getVirtualPointerLevel() == 0);

        // If a referenced type (which it should always be), convert to regular
        // type.
        // (To facilitate comparisons against basic types such as VTP_STRING.)
        if (type.isByRef) {
            type.pointerLevel--;
        }
        type.isByRef = false;

        // Check pointers are valid
        if (!mData.isIndexValid(sourceIndex) || !mData.isIndexValid(destIndex)
                || sourceIndex == 0 || destIndex == 0) {
            setError(ERR_UNSET_POINTER);
            return false;
        }

        // Calculate element size
        int size = 1;
        if (type.basicType >= 0) {
            size = mDataTypes.getStructures().get(type.basicType).dataSize;
        }

        // If the data types are arrays, then their sizes could be different.
        // If so, this is a run-time error.
        if (type.arrayLevel > 0) {
            int s = sourceIndex + (type.arrayLevel - 1) * 2,
                    d = destIndex + (type.arrayLevel - 1) * 2;
            for (int i = 0; i < type.arrayLevel; i++) {
                assertTrue(mData.isIndexValid(s));
                assertTrue(mData.isIndexValid(s + 1));
                assertTrue(mData.isIndexValid(d));
                assertTrue(mData.isIndexValid(d + 1));
                if (mData.data().get(s).getIntVal() != mData.data().get(d).getIntVal()) {
                    setError(ERR_ARRAY_SIZE_MISMATCH);
                    return false;
                }

                // Update data size
                size *= mData.data().get(s).getIntVal();
                size += 2;

                // Point to first element in array
                s -= 2;
                d -= 2;
            }
        }

        // If data type doesn't contain strings, can do a straight block copy
        if (!mDataTypes.containsString(type)) {
            BlockCopy(sourceIndex, destIndex, size);
        } else {
            CopyField(sourceIndex, destIndex, type);
        }

        return true;
    }

    boolean CheckPointers(int index, ValType type, int destIndex) {

        // Check that pointers in data at "index" of type "type" can be stored
        // at
        // "destIndex" without any pointer scope errors.
        // (See CheckPointer for defn of "pointer scope error")
        assertTrue(mData.isIndexValid(index));
        assertTrue(mDataTypes.isTypeValid(type));

        // Type does not contain any pointers?
        if (!mDataTypes.containsPointer(type)) {
            return true;
        }

        // Type is a pointer?
        if (type.pointerLevel > 0) {
            return CheckPointer(destIndex, mData.data().get(index).getIntVal());
        }

        // Type is not a pointer, but contains one or more pointers.
        // Need to recursively break down object and check

        // Type is array?
        if (type.arrayLevel > 0) {

            // Find and check elements
            int elements = mData.data().get(index).getIntVal();
            int elementSize = mData.data().get(index + 1).getIntVal();
            int arrayStart = index + 2;

            // Calculate element type
            ValType elementType = new ValType(type);
            elementType.arrayLevel--;

            // Check each element
            for (int i = 0; i < elements; i++) {
                if (!CheckPointers(arrayStart + i * elementSize, elementType,
                        destIndex)) {
                    return false;
                }
            }

            return true;
        } else {

            // Must be a structure
            assertTrue(type.basicType >= 0);

            // Find structure definition
            Structure s = mDataTypes.getStructures().get(type.basicType);

            // Check each field in structure
            for (int i = 0; i < s.fieldCount; i++) {
                StructureField f = mDataTypes.getFields().get(
                        s.firstFieldIndex + i);
                if (!CheckPointers(index + f.dataOffset, f.type, destIndex)) {
                    return false;
                }
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
        assertTrue(mData.isIndexValid(dest));

        // Null pointer case
        if (ptr == 0) {
            return true;
        }

        // Check whether pointer points to temporary stack data
        if (ptr < mData.Permanent()) {

            // Such pointers can only be stored in variables in the current
            // stack frame.
            if (mUserCallStack.isEmpty()
                    || !(dest >= mData.StackTop() && dest < mUserCallStack.lastElement().prevStackTop)) {
                return false;
            }
        }

        return true;
    }

    boolean PopArrayDimensions(ValType type) {
        assertTrue(mDataTypes.isTypeValid(type));
        assertTrue(type.getVirtualPointerLevel() == 0);

        // Pop and validate array indices from stack into type
        int i;
        Value v = new Value();
        for (i = 0; i < type.arrayLevel; i++) {
            v = mStack.pop();
            int size = v.getIntVal() + 1;
            if (size < 1) {
                setError(ERR_ZERO_LENGTH_ARRAY);
                return false;
            }
            type.arrayDimensions[i] = size;
        }

        return true;
    }

    boolean ValidateTypeSize(ValType type) {

        // Enforce variable size limitations.
        // This prevents rogue programs from trying to allocate unrealistic
        // amounts
        // of data.
        if (mDataTypes.isDataSizeBiggerThan(type, mData.MaxDataSize())) {
            setError(ERR_VARIABLE_TOO_BIG);
            return false;
        }

        if (!mData.hasRoomFor(mDataTypes.getDataSize(type))) {
            setError(ERR_OUT_OF_MEMORY);
            return false;
        }

        return true;
    }

    boolean ValidateTypeSizeForStack(ValType type) {
        // Enforce variable size limitations.
        // This prevents rogue programs from trying to allocate unrealistic
        // amounts
        // of data.
        if (mDataTypes.isDataSizeBiggerThan(type, mData.MaxDataSize())) {
            setError(ERR_VARIABLE_TOO_BIG);
            return false;
        }

        if (!mData.hasStackRoomFor(mDataTypes.getDataSize(type))) {
            setError(ERR_USER_FUNC_STACK_OVERFLOW);
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
        if (offset < mCode.size() - 1 && mCode.get(offset).opCode != OpCode.OP_BREAKPT) {

            // Record previous op-code
            PatchedBreakPt bp = new PatchedBreakPt();
            bp.offset = offset;
            bp.replacedOpCode = mCode.get(offset).opCode;
            mPatchedBreakPts.add(bp);

            // Patch in breakpoint
            mCode.get(offset).opCode = OpCode.OP_BREAKPT;
        }
    }

    void internalPatchOut() {

        // Patch out breakpoints and restore program to its no breakpoint state.
        for (PatchedBreakPt pt : mPatchedBreakPts) {
            if (pt.offset < mCode.size()) {
                mCode.get(pt.offset).opCode = pt.replacedOpCode;
            }
        }
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

        System.out.println(mDebugger.getUserBreakPointCount());
        // User breakpts
        for (int i = 0; i < mDebugger.getUserBreakPointCount(); i++) {

            // Find line number
            int line = mDebugger.getUserBreakPointLine(i);

            // Convert to offset
            int offset = 0;
            while (offset < mCode.size()
                    && mCode.get(offset).sourceLine < line) {
                offset++;
            }

            // Patch in breakpt
            if (offset < mCode.size()) {
                PatchInBreakPt(offset);
            }
        }

        // Patch in temp breakpts
        for (TempBreakPt pt : mTempBreakPts) {
            PatchInBreakPt(pt.offset);
        }

        m_breakPtsPatched = true;
    }

    TempBreakPt MakeTempBreakPt(int offset) {
        TempBreakPt breakPt = new TempBreakPt();
        breakPt.offset = offset;
        return breakPt;
    }

    int CalcBreakPtOffset(int line) {
        int offset = 0;
        while (offset < mCode.size() && mCode.get(offset).sourceLine < line) {
            offset++;
        }
        // Is breakpoint line valid?
        if (offset < mCode.size() && mCode.get(offset).sourceLine == line) {
            return offset;
        } else {
            return 0xffff; // 0xffff means line invalid
        }
        //TODO Value is meant to be unsigned; confirm this doesn't cause issues
    }

    public void AddStepBreakPts(boolean stepInto) {
        // Add temporary breakpoints to catch execution after stepping over the
        // current line
        if (mIp >= mCode.size()) {
            return;
        }
        patchOut();

        // Calculate op-code range that corresponds to the current line.
        int line, startOffset, endOffset;
        startOffset = mIp;
        line = mCode.get(startOffset).sourceLine;

        // Search for start of line
        while (startOffset > 0 && mCode.get(startOffset - 1).sourceLine == line) {
            startOffset--;
        }

        // Search for start of next line
        endOffset = mIp + 1;
        while (endOffset < mCode.size() && mCode.get(endOffset).sourceLine == line) {
            endOffset++;
        }

        // Create breakpoint on next line
        mTempBreakPts.add(MakeTempBreakPt(endOffset));

        // Scan for jumps, and place breakpoints at destination addresses
        for (int i = startOffset; i < endOffset; i++) {
            // TODO had to reduce dest from 0xffffffff since Java does not like unsigned values
            int dest = 0x7fffffff;
            switch (mCode.get(i).opCode) {
                case OpCode.OP_CALL:
                    if (!stepInto) // If stepInto then fall through to JUMP
                        // handling.
                    {
                        break; // Otherwise break out, and no BP will be set.
                    }
                case OpCode.OP_JUMP:
                case OpCode.OP_JUMP_TRUE:
                case OpCode.OP_JUMP_FALSE:
                    dest = mCode.get(i).value.getIntVal(); // Destination jump
                    // address
                    break;
                case OpCode.OP_RETURN:
                case OpCode.OP_RETURN_USER_FUNC:
                    if (!mUserCallStack.isEmpty()) // Look at call stack and place
                        // breakpoint on return
                    {
                        dest = mUserCallStack.lastElement().returnAddr;
                    }
                    break;
                case OpCode.OP_CREATE_USER_FRAME:
                    if (stepInto) {
                        dest = mUserFunctions.get(mCode.get(i).value
                                .getIntVal()).mProgramOffset;
                    }
                    break;
                default:
                    break;
            }

            if (dest < mCode.size() // Destination valid?
                    && (dest < startOffset || dest >= endOffset)) // Destination outside line we are stepping over?
            {
                mTempBreakPts.add(MakeTempBreakPt(dest));// Add breakpoint
            }
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

    public VMState getState() {
        VMState s = new VMState();

        // Instruction pointer
        s.ip = mIp;

        // Registers
        s.reg = mReg;
        s.reg2 = mReg2;
        s.regString = mRegString;
        s.reg2String = mReg2String;

        // Stacks
        s.stackTop = mStack.size();
        s.userFuncStackTop = mUserCallStack.size();
        s.currentUserFrame = mCurrentUserFrame;

        // Top of program
        s.codeSize = getInstructionCount();
        s.codeBlockCount = mCodeBlocks.size();

        // Var data
        Mutable<Integer> tempTop = new Mutable<>(0), tempLock = new Mutable<>(0);
        mData.saveState(tempTop, tempLock);
        s.stackDataTop = tempTop.get();
        s.tempDataLock = tempLock.get();

        // Error state
        s.error = hasError();
        if (s.error) {
            s.errorString = getError();
        } else {
            s.errorString = "";
        }

        // Other state
        s.paused = m_paused;

        return s;
    }

    public void SetState(VMState state) {

        // Instruction pointer
        mIp = state.ip;

        // Registers
        mReg = state.reg;
        mReg2 = state.reg2;
        mRegString = state.regString;
        mReg2String = state.reg2String;

        // Stacks
        if (state.stackTop < mStack.size()) {
            mStack.resize(state.stackTop);
        }
        if (state.userFuncStackTop < mUserCallStack.size()) {
            mUserCallStack.setSize(state.userFuncStackTop);
        }
        mCurrentUserFrame = state.currentUserFrame;

        // Top of program
        if (state.codeSize < mCode.size()) {
            mCode.setSize(state.codeSize);
        }
        if (state.codeBlockCount < mCodeBlocks.size()) {
            mCodeBlocks.setSize(state.codeBlockCount);
        }

        // Var data
        UnwindTemp();
        UnwindStack(state.stackDataTop);
        mData.restoreState(state.stackDataTop, state.tempDataLock, true);

        // Error state
        if (state.error) {
            setError(state.errorString);
        } else {
            clearError();
        }

        // Other state
        m_paused = state.paused;
    }

    // Displaying data
    public String BasicValToString(Value val, int type,
                                   boolean constant) {
        switch (type) {
            case BasicValType.VTP_INT:
                return String.valueOf(val.getIntVal());
            case BasicValType.VTP_REAL:
                return String.valueOf(val.getRealVal());
            case BasicValType.VTP_STRING:
                if (constant) {
                    if (val.getIntVal() >= 0
                            && val.getIntVal() < mStringConstants.size()) {
                        return "\"" + mStringConstants.get(val.getIntVal()) + "\"";
                    } else {
                        return "???";
                    }
                } else {
                    return mStrings.isIndexValid(val.getIntVal()) ? "\""
                            + mStrings.getValueAt(val.getIntVal()) + "\""
                            : "???";
                }
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

    void Deref(Value val, ValType type) {
        type.pointerLevel--;
        if (type.pointerLevel == 0 && !type.isBasicType()) {

            // Can't follow pointer, as type is not basic (and therefore cannot
            // be loaded into a register)
            // Use value by reference instead
            type.pointerLevel = 1;
            type.isByRef = true;
        } else {

            // Follow pointer
            if (!mData.isIndexValid(val.getIntVal())) // DEBUGGING!!!
            {
                assertTrue(mData.isIndexValid(val.getIntVal()));
            }
            val.setVal(mData.data().get(val.getIntVal()));
        }
    }

    public String ValToString(Value val, ValType type, Mutable<Integer> maxChars) {
        assertTrue(mDataTypes.isTypeValid(type));
        assertTrue(type.getPhysicalPointerLevel() > 0 || type.isBasicType());

        if (maxChars.get().intValue() <= 0) {
            return "";
        }

        // Basic types
        if (type.isBasicType()) {
            return TrimToLength(BasicValToString(val, type.basicType, false), maxChars);
        }

        // Pointer types
        if (type.getVirtualPointerLevel() > 0) {

            // Follow pointer down
            if (val.getIntVal() == 0) {
                return TrimToLength("[UNSET POINTER]", maxChars);
            }
            Deref(val, type);
            return TrimToLength("&", maxChars)
                    + ValToString(val, type, maxChars);
        }

        // Type is not basic, or a pointer. Must be a value by reference. Either
        // a structure or an array
        assertTrue(type.pointerLevel == 1);
        assertTrue(type.isByRef);
        int dataIndex = val.getIntVal(); // Points to data
        if (dataIndex == 0) {
            return TrimToLength("[UNSET]", maxChars);
        }
        String result = "";

        // Arrays
        if (type.arrayLevel > 0) {
            assertTrue(mData.isIndexValid(dataIndex));
            assertTrue(mData.isIndexValid(dataIndex + 1));

            // Read array header
            int elements = mData.data().get(dataIndex).getIntVal();
            int elementSize = mData.data().get(dataIndex + 1).getIntVal();
            int arrayStart = dataIndex + 2;

            // Enumerate elements
            result = TrimToLength("{", maxChars);
            for (int i = 0; i < elements && maxChars.get() > 0; i++) {
                Value element = new Value(arrayStart + i * elementSize); // Address
                // of
                // element
                ValType elementType = new ValType(type); // Element type.
                elementType.arrayLevel--; // Less one array level.
                elementType.pointerLevel = 1; // Currently have a pointer
                elementType.isByRef = false;

                // Deref to reach data
                Deref(element, elementType);

                // Describe element
                result += ValToString(element, elementType, maxChars);
                if (i < elements - 1) {
                    result += TrimToLength(", ", maxChars);
                }
            }
            result += TrimToLength("}", maxChars);
            return result;
        }

        // Structures
        if (type.basicType >= 0) {
            result = TrimToLength("{", maxChars);
            Structure structure = mDataTypes.getStructures().get(type.basicType);
            for (int i = 0; i < structure.fieldCount && maxChars.get() > 0; i++) {
                StructureField field = mDataTypes.getFields().get(
                        structure.firstFieldIndex + i);
                Value fieldVal = new Value(dataIndex + field.dataOffset);
                ValType fieldType = new ValType(field.type);
                fieldType.pointerLevel++;
                Deref(fieldVal, fieldType);
                result += TrimToLength(field.name + "=", maxChars)
                        + ValToString(fieldVal, fieldType, maxChars);
                if (i < structure.fieldCount - 1) {
                    result += TrimToLength(", ", maxChars);
                }
            }
            result += TrimToLength("}", maxChars);
            return result;
        }

        return "???";
    }

    public String VarToString(Variable v, int maxChars) {
        return DataToString(v.dataIndex, v.type, maxChars);
    }

    public String DataToString(int dataIndex, ValType type, int maxChars) {
        Value val = new Value(dataIndex);
        type.pointerLevel++;
        Deref(val, type);
        return ValToString(val, type, new Mutable<Integer>(maxChars));
    }

    boolean ReadProgramData(int basictype) {

        // Read program data into register.

        // Check for out-of-data.
        if (mProgramDataOffset >= mProgramData.size()) {
            setError(ERR_OUT_OF_DATA);
            return false;
        }

        // Find program data
        ProgramDataElement e = mProgramData.get(mProgramDataOffset++);

        // Convert to requested type
        switch (basictype) {
            case BasicValType.VTP_STRING:

                // Convert type to int.
                switch (e.getType()) {
                    case BasicValType.VTP_STRING:
                        assertTrue(e.getValue().getIntVal() >= 0);
                        assertTrue(e.getValue().getIntVal() < mStringConstants.size());
                        setRegString(mStringConstants.get(e.getValue().getIntVal()));
                        return true;
                    case BasicValType.VTP_INT:
                        setRegString(String.valueOf(e.getValue().getIntVal()));
                        return true;
                    case BasicValType.VTP_REAL:
                        setRegString(String.valueOf(e.getValue().getRealVal()));
                        return true;
                    default:
                        break;
                }
                break;

            case BasicValType.VTP_INT:
                switch (e.getType()) {
                    case BasicValType.VTP_STRING:
                        setError(ERR_DATA_IS_STRING);
                        return false;
                    case BasicValType.VTP_INT:
                        getReg().setIntVal(e.getValue().getIntVal());
                        return true;
                    case BasicValType.VTP_REAL:
                        getReg().setIntVal((int) e.getValue().getRealVal());
                        return true;
                    default:
                        break;
                }
                break;

            case BasicValType.VTP_REAL:
                switch (e.getType()) {
                    case BasicValType.VTP_STRING:
                        setError(ERR_DATA_IS_STRING);
                        return false;
                    case BasicValType.VTP_INT:
                        getReg().setRealVal((float) e.getValue().getIntVal());
                        return true;
                    case BasicValType.VTP_REAL:
                        getReg().setRealVal(e.getValue().getRealVal());
                        return true;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        assertTrue(false);
        return false;
    }

    int StoredDataSize(int sourceIndex, ValType type) {
        assertTrue(!type.isByRef);

        if (type.pointerLevel == 0 && type.arrayLevel > 0) {
            // Calculate actual array size
            // Array is prefixed by element count and element size.
            return mData.data().get(sourceIndex).getIntVal()
                    * mData.data().get(sourceIndex + 1).getIntVal() + 2;
        } else {
            return mDataTypes.getDataSize(type);
        }
    }

    boolean CopyToParam(int sourceIndex, ValType type) {

        // Check source index is valid
        if (!mData.isIndexValid(sourceIndex) || sourceIndex == 0) {
            setError(ERR_UNSET_POINTER);
            return false;
        }

        // Calculate data size.
        // Note that the "type" does not specify array dimensions (if type is an
        // array),
        // so they must be read from the source array.
        int size = StoredDataSize(sourceIndex, type);

        // Allocate data for parameter on stack
        if (!mData.hasStackRoomFor(size)) {
            setError(ERR_USER_FUNC_STACK_OVERFLOW);
            return false;
        }
        int dataIndex = mData.allocateStack(size);

        // Block copy the data
        BlockCopy(sourceIndex, dataIndex, size);

        // Duplicate any contained strings
        if (mDataTypes.containsString(type)) {
            DuplicateStrings(dataIndex, type);
        }

        // Store pointer in register
        getReg().setIntVal(dataIndex);

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
        assertTrue(mDataTypes.containsString(type));
        assertTrue(!type.isByRef);
        assertTrue(type.pointerLevel == 0);

        // Type IS string case
        if (type.matchesType(BasicValType.VTP_STRING)) {

            Value val = mData.data().get(dataIndex);
            // Empty strings (index 0) can be ignored
            if (val.getIntVal() != 0) {

                // Allocate new string
                int newStringIndex = mStrings.alloc();

                // Copy previous string
                mStrings.setValue(newStringIndex,
                        mStrings.getValueAt(val.getIntVal()));

                // Point to new string
                val.setIntVal(newStringIndex);
            }
        }

        // Array case
        else if (type.arrayLevel > 0) {

            // Read array header
            int elements = mData.data().get(dataIndex).getIntVal();
            int elementSize = mData.data().get(dataIndex + 1).getIntVal();
            int arrayStart = dataIndex + 2;

            // Calculate element type
            ValType elementType = new ValType(type);
            elementType.arrayLevel--;

            // Duplicate strings in each array element
            for (int i = 0; i < elements; i++) {
                DuplicateStrings(arrayStart + i * elementSize, elementType);
            }
        }

        // Otherwise must be a structure
        else {
            assertTrue(type.basicType >= 0);

            // Find structure definition
            Structure s = mDataTypes.getStructures().get(type.basicType);

            // Duplicate strings for each field in structure
            for (int i = 0; i < s.fieldCount; i++) {
                StructureField f = mDataTypes.getFields().get(s.firstFieldIndex + i);
                if (mDataTypes.containsString(f.type)) {
                    DuplicateStrings(dataIndex + f.dataOffset, f.type);
                }
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
        if (sourceIsTemp) {
            UnwindTemp(new ProtectedStackRange(sourceIndex, sourceIndex
                    + StoredDataSize(sourceIndex, type)));
        } else {
            UnwindTemp();
        }

        // Free the data
        mData.freeTempData();

        // Calculate data size.
        // Note that the "type" does not specify array dimensions (if type is an
        // array),
        // so they must be read from the source array.
        int size = StoredDataSize(sourceIndex, type);

        // Allocate data for parameter on stack
        if (!mData.hasStackRoomFor(size)) {
            setError(ERR_USER_FUNC_STACK_OVERFLOW);
            return false;
        }
        int dataIndex = mData.allocateTemp(size, false);

        // Block copy the data
        BlockCopy(sourceIndex, dataIndex, size);

        // Extra logic required to manage strings
        if (mDataTypes.containsString(type)) {

            // If source was NOT temp data, then the object has been copied,
            // rather
            // than moved, and we must duplicate all the contained string
            // objects.
            if (!sourceIsTemp) {
                DuplicateStrings(dataIndex, type);
            }
        }

        // Store pointer in register
        getReg().setIntVal(dataIndex);

        return true;
    }

    void UnwindTemp() {
        UnwindTemp(new ProtectedStackRange());
    }

    void UnwindTemp(ProtectedStackRange protect) {
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
                    new ProtectedStackRange());
            mStackDestructors.remove(mStackDestructors.size() - 1);
        }

        // Note: We don't actually remove the data from the stack. Calling code
        // must
        // handle that instead.
    }

    void DestroyData(StackDestructor d, ProtectedStackRange protect) {
        // Apply destructor logic to data block.
        DestroyData(d.addr, mTypeSet.getValType(d.dataTypeIndex), protect);
    }

    void DestroyData(int index, ValType type, ProtectedStackRange protect) {
        assertTrue(mDataTypes.containsString(type));
        assertTrue(!type.isByRef);

        // Note: Current "destruction" logic involves deallocating strings
        // stored
        // in the data. (But could later be extended to something more general
        // purpose.)
        if (type.matchesType(BasicValType.VTP_STRING)) {

            // Don't destroy if in protected range
            if (protect.containsAddress(index)) {
                return;
            }

            // Type IS string case

            // Deallocate the string (if allocated)
            int stringIndex = mData.data().get(index).getIntVal();
            if (stringIndex != 0) {
                mStrings.freeAtIndex(stringIndex);
            }
        } else if (type.arrayLevel > 0) {

            // Array case
            ValType elementType = new ValType(type);
            elementType.arrayLevel--;
            int count = mData.data().get(index).getIntVal();
            int elementSize = mData.data().get(index + 1).getIntVal();
            int arrayStart = index + 2;

            // Don't destroy if in protected range
            if (protect.containsRange(index, arrayStart + count * elementSize)) {
                return;
            }

            // Recursively destroy each element
            for (int i = 0; i < count; i++) {
                DestroyData(arrayStart + i * elementSize, elementType, protect);
            }
        } else {

            // At this point we know the type contains a string and is not a
            // string
            // or array.
            // Can only be a structure.
            assertTrue(type.pointerLevel == 0);
            assertTrue(type.basicType >= 0);

            // Recursively destroy each structure field (that contains a string)
            Structure s = mDataTypes.getStructures().get(type.basicType);

            // Don't destroy if in protected range
            if (protect.containsRange(index, index + s.dataSize)) {
                return;
            }

            for (int i = 0; i < s.fieldCount; i++) {

                // Get field info
                StructureField f = mDataTypes.getFields().get(
                        s.firstFieldIndex + i);

                // Destroy if contains string(s)
                if (mDataTypes.containsString(f.type)) {
                    DestroyData(index + f.dataOffset, f.type, protect);
                }
            }
        }
    }

    public int NewCodeBlock() {
        mCodeBlocks.add(new CodeBlock());

        // Set pointer to code
        CurrentCodeBlock().programOffset = mCode.size();

        // Bind code block
        mBoundCodeBlock = mCodeBlocks.size() - 1;

        // Return index of new code block
        return mBoundCodeBlock;
    }

    public CodeBlock CurrentCodeBlock() {
        assertTrue(!mCodeBlocks.isEmpty());
        return mCodeBlocks.lastElement();
    }

    public int CurrentCodeBlockIndex() {
        assertTrue(!mCodeBlocks.isEmpty());
        return mCodeBlocks.size() - 1;
    }

    public boolean IsCodeBlockValid(int index) {
        return index >= 0 && index < mCodeBlocks.size();
    }

    public int GetCodeBlockOffset(int index) {
        assertTrue(IsCodeBlockValid(index));
        return mCodeBlocks.get(index).programOffset;
    }

    public RollbackPoint getRollbackPoint() {
        RollbackPoint r = new RollbackPoint();

        r.codeBlockCount = mCodeBlocks.size();
        r.boundCodeBlock = mBoundCodeBlock;
        r.functionPrototypeCount = mUserFunctionPrototypes.size();
        r.functionCount = mUserFunctions.size();
        r.dataCount = mProgramData.size();
        r.instructionCount = mCode.size();

        return r;
    }

    public void rollback(RollbackPoint rollbackPoint) {

        // Rollback virtual machine
        mCodeBlocks.setSize(rollbackPoint.codeBlockCount);
        mBoundCodeBlock = rollbackPoint.boundCodeBlock;
        mUserFunctionPrototypes.setSize(rollbackPoint.functionPrototypeCount);
        mUserFunctions.setSize(rollbackPoint.functionCount);
        mProgramData.setSize(rollbackPoint.dataCount);
        mCode.setSize(rollbackPoint.instructionCount);
    }

    // Streaming
    public void streamOut(DataOutputStream stream) throws IOException {
        int i;
        // Stream header
        Streaming.writeString(stream, STREAM_HEADER);
        Streaming.writeLong(stream, STREAM_VERSION);

        // Plugin DLLs
        //TODO Reimplement libraries
        //m_plugins.StreamOut(stream);

        // Variables
        mVariables.streamOut(stream); // Note: mVariables automatically
        // streams out mDataTypes

        // String constants
        Streaming.writeLong(stream, mStringConstants.size());
        for (i = 0; i < mStringConstants.size(); i++) {
            Streaming.writeString(stream, mStringConstants.get(i));
        }

        // Data type lookup table
        mTypeSet.streamOut(stream);

        // Program code
        Streaming.writeLong(stream, mCode.size());
        for (i = 0; i < mCode.size(); i++) {
            mCode.get(i).streamOut(stream);
        }

        // Program data (for "DATA" statements)
        Streaming.writeLong(stream, mProgramData.size());
        for (i = 0; i < mProgramData.size(); i++) {
            mProgramData.get(i).streamOut(stream);
        }

        // User function prototypes
        Streaming.writeLong(stream, mUserFunctionPrototypes.size());
        for (i = 0; i < mUserFunctionPrototypes.size(); i++) {
            mUserFunctionPrototypes.get(i).streamOut(stream);
        }

        // User functions
        Streaming.writeLong(stream, mUserFunctions.size());
        for (i = 0; i < mUserFunctions.size(); i++) {
            mUserFunctions.get(i).streamOut(stream);
        }

        // Code blocks
        Streaming.writeLong(stream, mCodeBlocks.size());
        for (i = 0; i < mCodeBlocks.size(); i++) {
            mCodeBlocks.get(i).streamOut(stream);
        }
    }

    public boolean streamIn(DataInputStream stream) throws IOException {
        // Read and validate stream header
        if (!Streaming.readString(stream).equals(STREAM_HEADER)) {
            return false;
        }
        if (Streaming.readLong(stream) != STREAM_VERSION) {
            return false;
        }

        // Plugin DLLs
        //TODO Reimplement libraries
                /*
			    if (!m_plugins.StreamIn(stream)) {
			        setError(m_plugins.Error());
			        return false;
			    }

			    // Register plugin structures and functions in VM
			    m_plugins.StructureManager().AddVMStructures(DataTypes());
			    m_plugins.CreateVMFunctionSpecs();
			    */

        // Variables
        mVariables.streamIn(stream);

        // String constants
        int count, i;
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            mStringConstants.setSize(count);
            for (i = 0; i < count; i++) {
                mStringConstants.set(i, Streaming.readString(stream));
            }

            // Data type lookup table
            mTypeSet.streamIn(stream);
        }
        // Program code
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            mCode.setSize(count);
            for (i = 0; i < count; i++) {
                mCode.set(i, new Instruction());
                mCode.get(i).streamIn(stream);
            }
        }
        // Program data (for "DATA" statements)
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            mProgramData.setSize(count);
            for (i = 0; i < count; i++) {
                mProgramData.set(i, new ProgramDataElement());
                mProgramData.get(i).streamIn(stream);
            }
        }
        // User function prototypes
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            mUserFunctionPrototypes.setSize(count);
            for (i = 0; i < count; i++) {
                mUserFunctionPrototypes.set(i, new UserFuncPrototype());
                mUserFunctionPrototypes.get(i).streamIn(stream);
            }
        }
        // User functions
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            mUserFunctions.setSize(count);
            for (i = 0; i < count; i++) {
                mUserFunctions.set(i, new UserFunc());
                mUserFunctions.get(i).streamIn(stream);
            }
        }
        // Code blocks
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            mCodeBlocks.setSize(count);
            for (i = 0; i < count; i++) {
                mCodeBlocks.set(i, new CodeBlock());
                mCodeBlocks.get(i).streamIn(stream);
            }
        }
        return true;

    }

    void patchOut() {
        if (m_breakPtsPatched) {
            internalPatchOut();
        }
    }

    // General
    public boolean isDone() {
        assertTrue(isIPValid());
        return mStopped || mCode.get(mIp).opCode == OpCode.OP_END; // Reached end of
        // program?
    }

    public boolean isRunning() {
        return !isDone() && !isPaused();
    }

    public void stop() {
        mStopped = true;
    }

    public void getIPInSourceCode(Mutable<Integer> line, Mutable<Integer> col) {
        assertTrue(isIPValid());
        line.set(mCode.get(mIp).sourceLine);
        col.set(mCode.get(mIp).sourceChar);
    }

    public InstructionPosition getIPInSourceCode() {
        assertTrue(isIPValid());
        return new InstructionPosition(mCode.get(mIp).sourceLine, mCode.get(mIp).sourceChar);
    }

    public void bindCodeBlock(int index) {
        mBoundCodeBlock = index;
    }

    public int getBoundCodeBlock() {
        return mBoundCodeBlock;
    }


    // IP and registers
    public int getIP() {
        return mIp;
    }

    public Value getReg() {
        return mReg;
    }

    public Value getReg2() {
        return mReg2;
    }

    public void setReg(Value value) {
        mReg.setVal(value);
    }

    public void setReg2(Value value) {
        mReg2.setVal(value);
    }

    public String getRegString() {
        return mRegString;
    }

    public String getReg2String() {
        return mReg2String;
    }

    public void setRegString(String string) {
        mRegString = string;
    }

    public void setReg2String(String string) {
        mReg2String = string;
    }

    public ValueStack getStack() {
        return mStack;
    }

    public void setStack(ValueStack stack) {
        mStack = stack;
    }


    // Variables, data and data types
    public TypeLibrary getDataTypes() {
        return mDataTypes;
    }

    public Data getData() {
        return mData;
    }

    public VariableCollection getVariables() {
        return mVariables;
    }

    public Vector<ProgramDataElement> getProgramData() {
        return mProgramData;
    }

    // User functions
    public Vector<UserFuncPrototype> getUserFunctionPrototypes() {
        return mUserFunctionPrototypes;
    }

    public Vector<UserFunc> getUserFunctions() {
        return mUserFunctions;
    }

    public Vector<UserFuncStackFrame> getUserCallStack() {
        return mUserCallStack;
    }

    public int getCurrentUserFrame() {
        return mCurrentUserFrame;
    }

    // Debugging
    public boolean isPaused() {
        return m_paused;
    }

    public void pause() {
        m_paused = true;
    }

    public boolean isBreakPointsPatched() {
        return m_breakPtsPatched;
    }


    public void clearTempBreakPoints() {
        patchOut();
        mTempBreakPts.clear();
    }

    public void patchIn() {
        if (!m_breakPtsPatched) {
            InternalPatchIn();
        }
    }

    public void repatchBreakpoints() {
        patchOut();
        patchIn();
    }

    public void gotoInstruction(int offset) {
        assertTrue(isOffsetValid(offset));
        mIp = offset;
    }

    public boolean skipInstruction() { // USE WITH CARE!!!
        if (mIp < getInstructionCount() + 1) {
            mIp++; // Proceed to next instruction
            return true;
        } else {
            return false;
        }
    }

    public boolean isOffsetValid(int offset) {
        return offset >= 0 && offset < getInstructionCount();
    }

    public boolean isIPValid() {
        return isOffsetValid(mIp);
    }

    // Building raw VM instructions
    public int getInstructionCount() {
        return mCode.size();
    }

    public void addInstruction(Instruction i) {
        patchOut();
        mCode.add(i);
    }

    public void rollbackProgram(int size) {
        assertTrue(size >= 0);
        assertTrue(size <= getInstructionCount());
        while (size < getInstructionCount()) {
            mCode.remove(mCode.size() - 1);
        }
    }

    public Instruction getInstruction(int index) {
        assertTrue(index < mCode.size());
        patchOut();
        return mCode.get(index);
    }
    public void setInstruction(int index, Instruction instruction) {
        assertTrue(index < mCode.size());
        patchOut();
        mCode.set(index, instruction);
    }

    public void removeLastInstruction() {
        mCode.remove(mCode.size() - 1);
    }

    public int getStoreTypeIndex(ValType type) {
        return mTypeSet.getIndex(type);
    }

    public ValType getStoredType(int index) {
        return mTypeSet.getValType(index);
    }

    // Program data
    public void storeProgramData(int type, Value v) {
        ProgramDataElement d = new ProgramDataElement();
        d.setType(type);
        d.setValue(v);
        mProgramData.add(d);
    }

    public Vector<String> getStringConstants() {
        return mStringConstants;
    }

    // External functions
    public int addFunction(Function func) {
        int result = getFunctionCount();
        mFunctions.add(func);
        return result;
    }

    public int getFunctionCount() {
        return mFunctions.size();
    }

    public int addOperatorFunction(Function func) {
        int result = getOperatorFunctionCount();
        mOperatorFunctions.add(func);
        return result;
    }

    public int getOperatorFunctionCount() {
        return mOperatorFunctions.size();
    }

    // Called by external functions
    public Value getParam(int index) {
        // Read param from param stack.
        // Index 1 is TOS
        // Index 2 is TOS - 1
        // ...
        assertTrue(index > 0);
        assertTrue(index <= mStack.size());
        return mStack.get(mStack.size() - index);
    }

    public Integer getIntParam(int index) {
        return getParam(index).getIntVal();
    }

    public Float getRealParam(int index) {
        return getParam(index).getRealVal();
    }

    public String getStringParam(int index) {
        return mStrings.getValueAt(getIntParam(index));
    }

    public void setStringParam(int index, String string) {
        mStrings.setValue(getIntParam(index), string);
    }

    public String getString(int index) {
        return mStrings.getValueAt(index);
    }

    public void setString(int index, String string) {
        mStrings.setValue(index, string);
    }

    public int allocString() {
        return mStrings.alloc();
    }

    public int getStringStoreElementCount() {
        return mStrings.getStoredElements();
    }

    public Store<String> getStringStore() {
        return mStrings;
    }

    // Reference params (called by external functions)
    public boolean checkNullRefParam(int index) {

        // Return true if param is not a null reference
        // Otherwise return false and set a virtual machine error
        boolean result = getIntParam(index) > 0;
        if (!result) {
            functionError("Unset pointer");
        }
        return result;
    }

    public Value getRefParam(int index) {

        // Get reference parameter.
        // Returns a reference to the actual Value object
        int ptr = getIntParam(index);
        assertTrue(ptr > 0);
        assertTrue(mData.isIndexValid(ptr));
        return mData.data().get(ptr);
    }

    public void functionError(String name) {
        setError("Function error: " + name);
    }

    public void miscError(String name) {
        setError(name);
    }


    // Initialisation functions
    public void addInitFunction(Function func) {
        mInitFunctions.add(func);
    }

    // Resources
    public void addResources(Resources resources) {
        mResources.add(resources);
    }


    // Plugin DLLs
    //TODO Reimplement libraries
	    /*PluginDLLManager& Plugins() { return m_plugins; }*/
    // Builtin/plugin function callback support
    public boolean isEndCallback() {
        assertTrue(isIPValid());
        return mCode.get(mIp).opCode == OpCode.OP_END_CALLBACK;        // Reached end callback opcode?
    }
}
