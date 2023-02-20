package com.basic4gl.compiler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

import com.basic4gl.compiler.Token.TokenType;
import com.basic4gl.compiler.FlowControl.FlowControlType;
import com.basic4gl.compiler.util.*;
import com.basic4gl.lib.util.Library;
import com.basic4gl.runtime.*;
import com.basic4gl.runtime.types.*;
import com.basic4gl.runtime.util.Mutable;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import com.basic4gl.runtime.stackframe.RuntimeFunction;
import com.basic4gl.runtime.stackframe.UserFunc;
import com.basic4gl.runtime.stackframe.UserFuncPrototype;
import com.basic4gl.runtime.util.Function;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Basic4GL v2 language compiler.
 * Used to compile source code in BASIC language to TomVM Op codes.
 */
public class TomBasicCompiler extends HasErrorState {
	static final int TC_STEPSBETWEENREFRESH = 1000;

	/**
	 * Allow 256 functions of the same name
	 * (should be more than enough for anything...)
	 */
	static final int TC_MAXOVERLOADEDFUNCTIONS = 256;

	// Virtual machine
	private final TomVM vm;

	// Parser
	private final Parser parser;

	// DLL manager
	// TODO Reimplement libraries
	// PluginDLLManager m_plugins;

	// Settings
	private final boolean isCaseSensitive;
	private final Map<String, Operator> unaryOperators; // Recognised operators. Unary have one operand (e.g NOT x)
	private final Map<String, Operator> binaryOperators; // Binary have to (e.g. x + y)
	private final ArrayList<String> reservedWords;
	private final Map<String, Constant> constants; // Permanent constants.
	private final Map<String, Constant> programConstants; // Constants declared using the const command.

	private final Vector<Library> libraries = new Vector<Library>();
	private final Vector<FuncSpec> functions;
	private final Map<String, List<Integer>> functionIndex; // Maps function name to index of function (in mFunctions array)
	private LanguageSyntax syntax;
	private String symbolPrefix = ""; // Prefix all symbols with this text

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
	private final Vector<Jump> jumps; // Jumps to fix up
	private final Vector<Jump> resets; // Resets to fix up
	private final Vector<FlowControl> flowControls; // Flow control structure stack
	private Token token;
	private boolean needColon; // True if next instruction must be separated by a
	// colon (or newline)
	private boolean freeTempData; // True if need to generate code to free temporary
	// data before the next instruction
	private int lastLine, lastCol;
	private boolean inFunction;
	private final InstructionPos functionStart;
	private int functionJumpOver;
	private final Map<String, Integer> globalUserFunctionIndex; // Maps function name to
	// index of function
	private final Map<String, Integer> localUserFunctionIndex; // Local user function index
	// (for the current code
	// block being compiled)
	private Map<String, Integer> visibleUserFunctionIndex; // Combines local and
	// global (where a local
	// function overrides a
	// global one of the
	// same name)
	private final Map<Integer, String> userFunctionReverseIndex; // Index->Name lookup. For
	// debug views.
	private int currentFunction; // Index of current active user function. Usually
	// this will be the last in the vm.UserFunctions()
	// vector,
	// can be different in special cases (e.g. when compiler is called from
	// debugger to evaluate an expression).
	private UserFuncPrototype userFuncPrototype; // Prototype of function being
	// declared.
	private final Vector<com.basic4gl.compiler.RuntimeFunction> runtimeFunctions;
	private final Map<String, Integer> runtimeFunctionIndex;

	// Language extension
	private final Vector<UnOperExt> unOperExts; // Unary operator extensions
	private final Vector<BinOperExt> binOperExts; // Binary operator extensions

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
		OperType type;
		short opCode;
		/**
		 * 1 . Calculate "op Reg" (e.g. "Not Reg")
		 * 2 . Calculate "Reg2 op Reg" (e.g. "Reg2 - Reg")
		 */
		int params;

		/**
		 * Operator binding. Higher = tighter.
		 */
		int binding;

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
	}

	static class StackedOperator {
		/**
		 * Stacked operator
		 */
		Operator operator;
		
		/**
		 * Address of lazy jump op code (for "and" and "or" operations)
		 */
		int lazyJumpAddress;

		StackedOperator(Operator o) {
			operator = o;
			lazyJumpAddress = -1;
		}

		StackedOperator(Operator o, int lazyJumpAddr) {
			operator = o;
			lazyJumpAddress = lazyJumpAddr;
		}
	}

	/**
	 * A program label, i.e. a named destination for "goto" and "gosub"s
	 */
	static class Label implements Streamable {
		/**
		 * Instruction index in code
		 */
		int offset;

		/**
		 * Program data offset. (For use with "RESET labelname" command.)
		 */
		int programDataOffset;

		Label(int offset, int dataOffset) {
			this.offset = offset;
			programDataOffset = dataOffset;
		}

		Label() {
			offset = 0;
			programDataOffset = 0;
		}

		public void streamOut(DataOutputStream stream) throws IOException{
			Streaming.WriteLong(stream, offset);
			Streaming.WriteLong(stream, programDataOffset);

		}

		public boolean streamIn(DataInputStream stream) throws IOException{
			offset = (int) Streaming.ReadLong(stream);
			programDataOffset = (int) Streaming.ReadLong(stream);

			return true;
		}
	}

	/**
	 * Used to track program jumps. Actual addresses are patched into jump
	 * instructions after the main compilation pass has completed. (Thus forward
	 * jumps are possible.)
	 */
	static class Jump {
		/**
		 * Instruction containing jump instruction
		 */
		int jumpInstruction;
		/**
		 * Label to which we are jumping
		 */
		String labelName;

		Jump(int instruction, String labelName) {
			jumpInstruction = instruction;
			this.labelName = labelName;
		}

		Jump() {
			jumpInstruction = 0;
			labelName = "";
		}
	}

	// Misc
	static class ParserPos {
		int line;
		int column;
		Token token;
	}

	public enum LanguageSyntax {
		LS_TRADITIONAL(0), // As compatible as possible with other BASICs
		LS_BASIC4GL(1), // Standard Basic4GL syntax for backwards compatibility
		// with existing code.
		LS_TRADITIONAL_PRINT(2); // Traditional mode PRINT, but otherwise
		// standard Basic4GL syntax
		private int type;

		LanguageSyntax(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}
	}

	enum UserFunctionType {
		UFT_IMPLEMENTATION,
		UFT_FWDDECLARATION,
		UFT_RUNTIMEDECLARATION
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

		/**
		 * Virtual machine rollback
 		 */
		com.basic4gl.runtime.RollbackPoint vmRollback;

		/**
		 * Runtime functions
		 */
		int runtimeFunctionCount;
	}

	// TODO Reimplement libraries
	// public TomBasicCompiler(TomVM vm, PluginDLLManager plugins) {
	// this(vm, plugins, false);
	// }
	public TomBasicCompiler(TomVM vm) {
		this(vm, false);
	}

	// TODO Reimplement libraries
	// public TomBasicCompiler(TomVM vm, PluginDLLManager plugins,
	// boolean caseSensitive) {
	public TomBasicCompiler(TomVM vm, boolean caseSensitive) {
		this.vm = vm;
		// TODO Reimplement libraries
		// m_plugins = plugins;
		isCaseSensitive = caseSensitive;
		syntax = LanguageSyntax.LS_BASIC4GL;

		operandStack = new Vector<ValType>();
		operatorStack = new Vector<StackedOperator>();
		jumps = new Vector<Jump>();
		resets = new Vector<Jump>();
		flowControls = new Vector<FlowControl>();

		binaryOperators = new HashMap<String, Operator>();
		unaryOperators = new HashMap<String, Operator>();

		reservedWords = new ArrayList<String>();

		parser = new Parser();

		programConstants = new HashMap<String, Constant>();
		labels = new HashMap<String, Label>();
		labelIndex = new HashMap<Integer, String>();

		functionIndex = new HashMap<String, List<Integer>>();
		constants = new HashMap<String, Constant>();

		localUserFunctionIndex = new HashMap<String, Integer>();
		globalUserFunctionIndex = new HashMap<String, Integer>();
		visibleUserFunctionIndex = new HashMap<String, Integer>();
		userFunctionReverseIndex = new HashMap<Integer, String>();
		runtimeFunctionIndex = new HashMap<String, Integer>();
		runtimeFunctions = new Vector<com.basic4gl.compiler.RuntimeFunction>();
		functions = new Vector<FuncSpec>();

		unOperExts = new Vector<UnOperExt>();
		binOperExts = new Vector<BinOperExt>();

		functionStart = new InstructionPos();
		userFuncPrototype = new UserFuncPrototype();

		clearState();

		// Setup operators
		// Note: From experimentation it appears QBasic binds "xor" looser than
		// "and" and "or". So for compatibility, we will too..
		binaryOperators.put("xor", new Operator(
				OperType.OT_BOOLOPERATOR, OpCode.OP_OP_XOR, 2, 10));
		binaryOperators.put("or", new Operator(
				OperType.OT_BOOLOPERATOR, OpCode.OP_OP_OR, 2, 11));
		binaryOperators.put("and", new Operator(
				OperType.OT_BOOLOPERATOR, OpCode.OP_OP_AND, 2, 12));
		binaryOperators.put("lor", new Operator(
				OperType.OT_LAZYBOOLOPERATOR, OpCode.OP_OP_OR, 2, 11));
		binaryOperators.put("land", new Operator(
				OperType.OT_LAZYBOOLOPERATOR, OpCode.OP_OP_AND, 2, 12));
		unaryOperators.put("not", new Operator(
				OperType.OT_BOOLOPERATOR, OpCode.OP_OP_NOT, 1, 20));
		binaryOperators.put("=", new Operator(
				OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_EQUAL, 2, 30));
		binaryOperators.put("<>", new Operator(
				OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_NOT_EQUAL, 2,
				30));
		binaryOperators.put(">",
				new Operator(OperType.OT_RETURNBOOLOPERATOR,
						OpCode.OP_OP_GREATER, 2, 30));
		binaryOperators.put(">=", new Operator(
				OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_GREATER_EQUAL,
				2, 30));
		binaryOperators.put("<", new Operator(
				OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_LESS, 2, 30));
		binaryOperators.put("<=", new Operator(
				OperType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_LESS_EQUAL, 2,
				30));
		binaryOperators.put("+", new Operator(OperType.OT_OPERATOR,
				OpCode.OP_OP_PLUS, 2, 40));
		binaryOperators.put("-", new Operator(OperType.OT_OPERATOR,
				OpCode.OP_OP_MINUS, 2, 40));
		binaryOperators.put("*", new Operator(OperType.OT_OPERATOR,
				OpCode.OP_OP_TIMES, 2, 41));
		binaryOperators.put("/", new Operator(OperType.OT_OPERATOR,
				OpCode.OP_OP_DIV, 2, 42));
		binaryOperators.put("%", new Operator(OperType.OT_OPERATOR,
				OpCode.OP_OP_MOD, 2, 43));
		unaryOperators.put("-", new Operator(OperType.OT_OPERATOR,
				OpCode.OP_OP_NEG, 1, 50));

		// Setup reserved words
		reservedWords.add( "dim");
		reservedWords.add( "goto");
		reservedWords.add( "if");
		reservedWords.add( "then");
		reservedWords.add( "elseif");
		reservedWords.add( "else");
		reservedWords.add( "endif");
		reservedWords.add( "end");
		reservedWords.add( "gosub");
		reservedWords.add( "return");
		reservedWords.add( "for");
		reservedWords.add( "to");
		reservedWords.add( "step");
		reservedWords.add( "next");
		reservedWords.add( "while");
		reservedWords.add( "wend");
		reservedWords.add( "run");
		reservedWords.add( "struc");
		reservedWords.add( "endstruc");
		reservedWords.add( "const");
		reservedWords.add( "alloc");
		reservedWords.add( "null");
		reservedWords.add( "data");
		reservedWords.add( "read");
		reservedWords.add( "reset");
		reservedWords.add( "type"); // QBasic/Freebasic compatibility
		reservedWords.add( "as"); //
		reservedWords.add( "integer"); //
		reservedWords.add( "single"); //
		reservedWords.add( "double"); //
		reservedWords.add( "string"); //
		reservedWords.add( "language"); // Language syntax
		reservedWords.add( "traditional");
		reservedWords.add( "basic4gl");
		reservedWords.add( "traditional_print");
		reservedWords.add( "input");
		reservedWords.add( "do");
		reservedWords.add( "loop");
		reservedWords.add( "until");
		reservedWords.add( "function");
		reservedWords.add( "sub");
		reservedWords.add( "endfunction");
		reservedWords.add( "endsub");
		reservedWords.add( "declare");
		reservedWords.add( "runtime");
		reservedWords.add( "bindcode");
		reservedWords.add( "exec");
	}

	public List<String> getUnaryOperators(){
		return new ArrayList<String>(unaryOperators.keySet());
	}
	public List<String> getBinaryOperators(){
		return new ArrayList<String>(binaryOperators.keySet());
	}

	void clearState() {
		regType = new ValType (BasicValType.VTP_INT);
		reg2Type = new ValType (BasicValType.VTP_INT);
		freeTempData = false;
		operandStack.clear();
		operatorStack.clear();
		jumps.clear();
		resets.clear();
		flowControls.clear();
		syntax = LanguageSyntax.LS_BASIC4GL;
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
		// TODO Reimplement libraries
		// InitPlugins();
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

	boolean checkParser() {
		// Check parser for error
		// Copy error state (if any)
		if (parser.hasError()) {
			setError("Parse error: " + parser.getError());
			parser.clearError();
			return false;
		}
		return true;
	}

	// TODO Reimplement libraries
	/*
	 * void InitPlugins() {
	 * m_plugins.StructureManager().AddVMStructures(mVM.DataTypes());
	 * m_plugins.CreateVMFunctionSpecs(); }
	 */

	boolean GetToken() {
		return GetToken(false, false);
	}

	boolean GetToken(boolean skipEOL, boolean dataMode) {

		// Read a token
		token = parser.NextToken(skipEOL, dataMode);
		if (!checkParser()) {
			return false;
		}

		// Text token processing
		if (token.tokenType == TokenType.CTT_TEXT) {

			// Apply case sensitivity
			if (!isCaseSensitive) {
				token.text = token.text.toLowerCase();
			}

			// Match against reserved keywords
			if (reservedWords.contains(token.text)) {
				token.tokenType = TokenType.CTT_KEYWORD;
			}

			// Match against external functions
			else if (IsFunction(token.text)) {
				token.tokenType = TokenType.CTT_FUNCTION;
			} else if (isUserFunction(token.text)) {
				token.tokenType = TokenType.CTT_USER_FUNCTION;
			} else if (isRuntimeFunction(token.text)) {
				token.tokenType = TokenType.CTT_RUNTIME_FUNCTION;
			} else {

				// Match against constants

				// Try permanent constants first
				Constant constant;
				boolean isConstant;
				constant = constants.get(token.text);
				isConstant = (constant != null);

				// Try plugin DLL constants next
				// TODO Reimplement libraries
				// if (!isConstant)
				// isConstant = m_plugins.FindConstant(m_token.m_text,
				// constant);

				// Otherwise try program constants
				if (!isConstant) {
					constant = programConstants.get(token.text);
					isConstant = (constant != null);
				}

				if (isConstant) {
					// Replace token with constant
					token.tokenType = TokenType.CTT_CONSTANT;

					// Replace text with text value of constant
					switch (constant.mBasicType) {
					case BasicValType.VTP_INT:
						token.text = String.valueOf(constant.mIntVal);
						break;
					case BasicValType.VTP_REAL:
						token.text = String.valueOf(constant.mRealVal);
						break;
					case BasicValType.VTP_STRING:
						token.text = constant.mStringVal;
						break;
					default:
						break;
					}
					token.m_valType = constant.mBasicType;
				}
			}
		} else if (token.tokenType == TokenType.CTT_CONSTANT
				&& token.m_valType == BasicValType.VTP_STRING) {

			// 19-Jul-2003
			// Prefix string text constants with "S". This prevents them
			// matching
			// any recognised keywords (which are stored in lower case).
			// (This is basically a work around to existing code which doesn't
			// check the token type if it matches a reserved keyword).
			token.text = (String) "S" + token.text;
		}
		return true;
	}

	void internalCompile() {

		// Allocate a new code block
		vm.NewCodeBlock();

		// Clear error state
		clearError();
		parser.clearError();

		// Read initial token
		if (!GetToken(true, false)) {
			return;
		}

		// Compile code
		while (!parser.Eof() && compileInstruction()) {
		}

		// Terminate program
		AddInstruction(OpCode.OP_END, BasicValType.VTP_INT, new Value());

		if (!hasError()) {
			// Link up gotos
			for (Jump jump : jumps) {

				// Find instruction
				assertTrue(jump.jumpInstruction < vm.getInstructionCount());
				Instruction instr = vm.getInstruction(jump.jumpInstruction);

				// Point token to goto instruction, so that it will be displayed
				// if there is an error.
				token.line = instr.mSourceLine;
				token.col = instr.mSourceChar;

				// Label must exist
				if (!labelExists(jump.labelName)) {
					setError("Label: " + jump.labelName + " does not exist");
					return;
				}

				// Patch in offset
				instr.mValue.setIntVal(getLabel(jump.labelName).offset);
			}

			// Link up resets
			for (Jump jump : resets) {

				// Find instruction
				assertTrue(jump.jumpInstruction < vm.getInstructionCount());
				Instruction instr = vm.getInstruction(jump.jumpInstruction);

				// Point token to reset instruction, so that it will be
				// displayed
				// if there is an error.
				token.line = instr.mSourceLine;
				token.col = instr.mSourceChar;

				// Label must exist
				if (!labelExists(jump.labelName)) {
					setError("Label: " + jump.labelName + " does not exist");
					return;
				}

				// Patch in data offset
				instr.mValue
				.setIntVal(getLabel(jump.labelName).programDataOffset);
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
		}
	}

	boolean NeedAutoEndif() {

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

	public boolean IsFunction(String name) {
		// TODO Reimplement libraries
		return isBuiltinFunction(name);// || m_plugins.IsPluginFunction(name);
	}

	public TomVM VM() {
		return vm;
	}

	public Parser Parser() {
		return parser;
	}

	public boolean CaseSensitive() {
		return isCaseSensitive;
	}

	// TODO Reimplement libraries
	// public PluginDLLManager Plugins() {
	// return m_plugins;
	// }

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
		// Multimap<String,Integer>.iterator i = m_functionIndex.find
		// (LowerCase (name));
		// return i != m_functionIndex.lastElement() && i.first == LowerCase
		// (name);
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

	public Vector<Library> getLibraries() { return libraries;}
	public Vector<FuncSpec> getFunctions() {
		return functions;
	}

	public Map<String, List<Integer>> getFunctionIndex() {
		return functionIndex;
	}

	// Language extension
	public void addUnOperExtension(UnOperExt e) {
		unOperExts.add(e);
	}

	public void addBinOperExtension(BinOperExt e) {
		binOperExts.add(e);
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

		// Check DLL constants
		// TODO Reimplement libraries
		// Constant compConst = new Constant();
		// return (m_plugins.FindConstant(text, compConst));
		return false; // Remove line after libraries are reimplemented
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
		return token.line;
	}

	public long getTokenColumn() {
		return token.col;
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
		assertTrue(vm.getUserFunctions().size() > 0);
		assertTrue(currentFunction >= 0);
		assertTrue(currentFunction < vm.getUserFunctions().size());
		return vm.getUserFunctions().get(currentFunction);
	}

	UserFuncPrototype getCurrentUserFunctionPrototype() {
		// Return prototype of function currently being declared
		assertTrue(vm.getUserFunctionPrototypes().size() > 0);
		assertTrue(getCurrentUserFunction().mPrototypeIndex >= 0);
		assertTrue(getCurrentUserFunction().mPrototypeIndex < vm.getUserFunctionPrototypes()
				.size());
		return vm.getUserFunctionPrototypes().get(getCurrentUserFunction().mPrototypeIndex);
	}

	private boolean compileBindCodeInternal() {

		// Evaluate code handle
		if (!compileExpression()) {
			return false;
		}

		if (!compileConvert (BasicValType.VTP_INT)) {
			return false;
		}

		// Add bindcode op-code
		AddInstruction(OpCode.OP_BINDCODE, BasicValType.VTP_INT,
				new Value());

		return true;
	}

	private boolean compileBindCode() {

		// Skip "bindcode" keyword
		if (!GetToken()) {
			return false;
		}

		// Combine bind code
		return compileBindCodeInternal();
	}

	private boolean compileExec() {

		// Skip "exec" keyword
		if (!GetToken()) {
			return false;
		}

		// Check if explicit code block specified
		if (!AtSeparatorOrSpecial()) {
			if (!compileBindCodeInternal()) {
				return false;
			}
		}

		// Add exec op-code
		AddInstruction(OpCode.OP_EXEC, BasicValType.VTP_INT, new Value());

		return true;
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
		for(Iterator<Map.Entry<String, Label>> it = labels.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Label> entry = it.next();
			if (entry.getValue().offset >= rollbackPoint.vmRollback.instructionCount){
				it.remove();
			}
		}


		// Remove global function name->index records
		// (Can detect them as any global function with an invalid function index)
		for(Iterator<Map.Entry<String, Integer>> it = globalUserFunctionIndex.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Integer> entry = it.next();
			if (entry.getValue() >= rollbackPoint.vmRollback.functionCount){
				it.remove();
			}
		}


		// Remove function index->name records (used for debugging)
		for(Iterator<Map.Entry<Integer, String>> it = userFunctionReverseIndex.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Integer, String> entry = it.next();
			if (entry.getKey() >= rollbackPoint.vmRollback.functionCount){
				it.remove();
			}
		}

		// Remove runtime functions
		runtimeFunctions.setSize(rollbackPoint.runtimeFunctionCount);

		for(Iterator<Map.Entry<String, Integer>> it = runtimeFunctionIndex.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Integer> entry = it.next();
			if (entry.getValue() >= rollbackPoint.runtimeFunctionCount){
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
			parser.SetPos(top.m_sourcePos.getSourceLine(),
					top.m_sourcePos.getSourceColumn());

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
			parser.SetPos(functionStart.getSourceLine(),
					functionStart.getSourceColumn());

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
			if (!vm.getUserFunctions().get(localUserFunctionIndex.get(key)).mImplemented) {
				setError((String) "Function/sub '" + key
						+ "' was DECLAREd, but not implemented");
				return false;
			}
		}

		return true;
	}

	// Compilation
	void AddInstruction(short opCode, int basictype, Value val) {

		// Add instruction, and include source code position audit
		int line = token.line, col = token.col;

		// Prevent line and col going backwards. (Debugging tools rely on
		// source-
		// offsets never decreasing as source code is traversed.)
		if (line < lastLine || (line == lastLine && col < lastCol)) {
			line = lastLine;
			col = lastCol;
		}
		vm.addInstruction(new Instruction(opCode, basictype, val, line, col));
		lastLine = line;
		lastCol = col;
	}

	private boolean compileInstruction() {
		needColon = true; // Instructions by default must be separated by
		// colons

		// Is it a label?
		Token nextToken = parser.PeekToken(false, false);
		if (!checkParser()) {
			return false;
		}
		if (token.m_newLine && token.tokenType == TokenType.CTT_TEXT
				&& nextToken.text.equals(":")) {

			// Labels cannot exist inside subs/functions
			if (inFunction) {
				setError((String) "You cannot use a label inside a function or subroutine");
				return false;
			}

			// Label declaration
			String labelName = symbolPrefix + token.text;

			// Must not already exist
			if (labelExists(labelName)) {
				setError("Duplicate label name: " + labelName);
				return false;
			}

			// Create new label
			addLabel(labelName, new Label(vm.getInstructionCount(), vm
					.getProgramData().size()));

			// Skip label
			if (!GetToken()) {
				return false;
			}
		}
		// Determine the type of instruction, based on the current token
		else if (token.text.equals("struc")
				|| token.text.equals("type")) {
			if (!compileStructure()) {
				return false;
			}
		} else if (token.text.equals("dim")) {
			if (!compileDim(false, false)) {
				return false;
			}
		} else if (token.text.equals("goto")) {
			if (!GetToken()) {
				return false;
			}
			if (!compileGoto(OpCode.OP_JUMP)) {
				return false;
			}
		} else if (token.text.equals("gosub")) {
			if (!GetToken()) {
				return false;
			}
			if (!compileGoto(OpCode.OP_CALL)) {
				return false;
			}
		} else if (token.text.equals("return")) {
			if (!compileReturn()) {
				return false;
			}
		} else if (token.text.equals("if")) {
			if (!compileIf(false)) {
				return false;
			}
		} else if (token.text.equals("elseif")) {
			if (!(compileElse(true) && compileIf(true))) {
				return false;
			}
		} else if (token.text.equals("else")) {
			if (!compileElse(false)) {
				return false;
			}
		} else if (token.text.equals("endif")) {
			if (!compileEndIf(false)) {
				return false;
			}
		} else if (token.text.equals("for")) {
			if (!compileFor()) {
				return false;
			}
		} else if (token.text.equals("next")) {
			if (!compileNext()) {
				return false;
			}
		} else if (token.text.equals("while")) {
			if (!compileWhile()) {
				return false;
			}
		} else if (token.text.equals("wend")) {
			if (!compileWend()) {
				return false;
			}
		} else if (token.text.equals("do")) {
			if (!compileDo()) {
				return false;
			}
		} else if (token.text.equals("loop")) {
			if (!compileLoop()) {
				return false;
			}
		} else if (token.text.equals("end")) {
			if (!GetToken()) {
				return false;
			}

			// Special case! "End" immediately followed by "if" is syntactically
			// equivalent to "endif"
			if (token.text.equals("if")) {
				if (!compileEndIf(false)) {
					return false;
				}
			}
			// Special case! "End" immediately followed by "function" is
			// syntactically equivalent to "endfunction"
			else if (token.text.equals("function")) {
				if (!compileEndUserFunction(true)) {
					return false;
				}
			} else if (token.text.equals("sub")) {
				if (!compileEndUserFunction(false)) {
					return false;
				}
			} else
				// Otherwise is "End" program instruction
			{
				AddInstruction(OpCode.OP_END, BasicValType.VTP_INT,
						new Value());
			}
		} else if (token.text.equals("run")) {
			if (!GetToken()) {
				return false;
			}
			AddInstruction(OpCode.OP_RUN, BasicValType.VTP_INT, new Value());
		} else if (token.text.equals("const")) {
			if (!compileConstant()) {
				return false;
			}
		} else if (token.text.equals("alloc")) {
			if (!compileAlloc()) {
				return false;
			}
		} else if (token.text.equals("data")) {
			if (!compileData()) {
				return false;
			}
		} else if (token.text.equals("read")) {
			if (!compileDataRead()) {
				return false;
			}
		} else if (token.text.equals("reset")) {
			if (!compileDataReset()) {
				return false;
			}
		} else if (token.text.equals("print")) {
			if (!compilePrint(false)) {
				return false;
			}
		} else if (token.text.equals("printr")) {
			if (!compilePrint(true)) {
				return false;
			}
		} else if (token.text.equals("input")) {
			if (!compileInput()) {
				return false;
			}
		} else if (token.text.equals("language")) {
			if (!compileLanguage()) {
				return false;
			}
		} else if (token.text.equals("function")
				|| token.text.equals("sub")) {
			if (!compileUserFunction(UserFunctionType.UFT_IMPLEMENTATION)) {
				return false;
			}
		} else if (token.text.equals("endfunction")) {
			if (!compileEndUserFunction(true)) {
				return false;
			}
		} else if (token.text.equals("endsub")) {
			if (!compileEndUserFunction(false)) {
				return false;
			}
		} else if (token.text.equals("declare")) {
			if (!compileUserFunctionFwdDecl()) {
				return false;
			}
		} else if (token.text.equals("runtime")) {
			if (!compileUserFunctionRuntimeDecl()) {
				return false;
			}
		} else if (token.text.equals("bindcode")) {
			if (!compileBindCode()) {
				return false;
			}
		} else if (token.text.equals("exec")) {
			if (!compileExec()) {
				return false;
			}

		} else if (token.tokenType == TokenType.CTT_FUNCTION) {
			if (!compileFunction()) {
				return false;
			}
		} else if (token.tokenType == TokenType.CTT_USER_FUNCTION) {
			if (!compileUserFunctionCall(false, false)) {
				return false;
			}
		} else if (token.tokenType == TokenType.CTT_RUNTIME_FUNCTION) {
			if (!compileUserFunctionCall(false, true)) {
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
		if (!SkipSeparators()) {
			return false;
		}

		return true;
	}

	boolean AtSeparator() {
		return token.tokenType == TokenType.CTT_EOL
				|| token.tokenType == TokenType.CTT_EOF
				|| token.text.equals(":");
	}

	boolean AtSeparatorOrSpecial() {

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
		return AtSeparator() || token.text.equals("endif")
				|| token.text.equals("else")
				|| token.text.equals("elseif");
	}

	boolean SkipSeparators() {

		// Expect separator. Either as an EOL or EOF or ':'
		if (needColon && !AtSeparatorOrSpecial()) {
			setError("Expected ':'");
			return false;
		}

		// Skip separators
		while (token.tokenType == TokenType.CTT_EOL
				|| token.tokenType == TokenType.CTT_EOF
				|| token.text.equals(":")) {

			// If we reach the end of the line, insert any necessary implicit
			// "endifs"
			if (token.tokenType == TokenType.CTT_EOL
					|| token.tokenType == TokenType.CTT_EOF) {

				// Generate any automatic endifs for the previous line
				while (NeedAutoEndif()) {
					if (!compileEndIf(true)) {
						return false;
					}
				}

				// Convert any remaining flow control structures to block
				for (FlowControl flowControl : flowControls) {
					flowControl.blockIf = true;
				}
			}

			if (!GetToken(true, false)) {
				return false;
			}
		}

		return true;
	}

	private boolean compileStructure() {

		// Skip STRUC
		String keyword = token.text; // Record whether "struc" or "type" was
		// used to declare type
		if (!GetToken()) {
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
		if (token.tokenType != TokenType.CTT_TEXT) {
			setError("Expected structure name");
			return false;
		}
		String name = symbolPrefix + token.text;
		if (!checkName(name)) {
			return false;
		}
		if (vm.getDataTypes().StrucStored(name)) { // Must be unused
			setError("'" + name + "' has already been used as a structure name");
			return false;
		}
		if (!GetToken()) // Skip structure name
		{
			return false;
		}

		if (!SkipSeparators()) // Require : or new line
		{
			return false;
		}

		// Create structure
		vm.getDataTypes().NewStruc(name);

		// Expect at least one field
		if (token.text.equals("endstruc") || token.text.equals("end")) {
			setError("Expected DIM or field name");
			return false;
		}

		// Populate with fields
		while (!token.text.equals("endstruc")
				&& !token.text.equals("end")) {

			// dim statement is optional
			/*
			 * if (m_token.m_text != "dim") { setError
			 * ("Expected 'dim' or 'endstruc'"); return false; }
			 */
			if (!compileDim(true, false)) {
				return false;
			}

			if (!SkipSeparators()) {
				return false;
			}
		}

		if (token.text.equals("end")) {

			// Skip END
			if (!GetToken()) {
				return false;
			}

			// Check END keyword matches declaration keyword
			if (!token.text.equals(keyword)) {
				setError("Expected '" + keyword + "'");
				return false;
			}

			// Skip STRUC/TYPE
			if (!GetToken()) {
				return false;
			}
		} else {

			// Make sure "struc" was declared
			if (!keyword.equals("struc")) {
				setError("Expected 'END '" + keyword + "'");
				return false;
			}

			// Skip ENDSTRUC
			if (!GetToken()) {
				return false;
			}
		}
		return true;
	}

	private boolean compileDim(boolean forStruc, boolean forFuncParam) {

		// Skip optional DIM
		if (token.text.equals("dim")) {
			if (!GetToken()) {
				return false;
			}
		}

		// Expect at least one field in dim
		if (AtSeparatorOrSpecial()) {
			setError("Expected variable declaration");
			return false;
		}

		// Parse fields in dim
		boolean needComma = false; // First element doesn't need a comma
		while (!AtSeparatorOrSpecial()
				&& (!forFuncParam || !token.text.equals(")"))) {

			// Handle commas
			if (needComma) {
				if (!token.text.equals(",")) {
					setError("Expected ','");
					return false;
				}
				if (!GetToken()) {
					return false;
				}
			}
			needComma = true; // Remaining elements do need commas

			// Extract field type
			String name = "";
			ValType type = new ValType();
			// Create wrappers to pass values by reference
			Mutable<String> nameRef = new Mutable<String>(name);
			Mutable<ValType> typeRef = new Mutable<ValType>(type);
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
				Structure struc = vm.getDataTypes().CurrentStruc();
				if (vm.getDataTypes().FieldStored(struc, name)) {
					setError((String) "Field '" + name
							+ "' has already been DIMmed in structure '"
							+ struc.m_name + "'");
					return false;
				}
				if (type.m_pointerLevel == 0
						&& type.m_basicType == vm.getDataTypes()
						.GetStruc(struc.m_name)) {
					setError("Structure cannot contain an element of its own type");
					return false;
				}

				// Add field to structure
				vm.getDataTypes().NewField(name, type);
			} else if (forFuncParam) {

				// Check parameter of the same name has not already been added
				int varIndex = userFuncPrototype.getLocalVar(name);
				if (varIndex >= 0) {
					setError("There is already a function parameter called '"
							+ name + "'");
					return false;
				}

				// Add parameter type to function definition
				userFuncPrototype.newParam(name, type);

			} else {

				// Regular DIM.
				// Prefix DIMmed variable names
				name = symbolPrefix + name;

				if (inFunction) {

					// Local variable

					// Check if variable has already been DIMmed locally.
					// (This is allowed, but only if DIMmed to the same type.)
					int varIndex = getCurrentUserFunctionPrototype().getLocalVar(name);
					if (varIndex >= 0) {
						if (!(getCurrentUserFunctionPrototype().localVarTypes.get(varIndex).Equals(type))) {
							setError("Local variable '"
									+ name
									+ "' has already been allocated as a different type.");
							return false;
						}

						// Var already created (earlier), so fall through
						// to
						// allocation code generation
					} else
						// Create new variable
					{
						varIndex = getCurrentUserFunctionPrototype().newLocalVar(name, type);
					}

					// Generate code to allocate local variable data
					// Note: Opcode contains the index of the variable. Var
					// type and size data stored in the user function defn.
					AddInstruction(OpCode.OP_DECLARE_LOCAL,
							BasicValType.VTP_INT, new Value(varIndex));

					// Data containing strings will need to be "destroyed" when
					// the stack unwinds.
					if (vm.getDataTypes().ContainsString(type)) {
						AddInstruction(OpCode.OP_REG_DESTRUCTOR,
								BasicValType.VTP_INT,
								new Value((int) vm.getStoreTypeIndex(type)));
					}

					// Optional "= value"?
					if (token.text.equals("=")) {

						// Add opcode to load variable address
						AddInstruction(OpCode.OP_LOAD_LOCAL_VAR,
								BasicValType.VTP_INT, new Value(varIndex));

						// Set register type
						regType.Set(getCurrentUserFunctionPrototype().localVarTypes
								.get(varIndex));
						regType.m_pointerLevel++;

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
						if (!InternalCompileAssignment()) {
							return false;
						}
					}

				} else {

					// Check if variable has already been DIMmed. (This is
					// allowed, but
					// only if DIMed to the same type.)
					int varIndex = vm.getVariables().getVariableIndex(name);
					if (varIndex >= 0) {
						if (!(vm.getVariables().getVariables().get(varIndex).m_type.Equals(type))) {
							setError("Var '"
									+ name
									+ "' has already been allocated as a different type.");
							return false;
						}

						// Var already created (earlier), so fall through
						// to
						// allocation code generation.
					} else
						// Create new variable
					{
						varIndex = vm.getVariables().createVar(name, type);
					}

					// Generate code to allocate variable data
					// Note: Opcode contains the index of the variable. Var
					// type
					// and size data is stored in the variable entry.
					AddInstruction(OpCode.OP_DECLARE, BasicValType.VTP_INT,
							new Value(varIndex));

					// Optional "= value"?
					if (token.text.equals("=")) {

						// Add opcode to load variable address
						AddInstruction(OpCode.OP_LOAD_VAR,
								BasicValType.VTP_INT, new Value(varIndex));

						// Set register type
						regType.Set(vm.getVariables().getVariables()
								.get(varIndex).m_type);
						regType.m_pointerLevel++;

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
						if (!InternalCompileAssignment()) {
							return false;
						}
					}

				}

				// If this was an array and not a pointer, then its array
				// indices
				// will have been pushed to the stack.
				// The DECLARE operator automatically removes them however
				if (type.PhysicalPointerLevel() == 0) {
					for (int i = 0; i < type.m_arrayLevel; i++) {
						operandStack.remove(operandStack.size() - 1);
					}
				}
			}
		}
		return true;
	}

	private boolean compileTokenName(Mutable<String> name,
			Mutable<TokenType> tokenType, boolean allowSuffix) {
		tokenType.set(token.tokenType);
		name.set(token.text);

		if (tokenType.get() != TokenType.CTT_TEXT
				&& tokenType.get() != TokenType.CTT_USER_FUNCTION
				&& tokenType.get() != TokenType.CTT_RUNTIME_FUNCTION) {
			setError("Expected name");
			return false;
		}

		if (!allowSuffix) {
			char last = name.get().charAt(name.get().length() - 1);
			if (last == '#' || last == '%' || last == '$') {
				setError("Subroutine names cannot end with: " + last);
				return false;
			}
		}

		if (!GetToken()) {
			return false;
		}

		return true;
	}

	private boolean compileDataType(Mutable<String> name, Mutable<ValType> type,
			Mutable<TokenType> tokenType) {

		type.get().Set(BasicValType.VTP_UNDEFINED);
		name.set("");

		// Look for structure type
		if (token.tokenType == TokenType.CTT_TEXT) {
			String structureName = symbolPrefix + token.text;
			int i = vm.getDataTypes().GetStruc(structureName);
			if (i >= 0) {
				type.get().m_basicType = i;
				if (!GetToken()) // Skip token type keyword
				{
					return false;
				}
			}
		}

		// Look for preceeding & (indicates pointer)
		if (token.text.equals("&")) {
			type.get().m_pointerLevel++;
			if (!GetToken()) {
				return false;
			}
		}

		// Look for variable name
		if (!compileTokenName(name, tokenType, true)) {
			return false;
		}

		// Determine variable type
		char last = '\0';
		if (name.get().length() > 0) {
			last = name.get().charAt(name.get().length() - 1);
		}
		if (type.get().m_basicType == BasicValType.VTP_UNDEFINED) {
			if (last == '$') {
				type.get().m_basicType = BasicValType.VTP_STRING;
			} else if (last == '#') {
				type.get().m_basicType = BasicValType.VTP_REAL;
			} else if (last == '%') {
				type.get().m_basicType = BasicValType.VTP_INT;
			}
		} else {
			if (last == '$' || last == '#' || last == '%') {
				setError((String) "\""
						+ name
						+ "\" is a structure variable, and cannot end with #, $ or %");
				return false;
			}
		}

		return true;
	}

	private boolean compileAs(Mutable<String> name, Mutable<ValType> type) {
		if (type.get().m_basicType != BasicValType.VTP_UNDEFINED) {
			setError("'" + name
					+ "'s type has already been defined. Cannot use 'as' here.");
			return false;
		}

		// Skip "as"
		if (!GetToken()) {
			return false;
		}

		// Expect "single", "double", "integer", "string" or a structure type
		if (token.tokenType != TokenType.CTT_TEXT
				&& token.tokenType != TokenType.CTT_KEYWORD) {
			setError("Expected 'single', 'double', 'integer', 'string' or type name");
			return false;
		}
		if (token.text.equals("integer")) {
			type.get().m_basicType = BasicValType.VTP_INT;
		} else if (token.text.equals("single")
				|| token.text.equals("double"))

			// Note: Basic4GL supports only one type of floating point number.
			// We will accept both keywords, but simply allocate a real (=
			// single
			// precision) floating point number each time.
		{
			type.get().m_basicType = BasicValType.VTP_REAL;
		} else if (token.text.equals("string")) {
			type.get().m_basicType = BasicValType.VTP_STRING;
		} else {

			// Look for recognised structure name
			String structureName = symbolPrefix + token.text;
			int i = vm.getDataTypes().GetStruc(structureName);
			if (i < 0) {
				setError("Expected 'single', 'double', 'integer', 'string' or type name");
				return false;
			}
			type.get().m_basicType = i;
		}

		// Skip type name
		if (!GetToken()) {
			return false;
		}

		return true;
	}

	private boolean compileDimField(Mutable<String> name, Mutable<ValType> type,
			boolean forStruc, boolean forFuncParam) {

		// Compile data type
		TokenType tokenType = TokenType.CTT_CONSTANT;
		Mutable<TokenType> tokenTypeRef = new Mutable<TokenType>(tokenType);
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
		if (token.text.equals("(")) {

			boolean foundComma = false;
			while (token.text.equals("(") || foundComma) {

				// Room for one more dimension?
				if (type.get().m_arrayLevel >= TomVM.ARRAY_MAX_DIMENSIONS) {
					setError("Arrays cannot have more than "
							+ String.valueOf(TomVM.ARRAY_MAX_DIMENSIONS)
							+ " dimensions.");
					return false;
				}
				if (!GetToken()) // Skip "("
				{
					return false;
				}

				// Validate dimensions.
				// Firstly, pointers don't have dimensions declared with them.
				if (type.get().m_pointerLevel > 0) {
					if (!token.text.equals(")")) {
						setError("Use '()' to declare a pointer to an array");
						return false;
					}
					type.get().m_arrayLevel++;
				}
				// Structure field types must have constant array size that we
				// can
				// evaluate at compile time (i.e right now).
				else if (forStruc) {

					// Evaluate constant expression
					Integer expressionType = BasicValType.VTP_INT;
					Value value = new Value();
					String stringValue = "";

					Mutable<Integer> expressionTypeRef = new Mutable<Integer>(expressionType);
					Mutable<Value> valueRef = new Mutable<Value>(value);
					Mutable<String> stringValueRef = new Mutable<String>(
							stringValue);

					if (!EvaluateConstantExpression(expressionTypeRef,
							valueRef, stringValueRef)) {
						return false;
					}

					// Update local values from references
					expressionType = expressionTypeRef.get();
					value = valueRef.get();
					stringValue = stringValueRef.get();

					// Store array dimension
					type.get().AddDimension(value.getIntVal() + 1);
				} else if (forFuncParam) {
					// Array sizes for function parameters aren't declared.
					// (Syntax is "dim myArray()")
					type.get().m_arrayLevel++;
				}
				// Regular DIMmed array dimensions are sized at run time.
				// Here we generate code to calculate the dimension size and
				// push it to
				// stack.
				else {
					if (!(compileExpression()
							&& compileConvert (BasicValType.VTP_INT) && compilePush())) {
						return false;
					}
					type.get().m_arrayLevel++;
				}

				// Expect closing ')', or a separating comma
				foundComma = false;
				if (token.text.equals(")")) {
					if (!GetToken()) {
						return false;
					}
				} else if (token.text.equals(",")) {
					foundComma = true;
				} else {
					setError("Expected ')' or ','");
					return false;
				}
			}
		}

		// "as" keyword (QBasic/FreeBasic compatibility)
		if (token.text.equals("as")) {
			if (!compileAs(name, type)) {
				return false;
			}
		}

		// If data type still not specified, default to integer
		if (type.get().m_basicType == BasicValType.VTP_UNDEFINED) {
			type.get().m_basicType = BasicValType.VTP_INT;
		}

		return true;
	}

	private boolean compileLoadVar() {

		// Look for "take address"
		boolean takeAddress = false;
		if (token.text.equals("&")) {
			takeAddress = true;
			if (!GetToken()) {
				return false;
			}
		}

		// Look for variable name
		if (token.tokenType != TokenType.CTT_TEXT) {
			setError("Expected variable name");
			return false;
		}

		// Prefix variable names
		String varName = symbolPrefix + token.text;

		// Find variable
		boolean found = false;

		// Check local variable first
		if (inFunction) {

			// Look for variable
			int varIndex = getCurrentUserFunctionPrototype().getLocalVar(varName);

			// Set register type
			if (varIndex >= 0) {

				// Generate code to load variable
				AddInstruction(OpCode.OP_LOAD_LOCAL_VAR,
						BasicValType.VTP_INT, new Value(varIndex));

				// Set register type
				regType.Set(getCurrentUserFunctionPrototype().localVarTypes.get(varIndex));
				regType.m_pointerLevel++;

				found = true;
			}
		}

		// Then try global
		if (!found) {

			// Look for variable
			int varIndex = vm.getVariables().getVariableIndex(varName);

			if (varIndex >= 0) {

				// Generate code to load variable
				AddInstruction(OpCode.OP_LOAD_VAR, BasicValType.VTP_INT,
						new Value(varIndex));

				// Set register type
				regType
				.Set(vm.getVariables().getVariables().get(varIndex).m_type);
				regType.m_pointerLevel++;

				found = true;
			}
		}

		if (!found) {
			setError((String) "Unknown variable: " + token.text
					+ ". Variables must be declared first with DIM");
			return false;
		}

		// Skip past variable name
		if (!GetToken()) {
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

	private boolean compileDeref() {

		// Generate code to dereference pointer in reg. (i.e reg = [reg]).
		assertTrue(vm.getDataTypes().TypeValid(regType));

		// Not a pointer?
		if (regType.VirtualPointerLevel() <= 0) {
			assertTrue(false); // This should never happen
			setError("INTERNAL COMPILER ERROR: Attempted to dereference a non-pointer");
			return false;
		}

		// If reg is pointing to a structure or an array, we don't dereference
		// (as we can't fit an array or structure into a 4 byte register!).
		// Instead we leave it as a pointer, but update the type to indicate
		// to the compiler that we are using a pointer internally to represent
		// a variable.
		assertTrue(!regType.m_byRef);
		if (regType.PhysicalPointerLevel() == 1 // Must be pointer to actual
				// data (not pointer to
				// pointer e.t.c)
				&& (regType.m_arrayLevel > 0 // Array
						|| regType.m_basicType >= 0)) { // or structure
			regType.m_byRef = true;
			return true;
		}


		// Generate dereference instruction
		regType.m_pointerLevel--;
		AddInstruction(OpCode.OP_DEREF, regType.StoredType(), new Value()); // Load
		// variable

		return true;
	}

	private boolean compileDerefs() {

		// Generate code to dereference pointer
		if (!compileDeref()) {
			return false;
		}

		// In Basic4GL syntax, pointers are implicitly dereferenced (similar to
		// C++'s
		// "reference" type.)
		if (regType.VirtualPointerLevel() > 0) {
			if (!compileDeref()) {
				return false;
			}
		}

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
			if (token.text.equals(".")) {

				// Lookup subfield
				// Register must contain a structure type
				if (regType.VirtualPointerLevel() != 0
						|| regType.m_arrayLevel != 0
						|| regType.m_basicType < 0) {
					setError("Unexpected '.'");
					return false;
				}
				assertTrue(vm.getDataTypes().TypeValid(regType));

				// Skip "."
				if (!GetToken()) {
					return false;
				}

				// Read field name
				if (token.tokenType != TokenType.CTT_TEXT) {
					setError("Expected field name");
					return false;
				}
				String fieldName = token.text;
				if (!GetToken()) {
					return false;
				}

				// Validate field
				Structure s = vm.getDataTypes().Structures()
						.get(regType.m_basicType);
				int fieldIndex = vm.getDataTypes().GetField(s, fieldName);
				if (fieldIndex < 0) {
					setError((String) "'" + fieldName
							+ "' is not a field of structure '" + s.m_name
							+ "'");
					return false;
				}

				// Generate code to calculate address of field
				// Reg is initially pointing to address of structure.
				StructureField field = vm.getDataTypes().Fields()
						.get(fieldIndex);
				AddInstruction(OpCode.OP_ADD_CONST, BasicValType.VTP_INT,
						new Value(field.m_dataOffset));

				// Reg now contains pointer to field
				regType.Set(field.m_type);
				regType.m_pointerLevel++;

				// Dereference to reach data
				if (!compileDerefs()) {
					return false;
				}
			} else if (token.text.equals("(")) {

				// Register must contain an array
				if (regType.VirtualPointerLevel() != 0
						|| regType.m_arrayLevel == 0) {
					setError("Unexpected '('");
					return false;
				}

				do {
					if (regType.m_arrayLevel == 0) {
						setError("Unexpected ','");
						return false;
					}

					// Index into array
					if (!GetToken()) // Skip "(" or ","
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
					if (!compileConvert (BasicValType.VTP_INT)) {
						setError("Array index must be a number. "
								+ vm.getDataTypes().DescribeVariable("",
								regType) + " is not a number");
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
					AddInstruction(OpCode.OP_ARRAY_INDEX,
							BasicValType.VTP_INT, new Value());

					// reg now points to an element
					regType.Set(reg2Type);
					regType.m_byRef = false;
					regType.m_pointerLevel = 1;
					regType.m_arrayLevel--;

					// Dereference to get to element
					if (!compileDerefs()) {
						return false;
					}

				} while (token.text.equals(","));

				// Expect closing bracket
				if (!token.text.equals(")")) {
					setError("Expected ')'");
					return false;
				}
				if (!GetToken()) {
					return false;
				}
			} else {
				done = true;
			}
		}

		// Compile take address (if necessary)
		if (takeAddress) {
			if (!compileTakeAddress()) {
				return false;
			}
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
		operatorStack.add(new StackedOperator(new Operator(
				OperType.OT_STOP, OpCode.OP_NOP, 0, -200000))); // Stop
		// evaluation
		// operator

		if (!compileExpressionLoad(mustBeConstant)) {
			return false;
		}

		Operator o = null;
		while ((token.text.equals(")") && getOperatorTOS().operator.type != OperType.OT_STOP)
				|| ((o = binaryOperators.get(token.text)) != null)) {

			// Special case, right bracket
			if (token.text.equals(")")) {

				// Evaluate all operators down to left bracket
				while (getOperatorTOS().operator.type != OperType.OT_STOP
						&& getOperatorTOS().operator.type != OperType.OT_LBRACKET) {
					if (!compileOperation()) {
						return false;
					}
				}

				// If operator stack is empty, then the expression terminates
				// before
				// the closing bracket
				if (getOperatorTOS().operator.type == OperType.OT_STOP) {
					operatorStack.remove(operatorStack.size() - 1); // Remove
					// stopper
					return true;
				}

				// Remove left bracket
				operatorStack.remove(operatorStack.size() - 1);

				// Move on
				if (!GetToken()) {
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
						AddInstruction(OpCode.OP_JUMP_FALSE,
								BasicValType.VTP_INT, new Value(0));
					} else if (o.opCode == OpCode.OP_OP_OR) {
						lazyJumpAddr = vm.getInstructionCount();
						AddInstruction(OpCode.OP_JUMP_TRUE,
								BasicValType.VTP_INT, new Value(0));
					}
				}

				// Save operator to stack
				operatorStack.add(new StackedOperator(o, lazyJumpAddr));

				// Push first operand
				if (!compilePush()) {
					return false;
				}

				// Load second operand
				if (!GetToken()) {
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

			// Try plug in language extension first
			if (compileExtendedUnOperation(o.operator.opCode)) {
				return true;
			}

			// Can only operate on basic types.
			// (This will change once vector and matrix routines have been
			// implemented).
			if (!regType.IsBasic()) {
				setError("Operator cannot be applied to this data type");
				return false;
			}

			// Special case, boolean operator.
			// Must convert to boolean first
			if (o.operator.type == OperType.OT_BOOLOPERATOR
					|| o.operator.type == OperType.OT_LAZYBOOLOPERATOR) {
				compileConvert (BasicValType.VTP_INT);
			}

			// Perform unary operation
			AddInstruction(o.operator.opCode, regType.m_basicType,
					new Value());

			// Special case, boolean operator
			// Result will be an integer
			if (o.operator.type == OperType.OT_RETURNBOOLOPERATOR) {
				regType.Set (BasicValType.VTP_INT);
			}
		} else if (o.operator.params == 2) {

			// Generate code to pop first operand from stack into Reg2
			if (!compilePop()) {
				return false;
			}

			// Try plug in language extension first
			if (compileExtendedBinOperation(o.operator.opCode)) {
				return true;
			}

			// Ensure operands are equal type. Generate code to convert one if
			// necessary.

			int opCodeType; // Data type to store in OP_CODE
			if (regType.IsNull() || reg2Type.IsNull()) {

				// Can compare null to any pointer type. However, operator must
				// be '=' or '<>'
				if (o.operator.opCode != OpCode.OP_OP_EQUAL
						&& o.operator.opCode != OpCode.OP_OP_NOT_EQUAL) {
					setError("Operator cannot be applied to this data type");
					return false;
				}

				// Convert null pointer type to non null pointer type
				// Note: If both pointers a null, CompileConvert will simply do
				// nothing
				if (regType.IsNull()) {
					if (!compileConvert(reg2Type)) {
						return false;
					}
				}

				if (reg2Type.IsNull()) {
					if (!compileConvert2(regType)) {
						return false;
					}
				}

				opCodeType = BasicValType.VTP_INT; // Integer comparison is
				// used internally
			} else if (regType.VirtualPointerLevel() > 0
					|| reg2Type.VirtualPointerLevel() > 0) {

				// Can compare 2 pointers. However operator must be '=' or '<>'
				// and
				// pointer types must be exactly the same
				if (o.operator.opCode != OpCode.OP_OP_EQUAL
						&& o.operator.opCode != OpCode.OP_OP_NOT_EQUAL) {
					setError("Operator cannot be applied to this data type");
					return false;
				}
				if (!regType.ExactEquals(reg2Type)) {
					setError("Cannot compare pointers to different types");
					return false;
				}

				opCodeType = BasicValType.VTP_INT; // Integer comparison is
				// used internally
			} else {

				// Otherwise all operators can be applied to basic data types
				if (!regType.IsBasic() || !reg2Type.IsBasic()) {
					setError("Operator cannot be applied to this data type");
					return false;
				}

				// Convert operands to highest type
				int highest = regType.m_basicType;
				if (reg2Type.m_basicType > highest) {
					highest = reg2Type.m_basicType;
				}
				if (o.operator.type == OperType.OT_BOOLOPERATOR
						|| o.operator.type == OperType.OT_LAZYBOOLOPERATOR) {
					highest = BasicValType.VTP_INT;
				}
				if (syntax == LanguageSyntax.LS_TRADITIONAL
						&& o.operator.opCode == OpCode.OP_OP_DIV)
					// 14-Aug-05 Tom: In traditional mode, division is always
					// between floating pt numbers
				{
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
			AddInstruction(o.operator.opCode, opCodeType, new Value());

			// Special case, boolean operator
			// Result will be an integer
			if (o.operator.type == OperType.OT_RETURNBOOLOPERATOR) {
				regType.Set (BasicValType.VTP_INT);
			}
		} else {
			assertTrue(false);
		}

		// Fix up lazy jumps
		if (o.lazyJumpAddress >= 0) {
			vm.getInstruction(o.lazyJumpAddress).mValue.setVal((int) vm
					.getInstructionCount());
		}

		return true;
	}

	private boolean compileLoad() {

		// Compile load var or constant, or function result
		if (token.tokenType == TokenType.CTT_CONSTANT
				|| token.text.equals("null")) {
			return compileLoadConst();
		} else if (token.tokenType == TokenType.CTT_TEXT
				|| token.text.equals("&")) {
			return compileLoadVar();
		} else if (token.tokenType == TokenType.CTT_FUNCTION) {
			return compileFunction(true);
		} else if (token.tokenType == TokenType.CTT_USER_FUNCTION) {
			return compileUserFunctionCall(true, false);
		} else if (token.tokenType == TokenType.CTT_RUNTIME_FUNCTION) {
			return compileUserFunctionCall(true, true);
		}

		setError("Expected constant, variable or function");
		return false;
	}

	private boolean compileExpressionLoad() {
		return compileExpressionLoad(false);
	}

	private boolean compileExpressionLoad(boolean mustBeConstant) {

		// Like CompileLoad, but will also accept and stack preceeding unary
		// operators

		// Push any unary operators found
		while (true) {

			// Special case, left bracket
			if (token.text.equals("(")) {
				operatorStack.add(new StackedOperator(new Operator(
						OperType.OT_LBRACKET, OpCode.OP_NOP, 0, -10000))); // Brackets
			}
			// bind
			// looser
			// than
			// anything else

			// Otherwise look for recognised unary operator
			else {
				Operator o = unaryOperators.get(token.text);
				if (o != null) // Operator found
				{
					operatorStack.add(new StackedOperator(o)); // => Stack
				}
				// it
				else { // Not an operator
					if (mustBeConstant) {
						return compileLoadConst();
					} else {
						return compileLoad(); // => Proceed on to load
					}
					// variable/constant
				}
			}

			if (!GetToken()) {
				return false;
			}
		}
	}

	private boolean compileNull() {
		AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_INT,
				new Value(0)); // Load 0 into
		// register
		regType.Set(new ValType (BasicValType.VTP_NULL, (byte) 0, (byte) 1,
				false)); // Type
		// is
		// pointer
		// to
		// VTP_NULL
		return GetToken();
	}

	private boolean compileLoadConst() {

		// Special case, "null" reserved word
		if (token.text.equals("null")) {
			return compileNull();
		}

		// Compile load constant
		if (token.tokenType == TokenType.CTT_CONSTANT) {

			// Special case, string constants
			if (token.m_valType == BasicValType.VTP_STRING) {

				// Allocate new string constant
				String text;
				text = token.text.substring(1,	token.text.length()); // Remove S prefix
				int index = vm.StoreStringConstant(text);

				// store load instruction
				AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING,
						new Value(index));
				regType.Set (BasicValType.VTP_STRING);
			} else if (token.m_valType == BasicValType.VTP_REAL) {
				AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_REAL,
						new Value(Float.valueOf(token.text)));
				regType.Set (BasicValType.VTP_REAL);
			} else if (token.m_valType == BasicValType.VTP_INT) {
				AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_INT,
						new Value(Cast.StringToInt(token.text)));
				regType.Set (BasicValType.VTP_INT);
			} else {
				setError("Unknown data type");
				return false;
			}

			return GetToken();
		}

		setError("Expected constant");
		return false;
	}

	private boolean compilePush() {

		// Store pushed value type
		operandStack.add(new ValType(regType));

		// Generate push code
		AddInstruction(OpCode.OP_PUSH, regType.StoredType(), new Value());

		return true;
	}

	private boolean compilePop() {

		if (operandStack.isEmpty()) {
			setError("Expression error");
			return false;
		}

		// Retrieve pushed value type
		reg2Type.Set(operandStack.lastElement());
		operandStack.remove(operandStack.size() - 1);

		// Generate pop code
		AddInstruction(OpCode.OP_POP, reg2Type.StoredType(), new Value());

		return true;
	}

	private boolean compileConvert(int basictype) {

		// Convert reg to given type
		if (regType.Equals(basictype)) // Already same type
		{
			return true;
		}

		// Determine opcode
		short code = OpCode.OP_NOP;
		if (regType.Equals (BasicValType.VTP_INT)) {
			if (basictype == BasicValType.VTP_REAL) {
				code = OpCode.OP_CONV_INT_REAL;
			} else if (basictype == BasicValType.VTP_STRING) {
				code = OpCode.OP_CONV_INT_STRING;
			}
		} else if (regType.Equals (BasicValType.VTP_REAL)) {
			if (basictype == BasicValType.VTP_INT) {
				code = OpCode.OP_CONV_REAL_INT;
			} else if (basictype == BasicValType.VTP_STRING) {
				code = OpCode.OP_CONV_REAL_STRING;
			}
		}

		// Store instruction
		if (code != OpCode.OP_NOP) {
			AddInstruction(code, BasicValType.VTP_INT, new Value());
			regType.Set(basictype);
			return true;
		}

		setError("Incorrect data type");
		return false;
	}

	private boolean compileConvert2(int type) {

		// Convert reg2 to given type
		if (reg2Type.Equals(type)) // Already same type
		{
			return true;
		}

		// Determine opcode
		short code = OpCode.OP_NOP;
		if (reg2Type.Equals (BasicValType.VTP_INT)) {
			if (type == BasicValType.VTP_REAL) {
				code = OpCode.OP_CONV_INT_REAL2;
			} else if (type == BasicValType.VTP_STRING) {
				code = OpCode.OP_CONV_INT_STRING2;
			}
		} else if (reg2Type.Equals (BasicValType.VTP_REAL)) {
			if (type == BasicValType.VTP_INT) {
				code = OpCode.OP_CONV_REAL_INT2;
			} else if (type == BasicValType.VTP_STRING) {
				code = OpCode.OP_CONV_REAL_STRING2;
			}
		}

		// Store instruction
		if (code != OpCode.OP_NOP) {
			AddInstruction(code, BasicValType.VTP_INT, new Value());
			reg2Type.Set(type);
			return true;
		}

		setError("Incorrect data type");
		return false;
	}

	private boolean compileConvert(ValType type) {

		// Can convert null to a different pointer type
		if (regType.IsNull()) {
			if (type.VirtualPointerLevel() <= 0) {
				setError("Cannot convert null to "
						+ vm.getDataTypes().DescribeVariable("", type));
				return false;
			}

			// No generated code necessary, just substitute in type
			regType.Set(type);
			return true;
		}

		// Can convert values to references. (This is used when evaluating
		// function
		// parameters.)
		if (type.m_pointerLevel == 1 && type.m_byRef // type is a reference
				&& regType.m_pointerLevel == 0 // regType is a value
				&& regType.m_basicType == type.m_basicType // Same type of
				// data
				&& regType.m_arrayLevel == type.m_arrayLevel) {

			// Convert register to pointer
			if (compileTakeAddress()) {

				// Convert pointer to reference
				regType.m_byRef = true;
				return true;
			} else {
				return false;
			}
		}

		// Can convert to basic types.
		// For non basic types, all we can do is verify that the register
		// contains
		// the type that we expect, and raise a compiler error otherwise.
		if (type.IsBasic()) {
			return compileConvert(type.m_basicType);
		} else if (regType.ExactEquals(type)) {
			return true; // Note: Exact equals is required as == will say that
		}
		// pointers are equal to references.
		// (Internally this is true, but we want to enforce
		// that programs use the correct type.)

		setError("Cannot convert to "
				+ vm.getDataTypes().DescribeVariable("", type));
		return false;
	}

	private boolean compileConvert2(ValType type) {

		// Can convert null to a different pointer type
		if (reg2Type.IsNull()) {
			if (type.VirtualPointerLevel() <= 0) {
				setError("Cannot convert null to "
						+ vm.getDataTypes().DescribeVariable("", type));
				return false;
			}

			// No generated code necessary, just substitute in type
			reg2Type.Set(type);
			return true;
		}

		// Can convert to basic types.
		// For non basic types, all we can do is verify that the register
		// contains
		// the type that we expect, and raise a compiler error otherwise.
		if (type.IsBasic()) {
			return compileConvert2(type.m_basicType);
		} else if (reg2Type.ExactEquals(type)) {
			return true; // Note: Exact equals is required as == will say that
		}
		// pointers are equal to references.
		// (Internally this is true, but we want to enforce
		// that programs use the correct type.)

		setError("Cannot convert to "
				+ vm.getDataTypes().DescribeVariable("", type));
		return false;
	}

	private boolean compileTakeAddress() {

		// Take address of data in reg.
		// We do this my moving the previously generate deref from the end of
		// the program.
		// (If the last instruction is not a deref, then there is a problem.)

		// Special case: Implicit pointer
		if (regType.m_byRef) {
			regType.m_byRef = false; // Convert to explicit pointer
			return true;
		}

		// Check last instruction was a deref
		if (vm.getInstructionCount() <= 0
				|| vm.getInstruction(vm.getInstructionCount() - 1).mOpCode != OpCode.OP_DEREF) {
			setError("Cannot take address of this data");
			return false;
		}

		// Remove it
		vm.removeLastInstruction();
		regType.m_pointerLevel++;

		return true;
	}

	private boolean compileAssignment() {

		// Generate code to load target variable
		if (!compileLoadVar()) {
			return false;
		}

		// Compile code to assign value to variable
		if (!InternalCompileAssignment()) {
			return false;
		}

		return true;
	}

	boolean InternalCompileAssignment() {

		// Expect =
		if (!token.text.equals("=")) {
			setError("Expected '='");
			return false;
		}

		// Convert load target variable into take address of target variable
		if (!compileTakeAddress()) {
			setError("Left side cannot be assigned to");
			return false;
		}

		// Skip =
		if (!GetToken()) {
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
		if (reg2Type.m_pointerLevel == 1 && reg2Type.m_arrayLevel == 0
				&& reg2Type.m_basicType < 0) {

			// Attempt to convert value in reg to same type
			if (!compileConvert(reg2Type.m_basicType)) {
				setError("Types do not match");
				return false;
			}

			// Save reg into [reg2]
			AddInstruction(OpCode.OP_SAVE, reg2Type.m_basicType,
					new Value());
		}

		// Pointer case. m_reg2 must point to a pointer and m_reg1 point to a
		// value.
		else if (reg2Type.VirtualPointerLevel() == 2
				&& regType.VirtualPointerLevel() == 1) {

			// Must both point to same type, OR m_reg1 must point to null
			if (regType.IsNull()
					|| (regType.m_arrayLevel == reg2Type.m_arrayLevel && regType.m_basicType == reg2Type.m_basicType)) {

				// Validate pointer scope before saving to variable
				AddInstruction(OpCode.OP_CHECK_PTR, BasicValType.VTP_INT,
						new Value());

				// Save address to pointer
				AddInstruction(OpCode.OP_SAVE, BasicValType.VTP_INT,
						new Value());
			} else {
				setError("Types do not match");
				return false;
			}
		}

		// Copy object case
		else if (reg2Type.VirtualPointerLevel() == 1
				&& regType.VirtualPointerLevel() == 0
				&& regType.PhysicalPointerLevel() == 1) {

			// Check that both are the same type
			if (regType.m_arrayLevel == reg2Type.m_arrayLevel
					&& regType.m_basicType == reg2Type.m_basicType) {

				// Add op-code to check pointers if necessary
				ValType dataType = new ValType(regType);
				dataType.m_pointerLevel--;
				dataType.m_byRef = false;
				if (vm.getDataTypes().ContainsPointer(dataType)) {
					AddInstruction(OpCode.OP_CHECK_PTRS,
							BasicValType.VTP_INT,
							new Value((int) vm.getStoreTypeIndex(dataType)));
				}

				AddInstruction(OpCode.OP_COPY, BasicValType.VTP_INT,
						new Value((int) vm.getStoreTypeIndex(regType)));
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
				|| jumpType == OpCode.OP_JUMP_FALSE || jumpType == OpCode.OP_CALL);

		// Cannot use goto inside a function or sub (can use GOSUB though)
		if (inFunction && jumpType != OpCode.OP_CALL) {
			setError("Cannot use 'goto' inside a function or subroutine");
			return false;
		}

		// Validate label
		if (token.tokenType != TokenType.CTT_TEXT) {
			setError("Expected label name");
			return false;
		}

		// Record jump, so that we can fix up the offset in the second compile
		// pass.
		String labelName = symbolPrefix + token.text;
		jumps.add(new Jump(vm.getInstructionCount(), labelName));

		// Add jump instruction
		AddInstruction(jumpType, BasicValType.VTP_INT, new Value(0));

		// Move on
		return GetToken();
	}

	private boolean compileIf(boolean elseif) {

		// Skip "if"
		int line = parser.Line(), col = parser.Col();
		if (!GetToken()) {
			return false;
		}

		// Generate code to evaluate expression
		if (!compileExpression()) {
			return false;
		}

		// Generate code to convert to integer
		if (!compileConvert (BasicValType.VTP_INT)) {
			return false;
		}

		// Free any temporary data expression may have created
		if (!compileFreeTempData()) {
			return false;
		}

		// Special case!
		// If next instruction is a "goto", then we can ommit the "then"
		if (!token.text.equals("goto")) {

			// Otherwise expect "then"
			if (!token.text.equals("then")) {
				setError("Expected 'then'");
				return false;
			}
			if (!GetToken()) {
				return false;
			}
		}

		// Determine whether this "if" has an automatic "endif" inserted at the
		// end of the line
		boolean autoEndif = (syntax == LanguageSyntax.LS_TRADITIONAL) // Only
				// applies
				// to
				// traditional syntax
				&& !(token.tokenType == TokenType.CTT_EOL || token.tokenType == TokenType.CTT_EOF); // "then"
		// must
		// not
		// be
		// the
		// last
		// token
		// on
		// the
		// line

		// Create flow control structure
		flowControls.add(new FlowControl(FlowControlType.FCT_IF, vm
				.getInstructionCount(), 0, line, col, elseif, "", !autoEndif));

		// Create conditional jump
		AddInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT,
				new Value(0));

		needColon = false; // Don't need colon between this and next
		// instruction
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
		int line = parser.Line(), col = parser.Col();
		if (!elseif) {
			if (!GetToken()) {
				return false;
			}
		}

		// Push else to flow control stack
		flowControls.add(new FlowControl(FlowControlType.FCT_ELSE,
				vm.getInstructionCount(), 0, line, col, top.impliedEndif, "",
				top.blockIf));

		// Generate code to jump around else block
		AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(0));

		// Fixup jump around IF block
		assertTrue(top.jumpOut < vm.getInstructionCount());
		vm.getInstruction(top.jumpOut).mValue.setIntVal(vm
				.getInstructionCount());

		needColon = false; // Don't need colon between this and next
		// instruction
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
			if (!GetToken()) {
				return false;
			}
		}

		// Fixup jump around IF or ELSE block
		assertTrue(top.jumpOut < vm.getInstructionCount());
		vm.getInstruction(top.jumpOut).mValue.setIntVal(vm
				.getInstructionCount());

		// If there's an implied endif then add it
		if (top.impliedEndif) {
			return compileEndIf(automatic);
		} else {
			return true;
		}
	}

	private boolean compileFor() {

		// Skip "for"
		int line = parser.Line(), col = parser.Col();
		if (!GetToken()) {
			return false;
		}

		// Extract loop variable name
		Token nextToken = parser.PeekToken(false, false);
		if (!checkParser()) {
			return false;
		}
		if (nextToken.text.equals("(")) {
			setError("Cannot use array variable in 'for' - 'next' structure");
			return false;
		}
		String loopVarUnprefixed = token.text;
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
				ValType type = new ValType(getCurrentUserFunctionPrototype().localVarTypes.get(varIndex));
				if (!(type.Equals(BasicValType.VTP_INT) || type
						.Equals(BasicValType.VTP_REAL))) {
					setError("Loop variable must be an Integer or Real");
					return false;
				}
				loopVarType = type.m_basicType;
			}
		}

		// Check global variable
		if (!found) {
			int varIndex = vm.getVariables().getVariableIndex(loopVar);
			if (varIndex >= 0) {
				found = true;

				// Check type is INT or REAL
				ValType type = new ValType(vm.getVariables().getVariables().get(varIndex).m_type);
				if (!(type.Equals (BasicValType.VTP_INT) || type
						.Equals (BasicValType.VTP_REAL))) {
					setError("Loop variable must be an Integer or Real");
					return false;
				}
				loopVarType = type.m_basicType;
			}
		}
		if (!found) {
			setError("Unknown variable: " + token.text
					+ ". Must be declared with DIM");
			return false;
		}

		// Compile assignment
		int varLine = parser.Line(), varCol = parser.Col();
		Token varToken = token;
		if (!compileAssignment()) {
			return false;
		}

		// Save loop back position
		int loopPos = vm.getInstructionCount();

		// Expect "to"
		if (!token.text.equals("to")) {
			setError("Expected 'to'");
			return false;
		}
		if (!GetToken()) {
			return false;
		}

		// Compile load variable and push
		ParserPos savedPos = SavePos(); // Save parser position
		parser.SetPos(varLine, varCol); // Point to variable name
		token = varToken;

		if (!compileLoadVar()) // Load variable
		{
			return false;
		}
		if (!compilePush()) // And push
		{
			return false;
		}

		RestorePos(savedPos); // Restore parser position

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

		if (token.text.equals("step")) {

			// Skip step instruction
			if (!GetToken()) {
				return false;
			}

			// Compile step constant (expression)
			String stringValue = "";
			Mutable<Integer> stepTypeRef = new Mutable<Integer>(stepType);
			Mutable<Value> stepValueRef = new Mutable<Value>(stepValue);
			Mutable<String> stringValueRef = new Mutable<String>(stringValue);
			if (!EvaluateConstantExpression(stepTypeRef, stepValueRef,
					stringValueRef)) {
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
				+ (stepType == BasicValType.VTP_INT ? String.valueOf(stepValue
						.getIntVal()) : String.valueOf(stepValue.getRealVal()));

		// Create flow control structure
		flowControls.add(new FlowControl(FlowControlType.FCT_FOR, vm
				.getInstructionCount(), loopPos, line, col, false, step, false));

		// Create conditional jump
		AddInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT,
				new Value(0));

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
		int nextLine = token.line, nextCol = token.col;
		if (!GetToken()) {
			return false;
		}

		// Generate instruction to increment loop variable
		parser.SetSpecial(top.data, nextLine, nextCol); // Special mode.
		// Compile this
		// string instead of
		// source code.
		// We pass in the
		// line and the
		// column of the
		// "next"
		// instruction so
		// that generated
		// code will be
		// associated with
		// this offset in
		// the source code.
		// (This keeps the
		// debugger happy)
		Token saveToken = token;
		if (!GetToken()) {
			return false;
		}
		if (!compileAssignment()) {
			return false;
		}
		parser.SetNormal();
		token = saveToken;

		// Generate jump back instruction
		AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(
				top.jumpLoop));

		// Fixup jump around FOR block
		assertTrue(top.jumpOut < vm.getInstructionCount());
		vm.getInstruction(top.jumpOut).mValue.setIntVal(vm
				.getInstructionCount());
		return true;
	}

	private boolean compileWhile() {

		// Save loop position
		int loopPos = vm.getInstructionCount();

		// Skip "while"
		int line = parser.Line(), col = parser.Col();
		if (!GetToken()) {
			return false;
		}

		// Generate code to evaluate expression
		if (!compileExpression()) {
			return false;
		}

		// Generate code to convert to integer
		if (!compileConvert (BasicValType.VTP_INT)) {
			return false;
		}

		// Free any temporary data expression may have created
		if (!compileFreeTempData()) {
			return false;
		}

		// Create flow control structure
		flowControls.add(new FlowControl(FlowControlType.FCT_WHILE,
				vm.getInstructionCount(), loopPos, line, col));

		// Create conditional jump
		AddInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT,
				new Value(0));
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
		if (!GetToken()) {
			return false;
		}

		// Generate jump back
		AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(
				top.jumpLoop));

		// Fixup jump around WHILE block
		assertTrue(top.jumpOut < vm.getInstructionCount());
		vm.getInstruction(top.jumpOut).mValue.setIntVal(vm
				.getInstructionCount());
		return true;
	}

	private boolean compileDo() {

		// Save loop position
		int loopPos = vm.getInstructionCount();

		// Skip "do"
		int line = parser.Line(), col = parser.Col();
		if (!GetToken()) {
			return false;
		}

		// Look for "while" or "until"
		if (token.text.equals("while") || token.text.equals("until")) {

			// Is this a negative condition?
			boolean negative = token.text.equals("until");

			// Skip "while" or "until"
			if (!GetToken()) {
				return false;
			}

			// Generate code to evaluate expression
			if (!compileExpression()) {
				return false;
			}

			// Generate code to convert to integer
			if (!compileConvert (BasicValType.VTP_INT)) {
				return false;
			}

			// Free any temporary data expression may have created
			if (!compileFreeTempData()) {
				return false;
			}

			// Create flow control structure
			flowControls.add(new FlowControl(
					FlowControlType.FCT_DO_PRE, vm.getInstructionCount(),
					loopPos, line, col));

			// Create conditional jump
			AddInstruction(negative ? OpCode.OP_JUMP_TRUE
					: OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT,
					new Value(0));

			// Done
			return true;
		} else {

			// Post condition DO.
			// Create flow control structure
			flowControls.add(new FlowControl(
					FlowControlType.FCT_DO_POST, vm.getInstructionCount(),
					loopPos, line, col));
			return true;
		}
	}

	private boolean compileLoop() {

		if (!(isFlowControlTopEqual(FlowControlType.FCT_DO_PRE) || isFlowControlTopEqual(FlowControlType.FCT_DO_POST))) {
			setError("'loop' without 'do'");
			return false;
		}

		// Find DO details
		FlowControl top = getFlowControlTOS();
		flowControls.remove(flowControls.size() - 1);

		// Skip "DO"
		if (!GetToken()) {
			return false;
		}

		// Look for "while" or "until"
		if (token.text.equals("while") || token.text.equals("until")) {

			// This must be a post condition "do"
			if (top.controlType != FlowControlType.FCT_DO_POST) {
				setError("'until' or 'while' condition has already been specified for this 'do'");
				return false;
			}

			// Is this a negative condition?
			boolean negative = token.text.equals("until");

			// Skip "while" or "until"
			if (!GetToken()) {
				return false;
			}

			// Generate code to evaluate expression
			if (!compileExpression()) {
				return false;
			}

			// Generate code to convert to integer
			if (!compileConvert (BasicValType.VTP_INT)) {
				return false;
			}

			// Free any temporary data expression may have created
			if (!compileFreeTempData()) {
				return false;
			}

			// Create conditional jump back to "do"
			AddInstruction(negative ? OpCode.OP_JUMP_FALSE
					: OpCode.OP_JUMP_TRUE, BasicValType.VTP_INT, new Value(
							top.jumpLoop));

			// Done
			return true;
		} else {

			// Jump unconditionally back to "do"
			AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(
					top.jumpLoop));

			// If this is a precondition "do", fixup the jump around the "do"
			// block
			if (top.controlType == FlowControlType.FCT_DO_PRE) {
				assertTrue(top.jumpOut < vm.getInstructionCount());
				vm.getInstruction(top.jumpOut).mValue.setIntVal(vm
						.getInstructionCount());
			}

			// Done
			return true;
		}
	}

	boolean checkName(String name) {

		// Check that name is a suitable variable, structure or structure field
		// name.
		if (constants.containsKey(name)
				|| programConstants.containsKey(name)) {
			setError("'" + name + "' is a constant, and cannot be used here");
			return false;
		}
		if (reservedWords.contains(name)) {
			setError("'" + name
					+ "' is a reserved word, and cannot be used here");
			return false;
		}
		return true;
	}
	public void AddConstants(Map<String, Constant> constants){
		if (constants == null) {
			return;
		}
		//TODO Check if constant already exists before adding
		for (String key: constants.keySet()) {
			this.constants.put(key.toLowerCase(), constants.get(key));
		}
	}
	public void AddFunctions(Library library, Map<String, FuncSpec[]> specs) {
		int specIndex;
		int vmIndex;
		int i;

		if(library == null || specs == null) {
			return;
		}
		libraries.add(library);
		for (String name : specs.keySet()) {
			i = 0;
			for (FuncSpec func : specs.get(name)) {
				//Initialize function
				Object instance;
				//TODO Only initialize functions that are used
				try {
					Constructor<?> constructor = func.getFunctionClass().getConstructor(library.getClass());
					instance = constructor.newInstance(library);
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
					return;
				} catch (Exception e2) {
					e2.printStackTrace();
					return;
				}

				// Register wrapper function to virtual machine
				if (instance != null && instance instanceof Function) {
					vmIndex = vm.addFunction((Function)instance);
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
					l = new ArrayList<Integer>();
					functionIndex.put(name.toLowerCase(), l);
				}
				l.add(specIndex);

				i++;
			}

		}

	}

	private boolean compileFunction() {
		return compileFunction(false);
	}

	private boolean compileFunction(boolean needResult) {

		// Find function specifications.
		// (Note: There may be more than one with the same name.
		// We collect the possible candidates in an array, and prune out the
		// ones
		// whose paramater types are incompatible as we go..)
		ExtFuncSpec[] functions = new ExtFuncSpec[TC_MAXOVERLOADEDFUNCTIONS];
		int functionCount = 0;

		// Find builtin functions
		boolean found = false;
		for (Integer i : functionIndex.get(token.text)) {
			if (!(functionCount < TC_MAXOVERLOADEDFUNCTIONS)) {
				break;
			}
			FuncSpec spec = this.functions.get(i); // Get specification
			found = true;

			// Check whether function returns a value (if we need one)
			if (!needResult || spec.isFunction()) {
				if (functions[functionCount] == null) {
					functions[functionCount] = new ExtFuncSpec();
				}
				functions[functionCount].spec = spec;
				functions[functionCount].builtin = true;
				functionCount++;
			}
		}

		// Find plugin DLL functions
		// TODO Reimplement libraries
		// m_plugins.FindFunctions(m_token.m_text, functions, functionCount,
		// TC_MAXOVERLOADEDFUNCTIONS);

		// No functions?
		if (functionCount == 0) {

			if (found) {
				// We found some functions, but discarded them all. This would
				// only
				// ever happen if we required a return value, but none of the
				// functions
				// return one.
				setError(token.text + " does not return a value");
				return false;
			} else {
				setError(token.text + " is not a recognised function name");
				return false;
			}
		}

		// Skip function name token
		if (!GetToken()) {
			return false;
		}

		// Only the first instance will be checked to see whether we need
		// brackets.
		// (Therefore either all instances should have brackets, or all
		// instances
		// should have no brackets.)
		boolean brackets = functions[0].spec.hasBrackets();
		if (syntax == LanguageSyntax.LS_TRADITIONAL && brackets) { // Special
			// brackets
			// rules
			// for
			// traditional
			// syntax

			brackets = false;
			// Look for a version of the function that:
			// * Has at least one parameter, AND
			// * Returns a value
			//
			// If one is found, then we require brackets. Otherwise we don't.
			for (int i = 0; i < functionCount && !brackets; i++) {
				brackets = functions[i].spec.isFunction();
			}
			// && functions.get(i).m_paramTypes.getParams().size() > 0; // Need to
			// rethink below loop before we can enable this
		}

		// Expect opening bracket
		if (brackets) {
			if (!token.text.equals("(")) {
				setError("Expected '('");
				return false;
			}
			// Skip it
			if (!GetToken()) {
				return false;
			}
		}

		// Generate code to push parameters
		boolean first = true;
		int count = 0;
		int pushCount = 0; // Usually pushCount = count (the parameter count).
		// However "any type" parameters also have their
		// data type pushed with them, in which case
		// pushCount > count.
		while (functionCount > 0 && !token.text.equals(")")
				&& !AtSeparatorOrSpecial()) {

			// Trim functions with less parameters than we have found
			int src, dst;
			dst = 0;
			for (src = 0; src < functionCount; src++) {
				if (functions[src].spec.getParamTypes().getParams().size() > count) {
					functions[dst++] = functions[src];
				}
			}
			functionCount = dst;

			// None left?
			if (functionCount == 0) {
				if (brackets) {
					setError("Expected ')'");
				} else {
					setError("Expected ':' or end of line");
				}
				return false;
			}

			if (!first) {
				// Expect comma
				if (!token.text.equals(",")) {
					setError("Expected ','");
					return false;
				}
				// Skip it
				if (!GetToken()) {
					return false;
				}
			}
			first = false;

			// Generate code to evaluate parameter
			if (!compileExpression()) {
				return false;
			}

			// Find first valid function which matches at this parameter
			int matchIndex = -1;
			boolean isAnyType = false;
			int i;
			for (i = 0; i < functionCount && matchIndex < 0; i++) {
				ValType type = new ValType(functions[i].spec.getParamTypes().getParams()
						.get(count));

				// Check for undefined type parameter
				if (type.Equals (BasicValType.VTP_UNDEFINED)) {

					// Function definition indicates whether parameter type is
					// valid
					// via a compiler time callback function.
					if (functions[i].spec.getParamValidationCallback().run(
							count, regType)) {

						// Found "any type" match
						matchIndex = i;
						isAnyType = true;
					} else {
						setError("Incorrect data type");
					}
				} else {

					// Specific type requested.
					// Check parameter can be converted to that type
					if (compileConvert(type)) {

						// Found specific type match
						matchIndex = i;
						isAnyType = false;
					}
				}
			}

			if (matchIndex >= 0) {

				// Clear any errors that non-matching instances might have set.
				clearError();

				ValType type = new ValType(functions[matchIndex].spec.getParamTypes()
						.getParams().get(count));

				// Filter out all functions whose "count" parameter doesn't
				// match "type".
				dst = 0;
				for (src = 0; src < functionCount; src++) {

					if (isAnyType) {
						// If the first function to match accepts an "any type"
						// parameter, then all other overloads must be an
						// "any type"
						// parameter.
						if (functions[src].spec.getParamValidationCallback()
								.run(count, regType)) {
							functions[dst++] = functions[src];
						}
					} else {
						// Likewise if the first function to match requires a
						// specific
						// parameter type, then all other overloads must require
						// that
						// same type.
						if (functions[src].spec.getParamTypes().getParams()
								.get(count).Equals(type)) {
							functions[dst++] = functions[src];
						}
					}
				}
				functionCount = dst;
				assertTrue(functionCount > 0); // (Should at least have the
				// function that originally matched
				// the register)

				// Generate code to push parameter to stack
				compilePush();
				pushCount++;

				// If parameter is an "any type" then generate code to push the
				// parameter type to the stack.
				if (isAnyType) {
					AddInstruction(OpCode.OP_LOAD_CONST,
							BasicValType.VTP_INT,
							new Value((int) vm.getStoreTypeIndex(regType)));
					regType.Set (BasicValType.VTP_INT);
					compilePush();
					pushCount++;
				}
			} else {
				return false; // No function matched. (Return last compile
			}
			// convert error).

			count++; // Count parameters pushed
		}

		// Find the first function instance that accepts this number of
		// parameters
		int matchIndex = -1;
		int i;
		for (i = 0; i < functionCount && matchIndex < 0; i++) {
			if (functions[i].spec.getParamTypes().getParams().size() == count) {
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
		ExtFuncSpec spec = functions[matchIndex];

		// Expect closing bracket
		if (brackets) {
			if (!token.text.equals(")")) {
				setError("Expected ')'");
				return false;
			}
			// Skip it
			if (!GetToken()) {
				return false;
			}
		}

		// Generate code to call function
		if (spec.builtin)

			// Builtin function
		{
			AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT,
					new Value(spec.spec.getIndex()));
		} else

			// DLL function
			// Note: The DLL index is encoded as the high byte.
			// The 3 low bytes are the function index within the DLL.
			// This may be revised later...
		{
			AddInstruction(
					OpCode.OP_CALL_DLL,
					BasicValType.VTP_INT,
					new Value((spec.pluginIndex << 24)
							| (spec.spec.getIndex() & 0x00ffffff)));
		}

		// If function has return type, it will have changed the type in the
		// register
		if (spec.spec.isFunction()) {
			regType.Set(spec.spec.getReturnType());

			// If data is too large to fit in the register, it will be returned
			// in the "temp" area. If the data contains strings, they will need
			// to
			// be "destroyed" when temp data is unwound.
			if (!regType.CanStoreInRegister()
					&& vm.getDataTypes().ContainsString(regType)) {
				AddInstruction(OpCode.OP_REG_DESTRUCTOR,
						BasicValType.VTP_INT,
						new Value((int) vm.getStoreTypeIndex(regType)));
			}

			if (!compileDataLookup(false)) {
				return false;
			}
		}

		// Note whether function has generated temporary data
		freeTempData = freeTempData | spec.spec.getFreeTempData();

		// Generate code to clean up stack
		for (int i2 = 0; i2 < pushCount; i2++) {
			if (!compilePop()) {
				return false;
			}
		}

		// Generate explicit timesharing break (if necessary)
		if (spec.spec.getTimeshare()) {
			AddInstruction(OpCode.OP_TIMESHARE, BasicValType.VTP_INT,
					new Value());
		}

		return true;
	}

	private boolean compileConstant() {

		// Skip CONST
		if (!GetToken()) {
			return false;
		}

		// Expect at least one field in dim
		if (AtSeparatorOrSpecial()) {
			setError("Expected constant declaration");
			return false;
		}

		// Parse fields in dim
		boolean needComma = false; // First element doesn't need a comma
		while (!AtSeparatorOrSpecial()) {

			// Handle commas
			if (needComma) {
				if (!token.text.equals(",")) {
					setError("Expected ','");
					return false;
				}
				if (!GetToken()) {
					return false;
				}
			}
			needComma = true; // Remaining elements do need commas

			// Read constant name
			if (token.tokenType != TokenType.CTT_TEXT) {
				setError("Expected constant name");
				return false;
			}
			String name = token.text;
			if (programConstants.containsKey(name)) {
				setError("'" + name
						+ "' has already been declared as a constant.");
				return false;
			}
			if (!checkName(name)) {
				return false;
			}
			if (!GetToken()) {
				return false;
			}

			// Determine constant type from last character of constant name
			Integer type = BasicValType.VTP_UNDEFINED;
			if (name.length() > 0) {
				char last = name.charAt(name.length() - 1);
				if (last == '$') {
					type = BasicValType.VTP_STRING;
				} else if (last == '#') {
					type = BasicValType.VTP_REAL;
				} else if (last == '%') {
					type = BasicValType.VTP_INT;
				}
			}

			if (token.text.equals("as")) {
				if (type != BasicValType.VTP_UNDEFINED) {
					setError("'"
							+ name
							+ "'s type has already been defined. Cannot use 'as' here.");
					return false;
				}
				if (!GetToken()) {
					return false;
				}
				if (token.text.equals("integer")) {
					type = BasicValType.VTP_INT;
				} else if (token.text.equals("single")
						|| token.text.equals("double"))

					// Note: Basic4GL supports only one type of floating point
					// number.
					// We will accept both keywords, but simply allocate a real
					// (= single
					// precision) floating point number each time.
				{
					type = BasicValType.VTP_REAL;
				} else if (token.text.equals("string")) {
					type = BasicValType.VTP_STRING;
				} else {
					setError("Expected 'integer', 'single', 'double', 'string'");
					return false;
				}
				if (!GetToken()) {
					return false;
				}
			}

			// Default type to integer if not defined
			if (type == BasicValType.VTP_UNDEFINED) {
				type = BasicValType.VTP_INT;
			}

			// Expect =
			if (!token.text.equals("=")) {
				setError("Expected '='");
				return false;
			}
			if (!GetToken()) {
				return false;
			}

			// Compile constant expression
			Value value = new Value();
			String stringValue = "";

			Mutable<Integer> typeRef = new Mutable<Integer>(type);
			Mutable<Value> valueRef = new Mutable<Value>(value);
			Mutable<String> stringValueRef = new Mutable<String>(stringValue);

			if (!EvaluateConstantExpression(typeRef, valueRef, stringValueRef)) {
				return false;
			}

			// Update local values from references
			type = typeRef.get();
			value = valueRef.get();
			stringValue = stringValueRef.get();

			switch (type) {
			case BasicValType.VTP_INT:
				programConstants.put(name,
						new Constant(value.getIntVal()));
				break;
			case BasicValType.VTP_REAL:
				programConstants.put(name,
						new Constant(value.getRealVal()));
				break;
			case BasicValType.VTP_STRING:
				programConstants.put(name, new Constant(
						(String) ("S" + stringValue)));
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
			AddInstruction(OpCode.OP_FREE_TEMP, BasicValType.VTP_INT,
					new Value());
		}
		freeTempData = false;

		return true;
	}

	private boolean compileExtendedUnOperation(short operOpCode) {

		Mutable<ValType> type = new Mutable<ValType>(new ValType());
		Mutable<Integer> opFunc = new Mutable<Integer>(-1);
		Mutable<Boolean> freeTempData = new Mutable<Boolean>(false);
		Mutable<ValType> resultType = new Mutable<ValType>(new ValType());
		boolean found = false;

		// Iterate through external operator extension functions until we find
		// one that can handle our data.
		for (int i = 0; i < unOperExts.size() && !found; i++) {

			// Setup input data
			type.get().Set(regType);
			opFunc.set(-1);
			freeTempData.set(false);
			resultType.set(new ValType());

			// Call function
			found = unOperExts.get(i).run(type, operOpCode, opFunc, resultType,
					freeTempData);
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
		AddInstruction(OpCode.OP_CALL_OPERATOR_FUNC, BasicValType.VTP_INT,
				new Value(opFunc.get()));

		// Set register to result type
		regType.Set(resultType.get());

		// Record whether we need to free temp data
		this.freeTempData = this.freeTempData || freeTempData.get();

		return true;
	}

	private boolean compileExtendedBinOperation(short operOpCode) {

		Mutable<ValType> type1 = new Mutable<ValType>(new ValType());
		Mutable<ValType> type2 = new Mutable<ValType>(new ValType());
		Mutable<Integer> opFunc = new Mutable<Integer>(-1);
		Mutable<Boolean> freeTempData = new Mutable<Boolean>(false);
		Mutable<ValType> resultType = new Mutable<ValType>(new ValType());
		boolean found = false;

		// Iterate through external operator extension functions until we find
		// one that can handle our data.
		for (int i = 0; i < binOperExts.size() && !found; i++) {

			// Setup input data
			type1.get().Set(regType);
			type2.get().Set(reg2Type);
			opFunc.set(-1);
			freeTempData.set(false);
			resultType.set(new ValType());

			// Call function
			found = binOperExts.get(i).run(type1, type2, operOpCode, opFunc,
					resultType, freeTempData);

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
		AddInstruction(OpCode.OP_CALL_OPERATOR_FUNC, BasicValType.VTP_INT,
				new Value(opFunc.get()));

		// Set register to result type
		regType.Set(resultType.get());

		// Record whether we need to free temp data
		this.freeTempData = this.freeTempData || freeTempData.get();

		return true;
	}

	public String FunctionName(int index) // Find function name for function #.
	// Used for debug reporting
	{
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
		if (!GetToken()) {
			return false;
		}

		// Expect &pointer variable
		if (token.text.equals("&")) {
			setError("First argument must be a pointer");
			return false;
		}

		// Load pointer
		if (!(compileLoadVar() && compileTakeAddress())) {
			return false;
		}

		// Store pointer type
		ValType ptrType = new ValType(regType), dataType = new ValType(
				regType);
		dataType.m_byRef = false;
		dataType.m_pointerLevel--;

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
		for (i = 0; i < dataType.m_arrayLevel; i++) {

			// Expect ,
			if (!token.text.equals(",")) {
				setError("Expected ','");
				return false;
			}
			if (!GetToken()) {
				return false;
			}

			// Generate code to evaluate array index, and convert to an integer.
			if (!compileExpression()) {
				return false;
			}
			if (!compileConvert (BasicValType.VTP_INT)) {
				setError("Array index must be a number. "
						+ vm.getDataTypes().DescribeVariable("", regType)
						+ " is not a number");
				return false;
			}

			// Push array index to stack
			if (!compilePush()) {
				return false;
			}
		}

		// Add alloc instruction
		AddInstruction(OpCode.OP_ALLOC, BasicValType.VTP_INT, new Value(
				(int) vm.getStoreTypeIndex(dataType)));

		// Instruction automatically removes all array indices that were pushed
		// to
		// the stack.
		for (i = 0; i < dataType.m_arrayLevel; i++) {
			operandStack.remove(operandStack.size() - 1);
		}

		// Instruction also automatically leaves the register pointing to the
		// new
		// data.
		regType.Set(ptrType);
		regType.m_byRef = false;

		// Generate code to pop destination address
		if (!compilePop()) {
			return false;
		}

		// Generate code to save address to pointer
		AddInstruction(OpCode.OP_SAVE, BasicValType.VTP_INT, new Value());

		return true;
	}

	ParserPos SavePos() {

		// Save the current parser position, so we can return to it later.
		ParserPos pos = new ParserPos();
		pos.line = parser.Line();
		pos.column = parser.Col();
		pos.token = token;
		return pos;
	}

	void RestorePos(ParserPos pos) {

		// Restore parser position
		parser.SetPos(pos.line, pos.column);
		token = pos.token;
	}

	// Debugging
	public String DescribeStackCall(int returnAddr) {

		// Return a string describing the gosub call
		if (returnAddr == 0 || returnAddr >= vm.getInstructionCount()) {
			return "???";
		}

		// Look at instruction immediately before return address.
		// This should be the gosub
		if (vm.getInstruction(returnAddr - 1).mOpCode != OpCode.OP_CALL) {
			return "???";
		}

		// Get target address
		int target = vm.getInstruction(returnAddr - 1).mValue.getIntVal();

		// Lookup label name
		String name = labelIndex.get(target);
		if (name == null) {
			return "???";
		}

		// Return label name
		return name;
	}

	public boolean TempCompileExpression(String expression, ValType valType,
			boolean inFunction, int currentFunction) {

		// Load expression into parser
		parser.SourceCode().clear();
		parser.SourceCode().add(expression);
		parser.reset();
		lastLine = 0;
		lastCol = 0;

		// Reset compiler state
		clearState();
		this.inFunction = inFunction;
		this.currentFunction = currentFunction;

		// Clear error state
		clearError();
		parser.clearError();

		// Read first token
		if (!GetToken(true, false)) {
			return false;
		}

		// Compile code
		if (!compileExpression()) {
			return false;
		}

		if (token.tokenType != TokenType.CTT_EOL) {
			setError("Extra characters after expression");
			return false;
		}

		// Terminate program
		AddInstruction(OpCode.OP_END, BasicValType.VTP_INT, new Value());

		// Return expression result type
		valType.Set(regType);
		return true;
	}

	private boolean compileData() {

		// Skip "data"
		if (!GetToken(false, true)) // Use "DATA" mode read
		{
			return false;
		}

		// Compile data elements
		boolean needComma = false;
		do {

			// Handle commas
			if (needComma) {
				if (!token.text.equals(",")) {
					setError("Expected ','");
					return false;
				}
				if (!GetToken(false, true)) {
					return false;
				}
			}
			needComma = true; // Remaining elements do need commas

			// Consecutive commas?
			if (token.text.equals(",") || AtSeparatorOrSpecial()) {

				// Store a blank string
				vm.storeProgramData(BasicValType.VTP_STRING, new Value(0));
			} else {

				// Extract value
				Value v = new Value();
				if (token.m_valType == BasicValType.VTP_STRING) {

					// Allocate new string constant
					String text = token.text.substring(1,
							token.text.length() - 1); // Remove S prefix
					v.setIntVal(vm.StoreStringConstant(text));
				} else if (token.m_valType == BasicValType.VTP_INT) {
					v.setIntVal(Cast.StringToInt(token.text));
				} else {
					v.setRealVal(Float.valueOf(token.text));
				}

				// Store data in VM
				vm.storeProgramData(token.m_valType, v);

				// Next token
				if (!GetToken()) {
					return false;
				}
			}
		} while (!AtSeparatorOrSpecial());
		return true;
	}

	private boolean compileDataRead() {

		// Skip "read"
		if (!GetToken()) {
			return false;
		}

		// Expect at one variable name
		if (AtSeparatorOrSpecial()) {
			setError("Expected variable name");
			return false;
		}

		// Parse fields in dim
		boolean needComma = false; // First element doesn't need a comma
		while (!AtSeparatorOrSpecial()) {

			// Handle commas
			if (needComma) {
				if (!token.text.equals(",")) {
					setError("Expected ','");
					return false;
				}
				if (!GetToken()) {
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
			if (!type.IsBasic()) {
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
			AddInstruction(OpCode.OP_DATA_READ, type.m_basicType,
					new Value());

			if (!compilePop()) {
				return false;
			}

			// Save reg into [reg2]
			AddInstruction(OpCode.OP_SAVE, reg2Type.m_basicType,
					new Value());
		}
		return true;
	}

	private boolean compileDataReset() {

		// Skip "reset"
		if (!GetToken()) {
			return false;
		}

		// If label specified, use offset stored in label
		if (!AtSeparatorOrSpecial()) {

			// Validate label
			if (token.tokenType != TokenType.CTT_TEXT) {
				setError("Expected label name");
				return false;
			}

			// Record reset, so that we can fix up the offset in the second
			// compile pass.
			String labelName = symbolPrefix + token.text;
			resets.add(new Jump(vm.getInstructionCount(), labelName));

			// Skip label name
			if (!GetToken()) {
				return false;
			}
		}

		// Add jump instruction
		AddInstruction(OpCode.OP_DATA_RESET, BasicValType.VTP_INT,
				new Value(0));

		return true;
	}

	boolean EvaluateConstantExpression(Mutable<Integer> basictype,
			Mutable<Value> result, Mutable<String> stringResult) {

		// Note: If type is passed in as VTP_UNDEFINED, then any constant
		// expression
		// will be accepted, and type will be set to the type that was found.
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
		AddInstruction(OpCode.OP_END, BasicValType.VTP_INT, new Value());

		// Setup virtual machine to execute expression
		// Note: Expressions can't branch or loop, and it's very difficult to
		// write
		// one that evaluates to a large number of op-codes. Therefore we won't
		// worry
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
		basictype.set(regType.m_basicType);
		if (basictype.get() == BasicValType.VTP_STRING) {
			stringResult.set(vm.getRegString());
		} else {
			result.set(vm.getReg());
		}

		return true;
	}

	private boolean compileConstantExpression() {
		return compileConstantExpression (BasicValType.VTP_UNDEFINED);
	}

	private boolean compileConstantExpression(int basictype) {

		// Evaluate constant expression
		Value value = new Value();
		String stringValue = "";

		// Create wrappers to pass values by reference
		Mutable<Integer> typeRef = new Mutable<Integer>(basictype);
		Mutable<Value> valueRef = new Mutable<Value>(value);
		Mutable<String> stringValueRef = new Mutable<String>(stringValue);

		if (!EvaluateConstantExpression(typeRef, valueRef, stringValueRef)) {
			return false;
		}

		// Update local values from wrappers
		basictype = typeRef.get();
		value = valueRef.get();
		stringValue = stringValueRef.get();

		// Generate "load constant" instruction
		if (basictype == BasicValType.VTP_STRING) {

			// Create string constant entry if necessary
			int index = vm.StoreStringConstant(stringValue);
			AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING,
					new Value(index));
		} else {
			AddInstruction(OpCode.OP_LOAD_CONST, basictype, value);
		}

		return true;
	}

	FuncSpec FindFunction(String name, int paramCount) {

		// Search for function with matching name & param count
		List<Integer> l = functionIndex.get(name);
		if (l != null) {
			for (Integer i : l) {
				FuncSpec spec = functions.get(i);
				if (spec.getParamTypes().getParams().size() == paramCount) {
					return spec;
				}
			}
		}

		// None found
		setError((String) "'" + name + "' function not found");
		return null;
	}


	private boolean compilePrint(boolean forceNewLine) {

		// The print function has a special syntax, and must be compiled
		// separately

		// Skip "print"
		if (!GetToken()) {
			return false;
		}

		boolean foundSemiColon = false;
		int operandCount = 0;
		while (!AtSeparatorOrSpecial()) {

			// Look for semicolon
			if (token.text.equals(";")) {

				// Record it, and move on to next
				foundSemiColon = true;
				if (!GetToken()) {
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
				if (!(compileExpression() && compileConvert (BasicValType.VTP_STRING))) {
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
			AddInstruction(OpCode.OP_OP_PLUS, BasicValType.VTP_STRING,
					new Value());
			regType.Set (BasicValType.VTP_STRING);

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
				|| ((syntax == LanguageSyntax.LS_TRADITIONAL || syntax == LanguageSyntax.LS_TRADITIONAL_PRINT) && !foundSemiColon);

		if (!newLine && operandCount == 0) // Nothing to print?
		{
			return true; // Do nothing!
		}

		FuncSpec spec = FindFunction(newLine ? "printr" : "print",
				operandCount);
		if (spec == null) {
			return false;
		}

		// Generate code to call it
		AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT,
				new Value(spec.getIndex()));

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
		// support
		// inputting multiple values on one line.
		// But it's a lot better than before.

		// Skip "input"
		if (!GetToken()) {
			return false;
		}

		// Check for prompt
		if (token.tokenType == TokenType.CTT_CONSTANT
				&& token.m_valType == BasicValType.VTP_STRING) {

			// Allocate new string constant
			String text = token.text.substring(1,
					token.text.length()); // Remove
			// S
			// prefix

			if (!GetToken()) {
				return false;
			}

			// Expect , or ;
			if (token.text.equals(";")) {
				text = text + "? ";
			} else if (!token.text.equals(",")) {
				setError("Expected ',' or ';'");
				return false;
			}
			if (!GetToken()) {
				return false;
			}

			// Create new string constant
			int index = vm.StoreStringConstant(text);

			// Generate code to print it (load, push, call "print" function)
			AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING,
					new Value(index));
			regType.Set (BasicValType.VTP_STRING);
			if (!compilePush()) {
				return false;
			}

			// Generate code to call "print" function
			FuncSpec printSpec = FindFunction("print", 1);
			if (printSpec == null) {
				return false;
			}
			AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT,
					new Value(printSpec.getIndex()));

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
		if (!regType.IsBasic()) {
			setError("Input variable must be a basic string, integer or real type");
			return false;
		}
		Integer variableType = regType.m_basicType;

		// Generate code to push its address to stack
		if (!(compileTakeAddress() && compilePush())) {
			return false;
		}

		// Generate code to call "input$()" function
		FuncSpec inputSpec = FindFunction("input$", 0);
		if (inputSpec == null) {
			return false;
		}
		AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT,
				new Value(inputSpec.getIndex()));
		AddInstruction(OpCode.OP_TIMESHARE, BasicValType.VTP_INT,
				new Value()); // Timesharing break
		// is necessary
		regType.Set (BasicValType.VTP_STRING);

		// If the variable is not a string, then we need to convert it to the
		// target
		// type. We do this by inserting an implicit call to the val() function.
		if (variableType != BasicValType.VTP_STRING) {

			// Push register back as input to val function
			if (!compilePush()) {
				return false;
			}

			// Generate code to call "val()" function
			FuncSpec valSpec = FindFunction("val", 1);
			if (valSpec == null) {
				return false;
			}
			AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT,
					new Value(valSpec.getIndex()));
			regType.Set (BasicValType.VTP_REAL);

			// Clean up stack
			if (!compilePop()) {
				return false;
			}
		}

		// Generate code to pop target address into reg2
		if (!compilePop()) {
			return false;
		}

		if (!compileConvert(reg2Type.m_basicType)) {
			setError("Types do not match"); // Technically this should never
			// actually happen
			return false;
		}

		// Generate code to save value
		AddInstruction(OpCode.OP_SAVE, reg2Type.m_basicType, new Value());

		return true;
	}

	private boolean compileLanguage() {

		// Compile language directive
		// Skip "language"
		if (!GetToken()) {
			return false;
		}

		// Expect syntax type
		if (token.text.equals("traditional")) {
			syntax = LanguageSyntax.LS_TRADITIONAL;
		} else if (token.text.equals("basic4gl")) {
			syntax = LanguageSyntax.LS_BASIC4GL;
		} else if (token.text.equals("traditional_print")) {
			syntax = LanguageSyntax.LS_TRADITIONAL_PRINT;
		} else {
			setError("Expected 'traditional', 'basic4gl' or 'traditional_print'");
			return false;
		}

		// Skip syntax token
		if (!GetToken()) {
			return false;
		}

		return true;
	}

	private boolean compileUserFunctionFwdDecl() {

		// Skip "declare"
		if (!GetToken()) {
			return false;
		}

		// Look for "sub" or "function"
		return compileUserFunction(UserFunctionType.UFT_FWDDECLARATION);
	}

	private boolean compileUserFunctionRuntimeDecl() {
		// Skip "runtime"
		if (!GetToken()) {
			return false;
		}

		return compileUserFunction(UserFunctionType.UFT_RUNTIMEDECLARATION);
	}

	private boolean compileUserFunction(UserFunctionType funcType) {
		// Function or sub?
		boolean hasReturnVal;
		if (token.text.equals("function")) {
			hasReturnVal = true;
		} else if (token.text.equals("sub")) {
			hasReturnVal = false;
		} else {
			setError("Expected 'sub' or 'function'");
			return false;
		}

		// Check that we are not already inside a function
		if (inFunction) {
			setError("Cannot define a function or subroutine inside another function or subroutine");
			return false;
		}

		// Check that we are not inside a control structure
		if (!checkUnclosedFlowControl()) {
			return false;
		}

		// Mark start of function in source code
		functionStart.setSourcePosition(parser.Line(), parser.Col());

		// Skip "func"
		if (!GetToken()) {
			return false;
		}

		// Compile data type
		TokenType tokenType = TokenType.CTT_CONSTANT;
		ValType type = new ValType (BasicValType.VTP_UNDEFINED);
		String name = "";
		Mutable<TokenType> tokenTypeRef = new Mutable<TokenType>(tokenType);
		Mutable<ValType> typeRef = new Mutable<ValType>(type);
		Mutable<String> nameRef = new Mutable<String>(name);

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
				setError("'"
						+ name
						+ "' has already been used as a built-in function/subroutine name");
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
		if (vm.getDataTypes().GetStruc(name) >= 0) {
			setError("'" + name + "' has already been used as a structure name");
			return false;
		}

		// Allocate a new user function
		userFuncPrototype.reset();

		// Expect "("
		if (!token.text.equals("(")) {
			setError("Expected '('");
			return false;
		}
		if (!GetToken()) {
			return false;
		}

		// Look for function parameters
		if (!token.text.equals(")")) {
			if (!compileDim(false, true)) {
				return false;
			}
		}

		// Expect ")"
		if (!token.text.equals(")")) {
			setError("Expected ')'");
			return false;
		}
		if (!GetToken()) {
			return false;
		}

		// Calculate final return value
		if (hasReturnVal) {

			// Any trailing () denote an array
			while (token.text.equals("(")) {

				// Room for one more dimension?
				if (type.m_arrayLevel >= TomVM.ARRAY_MAX_DIMENSIONS) {
					setError((String) "Arrays cannot have more than "
							+ String.valueOf(TomVM.ARRAY_MAX_DIMENSIONS)
							+ " dimensions.");
					return false;
				}

				// Add dimension
				type.m_arrayLevel++;

				if (!GetToken()) // Skip "("
				{
					return false;
				}

				// Expect ")"
				if (!token.text.equals(")")) {
					setError("')' expected");
					return false;
				}
				if (!GetToken()) {
					return false;
				}
			}

			// "as" keyword (QBasic/FreeBasic compatibility)
			if (token.text.equals("as")) {

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
			if (type.m_basicType == BasicValType.VTP_UNDEFINED) {
				type.m_basicType = BasicValType.VTP_INT;
			}

			// Store function return value type
			userFuncPrototype.hasReturnVal = true;
			userFuncPrototype.returnValType = type;
		} else {
			userFuncPrototype.hasReturnVal = false;
		}

		// Store function, and get its index (in m_currentFunction)
		Vector<UserFunc> functions = vm.getUserFunctions();
		Vector<UserFuncPrototype> prototypes = vm.getUserFunctionPrototypes();

		if (funcType == UserFunctionType.UFT_FWDDECLARATION) {
			// Forward declaration.

			// Function name must not already have been used
			if (isLocalUserFunction(name)) {
				setError("'"
						+ name
						+ "' has already been used as a function/subroutine name");
				return false;
			}

			// Function name must not have been used for a runtime function
			if (isRuntimeFunction(name)) {
				setError((String) "'"
						+ name
						+ "' has already been used as a runtime function/subroutine name");
				return false;
			}

			// Allocate new function
			prototypes.add(userFuncPrototype);
			userFuncPrototype = new UserFuncPrototype();
			functions.add(new UserFunc(prototypes.size() - 1, false));
			currentFunction = functions.size() - 1;

			// Map name to function
			localUserFunctionIndex.put(name, currentFunction);
			visibleUserFunctionIndex.put(name, currentFunction);
			if (!isGlobalUserFunction(name)) {
				globalUserFunctionIndex.put(name, currentFunction);
			}

			// Build reverse index (for debugger)
			userFunctionReverseIndex.put(currentFunction, name);
		} else if (funcType == UserFunctionType.UFT_RUNTIMEDECLARATION) {

			// Function name must not already have been used
			if (isLocalUserFunction(name)) {
				setError("'"
						+ name
						+ "' has already been used as a function/subroutine name");
				return false;
			}

			// Function name must not have been used for a runtime function
			if (isRuntimeFunction(name)) {
				setError("'"
						+ name
						+ "' has already been used as a runtime function/subroutine name");
				return false;
			}

			// Store prototype
			prototypes.add(userFuncPrototype);
			userFuncPrototype = new UserFuncPrototype();

			// Store runtime function
			runtimeFunctions.add(new com.basic4gl.compiler.RuntimeFunction(
					prototypes.size() - 1));

			// Map name to runtime function
			runtimeFunctionIndex.put(name, runtimeFunctions.size() - 1);
		} else if (funcType == UserFunctionType.UFT_IMPLEMENTATION) {

			// Function implementation

			// Create jump-past-function op-code
			functionJumpOver = vm.getInstructionCount();
			AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT, new Value(
					0)); // Jump target will be fixed up when "endfunction" is
			// compiled

			if (isRuntimeFunction(name)) {

				// Implementation of runtime function
				int index = runtimeFunctionIndex.get(name);
				RuntimeFunction runtimeFunction = vm.CurrentCodeBlock()
						.GetRuntimeFunction(index);

				// Check if already implemented
				if (runtimeFunction.functionIndex >= 0) {
					setError("Runtime function/sub '" + name
							+ "' has already been implemented");
					return false;
				}

				// Function must match runtime prototype
				if (!userFuncPrototype.matches(prototypes
						.get(runtimeFunctions.get(index).prototypeIndex))) {
					setError("Function/sub does not match its RUNTIME declaration");
					return false;
				}

				// Allocate new function
				prototypes.add(userFuncPrototype);
				userFuncPrototype = new UserFuncPrototype();
				functions.add(new UserFunc(prototypes.size() - 1, true, vm
						.getInstructionCount()));
				currentFunction = functions.size() - 1;

				// Map runtime function to implementation
				runtimeFunction.functionIndex = currentFunction;
			} else {
				if (isLocalUserFunction(name)) {

					// Function already DECLAREd.
					currentFunction = localUserFunctionIndex.get(name);

					// Must not be already implemented
					if (functions.get(currentFunction).mImplemented) {
						setError("'"
								+ name
								+ "' has already been used as a function/subroutine name");
						return false;
					}

					// Function prototypes must match
					if (!userFuncPrototype.matches(prototypes.get(functions
							.get(currentFunction).mPrototypeIndex))) {
						setError((String) "Function/subroutine does not match how it was DECLAREd");
						return false;
					}

					// Save updated function spec
					// Function starts at next offset
					functions.get(currentFunction).mImplemented = true;
					functions.get(currentFunction).mProgramOffset = vm
							.getInstructionCount();
				} else {

					// Completely new function

					// Allocate a new prototype
					prototypes.add(userFuncPrototype);
					userFuncPrototype = new UserFuncPrototype();

					// Allocate a new function
					functions.add(new UserFunc(prototypes.size() - 1, true,
							vm.getInstructionCount()));
					currentFunction = functions.size() - 1;
				}

				// Map name to function
				localUserFunctionIndex.put(name, currentFunction);
				visibleUserFunctionIndex.put(name, currentFunction);
				if (!isGlobalUserFunction(name)) {
					globalUserFunctionIndex.put(name, currentFunction);
				}
			}

			// Build reverse index (for debugger)
			userFunctionReverseIndex.put(currentFunction, name);

			// Compile the body of the function
			inFunction = true;
		}

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
		if (!GetToken()) {
			return false;
		}

		// If end of function is reached without a return value, need to trigger
		// a runtime error.
		if (getCurrentUserFunctionPrototype().hasReturnVal) {
			AddInstruction(OpCode.OP_NO_VALUE_RETURNED, BasicValType.VTP_INT,
					new Value(0));
		} else
			// Add return-from-user-function instruction
		{
			AddInstruction(OpCode.OP_RETURN_USER_FUNC, BasicValType.VTP_INT,
					new Value(0));
		}

		// Fix up jump-past-function op-code
		assertTrue(functionJumpOver < vm.getInstructionCount());
		vm.getInstruction(functionJumpOver).mValue.setIntVal(vm
				.getInstructionCount());

		// Let compiler know we have left the function
		inFunction = false;

		// TODO: If function is supposed to return a value, add an op-code that
		// triggers
		// a run-time error (meaning that the end of the function was reached
		// without
		// finding a "return" command).

		return true;
	}

	private boolean compileUserFunctionCall(boolean mustReturnValue,
			boolean isRuntimeFunc) {
		assertTrue((!isRuntimeFunc && token.tokenType == TokenType.CTT_USER_FUNCTION) || (isRuntimeFunc && token.tokenType == TokenType.CTT_RUNTIME_FUNCTION));
		assertTrue((!isRuntimeFunc && isUserFunction(token.text)) || (isRuntimeFunc && isRuntimeFunction(token.text)));

		// Read function name
		String name = token.text;
		if (!GetToken()) {
			return false;
		}

		// Lookup prototype
		int index;
		int prototypeIndex;
		if (isRuntimeFunc) {
			index = runtimeFunctionIndex.get(name);
			prototypeIndex = runtimeFunctions.get(index).prototypeIndex;
		} else {
			index = visibleUserFunctionIndex.get(name);
			prototypeIndex = vm.getUserFunctions().get(index).mPrototypeIndex;
		}
		UserFuncPrototype prototype = vm.getUserFunctionPrototypes().get(
				prototypeIndex);

		if (mustReturnValue && !prototype.hasReturnVal) {
			setError("'" + name + "' does not return a value");
			return false;
		}

		// Add op-code to prepare function stack frame.
		// Stack frame remains inactive while evaluating its parameters.
		if (isRuntimeFunc) {
			AddInstruction(OpCode.OP_CREATE_RUNTIME_FRAME,
					BasicValType.VTP_INT, new Value(index));
		} else {
			AddInstruction(OpCode.OP_CREATE_USER_FRAME, BasicValType.VTP_INT,
					new Value(index));
		}

		// Expect "("
		if (!token.text.equals("(")) {
			setError("Expected '('");
			return false;
		}
		if (!GetToken()) {
			return false;
		}

		// Evaluate function parameters
		boolean needComma = false;
		for (int i = 0; i < prototype.paramCount; i++) {
			if (needComma) {
				if (!token.text.equals(",")) {
					setError("Expected ','");
					return false;
				}
				if (!GetToken()) {
					return false;
				}
			}
			needComma = true;

			Mutable<UserFuncPrototype> funcRef = new Mutable<UserFuncPrototype>(
					prototype);
			if (!compileUserFuncParam(funcRef, i)) {
				return false;
			}
			// Update local value from reference
			prototype = funcRef.get();
		}

		// Expect ")"
		if (!token.text.equals(")")) {
			setError("Expected ')'");
			return false;
		}
		if (!GetToken()) {
			return false;
		}

		// Add op-code to call function.
		// Type: Unused.
		// Value: index of function specification.
		AddInstruction(OpCode.OP_CALL_USER_FUNC, BasicValType.VTP_INT,
				new Value());

		if (prototype.hasReturnVal) {

			// Data containing strings will need to be "destroyed" when the
			// stack unwinds.
			if (!prototype.returnValType.CanStoreInRegister()
					&& vm.getDataTypes().ContainsString(prototype.returnValType)) {
				AddInstruction(
						OpCode.OP_REG_DESTRUCTOR,
						BasicValType.VTP_INT,
						new Value((int) vm
								.getStoreTypeIndex(prototype.returnValType)));
			}

			// Set register type to value returned from function (if applies)
			regType.Set(prototype.returnValType.RegisterType());
			if (!compileDataLookup(false)) {
				return false;
			}

			// If function returns a value larger than the register, temp data
			// will
			// need to be freed.
			if (!prototype.returnValType.CanStoreInRegister()) {
				freeTempData = true;
			}
		}

		return true;
	}

	private boolean compileUserFuncParam(Mutable<UserFuncPrototype> prototype, int i) {

		// Generate code to store result as a function parameter
		ValType type = new ValType(prototype.get().localVarTypes.get(i));

		// Basic type case
		if (type.IsBasic()) {

			// Generate code to compile function parameter
			if (!compileExpression(false)) {
				return false;
			}

			// Attempt to convert value in reg to same type
			if (!compileConvert(type.m_basicType)) {
				setError("Types do not match");
				return false;
			}

			// Save reg into parameter
			AddInstruction(OpCode.OP_SAVE_PARAM, type.m_basicType,
					new Value(i));
		}

		// Pointer case. Parameter must be a pointer and m_reg must point to a
		// value accessible through a variable.
		else if (type.VirtualPointerLevel() == 1) {

			// Special case: We accept "null" to pointer parameters
			if (token.text.equals("null")) {
				if (!compileNull()) {
					return false;
				}
				AddInstruction(OpCode.OP_SAVE_PARAM, BasicValType.VTP_INT,
						new Value(i));
			} else {

				// Otherwise we implicitly take the address of any variable
				// passed in
				if (!compileLoadVar()) {
					return false;
				}

				if (!compileTakeAddress()) {
					return false;
				}

				// Register should now match the expected type
				if (regType.m_pointerLevel == type.m_pointerLevel
						&& regType.m_arrayLevel == type.m_arrayLevel
						&& regType.m_basicType == type.m_basicType) {
					AddInstruction(OpCode.OP_SAVE_PARAM,
							BasicValType.VTP_INT, new Value(i));
				} else {
					setError("Types do not match");
					return false;
				}
			}
		}

		// Not basic, and not a pointer.
		// Must be a large object (structure or array)
		else {
			assertTrue(type.m_pointerLevel == 0);

			// Generate code to compile function parameter
			if (!compileExpression(false)) {
				return false;
			}

			if (regType.m_pointerLevel == 1 && regType.m_byRef
					&& regType.m_arrayLevel == type.m_arrayLevel
					&& regType.m_basicType == type.m_basicType) {
				AddInstruction(OpCode.OP_COPY_USER_STACK,
						BasicValType.VTP_INT,
						new Value((int) vm.getStoreTypeIndex(type)));
				AddInstruction(OpCode.OP_SAVE_PARAM_PTR,
						BasicValType.VTP_INT, new Value(i));
			} else {
				setError("Types do not match");
				return false;
			}
		}

		// Data containing strings will need to be "destroyed" when the stack
		// unwinds.
		if (vm.getDataTypes().ContainsString(type)) {
			AddInstruction(OpCode.OP_REG_DESTRUCTOR, BasicValType.VTP_INT,
					new Value((int) vm.getStoreTypeIndex(type)));
		}

		return true;
	}

	private boolean compileReturn() {
		if (!GetToken()) {
			return false;
		}

		if (inFunction) {
			if (getCurrentUserFunctionPrototype().hasReturnVal) {
				ValType type = new ValType(getCurrentUserFunctionPrototype().returnValType);

				// Generate code to compile and return value
				if (!compileExpression()) {
					return false;
				}
				if (!compileConvert(type.RegisterType())) {
					return false;
				}

				// Basic values and pointers can be returned in the register
				if (!type.CanStoreInRegister()) {

					// Add instruction to move that data into temp data
					AddInstruction(OpCode.OP_MOVE_TEMP, BasicValType.VTP_INT,
							new Value((int) vm.getStoreTypeIndex(type)));

					// Add return-from-function OP-code
					// Note: The 0 in the instruction value indicates that temp
					// data should NOT be freed on return (as we have just moved
					// the return value there.)
					AddInstruction(OpCode.OP_RETURN_USER_FUNC,
							BasicValType.VTP_INT, new Value(0));
				} else
					// Add return-from-function OP-code
					// Note: The 1 in the instruction value indicates that temp
					// data should be freed on return.
				{
					AddInstruction(OpCode.OP_RETURN_USER_FUNC,
							BasicValType.VTP_INT, new Value(1));
				}
			} else {
				AddInstruction(OpCode.OP_RETURN_USER_FUNC,
						BasicValType.VTP_INT, new Value(1));
			}
		} else {
			// Add "return from Gosub" op-code
			AddInstruction(OpCode.OP_RETURN, BasicValType.VTP_INT,
					new Value());
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
				Streaming.WriteString(stream, key);
				programConstants.get(key).streamOut(stream);
			}
			Streaming.WriteString(stream, "");

			// Stream out labels
			for (String key : labels.keySet()) {
				Streaming.WriteString(stream, key);
				labels.get(key).streamOut(stream);
			}
			Streaming.WriteString(stream, "");

			// Stream out user function/subroutine names
			for (String key : globalUserFunctionIndex.keySet()) {
				Streaming.WriteString(stream, key);
				Streaming.WriteLong(stream, globalUserFunctionIndex.get(key));
			}
			Streaming.WriteString(stream, "");

			// Stream out runtime functions
			// Note that strictly speaking these aren't "names", but because
			// they are
			// required when name information is present, and not required when
			// it is
			// absent, we are bundling them into the same #ifdef
			Streaming.WriteLong(stream, runtimeFunctions.size());
			for (int i = 0; i < runtimeFunctions.size(); i++) {
				runtimeFunctions.get(i).streamOut(stream);
			}

			// Stream out runtime function names
			for (String key : runtimeFunctionIndex.keySet()) {
				Streaming.WriteString(stream, key);
				Streaming.WriteLong(stream, runtimeFunctionIndex.get(key));
			}
			Streaming.WriteString(stream, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean streamIn(DataInputStream stream) {
		try {

			// TODO Reimplement Libraries
			// Unload any plugins
			// m_plugins.Clear();

			// Clear current program (if any)
			clearProgram();

			// Stream in VM state
			if (!vm.streamIn(stream)) {
				setError(vm.getError());
				return false;
			}

			// Stream in constant names
			String name = Streaming.ReadString(stream);
			while (!name.equals("")) {

				// Read constant details
				Constant constant = new Constant();
				constant.streamIn(stream);

				// Store constant
				programConstants.put(name, constant);

				// Next constant
				name = Streaming.ReadString(stream);
			}

			// Stream in label names
			name = Streaming.ReadString(stream);

			while (!name.equals("")) {

				// Read label details
				Label label = new Label();
				label.streamIn(stream);

				// Store label
				labels.put(name, label);
				labelIndex.put(label.offset, name);

				// Next label
				name = Streaming.ReadString(stream);

			}

			// Stream in user function/subroutine names
			name = Streaming.ReadString(stream);
			while (!name.equals("")) {

				// Read function details
				int index = (int) Streaming.ReadLong(stream);

				// Store function index
				globalUserFunctionIndex.put(name, index);

				// Next function
				name = Streaming.ReadString(stream);
			}
			// Stream in runtime functions
			// Note that strictly speaking these aren't "names", but because
			// they are
			// required when name information is present, and not required when
			// it is
			// absent, we are bundling them into the same #ifdef
			int count = (int) Streaming.ReadLong(stream);
			runtimeFunctions.setSize(count);
			for (int i = 0; i < count; i++) {
				com.basic4gl.compiler.RuntimeFunction function = new com.basic4gl.compiler.RuntimeFunction();
				function.streamIn(stream);

				runtimeFunctions.set(i, function);
			}
			name = Streaming.ReadString(stream);
			while (!name.equals("")) {

				// Read runtime function details
				int index = (int) Streaming.ReadLong(stream);

				// Store runtime function index
				runtimeFunctionIndex.put(name, index);

				// Next function
				name = Streaming.ReadString(stream);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

}