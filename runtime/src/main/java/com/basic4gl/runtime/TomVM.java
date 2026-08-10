package com.basic4gl.runtime;

import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

import com.basic4gl.runtime.VariableCollection.Variable;
import com.basic4gl.runtime.plugin.Basic4GLRuntime;
import com.basic4gl.runtime.plugin.PluginManager;
import com.basic4gl.runtime.plugin.TomVMPluginAdapter;
import com.basic4gl.runtime.stackframe.*;
import com.basic4gl.runtime.types.*;
import com.basic4gl.runtime.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            ERR_FUNC_PTR_INCOMPATIBLE = "Function/sub pointer is incompatible";
    // External functions
    /**
     * functions are standard functions where the parameters are pushed to the stack.
     */
    public ArrayList<Function> functions;
    /**
     * Initialisation functions
     */
    private final ArrayList<Function> initFunctions;
    // Registers
    /**
     * Register values (when int or float)
     */
    //    private Value reg, reg2;

    private int regValue, reg2Value;
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
    private final ArrayList<UserFuncStackFrame> userCallStack;

    private static final int MAX_FRAME_POOL_SIZE = 1024;
    private final ArrayList<UserFuncStackFrame> framePool = new ArrayList<>();

    // Individual code blocks
    private final ArrayList<CodeBlock> codeBlocks;
    private int boundCodeBlock;

    // Data destruction
    private final ArrayList<StackDestructor> stackDestructors;
    private final ArrayList<StackDestructor> tempDestructors;

    // Plugins
    private final PluginManager plugins;
    private final Basic4GLRuntime pluginRuntime;

    // Debugger
    private final IVMDebugger debugger;

    // Variables, data and data types
    private final TypeLibrary dataTypes;
    private final Data data;
    private final VariableCollection variables;
    /**
     * Constant strings declared in program
     */
    private final ArrayList<String> stringConstants;

    private final Store<String> stringStore;
    private final List<Resources> resources;
    private final ArrayList<UserFuncPrototype> userFunctionPrototypes;
    private final ArrayList<UserFunc> userFunctions;

    // Program data

    /**
     * General purpose program data
     * <p>(e.g declared with "DATA" keyword in BASIC)
     */
    private final ArrayList<ProgramDataElement> programData;

    private int programDataOffset;

    // Instructions
    private final ArrayList<Instruction> codeInstructions;
    private int[] codeInstructionOpCodes;
    private int[] codeInstructionValues;
    private int[] codeInstructionVarTypes;
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

    private Basic4GLLongRunningFunction longRunningFunction;
    private ILongRunningFunctionListener longRunningFnDoneNotifiedListener;

    /**
     * Conditional timeshare break
     */
    private boolean timeshare;

    public TomVM(PluginManager plugins, IVMDebugger debugger) {
        this(plugins, debugger, MAX_DATA, MAX_STACK);
    }

    public TomVM(PluginManager plugins, IVMDebugger debugger, int maxDataSize, int maxStackSize) {
        this.debugger = debugger;

        data = new Data(maxDataSize, maxStackSize);
        dataTypes = new TypeLibrary();
        variables = new VariableCollection(data, dataTypes);

        regValue = 0;
        reg2Value = 0;

        resources = new ArrayList<>();

        stringStore = new Store<>("");
        stack = new ValueStack(stringStore);
        userCallStack = new ArrayList<>();
        stackDestructors = new ArrayList<>();
        tempDestructors = new ArrayList<>();

        programData = new ArrayList<>();
        codeBlocks = new ArrayList<>();
        stringConstants = new ArrayList<>();

        typeSet = new ValTypeSet();

        codeInstructions = new ArrayList<>();
        codeInstructionOpCodes = new int[0];
        codeInstructionVarTypes = new int[0];
        codeInstructionValues = new int[0];

        functions = new ArrayList<>();
        initFunctions = new ArrayList<>();
        userFunctions = new ArrayList<>();
        userFunctionPrototypes = new ArrayList<>();
        patchedBreakPoints = new ArrayList<>();
        tempBreakPoints = new ArrayList<>();

        this.plugins = plugins;
        // Create plugin runtime
        this.pluginRuntime = new TomVMPluginAdapter(this, this.plugins.getStructureManager());

        clearProgram();
    }

    /**
     * New program
     */
    public void clearProgram() {
        // Cancel any long running function
        cancelLongRunningFunction();

        // Clear variables, data and data types
        clearVariables();
        variables.clear();
        variableDataIndexes = new int[0];
        dataTypes.clear();
        programData.clear();
        codeBlocks.clear();

        // Clear string constants
        stringConstants.clear();

        // Deallocate code
        codeInstructions.clear();
        codeInstructionVarTypes = new int[0];
        codeInstructionValues = new int[0];
        codeInstructionOpCodes = new int[0];

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
        Arrays.fill(variableDataIndexes, 0);

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
        regValue = (0);
        reg2Value = (0);
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

    Function[] functionArr;
    int[] variableDataIndexes = new int[0];

    private UserFuncStackFrame acquireFrame() {
        int size = framePool.size();

        if (size == 0) {
            return new UserFuncStackFrame();
        }

        return framePool.remove(size - 1);
    }

    private void releaseFrame(UserFuncStackFrame frame) {
        if (frame == null) {
            return;
        }

        frame.resetForReuse();

        if (framePool.size() < MAX_FRAME_POOL_SIZE) {
            framePool.add(frame);
        }
    }

    public void continueVM() {
        // Reduced from 0xffffffff since Java doesn't support unsigned ints
        continueVM(0x7fffffff);
    }

    public void continueVM(int steps) // Continue execution from last position
            {

        clearError();
        paused = false;
        timeshare = false;

        //        Instruction instruction;
        int instructionOpCode;
        int instructionVarType;
        int instructionValue;
        int stepCount = 0;
        int tempI;
        UserFuncStackFrame stackFrame;

        // Handle long running functions
        if (longRunningFunction != null) {
            if (longRunningFunction.isPolled()) {
                longRunningFunction.poll();
            }
            return;
        }

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

            //            instruction = codeInstructions.get(ip);
            instructionOpCode = codeInstructionOpCodes[ip];
            instructionVarType = codeInstructionVarTypes[ip];
            instructionValue = codeInstructionValues[ip];
            switch (instructionOpCode) {
                case OpCode.OP_NOP:
                    ip++; // Proceed to next instruction
                    continue step;
                case OpCode.OP_END:
                    break;
                case OpCode.OP_LOAD_CONST:

                    // Load value
                    if (instructionVarType == BasicValType.VTP_STRING) {
                        // assertTrue(instructionValue >= 0);
                        // assertTrue(instructionValue < stringConstants.size());
                        setRegString(stringConstants.get(instructionValue));
                    } else {
                        regValue = (instructionValue);
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_LOAD_VAR: {

                    // Load variable.
                    // Instruction contains index of variable.
                    // assertTrue(variables.isIndexValid(instructionValue));
                    //                    Variable var = variables.getVariables().get(instructionValue);
                    int variableDataIndex = variableDataIndexes[instructionValue];
                    if (variableDataIndex != 0) {
                        // Load address of variable's data into register
                        regValue = (variableDataIndex);
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNDIMMED_VARIABLE);
                    break;
                }

                case OpCode.OP_LOAD_LOCAL_VAR: {

                    // Find current stack frame
                    // assertTrue(currentUserFrame >= 0);
                    // assertTrue(currentUserFrame < userCallStack.size());
                    UserFuncStackFrame currentFrame = userCallStack.get(currentUserFrame);

                    // Find variable
                    int index = instructionValue;

                    // Instruction contains index of variable.
                    if (currentFrame.localVarDataOffsets.get(index) != 0) {
                        // Load address of variable's data into register
                        regValue = (currentFrame.localVarDataOffsets.get(index));
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNDIMMED_VARIABLE);
                    break;
                }

                case OpCode.OP_DEREF: {

                    // Dereference reg.
                    if (regValue != 0) {
                        int regAddr = regValue;
                        // assertTrue(data.isIndexValid(regAddr));
                        // Find value that reg points to
                        int rawValue = data.data().getIntValue(regAddr);
                        switch (instructionVarType) {
                            case BasicValType.VTP_INT:
                            case BasicValType.VTP_REAL:
                            case BasicValType.VTP_FUNC_PTR:
                                regValue = (rawValue);
                                ip++; // Proceed to next instruction
                                continue step;
                            case BasicValType.VTP_STRING:
                                // assertTrue(stringStore.isIndexValid(rawValue));
                                setRegString(stringStore.getValueAt(rawValue));
                                ip++; // Proceed to next instruction
                                continue step;
                            default:
                                break;
                        }
                        // assertTrue(false);
                    }
                    setError(ERR_UNSET_POINTER);
                    break;
                }
                case OpCode.OP_ADD_CONST:
                    // Check pointer
                    if (regValue != 0) {
                        regValue = (regValue + instructionValue);
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    setError(ERR_UNSET_POINTER);
                    break;

                case OpCode.OP_ARRAY_INDEX:
                    if (reg2Value != 0) {
                        // Input: mReg2 = Array address
                        // mReg = Array index
                        // Output: mReg = Element address
                        // assertTrue(data.isIndexValid(reg2Value));
                        // assertTrue(data.isIndexValid(reg2Value + 1));

                        // mReg2 points to array header (2 values)
                        // First value is highest element (i.e number of elements +
                        // 1)
                        // Second value is size of array element.
                        // Array data immediately follows header
                        int arrayHeader = reg2Value;
                        int elementCount = data.data().getIntValue(arrayHeader);
                        int elementSize = data.data().getIntValue(arrayHeader + 1);
                        if (regValue >= 0 && regValue < elementCount) {
                            // assertTrue(elementSize >= 0);
                            regValue = (reg2Value + 2 + regValue * elementSize);

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
                    if (instructionVarType == BasicValType.VTP_STRING) {
                        stack.pushString(getRegString());
                    } else {
                        stack.push(regValue);
                    }

                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_POP:

                    // Pop reg2 from stack
                    if (instructionVarType == BasicValType.VTP_STRING) {

                        setReg2String(stack.popString());
                    } else {
                        reg2Value = stack.pop();
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_SAVE: {

                    // Save reg into [reg2]
                    if (reg2Value > 0) {
                        int destAddr = reg2Value;
                        // assertTrue(data.isIndexValid(destAddr));
                        switch (instructionVarType) {
                            case BasicValType.VTP_INT:
                            case BasicValType.VTP_REAL:
                            case BasicValType.VTP_FUNC_PTR:
                                // mData.Data().set(mReg2.getIntVal(), new Value(mReg));
                                data.data().setIntValue(destAddr, regValue);

                                ip++; // Proceed to next instruction
                                continue step;
                            case BasicValType.VTP_STRING:

                                // Allocate string space if necessary
                                int stringIndex = data.data().getIntValue(destAddr);
                                if (stringIndex == 0) {
                                    stringIndex = stringStore.alloc();
                                    data.data().setIntValue(destAddr, stringIndex);
                                }

                                // Copy string value
                                stringStore.setValue(stringIndex, getRegString());
                                ip++; // Proceed to next instruction
                                continue step;
                            default:
                                break;
                        }
                        // assertTrue(false);
                    }
                    setError(ERR_UNSET_POINTER);
                    break;
                }

                case OpCode.OP_COPY: {

                    // Copy data
                    if (copyData(regValue, reg2Value, typeSet.getValType(instructionValue))) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }
                case OpCode.OP_DECLARE: {

                    // Allocate variable.
                    // assertTrue(variables.isIndexValid(instructionValue));
                    Variable var = variables.getVariables().get(instructionValue);

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
                    if (instructionValue >= variableDataIndexes.length) {
                        rebuildVariableDataIndexes();
                    } else {
                        variableDataIndexes[instructionValue] = var.dataIndex;
                    }

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_DECLARE_LOCAL: {

                    // Allocate local variable

                    // Find current stack frame
                    // assertTrue(currentUserFrame >= 0);
                    // assertTrue(currentUserFrame < userCallStack.size());
                    UserFuncStackFrame currentFrame = userCallStack.get(currentUserFrame);
                    UserFunc userFunc = userFunctions.get(currentFrame.userFuncIndex);
                    UserFuncPrototype prototype = userFunctionPrototypes.get(userFunc.prototypeIndex);

                    // Find variable type
                    int index = instructionValue;
                    // assertTrue(index >= 0);
                    // assertTrue(index < prototype.localVarTypes.size());
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
                    regValue = (dataIndex);

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_JUMP:

                    // Jump
                    // assertTrue(instructionValue >= 0);
                    // assertTrue(instructionValue < codeInstructions.size());
                    ip = instructionValue;
                    continue step; // Proceed without incrementing instruction

                case OpCode.OP_JUMP_TRUE:

                    // Jump if reg != 0
                    // assertTrue(instructionValue >= 0);
                    // assertTrue(instructionValue < codeInstructions.size());
                    if (regValue != 0) {
                        ip = instructionValue;
                        continue step; // Proceed without incrementing instruction
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_JUMP_FALSE:

                    // Jump if reg == 0
                    // assertTrue(instructionValue >= 0);
                    // assertTrue(instructionValue < codeInstructions.size());
                    if (regValue == 0) {
                        ip = instructionValue;
                        continue step; // Proceed without incrementing instruction
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_NEG:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (-regValue);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = floatToRawIntBits(-intBitsToFloat(regValue));
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_PLUS:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (regValue + reg2Value);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = floatToRawIntBits(intBitsToFloat(regValue) + intBitsToFloat(reg2Value));
                    } else if (instructionVarType == BasicValType.VTP_STRING) {
                        setRegString(getReg2String() + getRegString());
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_MINUS:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (reg2Value - regValue);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = floatToRawIntBits(intBitsToFloat(reg2Value) - intBitsToFloat(regValue));
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_TIMES:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (regValue * reg2Value);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {

                        regValue = floatToRawIntBits(intBitsToFloat(regValue) * intBitsToFloat(reg2Value));
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_DIV:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (reg2Value / regValue);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = floatToRawIntBits(intBitsToFloat(reg2Value) / intBitsToFloat(regValue));
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_MOD:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        int i = reg2Value % regValue;
                        if (i >= 0) {
                            regValue = (i);
                        } else {
                            regValue = (regValue + i);
                        }
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_NOT:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (regValue == 0 ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_EQUAL:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (reg2Value == regValue ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = (intBitsToFloat(reg2Value) == intBitsToFloat(regValue) ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_STRING) {
                        regValue = (getReg2String().equals(getRegString()) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_NOT_EQUAL:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (reg2Value != regValue ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = (intBitsToFloat(reg2Value) != intBitsToFloat(regValue) ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_STRING) {
                        regValue = (!getReg2String().equals(getRegString()) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_GREATER:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (reg2Value > regValue ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = (intBitsToFloat(reg2Value) > intBitsToFloat(regValue) ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_STRING) {
                        regValue = ((getReg2String().compareTo(getRegString()) > 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_GREATER_EQUAL:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (reg2Value >= regValue ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = (intBitsToFloat(reg2Value) >= intBitsToFloat(regValue) ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_STRING) {
                        regValue = ((getReg2String().compareTo(getRegString()) >= 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_LESS:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (reg2Value < regValue ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = (intBitsToFloat(reg2Value) < intBitsToFloat(regValue) ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_STRING) {
                        regValue = ((getReg2String().compareTo(getRegString()) < 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_LESS_EQUAL:
                    if (instructionVarType == BasicValType.VTP_INT) {
                        regValue = (reg2Value <= regValue ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_REAL) {
                        regValue = (intBitsToFloat(reg2Value) <= intBitsToFloat(regValue) ? -1 : 0);
                    } else if (instructionVarType == BasicValType.VTP_STRING) {
                        regValue = ((getReg2String().compareTo(getRegString()) <= 0) ? -1 : 0);
                    } else {
                        setError(ERR_BAD_OPERATOR);
                        break;
                    }
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_REAL:
                    regValue = floatToRawIntBits((float) regValue);
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_REAL2:
                    reg2Value = floatToRawIntBits((float) reg2Value);
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_INT:
                    regValue = ((int) intBitsToFloat(regValue));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_INT2:
                    reg2Value = ((int) intBitsToFloat(reg2Value));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_STRING:
                    setRegString(String.valueOf(regValue));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_STRING:
                    setRegString(String.valueOf(intBitsToFloat(regValue)));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_INT_STRING2:
                    setReg2String(String.valueOf(reg2Value));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CONV_REAL_STRING2:
                    setReg2String(String.valueOf(intBitsToFloat(reg2Value)));
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_AND:
                    regValue = (regValue & reg2Value);
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_OR:
                    regValue = (regValue | reg2Value);
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_OP_XOR:
                    regValue = (regValue ^ reg2Value);
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_CALL_FUNC:
                    // assertTrue(instructionValue >= 0);
                    // assertTrue(instructionValue < functions.size());

                    // Call external function
                    functionArr[instructionValue].run(this);

                    if (!hasError()) {
                        ip++; // Proceed to next instruction
                        continue step;
                    }
                    break;

                case OpCode.OP_TIMESHARE:
                    ip++; // Move on to next instruction
                    break; // And return

                case OpCode.OP_COND_TIMESHARE:
                    if (!timeshare) {
                        // If no timeshare flagged, continue executing
                        ip++; // Proceed to next instruction
                        continue step;
                    }
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
                    ValType type = new ValType(typeSet.getValType(instructionValue));
                    if (!popArrayDimensions(type)) {
                        break;
                    }

                    // Validate type size
                    if (!validateTypeSize(type)) {
                        break;
                    }

                    // Allocate and initialise new data
                    regValue = (data.allocate(dataTypes.getDataSize(type)));
                    data.initData(regValue, type, dataTypes);

                    ip++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_CALL: {

                    // Call
                    // assertTrue(instructionValue >= 0);
                    // assertTrue(instructionValue < codeInstructions.size());

                    // Check for stack overflow
                    if (userCallStack.size() >= MAX_USER_STACK_CALLS) {
                        setError(ERR_STACK_OVERFLOW);
                        break;
                    }

                    // Push stack frame, with return address
                    stackFrame = acquireFrame();
                    stackFrame.initForGosub(ip + 1);
                    userCallStack.add(stackFrame);

                    // Jump to subroutine
                    ip = instructionValue;
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
                    // assertTrue(CollectionUtil.last(userCallStack).userFuncIndex == -1);

                    int top = userCallStack.size() - 1;
                    stackFrame = userCallStack.get(top);

                    // Optional defensive check. Existing code treats this as impossible.
                    if (stackFrame.userFuncIndex != -1) {
                        setError(ERR_STACK_ERROR);
                        break;
                    }

                    tempI = stackFrame.returnAddr;
                    userCallStack.remove(top);
                    releaseFrame(stackFrame);

                    if (tempI >= codeInstructions.size()) {
                        setError(ERR_STACK_ERROR);
                        break;
                    }

                    // Jump to return address
                    ip = tempI;
                    continue step; // Proceed without incrementing instruction

                case OpCode.OP_CALL_DLL: {

                    // Call plugin function
                    int index = instructionValue;
                    this.plugins
                            .getLoadedLibraries()
                            .get(index >> 24)
                            .getFunction(index & 0x00ffffff)
                            .run(this.pluginRuntime);

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
                    int funcIndex = instructionValue;

                    stackFrame = acquireFrame();
                    stackFrame.initForUserFunction(
                            userFunctionPrototypes.get(userFunctions.get(funcIndex).prototypeIndex), funcIndex);
                    userCallStack.add(stackFrame);

                    // Save previous stack frame data
                    Mutable<Integer> tempTop = new Mutable<>(0);
                    Mutable<Integer> tempLock = new Mutable<>(0);
                    data.saveState(tempTop, tempLock);
                    stackFrame.prevStackTop = tempTop.get();
                    stackFrame.prevTempDataLock = tempLock.get();

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_CREATE_FUNC_PTR_FRAME: {

                    // This is identical to OP_CREATE_USER_FRAME, except that the function
                    // index will be in reg, rather than the instruction.

                    // Check for null function pointer
                    if (regValue == 0) {
                        setError(ERR_UNSET_POINTER);
                        break;
                    }

                    // Check for stack overflow
                    if (userCallStack.size() >= MAX_USER_STACK_CALLS) {
                        setError(ERR_STACK_OVERFLOW);
                        break;
                    }

                    // Function index + 1 is in reg
                    // (+1 is so that we can use 0 for null)
                    int funcIndex = regValue - 1;

                    stackFrame = acquireFrame();
                    stackFrame.initForUserFunction(
                            userFunctionPrototypes.get(userFunctions.get(funcIndex).prototypeIndex), funcIndex);
                    userCallStack.add(stackFrame);

                    // Save previous stack frame data
                    Mutable<Integer> stackTopRef = new Mutable<>(0);
                    Mutable<Integer> tempDataLockRef = new Mutable<>(0);
                    data.saveState(stackTopRef, tempDataLockRef);
                    stackFrame.prevStackTop = stackTopRef.get();
                    stackFrame.prevTempDataLock = tempDataLockRef.get();

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_CHECK_FUNC_PTR: {

                    // Function pointer can be null (0)
                    if (regValue == 0) {
                        ip++; // Proceed to next instruction
                        continue step;
                    }

                    // Function index + 1 is in reg
                    // (+1 is so that we can use 0 for null)
                    int funcIndex = regValue - 1;

                    // Check function prototype is compatible with prototype referenced by instruction
                    UserFuncPrototype srcProto =
                            userFunctionPrototypes.get(userFunctions.get(funcIndex).prototypeIndex);
                    UserFuncPrototype dstProto = userFunctionPrototypes.get(instructionValue);

                    if (!srcProto.isCompatibleWith(dstProto)) {
                        setError(ERR_FUNC_PTR_INCOMPATIBLE);
                        break;
                    }

                    ip++; // Proceed to next instruction
                    continue step;
                }
                case OpCode.OP_CREATE_RUNTIME_FRAME: {
                    // assertTrue(!codeBlocks.isEmpty());

                    // Find function index
                    int funcIndex = -1;

                    // Look for function in bound code block
                    int runtimeIndex = instructionValue;
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
                    stackFrame = acquireFrame();
                    stackFrame.initForUserFunction(
                            userFunctionPrototypes.get(userFunctions.get(funcIndex).prototypeIndex), funcIndex);
                    userCallStack.add(stackFrame);

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
                    int frameIndex = userCallStack.size() - 1;
                    stackFrame = userCallStack.get(frameIndex);

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
                    // assertTrue(!userCallStack.isEmpty());

                    // Find current stack frame
                    top = userCallStack.size() - 1;
                    stackFrame = userCallStack.get(top);

                    boolean doFreeTempData = instructionValue == 1;

                    // assertTrue(stackFrame.userFuncIndex >= 0);

                    // Restore previous stack frame data
                    int returnAddr = stackFrame.returnAddr;
                    int prevCurrentFrame = stackFrame.prevCurrentFrame;
                    int prevStackTop = stackFrame.prevStackTop;
                    int prevTempDataLock = stackFrame.prevTempDataLock;

                    if (doFreeTempData) {
                        unwindTemp();
                    }

                    unwindStack(prevStackTop);
                    data.restoreState(prevStackTop, prevTempDataLock, doFreeTempData);

                    userCallStack.remove(top);
                    releaseFrame(stackFrame);

                    // Return to return address
                    ip = returnAddr;

                    // Make previous frame active
                    currentUserFrame = prevCurrentFrame;

                    // Remove stack frame
                    userCallStack.remove(userCallStack.size() - 1);

                    continue step; // Proceed without incrementing instruction
                }

                case OpCode.OP_NO_VALUE_RETURNED:
                    setError(ERR_NO_VALUE_RETURNED);
                    break;
                case OpCode.OP_BINDCODE:
                    boundCodeBlock = regValue;
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
                            // assertTrue(codeBlock.programOffset >= 0);
                            // assertTrue(codeBlock.programOffset < codeInstructions.size());

                            // Check for stack overflow
                            if (userCallStack.size() >= MAX_USER_STACK_CALLS) {
                                setError(ERR_STACK_OVERFLOW);
                                break;
                            }

                            // Push stack frame, with return address
                            stackFrame = acquireFrame();
                            stackFrame.initForGosub(ip + 1);
                            userCallStack.add(stackFrame);

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
                    if (readProgramData(instructionVarType)) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                case OpCode.OP_DATA_RESET:
                    programDataOffset = instructionValue;
                    ip++; // Proceed to next instruction
                    continue step;

                case OpCode.OP_SAVE_PARAM: {

                    // Allocate parameter data
                    if (!data.hasStackRoomFor(1)) {
                        setError(ERR_USER_FUNC_STACK_OVERFLOW);
                        break;
                    }
                    int dataIndex = data.allocateStack(1);
                    int paramIndex = instructionValue;

                    // Initialize parameter
                    // assertTrue(!userCallStack.isEmpty());
                    CollectionUtil.last(userCallStack).localVarDataOffsets.set(paramIndex, dataIndex);

                    // Transfer register value to parameter
                    switch (instructionVarType) {
                        case BasicValType.VTP_INT:
                        case BasicValType.VTP_REAL:
                        case BasicValType.VTP_FUNC_PTR:
                            // TODO Confirm value is properly set
                            // TODO Check other "dest" variables
                            data.data().setIntValue(dataIndex, regValue);
                            break;
                        case BasicValType.VTP_STRING:

                            // Allocate string space
                            int stringIndex = stringStore.alloc();
                            data.data().setIntValue(dataIndex, stringIndex);

                            // Copy string value
                            stringStore.setValue(stringIndex, getRegString());
                            break;
                        default:
                            // assertTrue(false);
                    }

                    // Save parameter offset in register (so that OpCode.OP_REG_DESTRUCTOR
                    // will work)
                    regValue = (dataIndex);
                    ip++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_COPY_USER_STACK: {

                    // Copy data pointed to by mReg into next stack frame
                    // parameter.
                    // Instruction value points to the parameter data type.
                    if (copyToParam(regValue, typeSet.getValType(instructionValue))) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }

                case OpCode.OP_MOVE_TEMP: {
                    if (moveToTemp(regValue, typeSet.getValType(instructionValue))) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        break;
                    }
                }

                case OpCode.OP_CHECK_PTR: {
                    if (checkPointer(reg2Value, regValue)) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        setError(ERR_POINTER_SCOPE_ERROR);
                        break;
                    }
                }

                case OpCode.OP_CHECK_PTRS: {
                    if (checkPointers(regValue, typeSet.getValType(instructionValue), reg2Value)) {
                        ip++; // Proceed to next instruction
                        continue step;
                    } else {
                        setError(ERR_POINTER_SCOPE_ERROR);
                        break;
                    }
                }

                case OpCode.OP_REG_DESTRUCTOR: {

                    // Register destructor for data pointed to by mReg.
                    int ptr = regValue;
                    // assertTrue(ptr >= 0);
                    if (ptr == 0) {
                        // Do nothing
                    } else if (ptr < data.getTempData()) {

                        // Pointer into temp data found
                        // assertTrue(
                        //                                tempDestructors.isEmpty() ||
                        // CollectionUtil.last(tempDestructors).addr < ptr);
                        tempDestructors.add(new StackDestructor(ptr, instructionValue));
                    } else if (ptr >= data.getStackTop() && ptr < data.getPermanent()) {

                        // Pointer into stack data found
                        // assertTrue(
                        //                                stackDestructors.isEmpty() ||
                        // CollectionUtil.last(stackDestructors).addr > ptr);
                        stackDestructors.add(new StackDestructor(ptr, instructionValue));
                    }
                    ip++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_SWAP: {
                    // Swap registers
                    int temp = regValue;
                    regValue = reg2Value;
                    reg2Value = temp;

                    String tempString = getRegString();
                    setRegString(getReg2String());
                    setReg2String(tempString);

                    ip++; // Proceed to next instruction
                    continue step;
                }

                case OpCode.OP_SAVE_PARAM_PTR: {

                    // Save register pointer into param pointer
                    // assertTrue(!userCallStack.isEmpty());
                    userCallStack
                            .get(userCallStack.size() - 1)
                            .localVarDataOffsets
                            .set(instructionValue, regValue);

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
        // assertTrue(data.isIndexValid(sourceIndex));
        // assertTrue(data.isIndexValid(sourceIndex + size - 1));
        // assertTrue(data.isIndexValid(destIndex));
        // assertTrue(data.isIndexValid(destIndex + size - 1));
        data.data().copyInts(sourceIndex, destIndex, size);
    }

    void copyStructure(int sourceIndex, int destIndex, ValType type) {
        // assertTrue(dataTypes.isTypeValid(type));
        // assertTrue(type.getVirtualPointerLevel() == 0);
        // assertTrue(type.arrayLevel == 0);
        // assertTrue(type.basicType >= 0);

        // Find structure definition
        Structure s = dataTypes.getStructures().get(type.basicType);

        // Copy fields in structure
        for (int i = 0; i < s.fieldCount; i++) {
            StructureField f = dataTypes.getFields().get(s.firstFieldIndex + i);
            copyField(sourceIndex + f.dataOffset, destIndex + f.dataOffset, f.type);
        }
    }

    void copyArray(int sourceIndex, int destIndex, ValType type) {
        // assertTrue(dataTypes.isTypeValid(type));
        // assertTrue(type.getVirtualPointerLevel() == 0);
        // assertTrue(type.arrayLevel > 0);
        // assertTrue(data.isIndexValid(sourceIndex));
        // assertTrue(data.isIndexValid(destIndex));
        int sourceElementCount = data.data().getIntValue(sourceIndex);
        int sourceElementSize = data.data().getIntValue(sourceIndex + 1);
        // assertTrue(sourceElementCount == data.data().getIntValue(destIndex)); // Array sizes match
        // assertTrue(sourceElementSize == data.data().getIntValue(destIndex + 1)); // Element sizes match

        // Find element type and size
        ValType elementType = new ValType(type);
        elementType.arrayLevel--;
        int elementSize = sourceElementSize;

        // Copy elements
        for (int i = 0; i < sourceElementCount; i++) {
            if (elementType.arrayLevel > 0) {
                copyArray(sourceIndex + 2 + i * elementSize, destIndex + 2 + i * elementSize, elementType);
            } else {
                copyField(sourceIndex + 2 + i * elementSize, destIndex + 2 + i * elementSize, elementType);
            }
        }
    }

    void copyField(int sourceIndex, int destIndex, ValType type) {

        // assertTrue(dataTypes.isTypeValid(type));

        // If type is basic string, copy string value
        if (type.matchesType(BasicValType.VTP_STRING)) {
            int srcStringIndex = data.data().getIntValue(sourceIndex);
            int destStringIndex = data.data().getIntValue(destIndex);
            if (srcStringIndex > 0 || destStringIndex > 0) {

                // Allocate string space if necessary
                if (destStringIndex == 0) {
                    destStringIndex = stringStore.alloc();
                    data.data().setIntValue(destIndex, destStringIndex);
                }

                // Copy string value
                stringStore.setValue(destStringIndex, stringStore.getValueAt(srcStringIndex));
            }
        }

        // If type is basic, or pointer then just copy value
        else if (type.isBasicType() || type.getVirtualPointerLevel() > 0) {
            data.data().setIntValue(destIndex, data.data().getIntValue(sourceIndex));
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
        // assertTrue(dataTypes.isTypeValid(type));
        // assertTrue(type.getVirtualPointerLevel() == 0);

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
                // assertTrue(data.isIndexValid(s));
                // assertTrue(data.isIndexValid(s + 1));
                // assertTrue(data.isIndexValid(d));
                // assertTrue(data.isIndexValid(d + 1));
                int sourceElements = data.data().getIntValue(s);
                if (sourceElements != data.data().getIntValue(d)) {
                    setError(ERR_ARRAY_SIZE_MISMATCH);
                    return false;
                }

                // Update data size
                size *= sourceElements;
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
        // assertTrue(data.isIndexValid(index));
        // assertTrue(dataTypes.isTypeValid(type));

        // Type does not contain any pointers?
        if (!dataTypes.containsPointer(type)) {
            return true;
        }

        // Type is a pointer?
        if (type.pointerLevel > 0) {
            return checkPointer(destIndex, data.data().getIntValue(index));
        }

        // Type is not a pointer, but contains one or more pointers.
        // Need to recursively break down object and check

        // Type is array?
        if (type.arrayLevel > 0) {

            // Find and check elements
            int elements = data.data().getIntValue(index);
            int elementSize = data.data().getIntValue(index + 1);
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
            // assertTrue(type.basicType >= 0);

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
        // assertTrue(data.isIndexValid(dest));

        // Null pointer case
        if (ptr == 0) {
            return true;
        }

        // Check whether pointer points to temporary stack data
        if (ptr < data.getPermanent()) {

            // Such pointers can only be stored in variables in the current
            // stack frame.
            if (userCallStack.isEmpty()
                    || !(dest >= data.getStackTop() && dest < CollectionUtil.last(userCallStack).prevStackTop)) {
                return false;
            }
        }

        return true;
    }

    private void rebuildVariableDataIndexes() {
        ArrayList<Variable> vars = variables.getVariables();
        int[] indexes = new int[vars.size()];

        for (int i = 0; i < vars.size(); i++) {
            indexes[i] = vars.get(i).dataIndex;
        }

        variableDataIndexes = indexes;
    }

    boolean popArrayDimensions(ValType type) {
        // assertTrue(dataTypes.isTypeValid(type));
        // assertTrue(type.getVirtualPointerLevel() == 0);

        // Pop and validate array indices from stack into type
        int i;
        Integer v;
        for (i = 0; i < type.arrayLevel; i++) {
            v = stack.pop();
            int size = v + 1;
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

            onInstructionsUpdated();
        }
    }

    void internalPatchOut() {

        // Patch out breakpoints and restore program to its no breakpoint state.
        for (PatchedBreakPt pt : patchedBreakPoints) {
            if (pt.getOffset() < codeInstructions.size()) {
                codeInstructions.get(pt.getOffset()).opCode = pt.getReplacedOpCode();
            }
        }
        onInstructionsUpdated();
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
                        dest = CollectionUtil.last(userCallStack).returnAddr;
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
            int returnAddr = CollectionUtil.last(userCallStack).returnAddr;
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
        s.setReg(new Value(regValue));
        s.setReg2(new Value(reg2Value));
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

        // Current long running function
        s.setLongRunningFunction(longRunningFunction);
        longRunningFunction = null;

        // Other state
        s.setPaused(paused);

        return s;
    }

    public void setState(VMState state) {

        // Instruction pointer
        ip = state.getIp();

        // Registers
        regValue = state.getReg().getIntVal();
        reg2Value = state.getReg2().getIntVal();
        regString = state.getRegString();
        reg2String = state.getReg2String();

        // Stacks
        if (state.getStackTop() < stack.size()) {
            stack.resize(state.getStackTop());
        }
        if (state.getUserFuncStackTop() < userCallStack.size()) {
            CollectionUtil.resize(userCallStack, state.getUserFuncStackTop());
        }
        currentUserFrame = state.getCurrentUserFrame();

        // Top of program
        if (state.getCodeSize() < codeInstructions.size()) {
            CollectionUtil.resize(codeInstructions, state.getCodeSize());

            onInstructionsUpdated();
        }
        if (state.getCodeBlockCount() < codeBlocks.size()) {
            CollectionUtil.resize(codeBlocks, state.getCodeBlockCount());
        }

        // Var data
        unwindTemp();
        unwindStack(state.getStackDataTop());
        data.restoreState(state.getStackDataTop(), state.getTempDataLock(), true);

        // Long running function
        cancelLongRunningFunction();
        longRunningFunction = state.getLongRunningFunction();

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
            case BasicValType.VTP_FUNC_PTR:
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
        int remaining = Math.max(0, length.get());
        if (remaining == 0 || str.isEmpty()) {
            length.set(remaining);
            return "";
        }
        if (str.length() <= remaining) {
            length.set(remaining - str.length());
            return str;
        }
        length.set(0);
        return str.substring(0, remaining);
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
                // assertTrue(data.isIndexValid(val.getIntVal()));
            }
            val.setIntVal(data.data().getIntValue(val.getIntVal()));
        }
    }

    public String valToString(Value val, ValType type, Mutable<Integer> maxChars) {
        // assertTrue(dataTypes.isTypeValid(type));
        // assertTrue(type.getPhysicalPointerLevel() > 0 || type.isBasicType());

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
        // assertTrue(type.pointerLevel == 1);
        // assertTrue(type.isByRef);
        int dataIndex = val.getIntVal(); // Points to data
        if (dataIndex == 0) {
            return trimToLength("[UNSET]", maxChars);
        }
        String result = "";

        // Arrays
        if (type.arrayLevel > 0) {
            // assertTrue(data.isIndexValid(dataIndex));
            // assertTrue(data.isIndexValid(dataIndex + 1));

            // Read array header
            int elements = data.data().getIntValue(dataIndex);
            int elementSize = data.data().getIntValue(dataIndex + 1);
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
                        // assertTrue(e.getValue().getIntVal() >= 0);
                        // assertTrue(e.getValue().getIntVal() < stringConstants.size());
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
                        regValue = (e.getValue().getIntVal());
                        return true;
                    case BasicValType.VTP_REAL:
                        regValue = ((int) e.getValue().getRealVal());
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
                        regValue = floatToRawIntBits((float) e.getValue().getIntVal());
                        return true;
                    case BasicValType.VTP_REAL:
                        regValue = floatToRawIntBits(e.getValue().getRealVal());
                        return true;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        // assertTrue(false);
        return false;
    }

    int storedDataSize(int sourceIndex, ValType type) {
        // assertTrue(!type.isByRef);

        if (type.pointerLevel == 0 && type.arrayLevel > 0) {
            // Calculate actual array size
            // Array is prefixed by element count and element size.
            return data.data().getIntValue(sourceIndex) * data.data().getIntValue(sourceIndex + 1) + 2;
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
        regValue = (dataIndex);

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
        // assertTrue(dataTypes.containsString(type));
        // assertTrue(!type.isByRef);
        // assertTrue(type.pointerLevel == 0);

        // Type IS string case
        if (type.matchesType(BasicValType.VTP_STRING)) {

            int stringIndex = data.data().getIntValue(dataIndex);
            // Empty strings (index 0) can be ignored
            if (stringIndex != 0) {

                // Allocate new string
                int newStringIndex = stringStore.alloc();

                // Copy previous string
                stringStore.setValue(newStringIndex, stringStore.getValueAt(stringIndex));

                // Point to new string
                data.data().setIntValue(dataIndex, newStringIndex);
            }
        }

        // Array case
        else if (type.arrayLevel > 0) {

            // Read array header
            int elements = data.data().getIntValue(dataIndex);
            int elementSize = data.data().getIntValue(dataIndex + 1);
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
            // assertTrue(type.basicType >= 0);

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
        regValue = (dataIndex);

        return true;
    }

    void unwindTemp() {
        unwindTemp(new ProtectedStackRange());
    }

    void unwindTemp(ProtectedStackRange protect) {
        int newTop = data.getTempDataLock();

        // Run destrution logic over data that is about to be deallocated.
        while (!tempDestructors.isEmpty() && CollectionUtil.last(tempDestructors).addr >= newTop) {
            destroyData(CollectionUtil.last(tempDestructors), protect);
            tempDestructors.remove(tempDestructors.size() - 1);
        }

        // Note: We don't actually remove the data from the stack. Calling code
        // must
        // handle that instead.
    }

    void unwindStack(int newTop) {

        // Run destruction logic over data that is about to be deallocated.
        while (!stackDestructors.isEmpty() && CollectionUtil.last(stackDestructors).addr < newTop) {
            destroyData(CollectionUtil.last(stackDestructors), new ProtectedStackRange());
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
        // assertTrue(dataTypes.containsString(type));
        // assertTrue(!type.isByRef);

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
            int stringIndex = data.data().getIntValue(index);
            if (stringIndex != 0) {
                stringStore.freeAtIndex(stringIndex);
            }
        } else if (type.arrayLevel > 0) {

            // Array case
            ValType elementType = new ValType(type);
            elementType.arrayLevel--;
            int count = data.data().getIntValue(index);
            int elementSize = data.data().getIntValue(index + 1);
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
            // assertTrue(type.pointerLevel == 0);
            // assertTrue(type.basicType >= 0);

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
        // assertTrue(!codeBlocks.isEmpty());
        return CollectionUtil.last(codeBlocks);
    }

    public int getCurrentCodeBlockIndex() {
        // assertTrue(!codeBlocks.isEmpty());
        return codeBlocks.size() - 1;
    }

    public boolean isCodeBlockValid(int index) {
        return index >= 0 && index < codeBlocks.size();
    }

    public int getCodeBlockOffset(int index) {
        // assertTrue(isCodeBlockValid(index));
        return codeBlocks.get(index).programOffset;
    }

    public CodeBlock getCodeBlock(int index) {
        // assertTrue(isCodeBlockValid(index));
        return codeBlocks.get(index);
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
        CollectionUtil.resize(codeBlocks, rollbackPoint.codeBlockCount);
        boundCodeBlock = rollbackPoint.boundCodeBlock;
        CollectionUtil.resize(userFunctionPrototypes, rollbackPoint.functionPrototypeCount);
        CollectionUtil.resize(userFunctions, rollbackPoint.functionCount);
        CollectionUtil.resize(programData, rollbackPoint.dataCount);
        CollectionUtil.resize(codeInstructions, rollbackPoint.instructionCount);

        onInstructionsUpdated();
    }

    // Streaming
    public void streamOut(DataOutputStream stream) throws IOException {
        int i;
        // Stream header
        Streaming.writeString(stream, STREAM_HEADER);
        Streaming.writeLong(stream, STREAM_VERSION);

        // Plugins
        this.plugins.streamOut(stream);

        // Variables
        variables.streamOut(stream); // Note: `variables` automatically streams out `dataTypes`

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

        // Plugins
        if (!this.plugins.streamIn(stream)) {
            setError(this.plugins.getError());
            return false;
        }

        // Register plugin structures and functions in VM
        this.plugins.getStructureManager().addVMStructures(getDataTypes());
        this.plugins.createVMFunctionSpecs();

        // Variables
        variables.streamIn(stream);
        rebuildVariableDataIndexes();

        // String constants
        int count, i;
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            CollectionUtil.resize(stringConstants, count);
            for (i = 0; i < count; i++) {
                stringConstants.set(i, Streaming.readString(stream));
            }

            // Data type lookup table
            typeSet.streamIn(stream);
        }
        // Program code
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            CollectionUtil.resize(codeInstructions, count);
            for (i = 0; i < count; i++) {
                codeInstructions.set(i, new Instruction());
                codeInstructions.get(i).streamIn(stream);
            }
        }
        onInstructionsUpdated();

        // Program data (for "DATA" statements)
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            CollectionUtil.resize(programData, count);
            for (i = 0; i < count; i++) {
                programData.set(i, new ProgramDataElement());
                programData.get(i).streamIn(stream);
            }
        }
        // User function prototypes
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            CollectionUtil.resize(userFunctionPrototypes, count);
            for (i = 0; i < count; i++) {
                userFunctionPrototypes.set(i, new UserFuncPrototype());
                userFunctionPrototypes.get(i).streamIn(stream);
            }
        }
        // User functions
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            CollectionUtil.resize(userFunctions, count);
            for (i = 0; i < count; i++) {
                userFunctions.set(i, new UserFunc());
                userFunctions.get(i).streamIn(stream);
            }
        }
        // Code blocks
        count = (int) Streaming.readLong(stream);
        if (count != -1) {
            CollectionUtil.resize(codeBlocks, count);
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
        // assertTrue(isIPValid());
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
        // assertTrue(isIPValid());
        line.set(codeInstructions.get(ip).sourceLine);
        col.set(codeInstructions.get(ip).sourceChar);
    }

    public InstructionPosition getIPInSourceCode() {
        // assertTrue(isIPValid());
        return new InstructionPosition(0, codeInstructions.get(ip).sourceLine, codeInstructions.get(ip).sourceChar);
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

    public int getReg() {
        return regValue;
    }

    public int getReg2() {
        return reg2Value;
    }

    public int getRegIntVal() {
        return regValue;
    }

    public int getReg2IntVal() {
        return reg2Value;
    }

    public float getRegFloatValue() {
        return intBitsToFloat(regValue);
    }

    public float getReg2FloatValue() {
        return intBitsToFloat(reg2Value);
    }

    public void setRegIntVal(int value) {
        regValue = value;
    }

    public void setReg2IntVal(int value) {
        reg2Value = value;
    }

    public void setRegFloatValue(float value) {
        regValue = floatToRawIntBits(value);
    }

    public void setReg2FloatValue(float value) {
        reg2Value = floatToRawIntBits(value);
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

    public ArrayList<ProgramDataElement> getProgramData() {
        return programData;
    }

    // User functions
    public ArrayList<UserFuncPrototype> getUserFunctionPrototypes() {
        return userFunctionPrototypes;
    }

    public ArrayList<UserFunc> getUserFunctions() {
        return userFunctions;
    }

    public ArrayList<UserFuncStackFrame> getUserCallStack() {
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
        // assertTrue(isOffsetValid(offset));
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

        onInstructionsUpdated();
    }

    public void rollbackProgram(int size) {
        // assertTrue(size >= 0);
        // assertTrue(size <= getInstructionCount());
        while (size < getInstructionCount()) {
            codeInstructions.remove(codeInstructions.size() - 1);
        }

        onInstructionsUpdated();
    }

    public Instruction getInstruction(int index) {
        // assertTrue(index < codeInstructions.size());
        patchOut();
        return codeInstructions.get(index);
    }

    public void setInstruction(int index, Instruction instruction) {
        // assertTrue(index < codeInstructions.size());
        patchOut();
        codeInstructions.set(index, instruction);
    }

    public void removeLastInstruction() {
        codeInstructions.remove(codeInstructions.size() - 1);

        onInstructionsUpdated();
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

    public ArrayList<String> getStringConstants() {
        return stringConstants;
    }

    // External functions
    public int addFunction(Function func) {
        int result = getFunctionCount();
        functions.add(func);

        functionArr = this.functions.toArray(new Function[0]);

        return result;
    }

    public int getFunctionCount() {
        return functions.size();
    }

    // Called by external functions
    //    public Value getParam(int index) {
    //        // Read param from param stack.
    //        // Index 1 is TOS
    //        // Index 2 is TOS - 1
    //        // ...
    //        //assertTrue(index > 0);
    //        //assertTrue(index <= stack.size());
    //        return new Value(stack.get(stack.size() - index));
    //    }

    public int getIntParam(int index) {
        return stack.get(stack.size() - index); // getParam(index).getIntVal();
    }

    public float getRealParam(int index) {
        return intBitsToFloat(getIntParam(index)); // getParam(index).getRealVal();
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
        // assertTrue(ptr > 0);
        // assertTrue(data.isIndexValid(ptr));
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

    // Plugins
    public PluginManager getPlugins() {
        return plugins;
    }

    // Builtin/plugin function callback support
    public boolean isEndCallback() {
        // assertTrue(isIPValid());
        return codeInstructions.get(ip).opCode == OpCode.OP_END_CALLBACK; // Reached end callback opcode?
    }

    public void beginLongRunningFunction(Basic4GLLongRunningFunction handler) {

        // Should never be any existing long running fn, but just in case
        cancelLongRunningFunction();

        // Set handler as the new long running fn.
        // This will prevent VM from executing any more op-codes
        longRunningFunction = handler;
    }

    public void endLongRunningFunction() {
        if (longRunningFunction != null) {
            // Delete if required
            if (longRunningFunction.deleteWhenDone()) {
                longRunningFunction.dispose();
            }
            // Unhook handler. This allows VM to continue executing op-codes.
            longRunningFunction = null;

            // Notify
            if (longRunningFnDoneNotifiedListener != null) {
                longRunningFnDoneNotifiedListener.onLongRunningFunctionDone(false);
            }
        }
    }

    public void cancelLongRunningFunction() {
        if (longRunningFunction != null) {
            boolean deleteWhenDone = longRunningFunction.deleteWhenDone();
            longRunningFunction.cancel();
            if (deleteWhenDone) {
                longRunningFunction.dispose();
            }
            longRunningFunction = null;

            // Notify
            if (longRunningFnDoneNotifiedListener != null) {
                longRunningFnDoneNotifiedListener.onLongRunningFunctionDone(true);
            }
        }
    }

    public void setTimeshareBreakRequired() {
        timeshare = true;
    }

    public boolean isInLongRunningFunction() {
        return longRunningFunction != null;
    }

    /**
     * If VM is waiting for a long running function that does not require polling,
     * then application can block and wait for next event before calling continue() again.
     * @return
     */
    public boolean canWaitForEvents() {
        return longRunningFunction != null && !longRunningFunction.isPolled();
    }

    public void setLongRunningFunctionDoneNotified(ILongRunningFunctionListener listener) {
        longRunningFnDoneNotifiedListener = listener;
    }

    public Instruction[] getInstructions() {
        return codeInstructions.toArray(new Instruction[0]);
    }

    private void onInstructionsUpdated() {

        codeInstructionValues =
                codeInstructions.stream().mapToInt(x -> x.value.getIntVal()).toArray();
        codeInstructionOpCodes =
                codeInstructions.stream().mapToInt(x -> x.opCode).toArray();
        codeInstructionVarTypes =
                codeInstructions.stream().mapToInt(x -> x.basicVarType).toArray();
    }
}
