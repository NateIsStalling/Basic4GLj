package com.basic4gl.runtime;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.runtime.VariableCollection.Variable;
import com.basic4gl.runtime.stackframe.*;
import com.basic4gl.runtime.types.*;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.IVMDebugger;
import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.util.Resources;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
    /**
     * functions are standard functions where the parameters are pushed to the stack.
     */
    public Vector<Function> functions;
    /**
     *  operatorFunctions are generally used for language extension,
     *  and perform the job of either a unary or binary operator.
     *  <p>That is, they perform Reg2 operator Reg1, and place the result in Reg1.
     */
    public Vector<Function> operatorFunctions;
    // Registers
    /**
     * Register values (when int or float)
     */
    private Value reg, reg2;

    /**
     * Register values when string
     */
    private String regString, reg2String;

    /**
     * The current active frame.
     * <p> Note that this is often but NOT ALWAYS the top of the stack.
     * <p> (When evaluating parameters for a impending function call,
     * the pending function stack frame is the top of the stack, but is not yet active).
     */
    private int currentUserFrame;

    // Runtime stacks

    /**
     * Used for expression evaluation
     */
    private ValueStack stack;
    /**
     * Call stack for user functions
     */
    private final Vector<UserFuncStackFrame> userCallStack;

    // Individual code blocks
    private final Vector<CodeBlock> codeBlocks;
    private int boundCodeBlock;

    // Data destruction
    private final Vector<StackDestructor> stackDestructors;
    private final Vector<StackDestructor> tempDestructors;

    // Plugin DLLs
    // TODO Reimplement libraries
    // PluginDLLManager plugins;
    // IDLL_Basic4GL_Runtime pluginRuntime;

    // Debugger
    private final IVMDebugger debugger;

    // Variables, data and data types
    private final TypeLibrary dataTypes;
    private final Data data;
    private final VariableCollection variables;
    /**
     * Constant strings declared in program
     */
    private final Vector<String> stringConstants;

    private final Store<String> stringStore;
    private final List<Resources> resources;
    private final Vector<UserFuncPrototype> userFunctionPrototypes;
    private final Vector<UserFunc> userFunctions;

    /**
     * Initialisation functions
     */
    private final Vector<Function> initFunctions;

    // Program data

    /**
     * General purpose program data
     * <p>(e.g declared with "DATA" keyword in BASIC)
     */
    private final Vector<ProgramDataElement> programData;

    private int programDataOffset;

    // Instructions
    private final Vector<Instruction> codeInstructions;
    private final ValTypeSet typeSet;

    /**
     * Instruction pointer
     */
    private int ip;

    // Debugging
    /**
     * Patched in breakpoints
     */
    private final ArrayList<PatchedBreakPt> patchedBreakPoints;
    /**
     * Temporary breakpoints, generated for stepping over a line
     */
    private final ArrayList<TempBreakPt> tempBreakPoints;

    /**
     * Set to true when program hits a breakpoint. (Or can be set by caller.)
     */
    private boolean paused;

    /**
     * Set to true if breakpoints are patched and in synchronisation with compiled code
     */
    private boolean breakPointsPatched;

    private boolean stopped;

    // TODO Reimplement libraries
    // public TomVM(PluginDLLManager plugins, IVMDebugger debugger) {
    public TomVM(IVMDebugger debugger) {
        this(debugger, MAX_DATA, MAX_STACK);
    }

    // TODO Reimplement libraries
    // public TomVM(PluginDLLManager plugins, IVMDebugger debugger,
    //		int maxDataSize, int maxStackSize) {
    public TomVM(IVMDebugger debugger, int maxDataSize, int maxStackSize) {
        this.debugger = debugger;

        data = new Data(maxDataSize, maxStackSize);
        dataTypes = new TypeLibrary();
        variables = new VariableCollection(data, dataTypes);

        reg = new Value();
        reg2 = new Value();
        resources = new ArrayList<>();

        stringStore = new Store<>("");
        stack = new ValueStack(stringStore);
        userCallStack = new Vector<>();
        stackDestructors = new Vector<>();
        tempDestructors = new Vector<>();

        programData = new Vector<>();
        codeBlocks = new Vector<>();
        stringConstants = new Vector<>();

        typeSet = new ValTypeSet();

        codeInstructions = new Vector<>();
        functions = new Vector<>();
        operatorFunctions = new Vector<>();
        userFunctions = new Vector<>();
        userFunctionPrototypes = new Vector<>();
        patchedBreakPoints = new ArrayList<>();
        tempBreakPoints = new ArrayList<>();

        initFunctions = new Vector<>();
        // TODO Reimplement libraries
        // this.plugins = plugins;
        // Create plugin runtime
        // this.pluginRuntime = new TomVMDLLAdapter(this,
        //		this.plugins.StructureManager());

        clearProgram();
    }

    /**
     * New program
     */
    public void clearProgram() {
        // Clear variables, data and data types
        clearVariables();
        variables.clear();
        dataTypes.clear();
        programData.clear();
        codeBlocks.clear();

        // Clear string constants
        stringConstants.clear();

        // Deallocate code
        codeInstructions.clear();
        typeSet.clear();
        userFunctions.clear();
        userFunctionPrototypes.clear();
        ip = 0;
        paused = false;

        // Clear breakpoints
        patchedBreakPoints.clear();
        tempBreakPoints.clear();
        breakPointsPatched = false;
    }

    /**
     * Clear variables
     */
    public void clearVariables() {
        variables.deallocate(); // Deallocate variables
        data.clear(); // Deallocate variable data
        stringStore.clear(); // Clear strings
        stack.clear(); // Clear runtime stacks
        userCallStack.clear();
        stackDestructors.clear();
        tempDestructors.clear();
        currentUserFrame = -1;
        boundCodeBlock = 0;

        // Clear resources
        clearResources();

        // Init registers
        reg.setIntVal(0);
        reg2.setIntVal(0);
        regString = "";
        reg2String = "";
        programDataOffset = 0;
    }

    public IVMDebugger getDebugger() {
        return debugger;
    }

    public void clearResources() {
        // Clear resources
        for (Resources res : resources) {
            res.clear();
        }
    }

    public void resetVM() {

        // Clear error state
        clearError();

        // Deallocate variables
        clearVariables();

        // Call registered initialisation functions
        for (int i = 0; i < initFunctions.size(); i++) {
            initFunctions.get(i).run(this);
        }

        // Move to start of program
        ip = 0;
        paused = false;
    }

    public void continueVM() {
        // Reduced from 0xffffffff since Java doesn't support unsigned ints
        continueVM(0x7fffffff);
    }

    public void continueVM(int steps) // Continue execution from last position
            {

        clearError();
        paused = false;

        Instruction instruction;
        int stepCount = 0;
        int tempI;

        // Virtual machine main loop
        step:
        while (true) { // breaks on last line; taking advantage of loops having labels for continue
            // statements to replicate GOTO

            if (stopped) {
                return;
            }

            // Count steps
            if (++stepCount > steps) {
                return;
            }

            instruction = codeInstructions.get(ip);
            switch (instruction.opCode) {
                case OpCode.OP_NOP:
                    ip++; // Proceed to next instruction
                    continue step;
                case OpCode.OP_END:
                    break;
                case OpCode.OP_LOAD_CONST:

                    // Load value
                    if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        assertTrue(instruction.value.getIntVal() >= 0);
                        assertTrue(instruction.value.getIntVal() < stringConstants.size());
                        setRegString(stringConstants.get(instruction.value.getIntVal()));
                    } else {
                        setReg(new Value(instruction.value));
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_LOAD_VAR: {

                    // Load variable.
                    // Instruction contains index of variable.
                    assertTrue(variables.isIndexValid(instruction.value.getIntVal()));
                    Variable var = variables.getVariables().get(instruction.value.getIntVal());
                    if (var.allocated()) {
                        // Load address of variable's data into register
                        getReg().setIntVal(var.dataIndex);
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNDIMMED_VARIABLE);
                    break;
                }

                case OpCode.OP_LOAD_LOCAL_VAR: {

                    // Find current stack frame
                    assertTrue(currentUserFrame >= 0);
                    assertTrue(currentUserFrame < userCallStack.size());
                    UserFuncStackFrame currentFrame = userCallStack.get(currentUserFrame);

                    // Find variable
                    int index = instruction.value.getIntVal();

                    // Instruction contains index of variable.
                    if (currentFrame.localVarDataOffsets.get(index) != 0) {
                        // Load address of variable's data into register
                        getReg().setIntVal(currentFrame.localVarDataOffsets.get(index));
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNDIMMED_VARIABLE);
                    break;
                }

                case OpCode.OP_DEREF: {

                    // Dereference reg.
                    if (getReg().getIntVal() != 0) {
                        assertTrue(data.isIndexValid(getReg().getIntVal()));
                        // Find value that reg points to
                        Value val = data.data().get(getReg().getIntVal());
                        switch (instruction.basicVarType) {
                            case BasicValType.VTP_INT:
                            case BasicValType.VTP_REAL:
                                setReg(val);
                                ip++; // Proceed to next instruction
                                continue step;
                            case BasicValType.VTP_STRING:
                                assertTrue(stringStore.isIndexValid(val.getIntVal()));
                                setRegString(stringStore.getValueAt(val.getIntVal()));
                                ip++; // Proceed to next instruction
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
                        getReg().setIntVal(getReg().getIntVal() + instruction.value.getIntVal());
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNSET_POINTER);
                    break;

                case OpCode.OP_ARRAY_INDEX:
                    if (getReg2().getIntVal() != 0) {
                        // Input: mReg2 = Array address
                        // mReg = Array index
                        // Output: mReg = Element address
                        assertTrue(data.isIndexValid(getReg2().getIntVal()));
                        assertTrue(data.isIndexValid(getReg2().getIntVal() + 1));

                        // mReg2 points to array header (2 values)
                        // First value is highest element (i.e number of elements +
                        // 1)
                        // Second value is size of array element.
                        // Array data immediately follows header
                        if (getReg().getIntVal() >= 0
                                && getReg().getIntVal()
                                        < data.data().get(getReg2().getIntVal()).getIntVal()) {
                            assertTrue(
                                    data.data().get(getReg2().getIntVal() + 1).getIntVal() >= 0);
                            getReg().setIntVal(getReg2().getIntVal()
                                    + 2
                                    + getReg().getIntVal()
                                            * data.data()
                                                    .get(getReg2().getIntVal() + 1)
                                                    .getIntVal());

                            ip++; // Proceed to next instruction
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
                        stack.pushString(getRegString());
                    } else {
                        stack.push(getReg());
                    }

                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_POP:

                    // Pop reg2 from stack
                    if (instruction.basicVarType == BasicValType.VTP_STRING) {

                        setReg2String(stack.popString());
                    } else {
                        getReg2().setVal(stack.pop());
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_SAVE: {

                    // Save reg into [reg2]
                    if (getReg2().getIntVal() > 0) {
                        assertTrue(data.isIndexValid(getReg2().getIntVal()));
                        Value dest = data.data().get(getReg2().getIntVal());
                        switch (instruction.basicVarType) {
                            case BasicValType.VTP_INT:
                            case BasicValType.VTP_REAL:
                                // mData.Data().set(mReg2.getIntVal(), new Value(mReg));
                                dest.setVal(getReg());

                                ip++; // Proceed to next instruction
                                continue step;
                            case BasicValType.VTP_STRING:

                                // Allocate string space if necessary
                                if (dest.getIntVal() == 0) {
                                    dest.setIntVal(stringStore.alloc());
                                }

                                // Copy string value
                                stringStore.setValue(dest.getIntVal(), getRegString());
                                ip++; // Proceed to next instruction
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
                    if (copyData(
                            getReg().getIntVal(),
                            getReg2().getIntVal(),
                            typeSet.getValType(instruction.value.getIntVal()))) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }
                case OpCode.OP_DECLARE: {

                    // Allocate variable.
                    assertTrue(variables.isIndexValid(instruction.value.getIntVal()));
                    Variable var = variables.getVariables().get(instruction.value.getIntVal());

                    // Must not already be allocated
                    if (var.allocated()) {
                        setError(ERR_REDIMMED_VARIABLE);
                        break;
                    }

                    // Pop and validate array dimensions sizes into type (if
                    // applicable)
                    if (var.type.getPhysicalPointerLevel() == 0 && !popArrayDimensions(var.type)) {
                        break;
                    }

                    // Validate type size
                    if (!validateTypeSize(var.type)) {
                        break;
                    }

                    // Allocate variable
                    var.allocate(data, dataTypes);

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_DECLARE_LOCAL: {

                    // Allocate local variable

                    // Find current stack frame
                    assertTrue(currentUserFrame >= 0);
                    assertTrue(currentUserFrame < userCallStack.size());
                    UserFuncStackFrame currentFrame = userCallStack.get(currentUserFrame);
                    UserFunc userFunc = userFunctions.get(currentFrame.userFuncIndex);
                    UserFuncPrototype prototype = userFunctionPrototypes.get(userFunc.prototypeIndex);

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
                    if (type.getPhysicalPointerLevel() == 0 && !popArrayDimensions(type)) {
                        break;
                    }

                    // Validate type size
                    if (!validateTypeSizeForStack(type)) {
                        break;
                    }

                    // Allocate new data
                    int dataIndex = data.allocateStack(dataTypes.getDataSize(type));

                    // Initialise it
                    data.initData(dataIndex, type, dataTypes);

                    // Store data index in stack frame
                    currentFrame.localVarDataOffsets.set(index, dataIndex);

                    // Also store in register, so that OpCode.OP_REG_DESTRUCTOR can be used
                    getReg().setIntVal(dataIndex);

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_JUMP:

                    // Jump
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < codeInstructions.size());
                    ip = instruction.value.getIntVal();
                    continue step; // Proceed without incrementing instruction

                case OpCode.OP_JUMP_TRUE:

                    // Jump if reg != 0
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < codeInstructions.size());
                    if (getReg().getIntVal() != 0) {
                        ip = instruction.value.getIntVal();
                        continue step; // Proceed without incrementing instruction
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_JUMP_FALSE:

                    // Jump if reg == 0
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < codeInstructions.size());
                    if (getReg().getIntVal() == 0) {
                        ip = instruction.value.getIntVal();
                        continue step; // Proceed without incrementing instruction
                    }
                    ip++; // Proceed to next instruction
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
                    ip++; // Proceed to next instruction
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
                    ip++; // Proceed to next instruction
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
                    ip++; // Proceed to next instruction
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
                    ip++; // Proceed to next instruction
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
                    ip++; // Proceed to next instruction
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
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_NOT:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg().getIntVal() == 0 ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_EQUAL:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg2().getIntVal() == getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(getReg2().getRealVal() == getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal(getReg2String().equals(getRegString()) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_NOT_EQUAL:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg2().getIntVal() != getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(getReg2().getRealVal() != getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal(!getReg2String().equals(getRegString()) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_GREATER:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg2().getIntVal() > getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(getReg2().getRealVal() > getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal((getReg2String().compareTo(getRegString()) > 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_GREATER_EQUAL:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg2().getIntVal() >= getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(getReg2().getRealVal() >= getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal((getReg2String().compareTo(getRegString()) >= 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_LESS:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg2().getIntVal() < getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(getReg2().getRealVal() < getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal((getReg2String().compareTo(getRegString()) < 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_LESS_EQUAL:
                    if (instruction.basicVarType == BasicValType.VTP_INT) {
                        getReg().setIntVal(getReg2().getIntVal() <= getReg().getIntVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_REAL) {
                        getReg().setIntVal(getReg2().getRealVal() <= getReg().getRealVal() ? -1 : 0);
                    } else if (instruction.basicVarType == BasicValType.VTP_STRING) {
                        getReg().setIntVal((getReg2String().compareTo(getRegString()) <= 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_REAL:
                    getReg().setRealVal((float) getReg().getIntVal());
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_REAL2:
                    getReg2().setRealVal((float) getReg2().getIntVal());
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_INT:
                    getReg().setIntVal((int) getReg().getRealVal());
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_INT2:
                    getReg2().setIntVal((int) getReg2().getRealVal());
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_STRING:
                    setRegString(String.valueOf(getReg().getIntVal()));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_STRING:
                    setRegString(String.valueOf(getReg().getRealVal()));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_STRING2:
                    setReg2String(String.valueOf(getReg2().getIntVal()));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_STRING2:
                    setReg2String(String.valueOf(getReg2().getRealVal()));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_AND:
                    getReg().setIntVal(getReg().getIntVal() & getReg2().getIntVal());
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_OR:
                    getReg().setIntVal(getReg().getIntVal() | getReg2().getIntVal());
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_XOR:
                    getReg().setIntVal(getReg().getIntVal() ^ getReg2().getIntVal());
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CALL_FUNC:
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < functions.size());

                    // Call external function
                    functions.get(instruction.value.getIntVal()).run(this);

                    if (!hasError()) {
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    break;

                case OpCode.OP_CALL_OPERATOR_FUNC:
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < operatorFunctions.size());

                    // Call external function
                    operatorFunctions.get(instruction.value.getIntVal()).run(this);
                    if (!hasError()) {
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    break;

                case OpCode.OP_TIMESHARE:
                    ip++; // Move on to next instruction
                    break; // And return

                case OpCode.OP_FREE_TEMP:

                    // Free temporary data
                    unwindTemp();
                    data.freeTempData();
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_ALLOC: {

                    // Extract type, and array dimensions
                    ValType type = new ValType(typeSet.getValType(instruction.value.getIntVal()));
                    if (!popArrayDimensions(type)) {
                        break;
                    }

                    // Validate type size
                    if (!validateTypeSize(type)) {
                        break;
                    }

                    // Allocate and initialise new data
                    getReg().setIntVal(data.allocate(dataTypes.getDataSize(type)));
                    data.initData(getReg().getIntVal(), type, dataTypes);

                    ip++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_CALL: {

                    // Call
                    assertTrue(instruction.value.getIntVal() >= 0);
                    assertTrue(instruction.value.getIntVal() < codeInstructions.size());

                    // Check for stack overflow
                    if (userCallStack.size() >= MAX_USER_STACK_CALLS) {
                        setError(ERR_STACK_OVERFLOW);
                        break;
                    }

                    // Push stack frame, with return address
                    userCallStack.add(new UserFuncStackFrame());
                    UserFuncStackFrame stackFrame = userCallStack.lastElement();
                    stackFrame.initForGosub(ip + 1);

                    // Jump to subroutine
                    ip = instruction.value.getIntVal();
                    continue step; // Proceed without incrementing instruction
                }
                case OpCode.OP_RETURN:

                    // Return from GOSUB

                    // Pop and validate return address
                    if (userCallStack.isEmpty()) {
                        setError(ERR_RETURN_WITHOUT_GOSUB);
                        break;
                    }
                    // -1 means GOSUB. Should be impossible to execute
                    // an OpCode.OP_RETURN if stack top is not a GOSUB
                    assertTrue(userCallStack.lastElement().userFuncIndex == -1);

                    tempI = userCallStack.lastElement().returnAddr;
                    userCallStack.remove(userCallStack.size() - 1);
                    if (tempI >= codeInstructions.size()) {
                        setError(ERR_STACK_ERROR);
                        break;
                    }

                    // Jump to return address
                    ip = tempI;
                    continue step; // Proceed without incrementing instruction

                case OpCode.OP_CALL_DLL: {

                    // Call plugin DLL function
                    // TODO Reimplement libraries
                    // int index = instruction.mValue.getIntVal();
                    // this.plugins.GetPluginDLL(index >> 24)
                    //		.GetFunction(index & 0x00ffffff).Run(this.pluginRuntime);
                    setError(ERR_DLL_NOT_IMPLEMENTED); // Remove line when libraries are implemented
                    if (!hasError()) {
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    break;
                }

                case OpCode.OP_CREATE_USER_FRAME: {

                    // Check for stack overflow
                    if (userCallStack.size() >= MAX_USER_STACK_CALLS) {
                        setError(ERR_STACK_OVERFLOW);
                        break;
                    }

                    // Create and initialize stack frame
                    int funcIndex = instruction.value.getIntVal();
                    userCallStack.add(new UserFuncStackFrame());
                    UserFuncStackFrame stackFrame = userCallStack.lastElement();
                    stackFrame.initForUserFunction(
                            userFunctionPrototypes.get(userFunctions.get(funcIndex).prototypeIndex), funcIndex);

                    // Save previous stack frame data
                    Mutable<Integer> tempTop = new Mutable<>(0), tempLock = new Mutable<>(0);
                    data.saveState(tempTop, tempLock);
                    stackFrame.prevStackTop = tempTop.get();
                    stackFrame.prevTempDataLock = tempLock.get();

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_CREATE_RUNTIME_FRAME: {
                    assertTrue(!codeBlocks.isEmpty());

                    // Find function index
                    int funcIndex = -1;

                    // Look for function in bound code block
                    int runtimeIndex = instruction.value.getIntVal();
                    if (boundCodeBlock > 0 && boundCodeBlock < codeBlocks.size()) {
                        CodeBlock codeBlock = codeBlocks.get(boundCodeBlock);
                        if (codeBlock.programOffset >= 0) {
                            funcIndex = codeBlock.getRuntimeFunction(runtimeIndex).functionIndex;
                        }
                    }

                    // If not found, look in main program
                    if (funcIndex < 0) {
                        funcIndex = codeBlocks.get(0).getRuntimeFunction(runtimeIndex).functionIndex;
                    }

                    // No function => Runtime error
                    if (funcIndex < 0) {
                        setError(ERR_NO_RUNTIME_FUNCTION);
                        break;
                    }

                    // From here on the logic is the same as OpCode.OP_CREATE_USER_FRAME
                    // Check for stack overflow
                    if (userCallStack.size() >= MAX_USER_STACK_CALLS) {
                        setError(ERR_STACK_OVERFLOW);
                        break;
                    }

                    // Create and initialize stack frame
                    userCallStack.add(new UserFuncStackFrame());
                    UserFuncStackFrame stackFrame = userCallStack.lastElement();
                    stackFrame.initForUserFunction(
                            userFunctionPrototypes.get(userFunctions.get(funcIndex).prototypeIndex), funcIndex);

                    // Save previous stack frame data
                    Mutable<Integer> tempTop = new Mutable<>(0), tempLock = new Mutable<>(0);
                    data.saveState(tempTop, tempLock);
                    stackFrame.prevStackTop = tempTop.get();
                    stackFrame.prevTempDataLock = tempLock.get();

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_CALL_USER_FUNC: {

                    // Call user defined function
                    UserFuncStackFrame stackFrame = userCallStack.lastElement();
                    UserFunc userFunc = userFunctions.get(stackFrame.userFuncIndex);

                    // Make active
                    stackFrame.prevCurrentFrame = currentUserFrame;
                    currentUserFrame = userCallStack.size() - 1;

                    // Call function
                    stackFrame.returnAddr = ip + 1;
                    ip = userFunc.programOffset;
                    continue step; // Proceed without incrementing instruction
                }

                case OpCode.OP_RETURN_USER_FUNC: {
                    assertTrue(!userCallStack.isEmpty());

                    // Find current stack frame
                    UserFuncStackFrame stackFrame = userCallStack.lastElement();
                    assertTrue(stackFrame.userFuncIndex >= 0);

                    // Restore previous stack frame data
                    boolean doFreeTempData = instruction.value.getIntVal() == 1;
                    if (doFreeTempData) {
                        unwindTemp();
                    }
                    unwindStack(stackFrame.prevStackTop);
                    data.restoreState(stackFrame.prevStackTop, stackFrame.prevTempDataLock, doFreeTempData);

                    // Return to return address
                    ip = stackFrame.returnAddr;

                    // Make previous frame active
                    currentUserFrame = stackFrame.prevCurrentFrame;

                    // Remove stack frame
                    userCallStack.remove(userCallStack.size() - 1);

                    continue step; // Proceed without incrementing instruction
                }

                case OpCode.OP_NO_VALUE_RETURNED:
                    setError(ERR_NO_VALUE_RETURNED);
                    break;
                case OpCode.OP_BINDCODE:
                    boundCodeBlock = getReg().getIntVal();
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_EXEC:

                    // Call runtime compiled code block.
                    // Call is like a GOSUB.
                    // RETURN will return back to the next op-code
                    if (boundCodeBlock > 0 && boundCodeBlock < codeBlocks.size()) {
                        CodeBlock codeBlock = codeBlocks.get(boundCodeBlock);
                        if (codeBlock.programOffset >= 0) {

                            // From here the code is the same as OpCode.OP_CALL
                            assertTrue(codeBlock.programOffset >= 0);
                            assertTrue(codeBlock.programOffset < codeInstructions.size());

                            // Check for stack overflow
                            if (userCallStack.size() >= MAX_USER_STACK_CALLS) {
                                setError(ERR_STACK_OVERFLOW);
                                break;
                            }

                            // Push stack frame, with return address
                            userCallStack.add(new UserFuncStackFrame());
                            UserFuncStackFrame stackFrame = userCallStack.lastElement();
                            stackFrame.initForGosub(ip + 1);

                            // Jump to subroutine
                            ip = codeBlock.programOffset;
                            continue step;
                        }
                    }

                    setError(ERR_INVALID_CODE_BLOCK);
                    break;

                case OpCode.OP_END_CALLBACK:
                    break; // Timeshare break. Calling code will then detect this op-code has been reached

                case OpCode.OP_DATA_READ:

                    // Read program data into register
                    if (readProgramData(instruction.basicVarType)) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                case OpCode.OP_DATA_RESET:
                    programDataOffset = instruction.value.getIntVal();
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_SAVE_PARAM: {

                    // Allocate parameter data
                    if (!data.hasStackRoomFor(1)) {
                        setError(ERR_USER_FUNC_STACK_OVERFLOW);
                        break;
                    }
                    int dataIndex = data.allocateStack(1);
                    int paramIndex = instruction.value.getIntVal();

                    // Initialize parameter
                    assertTrue(!userCallStack.isEmpty());
                    userCallStack.lastElement().localVarDataOffsets.set(paramIndex, dataIndex);

                    // Transfer register value to parameter
                    Value dest = data.data().get(dataIndex);
                    switch (instruction.basicVarType) {
                        case BasicValType.VTP_INT:
                        case BasicValType.VTP_REAL:
                            // TODO Confirm value is properly set
                            // TODO Check other "dest" variables
                            dest.setVal(getReg());
                            break;
                        case BasicValType.VTP_STRING:

                            // Allocate string space
                            dest.setIntVal(stringStore.alloc());

                            // Copy string value
                            stringStore.setValue(dest.getIntVal(), getRegString());
                            break;
                        default:
                            assertTrue(false);
                    }

                    // Save parameter offset in register (so that OpCode.OP_REG_DESTRUCTOR
                    // will work)
                    getReg().setIntVal(dataIndex);
                    ip++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_COPY_USER_STACK: {

                    // Copy data pointed to by mReg into next stack frame
                    // parameter.
                    // Instruction value points to the parameter data type.
                    if (copyToParam(getReg().getIntVal(), typeSet.getValType(instruction.value.getIntVal()))) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }

                case OpCode.OP_MOVE_TEMP: {
                    if (moveToTemp(getReg().getIntVal(), typeSet.getValType(instruction.value.getIntVal()))) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }

                case OpCode.OP_CHECK_PTR: {
                    if (checkPointer(getReg2().getIntVal(), getReg().getIntVal())) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        setError(ERR_POINTER_SCOPE_ERROR);
                        break;
                    }
                }

                case OpCode.OP_CHECK_PTRS: {
                    if (checkPointers(
                            getReg().getIntVal(),
                            typeSet.getValType(instruction.value.getIntVal()),
                            getReg2().getIntVal())) {
                        ip++; // Proceed to next instruction
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
                    } else if (ptr < data.getTempData()) {

                        // Pointer into temp data found
                        assertTrue(tempDestructors.isEmpty() || tempDestructors.lastElement().addr < ptr);
                        tempDestructors.add(new StackDestructor(ptr, instruction.value.getIntVal()));
                    } else if (ptr >= data.getStackTop() && ptr < data.getPermanent()) {

                        // Pointer into stack data found
                        assertTrue(stackDestructors.isEmpty() || stackDestructors.lastElement().addr > ptr);
                        stackDestructors.add(new StackDestructor(ptr, instruction.value.getIntVal()));
                    }
                    ip++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_SAVE_PARAM_PTR: {

                    // Save register pointer into param pointer
                    assertTrue(!userCallStack.isEmpty());
                    userCallStack
                            .lastElement()
                            .localVarDataOffsets
                            .set(instruction.value.getIntVal(), getReg().getIntVal());

                    ip++; // Proceed to next instruction
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
                    if (!stack.isEmpty()) {
                        setError(ERR_RUN_CALLED_INSIDE_EXECUTE);
                    } else {
                        resetVM(); // Reset program
                    }
                    break; // Timeshare break

                case OpCode.OP_BREAKPT:
                    paused = true; // Pause program
                    break; // Timeshare break

                default:
                    setError(ERR_INVALID);
            }
            break; // DO NOT LOOP
        }
    }

    // Constant strings
    public int storeStringConstant(String string) {
        int index = stringConstants.size();
        stringConstants.add(string);
        return index;
    }

    // Internal methods
    void blockCopy(int sourceIndex, int destIndex, int size) {

        // Block copy data
        assertTrue(data.isIndexValid(sourceIndex));
        assertTrue(data.isIndexValid(sourceIndex + size - 1));
        assertTrue(data.isIndexValid(destIndex));
        assertTrue(data.isIndexValid(destIndex + size - 1));
        for (int i = 0; i < size; i++) {
            data.data().set(destIndex + i, data.data().get(sourceIndex + i));
        }
    }

    void copyStructure(int sourceIndex, int destIndex, ValType type) {
        assertTrue(dataTypes.isTypeValid(type));
        assertTrue(type.getVirtualPointerLevel() == 0);
        assertTrue(type.arrayLevel == 0);
        assertTrue(type.basicType >= 0);

        // Find structure definition
        Structure s = dataTypes.getStructures().get(type.basicType);

        // Copy fields in structure
        for (int i = 0; i < s.fieldCount; i++) {
            StructureField f = dataTypes.getFields().get(s.firstFieldIndex + i);
            copyField(sourceIndex + f.dataOffset, destIndex + f.dataOffset, f.type);
        }
    }

    void copyArray(int sourceIndex, int destIndex, ValType type) {
        assertTrue(dataTypes.isTypeValid(type));
        assertTrue(type.getVirtualPointerLevel() == 0);
        assertTrue(type.arrayLevel > 0);
        assertTrue(data.isIndexValid(sourceIndex));
        assertTrue(data.isIndexValid(destIndex));
        assertTrue(data.data().get(sourceIndex).getIntVal()
                == data.data().get(destIndex).getIntVal()); // Array sizes match
        assertTrue(data.data().get(sourceIndex + 1).getIntVal()
                == data.data().get(destIndex + 1).getIntVal()); // Element sizes match

        // Find element type and size
        ValType elementType = new ValType(type);
        elementType.arrayLevel--;
        int elementSize = data.data().get(sourceIndex + 1).getIntVal();

        // Copy elements
        for (int i = 0; i < data.data().get(sourceIndex).getIntVal(); i++) {
            if (elementType.arrayLevel > 0) {
                copyArray(sourceIndex + 2 + i * elementSize, destIndex + 2 + i * elementSize, elementType);
            } else {
                copyField(sourceIndex + 2 + i * elementSize, destIndex + 2 + i * elementSize, elementType);
            }
        }
    }

    void copyField(int sourceIndex, int destIndex, ValType type) {

        assertTrue(dataTypes.isTypeValid(type));

        // If type is basic string, copy string value
        if (type.matchesType(BasicValType.VTP_STRING)) {
            Value src = data.data().get(sourceIndex);
            Value dest = data.data().get(destIndex);
            if (src.getIntVal() > 0 || dest.getIntVal() > 0) {

                // Allocate string space if necessary
                if (dest.getIntVal() == 0) {
                    dest.setIntVal(stringStore.alloc());
                }

                // Copy string value
                stringStore.setValue(
                        dest.getIntVal(),
                        stringStore.getValueAt(data.data().get(sourceIndex).getIntVal()));
            }
        }

        // If type is basic, or pointer then just copy value
        else if (type.isBasicType() || type.getVirtualPointerLevel() > 0) {
            data.data().set(destIndex, data.data().get(sourceIndex));
        }

        // If contains no strings, can just block copy
        else if (!dataTypes.containsString(type)) {
            blockCopy(sourceIndex, destIndex, dataTypes.getDataSize(type));
        }

        // Otherwise copy array or structure
        else if (type.arrayLevel > 0) {
            copyArray(sourceIndex, destIndex, type);
        } else {
            copyStructure(sourceIndex, destIndex, type);
        }
    }

    boolean copyData(int sourceIndex, int destIndex, ValType type) {
        assertTrue(dataTypes.isTypeValid(type));
        assertTrue(type.getVirtualPointerLevel() == 0);

        // If a referenced type (which it should always be), convert to regular
        // type.
        // (To facilitate comparisons against basic types such as VTP_STRING.)
        if (type.isByRef) {
            type.pointerLevel--;
        }
        type.isByRef = false;

        // Check pointers are valid
        if (!data.isIndexValid(sourceIndex) || !data.isIndexValid(destIndex) || sourceIndex == 0 || destIndex == 0) {
            setError(ERR_UNSET_POINTER);
            return false;
        }

        // Calculate element size
        int size = 1;
        if (type.basicType >= 0) {
            size = dataTypes.getStructures().get(type.basicType).dataSize;
        }

        // If the data types are arrays, then their sizes could be different.
        // If so, this is a run-time error.
        if (type.arrayLevel > 0) {
            int s = sourceIndex + (type.arrayLevel - 1) * 2, d = destIndex + (type.arrayLevel - 1) * 2;
            for (int i = 0; i < type.arrayLevel; i++) {
                assertTrue(data.isIndexValid(s));
                assertTrue(data.isIndexValid(s + 1));
                assertTrue(data.isIndexValid(d));
                assertTrue(data.isIndexValid(d + 1));
                if (data.data().get(s).getIntVal() != data.data().get(d).getIntVal()) {
                    setError(ERR_ARRAY_SIZE_MISMATCH);
                    return false;
                }

                // Update data size
                size *= data.data().get(s).getIntVal();
                size += 2;

                // Point to first element in array
                s -= 2;
                d -= 2;
            }
        }

        // If data type doesn't contain strings, can do a straight block copy
        if (!dataTypes.containsString(type)) {
            blockCopy(sourceIndex, destIndex, size);
        } else {
            copyField(sourceIndex, destIndex, type);
        }

        return true;
    }

    boolean checkPointers(int index, ValType type, int destIndex) {

        // Check that pointers in data at "index" of type "type" can be stored
        // at
        // "destIndex" without any pointer scope errors.
        // (See CheckPointer for defn of "pointer scope error")
        assertTrue(data.isIndexValid(index));
        assertTrue(dataTypes.isTypeValid(type));

        // Type does not contain any pointers?
        if (!dataTypes.containsPointer(type)) {
            return true;
        }

        // Type is a pointer?
        if (type.pointerLevel > 0) {
            return checkPointer(destIndex, data.data().get(index).getIntVal());
        }

        // Type is not a pointer, but contains one or more pointers.
        // Need to recursively break down object and check

        // Type is array?
        if (type.arrayLevel > 0) {

            // Find and check elements
            int elements = data.data().get(index).getIntVal();
            int elementSize = data.data().get(index + 1).getIntVal();
            int arrayStart = index + 2;

            // Calculate element type
            ValType elementType = new ValType(type);
            elementType.arrayLevel--;

            // Check each element
            for (int i = 0; i < elements; i++) {
                if (!checkPointers(arrayStart + i * elementSize, elementType, destIndex)) {
                    return false;
                }
            }

            return true;
        } else {

            // Must be a structure
            assertTrue(type.basicType >= 0);

            // Find structure definition
            Structure s = dataTypes.getStructures().get(type.basicType);

            // Check each field in structure
            for (int i = 0; i < s.fieldCount; i++) {
                StructureField f = dataTypes.getFields().get(s.firstFieldIndex + i);
                if (!checkPointers(index + f.dataOffset, f.type, destIndex)) {
                    return false;
                }
            }

            return true;
        }
    }

    boolean checkPointer(int dest, int ptr) {

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
        assertTrue(data.isIndexValid(dest));

        // Null pointer case
        if (ptr == 0) {
            return true;
        }

        // Check whether pointer points to temporary stack data
        if (ptr < data.getPermanent()) {

            // Such pointers can only be stored in variables in the current
            // stack frame.
            if (userCallStack.isEmpty()
                    || !(dest >= data.getStackTop() && dest < userCallStack.lastElement().prevStackTop)) {
                return false;
            }
        }

        return true;
    }

    boolean popArrayDimensions(ValType type) {
        assertTrue(dataTypes.isTypeValid(type));
        assertTrue(type.getVirtualPointerLevel() == 0);

        // Pop and validate array indices from stack into type
        int i;
        Value v = new Value();
        for (i = 0; i < type.arrayLevel; i++) {
            v = stack.pop();
            int size = v.getIntVal() + 1;
            if (size < 1) {
                setError(ERR_ZERO_LENGTH_ARRAY);
                return false;
            }
            type.arrayDimensions[i] = size;
        }

        return true;
    }

    boolean validateTypeSize(ValType type) {

        // Enforce variable size limitations.
        // This prevents rogue programs from trying to allocate unrealistic
        // amounts
        // of data.
        if (dataTypes.isDataSizeBiggerThan(type, data.getMaxDataSize())) {
            setError(ERR_VARIABLE_TOO_BIG);
            return false;
        }

        if (!data.hasRoomFor(dataTypes.getDataSize(type))) {
            setError(ERR_OUT_OF_MEMORY);
            return false;
        }

        return true;
    }

    boolean validateTypeSizeForStack(ValType type) {
        // Enforce variable size limitations.
        // This prevents rogue programs from trying to allocate unrealistic
        // amounts
        // of data.
        if (dataTypes.isDataSizeBiggerThan(type, data.getMaxDataSize())) {
            setError(ERR_VARIABLE_TOO_BIG);
            return false;
        }

        if (!data.hasStackRoomFor(dataTypes.getDataSize(type))) {
            setError(ERR_USER_FUNC_STACK_OVERFLOW);
            return false;
        }

        return true;
    }

    void patchInBreakPoint(int offset) {

        // Only patch if offset is valid and there is no breakpoint there
        // already.
        // Note: Don't patch in breakpoint to last instruction of program as
        // this is
        // always OpCode.OP_END anyway.
        if (offset < codeInstructions.size() - 1 && codeInstructions.get(offset).opCode != OpCode.OP_BREAKPT) {

            // Record previous op-code
            PatchedBreakPt bp = new PatchedBreakPt();
            bp.setOffset(offset);
            bp.setReplacedOpCode(codeInstructions.get(offset).opCode);
            patchedBreakPoints.add(bp);

            // Patch in breakpoint
            codeInstructions.get(offset).opCode = OpCode.OP_BREAKPT;
        }
    }

    void internalPatchOut() {

        // Patch out breakpoints and restore program to its no breakpoint state.
        for (PatchedBreakPt pt : patchedBreakPoints) {
            if (pt.getOffset() < codeInstructions.size()) {
                codeInstructions.get(pt.getOffset()).opCode = pt.getReplacedOpCode();
            }
        }
        patchedBreakPoints.clear();
        breakPointsPatched = false;
    }

    void internalPatchIn() {

        // Patch breakpoint instructions into the virtual machine code program.
        // This consists of swapping the virtual machine op-codes with
        // OpCode.OP_BREAKPT
        // codes.
        // We record the old op-code in the mPatchedBreakPts list, so we can
        // restore
        // the program when we've finished.

        System.out.println(debugger.getUserBreakPointCount());
        // User breakpts
        for (int i = 0; i < debugger.getUserBreakPointCount(); i++) {

            // Find line number
            int line = debugger.getUserBreakPointLine(i);

            // Convert to offset
            int offset = 0;
            while (offset < codeInstructions.size() && codeInstructions.get(offset).sourceLine < line) {
                offset++;
            }

            // Patch in breakpt
            if (offset < codeInstructions.size()) {
                patchInBreakPoint(offset);
            }
        }

        // Patch in temp breakpts
        for (TempBreakPt pt : tempBreakPoints) {
            patchInBreakPoint(pt.getOffset());
        }

        breakPointsPatched = true;
    }

    TempBreakPt makeTempBreakPoint(int offset) {
        TempBreakPt breakPt = new TempBreakPt();
        breakPt.setOffset(offset);
        return breakPt;
    }

    int calcBreakPointOffset(int line) {
        int offset = 0;
        while (offset < codeInstructions.size() && codeInstructions.get(offset).sourceLine < line) {
            offset++;
        }
        // Is breakpoint line valid?
        if (offset < codeInstructions.size() && codeInstructions.get(offset).sourceLine == line) {
            return offset;
        } else {
            return 0xffff; // 0xffff means line invalid
        }
        // TODO Value is meant to be unsigned; confirm this doesn't cause issues
    }

    public void addStepBreakPoints(boolean stepInto) {
        // Add temporary breakpoints to catch execution after stepping over the
        // current line
        if (ip >= codeInstructions.size()) {
            return;
        }
        patchOut();

        // Calculate op-code range that corresponds to the current line.
        int line, startOffset, endOffset;
        startOffset = ip;
        line = codeInstructions.get(startOffset).sourceLine;

        // Search for start of line
        while (startOffset > 0 && codeInstructions.get(startOffset - 1).sourceLine == line) {
            startOffset--;
        }

        // Search for start of next line
        endOffset = ip + 1;
        while (endOffset < codeInstructions.size() && codeInstructions.get(endOffset).sourceLine == line) {
            endOffset++;
        }

        // Create breakpoint on next line
        tempBreakPoints.add(makeTempBreakPoint(endOffset));

        // Scan for jumps, and place breakpoints at destination addresses
        for (int i = startOffset; i < endOffset; i++) {
            // TODO had to reduce dest from 0xffffffff since Java does not like unsigned values
            int dest = 0x7fffffff;
            switch (codeInstructions.get(i).opCode) {
                case OpCode.OP_CALL:
                    if (!stepInto) // If stepInto then fall through to JUMP
                    // handling.
                    {
                        break; // Otherwise break out, and no BP will be set.
                    }
                case OpCode.OP_JUMP:
                case OpCode.OP_JUMP_TRUE:
                case OpCode.OP_JUMP_FALSE:
                    dest = codeInstructions.get(i).value.getIntVal(); // Destination jump
                    // address
                    break;
                case OpCode.OP_RETURN:
                case OpCode.OP_RETURN_USER_FUNC:
                    if (!userCallStack.isEmpty()) // Look at call stack and place
                    // breakpoint on return
                    {
                        dest = userCallStack.lastElement().returnAddr;
                    }
                    break;
                case OpCode.OP_CREATE_USER_FRAME:
                    if (stepInto) {
                        dest = userFunctions.get(codeInstructions.get(i).value.getIntVal()).programOffset;
                    }
                    break;
                default:
                    break;
            }

            if (dest < codeInstructions.size() // Destination valid?
                    && (dest < startOffset || dest >= endOffset)) // Destination outside line we are stepping over?
            {
                tempBreakPoints.add(makeTempBreakPoint(dest)); // Add breakpoint
            }
        }
    }

    /**
     * Add breakpoint to step out of gosub
     * @return
     */
    public boolean addStepOutBreakPoint() {

        // Call stack must contain at least 1 return
        if (!userCallStack.isEmpty()) {
            int returnAddr = userCallStack.lastElement().returnAddr;
            if (returnAddr < codeInstructions.size()) { // Validate it
                // Place breakpoint
                tempBreakPoints.add(makeTempBreakPoint(returnAddr));
                return true;
            }
        }
        return false;
    }

    public VMState getState() {
        VMState s = new VMState();

        // Instruction pointer
        s.setIp(ip);

        // Registers
        s.setReg(reg);
        s.setReg2(reg2);
        s.setRegString(regString);
        s.setReg2String(reg2String);

        // Stacks
        s.setStackTop(stack.size());
        s.setUserFuncStackTop(userCallStack.size());
        s.setCurrentUserFrame(currentUserFrame);

        // Top of program
        s.setCodeSize(getInstructionCount());
        s.setCodeBlockCount(codeBlocks.size());

        // Var data
        Mutable<Integer> tempTop = new Mutable<>(0), tempLock = new Mutable<>(0);
        data.saveState(tempTop, tempLock);
        s.setStackDataTop(tempTop.get());
        s.setTempDataLock(tempLock.get());

        // Error state
        s.setError(hasError());
        if (s.isError()) {
            s.setErrorString(getError());
        } else {
            s.setErrorString("");
        }

        // Other state
        s.setPaused(paused);

        return s;
    }

    public void setState(VMState state) {

        // Instruction pointer
        ip = state.getIp();

        // Registers
        reg = state.getReg();
        reg2 = state.getReg2();
        regString = state.getRegString();
        reg2String = state.getReg2String();

        // Stacks
        if (state.getStackTop() < stack.size()) {
            stack.resize(state.getStackTop());
        }
        if (state.getUserFuncStackTop() < userCallStack.size()) {
            userCallStack.setSize(state.getUserFuncStackTop());
        }
        currentUserFrame = state.getCurrentUserFrame();

        // Top of program
        if (state.getCodeSize() < codeInstructions.size()) {
            codeInstructions.setSize(state.getCodeSize());
        }
        if (state.getCodeBlockCount() < codeBlocks.size()) {
            codeBlocks.setSize(state.getCodeBlockCount());
        }

        // Var data
        unwindTemp();
        unwindStack(state.getStackDataTop());
        data.restoreState(state.getStackDataTop(), state.getTempDataLock(), true);

        // Error state
        if (state.isError()) {
            setError(state.getErrorString());
        } else {
            clearError();
        }

        // Other state
        paused = state.isPaused();
    }

    // Displaying data
    public String basicValToString(Value val, int type, boolean constant) {
        switch (type) {
            case BasicValType.VTP_INT:
                return String.valueOf(val.getIntVal());
            case BasicValType.VTP_REAL:
                return String.valueOf(val.getRealVal());
            case BasicValType.VTP_STRING:
                if (constant) {
                    if (val.getIntVal() >= 0 && val.getIntVal() < stringConstants.size()) {
                        return "\"" + stringConstants.get(val.getIntVal()) + "\"";
                    } else {
                        return "???";
                    }
                } else {
                    return stringStore.isIndexValid(val.getIntVal())
                            ? "\"" + stringStore.getValueAt(val.getIntVal()) + "\""
                            : "???";
                }
            default:
                break;
        }
        return "???";
    }

    // TODO Move to utility class
    static String trimToLength(String str, Mutable<Integer> length) {
        if (str.length() > length.get()) {
            length.set(0);
            return str.substring(0, length.get());
        } else {
            return str;
        }
    }

    void deref(Value val, ValType type) {
        type.pointerLevel--;
        if (type.pointerLevel == 0 && !type.isBasicType()) {

            // Can't follow pointer, as type is not basic (and therefore cannot
            // be loaded into a register)
            // Use value by reference instead
            type.pointerLevel = 1;
            type.isByRef = true;
        } else {

            // Follow pointer
            if (!data.isIndexValid(val.getIntVal())) // DEBUGGING!!!
            {
                assertTrue(data.isIndexValid(val.getIntVal()));
            }
            val.setVal(data.data().get(val.getIntVal()));
        }
    }

    public String valToString(Value val, ValType type, Mutable<Integer> maxChars) {
        assertTrue(dataTypes.isTypeValid(type));
        assertTrue(type.getPhysicalPointerLevel() > 0 || type.isBasicType());

        if (maxChars.get().intValue() <= 0) {
            return "";
        }

        // Basic types
        if (type.isBasicType()) {
            return trimToLength(basicValToString(val, type.basicType, false), maxChars);
        }

        // Pointer types
        if (type.getVirtualPointerLevel() > 0) {

            // Follow pointer down
            if (val.getIntVal() == 0) {
                return trimToLength("[UNSET POINTER]", maxChars);
            }
            deref(val, type);
            return trimToLength("&", maxChars) + valToString(val, type, maxChars);
        }

        // Type is not basic, or a pointer. Must be a value by reference. Either
        // a structure or an array
        assertTrue(type.pointerLevel == 1);
        assertTrue(type.isByRef);
        int dataIndex = val.getIntVal(); // Points to data
        if (dataIndex == 0) {
            return trimToLength("[UNSET]", maxChars);
        }
        String result = "";

        // Arrays
        if (type.arrayLevel > 0) {
            assertTrue(data.isIndexValid(dataIndex));
            assertTrue(data.isIndexValid(dataIndex + 1));

            // Read array header
            int elements = data.data().get(dataIndex).getIntVal();
            int elementSize = data.data().get(dataIndex + 1).getIntVal();
            int arrayStart = dataIndex + 2;

            // Enumerate elements
            result = trimToLength("{", maxChars);
            for (int i = 0; i < elements && maxChars.get() > 0; i++) {
                Value element = new Value(arrayStart + i * elementSize); // Address
                // of
                // element
                ValType elementType = new ValType(type); // Element type.
                elementType.arrayLevel--; // Less one array level.
                elementType.pointerLevel = 1; // Currently have a pointer
                elementType.isByRef = false;

                // Deref to reach data
                deref(element, elementType);

                // Describe element
                result += valToString(element, elementType, maxChars);
                if (i < elements - 1) {
                    result += trimToLength(", ", maxChars);
                }
            }
            result += trimToLength("}", maxChars);
            return result;
        }

        // Structures
        if (type.basicType >= 0) {
            result = trimToLength("{", maxChars);
            Structure structure = dataTypes.getStructures().get(type.basicType);
            for (int i = 0; i < structure.fieldCount && maxChars.get() > 0; i++) {
                StructureField field = dataTypes.getFields().get(structure.firstFieldIndex + i);
                Value fieldVal = new Value(dataIndex + field.dataOffset);
                ValType fieldType = new ValType(field.type);
                fieldType.pointerLevel++;
                deref(fieldVal, fieldType);
                result += trimToLength(field.name + "=", maxChars) + valToString(fieldVal, fieldType, maxChars);
                if (i < structure.fieldCount - 1) {
                    result += trimToLength(", ", maxChars);
                }
            }
            result += trimToLength("}", maxChars);
            return result;
        }

        return "???";
    }

    public String varToString(Variable v, int maxChars) {
        return dataToString(v.dataIndex, v.type, maxChars);
    }

    public String dataToString(int dataIndex, ValType type, int maxChars) {
        Value val = new Value(dataIndex);
        type.pointerLevel++;
        deref(val, type);
        return valToString(val, type, new Mutable<>(maxChars));
    }

    boolean readProgramData(int basicType) {

        // Read program data into register.

        // Check for out-of-data.
        if (programDataOffset >= programData.size()) {
            setError(ERR_OUT_OF_DATA);
            return false;
        }

        // Find program data
        ProgramDataElement e = programData.get(programDataOffset++);

        // Convert to requested type
        switch (basicType) {
            case BasicValType.VTP_STRING:

                // Convert type to int.
                switch (e.getType()) {
                    case BasicValType.VTP_STRING:
                        assertTrue(e.getValue().getIntVal() >= 0);
                        assertTrue(e.getValue().getIntVal() < stringConstants.size());
                        setRegString(stringConstants.get(e.getValue().getIntVal()));
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

    int storedDataSize(int sourceIndex, ValType type) {
        assertTrue(!type.isByRef);

        if (type.pointerLevel == 0 && type.arrayLevel > 0) {
            // Calculate actual array size
            // Array is prefixed by element count and element size.
            return data.data().get(sourceIndex).getIntVal()
                            * data.data().get(sourceIndex + 1).getIntVal()
                    + 2;
        } else {
            return dataTypes.getDataSize(type);
        }
    }

    boolean copyToParam(int sourceIndex, ValType type) {

        // Check source index is valid
        if (!data.isIndexValid(sourceIndex) || sourceIndex == 0) {
            setError(ERR_UNSET_POINTER);
            return false;
        }

        // Calculate data size.
        // Note that the "type" does not specify array dimensions (if type is an
        // array),
        // so they must be read from the source array.
        int size = storedDataSize(sourceIndex, type);

        // Allocate data for parameter on stack
        if (!data.hasStackRoomFor(size)) {
            setError(ERR_USER_FUNC_STACK_OVERFLOW);
            return false;
        }
        int dataIndex = data.allocateStack(size);

        // Block copy the data
        blockCopy(sourceIndex, dataIndex, size);

        // Duplicate any contained strings
        if (dataTypes.containsString(type)) {
            duplicateStrings(dataIndex, type);
        }

        // Store pointer in register
        getReg().setIntVal(dataIndex);

        return true;
    }

    void duplicateStrings(int dataIndex, ValType type) {

        // Called after data is block copied.
        // Strings are stored as pointers to string objects. After a block copy,
        // the source and destination blocks end up pointing to the same string
        // objects.
        // This method traverses the destination block, creates duplicate copies
        // of
        // any contained strings and fixes up the pointers to point to these new
        // string objects.
        assertTrue(dataTypes.containsString(type));
        assertTrue(!type.isByRef);
        assertTrue(type.pointerLevel == 0);

        // Type IS string case
        if (type.matchesType(BasicValType.VTP_STRING)) {

            Value val = data.data().get(dataIndex);
            // Empty strings (index 0) can be ignored
            if (val.getIntVal() != 0) {

                // Allocate new string
                int newStringIndex = stringStore.alloc();

                // Copy previous string
                stringStore.setValue(newStringIndex, stringStore.getValueAt(val.getIntVal()));

                // Point to new string
                val.setIntVal(newStringIndex);
            }
        }

        // Array case
        else if (type.arrayLevel > 0) {

            // Read array header
            int elements = data.data().get(dataIndex).getIntVal();
            int elementSize = data.data().get(dataIndex + 1).getIntVal();
            int arrayStart = dataIndex + 2;

            // Calculate element type
            ValType elementType = new ValType(type);
            elementType.arrayLevel--;

            // Duplicate strings in each array element
            for (int i = 0; i < elements; i++) {
                duplicateStrings(arrayStart + i * elementSize, elementType);
            }
        }

        // Otherwise must be a structure
        else {
            assertTrue(type.basicType >= 0);

            // Find structure definition
            Structure s = dataTypes.getStructures().get(type.basicType);

            // Duplicate strings for each field in structure
            for (int i = 0; i < s.fieldCount; i++) {
                StructureField f = dataTypes.getFields().get(s.firstFieldIndex + i);
                if (dataTypes.containsString(f.type)) {
                    duplicateStrings(dataIndex + f.dataOffset, f.type);
                }
            }
        }
    }

    boolean moveToTemp(int sourceIndex, ValType type) {

        // Free temp data
        // Note: We can do this because we know that the data isn't really
        // freed,
        // just marked as free. This is significant, because the data we are
        // moving
        // into the temp data area may be in temp-data already.

        // Special handling is required if data being copied is already in the
        // temp region
        boolean sourceIsTemp = sourceIndex > 0 && sourceIndex < data.getTempData();

        // Destroy temp data.
        // However, if the data being copied is in temp data, we use a
        // protected-stack-range to prevent it being destroyed.
        if (sourceIsTemp) {
            unwindTemp(new ProtectedStackRange(sourceIndex, sourceIndex + storedDataSize(sourceIndex, type)));
        } else {
            unwindTemp();
        }

        // Free the data
        data.freeTempData();

        // Calculate data size.
        // Note that the "type" does not specify array dimensions (if type is an
        // array),
        // so they must be read from the source array.
        int size = storedDataSize(sourceIndex, type);

        // Allocate data for parameter on stack
        if (!data.hasStackRoomFor(size)) {
            setError(ERR_USER_FUNC_STACK_OVERFLOW);
            return false;
        }
        int dataIndex = data.allocateTemp(size, false);

        // Block copy the data
        blockCopy(sourceIndex, dataIndex, size);

        // Extra logic required to manage strings
        if (dataTypes.containsString(type)) {

            // If source was NOT temp data, then the object has been copied,
            // rather
            // than moved, and we must duplicate all the contained string
            // objects.
            if (!sourceIsTemp) {
                duplicateStrings(dataIndex, type);
            }
        }

        // Store pointer in register
        getReg().setIntVal(dataIndex);

        return true;
    }

    void unwindTemp() {
        unwindTemp(new ProtectedStackRange());
    }

    void unwindTemp(ProtectedStackRange protect) {
        int newTop = data.getTempDataLock();

        // Run destrution logic over data that is about to be deallocated.
        while (!tempDestructors.isEmpty() && tempDestructors.lastElement().addr >= newTop) {
            destroyData(tempDestructors.lastElement(), protect);
            tempDestructors.remove(tempDestructors.size() - 1);
        }

        // Note: We don't actually remove the data from the stack. Calling code
        // must
        // handle that instead.
    }

    void unwindStack(int newTop) {

        // Run destruction logic over data that is about to be deallocated.
        while (!stackDestructors.isEmpty() && stackDestructors.lastElement().addr < newTop) {
            destroyData(stackDestructors.lastElement(), new ProtectedStackRange());
            stackDestructors.remove(stackDestructors.size() - 1);
        }

        // Note: We don't actually remove the data from the stack. Calling code
        // must
        // handle that instead.
    }

    void destroyData(StackDestructor d, ProtectedStackRange protect) {
        // Apply destructor logic to data block.
        destroyData(d.addr, typeSet.getValType(d.dataTypeIndex), protect);
    }

    void destroyData(int index, ValType type, ProtectedStackRange protect) {
        assertTrue(dataTypes.containsString(type));
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
            int stringIndex = data.data().get(index).getIntVal();
            if (stringIndex != 0) {
                stringStore.freeAtIndex(stringIndex);
            }
        } else if (type.arrayLevel > 0) {

            // Array case
            ValType elementType = new ValType(type);
            elementType.arrayLevel--;
            int count = data.data().get(index).getIntVal();
            int elementSize = data.data().get(index + 1).getIntVal();
            int arrayStart = index + 2;

            // Don't destroy if in protected range
            if (protect.containsRange(index, arrayStart + count * elementSize)) {
                return;
            }

            // Recursively destroy each element
            for (int i = 0; i < count; i++) {
                destroyData(arrayStart + i * elementSize, elementType, protect);
            }
        } else {

            // At this point we know the type contains a string and is not a
            // string
            // or array.
            // Can only be a structure.
            assertTrue(type.pointerLevel == 0);
            assertTrue(type.basicType >= 0);

            // Recursively destroy each structure field (that contains a string)
            Structure s = dataTypes.getStructures().get(type.basicType);

            // Don't destroy if in protected range
            if (protect.containsRange(index, index + s.dataSize)) {
                return;
            }

            for (int i = 0; i < s.fieldCount; i++) {

                // Get field info
                StructureField f = dataTypes.getFields().get(s.firstFieldIndex + i);

                // Destroy if contains string(s)
                if (dataTypes.containsString(f.type)) {
                    destroyData(index + f.dataOffset, f.type, protect);
                }
            }
        }
    }

    public int newCodeBlock() {
        codeBlocks.add(new CodeBlock());

        // Set pointer to code
        getCurrentCodeBlock().programOffset = codeInstructions.size();

        // Bind code block
        boundCodeBlock = codeBlocks.size() - 1;

        // Return index of new code block
        return boundCodeBlock;
    }

    public CodeBlock getCurrentCodeBlock() {
        assertTrue(!codeBlocks.isEmpty());
        return codeBlocks.lastElement();
    }

    public int getCurrentCodeBlockIndex() {
        assertTrue(!codeBlocks.isEmpty());
        return codeBlocks.size() - 1;
    }

    public boolean isCodeBlockValid(int index) {
        return index >= 0 && index < codeBlocks.size();
    }

    public int getCodeBlockOffset(int index) {
        assertTrue(isCodeBlockValid(index));
        return codeBlocks.get(index).programOffset;
    }

    public RollbackPoint getRollbackPoint() {
        RollbackPoint r = new RollbackPoint();

        r.codeBlockCount = codeBlocks.size();
        r.boundCodeBlock = boundCodeBlock;
        r.functionPrototypeCount = userFunctionPrototypes.size();
        r.functionCount = userFunctions.size();
        r.dataCount = programData.size();
        r.instructionCount = codeInstructions.size();

        return r;
    }

    public void rollback(RollbackPoint rollbackPoint) {

        // Rollback virtual machine
        codeBlocks.setSize(rollbackPoint.codeBlockCount);
        boundCodeBlock = rollbackPoint.boundCodeBlock;
        userFunctionPrototypes.setSize(rollbackPoint.functionPrototypeCount);
        userFunctions.setSize(rollbackPoint.functionCount);
        programData.setSize(rollbackPoint.dataCount);
        codeInstructions.setSize(rollbackPoint.instructionCount);
    }

    // Streaming
    public void streamOut(DataOutputStream stream) throws IOException {
        int i;
        // Stream header
        Streaming.writeString(stream, STREAM_HEADER);
        Streaming.writeLong(stream, STREAM_VERSION);

        // Plugin DLLs
        // TODO Reimplement libraries
        // this.plugins.StreamOut(stream);

        // Variables
        variables.streamOut(stream); // Note: mVariables automatically
        // streams out mDataTypes

        // String constants
        Streaming.writeLong(stream, stringConstants.size());
        for (i = 0; i < stringConstants.size(); i++) {
            Streaming.writeString(stream, stringConstants.get(i));
        }

        // Data type lookup table
        typeSet.streamOut(stream);

        // Program code
        Streaming.writeLong(stream, codeInstructions.size());
        for (i = 0; i < codeInstructions.size(); i++) {
            codeInstructions.get(i).streamOut(stream);
        }

        // Program data (for "DATA" statements)
        Streaming.writeLong(stream, programData.size());
        for (i = 0; i < programData.size(); i++) {
            programData.get(i).streamOut(stream);
        }

        // User function prototypes
        Streaming.writeLong(stream, userFunctionPrototypes.size());
        for (i = 0; i < userFunctionPrototypes.size(); i++) {
            userFunctionPrototypes.get(i).streamOut(stream);
        }

        // User functions
        Streaming.writeLong(stream, userFunctions.size());
        for (i = 0; i < userFunctions.size(); i++) {
            userFunctions.get(i).streamOut(stream);
        }

        // Code blocks
        Streaming.writeLong(stream, codeBlocks.size());
        for (i = 0; i < codeBlocks.size(); i++) {
            codeBlocks.get(i).streamOut(stream);
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
        // TODO Reimplement libraries
        /*
        if (!this.plugins.StreamIn(stream)) {
        	setError(this.plugins.Error());
        	return false;
        }

        // Register plugin structures and functions in VM
        this.plugins.StructureManager().AddVMStructures(DataTypes());
        this.plugins.CreateVMFunctionSpecs();
        */

        // Variables
        variables.streamIn(stream);

        // String constants
        int count, i;
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            stringConstants.setSize(count);
            for (i = 0; i < count; i++) {
                stringConstants.set(i, Streaming.readString(stream));
            }

            // Data type lookup table
            typeSet.streamIn(stream);
        }
        // Program code
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            codeInstructions.setSize(count);
            for (i = 0; i < count; i++) {
                codeInstructions.set(i, new Instruction());
                codeInstructions.get(i).streamIn(stream);
            }
        }
        // Program data (for "DATA" statements)
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            programData.setSize(count);
            for (i = 0; i < count; i++) {
                programData.set(i, new ProgramDataElement());
                programData.get(i).streamIn(stream);
            }
        }
        // User function prototypes
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            userFunctionPrototypes.setSize(count);
            for (i = 0; i < count; i++) {
                userFunctionPrototypes.set(i, new UserFuncPrototype());
                userFunctionPrototypes.get(i).streamIn(stream);
            }
        }
        // User functions
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            userFunctions.setSize(count);
            for (i = 0; i < count; i++) {
                userFunctions.set(i, new UserFunc());
                userFunctions.get(i).streamIn(stream);
            }
        }
        // Code blocks
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            codeBlocks.setSize(count);
            for (i = 0; i < count; i++) {
                codeBlocks.set(i, new CodeBlock());
                codeBlocks.get(i).streamIn(stream);
            }
        }
        return true;
    }

    void patchOut() {
        if (breakPointsPatched) {
            internalPatchOut();
        }
    }

    // General
    public boolean isDone() {
        assertTrue(isIPValid());
        return stopped || codeInstructions.get(ip).opCode == OpCode.OP_END; // Reached end of
        // program?
    }

    public boolean isRunning() {
        return !isDone() && !isPaused();
    }

    public void stop() {
        stopped = true;
    }

    public void getIPInSourceCode(Mutable<Integer> line, Mutable<Integer> col) {
        assertTrue(isIPValid());
        line.set(codeInstructions.get(ip).sourceLine);
        col.set(codeInstructions.get(ip).sourceChar);
    }

    public InstructionPosition getIPInSourceCode() {
        assertTrue(isIPValid());
        return new InstructionPosition(codeInstructions.get(ip).sourceLine, codeInstructions.get(ip).sourceChar);
    }

    public void bindCodeBlock(int index) {
        boundCodeBlock = index;
    }

    public int getBoundCodeBlock() {
        return boundCodeBlock;
    }

    // IP and registers
    public int getIP() {
        return ip;
    }

    public Value getReg() {
        return reg;
    }

    public Value getReg2() {
        return reg2;
    }

    public void setReg(Value value) {
        reg.setVal(value);
    }

    public void setReg2(Value value) {
        reg2.setVal(value);
    }

    public String getRegString() {
        return regString;
    }

    public String getReg2String() {
        return reg2String;
    }

    public void setRegString(String string) {
        regString = string;
    }

    public void setReg2String(String string) {
        reg2String = string;
    }

    public ValueStack getStack() {
        return stack;
    }

    public void setStack(ValueStack stack) {
        this.stack = stack;
    }

    // Variables, data and data types
    public TypeLibrary getDataTypes() {
        return dataTypes;
    }

    public Data getData() {
        return data;
    }

    public VariableCollection getVariables() {
        return variables;
    }

    public Vector<ProgramDataElement> getProgramData() {
        return programData;
    }

    // User functions
    public Vector<UserFuncPrototype> getUserFunctionPrototypes() {
        return userFunctionPrototypes;
    }

    public Vector<UserFunc> getUserFunctions() {
        return userFunctions;
    }

    public Vector<UserFuncStackFrame> getUserCallStack() {
        return userCallStack;
    }

    public int getCurrentUserFrame() {
        return currentUserFrame;
    }

    // Debugging
    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused = true;
    }

    public boolean isBreakPointsPatched() {
        return breakPointsPatched;
    }

    public void clearTempBreakPoints() {
        patchOut();
        tempBreakPoints.clear();
    }

    public void patchIn() {
        if (!breakPointsPatched) {
            internalPatchIn();
        }
    }

    public void repatchBreakpoints() {
        patchOut();
        patchIn();
    }

    public void gotoInstruction(int offset) {
        assertTrue(isOffsetValid(offset));
        ip = offset;
    }

    public boolean skipInstruction() { // USE WITH CARE!!!
        if (ip < getInstructionCount() + 1) {
            ip++; // Proceed to next instruction
            return true;
        } else {
            return false;
        }
    }

    public boolean isOffsetValid(int offset) {
        return offset >= 0 && offset < getInstructionCount();
    }

    public boolean isIPValid() {
        return isOffsetValid(ip);
    }

    // Building raw VM instructions
    public int getInstructionCount() {
        return codeInstructions.size();
    }

    public void addInstruction(Instruction i) {
        patchOut();
        codeInstructions.add(i);
    }

    public void rollbackProgram(int size) {
        assertTrue(size >= 0);
        assertTrue(size <= getInstructionCount());
        while (size < getInstructionCount()) {
            codeInstructions.remove(codeInstructions.size() - 1);
        }
    }

    public Instruction getInstruction(int index) {
        assertTrue(index < codeInstructions.size());
        patchOut();
        return codeInstructions.get(index);
    }

    public void setInstruction(int index, Instruction instruction) {
        assertTrue(index < codeInstructions.size());
        patchOut();
        codeInstructions.set(index, instruction);
    }

    public void removeLastInstruction() {
        codeInstructions.remove(codeInstructions.size() - 1);
    }

    public int getStoreTypeIndex(ValType type) {
        return typeSet.getIndex(type);
    }

    public ValType getStoredType(int index) {
        return typeSet.getValType(index);
    }

    // Program data
    public void storeProgramData(int type, Value v) {
        ProgramDataElement d = new ProgramDataElement();
        d.setType(type);
        d.setValue(v);
        programData.add(d);
    }

    public Vector<String> getStringConstants() {
        return stringConstants;
    }

    // External functions
    public int addFunction(Function func) {
        int result = getFunctionCount();
        functions.add(func);
        return result;
    }

    public int getFunctionCount() {
        return functions.size();
    }

    public int addOperatorFunction(Function func) {
        int result = getOperatorFunctionCount();
        operatorFunctions.add(func);
        return result;
    }

    public int getOperatorFunctionCount() {
        return operatorFunctions.size();
    }

    // Called by external functions
    public Value getParam(int index) {
        // Read param from param stack.
        // Index 1 is TOS
        // Index 2 is TOS - 1
        // ...
        assertTrue(index > 0);
        assertTrue(index <= stack.size());
        return stack.get(stack.size() - index);
    }

    public Integer getIntParam(int index) {
        return getParam(index).getIntVal();
    }

    public Float getRealParam(int index) {
        return getParam(index).getRealVal();
    }

    public String getStringParam(int index) {
        return stringStore.getValueAt(getIntParam(index));
    }

    public void setStringParam(int index, String string) {
        stringStore.setValue(getIntParam(index), string);
    }

    public String getString(int index) {
        return stringStore.getValueAt(index);
    }

    public void setString(int index, String string) {
        stringStore.setValue(index, string);
    }

    public int allocString() {
        return stringStore.alloc();
    }

    public int getStringStoreElementCount() {
        return stringStore.getStoredElements();
    }

    public Store<String> getStringStore() {
        return stringStore;
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
        assertTrue(data.isIndexValid(ptr));
        return data.data().get(ptr);
    }

    public void functionError(String name) {
        setError("Function error: " + name);
    }

    public void miscError(String name) {
        setError(name);
    }

    // Initialisation functions
    public void addInitFunction(Function func) {
        initFunctions.add(func);
    }

    // Resources
    public void addResources(Resources resources) {
        this.resources.add(resources);
    }

    // Plugin DLLs
    // TODO Reimplement libraries
    /*PluginDLLManager& Plugins() { return m_plugins; }*/
    // Builtin/plugin function callback support
    public boolean isEndCallback() {
        assertTrue(isIPValid());
        return codeInstructions.get(ip).opCode == OpCode.OP_END_CALLBACK; // Reached end callback opcode?
    }
}
