package com.basic4gl.compiler;

import static com.basic4gl.compiler.TomBasicCompiler.LanguageSyntax.*;
import static com.basic4gl.compiler.TomBasicCompiler.LanguageSyntax.LS_TRADITIONAL;
import static com.basic4gl.runtime.util.Assert.assertTrue;

import com.basic4gl.compiler.FlowControl.FlowControlType;
import com.basic4gl.compiler.Token.TokenType;
import com.basic4gl.runtime.plugin.ExtendedFunctionSpecification;
import com.basic4gl.compiler.util.*;
import com.basic4gl.lib.util.Library;
import com.basic4gl.runtime.*;
import com.basic4gl.runtime.plugin.PluginManager;
import com.basic4gl.runtime.stackframe.RuntimeFunction;
import com.basic4gl.runtime.stackframe.UserFunc;
import com.basic4gl.runtime.stackframe.UserFuncPrototype;
import com.basic4gl.runtime.types.*;
import com.basic4gl.runtime.util.Function;
import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Basic4GL v2 language compiler.
 * Used to compile source code in BASIC language to TomVM Op codes.
 */
public class TomBasicCompiler extends HasErrorState {
    static final LanguageSyntax DEFAULT_SYNTAX = LS_BASIC4GL;

    /**
     * Allow 256 functions of the same name
     * (should be more than enough for anything...)
     */
    static final int TC_MAXOVERLOADEDFUNCTIONS = 256;

    // Virtual machine
    private final TomVM vm;

    // Parser
    private final Parser parser;

    // Plugin manager
    private PluginManager plugins;

    // Settings
    private final boolean isCaseSensitive;
    /**
     * Recognised operators. Unary have one operand (e.g NOT x)
     */
    private final Map<String, Operator> unaryOperators;
    /**
     * Binary have to (e.g. x + y)
     */
    private final Map<String, Operator> binaryOperators;
    /**
     * Operator overload function names
     */
    private final Map<Integer, String> operatorOverloads;
    private final ArrayList<String> reservedWords;
    /**
     * Permanent constants.
     */
    private final Map<String, Constant> constants;
    /**
     * Constants declared using the const command.
     */
    private final Map<String, Constant> programConstants;

    private final Vector<Library> libraries = new Vector<>();
    private final Vector<FunctionSpecification> functions;
    /**
     * Maps function name to index of function (in functions array)
     */
    private final Map<String, List<Integer>> functionIndex;
    private LanguageSyntax syntax;
    /**
     * Prefix all symbols with this text
     */
    private String symbolPrefix = "";

    // Compiler state
    private ValType regType, reg2Type;
    private final Vector<ValType> operandStack;
    private final Vector<StackedOperator> operatorStack;

    StackedOperator getOperatorTOS() {
        return operatorStack.get(operatorStack.size() - 1);
    }

    void setOperatorTOS(StackedOperator operator) {
        operatorStack.set(operatorStack.size() - 1, operator);
    }

    private final Map<String, Label> labels;
    private final Map<Integer, String> labelIndex;
    /**
     * Jumps to fix up
     */
    private final Vector<Jump> jumps;
    /**
     * Resets to fix up
     */
    private final Vector<Jump> resets;
    /**
     * Flow control structure stack
     */
    private final Vector<FlowControl> flowControls;
    private Token token;
    /**
     * True if next instruction must be separated by a colon (or newline)
     */
    private boolean needColon;
    /**
     * True if need to generate code to free temporary data before the next instruction
     */
    private boolean freeTempData;
    private int lastLine, lastCol;
    private boolean inFunction;
    private final InstructionPosition functionStart;
    private int functionJumpOver;
    /**
     * Maps function name to index of function
     */
    private final Map<String, Integer> globalUserFunctionIndex;
    /**
     * Local user function index (for the current code block being compiled)
     */
    private final Map<String, Integer> localUserFunctionIndex;

    /**
     * Combines local and global (where a local function overrides a global one of the same name)
     */
    private Map<String, Integer> visibleUserFunctionIndex;

    /**
     * Index->Name lookup. For debug views.
     */
    private final Map<Integer, String> userFunctionReverseIndex;
    /**
     * Index of current active user function.
     * Usually this will be the last in the vm.getUserFunctions() vector;
     * can be different in special cases (e.g. when compiler is called from debugger to evaluate an expression).
     */
    private int currentFunction; //

    /**
     * Prototype of function being declared.
     */
//    private UserFuncPrototype userFuncPrototype;

    private final Vector<com.basic4gl.compiler.RuntimeFunction> runtimeFunctions;
    private final Map<String, Integer> runtimeFunctionIndex;
    private int currentCodeBlockIndex;

    // Language extension
    /**
     * Unary operator extensions
     */
    private final Vector<UnaryOperatorExtension> unaryOperatorExtensions;
    /**
     * Binary operator extensions
     */
    private final Vector<BinaryOperatorExtension> binaryOperatorExtensions;

    public List<String> getReservedWords() {
        return reservedWords;
    }

    /**
     * Internal compiler types
     */
    enum OperType {
        OT_OPERATOR,
        OT_RETURNBOOLOPERATOR,
        OT_BOOLOPERATOR,
        OT_LAZYBOOLOPERATOR,
        OT_LBRACKET,
        /**
         * Forces expression evaluation to stop
         */
        OT_STOP
    }

    /**
     * Used for tracking which operators are about to be applied to operands.
     * Basic4GL converts infix expressions into reverse polish using an operator
     * stack and an operand stack.
     */
    static class Operator {
        private OperType type;
        private short opCode;

        private int params;

        private int binding;

        Operator(OperType type, short opCode, int params, int binding) {
            this.type = type;
            this.opCode = opCode;
            this.params = params;
            this.binding = binding;
        }

        Operator() {
            type = OperType.OT_OPERATOR;
            opCode = OpCode.OP_NOP;
            params = 0;
            binding = 0;
        }

        Operator(Operator o) {
            type = o.type;
            opCode = o.opCode;
            params = o.params;
            binding = o.binding;
        }

        public OperType getType() {
            return type;
        }

        public void setType(OperType type) {
            this.type = type;
        }

        public short getOpCode() {
            return opCode;
        }

        public void setOpCode(short opCode) {
            this.opCode = opCode;
        }

        /**
         * 1 . Calculate "op Reg" (e.g. "Not Reg")
         * 2 . Calculate "Reg2 op Reg" (e.g. "Reg2 - Reg")
         */
        public int getParams() {
            return params;
        }

        public void setParams(int params) {
            this.params = params;
        }

        /**
         * Operator binding. Higher = tighter.
         */
        public int getBinding() {
            return binding;
        }

        public void setBinding(int binding) {
            this.binding = binding;
        }
    }

    static class StackedOperator {
        private Operator operator;

        private int lazyJumpAddress;

        StackedOperator(Operator o) {
            operator = o;
            lazyJumpAddress = -1;
        }

        StackedOperator(Operator o, int lazyJumpAddr) {
            operator = o;
            lazyJumpAddress = lazyJumpAddr;
        }

        /**
         * Stacked operator
         */
        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        /**
         * Address of lazy jump op code (for "and" and "or" operations)
         */
        public int getLazyJumpAddress() {
            return lazyJumpAddress;
        }

        public void setLazyJumpAddress(int lazyJumpAddress) {
            this.lazyJumpAddress = lazyJumpAddress;
        }
    }

    /**
     * A program label, i.e. a named destination for "goto" and "gosub"s
     */
    static class Label implements Streamable {
        private int offset;

        private int programDataOffset;

        Label(int offset, int dataOffset) {
            this.offset = offset;
            programDataOffset = dataOffset;
        }

        Label() {
            offset = 0;
            programDataOffset = 0;
        }

        public void streamOut(DataOutputStream stream) throws IOException {
            Streaming.writeLong(stream, offset);
            Streaming.writeLong(stream, programDataOffset);
        }

        public boolean streamIn(DataInputStream stream) throws IOException {
            offset = (int) Streaming.readLong(stream);
            programDataOffset = (int) Streaming.readLong(stream);

            return true;
        }

        /**
         * Instruction index in code
         */
        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        /**
         * Program data offset. (For use with "RESET labelname" command.)
         */
        public int getProgramDataOffset() {
            return programDataOffset;
        }

        public void setProgramDataOffset(int programDataOffset) {
            this.programDataOffset = programDataOffset;
        }
    }

    /**
     * Used to track program jumps. Actual addresses are patched into jump
     * instructions after the main compilation pass has completed. (Thus forward
     * jumps are possible.)
     */
    static class Jump {
        private int jumpInstruction;

        private String labelName;

        Jump(int instruction, String labelName) {
            jumpInstruction = instruction;
            this.labelName = labelName;
        }

        Jump() {
            jumpInstruction = 0;
            labelName = "";
        }

        /**
         * Instruction containing jump instruction
         */
        public int getJumpInstruction() {
            return jumpInstruction;
        }

        public void setJumpInstruction(int jumpInstruction) {
            this.jumpInstruction = jumpInstruction;
        }

        /**
         * Label to which we are jumping
         */
        public String getLabelName() {
            return labelName;
        }

        public void setLabelName(String labelName) {
            this.labelName = labelName;
        }
    }

    // Misc
    static class ParserPos {
        private int line;
        private int column;
        private Token token;

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public Token getToken() {
            return token;
        }

        public void setToken(Token token) {
            this.token = token;
        }
    }

    public enum LanguageSyntax {
        LS_TRADITIONAL(0), // As compatible as possible with other BASICs
        /**
         *  Standard Basic4GL syntax for backwards compatibility with existing code.
         */
        LS_BASIC4GL(1),
        /**
         * Traditional mode PRINT, but otherwise standard Basic4GL syntax
         */
        LS_TRADITIONAL_PRINT(2),
        /**
         * Like LS_TRADITIONAL, but also tries to match the variable suffixes of other BASIC types.
         */
        LS_TRADITIONAL_SUFFIX(3);

        private final int type;

        LanguageSyntax(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    enum UserFunctionType {
        /**
         * Function implementation
         */
        UFT_IMPLEMENTATION,
        /**
         * Forward declaration
         */
        UFT_FWDDECLARATION,
        /**
         * Declaring a function pointer type
         */
        UFT_RUNTIMEDECLARATION,
        /**
         * Declaring a function pointer type
         */
        UFT_POINTER
    }

    enum FunctionType {
        FT_NORMAL,
        FT_OPERATOR_UNARY,
        FT_OPERATOR_BINARY
    }
    /**
     * Allows the compiler to rollback cleanly if an error occurs during
     * compilation. Used during runtime compilation to ensure the compiler
     * does not leave the VM in an unstable state.
     * Note: Currently not everything is rolled back, just enough to keep the
     * VM stable. There may still be resources used (such as code instructions
     * allocated), but they should be benign and unreachable.
     */
    public static class RollbackPoint {

        private com.basic4gl.runtime.RollbackPoint vmRollback;

        private int runtimeFunctionCount;

        /**
         * Virtual machine rollback
         */
        public com.basic4gl.runtime.RollbackPoint getVmRollback() {
            return vmRollback;
        }

        public void setVmRollback(com.basic4gl.runtime.RollbackPoint vmRollback) {
            this.vmRollback = vmRollback;
        }

        /**
         * Runtime functions
         */
        public int getRuntimeFunctionCount() {
            return runtimeFunctionCount;
        }

        public void setRuntimeFunctionCount(int runtimeFunctionCount) {
            this.runtimeFunctionCount = runtimeFunctionCount;
        }
    }

    public TomBasicCompiler(TomVM vm, PluginManager plugins) {
        this(vm, plugins, false);
    }

    public TomBasicCompiler(TomVM vm, PluginManager plugins, boolean caseSensitive) {
        this.vm = vm;
        this.plugins = plugins;

        isCaseSensitive = caseSensitive;
        syntax = LS_BASIC4GL;

        operandStack = new Vector<>();
        operatorStack = new Vector<>();
        jumps = new Vector<>();
        resets = new Vector<>();
        flowControls = new Vector<>();

        binaryOperators = new HashMap<>();
        operatorOverloads = new HashMap<>();
        unaryOperators = new HashMap<>();

        reservedWords = new ArrayList<>();

        parser = new Parser();

        programConstants = new HashMap<>();
        labels = new HashMap<>();
        labelIndex = new HashMap<>();

        functionIndex = new HashMap<>();
        constants = new HashMap<>();

        localUserFunctionIndex = new HashMap<>();
        globalUserFunctionIndex = new HashMap<>();
        visibleUserFunctionIndex = new HashMap<>();
        userFunctionReverseIndex = new HashMap<>();
        runtimeFunctionIndex = new HashMap<>();
        runtimeFunctions = new Vector<>();
        functions = new Vector<>();

        unaryOperatorExtensions = new Vector<>();
        binaryOperatorExtensions = new Vector<>();

        functionStart = new InstructionPosition();

        clearState();

        // Setup operators
        // Note: From experimentation it appears QBasic binds "xor" looser than
        // "and" and "or". So for compatibility, we will too..
        binaryOperators.put("xor", new Operator(OperType.OT_BOOLOPERATOR, OpCode.OP_OP_XOR, 2, 10));
        binaryOperators.put("or", new Operator(OperType.OT_BOOLOPERATOR, OpCode.OP_OP_OR, 2, 11));
        binaryOperators.put("and", new Operator(OperType.OT_BOOLOPERATOR, OpCode.OP_OP_AND, 2, 12));
        binaryOperators.put("lor", new Operator(OperType.OT_LAZYBOOLOPERATOR, OpCode.OP_OP_OR, 2, 11));
        binaryOperators.put("land", new Operator(OperType.OT_LAZYBOOLOPERATOR, OpCode.OP_OP_AND, 2, 12));
        unaryOperators.put("not", new Operator(OperType.OT_BOOLOPERATOR, OpCode.OP_OP_NOT, 1, 20));
        binaryOperators.put("=", new Operator(OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_EQUAL, 2, 30));
        binaryOperators.put("<>", new Operator(OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_NOT_EQUAL, 2, 30));
        binaryOperators.put(">", new Operator(OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_GREATER, 2, 30));
        binaryOperators.put(">=", new Operator(OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_GREATER_EQUAL, 2, 30));
        binaryOperators.put("<", new Operator(OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_LESS, 2, 30));
        binaryOperators.put("<=", new Operator(OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_LESS_EQUAL, 2, 30));
        binaryOperators.put("+", new Operator(OperType.OT_OPERATOR, OpCode.OP_OP_PLUS, 2, 40));
        binaryOperators.put("-", new Operator(OperType.OT_OPERATOR, OpCode.OP_OP_MINUS, 2, 40));
        binaryOperators.put("*", new Operator(OperType.OT_OPERATOR, OpCode.OP_OP_TIMES, 2, 41));
        binaryOperators.put("/", new Operator(OperType.OT_OPERATOR, OpCode.OP_OP_DIV, 2, 42));
        binaryOperators.put("%", new Operator(OperType.OT_OPERATOR, OpCode.OP_OP_MOD, 2, 43));
        unaryOperators.put("-", new Operator(OperType.OT_OPERATOR, OpCode.OP_OP_NEG, 1, 50));

        // Operator overload function names

        operatorOverloads.put((int) OpCode.OP_OP_EQUAL, "operator=");
        operatorOverloads.put((int) OpCode.OP_OP_NOT_EQUAL, "operator<>");
        operatorOverloads.put((int) OpCode.OP_OP_GREATER, "operator>");
        operatorOverloads.put((int) OpCode.OP_OP_GREATER_EQUAL, "operator>=");
        operatorOverloads.put((int) OpCode.OP_OP_LESS, "operator<");
        operatorOverloads.put((int) OpCode.OP_OP_LESS_EQUAL, "operator<=");
        operatorOverloads.put((int) OpCode.OP_OP_PLUS, "operator+");
        operatorOverloads.put((int) OpCode.OP_OP_MINUS, "operator-");
        operatorOverloads.put((int) OpCode.OP_OP_TIMES, "operator*");
        operatorOverloads.put((int) OpCode.OP_OP_DIV, "operator/");
        operatorOverloads.put((int) OpCode.OP_OP_MOD, "operator%");
        operatorOverloads.put((int) OpCode.OP_OP_NEG, "operator-");

        // Setup reserved words
        reservedWords.add("dim");
        reservedWords.add("goto");
        reservedWords.add("if");
        reservedWords.add("then");
        reservedWords.add("elseif");
        reservedWords.add("else");
        reservedWords.add("endif");
        reservedWords.add("end");
        reservedWords.add("gosub");
        reservedWords.add("return");
        reservedWords.add("for");
        reservedWords.add("to");
        reservedWords.add("step");
        reservedWords.add("next");
        reservedWords.add("while");
        reservedWords.add("wend");
        reservedWords.add("run");
        reservedWords.add("struc");
        reservedWords.add("endstruc");
        reservedWords.add("const");
        reservedWords.add("alloc");
        reservedWords.add("null");
        reservedWords.add("data");
        reservedWords.add("read");
        reservedWords.add("reset");
        reservedWords.add("type"); // QBasic/Freebasic compatibility
        reservedWords.add("as"); //
        reservedWords.add("integer"); //
        reservedWords.add("single"); //
        reservedWords.add("double"); //
        reservedWords.add("string"); //
        reservedWords.add("language"); // Language syntax
        reservedWords.add("traditional");
        reservedWords.add("basic4gl");
        reservedWords.add("traditional_print");
        reservedWords.add("traditional_suffix");
        reservedWords.add("input");
        reservedWords.add("do");
        reservedWords.add("loop");
        reservedWords.add("until");
        reservedWords.add("function");
        reservedWords.add("sub");
        reservedWords.add("endfunction");
        reservedWords.add("endsub");
        reservedWords.add("declare");
        reservedWords.add("runtime");
        reservedWords.add("bindcode");
        reservedWords.add("exec");
        reservedWords.add("include");
        reservedWords.add("arraymax");		// Library mechanism doesn't support any-type parameters. Compiler needs to treat arraymax() as a special case
        reservedWords.add("begincodeblock");
        reservedWords.add("endcodeblock");
    }

    public List<String> getUnaryOperators() {
        return new ArrayList<>(unaryOperators.keySet());
    }

    public List<String> getBinaryOperators() {
        return new ArrayList<>(binaryOperators.keySet());
    }

    void clearState() {
        regType = new ValType(BasicValType.VTP_INT);
        reg2Type = new ValType(BasicValType.VTP_INT);
        freeTempData = false;
        operandStack.clear();
        operatorStack.clear();
        jumps.clear();
        resets.clear();
        flowControls.clear();
        syntax = DEFAULT_SYNTAX;
        inFunction = false;

        // No local user functions defined initially.
        // Visible functions are the global functions.
        localUserFunctionIndex.clear();
        visibleUserFunctionIndex = globalUserFunctionIndex;
    }

    /**
     * Clear existing program
     */
    public void clearProgram() {

        // Clear existing program
        vm.clearProgram();
        lastLine = 0;
        lastCol = 0;

        // Clear state
        globalUserFunctionIndex.clear();
        userFunctionReverseIndex.clear();
        runtimeFunctionIndex.clear();
        clearState();
        parser.reset();
        programConstants.clear();
        labels.clear();
        labelIndex.clear();

        currentFunction = -1;
        currentCodeBlockIndex = -1;

        initPlugins();
        runtimeFunctions.clear();
    }

    public boolean compile() {

        // Clear existing program
        clearProgram();

        // Compile source code
        internalCompile();

        return !hasError();
    }

    /**
     * Compile code and append to end of program.
     * Like compile(), but does not clear out the existing program first.
     */
    public boolean compileOntoEnd() {
        clearState();
        lastLine = 0;
        lastCol = 0;
        parser.reset();
        internalCompile();
        return !hasError();
    }

    /**
     * Check parser for error
     * Copy error state (if any)
     * @return true if no error, false if error (in which case error state is set)
     */
    boolean checkParser() {
        if (parser.hasError()) {
            setError("Parse error: " + parser.getError());
            parser.clearError();
            return false;
        }
        return true;
    }

    void initPlugins() {
        this.plugins.getStructureManager().addVMStructures(vm.getDataTypes());
        this.plugins.createVMFunctionSpecs();
    }

    boolean getToken() {
        return getToken(false, false);
    }

    boolean getToken(boolean skipEOL, boolean dataMode) {

        // Read a token
        token = parser.nextToken(skipEOL, dataMode);
        if (!checkParser()) {
            return false;
        }

        // Text token processing
        if (token.getTokenType() == TokenType.CTT_TEXT) {

            // Apply case sensitivity
            if (!isCaseSensitive) {
                token.setText(token.getText().toLowerCase());
            }

            // Match against reserved keywords
            if (reservedWords.contains(token.getText())) {
                token.setTokenType(TokenType.CTT_KEYWORD);
            }

            // Match against external functions
            else if (isFunction(token.getText())) {
                token.setTokenType(TokenType.CTT_FUNCTION);
            } else if (isUserFunction(token.getText())) {
                token.setTokenType(TokenType.CTT_USER_FUNCTION);
            } else if (isRuntimeFunction(token.getText())) {
                token.setTokenType(TokenType.CTT_RUNTIME_FUNCTION);
            } else {

                // Match against constants

                // Try permanent constants first
                Constant constant;
                boolean isConstant;
                constant = constants.get(token.getText());
                isConstant = (constant != null);

                // Try plugin constants next
                if (!isConstant) {
                    constant = this.plugins.findConstant(this.token.getText());
                    isConstant = (constant != null);
                }
                // Otherwise try program constants
                if (!isConstant) {
                    constant = programConstants.get(token.getText());
                    isConstant = (constant != null);
                }

                if (isConstant) {
                    // Replace token with constant
                    token.setTokenType(TokenType.CTT_CONSTANT);

                    // Replace text with text value of constant
                    switch (constant.getBasicType()) {
                        case BasicValType.VTP_INT:
                            token.setText(String.valueOf(constant.getIntValue()));
                            break;
                        case BasicValType.VTP_REAL:
                            token.setText(String.valueOf(constant.getRealValue()));
                            break;
                        case BasicValType.VTP_STRING:
                            token.setText(constant.getStringValue());
                            break;
                        default:
                            break;
                    }
                    token.setValType(constant.getBasicType());
                }
            }
        } else if (token.getTokenType() == TokenType.CTT_CONSTANT && token.getValType() == BasicValType.VTP_STRING) {

            // 19-Jul-2003
            // Prefix string text constants with "S". This prevents them
            // matching
            // any recognised keywords (which are stored in lower case).
            // (This is basically a work around to existing code which doesn't
            // check the token type if it matches a reserved keyword).
            token.setText((String) "S" + token.getText());
        }
        return true;
    }

    void internalCompile() {

        // Allocate a new code block
        currentCodeBlockIndex = vm.newCodeBlock();
        boolean isMainProgram = currentCodeBlockIndex == 0;

        // Clear error state
        clearError();
        parser.clearError();

        // Read initial token
        if (!getToken(true, false)) {
            return;
        }

        // Compile code
        while (!parser.isEof() && compileInstruction()) {}

        // Terminate program
        addInstruction(OpCode.OP_END, BasicValType.VTP_INT, new Value());

        if (!hasError()) {
            // Link up gotos
            for (Jump jump : jumps) {

                // Find instruction
                assertTrue(jump.jumpInstruction < vm.getInstructionCount());
                Instruction instr = vm.getInstruction(jump.jumpInstruction);

                // Point token to goto instruction, so that it will be displayed
                // if there is an error.
                token.setLine(instr.sourceLine);
                token.setCol(instr.sourceChar);

                // Label must exist
                if (!labelExists(jump.labelName)) {
                    setError("Label: " + jump.labelName + " does not exist");
                    return;
                }

                // Patch in offset
                instr.value.setIntVal(getLabel(jump.labelName).offset);
            }

            // Link up resets
            for (Jump jump : resets) {

                // Find instruction
                assertTrue(jump.jumpInstruction < vm.getInstructionCount());
                Instruction instr = vm.getInstruction(jump.jumpInstruction);

                // Point token to reset instruction, so that it will be
                // displayed
                // if there is an error.
                token.setLine(instr.sourceLine);
                token.setCol(instr.sourceChar);

                // Label must exist
                if (!labelExists(jump.labelName)) {
                    setError("Label: " + jump.labelName + " does not exist");
                    return;
                }

                // Patch in data offset
                instr.value.setIntVal(getLabel(jump.labelName).programDataOffset);
            }

            // Check for open function or flow control structures
            if (!checkUnclosedUserFunction()) {
                return;
            }

            if (!checkUnclosedFlowControl()) {
                return;
            }

            // Check for not implemented forward declared functions
            if (!checkFwdDeclFunctions()) {
                return;
            }

            if (!checkFwdDeclStructures()) {
                return;
            }

            if (isMainProgram && !checkUnclosedCodeBlock()) {
                return;
            }
        }
    }

    boolean needAutoEndif() {

        // Look at the top-most control structure
        if (flowControls.isEmpty()) {
            return false;
        }

        FlowControl top = getFlowControlTOS();

        // Auto endif required if top-most flow control is a non-block "if" or
        // "else"
        return (top.controlType == FlowControlType.FCT_IF || top.controlType == FlowControlType.FCT_ELSE)
                && !top.blockIf;
    }

    public boolean isFunction(String name) {
        return isBuiltinFunction(name) || this.plugins.isPluginFunction(name);
    }

    public TomVM getVM() {
        return vm;
    }

    public Parser getParser() {
        return parser;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

     public PluginManager getPlugins() {
        return this.plugins;
     }

    // Constants
    public void addConstant(String name, Constant c) {
        constants.put(name.toLowerCase(), c);
    }

    public void addConstant(String name, String s) {
        addConstant(name, new Constant(s));
    }

    public void addConstant(String name, int i) {
        addConstant(name, new Constant(i));
    }

    public void addConstant(String name, long i) {
        addConstant(name, new Constant(i));
    }

    public void addConstant(String name, float r) {
        addConstant(name, new Constant(r));
    }

    public void addConstant(String name, double r) {
        addConstant(name, new Constant(r));
    }

    public Map<String, Constant> getConstants() {
        return constants;
    }

    // Functions
    public boolean isBuiltinFunction(String name) {
        return functionIndex.containsKey(name.toLowerCase());
    }

    public boolean isUserFunction(String name) {
        return visibleUserFunctionIndex.containsKey(name.toLowerCase());
    }

    public boolean isLocalUserFunction(String name) {
        return localUserFunctionIndex.containsKey(name.toLowerCase());
    }

    public boolean isGlobalUserFunction(String name) {
        return globalUserFunctionIndex.containsKey(name.toLowerCase());
    }

    public boolean isRuntimeFunction(String name) {
        return runtimeFunctionIndex.containsKey(name.toLowerCase());
    }

    public Vector<Library> getLibraries() {
        return libraries;
    }

    public Vector<FunctionSpecification> getFunctions() {
        return functions;
    }

    public Map<String, List<Integer>> getFunctionIndex() {
        return functionIndex;
    }

    public int getUserFunctionIndex(String name) {
        assertTrue(isUserFunction(name));
        String functionName = name.toLowerCase();
        if (isLocalUserFunction(name)) {
            return getCurrentCodeBlock().userFunctions.get(functionName);
        } else {
            return globalUserFunctionIndex.get(functionName);
        }
    }

    // Language extension
    public void addUnOperExtension(UnaryOperatorExtension e) {
        unaryOperatorExtensions.add(e);
    }

    public void addBinOperExtension(BinaryOperatorExtension e) {
        binaryOperatorExtensions.add(e);
    }

    // Language features (for context highlighting)
    public boolean isReservedWord(String text) {
        return reservedWords.contains(text);
    }

    public boolean isConstant(String text) {

        // Check built in constants
        if (constants.containsKey(text)) {
            return true;
        }

        // Check Plugin constants
         Constant compConst = new Constant();
         return (this.plugins.findConstant(text) != null);
    }

    public boolean isBinaryOperator(String text) {
        return binaryOperators.containsKey(text);
    }

    public boolean isUnaryOperator(String text) {
        return unaryOperators.containsKey(text);
    }

    public boolean isOperator(String text) {
        return isBinaryOperator(text) || isUnaryOperator(text);
    }

    public long getTokenLine() {
        return token.getLine();
    }

    public long getTokenColumn() {
        return token.getCol();
    }

    public LanguageSyntax getLanguageSyntax() {
        return syntax;
    }

    public String getSymbolPrefix() {
        return symbolPrefix;
    }

    public void setSymbolPrefix(String prefix) {
        symbolPrefix = prefix;
    }

    public int getDefaultDataBasicValType() {
        // In traditional suffix mode, the default data type is floating point.
        // Otherwise the default Basic4GL data type is integer.
        return syntax == LS_TRADITIONAL_SUFFIX
                ? BasicValType.VTP_REAL
                : BasicValType.VTP_INT;
    }
    // Misc
    boolean labelExists(String labelText) {
        return labels.containsKey(labelText);
    }

    Label getLabel(String labelText) {
        assertTrue(labelExists(labelText));
        return labels.get(labelText);
    }

    void addLabel(String labelText, Label label) {
        assertTrue(!labelExists(labelText));
        labels.put(labelText, label);
        labelIndex.put(label.offset, labelText);
    }

    FlowControl getFlowControlTOS() {
        assertTrue(!flowControls.isEmpty());
        return flowControls.get(flowControls.size() - 1);
    }

    boolean isFlowControlTopEqual(FlowControlType type) {
        return !flowControls.isEmpty() && getFlowControlTOS().controlType == type;
    }

    UserFunc getCurrentUserFunction() {
        // Return function currently being declared
        assertTrue(!vm.getUserFunctions().isEmpty());
        assertTrue(currentFunction >= 0);
        assertTrue(currentFunction < vm.getUserFunctions().size());
        return vm.getUserFunctions().get(currentFunction);
    }

    UserFuncPrototype getCurrentUserFunctionPrototype() {
        // Return prototype of function currently being declared
        assertTrue(!vm.getUserFunctionPrototypes().isEmpty());
        assertTrue(getCurrentUserFunction().prototypeIndex >= 0);
        assertTrue(getCurrentUserFunction().prototypeIndex
                < vm.getUserFunctionPrototypes().size());
        return vm.getUserFunctionPrototypes().get(getCurrentUserFunction().prototypeIndex);
    }

    private boolean compileBindCodeInternal() {

        // Evaluate code handle
        if (!compileExpression()) {
            return false;
        }

        if (!compileConvert(BasicValType.VTP_INT)) {
            return false;
        }

        // Add bindcode op-code
        addInstruction(OpCode.OP_BINDCODE, BasicValType.VTP_INT, new Value());

        return true;
    }

    private boolean compileBindCode() {

        // Skip "bindcode" keyword
        if (!getToken()) {
            return false;
        }

        // Combine bind code
        return compileBindCodeInternal();
    }

    private boolean compileExec() {

        // Skip "exec" keyword
        if (!getToken()) {
            return false;
        }

        // Check if explicit code block specified
        if (!atSeparatorOrSpecial()) {
            if (!compileBindCodeInternal()) {
                return false;
            }
        }

        // Add exec op-code
        addInstruction(OpCode.OP_EXEC, BasicValType.VTP_INT, new Value());

        return true;
    }

    public CodeBlock getCurrentCodeBlock() {
        assertTrue(vm.isCodeBlockValid(currentCodeBlockIndex));
        return vm.getCodeBlock(currentCodeBlockIndex);
    }

    public RollbackPoint getRollbackPoint() {
        RollbackPoint r = new RollbackPoint();

        // Get virtual machine rollback info
        r.vmRollback = vm.getRollbackPoint();

        // Get compiler rollback info
        r.runtimeFunctionCount = runtimeFunctions.size();

        return r;
    }

    public void rollback(RollbackPoint rollbackPoint) {

        // Rollback virtual machine
        vm.rollback(rollbackPoint.vmRollback);

        // Rollback compiler

        // Remove new labels
        // (We can detect these as any labels with an offset past the instruction
        // count stored in the rollback).
        for (Iterator<Map.Entry<String, Label>> it = labels.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Label> entry = it.next();
            if (entry.getValue().offset >= rollbackPoint.vmRollback.instructionCount) {
                it.remove();
            }
        }

        // Remove global function name->index records
        // (Can detect them as any global function with an invalid function index)
        for (Iterator<Map.Entry<String, Integer>> it =
                        globalUserFunctionIndex.entrySet().iterator();
                it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            if (entry.getValue() >= rollbackPoint.vmRollback.functionCount) {
                it.remove();
            }
        }

        // Remove function index->name records (used for debugging)
        for (Iterator<Map.Entry<Integer, String>> it =
                        userFunctionReverseIndex.entrySet().iterator();
                it.hasNext(); ) {
            Map.Entry<Integer, String> entry = it.next();
            if (entry.getKey() >= rollbackPoint.vmRollback.functionCount) {
                it.remove();
            }
        }

        // Remove runtime functions
        runtimeFunctions.setSize(rollbackPoint.runtimeFunctionCount);

        for (Iterator<Map.Entry<String, Integer>> it =
                        runtimeFunctionIndex.entrySet().iterator();
                it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            if (entry.getValue() >= rollbackPoint.runtimeFunctionCount) {
                it.remove();
            }
        }
    }

    boolean checkUnclosedFlowControl() {

        // Check for open flow control structures
        if (!flowControls.isEmpty()) {

            // Find topmost structure
            FlowControl top = getFlowControlTOS();

            // Point parser to it
            parser.setPos(top.sourcePos.getSourceLine(), top.sourcePos.getSourceColumn());

            // Set error text
            switch (getFlowControlTOS().controlType) {
                case FCT_IF:
                    setError("'if' without 'endif'");
                    break;
                case FCT_ELSE:
                    setError("'else' without 'endif'");
                    break;
                case FCT_FOR:
                    setError("'for' without 'next'");
                    break;
                case FCT_WHILE:
                    setError("'while' without 'wend'");
                    break;
                case FCT_DO_PRE:
                case FCT_DO_POST:
                    setError("'do' without 'loop'");
                    break;
                default:
                    setError("Open flow control structure");
                    break;
            }

            return false;
        } else {
            return true;
        }
    }

    boolean checkUnclosedUserFunction() {
        if (inFunction) {

            // Point parser to function
            parser.setPos(functionStart.getSourceLine(), functionStart.getSourceColumn());

            // Return error
            if (getCurrentUserFunctionPrototype().hasReturnVal) {
                setError("'function' without 'endfunction'");
            } else {
                setError("'sub' without 'endsub'");
            }
            return false;
        } else {
            return true;
        }
    }

    boolean checkFwdDeclFunctions() {

        // Look for function that is declared, but not yet implemented
        for (String key : localUserFunctionIndex.keySet()) {
            if (!vm.getUserFunctions().get(localUserFunctionIndex.get(key)).implemented) {
                setError((String) "Function/sub '" + key + "' was DECLAREd, but not implemented");
                return false;
            }
        }

        return true;
    }

    boolean checkFwdDeclStructures()
    {
        for (Structure s : vm.getDataTypes().getStructures())
        {
            if (!s.isDefined)
            {
                setError("Structure '" + s.name + "' was DECLAREd, but not implemented");
                return false;
            }
        }

        return true;
    }

    boolean checkUnclosedCodeBlock()
    {
        if (currentCodeBlockIndex != 0)
        {
            setError("'begincodeblock' without 'endcodeblock'");
            return false;
        }

        return true;
    }

    /**
     * Add forced or conditional timesharing break op-code if necessary
     */
    void addTimeshareOpCode(ExtendedFunctionSpecification fn) {
        addTimeshareOpCode(fn.getSpecification());
    }

    /**
     * Add forced or conditional timesharing break op-code if necessary
     */
    void addTimeshareOpCode(FunctionSpecification fn)
    {
        if (fn.getTimeshare()) {
            addInstruction(OpCode.OP_TIMESHARE, BasicValType.VTP_INT, new Value());
        }
        else if (fn.getConditionalTimeshare()) {
            addInstruction(OpCode.OP_COND_TIMESHARE, BasicValType.VTP_INT, new Value());
        }
    }

    // Compilation
    void addInstruction(short opCode, int basictype, Value val) {

        // Add instruction, and include source code position audit
        int line = token.getLine(), col = token.getCol();

        // Prevent line and col going backwards. (Debugging tools rely on
        // source-offsets never decreasing as source code is traversed.)
        if (line < lastLine || (line == lastLine && col < lastCol)) {
            line = lastLine;
            col = lastCol;
        }
        vm.addInstruction(new Instruction(opCode, basictype, val, line, col));
        lastLine = line;
        lastCol = col;
    }

    private void addFunctionCallInstruction(ExtendedFunctionSpecification spec) {
        // Generate code to call function
        if (spec.isBuiltin())
        {
            // Builtin function
            addInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT, new Value(spec.getSpecification().getIndex()));
        } else {
            // DLL function
            // Note: The DLL index is encoded as the high byte.
            // The 3 low bytes are the function index within the DLL.
            // This may be revised later...
            addInstruction(
                    OpCode.OP_CALL_DLL,
                    BasicValType.VTP_INT,
                    new Value((spec.getPluginIndex() << 24) | (spec.getSpecification().getIndex() & 0x00ffffff)));
        }
    }

    private boolean compileInstruction() {
        // Instructions by default must be separated by colons
        needColon = true;

        // Is it a label?
        Token nextToken = parser.peekToken(false, false);
        if (!checkParser()) {
            return false;
        }
        if (token.isNewLine()
                && token.getTokenType() == TokenType.CTT_TEXT
                && nextToken.getText().equals(":")) {

            // Labels cannot exist inside subs/functions
            if (inFunction) {
                setError((String) "You cannot use a label inside a function or subroutine");
                return false;
            }

            // Label declaration
            String labelName = symbolPrefix + token.getText();

            // Must not already exist
            if (labelExists(labelName)) {
                setError("Duplicate label name: " + labelName);
                return false;
            }

            // Create new label
            addLabel(
                    labelName,
                    new Label(vm.getInstructionCount(), vm.getProgramData().size()));

            // Skip label
            if (!getToken()) {
                return false;
            }
        }
        // Determine the type of instruction, based on the current token
        else if (token.getText().equals("struc") || token.getText().equals("type")) {
            if (!compileStructure()) {
                return false;
            }
        } else if (token.getText().equals("dim")) {
            if (!compileDim(false, false)) {
                return false;
            }
        } else if (token.getText().equals("goto")) {
            if (!getToken()) {
                return false;
            }
            if (!compileGoto(OpCode.OP_JUMP)) {
                return false;
            }
        } else if (token.getText().equals("gosub")) {
            if (!getToken()) {
                return false;
            }
            if (!compileGoto(OpCode.OP_CALL)) {
                return false;
            }
        } else if (token.getText().equals("return")) {
            if (!compileReturn()) {
                return false;
            }
        } else if (token.getText().equals("if")) {
            if (!compileIf(false)) {
                return false;
            }
        } else if (token.getText().equals("elseif")) {
            if (!(compileElse(true) && compileIf(true))) {
                return false;
            }
        } else if (token.getText().equals("else")) {
            if (!compileElse(false)) {
                return false;
            }
        } else if (token.getText().equals("endif")) {
            if (!compileEndIf(false)) {
                return false;
            }
        } else if (token.getText().equals("for")) {
            if (!compileFor()) {
                return false;
            }
        } else if (token.getText().equals("next")) {
            if (!compileNext()) {
                return false;
            }
        } else if (token.getText().equals("while")) {
            if (!compileWhile()) {
                return false;
            }
        } else if (token.getText().equals("wend")) {
            if (!compileWend()) {
                return false;
            }
        } else if (token.getText().equals("do")) {
            if (!compileDo()) {
                return false;
            }
        } else if (token.getText().equals("loop")) {
            if (!compileLoop()) {
                return false;
            }
        } else if (token.getText().equals("end")) {
            if (!getToken()) {
                return false;
            }

            // Special case! "End" immediately followed by "if" is syntactically
            // equivalent to "endif"
            if (token.getText().equals("if")) {
                if (!compileEndIf(false)) {
                    return false;
                }
            }
            // Special case! "End" immediately followed by "function" is
            // syntactically equivalent to "endfunction"
            else if (token.getText().equals("function")) {
                if (!compileEndUserFunction(true)) {
                    return false;
                }
            } else if (token.getText().equals("sub")) {
                if (!compileEndUserFunction(false)) {
                    return false;
                }
            } else
            {
                // Otherwise is "End" program instruction
                addInstruction(OpCode.OP_END, BasicValType.VTP_INT, new Value());
            }
        } else if (token.getText().equals("run")) {
            if (!getToken()) {
                return false;
            }
            addInstruction(OpCode.OP_RUN, BasicValType.VTP_INT, new Value());
        } else if (token.getText().equals("const")) {
            if (!compileConstant()) {
                return false;
            }
        } else if (token.getText().equals("alloc")) {
            if (!compileAlloc()) {
                return false;
            }
        } else if (token.getText().equals("data")) {
            if (!compileData()) {
                return false;
            }
        } else if (token.getText().equals("read")) {
            if (!compileDataRead()) {
                return false;
            }
        } else if (token.getText().equals("reset")) {
            if (!compileDataReset()) {
                return false;
            }
        } else if (token.getText().equals("print")) {
            if (!compilePrint(false)) {
                return false;
            }
        } else if (token.getText().equals("printr")) {
            if (!compilePrint(true)) {
                return false;
            }
        } else if (token.getText().equals("input")) {
            if (!compileInput()) {
                return false;
            }
        } else if (token.getText().equals("language")) {
            if (!compileLanguage()) {
                return false;
            }
        } else if (token.getText().equals("function") || token.getText().equals("sub")) {
            if (!compileUserFunction(UserFunctionType.UFT_IMPLEMENTATION)) {
                return false;
            }
        } else if (token.getText().equals("endfunction")) {
            if (!compileEndUserFunction(true)) {
                return false;
            }
        } else if (token.getText().equals("endsub")) {
            if (!compileEndUserFunction(false)) {
                return false;
            }
        } else if (token.getText().equals("declare")) {
            if (!compileUserFunctionFwdDecl()) {
                return false;
            }
        } else if (token.getText().equals("runtime")) {
            if (!compileUserFunctionRuntimeDecl()) {
                return false;
            }
        } else if (token.getText().equals("bindcode")) {
            if (!compileBindCode()) {
                return false;
            }
        } else if (token.getText().equals("exec")) {
            if (!compileExec()) {
                return false;
            }

        } else if (token.getText().equals("begincodeblock")) {
            if (!compileBeginCodeBlock()) {
                return false;
            }
        }
        else if (token.getText().equals("endcodeblock")) {
            if (!compileEndCodeBlock()) {
                return false;
            }
        } else if (token.getTokenType() == TokenType.CTT_FUNCTION) {
            if (!compileFunction()) {
                return false;
            }
        } else if (token.getTokenType() == TokenType.CTT_USER_FUNCTION) {
            if (!compileUserFunctionCall(false, false, false)) {
                return false;
            }
        } else if (token.getTokenType() == TokenType.CTT_RUNTIME_FUNCTION) {
            if (!compileUserFunctionCall(false, true, false)) {
                return false;
            }
        } else if (!compileAssignment()) {
            return false;
        }

        // Free any temporary data (if necessary)
        if (!compileFreeTempData()) {
            return false;
        }

        // Skip separators (:, EOL)
        if (!skipSeparators()) {
            return false;
        }

        return true;
    }

    boolean atSeparator() {
        return token.getTokenType() == TokenType.CTT_EOL
                || token.getTokenType() == TokenType.CTT_EOF
                || token.getText().equals(":");
    }

    boolean atSeparatorOrSpecial() {

        // Note: Endif is special, as it doesn't require a preceding colon to
        // separate
        // from other instructions on the same line.
        // e.g.
        // if CONDITION then INSTRUCTION(S) endif
        // Likewise else
        // if CONDITION then INSTRUCTION(S) else INSTRUCTION(S) endif
        //
        // (unlike while, which would have to be written as:
        // while CONDITION: INSTRUCTION(S): wend
        // )
        return atSeparator()
                || token.getText().equals("endif")
                || token.getText().equals("else")
                || token.getText().equals("elseif");
    }

    boolean skipSeparators() {

        // Expect separator. Either as an EOL or EOF or ':'
        if (needColon && !atSeparatorOrSpecial()) {
            setError("Expected ':'");
            return false;
        }

        // Skip separators
        while (token.getTokenType() == TokenType.CTT_EOL
                || token.getTokenType() == TokenType.CTT_EOF
                || token.getText().equals(":")) {

            // If we reach the end of the line, insert any necessary implicit
            // "endifs"
            if (token.getTokenType() == TokenType.CTT_EOL || token.getTokenType() == TokenType.CTT_EOF) {

                // Generate any automatic endifs for the previous line
                while (needAutoEndif()) {
                    if (!compileEndIf(true)) {
                        return false;
                    }
                }

                // Convert any remaining flow control structures to block
                for (FlowControl flowControl : flowControls) {
                    flowControl.blockIf = true;
                }
            }

            if (!getToken(true, false)) {
                return false;
            }
        }

        return true;
    }

    private boolean compileBeginCodeBlock()
    {
        // A code block is compiled to mimic how the code would be compiled at runtime.
        // The compiler will allocate a code block number, and insert "jump around" and
        // "return" op codes around the compiled code.
        // This affects "runtime" function definitions and "GetFunctionByName" behaviour.
        // Programs can lookup code blocks at runtime with "GetCodeBlockByName" function.

        // Note for Note: "I" refers to Tom, the original author of this code :)
        // Note: This was implemented mainly so I could port my Thunderbolt game to Basic4GL Mobile,
        // without having to include the whole compiler in the runtime.
        // I don't expect this command will benefit anyone else, and would probably be more
        // confusing than helpful, so I'm going to leave it undocumented.

        // Code blocks cannot be nested.
        // Check we are in the main code block.
        if (currentCodeBlockIndex != 0)
        {
            setError("Must end previous code block with 'endcodeblock' before beginning a new one");
            return false;
        }

        // Also cannot start a code block within a function or flow control
        if (inFunction)
        {
            setError("Cannot begin a code block inside a user function");
            return false;
        }

        if (!flowControls.isEmpty())
        {
            setError("Cannot begin a code block inside a flow control structure");
            return false;
        }

        // Skip BEGINCODEBLOCK
        if (!getToken()) {
            return false;
        }

        // Code block "filename" follows
        Mutable<Value> dummy = new Mutable<>(new Value());
        Mutable<String> filename = new Mutable<>("");
        Mutable<Integer> valType = new Mutable<>(BasicValType.VTP_STRING);
        if (!evaluateConstantExpression(valType, dummy, filename)) {
            return false;
        }

        // To match runtime compilation code block will be terminated with OP_RETURN.
        // Therefore we can't just let execution carry on into the code block from the
        // previous instruction. Instead we generate a jump around.
        addInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(0));				// Offset will be filled in when code block ends

        // Allocate new code block
        currentCodeBlockIndex = vm.newCodeBlock();
        getCurrentCodeBlock().setFilename(filename.get());

        return true;
    }

    boolean compileEndCodeBlock()
    {
        // Must be inside code block
        if (currentCodeBlockIndex == 0)
        {
            setError("'endcodeblock' without 'begincodeblock'");
            return false;
        }

        // Also cannot end a code block within a function or flow control
        if (inFunction)
        {
            setError("Cannot end a code block inside a user function");
            return false;
        }

        if (!flowControls.isEmpty())
        {
            setError("Cannot end a code block inside a flow control structure");
            return false;
        }


        // Skip ENDCODEBLOCK
        if (!getToken()) {
            return false;
        }

        // Add OP_RETURN to end
        addInstruction(OpCode.OP_RETURN, BasicValType.VTP_INT, new Value());

        // Set "jump around offset"
        int jumpOpCode = getCurrentCodeBlock().programOffset - 1;
        vm.getInstruction(jumpOpCode).value = new Value((int)vm.getInstructionCount());

        // Resume compiling main code block
        currentCodeBlockIndex = 0;

        return true;
    }

    private boolean compileStructure() {

        // Skip STRUC
        // Record whether "struc" or "type" was used to declare type
        String keyword = token.getText();
        if (!getToken()) {
            return false;
        }

        // Check that we are not already inside a function
        if (inFunction) {
            setError("Cannot define a structure inside a function or subroutine");
            return false;
        }

        // Check that we are not inside a control structure
        if (!checkUnclosedFlowControl()) {
            return false;
        }

        // Expect structure name
        if (token.getTokenType() != TokenType.CTT_TEXT) {
            setError("Expected structure name");
            return false;
        }
        String name = symbolPrefix + token.getText();
        if (!checkName(name)) {
            return false;
        }

        // Check if already declared
        if (vm.getDataTypes().isStrucStored(name)) {
            vm.getDataTypes().makeStrucCurrent(name);

            // If already declared we can define it, but only if it has not already been defined
            if (vm.getDataTypes().getCurrentStruc().isDefined) {
                setError("'" + name + "' has already been used as a structure name");
                return false;
            }

            // Define existing structure
            vm.getDataTypes().defineExistingStruc();
        } else {
            // Create and define new structure
            vm.getDataTypes().createStruc(name);
        }

        // Skip structure name
        if (!getToken())
        {
            return false;
        }

        // Require : or new line
        if (!skipSeparators())
        {
            return false;
        }

        // Expect at least one field
        if (token.getText().equals("endstruc") || token.getText().equals("end")) {
            setError("Expected DIM or field name");
            return false;
        }

        // Populate with fields
        while (!token.getText().equals("endstruc") && !token.getText().equals("end")) {

            // dim statement is optional
            /*
             * if (token.text != "dim") { setError
             * ("Expected 'dim' or 'endstruc'"); return false; }
             */
            if (!compileDim(true, false)) {
                return false;
            }

            if (!skipSeparators()) {
                return false;
            }
        }

        if (token.getText().equals("end")) {

            // Skip END
            if (!getToken()) {
                return false;
            }

            // Check END keyword matches declaration keyword
            if (!token.getText().equals(keyword)) {
                setError("Expected '" + keyword + "'");
                return false;
            }

            // Skip STRUC/TYPE
            if (!getToken()) {
                return false;
            }
        } else {

            // Make sure "struc" was declared
            if (!keyword.equals("struc")) {
                setError("Expected 'END '" + keyword + "'");
                return false;
            }

            // Skip ENDSTRUC
            if (!getToken()) {
                return false;
            }
        }

        // Mark structure as defined
        vm.getDataTypes().getCurrentStruc().isDefined = true;

        return true;
    }

    private boolean compileDim(boolean forStruc, boolean forFuncParam) {
        return compileDim(forStruc, forFuncParam, new Mutable<>(new UserFuncPrototype()));
    }

    private boolean compileDim(boolean forStruc, boolean forFuncParam, Mutable<UserFuncPrototype> prototype) {

        // Skip optional DIM
        if (token.getText().equals("dim")) {
            if (!getToken()) {
                return false;
            }
        }

        // Expect at least one field in dim
        if (atSeparatorOrSpecial()) {
            setError("Expected variable declaration");
            return false;
        }

        // Parse fields in dim
        boolean needComma = false; // First element doesn't need a comma
        while (!atSeparatorOrSpecial() && (!forFuncParam || !token.getText().equals(")"))) {

            // Handle commas
            if (needComma) {
                if (!token.getText().equals(",")) {
                    setError("Expected ','");
                    return false;
                }
                if (!getToken()) {
                    return false;
                }
            }
            needComma = true; // Remaining elements do need commas

            // Extract field type
            String name = "";
            ValType type = new ValType();
            // Create wrappers to pass values by reference
            Mutable<String> nameRef = new Mutable<>(name);
            Mutable<ValType> typeRef = new Mutable<>(type);
            if (!compileDimField(nameRef, typeRef, forStruc, forFuncParam)) {
                return false;
            }
            // Update local values from wrappers
            name = nameRef.get();
            type = typeRef.get();

            if (!checkName(name)) {
                return false;
            }

            if (forStruc) {

                // Validate field name and type
                Structure struc = vm.getDataTypes().getCurrentStruc();
                if (vm.getDataTypes().isFieldStored(struc, name)) {
                    setError((String) "Field '" + name + "' has already been DIMmed in structure '" + struc.name + "'");
                    return false;
                }

                // Embedded structures must be fully defined (pointers to declared but undefined structures are okay though)
                if (type.pointerLevel == 0 && type.basicType >= 0)
                {
                    Structure s = vm.getDataTypes().getStructures().get(type.basicType);
                    if (!s.isDefined)
                    {
                        setError("Cannot include not yet implemented structure '" + s.name + "'");
                        return false;
                    }
                }

                // Add field to structure
                vm.getDataTypes().createField(name, type);
            } else if (forFuncParam) {

                // Check parameter of the same name has not already been added
                int varIndex = prototype.get().getLocalVar(name);
                if (varIndex >= 0) {
                    setError("There is already a function parameter called '" + name + "'");
                    return false;
                }

                // Add parameter type to function definition
                prototype.get().newParam(name, type);

            } else {
                // Check parameter data type has been defined
                if (!checkTypeIsDefined(type)) {
                    return false;
                }

                // Regular DIM.
                // Prefix DIMmed variable names
                name = symbolPrefix + name;

                if (inFunction) {

                    // Local variable

                    // Check if variable has already been DIMmed locally.
                    // (This is allowed, but only if DIMmed to the same type.)
                    int varIndex = getCurrentUserFunctionPrototype().getLocalVar(name);
                    if (varIndex >= 0) {
                        if (!(getCurrentUserFunctionPrototype()
                                .localVarTypes
                                .get(varIndex)
                                .equals(type))) {
                            setError("Local variable '" + name + "' has already been allocated as a different type.");
                            return false;
                        }

                        // Var already created (earlier), so fall through
                        // to allocation code generation
                    } else
                    {
                        // Create new variable
                        varIndex = getCurrentUserFunctionPrototype().newLocalVar(name, type);
                    }

                    // Generate code to allocate local variable data
                    // Note: Opcode contains the index of the variable. Var
                    // type and size data stored in the user function defn.
                    addInstruction(OpCode.OP_DECLARE_LOCAL, BasicValType.VTP_INT, new Value(varIndex));

                    // Data containing strings will need to be "destroyed" when
                    // the stack unwinds.
                    if (vm.getDataTypes().containsString(type)) {
                        addInstruction(OpCode.OP_REG_DESTRUCTOR, BasicValType.VTP_INT, new Value((int)
                                vm.getStoreTypeIndex(type)));
                    }

                    // Optional "= value"?
                    if (token.getText().equals("=")) {

                        // Add opcode to load variable address
                        addInstruction(OpCode.OP_LOAD_LOCAL_VAR, BasicValType.VTP_INT, new Value(varIndex));

                        // Set register type
                        regType.setType(
                                getCurrentUserFunctionPrototype().localVarTypes.get(varIndex));
                        regType.pointerLevel++;

                        // Compile single deref.
                        // Unlike standard variable assignment, we don't
                        // automatically
                        // deref pointers here. (Otherwise it would be
                        // impossible to
                        // set the pointer within the DIM statement).
                        if (!compileDeref()) {
                            return false;
                        }

                        // Compile the rest of the assignment
                        if (!internalCompileAssignment()) {
                            return false;
                        }
                    }

                } else {

                    // Check if variable has already been DIMmed. (This is
                    // allowed, but
                    // only if DIMed to the same type.)
                    int varIndex = vm.getVariables().getVariableIndex(name);
                    if (varIndex >= 0) {
                        if (!(vm.getVariables()
                                .getVariables()
                                .get(varIndex)
                                .type
                                .equals(type))) {
                            setError("Var '" + name + "' has already been allocated as a different type.");
                            return false;
                        }

                        // Var already created (earlier), so fall through
                        // to allocation code generation.
                    } else {
                        // Create new variable
                        varIndex = vm.getVariables().createVar(name, type);
                    }

                    // Generate code to allocate variable data
                    // Note: Opcode contains the index of the variable. Var
                    // type
                    // and size data is stored in the variable entry.
                    addInstruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(varIndex));

                    // Optional "= value"?
                    if (token.getText().equals("=")) {

                        // Add opcode to load variable address
                        addInstruction(OpCode.OP_LOAD_VAR, BasicValType.VTP_INT, new Value(varIndex));

                        // Set register type
                        regType.setType(vm.getVariables().getVariables().get(varIndex).type);
                        regType.pointerLevel++;

                        // Compile single deref.
                        // Unlike standard variable assignment, we don't
                        // automatically
                        // deref pointers here. (Otherwise it would be
                        // impossible to
                        // set the pointer within the DIM statement).
                        if (!compileDeref()) {
                            return false;
                        }

                        // Compile the rest of the assignment
                        if (!internalCompileAssignment()) {
                            return false;
                        }
                    }
                }

                // If this was an array and not a pointer, then its array
                // indices
                // will have been pushed to the stack.
                // The DECLARE operator automatically removes them however
                if (type.getPhysicalPointerLevel() == 0) {
                    for (int i = 0; i < type.arrayLevel; i++) {
                        operandStack.remove(operandStack.size() - 1);
                    }
                }
            }
        }
        return true;
    }

    private boolean compileTokenName(Mutable<String> name, Mutable<TokenType> tokenType, boolean allowSuffix) {
        tokenType.set(token.getTokenType());
        name.set(token.getText());

        if (tokenType.get() != TokenType.CTT_TEXT
                && tokenType.get() != TokenType.CTT_USER_FUNCTION
                && tokenType.get() != TokenType.CTT_RUNTIME_FUNCTION) {
            setError("Expected name");
            return false;
        }

        if (!allowSuffix) {
            char last = name.get().charAt(name.get().length() - 1);
            if (last == '#' || last == '%' || last == '$' || last == '!') {
                setError("Subroutine names cannot end with: " + last);
                return false;
            }
        }

        if (!getToken()) {
            return false;
        }

        return true;
    }

    private boolean compileDataType(Mutable<String> name, Mutable<ValType> type, Mutable<TokenType> tokenType) {

        type.get().setType(BasicValType.VTP_UNDEFINED);
        name.set("");

        // Look for structure type
        if (token.getTokenType() == TokenType.CTT_TEXT) {
            String structureName = symbolPrefix + token.getText();
            int i = vm.getDataTypes().getStrucIndex(structureName);
            if (i >= 0) {
                type.get().basicType = i;
                if (!getToken()) // Skip token type keyword
                {
                    return false;
                }
            }
        }

        // Look for preceeding & (indicates pointer)
        if (token.getText().equals("&")) {
            type.get().pointerLevel++;
            if (!getToken()) {
                return false;
            }
        }

        // Look for variable name
        if (!compileTokenName(name, tokenType, true)) {
            return false;
        }

        // Determine variable type
        char last = '\0';
        if (!name.get().isEmpty()) {
            last = name.get().charAt(name.get().length() - 1);
        }
        if (type.get().basicType == BasicValType.VTP_UNDEFINED) {
            if (last == '$') {
                type.get().basicType = BasicValType.VTP_STRING;
            } else if (last == '#' || last == '!') {
                type.get().basicType = BasicValType.VTP_REAL;
            } else if (last == '%') {
                type.get().basicType = BasicValType.VTP_INT;
            }
        } else {
            if (last == '$' || last == '#' || last == '%' || last == '!') {
                setError((String) "\"" + name + "\" is a structure variable, and cannot end with #, $, % or !");
                return false;
            }
        }

        return true;
    }

    private boolean compileAs(Mutable<String> name, Mutable<ValType> type) {
        if (type.get().basicType != BasicValType.VTP_UNDEFINED) {
            setError("'" + name + "'s type has already been defined. Cannot use 'as' here.");
            return false;
        }

        // Skip "as"
        if (!getToken()) {
            return false;
        }

        // Expect "single", "double", "integer", "string" or a structure type
        if (token.getTokenType() != TokenType.CTT_TEXT && token.getTokenType() != TokenType.CTT_KEYWORD) {
            setError("Expected 'single', 'double', 'integer', 'string' or type name");
            return false;
        }
        if (token.getText().equals("integer")) {
            type.get().basicType = BasicValType.VTP_INT;
        } else if (token.getText().equals("single") || token.getText().equals("double")) {
            // Note: Basic4GL supports only one type of floating point number.
            // We will accept both keywords, but simply allocate a real (=
            // single
            // precision) floating point number each time.
            type.get().basicType = BasicValType.VTP_REAL;
        } else if (token.getText().equals("string")) {
            type.get().basicType = BasicValType.VTP_STRING;
        }
        else if (token.getText().equals("function") || token.getText().equals("sub"))
        {
            // Function or sub
            return compileUserFunction(UserFunctionType.UFT_POINTER, type);
        }else {

            // Look for recognised structure name
            String structureName = symbolPrefix + token.getText();
            int i = vm.getDataTypes().getStrucIndex(structureName);
            if (i < 0) {
                setError("Expected 'single', 'double', 'integer', 'string' or type name");
                return false;
            }
            type.get().basicType = i;
        }

        // Skip type name
        if (!getToken()) {
            return false;
        }

        return true;
    }

    private boolean compileDimField(
            Mutable<String> name, Mutable<ValType> type, boolean forStruc, boolean forFuncParam) {

        // Compile data type
        TokenType tokenType = TokenType.CTT_CONSTANT;
        Mutable<TokenType> tokenTypeRef = new Mutable<>(tokenType);
        if (!compileDataType(name, type, tokenTypeRef)) {
            return false;
        }
        tokenType = tokenTypeRef.get(); // Update local value from reference

        // Name token must be text
        if (tokenType != TokenType.CTT_TEXT) {
            setError("Expected variable name");
            return false;
        }

        // Look for array dimensions
        if (token.getText().equals("(")) {

            boolean foundComma = false;
            while (token.getText().equals("(") || foundComma) {

                // Room for one more dimension?
                if (type.get().arrayLevel >= TomVM.ARRAY_MAX_DIMENSIONS) {
                    setError("Arrays cannot have more than "
                            + String.valueOf(TomVM.ARRAY_MAX_DIMENSIONS)
                            + " dimensions.");
                    return false;
                }
                if (!getToken()) // Skip "("
                {
                    return false;
                }

                // Validate dimensions.
                // Firstly, pointers don't have dimensions declared with them.
                if (type.get().pointerLevel > 0) {
                    if (!token.getText().equals(")")) {
                        setError("Use '()' to declare a pointer to an array");
                        return false;
                    }
                    type.get().arrayLevel++;
                }
                // Structure field types must have constant array size that we
                // can
                // evaluate at compile time (i.e right now).
                else if (forStruc) {

                    // Evaluate constant expression
                    Integer expressionType = BasicValType.VTP_INT;
                    Value value = new Value();
                    String stringValue = "";

                    Mutable<Integer> expressionTypeRef = new Mutable<>(expressionType);
                    Mutable<Value> valueRef = new Mutable<>(value);
                    Mutable<String> stringValueRef = new Mutable<>(stringValue);

                    if (!evaluateConstantExpression(expressionTypeRef, valueRef, stringValueRef)) {
                        return false;
                    }

                    // Update local values from references
                    expressionType = expressionTypeRef.get();
                    value = valueRef.get();
                    stringValue = stringValueRef.get();

                    // Store array dimension
                    type.get().addDimension(value.getIntVal() + 1);
                } else if (forFuncParam) {
                    // Array sizes for function parameters aren't declared.
                    // (Syntax is "dim myArray()")
                    type.get().arrayLevel++;
                }
                // Regular DIMmed array dimensions are sized at run time.
                // Here we generate code to calculate the dimension size and
                // push it to
                // stack.
                else {
                    if (!(compileExpression() && compileConvert(BasicValType.VTP_INT) && compilePush())) {
                        return false;
                    }
                    type.get().arrayLevel++;
                }

                // Expect closing ')', or a separating comma
                foundComma = false;
                if (token.getText().equals(")")) {
                    if (!getToken()) {
                        return false;
                    }
                } else if (token.getText().equals(",")) {
                    foundComma = true;
                } else {
                    setError("Expected ')' or ','");
                    return false;
                }
            }
        }

        // "as" keyword (QBasic/FreeBasic compatibility)
        if (token.getText().equals("as")) {
            if (!compileAs(name, type)) {
                return false;
            }
        }

        // If data type still not specified, default to integer
        if (type.get().basicType == BasicValType.VTP_UNDEFINED) {
            type.get().basicType = getDefaultDataBasicValType();
        }

        return true;
    }

    private boolean compileLoadVar() {

        // Look for "take address"
        boolean takeAddress = false;
        if (token.getText().equals("&")) {
            if (!getToken()) {
                return false;
            }

            // Special case, take address of function
            if (token.getTokenType() == TokenType.CTT_USER_FUNCTION) {
                if (!compileUserFunctionAddress()) {
                    return false;
                }

                // There's not much point doing data lookups on a function pointer.
                // The only possible "lookup" is to call the pointer, which begets the
                // question: why take the address in the first place.
                // But it's syntactically consistent and there's no reason to disallow it.
                return compileDataLookup(false);
            }

            takeAddress = true;
        }

        // Look for variable name
        if (token.getTokenType() != TokenType.CTT_TEXT) {
            setError("Expected variable name or user function name");
            return false;
        }

        // Prefix variable names
        String varName = symbolPrefix + token.getText();

        // Find variable
        boolean found = false;

        // Check local variable first
        if (inFunction) {

            // Look for variable
            int varIndex = getCurrentUserFunctionPrototype().getLocalVar(varName);

            // Set register type
            if (varIndex >= 0) {

                // Generate code to load variable
                addInstruction(OpCode.OP_LOAD_LOCAL_VAR, BasicValType.VTP_INT, new Value(varIndex));

                // Set register type
                regType.setType(getCurrentUserFunctionPrototype().localVarTypes.get(varIndex));
                regType.pointerLevel++;

                found = true;
            }
        }

        // Then try global
        if (!found) {

            // Look for variable
            int varIndex = vm.getVariables().getVariableIndex(varName);

            if (varIndex >= 0) {

                // Generate code to load variable
                addInstruction(OpCode.OP_LOAD_VAR, BasicValType.VTP_INT, new Value(varIndex));

                // Set register type
                regType.setType(vm.getVariables().getVariables().get(varIndex).type);
                regType.pointerLevel++;

                found = true;
            }
        }

        if (!found) {
            setError((String) "Unknown variable: " + token.getText() + ". Variables must be declared first with DIM");
            return false;
        }

        // Skip past variable name
        if (!getToken()) {
            return false;
        }

        // Dereference to reach data
        if (!compileDerefs()) {
            return false;
        }

        // Compile data lookups (e.g. ".fieldname", array indices, take address
        // e.t.c)
        return compileDataLookup(takeAddress);
    }

    private boolean compileDerefs() {

        // Generate code to dereference pointer
        if (!compileDeref()) {
            return false;
        }

        // In Basic4GL syntax, pointers are implicitly dereferenced (similar to
        // C++'s
        // "reference" type.)
        if (regType.getVirtualPointerLevel() > 0) {
            if (!compileDeref()) {
                return false;
            }
        }

        return true;
    }

    private boolean compileDeref() {

        // Generate code to dereference pointer in reg. (i.e reg = [reg]).
        assertTrue(vm.getDataTypes().isTypeValid(regType));

        // Not a pointer?
        if (regType.getVirtualPointerLevel() <= 0) {
            setError("INTERNAL COMPILER ERROR: Attempted to dereference a non-pointer");
            assertTrue(false); // This should never happen
            return false;
        }

        // If reg is pointing to a structure or an array, we don't dereference
        // (as we can't fit an array or structure into a 4 byte register!).
        // Instead we leave it as a pointer, but update the type to indicate
        // to the compiler that we are using a pointer internally to represent
        // a variable.
        assertTrue(!regType.isByRef);
        if (regType.getPhysicalPointerLevel() == 1 // Must be pointer to actual
                // data (not pointer to
                // pointer e.t.c)
                && (regType.arrayLevel > 0 // Array
                        || regType.basicType >= 0)) { // or structure
            regType.isByRef = true;
            return true;
        }

        // Generate dereference instruction
        regType.pointerLevel--;
        addInstruction(OpCode.OP_DEREF, regType.getStoredType(), new Value()); // Load
        // variable

        return true;
    }

    private boolean compileDataLookup(boolean takeAddress) {

        // Compile various data operations that can be performed on data object.
        // These operations include:
        // * Array indexing: data (index)
        // * Field lookup: data.field
        // * Taking address: &data
        // Or any combination of the above.

        boolean done = false;
        while (!done) {
            if (token.getText().equals(".")) {

                // Lookup subfield
                // Register must contain a structure type
                if (regType.getVirtualPointerLevel() != 0 || regType.arrayLevel != 0 || regType.basicType < 0) {
                    setError("Unexpected '.'");
                    return false;
                }
                assertTrue(vm.getDataTypes().isTypeValid(regType));

                // Skip "."
                if (!getToken()) {
                    return false;
                }

                // Read field name
                if (token.getTokenType() != TokenType.CTT_TEXT) {
                    setError("Expected field name");
                    return false;
                }
                String fieldName = token.getText();
                if (!getToken()) {
                    return false;
                }

                // Validate field
                Structure s = vm.getDataTypes().getStructures().get(regType.basicType);
                int fieldIndex = vm.getDataTypes().getFieldIndex(s, fieldName);
                if (fieldIndex < 0) {
                    setError((String) "'" + fieldName + "' is not a field of structure '" + s.name + "'");
                    return false;
                }

                StructureField field = vm.getDataTypes().getFields().get(fieldIndex);

                // Check field type has been defined
                if (!checkTypeIsDefined(field.type)) {
                    return false;
                }

                // Generate code to calculate address of field
                // Reg is initially pointing to address of structure.
                addInstruction(OpCode.OP_ADD_CONST, BasicValType.VTP_INT, new Value(field.dataOffset));

                // Reg now contains pointer to field
                regType.setType(field.type);
                regType.pointerLevel++;

                // Dereference to reach data
                if (!compileDerefs()) {
                    return false;
                }
            } else if (token.getText().equals("(") &&
                    regType.basicType == BasicValType.VTP_FUNC_PTR &&
                    regType.pointerLevel == 0 &&
                    regType.arrayLevel == 0) {

                // Function pointer variable call
                if (!compileUserFunctionCall(false, false, true)) {
                    return false;
                }
            } else if (token.getText().equals("(")) {

                // Register must contain an array
                if (regType.getVirtualPointerLevel() != 0 || regType.arrayLevel == 0) {
                    setError("Unexpected '('");
                    return false;
                }

                do {
                    if (regType.arrayLevel == 0) {
                        setError("Unexpected ','");
                        return false;
                    }

                    // Index into array
                    if (!getToken()) // Skip "(" or ","
                    {
                        return false;
                    }

                    // Generate code to push array address
                    if (!compilePush()) {
                        return false;
                    }

                    // Evaluate array index, and convert to an integer.
                    if (!compileExpression()) {
                        return false;
                    }
                    if (!compileConvert(BasicValType.VTP_INT)) {
                        setError("Array index must be a number. "
                                + vm.getDataTypes().describeVariable("", regType)
                                + " is not a number");
                        return false;
                    }

                    // Generate code to pop array address into reg2
                    if (!compilePop()) {
                        return false;
                    }

                    // Generate code to index into array.
                    // Input: reg = Array index
                    // reg2 = Array address
                    // Output: reg = Pointer to array element
                    addInstruction(OpCode.OP_ARRAY_INDEX, BasicValType.VTP_INT, new Value());

                    // reg now points to an element
                    regType.setType(reg2Type);
                    regType.isByRef = false;
                    regType.pointerLevel = 1;
                    regType.arrayLevel--;

                    // Dereference to get to element
                    if (!compileDerefs()) {
                        return false;
                    }

                } while (token.getText().equals(","));

                // Expect closing bracket
                if (!token.getText().equals(")")) {
                    setError("Expected ')'");
                    return false;
                }
                if (!getToken()) {
                    return false;
                }
            } else {
                done = true;
            }
        }

        // Compile take address (if necessary)
        if (takeAddress) {
            return compileTakeAddress();
        }

        return true;
    }

    private boolean compileExpression() {
        return compileExpression(false);
    }

    private boolean compileExpression(boolean mustBeConstant) {

        // Compile expression.
        // Generates code that once executed will leave the result of the
        // expression
        // in Reg.

        // Must start with either:
        // A constant (numeric or string)
        // A variable reference

        // Push "stop evaluation" operand to stack. (To protect any existing
        // operators
        // on the stack.

        // Stop evaluation operator
        operatorStack.add(new StackedOperator(new Operator(OperType.OT_STOP, OpCode.OP_NOP, 0, -200000)));

        if (!compileExpressionLoad(mustBeConstant)) {
            return false;
        }

        Operator o = null;
        while ((token.getText().equals(")") && getOperatorTOS().operator.type != OperType.OT_STOP)
                || ((o = binaryOperators.get(token.getText())) != null)) {

            // Special case, right bracket
            if (token.getText().equals(")")) {

                // Evaluate all operators down to left bracket
                while (getOperatorTOS().operator.type != OperType.OT_STOP
                        && getOperatorTOS().operator.type != OperType.OT_LBRACKET) {
                    if (!compileOperation()) {
                        return false;
                    }
                }

                // If operator stack is empty, then the expression terminates before
                // the closing bracket
                if (getOperatorTOS().operator.type == OperType.OT_STOP) {
                    operatorStack.remove(operatorStack.size() - 1); // Remove
                    // stopper
                    return true;
                }

                // Remove left bracket
                operatorStack.remove(operatorStack.size() - 1);

                // Move on
                if (!getToken()) {
                    return false;
                }

                // Result may be an array or a structure to which a data lookup
                // can
                // be applied.
                if (!compileDataLookup(false)) {
                    return false;
                }
            }

            // Otherwise must be regular binary operator
            else {

                // Compare current operator with top of stack operator
                while (getOperatorTOS().operator.type != OperType.OT_STOP
                        && getOperatorTOS().operator.binding >= o.binding) {
                    if (!compileOperation()) {
                        return false;
                    }
                }

                // 14-Apr-06: Lazy evaluation.
                // Add jumps around the second part of AND or OR operations
                int lazyJumpAddr = -1;
                if (o.type == OperType.OT_LAZYBOOLOPERATOR) {
                    if (o.opCode == OpCode.OP_OP_AND) {
                        lazyJumpAddr = vm.getInstructionCount();
                        addInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT, new Value(0));
                    } else if (o.opCode == OpCode.OP_OP_OR) {
                        lazyJumpAddr = vm.getInstructionCount();
                        addInstruction(OpCode.OP_JUMP_TRUE, BasicValType.VTP_INT, new Value(0));
                    }
                }

                // Save operator to stack
                operatorStack.add(new StackedOperator(o, lazyJumpAddr));

                // Push first operand
                if (!compilePush()) {
                    return false;
                }

                // Load second operand
                if (!getToken()) {
                    return false;
                }
                if (!compileExpressionLoad(mustBeConstant)) {
                    return false;
                }
            }
        }

        // Perform remaining operations
        while (getOperatorTOS().operator.type != OperType.OT_STOP) {
            if (!compileOperation()) {
                return false;
            }
        }

        // Remove stopper
        operatorStack.remove(operatorStack.size() - 1);

        return true;
    }

    private boolean compileOperation() {

        // Compile topmost operation on operator stack
        assertTrue(!operatorStack.isEmpty());

        // Remove operator from stack
        StackedOperator o = getOperatorTOS();
        operatorStack.remove(operatorStack.size() - 1);

        // Must not be a left bracket
        if (o.operator.type == OperType.OT_LBRACKET) {
            setError("Expected ')'");
            return false;
        }

        // Binary or unary operation?
        if (o.operator.params == 1) {

            // Try overloaded operator functions first
            if (o.operator.type == OperType.OT_OPERATOR && compileOverloadedOperator(o.operator.getOpCode(), FunctionType.FT_OPERATOR_UNARY)) {
                return true;
            }

            // NOTE: commented out in newer versions of the original compiler code
            // Try plug in language extension first
            //if (compileExtendedUnOperation(o.operator.opCode)) {
            //    return true;
            //}

            // Can only operate on basic types.
            // (This will change once vector and matrix routines have been
            // implemented).
            if (!regType.isBasicType()) {
                setError("Operator cannot be applied to this data type");
                return false;
            }

            // Special case, boolean operator.
            // Must convert to boolean first
            if (o.operator.type == OperType.OT_BOOLOPERATOR || o.operator.type == OperType.OT_LAZYBOOLOPERATOR) {
                compileConvert(BasicValType.VTP_INT);
            }

            // Perform unary operation
            addInstruction(o.operator.opCode, regType.basicType, new Value());

            // Special case, boolean operator
            // Result will be an integer
            if (o.operator.type == OperType.OT_RETURNBOOLOPERATOR) {
                regType.setType(BasicValType.VTP_INT);
            }
        } else if (o.operator.params == 2) {

            // Generate code to pop first operand from stack into Reg2
            if (!compilePop()) {
                return false;
            }

            // Try overloaded operator functions first
            if (o.operator.type == OperType.OT_OPERATOR && compileOverloadedOperator(o.operator.getOpCode(), FunctionType.FT_OPERATOR_BINARY)) {
                return true;
            }
            // NOTE: commented out in newer versions of the original compiler code
            // Try plug in language extension first
            //if (compileExtendedBinOperation(o.operator.opCode)) {
            //    return true;
            //}

            // Ensure operands are equal type. Generate code to convert one if
            // necessary.

            int opCodeType; // Data type to store in OP_CODE
            if (regType.isNull() || reg2Type.isNull()) {

                // Can compare null to any pointer type. However, operator must
                // be '=' or '<>'
                if (o.operator.opCode != OpCode.OP_OP_EQUAL && o.operator.opCode != OpCode.OP_OP_NOT_EQUAL) {
                    setError("Operator cannot be applied to this data type");
                    return false;
                }

                // Convert null pointer type to non null pointer type
                // Note: If both pointers a null, CompileConvert will simply do
                // nothing
                if (regType.isNull()) {
                    if (!compileConvert(reg2Type)) {
                        return false;
                    }
                }

                if (reg2Type.isNull()) {
                    if (!compileConvert2(regType)) {
                        return false;
                    }
                }

                // Integer comparison is used internally
                opCodeType = BasicValType.VTP_INT;
            } else if (regType.isFuncPtr() && reg2Type.isFuncPtr()) {
                // Can compare function pointers to other function pointers with compatible prototypes.
                // Operator must be '=' or '<>'.
                if (o.operator.opCode != OpCode.OP_OP_EQUAL && o.operator.opCode != OpCode.OP_OP_NOT_EQUAL) {
                    setError("Operator cannot be applied to this data type");
                    return false;
                }

                // Make sure prototypes are compatible
			    UserFuncPrototype p1 = vm.getUserFunctionPrototypes().get(regType.prototypeIndex);
                UserFuncPrototype p2 = vm.getUserFunctionPrototypes().get(reg2Type.prototypeIndex);
                if (!p1.isCompatibleWith(p2))
                {
                    setError("Function pointer types are not compatible");
                    return false;
                }

                // Integer comparison used internally
                opCodeType = BasicValType.VTP_INT;
            } else if (regType.getVirtualPointerLevel() > 0 || reg2Type.getVirtualPointerLevel() > 0) {

                // Can compare 2 pointers. However operator must be '=' or '<>'
                // and
                // pointer types must be exactly the same
                if (o.operator.opCode != OpCode.OP_OP_EQUAL && o.operator.opCode != OpCode.OP_OP_NOT_EQUAL) {
                    setError("Operator cannot be applied to this data type");
                    return false;
                }
                if (!regType.exactEquals(reg2Type)) {
                    setError("Cannot compare pointers to different types");
                    return false;
                }

                // Integer comparison is used internally
                opCodeType = BasicValType.VTP_INT;
            } else {
                // Otherwise all operators can be applied to basic data types
                if (!regType.isBasicType() || !reg2Type.isBasicType()) {
                    setError("Operator cannot be applied to this data type");
                    return false;
                }

                // Convert operands to highest type
                int highest = regType.basicType;
                if (reg2Type.basicType > highest) {
                    highest = reg2Type.basicType;
                }
                if (o.operator.type == OperType.OT_BOOLOPERATOR || o.operator.type == OperType.OT_LAZYBOOLOPERATOR) {
                    highest = BasicValType.VTP_INT;
                }
                if ((syntax == LS_TRADITIONAL || syntax == LS_TRADITIONAL_SUFFIX) && o.operator.opCode == OpCode.OP_OP_DIV)
                {
                    // 14-Aug-05 Tom: In traditional mode, division is always
                    // between floating pt numbers
                    highest = BasicValType.VTP_REAL;
                }

                if (!compileConvert(highest)) {
                    return false;
                }
                if (!compileConvert2(highest)) {
                    return false;
                }

                opCodeType = highest;
            }

            // Generate operation code
            addInstruction(o.operator.opCode, opCodeType, new Value());

            // Special case, boolean operator
            // Result will be an integer
            if (o.operator.type == OperType.OT_RETURNBOOLOPERATOR) {
                regType.setType(BasicValType.VTP_INT);
            }
        } else {
            assertTrue(false);
        }

        // Fix up lazy jumps
        if (o.lazyJumpAddress >= 0) {
            vm.getInstruction(o.lazyJumpAddress).value.setVal((int) vm.getInstructionCount());
        }

        return true;
    }

    private boolean compileOverloadedOperator(int opCode, FunctionType type)
    {
        // Look for operator overload
        String overloadFn = "";
        if (operatorOverloads.containsKey(opCode)) {
            overloadFn = operatorOverloads.get(opCode);
        }
        if ("".equals(overloadFn)) {
            return false;
        }

        // Save state before calling compileFunctionCall (in case call fails)
        int count = vm.getInstructionCount();
        ValType saveReg1Type = new ValType(regType);
        ValType saveReg2Type = new ValType(reg2Type);
        int numOperators = operatorStack.size();
        int numOperands = operandStack.size();

        // NOTE: added verbose = false parameter to compileFunctionCall to prevent noisy error messages being logged
        // if overload function is not found since this is a normal situation (i.e. most operators won't be overloaded).
        if (compileFunctionCall(overloadFn, type, true, false)) {
            return true;
        }

        // Restore state
        clearError();
        vm.rollbackProgram(count);
        regType = saveReg1Type;
        reg2Type = saveReg2Type;
        while (operatorStack.size() > numOperators) {
            operatorStack.remove(operatorStack.size() - 1);
        }
        while (operandStack.size() > numOperands) {
            operandStack.remove(operandStack.size() - 1);
        }
        return false;
    }

    private boolean compileExpressionLoad() {
        return compileExpressionLoad(false);
    }

    private boolean compileExpressionLoad(boolean mustBeConstant) {

        // Like CompileLoad, but will also accept and stack preceeding unary operators

        // Push any unary operators found
        while (true) {

            // Special case, left bracket
            if (token.getText().equals("(")) {
                // Brackets bind looser than anything else
                operatorStack.add(
                        new StackedOperator(new Operator(OperType.OT_LBRACKET, OpCode.OP_NOP, 0, -10000)));
            }

            // Otherwise look for recognised unary operator
            else {
                Operator o = unaryOperators.get(token.getText());
                if (o != null) // Operator found
                {
                    // => Stack it
                    operatorStack.add(new StackedOperator(o));
                } else {
                    // Not an operator
                    if (mustBeConstant) {
                        return compileLoadConst();
                    } else {
                        // => Proceed on to load variable/constant
                        return compileLoad();
                    }
                }
            }

            if (!getToken()) {
                return false;
            }
        }
    }

    private boolean compileLoad() {

        // Compile load var or constant, or function result
        if (token.getTokenType() == TokenType.CTT_CONSTANT || token.getText().equals("null")) {
            return compileLoadConst();
        } else if (token.getTokenType() == TokenType.CTT_TEXT || token.getText().equals("&")) {
            return compileLoadVar();
        } else if (token.getTokenType() == TokenType.CTT_FUNCTION) {
            return compileFunction();
        } else if (token.getTokenType() == TokenType.CTT_USER_FUNCTION) {
            return compileUserFunctionCall(true, false, false);
        } else if (token.getTokenType() == TokenType.CTT_RUNTIME_FUNCTION) {
            return compileUserFunctionCall(true, true, false);
        } else if (token.getTokenType() == TokenType.CTT_KEYWORD && token.getText().equals("arraymax")) {
            return compileArrayMax();
        }

        setError("Expected constant, variable or function");
        return false;
    }

    private boolean compileNull() {
        // Load 0 into register
        addInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_INT, new Value(0));
        // Type is pointer to VTP_NULL
        regType.setType(new ValType(BasicValType.VTP_NULL, (byte) 0, (byte) 1, false));
        return getToken();
    }

    private boolean compileLoadConst() {

        // Special case, "null" reserved word
        if (token.getText().equals("null")) {
            return compileNull();
        }

        // Compile load constant
        if (token.getTokenType() == TokenType.CTT_CONSTANT) {

            // Special case, string constants
            if (token.getValType() == BasicValType.VTP_STRING) {

                // Allocate new string constant
                String text;
                // TODO - 2026-04-23 Need to review this code. original source used `m_text.substr (1, m_token.m_text.length () - 1)`
                text = token.getText().substring(1, token.getText().length()); // Remove S prefix
                int index = vm.storeStringConstant(text);

                // store load instruction
                addInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING, new Value(index));
                regType.setType(BasicValType.VTP_STRING);
            } else if (token.getValType() == BasicValType.VTP_REAL) {
                addInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_REAL, new Value(Float.valueOf(token.getText())));
                regType.setType(BasicValType.VTP_REAL);
            } else if (token.getValType() == BasicValType.VTP_INT) {
                addInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_INT, new Value(Cast.toInt(token.getText())));
                regType.setType(BasicValType.VTP_INT);
            } else {
                setError("Unknown data type");
                return false;
            }

            return getToken();
        }

        setError("Expected constant");
        return false;
    }

    private boolean compilePush() {

        // Store pushed value type
        operandStack.add(new ValType(regType));

        // Generate push code
        addInstruction(OpCode.OP_PUSH, regType.getStoredType(), new Value());

        return true;
    }

    private boolean compilePop() {

        if (operandStack.isEmpty()) {
            setError("Expression error");
            return false;
        }

        // Retrieve pushed value type
        reg2Type.setType(operandStack.lastElement());
        operandStack.remove(operandStack.size() - 1);

        // Generate pop code
        addInstruction(OpCode.OP_POP, reg2Type.getStoredType(), new Value());

        return true;
    }

    private boolean compileConvert(int basictype) {

        // Convert reg to given type
        if (regType.matchesType(basictype))
        {
            // Already same type
            return true;
        }


        // Can convert function pointer, untyped function pointer or null to a function pointer.
        // Note: Calling code is responsible for ensuring functions have compatible prototypes
        // (and emitting the appropriate runtime check if necessary)
        if (basictype == BasicValType.VTP_FUNC_PTR &&
            (regType.getPhysicalPointerLevel() == 0 && regType.basicType == BasicValType.VTP_FUNC_PTR
                ||  regType.getPhysicalPointerLevel() == 0 && regType.basicType == BasicValType.VTP_UNTYPED_FUNC_PTR
                ||	regType.getPhysicalPointerLevel() == 1 && regType.isNull()))
        {
            return true;
        }

        // Determine opcode
        short code = OpCode.OP_NOP;
        if (regType.matchesType(BasicValType.VTP_INT)) {
            if (basictype == BasicValType.VTP_REAL) {
                code = OpCode.OP_CONV_INT_REAL;
            } else if (basictype == BasicValType.VTP_STRING) {
                code = OpCode.OP_CONV_INT_STRING;
            }
        } else if (regType.matchesType(BasicValType.VTP_REAL)) {
            if (basictype == BasicValType.VTP_INT) {
                code = OpCode.OP_CONV_REAL_INT;
            } else if (basictype == BasicValType.VTP_STRING) {
                code = OpCode.OP_CONV_REAL_STRING;
            }
        }

        // Store instruction
        if (code != OpCode.OP_NOP) {
            addInstruction(code, BasicValType.VTP_INT, new Value());
            regType.setType(basictype);
            return true;
        }

        setError("Incorrect data type");
        return false;
    }

    private boolean compileConvert2(int type) {

        // Convert reg2 to given type
        if (reg2Type.matchesType(type)) // Already same type
        {
            return true;
        }

        // Determine opcode
        short code = OpCode.OP_NOP;
        if (reg2Type.matchesType(BasicValType.VTP_INT)) {
            if (type == BasicValType.VTP_REAL) {
                code = OpCode.OP_CONV_INT_REAL2;
            } else if (type == BasicValType.VTP_STRING) {
                code = OpCode.OP_CONV_INT_STRING2;
            }
        } else if (reg2Type.matchesType(BasicValType.VTP_REAL)) {
            if (type == BasicValType.VTP_INT) {
                code = OpCode.OP_CONV_REAL_INT2;
            } else if (type == BasicValType.VTP_STRING) {
                code = OpCode.OP_CONV_REAL_STRING2;
            }
        }

        // Store instruction
        if (code != OpCode.OP_NOP) {
            addInstruction(code, BasicValType.VTP_INT, new Value());
            reg2Type.setType(type);
            return true;
        }

        setError("Incorrect data type");
        return false;
    }

    private boolean compileSwap() {
        // Swap register values at runtime and mirror the type swap at compile time.
        addInstruction(OpCode.OP_SWAP, BasicValType.VTP_UNDEFINED, new Value());

        ValType temp = new ValType(regType);
        regType.setType(reg2Type);
        reg2Type.setType(temp);

        return true;
    }

    private boolean compileConvert(ValType type) {

        // Can convert null to a different pointer type, or function pointer
        if (regType.isNull()) {
            if (type.getVirtualPointerLevel() <= 0 && type.basicType != BasicValType.VTP_FUNC_PTR) {
                setError("Cannot convert null to " + vm.getDataTypes().describeVariable("", type));
                return false;
            }

            // No generated code necessary, just substitute in type
            regType.setType(type);
            return true;
        }

        // Can convert values to references. (This is used when evaluating
        // function
        // parameters.)
        if (type.pointerLevel == 1
                && type.isByRef // type is a reference
                && regType.pointerLevel == 0 // regType is a value
                && regType.basicType == type.basicType // Same type of data
                && regType.arrayLevel == type.arrayLevel) {

            // Convert register to pointer
            if (compileTakeAddress()) {

                // Convert pointer to reference
                regType.isByRef = true;
                return true;
            } else {
                return false;
            }
        }

        // Can convert to basic types.
        // For non basic types, all we can do is verify that the register contains
        // the type that we expect, and raise a compiler error otherwise.
        if (type.isBasicType()) {
            ValType prevRegType = new ValType(regType);
            if (!compileConvert(type.basicType)) {
                return false;
            }
            // For function pointers, also need to verify that the prototypes are compatible
            if (type.basicType == BasicValType.VTP_FUNC_PTR && prevRegType.basicType == BasicValType.VTP_FUNC_PTR) {
    			UserFuncPrototype p1 = vm.getUserFunctionPrototypes().get(type.prototypeIndex);
                UserFuncPrototype p2 = vm.getUserFunctionPrototypes().get(prevRegType.prototypeIndex);
                if (p1.isCompatibleWith(p2)) {
                    return true;
                }
            } else {
                return true;
            }
        } else if (regType.exactEquals(type)) {
            // Note: Exact equals is required as == will say that pointers are equal to references.
            // (Internally this is true, but we want to enforce that programs use the correct type.)
            return true;
        }

        setError("Cannot convert to " + vm.getDataTypes().describeVariable("", type));
        return false;
    }

    private boolean compileConvert2(ValType type) {

        // Can convert null to a different pointer type, or function pointer
        if (reg2Type.isNull()) {
            if (type.getVirtualPointerLevel() <= 0 && type.basicType != BasicValType.VTP_FUNC_PTR) {
                setError("Cannot convert null to " + vm.getDataTypes().describeVariable("", type));
                return false;
            }

            // No generated code necessary, just substitute in type
            reg2Type.setType(type);
            return true;
        }

        // Can convert to basic types.
        // For non basic types, all we can do is verify that the register contains
        // the type that we expect, and raise a compiler error otherwise.
        if (type.isBasicType()) {
            ValType prevRegType = new ValType(reg2Type);
            if (!compileConvert2(type.basicType)) {
                return false;
            }
            // For function pointers, also need to verify that the prototypes are compatible
            if (type.basicType == BasicValType.VTP_FUNC_PTR && prevRegType.basicType == BasicValType.VTP_FUNC_PTR) {
			    UserFuncPrototype p1 = vm.getUserFunctionPrototypes().get(type.prototypeIndex);
                UserFuncPrototype p2 = vm.getUserFunctionPrototypes().get(prevRegType.prototypeIndex);
                if (p1.isCompatibleWith(p2)) {
                    return true;
                }
            } else {
                return true;
            }
        } else if (reg2Type.exactEquals(type)) {
            // Note: Exact equals is required as == will say that pointers are equal to references.
            // (Internally this is true, but we want to enforce that programs use the correct type.)
            return true;
        }

        setError("Cannot convert to " + vm.getDataTypes().describeVariable("", type));
        return false;
    }

    private boolean compileTakeAddress() {

        // Take address of data in reg.
        // We do this my moving the previously generate deref from the end of
        // the program.
        // (If the last instruction is not a deref, then there is a problem.)

        // Special case: Implicit pointer
        if (regType.isByRef) {
            regType.isByRef = false; // Convert to explicit pointer
            return true;
        }

        // Check last instruction was a deref
        if (vm.getInstructionCount() <= 0
                || vm.getInstruction(vm.getInstructionCount() - 1).opCode != OpCode.OP_DEREF) {
            setError("Cannot take address of this data");
            return false;
        }

        // Remove it
        vm.removeLastInstruction();
        regType.pointerLevel++;

        return true;
    }

    private boolean compileAssignment() {

        // Generate code to load target variable
        if (!compileLoadVar()) {
            return false;
        }

        // Is this an assignment?
        if (token.getText().equals("=")) {
            // Compile code to assign value to variable
            if (!internalCompileAssignment()) {
                return false;
            }
        }

        return true;
    }

    boolean internalCompileAssignment() {

        // Expect =
        if (!token.getText().equals("=")) {
            setError("Expected '='");
            return false;
        }

        // Convert load target variable into take address of target variable
        if (!compileTakeAddress()) {
            setError("Left side cannot be assigned to");
            return false;
        }

        // Skip =
        if (!getToken()) {
            return false;
        }

        // Push target address
        if (!compilePush()) {
            return false;
        }

        // Generate code to evaluate expression
        if (!compileExpression()) {
            return false;
        }

        // Pop target address into reg2
        if (!compilePop()) {
            return false;
        }

        // Simple type case: reg2 points to basic type
        if (reg2Type.pointerLevel == 1 && reg2Type.arrayLevel == 0 && reg2Type.basicType < 0) {

            // Attempt to convert value in reg to same type
            ValType prevRegType = new ValType(regType);
            if (!compileConvert(reg2Type.basicType)) {
                setError("Types do not match");
                return false;
            }

            // Extra checking required for function pointers.
            if (reg2Type.basicType == BasicValType.VTP_FUNC_PTR)
            {
                // Function pointer assignment requires function prototypes be compatible
                if (regType.basicType == BasicValType.VTP_FUNC_PTR)
                {
				    UserFuncPrototype p1 = vm.getUserFunctionPrototypes().get(reg2Type.prototypeIndex);
                    UserFuncPrototype p2 = vm.getUserFunctionPrototypes().get(prevRegType.prototypeIndex);
                    if (!p1.isCompatibleWith(p2))
                    {
                        setError("Types do not match");
                        return false;
                    }
                }

                // Assigning an untyped function pointer requires a run-time type check
                if (regType.basicType == BasicValType.VTP_UNTYPED_FUNC_PTR)
                {
                    addInstruction(OpCode.OP_CHECK_FUNC_PTR, BasicValType.VTP_INT, new Value(reg2Type.prototypeIndex));
                }
            }

            // Save reg into [reg2]
            addInstruction(OpCode.OP_SAVE, reg2Type.basicType, new Value());
        }

        // Pointer case. reg2 must point to a pointer and reg1 point to a
        // value.
        else if (reg2Type.getVirtualPointerLevel() == 2 && regType.getVirtualPointerLevel() == 1) {

            // Must both point to same type, OR reg1 must point to null
            if (regType.isNull()
                    || (regType.arrayLevel == reg2Type.arrayLevel && regType.basicType == reg2Type.basicType)) {

                // Validate pointer scope before saving to variable
                addInstruction(OpCode.OP_CHECK_PTR, BasicValType.VTP_INT, new Value());

                // Save address to pointer
                addInstruction(OpCode.OP_SAVE, BasicValType.VTP_INT, new Value());
            } else {
                setError("Types do not match");
                return false;
            }
        }

        // Copy object case
        else if (reg2Type.getVirtualPointerLevel() == 1
                && regType.getVirtualPointerLevel() == 0
                && regType.getPhysicalPointerLevel() == 1) {

            // Check that both are the same type
            if (regType.arrayLevel == reg2Type.arrayLevel && regType.basicType == reg2Type.basicType) {

                // Add op-code to check pointers if necessary
                ValType dataType = new ValType(regType);
                dataType.pointerLevel--;
                dataType.isByRef = false;
                if (vm.getDataTypes().containsPointer(dataType)) {
                    addInstruction(OpCode.OP_CHECK_PTRS, BasicValType.VTP_INT, new Value((int)
                            vm.getStoreTypeIndex(dataType)));
                }

                addInstruction(OpCode.OP_COPY, BasicValType.VTP_INT, new Value((int) vm.getStoreTypeIndex(regType)));
            } else {
                setError("Types do not match");
                return false;
            }
        } else {
            setError("Types do not match");
            return false;
        }

        return true;
    }

    private boolean compileGoto() {
        return compileGoto(OpCode.OP_JUMP);
    }

    /**
     *
     * @param jumpType Flow control Op code
     * @return
     */
    private boolean compileGoto(short jumpType) {
        assertTrue(jumpType == OpCode.OP_JUMP
                || jumpType == OpCode.OP_JUMP_TRUE
                || jumpType == OpCode.OP_JUMP_FALSE
                || jumpType == OpCode.OP_CALL);

        // Cannot use goto inside a function or sub (can use GOSUB though)
        if (inFunction && jumpType != OpCode.OP_CALL) {
            setError("Cannot use 'goto' inside a function or subroutine");
            return false;
        }

        // Validate label
        if (token.getTokenType() != TokenType.CTT_TEXT) {
            setError("Expected label name");
            return false;
        }

        // Record jump, so that we can fix up the offset in the second compile
        // pass.
        String labelName = symbolPrefix + token.getText();
        jumps.add(new Jump(vm.getInstructionCount(), labelName));

        // Add jump instruction
        addInstruction(jumpType, BasicValType.VTP_INT, new Value(0));

        // Move on
        return getToken();
    }

    private boolean compileIf(boolean elseif) {

        // Skip "if"
        int line = parser.getLine(), col = parser.getColumn();
        if (!getToken()) {
            return false;
        }

        // Generate code to evaluate expression
        if (!compileExpression()) {
            return false;
        }

        // Generate code to convert to integer
        if (!compileConvert(BasicValType.VTP_INT)) {
            return false;
        }

        // Free any temporary data expression may have created
        if (!compileFreeTempData()) {
            return false;
        }

        // Special case!
        // If next instruction is a "goto", then we can ommit the "then"
        if (!token.getText().equals("goto")) {

            // Otherwise expect "then"
            if (!token.getText().equals("then")) {
                setError("Expected 'then'");
                return false;
            }
            if (!getToken()) {
                return false;
            }
        }

        // Determine whether this "if" has an automatic "endif" inserted at the
        // end of the line
        boolean isTraditionalSyntax = (syntax == LS_TRADITIONAL || syntax == LS_TRADITIONAL_SUFFIX);
        // Only applies to traditional syntax
        // "then" must not be the last token on the line
        boolean autoEndif = isTraditionalSyntax
                && !(token.getTokenType() == TokenType.CTT_EOL || token.getTokenType() == TokenType.CTT_EOF);

        // Create flow control structure
        flowControls.add(new FlowControl(
                FlowControlType.FCT_IF, vm.getInstructionCount(), 0, line, col, elseif, "", !autoEndif));

        // Create conditional jump
        addInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT, new Value(0));

        // Don't need colon between this and next instruction
        needColon = false;

        return true;
    }

    private boolean compileElse(boolean elseif) {

        // Find "if" on top of flow control stack
        if (!isFlowControlTopEqual(FlowControlType.FCT_IF)) {
            setError("'else' without 'if'");
            return false;
        }
        FlowControl top = getFlowControlTOS();
        flowControls.remove(flowControls.size() - 1);

        // Skip "else"
        // (But not if it's really an "elseif". CompileIf will skip over it
        // then.)
        int line = parser.getLine(), col = parser.getColumn();
        if (!elseif) {
            if (!getToken()) {
                return false;
            }
        }

        // Push else to flow control stack
        flowControls.add(new FlowControl(
                FlowControlType.FCT_ELSE, vm.getInstructionCount(), 0, line, col, top.impliedEndif, "", top.blockIf));

        // Generate code to jump around else block
        addInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(0));

        // Fixup jump around IF block
        assertTrue(top.jumpOut < vm.getInstructionCount());
        vm.getInstruction(top.jumpOut).value.setIntVal(vm.getInstructionCount());

        // Don't need colon between this and next instruction
        needColon = false;

        return true;
    }

    private boolean compileEndIf(boolean automatic) {

        // Find if or else on top of flow control stack
        if (!(isFlowControlTopEqual(FlowControlType.FCT_IF) || isFlowControlTopEqual(FlowControlType.FCT_ELSE))) {
            setError("'endif' without 'if'");
            return false;
        }
        FlowControl top = getFlowControlTOS();
        flowControls.remove(flowControls.size() - 1);

        // Skip "endif"
        if (!top.impliedEndif && !automatic) {
            if (!getToken()) {
                return false;
            }
        }

        // Fixup jump around IF or ELSE block
        assertTrue(top.jumpOut < vm.getInstructionCount());
        vm.getInstruction(top.jumpOut).value.setIntVal(vm.getInstructionCount());

        // If there's an implied endif then add it
        if (top.impliedEndif) {
            return compileEndIf(automatic);
        } else {
            return true;
        }
    }

    private boolean compileFor() {

        // Skip "for"
        int line = parser.getLine(), col = parser.getColumn();
        if (!getToken()) {
            return false;
        }

        // Extract loop variable name
        Token nextToken = parser.peekToken(false, false);
        if (!checkParser()) {
            return false;
        }
        if (nextToken.getText().equals("(")) {
            setError("Cannot use array variable in 'for' - 'next' structure");
            return false;
        }
        String loopVarUnprefixed = token.getText();
        String loopVar = symbolPrefix + loopVarUnprefixed;

        // Verify variable is numeric
        boolean found = false;
        Integer loopVarType = BasicValType.VTP_INT;

        // Check local variable first
        if (inFunction) {

            // Look for variable
            int varIndex = getCurrentUserFunctionPrototype().getLocalVar(loopVar);

            // Set register type
            if (varIndex >= 0) {
                found = true;

                // Check type is INT or REAL
                ValType type = new ValType(
                        getCurrentUserFunctionPrototype().localVarTypes.get(varIndex));
                if (!(type.matchesType(BasicValType.VTP_INT) || type.matchesType(BasicValType.VTP_REAL))) {
                    setError("Loop variable must be an Integer or Real");
                    return false;
                }
                loopVarType = type.basicType;
            }
        }

        // Check global variable
        if (!found) {
            int varIndex = vm.getVariables().getVariableIndex(loopVar);
            if (varIndex >= 0) {
                found = true;

                // Check type is INT or REAL
                ValType type = new ValType(vm.getVariables().getVariables().get(varIndex).type);
                if (!(type.matchesType(BasicValType.VTP_INT) || type.matchesType(BasicValType.VTP_REAL))) {
                    setError("Loop variable must be an Integer or Real");
                    return false;
                }
                loopVarType = type.basicType;
            }
        }
        if (!found) {
            setError("Unknown variable: " + token.getText() + ". Must be declared with DIM");
            return false;
        }

        // Compile assignment
        int varLine = parser.getLine(), varCol = parser.getColumn();
        Token varToken = token;
        if (!compileAssignment()) {
            return false;
        }

        // Save loop back position
        int loopPos = vm.getInstructionCount();

        // Expect "to"
        if (!token.getText().equals("to")) {
            setError("Expected 'to'");
            return false;
        }
        if (!getToken()) {
            return false;
        }

        // Compile load variable and push
        ParserPos savedPos = savePosition(); // Save parser position
        parser.setPos(varLine, varCol); // Point to variable name
        token = varToken;

        if (!compileLoadVar()) // Load variable
        {
            return false;
        }
        if (!compilePush()) // And push
        {
            return false;
        }

        restorePosition(savedPos); // Restore parser position

        // Compile "to" expression
        if (!compileExpression()) {
            return false;
        }
        if (!compileConvert(loopVarType)) {
            return false;
        }

        // Evaluate step. (Must be a constant expression)
        Integer stepType = loopVarType;
        Value stepValue = new Value();

        if (token.getText().equals("step")) {

            // Skip step instruction
            if (!getToken()) {
                return false;
            }

            // Compile step constant (expression)
            String stringValue = "";
            Mutable<Integer> stepTypeRef = new Mutable<>(stepType);
            Mutable<Value> stepValueRef = new Mutable<>(stepValue);
            Mutable<String> stringValueRef = new Mutable<>(stringValue);
            if (!evaluateConstantExpression(stepTypeRef, stepValueRef, stringValueRef)) {
                return false;
            }

            // Update local values from references
            stepType = stepTypeRef.get();
            stepValue = stepValueRef.get();
            stringValue = stringValueRef.get();

        } else {

            // No explicit step.
            // Use 1 as default
            if (stepType == BasicValType.VTP_INT) {
                stepValue = new Value(1);
            } else {
                stepValue = new Value(1.0f);
            }
        }

        // Choose comparison operator (based on direction of step)
        Operator comparison;
        if (stepType == BasicValType.VTP_INT) {
            if (stepValue.getIntVal() > 0) {
                comparison = binaryOperators.get("<=");
            } else if (stepValue.getIntVal() < 0) {
                comparison = binaryOperators.get(">=");
            } else {
                comparison = binaryOperators.get("<>");
            }
        } else {
            assertTrue(stepType == BasicValType.VTP_REAL);
            if (stepValue.getRealVal() > 0) {
                comparison = binaryOperators.get("<=");
            } else if (stepValue.getRealVal() < 0) {
                comparison = binaryOperators.get(">=");
            } else {
                comparison = binaryOperators.get("<>");
            }
        }

        // Compile comparison expression
        operatorStack.add(new StackedOperator(comparison));
        if (!compileOperation()) {
            return false;
        }

        // Generate step expression
        String step = loopVarUnprefixed
                + " = "
                + loopVarUnprefixed
                + " + "
                + (stepType == BasicValType.VTP_INT
                        ? String.valueOf(stepValue.getIntVal())
                        : String.valueOf(stepValue.getRealVal()));

        // Create flow control structure
        flowControls.add(new FlowControl(
                FlowControlType.FCT_FOR, vm.getInstructionCount(), loopPos, line, col, false, step, false));

        // Create conditional jump
        addInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT, new Value(0));

        return true;
    }

    private boolean compileNext() {

        // Find for on top of flow control stack
        if (!isFlowControlTopEqual(FlowControlType.FCT_FOR)) {
            setError("'next' without 'for'");
            return false;
        }
        FlowControl top = getFlowControlTOS();
        flowControls.remove(flowControls.size() - 1);

        // Skip "next"
        int nextLine = token.getLine(), nextCol = token.getCol();
        if (!getToken()) {
            return false;
        }

        // Generate instruction to increment loop variable

        // Special mode. Compile this string instead of source code.
        // We pass in the line and the column of the "next" instruction so that
        // generated code will be associated with this offset in the source code.
        // (This keeps the debugger happy)
        parser.setSpecial(top.data, nextLine, nextCol);

        Token saveToken = token;
        if (!getToken()) {
            return false;
        }
        if (!compileAssignment()) {
            return false;
        }
        parser.setNormal();
        token = saveToken;

        // Generate jump back instruction
        addInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(top.jumpLoop));

        // Fixup jump around FOR block
        assertTrue(top.jumpOut < vm.getInstructionCount());
        vm.getInstruction(top.jumpOut).value.setIntVal(vm.getInstructionCount());
        return true;
    }

    private boolean compileWhile() {

        // Save loop position
        int loopPos = vm.getInstructionCount();

        // Skip "while"
        int line = parser.getLine(), col = parser.getColumn();
        if (!getToken()) {
            return false;
        }

        // Generate code to evaluate expression
        if (!compileExpression()) {
            return false;
        }

        // Generate code to convert to integer
        if (!compileConvert(BasicValType.VTP_INT)) {
            return false;
        }

        // Free any temporary data expression may have created
        if (!compileFreeTempData()) {
            return false;
        }

        // Create flow control structure
        flowControls.add(new FlowControl(FlowControlType.FCT_WHILE, vm.getInstructionCount(), loopPos, line, col));

        // Create conditional jump
        addInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT, new Value(0));
        return true;
    }

    private boolean compileWend() {

        // Find while on top of flow control stack
        if (!isFlowControlTopEqual(FlowControlType.FCT_WHILE)) {
            setError("'wend' without 'while'");
            return false;
        }
        FlowControl top = getFlowControlTOS();
        flowControls.remove(flowControls.size() - 1);

        // Skip "wend"
        if (!getToken()) {
            return false;
        }

        // Generate jump back
        addInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(top.jumpLoop));

        // Fixup jump around WHILE block
        assertTrue(top.jumpOut < vm.getInstructionCount());
        vm.getInstruction(top.jumpOut).value.setIntVal(vm.getInstructionCount());
        return true;
    }

    private boolean compileDo() {

        // Save loop position
        int loopPos = vm.getInstructionCount();

        // Skip "do"
        int line = parser.getLine(), col = parser.getColumn();
        if (!getToken()) {
            return false;
        }

        // Look for "while" or "until"
        if (token.getText().equals("while") || token.getText().equals("until")) {

            // Is this a negative condition?
            boolean negative = token.getText().equals("until");

            // Skip "while" or "until"
            if (!getToken()) {
                return false;
            }

            // Generate code to evaluate expression
            if (!compileExpression()) {
                return false;
            }

            // Generate code to convert to integer
            if (!compileConvert(BasicValType.VTP_INT)) {
                return false;
            }

            // Free any temporary data expression may have created
            if (!compileFreeTempData()) {
                return false;
            }

            // Create flow control structure
            flowControls.add(new FlowControl(FlowControlType.FCT_DO_PRE, vm.getInstructionCount(), loopPos, line, col));

            // Create conditional jump
            addInstruction(negative ? OpCode.OP_JUMP_TRUE : OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT, new Value(0));

            // Done
            return true;
        } else {

            // Post condition DO.
            // Create flow control structure
            flowControls.add(
                    new FlowControl(FlowControlType.FCT_DO_POST, vm.getInstructionCount(), loopPos, line, col));
            return true;
        }
    }

    private boolean compileLoop() {

        if (!(isFlowControlTopEqual(FlowControlType.FCT_DO_PRE)
                || isFlowControlTopEqual(FlowControlType.FCT_DO_POST))) {
            setError("'loop' without 'do'");
            return false;
        }

        // Find DO details
        FlowControl top = getFlowControlTOS();
        flowControls.remove(flowControls.size() - 1);

        // Skip "DO"
        if (!getToken()) {
            return false;
        }

        // Look for "while" or "until"
        if (token.getText().equals("while") || token.getText().equals("until")) {

            // This must be a post condition "do"
            if (top.controlType != FlowControlType.FCT_DO_POST) {
                setError("'until' or 'while' condition has already been specified for this 'do'");
                return false;
            }

            // Is this a negative condition?
            boolean negative = token.getText().equals("until");

            // Skip "while" or "until"
            if (!getToken()) {
                return false;
            }

            // Generate code to evaluate expression
            if (!compileExpression()) {
                return false;
            }

            // Generate code to convert to integer
            if (!compileConvert(BasicValType.VTP_INT)) {
                return false;
            }

            // Free any temporary data expression may have created
            if (!compileFreeTempData()) {
                return false;
            }

            // Create conditional jump back to "do"
            addInstruction(
                    negative ? OpCode.OP_JUMP_FALSE : OpCode.OP_JUMP_TRUE,
                    BasicValType.VTP_INT,
                    new Value(top.jumpLoop));

            // Done
            return true;
        } else {

            // Jump unconditionally back to "do"
            addInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(top.jumpLoop));

            // If this is a precondition "do",
            // fixup the jump around the "do" block
            if (top.controlType == FlowControlType.FCT_DO_PRE) {
                assertTrue(top.jumpOut < vm.getInstructionCount());
                vm.getInstruction(top.jumpOut).value.setIntVal(vm.getInstructionCount());
            }

            // Done
            return true;
        }
    }

    boolean checkName(String name) {

        // Check that name is a suitable variable, structure or structure field
        // name.
        if (constants.containsKey(name) || programConstants.containsKey(name)) {
            setError("'" + name + "' is a constant, and cannot be used here");
            return false;
        }
        if (reservedWords.contains(name)) {
            setError("'" + name + "' is a reserved word, and cannot be used here");
            return false;
        }
        return true;
    }

    public void addConstants(Map<String, Constant> constants) {
        if (constants == null) {
            return;
        }
        // TODO Check if constant already exists before adding
        for (String key : constants.keySet()) {
            this.constants.put(key.toLowerCase(), constants.get(key));
        }
    }

    public void addFunctions(Library library, Map<String, FunctionSpecification[]> specs) {
        int specIndex;
        int vmIndex;
        int i;

        if (library == null || specs == null) {
            return;
        }
        libraries.add(library);
        for (String name : specs.keySet()) {
            i = 0;
            for (FunctionSpecification func : specs.get(name)) {
                // Initialize function
                Object instance = null;
                // TODO Only initialize functions that are used
                try {
                    Constructor<?>[] constructors = func.getFunctionClass().getConstructors();
                    if (constructors.length == 0) {
                        throw new Exception("No constructors found for FunctionSpecification: " + name);
                    }
                    for (Constructor<?> constructor : constructors) {
                        // static class with default constructor
                        if (constructor.getParameterCount() == 0) {
                            instance = constructor.newInstance();
                            break;
                        }
                        // inner class with default constructor
                        // OR static class that injects the library
                        if (constructor.getParameterCount() == 1
                                && constructor.getParameters()[0].getType().equals(library.getClass())) {
                            instance = constructor.newInstance(library);
                            break;
                        }

                        // Constructor not recognized
                        System.out.println("Function constructor not supported: " + constructor.toString());
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return;
                }

                // Register wrapper function to virtual machine
                if (instance != null && instance instanceof Function) {
                    vmIndex = vm.addFunction((Function) instance);
                } else {
                    return;
                }

                // Register function spec to compiler
                specIndex = functions.size();

                // TODO Add handling for if spec is null or i is out of bounds
                func.setIndex(vmIndex);

                functions.add(func);

                // Add function name . function spec mapping
                List<Integer> l = functionIndex.get(name.toLowerCase());
                if (l == null) {
                    l = new ArrayList<>();
                    functionIndex.put(name.toLowerCase(), l);
                }
                l.add(specIndex);

                i++;
            }
        }
    }

    @Deprecated() // Need to review if this is still used or replace with addFunctions
    public void addFunction (String name,
                                 Function          func,
                                 ParamTypeList   params,
                                 boolean isFunction,
                                 boolean brackets, ValType           returnType,
                                 boolean                timeshare,
                                 boolean                freeTempData,
                                 ParamValidationCallback paramValidationCallback,
                                 boolean				conditionalTimeshare) {

        // Register wrapper function to virtual machine
        int vmIndex = vm.addFunction (func);

        // Register function spec to compiler
        int specIndex = functions.size ();
        FunctionSpecification spec = new FunctionSpecification (
                func.getClass(),
                params,
                brackets,
                isFunction,
                returnType,
                timeshare,
                freeTempData,
                paramValidationCallback);
        spec.setIndex(vmIndex);
        spec.setConditionalTimeshare(conditionalTimeshare);
        functions.add (spec);

        // Add function name -> function spec mapping
        List<Integer> l = functionIndex.get(name.toLowerCase());
        if (l == null) {
            l = new ArrayList<>();
            functionIndex.put(name.toLowerCase(), l);
        }
        l.add(specIndex);
    }

    private boolean compileFunction() {
        // Get function name
        String name = token.getText();
        if (!getToken()) {
            return false;
        }
        return compileFunctionCall(name, FunctionType.FT_NORMAL, false, true);
    }

    private boolean compileFunctionCall(String name, FunctionType type, boolean needResult, boolean verbose) {

        // Find function specifications.
        // (Note: There may be more than one with the same name.
        // We collect the possible candidates in an array, and prune out the
        // ones
        // whose paramater types are incompatible as we go..)
        ExtendedFunctionSpecification[] functions = new ExtendedFunctionSpecification[TC_MAXOVERLOADEDFUNCTIONS];
        int functionCount = 0;

        // Find builtin functions
        boolean found = false;
        List<Integer> functionIndexCollection = functionIndex.get(name);
        if (functionIndexCollection != null) {
            for (Integer i : functionIndexCollection) {
                if (!(functionCount < TC_MAXOVERLOADEDFUNCTIONS)) {
                    break;
                }
                // Get specification
                FunctionSpecification spec = this.functions.get(i);
                found = true;

                // Check whether function returns a value (if we need one)
                if (!needResult || spec.isFunction()) {
                    if (functions[functionCount] == null) {
                        functions[functionCount] = new ExtendedFunctionSpecification();
                    }
                    functions[functionCount].setSpecification(spec);
                    functions[functionCount].setBuiltin(true);
                    functionCount++;
                }
            }
        }

        // Find plugin functions
        Mutable<Integer> functionCountRef = new Mutable<>(functionCount);
        plugins.findFunctions(name, functions, functionCountRef, TC_MAXOVERLOADEDFUNCTIONS);
        functionCount = functionCountRef.get();

        // No functions?
        if (functionCount == 0) {
            if (found) {
                // We found some functions, but discarded them all. This would
                // only ever happen if we required a return value,
                // but none of the functions return one.
                setError(name + " does not return a value");
                return false;
            } else {
                setError(name + " is not a recognised function name", verbose);
                return false;
            }
        }


        // Only the first instance will be checked to see whether we need
        // brackets.
        // (Therefore either all instances should have brackets, or all
        // instances
        // should have no brackets.)
        boolean brackets = functions[0].getSpecification().hasBrackets();

        if ((syntax == LS_TRADITIONAL || syntax == LS_TRADITIONAL_SUFFIX ) && brackets) {
            // Special brackets rules for traditional syntax
            brackets = false;
            // Look for a version of the function that:
            // * Has at least one parameter, AND
            // * Returns a value
            //
            // If one is found, then we require brackets. Otherwise we don't.
            for (int i = 0; i < functionCount && !brackets; i++) {
                brackets = functions[i].getSpecification().isFunction();
            }
            // && functions.get(i).paramTypes.getParams().size() > 0; // Need to
            // rethink below loop before we can enable this
        }

        boolean isNoParamFunction = functionCount == 1 && functions[0].getSpecification().isFunction() && functions[0].getSpecification().getParamTypes().getParams().size() == 0;

        // Expect opening bracket
        if (type == FunctionType.FT_NORMAL && brackets) {
            if (!token.getText().equals("(")) {
                setError("Expected '('");
                return false;
            }
            // Skip it
            if (!getToken()) {
                return false;
            }
        }

        // Generate code to push parameters
        boolean first = true;
        int count = 0;

        // Usually pushCount = count (the parameter count).
        // However "any type" parameters also have their
        // data type pushed with them, in which case
        // pushCount > count.
        int pushCount = 0;

        while (functionCount > 0 &&
            ((type == FunctionType.FT_NORMAL && !token.getText().equals(")") && !atSeparatorOrSpecial () && !isNoParamFunction)		// Exit criteria for normal functions
                || (type == FunctionType.FT_OPERATOR_UNARY && count < 1)															// Exit criteria for unary functions
                || (type == FunctionType.FT_OPERATOR_BINARY && count < 2))) {														// Exit criteria for binary functions

            // Trim functions with less parameters than we have found
            int src, dst;
            dst = 0;
            for (src = 0; src < functionCount; src++) {
                if (functions[src].getSpecification().getParamTypes().getParams().size() > count) {
                    functions[dst++] = functions[src];
                }
            }
            functionCount = dst;

            // None left?
            if (functionCount == 0) {
                if (type == FunctionType.FT_NORMAL) {
                    if (brackets) {
                        setError("Expected ')'");
                    } else {
                        setError("Expected ':' or end of line");
                    }
                }
                return false;
            }

            if (type == FunctionType.FT_NORMAL && !first) {
                // Expect comma
                if (!token.getText().equals(",")) {
                    setError("Expected ','");
                    return false;
                }
                // Skip it
                if (!getToken()) {
                    return false;
                }
            }
            first = false;

            // Generate code to evaluate parameter
            switch (type)
            {
                case FT_NORMAL:
                    if (!compileExpression()) {
                        return false;
                    }
                    break;
                case FT_OPERATOR_UNARY:
                    // Register already holds value
                    break;
                case FT_OPERATOR_BINARY:
                    // Initially reg contains second parameter and reg2 contains first.
                    // Swap them each iteration to get the correct parameter into reg.
                    if (!compileSwap()) {
                        return false;
                    }
                    break;
            }

            // Find first valid function which matches at this parameter
            int matchIndex = -1;
            boolean isAnyType = false;
            int i;
            for (i = 0; i < functionCount && matchIndex < 0; i++) {
                ValType valType = new ValType(
                        functions[i].getSpecification().getParamTypes().getParams().get(count));

                // Check for undefined type parameter
                if (valType.matchesType(BasicValType.VTP_UNDEFINED)) {

                    // Function definition indicates whether parameter type is
                    // valid via a compiler time callback function.
                    if (functions[i].getSpecification().getParamValidationCallback().run(count, regType)) {

                        // Found "any type" match
                        matchIndex = i;
                        isAnyType = true;
                    } else {
                        setError("Incorrect data type");
                    }
                } else {

                    // Specific type requested.
                    // Check parameter can be converted to that type
                    if (compileConvert(valType)) {

                        // Found specific type match
                        matchIndex = i;
                        isAnyType = false;
                    }
                }
            }

            if (matchIndex >= 0) {

                // Clear any errors that non-matching instances might have set.
                clearError();

                ValType valType = new ValType(
                        functions[matchIndex].getSpecification().getParamTypes().getParams().get(count));

                // Filter out all functions whose "count" parameter doesn't
                // match "type".
                dst = 0;
                for (src = 0; src < functionCount; src++) {

                    if (isAnyType) {
                        // If the first function to match accepts an "any type"
                        // parameter, then all other overloads must be an
                        // "any type"
                        // parameter.
                        if (functions[src].getSpecification().getParamValidationCallback().run(count, regType)) {
                            functions[dst++] = functions[src];
                        }
                    } else {
                        // Likewise if the first function to match requires a specific parameter type,
                        // then all other overloads must require that same type.
                        if (functions[src]
                                .getSpecification()
                                .getParamTypes()
                                .getParams()
                                .get(count)
                                .equals(valType)) {
                            functions[dst++] = functions[src];
                        }
                    }
                }
                functionCount = dst;

                // (Should at least have the function that originally matched the register)
                assertTrue(functionCount > 0);

                // Generate code to push parameter to stack
                compilePush();
                pushCount++;

                // If parameter is an "any type" then generate code to push the
                // parameter type to the stack.
                if (isAnyType) {
                    addInstruction(
                            OpCode.OP_LOAD_CONST, BasicValType.VTP_INT, new Value((int) vm.getStoreTypeIndex(regType)));
                    regType.setType(BasicValType.VTP_INT);
                    compilePush();
                    pushCount++;
                }
            } else {
                // No function matched. (Return last compile convert error).
                return false;
            }

            // Count parameters pushed
            count++;
        }

        // Find the first function instance that accepts this number of
        // parameters
        int matchIndex = -1;
        int i;
        for (i = 0; i < functionCount && matchIndex < 0; i++) {
            if (functions[i].getSpecification().getParamTypes().getParams().size() == count) {
                matchIndex = i;
            }
        }
        if (matchIndex < 0) {
            if (count == 0) {
                setError("Expected function parameter");
            } else {
                setError("Expected ','");
            }
            return false;
        }
        ExtendedFunctionSpecification spec = functions[matchIndex];

        // Expect closing bracket
        if (brackets) {
            if (!token.getText().equals(")")) {
                setError("Expected ')'");
                return false;
            }
            // Skip it
            if (!getToken()) {
                return false;
            }
        }

        // Generate code to call function
        addFunctionCallInstruction(spec);

        // If function has return type, it will have changed the type in the register
        if (spec.getSpecification().isFunction()) {
            regType.setType(spec.getSpecification().getReturnType());

            // If data is too large to fit in the register, it will be returned
            // in the "temp" area. If the data contains strings, they will need
            // to be "destroyed" when temp data is unwound.
            if (!regType.canStoreInRegister() && vm.getDataTypes().containsString(regType)) {
                addInstruction(
                        OpCode.OP_REG_DESTRUCTOR, BasicValType.VTP_INT, new Value((int) vm.getStoreTypeIndex(regType)));
            }

            if (!compileDataLookup(false)) {
                return false;
            }
        }

        // Note whether function has generated temporary data
        freeTempData = freeTempData | spec.getSpecification().getFreeTempData();

        // Generate code to clean up stack
        for (int i2 = 0; i2 < pushCount; i2++) {
            if (!compilePop()) {
                return false;
            }
        }

        // Generate explicit timesharing break (if necessary)
        addTimeshareOpCode(spec.getSpecification());

        return true;
    }

    private boolean compileConstant() {

        // Skip CONST
        if (!getToken()) {
            return false;
        }

        // Expect at least one field in dim
        if (atSeparatorOrSpecial()) {
            setError("Expected constant declaration");
            return false;
        }

        // Parse fields in dim
        boolean needComma = false; // First element doesn't need a comma
        while (!atSeparatorOrSpecial()) {

            // Handle commas
            if (needComma) {
                if (!token.getText().equals(",")) {
                    setError("Expected ','");
                    return false;
                }
                if (!getToken()) {
                    return false;
                }
            }
            needComma = true; // Remaining elements do need commas

            // Read constant name
            if (token.getTokenType() != TokenType.CTT_TEXT) {
                setError("Expected constant name");
                return false;
            }
            String name = token.getText();
            if (programConstants.containsKey(name)) {
                setError("'" + name + "' has already been declared as a constant.");
                return false;
            }
            if (!checkName(name)) {
                return false;
            }
            if (!getToken()) {
                return false;
            }

            // Determine constant type from last character of constant name
            Integer type = BasicValType.VTP_UNDEFINED;
            if (!name.isEmpty()) {
                char last = name.charAt(name.length() - 1);
                if (last == '$') {
                    type = BasicValType.VTP_STRING;
                } else if (last == '#' || last == '!') {
                    type = BasicValType.VTP_REAL;
                } else if (last == '%') {
                    type = BasicValType.VTP_INT;
                }
            }

            if (token.getText().equals("as")) {
                if (type != BasicValType.VTP_UNDEFINED) {
                    setError("'" + name + "'s type has already been defined. Cannot use 'as' here.");
                    return false;
                }
                if (!getToken()) {
                    return false;
                }
                if (token.getText().equals("integer")) {
                    type = BasicValType.VTP_INT;
                } else if (token.getText().equals("single") || token.getText().equals("double")) {
                    // Note: Basic4GL supports only one type of floating point
                    // number.
                    // We will accept both keywords, but simply allocate a real
                    // (= single precision) floating point number each time.
                    type = BasicValType.VTP_REAL;
                } else if (token.getText().equals("string")) {
                    type = BasicValType.VTP_STRING;
                } else {
                    setError("Expected 'integer', 'single', 'double', 'string'");
                    return false;
                }
                if (!getToken()) {
                    return false;
                }
            }

            // Default type to integer if not defined
            if (type == BasicValType.VTP_UNDEFINED) {
                type = getDefaultDataBasicValType();
            }

            // Expect =
            if (!token.getText().equals("=")) {
                setError("Expected '='");
                return false;
            }
            if (!getToken()) {
                return false;
            }

            // Compile constant expression
            Value value = new Value();
            String stringValue = "";

            Mutable<Integer> typeRef = new Mutable<>(type);
            Mutable<Value> valueRef = new Mutable<>(value);
            Mutable<String> stringValueRef = new Mutable<>(stringValue);

            if (!evaluateConstantExpression(typeRef, valueRef, stringValueRef)) {
                return false;
            }

            // Update local values from references
            type = typeRef.get();
            value = valueRef.get();
            stringValue = stringValueRef.get();

            switch (type) {
                case BasicValType.VTP_INT:
                    programConstants.put(name, new Constant(value.getIntVal()));
                    break;
                case BasicValType.VTP_REAL:
                    programConstants.put(name, new Constant(value.getRealVal()));
                    break;
                case BasicValType.VTP_STRING:
                    programConstants.put(name, new Constant((String) ("S" + stringValue)));
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private boolean compileFreeTempData() {

        // Add instruction to free temp data (if necessary)
        if (freeTempData) {
            addInstruction(OpCode.OP_FREE_TEMP, BasicValType.VTP_INT, new Value());
        }
        freeTempData = false;

        return true;
    }

    private boolean compileExtendedUnOperation(short operOpCode) {

        Mutable<ValType> type = new Mutable<>(new ValType());
        Mutable<Integer> opFunc = new Mutable<>(-1);
        Mutable<Boolean> freeTempData = new Mutable<>(false);
        Mutable<ValType> resultType = new Mutable<>(new ValType());
        boolean found = false;

        // Iterate through external operator extension functions until we find
        // one that can handle our data.
        for (int i = 0; i < unaryOperatorExtensions.size() && !found; i++) {

            // Setup input data
            type.get().setType(regType);
            opFunc.set(-1);
            freeTempData.set(false);
            resultType.set(new ValType());

            // Call function
            found = unaryOperatorExtensions.get(i).run(type, operOpCode, opFunc, resultType, freeTempData);
        }

        if (!found) // No handler found.
        {
            return false; // This is not an error, but operation must be
        }
        // passed through to default operator handling.

        // Generate code to convert operands as necessary
        boolean conv = compileConvert(type.get());
        assertTrue(conv);

        // Generate code to call external operator function
        assertTrue(opFunc.get() >= 0);
        assertTrue(opFunc.get() < vm.getOperatorFunctionCount());
        addInstruction(OpCode.OP_CALL_OPERATOR_FUNC, BasicValType.VTP_INT, new Value(opFunc.get()));

        // Set register to result type
        regType.setType(resultType.get());

        // Record whether we need to free temp data
        this.freeTempData = this.freeTempData || freeTempData.get();

        return true;
    }

    private boolean compileExtendedBinOperation(short operOpCode) {

        Mutable<ValType> type1 = new Mutable<>(new ValType());
        Mutable<ValType> type2 = new Mutable<>(new ValType());
        Mutable<Integer> opFunc = new Mutable<>(-1);
        Mutable<Boolean> freeTempData = new Mutable<>(false);
        Mutable<ValType> resultType = new Mutable<>(new ValType());
        boolean found = false;

        // Iterate through external operator extension functions until we find
        // one that can handle our data.
        for (int i = 0; i < binaryOperatorExtensions.size() && !found; i++) {

            // Setup input data
            type1.get().setType(regType);
            type2.get().setType(reg2Type);
            opFunc.set(-1);
            freeTempData.set(false);
            resultType.set(new ValType());

            // Call function
            found = binaryOperatorExtensions.get(i).run(type1, type2, operOpCode, opFunc, resultType, freeTempData);
        }

        if (!found) // No handler found.
        {
            return false; // This is not an error, but operation must be
        }
        // passed through to default operator handling.

        // Generate code to convert operands as necessary
        boolean conv1 = compileConvert(type1.get());
        boolean conv2 = compileConvert2(type2.get());
        assertTrue(conv1);
        assertTrue(conv2);

        // Generate code to call external operator function
        assertTrue(opFunc.get() >= 0);
        assertTrue(opFunc.get() < vm.getOperatorFunctionCount());
        addInstruction(OpCode.OP_CALL_OPERATOR_FUNC, BasicValType.VTP_INT, new Value(opFunc.get()));

        // Set register to result type
        regType.setType(resultType.get());

        // Record whether we need to free temp data
        this.freeTempData = this.freeTempData || freeTempData.get();

        return true;
    }

    /**
     * Find function name for function #.
     * Used for debug reporting
     * @param index
     * @return
     */
    public String getFunctionNameAt(int index) {
        for (String key : functionIndex.keySet()) {
            for (Integer i : functionIndex.get(key)) {
                if (i.equals(index)) {
                    return key;
                }
            }
        }
        return "???";
    }

    private boolean compileAlloc() {

        // Skip "alloc"
        if (!getToken()) {
            return false;
        }

        // Expect &pointer variable
        if (token.getText().equals("&")) {
            setError("First argument must be a pointer");
            return false;
        }

        // Load pointer
        if (!(compileLoadVar() && compileTakeAddress())) {
            return false;
        }

        // Store pointer type
        ValType ptrType = new ValType(regType), dataType = new ValType(regType);
        dataType.isByRef = false;
        dataType.pointerLevel--;

        // Get pointer address
        if (!compileTakeAddress()) {
            setError("First argument must be a pointer");
            return false;
        }

        // Push destination address to stack
        if (!compilePush()) {
            return false;
        }

        // Generate code to push array dimensions (if any) to the stack.
        int i;
        for (i = 0; i < dataType.arrayLevel; i++) {

            // Expect ,
            if (!token.getText().equals(",")) {
                setError("Expected ','");
                return false;
            }
            if (!getToken()) {
                return false;
            }

            // Generate code to evaluate array index, and convert to an integer.
            if (!compileExpression()) {
                return false;
            }
            if (!compileConvert(BasicValType.VTP_INT)) {
                setError("Array index must be a number. "
                        + vm.getDataTypes().describeVariable("", regType)
                        + " is not a number");
                return false;
            }

            // Push array index to stack
            if (!compilePush()) {
                return false;
            }
        }

        // Add alloc instruction
        addInstruction(OpCode.OP_ALLOC, BasicValType.VTP_INT, new Value((int) vm.getStoreTypeIndex(dataType)));

        // Instruction automatically removes all array indices that were pushed
        // to the stack.
        for (i = 0; i < dataType.arrayLevel; i++) {
            operandStack.remove(operandStack.size() - 1);
        }

        // Instruction also automatically leaves the register pointing to the
        // new data.
        regType.setType(ptrType);
        regType.isByRef = false;

        // Generate code to pop destination address
        if (!compilePop()) {
            return false;
        }

        // Generate code to save address to pointer
        addInstruction(OpCode.OP_SAVE, BasicValType.VTP_INT, new Value());

        return true;
    }

    ParserPos savePosition() {

        // Save the current parser position, so we can return to it later.
        ParserPos pos = new ParserPos();
        pos.line = parser.getLine();
        pos.column = parser.getColumn();
        pos.token = token;
        return pos;
    }

    void restorePosition(ParserPos position) {

        // Restore parser position
        parser.setPos(position.line, position.column);
        token = position.token;
    }

    // Debugging
    public String describeStackCall(int returnAddress) {

        // Return a string describing the gosub call
        if (returnAddress == 0 || returnAddress >= vm.getInstructionCount()) {
            return "???";
        }

        // Look at instruction immediately before return address.
        // This should be the gosub
        if (vm.getInstruction(returnAddress - 1).opCode != OpCode.OP_CALL) {
            return "???";
        }

        // Get target address
        int target = vm.getInstruction(returnAddress - 1).value.getIntVal();

        // Lookup label name
        String name = labelIndex.get(target);
        if (name == null) {
            return "???";
        }

        // Return label name
        return name;
    }

    public boolean tempCompileExpression(String expression, ValType valType, boolean inFunction, int currentFunction) {

        // Load expression into parser
        parser.getSourceCode().clear();
        parser.getSourceCode().add(expression);
        parser.reset();
        lastLine = 0;
        lastCol = 0;

        // Reset compiler state
        clearState();
        this.inFunction = inFunction;
        this.currentFunction = currentFunction;
        this.currentCodeBlockIndex = 0;

        // Clear error state
        clearError();
        parser.clearError();

        // Read first token
        if (!getToken(true, false)) {
            return false;
        }

        // Compile code
        if (!compileExpression()) {
            return false;
        }

        if (token.getTokenType() != TokenType.CTT_EOL) {
            setError("Extra characters after expression");
            return false;
        }

        // Terminate program
        addInstruction(OpCode.OP_END, BasicValType.VTP_INT, new Value());

        // Return expression result type
        valType.setType(regType);
        return true;
    }

    private boolean compileData() {

        // Skip "data"
        if (!getToken(false, true))
        {
            // Use "DATA" mode read
            return false;
        }

        // Compile data elements
        boolean needComma = false;
        do {

            // Handle commas
            if (needComma) {
                if (!token.getText().equals(",")) {
                    setError("Expected ','");
                    return false;
                }
                if (!getToken(false, true)) {
                    return false;
                }
            }

            // Remaining elements do need commas
            needComma = true;

            // Consecutive commas?
            if (token.getText().equals(",") || atSeparatorOrSpecial()) {

                // Store a blank string
                vm.storeProgramData(BasicValType.VTP_STRING, new Value(0));
            } else {

                // Extract value
                Value v = new Value();
                if (token.getValType() == BasicValType.VTP_STRING) {

                    // Allocate new string constant
                    String text = token.getText().substring(1, token.getText().length() - 1); // Remove S prefix
                    v.setIntVal(vm.storeStringConstant(text));
                } else if (token.getValType() == BasicValType.VTP_INT) {
                    v.setIntVal(Cast.toInt(token.getText()));
                } else {
                    v.setRealVal(Float.valueOf(token.getText()));
                }

                // Store data in VM
                vm.storeProgramData(token.getValType(), v);

                // Next token
                if (!getToken()) {
                    return false;
                }
            }
        } while (!atSeparatorOrSpecial());
        return true;
    }

    private boolean compileDataRead() {

        // Skip "read"
        if (!getToken()) {
            return false;
        }

        // Expect at one variable name
        if (atSeparatorOrSpecial()) {
            setError("Expected variable name");
            return false;
        }

        // Parse fields in dim
        boolean needComma = false; // First element doesn't need a comma
        while (!atSeparatorOrSpecial()) {

            // Handle commas
            if (needComma) {
                if (!token.getText().equals(",")) {
                    setError("Expected ','");
                    return false;
                }
                if (!getToken()) {
                    return false;
                }
            }
            needComma = true; // Remaining elements do need commas

            // Generate code to load target variable address
            if (!compileLoadVar()) {
                return false;
            }

            // Must be a basic type.
            ValType type = new ValType(regType);
            if (!type.isBasicType()) {
                setError("Can only READ built in types (int, real or string)");
                return false;
            }

            // Convert load target variable into take address of target variable
            if (!compileTakeAddress()) {
                setError("Value cannot be READ into");
                return false;
            }

            if (!compilePush()) {
                return false;
            }

            // Generate READ op-code
            addInstruction(OpCode.OP_DATA_READ, type.basicType, new Value());

            if (!compilePop()) {
                return false;
            }

            // Save reg into [reg2]
            addInstruction(OpCode.OP_SAVE, reg2Type.basicType, new Value());
        }
        return true;
    }

    private boolean compileDataReset() {

        // Skip "reset"
        if (!getToken()) {
            return false;
        }

        // If label specified, use offset stored in label
        if (!atSeparatorOrSpecial()) {

            // Validate label
            if (token.getTokenType() != TokenType.CTT_TEXT) {
                setError("Expected label name");
                return false;
            }

            // Record reset, so that we can fix up the offset in the second
            // compile pass.
            String labelName = symbolPrefix + token.getText();
            resets.add(new Jump(vm.getInstructionCount(), labelName));

            // Skip label name
            if (!getToken()) {
                return false;
            }
        }

        // Add jump instruction
        addInstruction(OpCode.OP_DATA_RESET, BasicValType.VTP_INT, new Value(0));

        return true;
    }

    private boolean compileArrayMax()
    {
        // Skip "arraymax"
        if (!getToken()) {
            return false;
        }

        // Function takes a parameter and returns a value. Thus it requires brackets in all syntax variations.
        if (!token.getText().equals("(")) {
            setError("Expected '('");
            return false;
        }
        if (!getToken()) {
            return false;
        }

        // Compile single parameter
        if (!compileExpression()) {
            return false;
        }

        // Expect this to be an array
        if (regType.arrayLevel == 0)
        {
            setError("Expected an array");
            return false;
        }

        // Closing ")"
        if (!token.getText().equals(")"))
        {
            setError("Expected ')'");
            return false;
        }
        if (!getToken()) {
            return false;
        }

        // Push array as function parameter
        if (!compilePush()) {
            return false;
        }

        // Look up arraymax function
        // Must have a parameter, but don't check its type
        ExtendedFunctionSpecification spec = findExtendedFunction("arraymax", true, BasicValType.VTP_UNDEFINED);
        if (spec.getSpecification() == null) {
            return false;
        }
        if (!spec.getSpecification().isFunction()
                || !(spec.getSpecification().getReturnType().basicType == BasicValType.VTP_INT)) {
            setError("'arraymax' function should return an integer.");
            return false;
        }

        // Generate call
        addFunctionCallInstruction(spec);
        if (!compilePop()) {
            return false;
        }

        // Return type is integer
        regType = new ValType(BasicValType.VTP_INT);

        return true;
    }

    int getFnPointerPrototypeIndex(UserFuncPrototype prototype)
    {
        // TODO: Optimise with hash lookup?

        // Look for existing matching prototype
        Vector<UserFuncPrototype> prototypes = vm.getUserFunctionPrototypes();
        for (int i = 0; i < prototypes.size(); i++) {
            if (prototypes.get(i).isCompatibleWith(prototype)) {
                return i;
            }
        }
        // Or add one if none found
        prototypes.add(prototype);
        return prototypes.size() - 1;
    }


    boolean compileUserFunctionAddress()
    {
        // Current token must be the user function name
        assertTrue(token.getTokenType() == TokenType.CTT_USER_FUNCTION);

        // Read function name
        String name = token.getText();
        if (!getToken()) {
            return false;
        }

        // Lookup prototype
        int index = getUserFunctionIndex(name);

        // Add op code to load function index + 1 into register.
        // Note: We add 1, so that we can reserve 0 as a "null" value.
        // (This makes the code simpler, as we don't have to have any special initialisation logic,
        // or null checking logic when assigning function pointers).
        addInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_FUNC_PTR, new Value(index + 1));

        // Set register type
        regType = new ValType(BasicValType.VTP_FUNC_PTR);
        regType.prototypeIndex = vm.getUserFunctions().get(index).prototypeIndex;

        return true;
    }

    boolean checkTypeIsDefined(ValType type)
    {
        // Check that field data type has been fully defined
        if (type.basicType >= 0) {
		    Structure struc = vm.getDataTypes().getStructures().get(type.basicType);
            if (!struc.isDefined) {
                setError("Structure type '" + struc.name + "' has not been defined yet");
                return false;
            }
        }

        return true;
    }

    boolean evaluateConstantExpression(
            Mutable<Integer> basictype, Mutable<Value> result, Mutable<String> stringResult) {

        // Note: If type is passed in as VTP_UNDEFINED, then any constant
        // expression will be accepted,
        // and type will be set to the type that was found.
        // Otherwise it will only accept expressions that can be cast to the
        // specified type.
        // If the expression is a string, its value will be returned in
        // stringResult.
        // Otherwise its value will be returned in result.

        // Mark the current size of the program. This is where the expression
        // will start
        int expressionStart = vm.getInstructionCount();

        // Compile expression, specifying that it must be constant
        if (!compileExpression(true)) {
            return false;
        }

        // Convert to required type
        if (basictype.get() != BasicValType.VTP_UNDEFINED) {
            if (!compileConvert(basictype.get())) {
                return false;
            }
        }

        // Add "end program" opcode, so we can safely evaluate it
        addInstruction(OpCode.OP_END, BasicValType.VTP_INT, new Value());

        // Setup virtual machine to execute expression
        // Note: Expressions can't branch or loop, and it's very difficult to
        // write one that evaluates to a large number of op-codes.
        // Therefore we won't worry
        // about processing windows messages or checking for pause state etc.
        vm.clearError();
        vm.gotoInstruction(expressionStart);
        try {
            do {
                vm.continueVM(1000);
            } while (!vm.hasError() && !vm.isDone());
        } catch (Exception e) {
            setError("Error evaluating constant expression");
            return false;
        }
        if (vm.hasError()) {
            setError("Error evaluating constant expression");
            return false;
        }

        // Now we have the result type of the constant expression,
        // AND the virtual machine has its value stored in the register.

        // Roll back all the expression op-codes
        vm.gotoInstruction(0);
        vm.rollbackProgram(expressionStart);

        // Set return values
        basictype.set(regType.basicType);
        if (basictype.get() == BasicValType.VTP_STRING) {
            stringResult.set(vm.getRegString());
        } else {
            result.set(vm.getReg());
        }

        return true;
    }

    private boolean compileConstantExpression() {
        return compileConstantExpression(BasicValType.VTP_UNDEFINED);
    }

    private boolean compileConstantExpression(int basictype) {

        // Evaluate constant expression
        Value value = new Value();
        String stringValue = "";

        // Create wrappers to pass values by reference
        Mutable<Integer> typeRef = new Mutable<>(basictype);
        Mutable<Value> valueRef = new Mutable<>(value);
        Mutable<String> stringValueRef = new Mutable<>(stringValue);

        if (!evaluateConstantExpression(typeRef, valueRef, stringValueRef)) {
            return false;
        }

        // Update local values from wrappers
        basictype = typeRef.get();
        value = valueRef.get();
        stringValue = stringValueRef.get();

        // Generate "load constant" instruction
        if (basictype == BasicValType.VTP_STRING) {

            // Create string constant entry if necessary
            int index = vm.storeStringConstant(stringValue);
            addInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING, new Value(index));
        } else {
            addInstruction(OpCode.OP_LOAD_CONST, basictype, value);
        }

        return true;
    }

    ExtendedFunctionSpecification findFunction2(String name, int paramBasicValType) {
        return findExtendedFunction(name, paramBasicValType != BasicValType.VTP_UNDEFINED, paramBasicValType);
    }

    ExtendedFunctionSpecification findExtendedFunction(String name, boolean hasParam, int paramBasicValType) {

        // Search for plugin function with matching name & param type
        ExtendedFunctionSpecification[] extFunctions = new ExtendedFunctionSpecification[TC_MAXOVERLOADEDFUNCTIONS];
        Mutable<Integer> countRef = new Mutable<Integer>(0);
        plugins.findFunctions(name, extFunctions, countRef, TC_MAXOVERLOADEDFUNCTIONS);
        int count = countRef.get();
        for (int i = 0; i < count; i++) {
            Vector<ValType> params = extFunctions[i].getSpecification().getParamTypes().getParams();
            if ((params.isEmpty() && !hasParam) ||
                    (params.size() == 1 && hasParam && (paramBasicValType == BasicValType.VTP_UNDEFINED || params.get(0).basicType == paramBasicValType))) {
                return extFunctions[i];
            }
        }

        // Search for builtin function with matching name & param type
        ExtendedFunctionSpecification result = new ExtendedFunctionSpecification();
        result.setBuiltin(true);

        // Search for function with matching name & param type
        List<Integer> l = functionIndex.get(name);
        if (l != null) {
            for (Integer i : l) {
                FunctionSpecification spec = functions.get(i);
                Vector<ValType> params = spec.getParamTypes().getParams();
                if ((params.isEmpty() && !hasParam) ||
                        (params.size() == 1 && hasParam && (paramBasicValType == BasicValType.VTP_UNDEFINED || params.get(0).basicType == paramBasicValType)))
                {
                    result.setSpecification(spec);
                    return result;
                }
            }
        }

        // None found
        setError("'" + name + "' function not found");
        result.setSpecification(null);
        return result;
    }



    private boolean compilePrint(boolean forceNewLine) {

        // The print function has a special syntax,
        // and must be compiled separately

        // Skip "print"
        if (!getToken()) {
            return false;
        }

        boolean foundSemiColon = false;
        int operandCount = 0;
        while (!atSeparatorOrSpecial()) {

            // Look for semicolon
            if (token.getText().equals(";")) {

                // Record it, and move on to next
                foundSemiColon = true;
                if (!getToken()) {
                    return false;
                }
            } else {
                foundSemiColon = false;

                // If this is not the first operand, then there will be a string
                // sitting in the register. Need to push it first.
                if (operandCount > 0) {
                    if (!compilePush()) {
                        return false;
                    }
                }

                // Evaluate expression & convert it to string
                if (!(compileExpression() && compileConvert(BasicValType.VTP_STRING))) {
                    return false;
                }

                operandCount++;
            }
        }

        // Add all operands together
        while (operandCount > 1) {
            if (!compilePop()) {
                return false;
            }
            addInstruction(OpCode.OP_OP_PLUS, BasicValType.VTP_STRING, new Value());
            regType.setType(BasicValType.VTP_STRING);

            operandCount--;
        }

        // Push string as function parameter
        if (operandCount == 1) {
            if (!compilePush()) {
                return false;
            }
        }

        // Find print/printr function
        boolean newLine = forceNewLine
                || ((syntax == LS_TRADITIONAL || syntax == LS_TRADITIONAL_PRINT|| syntax == LS_TRADITIONAL_SUFFIX)
                        && !foundSemiColon);

        if (!newLine && operandCount == 0) // Nothing to print?
        {
            // Do nothing!
            return true;
        }

        ExtendedFunctionSpecification spec = findFunction2(
                newLine ? "printr" : "print",
                operandCount == 1 ? BasicValType.VTP_STRING : BasicValType.VTP_UNDEFINED);
        if (spec.getSpecification() == null) {
            return false;
        }

        // Generate code to call it
        addFunctionCallInstruction(spec);
        addTimeshareOpCode(spec);

        // Generate code to clean up stack
        if (operandCount == 1) {
            if (!compilePop()) {
                return false;
            }
        }

        return true;
    }

    private boolean compileInput() {

        // Input also has a special syntax.
        // This still isn't a complete input implementation, as it doesn't
        // support inputting multiple values on one line.
        // But it's a lot better than before.

        // Skip "input"
        if (!getToken()) {
            return false;
        }

        // Check for prompt
        if (token.getTokenType() == TokenType.CTT_CONSTANT && token.getValType() == BasicValType.VTP_STRING) {

            // Allocate new string constant
            // Remove S prefix
            String text = token.getText().substring(1, token.getText().length());

            if (!getToken()) {
                return false;
            }

            // Expect , or ;
            if (token.getText().equals(";")) {
                text += "? ";
            } else if (!token.getText().equals(",")) {
                setError("Expected ',' or ';'");
                return false;
            }
            if (!getToken()) {
                return false;
            }

            // Create new string constant
            int index = vm.storeStringConstant(text);

            // Generate code to print it (load, push, call "print" function)
            addInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING, new Value(index));
            regType.setType(BasicValType.VTP_STRING);
            if (!compilePush()) {
                return false;
            }

            // Generate code to call "print" function
            ExtendedFunctionSpecification printSpec = findFunction2("print", BasicValType.VTP_STRING);
            if (printSpec.getSpecification() == null) {
                return false;
            }
            addFunctionCallInstruction(printSpec);
            addTimeshareOpCode(printSpec);

            // Generate code to clean up stack
            if (!compilePop()) {
                return false;
            }
        }

        // Generate code to effectively perform
        // variable = Input$()
        // or
        // variable = val(Input$())
        //
        // (Depending on whether variable is a string)

        // Generate code to load target variable
        if (!compileLoadVar()) {
            return false;
        }

        // Must be a simple variable
        if (!regType.isBasicType()) {
            setError("Input variable must be a basic string, integer or real type");
            return false;
        }
        int variableType = regType.basicType;

        // Generate code to push its address to stack
        if (!(compileTakeAddress() && compilePush())) {
            return false;
        }

        // Generate code to call "input$()" function
        ExtendedFunctionSpecification inputSpec = findFunction2("input$", BasicValType.VTP_UNDEFINED);
        if (inputSpec.getSpecification() == null) {
            return false;
        }
        addFunctionCallInstruction(inputSpec);
        // Timesharing break is necessary
        addInstruction(OpCode.OP_TIMESHARE, BasicValType.VTP_INT, new Value());
        regType.setType(BasicValType.VTP_STRING);

        // If the variable is not a string, then we need to convert it to the
        // target type.
        // We do this by inserting an implicit call to the val() function.
        if (variableType != BasicValType.VTP_STRING) {

            // Push register back as input to val function
            if (!compilePush()) {
                return false;
            }

            // Generate code to call "val()" function
            ExtendedFunctionSpecification valSpec = findFunction2("val", BasicValType.VTP_STRING);
            if (valSpec.getSpecification() == null) {
                return false;
            }
            addFunctionCallInstruction(valSpec);
            addTimeshareOpCode(valSpec);

            regType.setType(BasicValType.VTP_REAL);

            // Clean up stack
            if (!compilePop()) {
                return false;
            }
        }

        // Generate code to pop target address into reg2
        if (!compilePop()) {
            return false;
        }

        if (!compileConvert(reg2Type.basicType)) {
            // Technically this should never actually happen
            setError("Types do not match");
            return false;
        }

        // Generate code to save value
        addInstruction(OpCode.OP_SAVE, reg2Type.basicType, new Value());

        return true;
    }

    private boolean compileLanguage() {

        // Compile language directive
        // Skip "language"
        if (!getToken()) {
            return false;
        }

        // Expect syntax type
        if (token.getText().equals("traditional")) {
            syntax = LS_TRADITIONAL;
        } else if (token.getText().equals("basic4gl")) {
            syntax = LS_BASIC4GL;
        } else if (token.getText().equals("traditional_print")) {
            syntax = LS_TRADITIONAL_PRINT;
        } else if (token.getText().equals("traditional_suffix")) {
            syntax = LS_TRADITIONAL_SUFFIX;
        } else {
            setError("Expected 'traditional', 'basic4gl' or 'traditional_print' or 'traditional_suffix'");
            return false;
        }

        // Skip syntax token
        if (!getToken()) {
            return false;
        }

        return true;
    }

    boolean compileFwdDecl()
    {
        // Skip "declare"
        if (!getToken()) {
            return false;
        }

        // Function / subroutine
        if (token.getText().equals("function") || token.getText().equals("sub")) {
            return compileUserFunction(UserFunctionType.UFT_FWDDECLARATION);
        }

        // Structure
        if (token.getText().equals("struc") || token.getText().equals("type")) {
            return compileStructureFwdDecl();
        }

        setError("Expected 'function', 'sub', 'struc' or 'type'");
        return false;
    }

    private boolean compileUserFunctionFwdDecl() {

        // Skip "declare"
        if (!getToken()) {
            return false;
        }

        // Look for "sub" or "function"
        return compileUserFunction(UserFunctionType.UFT_FWDDECLARATION);
    }

    private boolean compileUserFunctionRuntimeDecl() {
        // Skip "runtime"
        if (!getToken()) {
            return false;
        }

        return compileUserFunction(UserFunctionType.UFT_RUNTIMEDECLARATION);
    }

    private boolean compileUserFunction(UserFunctionType funcType) {
        // valtype is not used and dummy value is discarded here.
        return compileUserFunction(funcType, new Mutable<>(new ValType()));
    }

    /**
     * Compiles a user function or subroutine declaration.
     * @param funcType
     * @param funcPointerTypeRef used to return corresponding function pointer type, if funcType = UFT_POINTER
     * @return
     */
    private boolean compileUserFunction(UserFunctionType funcType, Mutable<ValType> funcPointerTypeRef) {

        // Note: funcPointerType is used to return corresponding function pointer type, if funcType = UFT_POINTER

        // Function or sub?
        boolean hasReturnVal;
        if (token.getText().equals("function")) {
            hasReturnVal = true;
        } else if (token.getText().equals("sub")) {
            hasReturnVal = false;
        } else {
            setError("Expected 'sub' or 'function'");
            return false;
        }

        // Check that we are not already inside a function
        if (funcType != UserFunctionType.UFT_POINTER && inFunction) {
            setError("Cannot define a function or subroutine inside another function or subroutine");
            return false;
        }

        // Check that we are not inside a control structure
        if (funcType != UserFunctionType.UFT_POINTER && !checkUnclosedFlowControl()) {
            return false;
        }

        // Mark start of function in source code
        functionStart.setSourcePosition(parser.getLine(), parser.getColumn());

        // Skip "func"
        if (!getToken()) {
            return false;
        }

        // Compile data type
        TokenType tokenType = TokenType.CTT_CONSTANT;
        ValType type = new ValType(BasicValType.VTP_UNDEFINED);
        String name = "";
        Mutable<TokenType> tokenTypeRef = new Mutable<>(tokenType);
        Mutable<ValType> typeRef = new Mutable<>(type);
        Mutable<String> nameRef = new Mutable<>(name);

        if (funcType == UserFunctionType.UFT_POINTER) {
            // Note: Pointers to function types don't have a name.
            // Which also means they don't have anything to suffix with a data type.
            // (Return type can be declared after the function definition, with "as").

            // Otherwise parse name and data type as normal
            if (hasReturnVal) {
                if (!compileDataType(nameRef, typeRef, tokenTypeRef)) {
                    return false;
                }
                // Update local values from references
                tokenType = tokenTypeRef.get();
                type = typeRef.get();
                name = nameRef.get();

            } else {
                if (!compileTokenName(nameRef, tokenTypeRef, false)) {
                    return false;
                }
                // Update local values from references
                tokenType = tokenTypeRef.get();
                name = nameRef.get();
            }

            // Validate function name
            if (tokenType != TokenType.CTT_TEXT
                    && tokenType != TokenType.CTT_USER_FUNCTION
                    && tokenType != TokenType.CTT_RUNTIME_FUNCTION) {
                if (tokenType == TokenType.CTT_FUNCTION) {
                    setError("'" + name + "' has already been used as a built-in function/subroutine name");
                } else {
                    setError("Expected a function/subroutine name");
                }
                return false;
            }

            // Must not be a variable name
            if (vm.getVariables().getVariableIndex(name) >= 0) {
                setError("'" + name + "' has already been used as a variable name");
                return false;
            }

            // Must not be a structure name
            if (vm.getDataTypes().getStrucIndex(name) >= 0) {
                setError("'" + name + "' has already been used as a structure name");
                return false;
            }
        }

        // Allocate a new user function
        UserFuncPrototype prototype = new UserFuncPrototype();
        Mutable<UserFuncPrototype> prototypeRef = new Mutable<>(prototype);

        // Expect "("
        if (!token.getText().equals("(")) {
            setError("Expected '('");
            return false;
        }
        if (!getToken()) {
            return false;
        }

        // Look for function parameters
        if (!token.getText().equals(")")) {
            if (!compileDim(false, true, prototypeRef)) {
                return false;
            }
            // Update local value from reference
            prototype = prototypeRef.get();
        }

        // Expect ")"
        if (!token.getText().equals(")")) {
            setError("Expected ')'");
            return false;
        }
        if (!getToken()) {
            return false;
        }

        // Calculate final return value
        if (hasReturnVal) {

            // Any trailing () denote an array
            while (token.getText().equals("(")) {

                // Room for one more dimension?
                if (type.arrayLevel >= TomVM.ARRAY_MAX_DIMENSIONS) {
                    setError((String) "Arrays cannot have more than "
                            + String.valueOf(TomVM.ARRAY_MAX_DIMENSIONS)
                            + " dimensions.");
                    return false;
                }

                // Add dimension
                type.arrayLevel++;

                if (!getToken()) // Skip "("
                {
                    return false;
                }

                // Expect ")"
                if (!token.getText().equals(")")) {
                    setError("')' expected");
                    return false;
                }
                if (!getToken()) {
                    return false;
                }
            }

            // "as" keyword (QBasic/FreeBasic compatibility)
            if (token.getText().equals("as")) {

                typeRef.set(type);
                nameRef.set(name);

                if (!compileAs(nameRef, typeRef)) {
                    return false;
                }

                // Update local values from references
                name = nameRef.get();
                type = typeRef.get();
            }

            // Default basic type to int if undefined
            if (type.basicType == BasicValType.VTP_UNDEFINED) {
                type.basicType = getDefaultDataBasicValType();
            }

            // Store function return value type
            prototype.hasReturnVal = true;
            prototype.returnValType = type;
        } else {
            prototype.hasReturnVal = false;
        }

        // Store function, and get its index (in currentFunction)
        Vector<UserFunc> functions = vm.getUserFunctions();
        Vector<UserFuncPrototype> prototypes = vm.getUserFunctionPrototypes();

        if (funcType == UserFunctionType.UFT_FWDDECLARATION) {
            // Forward declaration.

            // Function name must not already have been used
            if (isLocalUserFunction(name)) {
                setError("'" + name + "' has already been used as a function/subroutine name");
                return false;
            }

            // Function name must not have been used for a runtime function
            if (isRuntimeFunction(name)) {
                setError((String) "'" + name + "' has already been used as a runtime function/subroutine name");
                return false;
            }

            // Allocate new function
            prototypes.add(prototype);
            prototype = new UserFuncPrototype();
            functions.add(new UserFunc(prototypes.size() - 1, false));
            currentFunction = functions.size() - 1;

            // Map name to function
            getCurrentCodeBlock().userFunctions.put(name, currentFunction);
            if (!isGlobalUserFunction(name)) {
                globalUserFunctionIndex.put(name, currentFunction);
            }

            // Build reverse index (for debugger)
            userFunctionReverseIndex.put(currentFunction, name);
        } else if (funcType == UserFunctionType.UFT_RUNTIMEDECLARATION) {

            // Function name must not already have been used
            if (isLocalUserFunction(name)) {
                setError("'" + name + "' has already been used as a function/subroutine name");
                return false;
            }

            // Function name must not have been used for a runtime function
            if (isRuntimeFunction(name)) {
                setError("'" + name + "' has already been used as a runtime function/subroutine name");
                return false;
            }

            // Store prototype
            prototypes.add(prototype);
            prototype = new UserFuncPrototype();

            // Store runtime function
            runtimeFunctions.add(new com.basic4gl.compiler.RuntimeFunction(prototypes.size() - 1));

            // Map name to runtime function
            runtimeFunctionIndex.put(name, runtimeFunctions.size() - 1);
        } else if (funcType == UserFunctionType.UFT_IMPLEMENTATION) {

            // Function implementation

            // Create jump-past-function op-code
            functionJumpOver = vm.getInstructionCount();
            // Jump target will be fixed up when "endfunction" is compiled
            addInstruction(
                    OpCode.OP_JUMP,
                    BasicValType.VTP_INT,
                    new Value(0));

            if (isRuntimeFunction(name)) {

                // Implementation of runtime function
                int index = runtimeFunctionIndex.get(name);
                RuntimeFunction runtimeFunction = vm.getCurrentCodeBlock().getRuntimeFunction(index);

                // Check if already implemented
                if (runtimeFunction.functionIndex >= 0) {
                    setError("Runtime function/sub '" + name + "' has already been implemented");
                    return false;
                }

                // Function must match runtime prototype
                if (!prototype.matches(
                        prototypes.get(runtimeFunctions.get(index).getPrototypeIndex()))) {
                    setError("Function/sub does not match its RUNTIME declaration");
                    return false;
                }

                // Allocate new function
                prototypes.add(prototype);
                prototype = new UserFuncPrototype();
                functions.add(new UserFunc(prototypes.size() - 1, true, vm.getInstructionCount()));
                currentFunction = functions.size() - 1;

                // Map runtime function to implementation
                runtimeFunction.functionIndex = currentFunction;
            } else {
                if (isLocalUserFunction(name)) {

                    // Function already DECLAREd.
                    String userFunctionName = name.toLowerCase();
                    currentFunction = 0;
                    if (getCurrentCodeBlock().userFunctions.containsKey(userFunctionName)) {
                        currentFunction = getCurrentCodeBlock().userFunctions.get(userFunctionName);
                    }

                    // Must not be already implemented
                    if (functions.get(currentFunction).implemented) {
                        setError("'" + name + "' has already been used as a function/subroutine name");
                        return false;
                    }

                    // Function prototypes must match
                    if (!prototype.matches(prototypes.get(functions.get(currentFunction).prototypeIndex))) {
                        setError((String) "Function/subroutine does not match how it was DECLAREd");
                        return false;
                    }

                    // Save updated function spec
                    // Function starts at next offset
                    functions.get(currentFunction).implemented = true;
                    functions.get(currentFunction).programOffset = vm.getInstructionCount();
                } else {

                    // Completely new function

                    // Allocate a new prototype
                    prototypes.add(prototype);
                    prototype = new UserFuncPrototype();

                    // Allocate a new function
                    functions.add(new UserFunc(prototypes.size() - 1, true, vm.getInstructionCount()));
                    currentFunction = functions.size() - 1;
                }

                // Map name to function
                getCurrentCodeBlock().userFunctions.put(name, currentFunction);
                if (!isGlobalUserFunction(name)) {
                    globalUserFunctionIndex.put(name, currentFunction);
                }
            }

            // Build reverse index (for debugger)
            userFunctionReverseIndex.put(currentFunction, name);

            // Compile the body of the function
            inFunction = true;
        } else if (funcType == UserFunctionType.UFT_POINTER) {
            // Find matching prototype, or register a new one
            int index = getFnPointerPrototypeIndex(prototype);

            // Update function pointer type.
            // Note that some fields (e.g. array level) are already set by caller
            funcPointerTypeRef.get().basicType = BasicValType.VTP_FUNC_PTR;
            funcPointerTypeRef.get().prototypeIndex = index;
        }

        return true;
    }

    boolean compileStructureFwdDecl()
    {
        // Expect "struc" or "type"
        if (!token.getText().equals("struc") && !token.getText().equals("type"))
        {
            setError("Expected 'struc' or 'type'");
            return false;
        }

        // Skip token
        if (!getToken()) {
            return false;
        }

        // Check that we are not already inside a function
        if (inFunction) {
            setError("Cannot declare a structure inside a function or subroutine");
            return false;
        }

        // Check that we are not inside a control structure
        if (!checkUnclosedFlowControl()) {
            return false;
        }

        // Expect structure name
        if (token.getTokenType() != TokenType.CTT_TEXT) {
            setError("Expected structure name");
            return false;
        }
        String name = symbolPrefix + token.getText();
        if (!checkName(name)) {
            return false;
        }
        if (vm.getDataTypes().isStrucStored(name)) {
            // Must be unused
            setError("'" + name + "' has already been used as a structure name");
            return false;
        }

        // Skip structure name
        if (!getToken()) {
            return false;
        }

        // Create structure. Will be flagged as "undefined"
        vm.getDataTypes().createStruc(name);

        return true;
    }

    private boolean compileEndUserFunction(boolean hasReturnVal) {

        // Must be inside a function
        if (!inFunction) {
            if (hasReturnVal) {
                setError("'endfunction' without 'function'");
            } else {
                setError("'endsub' without 'sub'");
            }
            return false;
        }

        // Match end sub/function against sub/function type
        if (getCurrentUserFunctionPrototype().hasReturnVal != hasReturnVal) {
            if (hasReturnVal) {
                setError("'endfunction' without 'function'");
            } else {
                setError("'endsub' without 'sub'");
            }
            return false;
        }

        // Check for unclosed flow controls
        if (!checkUnclosedFlowControl()) {
            return false;
        }

        // Skip 'endfunction'
        if (!getToken()) {
            return false;
        }

        // If end of function is reached without a return value, need to trigger
        // a runtime error.
        if (getCurrentUserFunctionPrototype().hasReturnVal) {
            addInstruction(OpCode.OP_NO_VALUE_RETURNED, BasicValType.VTP_INT, new Value(0));
        } else {
            // Add return-from-user-function instruction
            addInstruction(OpCode.OP_RETURN_USER_FUNC, BasicValType.VTP_INT, new Value(0));
        }

        // Fix up jump-past-function op-code
        assertTrue(functionJumpOver < vm.getInstructionCount());
        vm.getInstruction(functionJumpOver).value.setIntVal(vm.getInstructionCount());

        // Let compiler know we have left the function
        inFunction = false;

        // TODO: If function is supposed to return a value, add an op-code that triggers
        // a run-time error (meaning that the end of the function was reached
        // without finding a "return" command).

        return true;
    }

    private boolean compileUserFunctionCall(boolean mustReturnValue, boolean isRuntimeFunc, boolean isFunctionPtr) {
        assertTrue(isFunctionPtr || (!isRuntimeFunc && token.getTokenType() == TokenType.CTT_USER_FUNCTION)
                || (isRuntimeFunc && token.getTokenType() == TokenType.CTT_RUNTIME_FUNCTION));
        assertTrue(isFunctionPtr || (!isRuntimeFunc && isUserFunction(token.getText()))
                || (isRuntimeFunc && isRuntimeFunction(token.getText())));


        String name = "";
        int index = -1;
        int prototypeIndex;

        if (isFunctionPtr)
        {
            // Look at function pointer type to get prototype index
            assertTrue(regType.basicType == BasicValType.VTP_FUNC_PTR);
            assertTrue(regType.pointerLevel == 0);
            name = "[Function pointer]";
            prototypeIndex = regType.prototypeIndex;
        } else {
            // Read function name
            name = token.getText();
            if (!getToken()) {
                return false;
            }

            // Lookup prototype
            if (isRuntimeFunc) {
                index = runtimeFunctionIndex.get(name);
                prototypeIndex = runtimeFunctions.get(index).getPrototypeIndex();
            } else {
                index = getUserFunctionIndex(name);
                prototypeIndex = vm.getUserFunctions().get(index).prototypeIndex;
            }
        }

        UserFuncPrototype prototype = vm.getUserFunctionPrototypes().get(prototypeIndex);

        if (mustReturnValue && !prototype.hasReturnVal) {
            setError("'" + name + "' does not return a value");
            return false;
        }

        // Add op-code to prepare function stack frame.
        // Stack frame remains inactive while evaluating its parameters.
        if (isFunctionPtr) {
            // Index will be in reg2
            addInstruction(OpCode.OP_CREATE_FUNC_PTR_FRAME, BasicValType.VTP_INT, new Value());
        } else if (isRuntimeFunc) {
            addInstruction(OpCode.OP_CREATE_RUNTIME_FRAME, BasicValType.VTP_INT, new Value(index));
        } else {
            addInstruction(OpCode.OP_CREATE_USER_FRAME, BasicValType.VTP_INT, new Value(index));
        }

        // Determine whether the function needs brackets
        boolean brackets = true;
        if (!isFunctionPtr && (syntax == LS_TRADITIONAL || syntax == LS_TRADITIONAL_SUFFIX)) {
            brackets = prototype.hasReturnVal && prototype.paramCount > 0;
        }

        // Expect "("
        if (brackets) {
            if (!token.getText().equals("(")) {
                setError("Expected '('");
                return false;
            }
            if (!getToken()) {
                return false;
            }
        }

        // Evaluate function parameters
        boolean needComma = false;
        for (int i = 0; i < prototype.paramCount; i++) {
            if (needComma) {
                if (!token.getText().equals(",")) {
                    setError("Expected ','");
                    return false;
                }
                if (!getToken()) {
                    return false;
                }
            }
            needComma = true;

            Mutable<UserFuncPrototype> funcRef = new Mutable<>(prototype);
            if (!compileUserFuncParam(funcRef, i)) {
                return false;
            }
            // Update local value from reference
            prototype = funcRef.get();
        }

        // Expect ")"
        if (brackets) {
            if (!token.getText().equals(")")) {
                setError("Expected ')'");
                return false;
            }
            if (!getToken()) {
                return false;
            }
        }

        // Add op-code to call function.
        // Type: Unused.
        // Value: index of function specification.
        addInstruction(OpCode.OP_CALL_USER_FUNC, BasicValType.VTP_INT, new Value());

        if (prototype.hasReturnVal) {

            // Data containing strings will need to be "destroyed" when the
            // stack unwinds.
            if (!prototype.returnValType.canStoreInRegister()
                    && vm.getDataTypes().containsString(prototype.returnValType)) {
                addInstruction(OpCode.OP_REG_DESTRUCTOR, BasicValType.VTP_INT, new Value((int)
                        vm.getStoreTypeIndex(prototype.returnValType)));
            }

            // Set register type to value returned from function (if applies)
            regType.setType(prototype.returnValType.getRegisterType());
            if (!compileDataLookup(false)) {
                return false;
            }

            // If function returns a value larger than the register, temp data
            // will need to be freed.
            if (!prototype.returnValType.canStoreInRegister()) {
                freeTempData = true;
            }
        } else {
            // Indicates no return type
            regType.setType(BasicValType.VTP_VOID);
        }

        return true;
    }

    private boolean compileUserFuncParam(Mutable<UserFuncPrototype> prototype, int i) {

        // Generate code to store result as a function parameter
        ValType type = new ValType(prototype.get().localVarTypes.get(i));

        // Basic type case
        if (type.isBasicType()) {

            // Generate code to compile function parameter
            if (!compileExpression(false)) {
                return false;
            }

            // Attempt to convert value in reg to same type
            // Note: Use full type, so that prototype # is taken into account for function pointer types.
            if (!compileConvert(type)) {
                setError("Types do not match");
                return false;
            }

            // Save reg into parameter
            addInstruction(OpCode.OP_SAVE_PARAM, type.basicType, new Value(i));
        }

        // Pointer case. Parameter must be a pointer and reg must point to a
        // value accessible through a variable.
        else if (type.getVirtualPointerLevel() == 1) {

            // Special case: We accept "null" to pointer parameters
            if (token.getText().equals("null")) {
                if (!compileNull()) {
                    return false;
                }
                addInstruction(OpCode.OP_SAVE_PARAM, BasicValType.VTP_INT, new Value(i));
            } else {

                // Compile expression
                if (!compileExpression(false)) {
                    return false;
                }

                // Implicitly take address if necessary
                if (regType.getVirtualPointerLevel() == 0) {
                    if (!compileTakeAddress()) {
                        return false;
                    }
                }

                // Register should now match the expected type
                if (regType.pointerLevel == type.pointerLevel
                        && regType.arrayLevel == type.arrayLevel
                        && regType.basicType == type.basicType) {
                    addInstruction(OpCode.OP_SAVE_PARAM, BasicValType.VTP_INT, new Value(i));
                } else {
                    setError("Types do not match");
                    return false;
                }
            }
        } else {
            // Not basic, and not a pointer.
            // Must be a large object (structure or array)
            assertTrue(type.pointerLevel == 0);

            // Generate code to compile function parameter
            if (!compileExpression(false)) {
                return false;
            }

            if (regType.pointerLevel == 1
                    && regType.isByRef
                    && regType.arrayLevel == type.arrayLevel
                    && regType.basicType == type.basicType) {
                addInstruction(
                        OpCode.OP_COPY_USER_STACK, BasicValType.VTP_INT, new Value((int) vm.getStoreTypeIndex(type)));
                addInstruction(OpCode.OP_SAVE_PARAM_PTR, BasicValType.VTP_INT, new Value(i));
            } else {
                setError("Types do not match");
                return false;
            }
        }

        // Data containing strings will need to be "destroyed" when the stack
        // unwinds.
        if (vm.getDataTypes().containsString(type)) {
            addInstruction(OpCode.OP_REG_DESTRUCTOR, BasicValType.VTP_INT, new Value((int) vm.getStoreTypeIndex(type)));
        }

        return true;
    }

    private boolean compileReturn() {
        if (!getToken()) {
            return false;
        }

        if (inFunction) {
            if (getCurrentUserFunctionPrototype().hasReturnVal) {
                ValType type = new ValType(getCurrentUserFunctionPrototype().returnValType);

                // Generate code to compile and return value
                if (!compileExpression()) {
                    return false;
                }
                if (!compileConvert(type.getRegisterType())) {
                    return false;
                }

                // Basic values and pointers can be returned in the register
                if (!type.canStoreInRegister()) {

                    // Add instruction to move that data into temp data
                    addInstruction(
                            OpCode.OP_MOVE_TEMP, BasicValType.VTP_INT, new Value((int) vm.getStoreTypeIndex(type)));

                    // Add return-from-function OP-code
                    // Note: The 0 in the instruction value indicates that temp
                    // data should NOT be freed on return (as we have just moved
                    // the return value there.)
                    addInstruction(OpCode.OP_RETURN_USER_FUNC, BasicValType.VTP_INT, new Value(0));
                } else {
                    // Add return-from-function OP-code
                    // Note: The 1 in the instruction value indicates that temp
                    // data should be freed on return.
                    addInstruction(OpCode.OP_RETURN_USER_FUNC, BasicValType.VTP_INT, new Value(1));
                }
            } else {
                addInstruction(OpCode.OP_RETURN_USER_FUNC, BasicValType.VTP_INT, new Value(1));
            }
        } else {
            // Add "return from Gosub" op-code
            addInstruction(OpCode.OP_RETURN, BasicValType.VTP_INT, new Value());
        }

        return true;
    }

    public String getUserFunctionName(int index) {
        String name = userFunctionReverseIndex.get(index);
        return name == null ? "???" : name;
    }

    // State streaming
    public void streamOut(DataOutputStream stream) {
        try {
            // Stream out VM state
            vm.streamOut(stream);

            // Stream out constants
            for (String key : programConstants.keySet()) {
                Streaming.writeString(stream, key);
                programConstants.get(key).streamOut(stream);
            }
            Streaming.writeString(stream, "");

            // Stream out labels
            for (String key : labels.keySet()) {
                Streaming.writeString(stream, key);
                labels.get(key).streamOut(stream);
            }
            Streaming.writeString(stream, "");

            // Stream out user function/subroutine names
            for (String key : globalUserFunctionIndex.keySet()) {
                Streaming.writeString(stream, key);
                Streaming.writeLong(stream, globalUserFunctionIndex.get(key));
            }
            Streaming.writeString(stream, "");

            // Stream out runtime functions
            // Note that strictly speaking these aren't "names", but because
            // they are
            // required when name information is present, and not required when
            // it is
            // absent, we are bundling them into the same #ifdef
            Streaming.writeLong(stream, runtimeFunctions.size());
            for (int i = 0; i < runtimeFunctions.size(); i++) {
                runtimeFunctions.get(i).streamOut(stream);
            }

            // Stream out runtime function names
            for (String key : runtimeFunctionIndex.keySet()) {
                Streaming.writeString(stream, key);
                Streaming.writeLong(stream, runtimeFunctionIndex.get(key));
            }
            Streaming.writeString(stream, "");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean streamIn(DataInputStream stream) {
        try {

            // Unload any plugins
             plugins.clear();

            // Clear current program (if any)
            clearProgram();

            // Stream in VM state
            if (!vm.streamIn(stream)) {
                setError(vm.getError());
                return false;
            }

            // Stream in constant names
            String name = Streaming.readString(stream);
            while (!name.isEmpty()) {

                // Read constant details
                Constant constant = new Constant();
                constant.streamIn(stream);

                // Store constant
                programConstants.put(name, constant);

                // Next constant
                name = Streaming.readString(stream);
            }

            // Stream in label names
            name = Streaming.readString(stream);

            while (!name.isEmpty()) {

                // Read label details
                Label label = new Label();
                label.streamIn(stream);

                // Store label
                labels.put(name, label);
                labelIndex.put(label.offset, name);

                // Next label
                name = Streaming.readString(stream);
            }

            // Stream in user function/subroutine names
            name = Streaming.readString(stream);
            while (!name.isEmpty()) {

                // Read function details
                int index = (int) Streaming.readLong(stream);

                // Store function index
                globalUserFunctionIndex.put(name, index);

                // Next function
                name = Streaming.readString(stream);
            }
            // Stream in runtime functions
            // Note that strictly speaking these aren't "names", but because
            // they are
            // required when name information is present, and not required when
            // it is
            // absent, we are bundling them into the same #ifdef
            int count = (int) Streaming.readLong(stream);
            runtimeFunctions.setSize(count);
            for (int i = 0; i < count; i++) {
                com.basic4gl.compiler.RuntimeFunction function = new com.basic4gl.compiler.RuntimeFunction();
                function.streamIn(stream);

                runtimeFunctions.set(i, function);
            }
            name = Streaming.readString(stream);
            while (!name.isEmpty()) {

                // Read runtime function details
                int index = (int) Streaming.readLong(stream);

                // Store runtime function index
                runtimeFunctionIndex.put(name, index);

                // Next function
                name = Streaming.readString(stream);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
