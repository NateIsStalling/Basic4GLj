package com.basic4gl.compiler;

/*  Created 5-Sep-2003: Thomas Mulgrew

 Used to compile source code in BASIC language to TomVM Op codes.
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import com.basic4gl.compiler.Token.TokenType;
import com.basic4gl.compiler.compFlowControl.compFlowControlType;
import com.basic4gl.compiler.util.compBinOperExt;
import com.basic4gl.compiler.util.compUnOperExt;
import com.basic4gl.util.Cast;
import com.basic4gl.util.Mutable;
import com.basic4gl.util.Streaming;
import com.basic4gl.util.compExtFuncSpec;
import com.basic4gl.util.compFuncSpec;
import com.basic4gl.vm.HasErrorState;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.VMValue;
import com.basic4gl.vm.vmInstruction;
import com.basic4gl.vm.vmRollbackPoint;
import com.basic4gl.vm.stackframe.vmRuntimeFunction;
import com.basic4gl.vm.stackframe.vmUserFunc;
import com.basic4gl.vm.stackframe.vmUserFuncPrototype;
import com.basic4gl.vm.types.Structure;
import com.basic4gl.vm.types.StructureField;
import com.basic4gl.vm.types.ValType;
import com.basic4gl.vm.types.ValType.BasicValType;
import com.basic4gl.vm.util.Constants;
import com.basic4gl.vm.util.Function;
import com.basic4gl.vm.types.OpCode;

//TODO Reimplement libraries
//import com.basic4gl.nate.plugins.PluginDLLManager;
//import com.basic4gl.nate.plugins.compExtFuncSpec;

////////////////////////////////////////////////////////////////////////////////
// TomBasicCompiler
//
// Basic4GL v2 language compiler.

public class TomBasicCompiler extends HasErrorState {
	static final int TC_STEPSBETWEENREFRESH = 1000;
	static final int TC_MAXOVERLOADEDFUNCTIONS = 256; // Allow 256 functions of
	// the same name (should
	// be more than enough
	// for anything...)

	// //////////////////////////////////////////////////////////////////////////////
	// TomBasicCompiler
	//
	// Basic4GL v2 language compiler.

	// Virtual machine
	TomVM m_vm;

	// Parser
	compParser m_parser;

	// DLL manager
	// TODO Reimplement libraries
	// PluginDLLManager m_plugins;

	// Settings
	boolean m_caseSensitive;
	Map<String, compOperator> m_unaryOperators; // Recognised operators. Unary
	// have one operand (e.g NOT x)
	Map<String, compOperator> m_binaryOperators; // Binary have to (e.g. x + y)
	public ArrayList<String> m_reservedWords;
	Map<String, compConstant> m_constants; // Permanent constants.
	Map<String, compConstant> m_programConstants; // Constants declared using
	// the const command.
	Vector<compFuncSpec> m_functions;
	public Map<String, List<Integer>> m_functionIndex; // Maps function name to index
	// of function (in mFunctions
	// array)
	compLanguageSyntax m_syntax;
	String m_symbolPrefix = ""; // Prefix all symbols with this text

	// Compiler state
	ValType m_regType, m_reg2Type;
	Vector<ValType> m_operandStack;
	Vector<compStackedOperator> m_operatorStack;

	compStackedOperator getOperatorTOS() {
		return m_operatorStack.get(m_operatorStack.size() - 1);
	}

	void setOperatorTOS(compStackedOperator operator) {
		m_operatorStack.set(m_operatorStack.size() - 1, operator);
	}

	Map<String, compLabel> m_labels;
	Map<Integer, String> m_labelIndex;
	Vector<compJump> m_jumps; // Jumps to fix up
	Vector<compJump> m_resets; // Resets to fix up
	Vector<compFlowControl> m_flowControl; // Flow control structure stack
	Token m_token;
	boolean m_needColon; // True if next instruction must be separated by a
	// colon (or newline)
	boolean m_freeTempData; // True if need to generate code to free temporary
	// data before the next instruction
	int m_lastLine, m_lastCol;
	boolean m_inFunction;
	InstructionPos m_functionStart;
	int m_functionJumpOver;
	Map<String, Integer> m_globalUserFunctionIndex; // Maps function name to
	// index of function
	Map<String, Integer> m_localUserFunctionIndex; // Local user function index
	// (for the current code
	// block being compiled)
	Map<String, Integer> m_visibleUserFunctionIndex; // Combines local and
	// global (where a local
	// function overrides a
	// global one of the
	// same name)
	Map<Integer, String> m_userFunctionReverseIndex; // Index->Name lookup. For
	// debug views.
	int m_currentFunction; // Index of current active user function. Usually
	// this will be the last in the vm.UserFunctions()
	// vector,
	// can be different in special cases (e.g. when compiler is called from
	// debugger to evaluate an expression).
	vmUserFuncPrototype m_userFuncPrototype; // Prototype of function being
	// declared.
	Vector<compRuntimeFunction> m_runtimeFunctions;
	Map<String, Integer> m_runtimeFunctionIndex;

	// Language extension
	Vector<compUnOperExt> m_unOperExts; // Unary operator extensions
	Vector<compBinOperExt> m_binOperExts; // Binary operator extensions

	// //////////////////////////////////////////////////////////////////////////////
	// Internal compiler types

	// compOperator
	// Used for tracking which operators are about to be applied to operands.
	// Basic4GL converts infix expressions into reverse polish using an operator
	// stack and an operand stack.

	enum compOpType {
		OT_OPERATOR, OT_RETURNBOOLOPERATOR, OT_BOOLOPERATOR, OT_LAZYBOOLOPERATOR, OT_LBRACKET, OT_STOP // Forces
		// expression
		// evaluation
		// to
		// stop
	}

	class compOperator {
		compOpType mType;
		short mOpCode;
		int mParams; // 1 . Calculate "op Reg" (e.g. "Not Reg")
		// 2 . Calculate "Reg2 op Reg" (e.g. "Reg2 - Reg")
		int mBinding; // Operator binding. Higher = tighter.

		compOperator(compOpType type, short opCode, int params, int binding) {
			mType = type;
			mOpCode = opCode;
			mParams = params;
			mBinding = binding;
		}

		compOperator() {
			mType = compOpType.OT_OPERATOR;
			mOpCode = OpCode.OP_NOP;
			mParams = 0;
			mBinding = 0;
		}

		compOperator(compOperator o) {
			mType = o.mType;
			mOpCode = o.mOpCode;
			mParams = o.mParams;
			mBinding = o.mBinding;
		}
	}

	class compStackedOperator {
		compOperator mOper; // Stacked operator
		int mLazyJumpAddr; // Address of lazy jump op code (for "and" and "or"

		// operations)

		compStackedOperator(compOperator o) {
			mOper = o;
			mLazyJumpAddr = -1;
		}

		compStackedOperator(compOperator o, int lazyJumpAddr) {
			mOper = o;
			mLazyJumpAddr = lazyJumpAddr;
		}
	}

	// CompLabel
	// A program label, i.e. a named destination for "goto" and "gosub"s
	class compLabel {
		int m_offset; // Instruction index in code
		int m_programDataOffset; // Program data offset. (For use with

		// "RESET labelname" command.)

		compLabel(int offset, int dataOffset) {
			m_offset = offset;
			m_programDataOffset = dataOffset;
		}

		compLabel() {
			m_offset = 0;
			m_programDataOffset = 0;
		}

		// #ifdef VM_STATE_STREAMING
		void StreamOut(ByteBuffer buffer) {
			try {
				Streaming.WriteLong(buffer, m_offset);
				Streaming.WriteLong(buffer, m_programDataOffset);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		void StreamIn(ByteBuffer buffer) {
			try {
				m_offset = (int) Streaming.ReadLong(buffer);
				m_programDataOffset = (int) Streaming.ReadLong(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// #endif
	}

	// compJump
	// Used to track program jumps. Actuall addresses are patched into jump
	// instructions after the main compilation pass has completed. (Thus forward
	// jumps are possible.)
	class compJump {
		int m_jumpInstruction; // Instruction containing jump instruction
		String m_labelName; // Label to which we are jumping

		compJump(int instruction, String labelName) {
			m_jumpInstruction = instruction;
			m_labelName = labelName;
		}

		compJump() {
			m_jumpInstruction = 0;
			m_labelName = "";
		}
	}

	// Misc
	class compParserPos {
		int m_line;
		int m_col;
		Token m_token;
	}

	enum compLanguageSyntax {
		LS_TRADITIONAL(0), // As compatible as possible with other BASICs
		LS_BASIC4GL(1), // Standard Basic4GL syntax for backwards compatibility
		// with existing code.
		LS_TRADITIONAL_PRINT(2); // Traditional mode PRINT, but otherwise
		// standard Basic4GL syntax
		private int mType;

		compLanguageSyntax(int type) {
			mType = type;
		}

		public int getType() {
			return mType;
		}
	}

	enum compUserFunctionType {
		UFT_IMPLEMENTATION, UFT_FWDDECLARATION, UFT_RUNTIMEDECLARATION
	};

	// //////////////////////////////////////////////////////////////////////////////
	// compRollbackPoint
	//
	// / Allows the compiler to rollback cleanly if an error occurs during
	// / compilation. Used during runtime compilation to ensure the compiler
	// does
	// / not leave the VM in an unstable state.
	// /
	// / Note: Currently not everything is rolled back, just enough to keep the
	// VM
	// / stable. There may still be resources used (such as code instructions
	// / allocated), but they should be benign and unreachable.
	class compRollbackPoint {

		// Virtual machine rollback
		vmRollbackPoint vmRollback;

		// Runtime functions
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
		m_vm = vm;
		// TODO Reimplement libraries
		// m_plugins = plugins;
		m_caseSensitive = caseSensitive;
		m_syntax = compLanguageSyntax.LS_BASIC4GL;

		m_operandStack = new Vector<ValType>();
		m_operatorStack = new Vector<compStackedOperator>();
		m_jumps = new Vector<compJump>();
		m_resets = new Vector<compJump>();
		m_flowControl = new Vector<compFlowControl>();

		m_binaryOperators = new HashMap<String, compOperator>();
		m_unaryOperators = new HashMap<String, compOperator>();

		m_reservedWords = new ArrayList<String>();

		m_parser = new compParser();

		m_programConstants = new HashMap<String, compConstant>();
		m_labels = new HashMap<String, compLabel>();
		m_labelIndex = new HashMap<Integer, String>();

		m_functionIndex = new HashMap<String, List<Integer>>();
		m_constants = new HashMap<String, compConstant>();

		m_localUserFunctionIndex = new HashMap<String, Integer>();
		m_globalUserFunctionIndex = new HashMap<String, Integer>();
		m_visibleUserFunctionIndex = new HashMap<String, Integer>();
		m_userFunctionReverseIndex = new HashMap<Integer, String>();
		m_runtimeFunctionIndex = new HashMap<String, Integer>();
		m_runtimeFunctions = new Vector<compRuntimeFunction>();
		m_functions = new Vector<compFuncSpec>();

		m_unOperExts = new Vector<compUnOperExt>();
		m_binOperExts = new Vector<compBinOperExt>();

		ClearState();

		// Setup operators
		// Note: From experimentation it appears QBasic binds "xor" looser than
		// "and" and "or". So for compatibility, we will too..
		m_binaryOperators.put("xor", new compOperator(
				compOpType.OT_BOOLOPERATOR, OpCode.OP_OP_XOR, 2, 10));
		m_binaryOperators.put("or", new compOperator(
				compOpType.OT_BOOLOPERATOR, OpCode.OP_OP_OR, 2, 11));
		m_binaryOperators.put("and", new compOperator(
				compOpType.OT_BOOLOPERATOR, OpCode.OP_OP_AND, 2, 12));
		m_binaryOperators.put("lor", new compOperator(
				compOpType.OT_LAZYBOOLOPERATOR, OpCode.OP_OP_OR, 2, 11));
		m_binaryOperators.put("land", new compOperator(
				compOpType.OT_LAZYBOOLOPERATOR, OpCode.OP_OP_AND, 2, 12));
		m_unaryOperators.put("not", new compOperator(
				compOpType.OT_BOOLOPERATOR, OpCode.OP_OP_NOT, 1, 20));
		m_binaryOperators.put("=", new compOperator(
				compOpType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_EQUAL, 2, 30));
		m_binaryOperators.put("<>", new compOperator(
				compOpType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_NOT_EQUAL, 2,
				30));
		m_binaryOperators.put(">",
				new compOperator(compOpType.OT_RETURNBOOLOPERATOR,
						OpCode.OP_OP_GREATER, 2, 30));
		m_binaryOperators.put(">=", new compOperator(
				compOpType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_GREATER_EQUAL,
				2, 30));
		m_binaryOperators.put("<", new compOperator(
				compOpType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_LESS, 2, 30));
		m_binaryOperators.put("<=", new compOperator(
				compOpType.OT_RETURNBOOLOPERATOR, OpCode.OP_OP_LESS_EQUAL, 2,
				30));
		m_binaryOperators.put("+", new compOperator(compOpType.OT_OPERATOR,
				OpCode.OP_OP_PLUS, 2, 40));
		m_binaryOperators.put("-", new compOperator(compOpType.OT_OPERATOR,
				OpCode.OP_OP_MINUS, 2, 40));
		m_binaryOperators.put("*", new compOperator(compOpType.OT_OPERATOR,
				OpCode.OP_OP_TIMES, 2, 41));
		m_binaryOperators.put("/", new compOperator(compOpType.OT_OPERATOR,
				OpCode.OP_OP_DIV, 2, 42));
		m_binaryOperators.put("%", new compOperator(compOpType.OT_OPERATOR,
				OpCode.OP_OP_MOD, 2, 43));
		m_unaryOperators.put("-", new compOperator(compOpType.OT_OPERATOR,
				OpCode.OP_OP_NEG, 1, 50));

		// Setup reserved words
		m_reservedWords.add( "dim");
		m_reservedWords.add( "goto");
		m_reservedWords.add( "if");
		m_reservedWords.add( "then");
		m_reservedWords.add( "elseif");
		m_reservedWords.add( "else");
		m_reservedWords.add( "endif");
		m_reservedWords.add( "end");
		m_reservedWords.add( "gosub");
		m_reservedWords.add( "return");
		m_reservedWords.add( "for");
		m_reservedWords.add( "to");
		m_reservedWords.add( "step");
		m_reservedWords.add( "next");
		m_reservedWords.add( "while");
		m_reservedWords.add( "wend");
		m_reservedWords.add( "run");
		m_reservedWords.add( "struc");
		m_reservedWords.add( "endstruc");
		m_reservedWords.add( "const");
		m_reservedWords.add( "alloc");
		m_reservedWords.add( "null");
		m_reservedWords.add( "data");
		m_reservedWords.add( "read");
		m_reservedWords.add( "reset");
		m_reservedWords.add( "type"); // QBasic/Freebasic compatibility
		m_reservedWords.add( "as"); //
		m_reservedWords.add( "integer"); //
		m_reservedWords.add( "single"); //
		m_reservedWords.add( "double"); //
		m_reservedWords.add( "string"); //
		m_reservedWords.add( "language"); // Language syntax
		m_reservedWords.add( "traditional");
		m_reservedWords.add( "basic4gl");
		m_reservedWords.add( "traditional_print");
		m_reservedWords.add( "input");
		m_reservedWords.add( "do");
		m_reservedWords.add( "loop");
		m_reservedWords.add( "until");
		m_reservedWords.add( "function");
		m_reservedWords.add( "sub");
		m_reservedWords.add( "endfunction");
		m_reservedWords.add( "endsub");
		m_reservedWords.add( "declare");
		m_reservedWords.add( "runtime");
		m_reservedWords.add( "bindcode");
		m_reservedWords.add( "exec");
	}

	void ClearState() {
		m_regType = new ValType(BasicValType.VTP_INT);
		m_reg2Type = new ValType(BasicValType.VTP_INT);
		m_freeTempData = false;
		m_operandStack.clear();
		m_operatorStack.clear();
		m_jumps.clear();
		m_resets.clear();
		m_flowControl.clear();
		m_syntax = compLanguageSyntax.LS_BASIC4GL;
		m_inFunction = false;

		// No local user functions defined initially.
		// Visible functions are the global functions.
		m_localUserFunctionIndex.clear();
		m_visibleUserFunctionIndex = m_globalUserFunctionIndex;
	}

	public void New() {

		// Clear existing program
		m_vm.New();
		m_lastLine = 0;
		m_lastCol = 0;

		// Clear state
		m_globalUserFunctionIndex.clear();
		m_userFunctionReverseIndex.clear();
		m_runtimeFunctionIndex.clear();
		ClearState();
		m_parser.Reset();
		m_programConstants.clear();
		m_labels.clear();
		m_labelIndex.clear();
		m_currentFunction = -1;
		// TODO Reimplement libraries
		// InitPlugins();
		m_runtimeFunctions.clear();

	}

	public boolean Compile() {

		// Clear existing program
		New();

		// Compile source code
		InternalCompile();

		return !Error();
	}

	public boolean CompileOntoEnd() {

		// Compile code and append to end of program.
		// Like ::Compile(), but does not clear out the existing program first.
		ClearState();
		m_lastLine = 0;
		m_lastCol = 0;
		m_parser.Reset();
		InternalCompile();
		return !Error();
	}

	boolean CheckParser() {
		// Check parser for error
		// Copy error state (if any)
		if (m_parser.Error()) {
			SetError("Parse error: " + m_parser.GetError());
			m_parser.ClearError();
			return false;
		}
		return true;
	}

	// TODO Reimplement libraries
	/*
	 * void InitPlugins() {
	 * m_plugins.StructureManager().AddVMStructures(m_vm.DataTypes());
	 * m_plugins.CreateVMFunctionSpecs(); }
	 */

	boolean GetToken() {
		return GetToken(false, false);
	}

	boolean GetToken(boolean skipEOL, boolean dataMode) {

		// Read a token
		m_token = m_parser.NextToken(skipEOL, dataMode);
		if (!CheckParser())
			return false;

		// Text token processing
		if (m_token.m_type == TokenType.CTT_TEXT) {

			// Apply case sensitivity
			if (!m_caseSensitive)
				m_token.m_text = m_token.m_text.toLowerCase();

			// Match against reserved keywords
			if (m_reservedWords.contains(m_token.m_text))
				m_token.m_type = TokenType.CTT_KEYWORD;

			// Match against external functions
			else if (IsFunction(m_token.m_text))
				m_token.m_type = TokenType.CTT_FUNCTION;

			else if (IsUserFunction(m_token.m_text))
				m_token.m_type = TokenType.CTT_USER_FUNCTION;

			else if (IsRuntimeFunction(m_token.m_text))
				m_token.m_type = TokenType.CTT_RUNTIME_FUNCTION;

			else {

				// Match against constants

				// Try permanent constants first
				compConstant constant;
				boolean isConstant;
				constant = m_constants.get(m_token.m_text);
				isConstant = (constant != null);

				// Try plugin DLL constants next
				// TODO Reimplement libraries
				// if (!isConstant)
				// isConstant = m_plugins.FindConstant(m_token.m_text,
				// constant);

				// Otherwise try program constants
				if (!isConstant) {
					constant = m_programConstants.get(m_token.m_text);
					isConstant = (constant != null);
				}

				if (isConstant) {
					// Replace token with constant
					m_token.m_type = TokenType.CTT_CONSTANT;

					// Replace text with text value of constant
					switch (constant.mValType) {
					case VTP_INT:
						m_token.m_text = String.valueOf(constant.mIntVal);
						break;
					case VTP_REAL:
						m_token.m_text = String.valueOf(constant.mRealVal);
						break;
					case VTP_STRING:
						m_token.m_text = constant.mStringVal;
						break;
					default:
						break;
					}
					m_token.m_valType = constant.mValType;
				}
			}
		} else if (m_token.m_type == TokenType.CTT_CONSTANT
				&& m_token.m_valType == BasicValType.VTP_STRING) {

			// 19-Jul-2003
			// Prefix string text constants with "S". This prevents them
			// matching
			// any recognised keywords (which are stored in lower case).
			// (This is basically a work around to existing code which doesn't
			// check the token type if it matches a reserved keyword).
			m_token.m_text = (String) "S" + m_token.m_text;
		}
		return true;
	}

	void InternalCompile() {

		// Allocate a new code block
		m_vm.NewCodeBlock();

		// Clear error state
		ClearError();
		m_parser.ClearError();

		// Read initial token
		if (!GetToken(true, false))
			return;

		// Compile code
		while (!m_parser.Eof() && CompileInstruction())
			;

		// Terminate program
		AddInstruction(OpCode.OP_END, BasicValType.VTP_INT.getType(), new VMValue());

		if (!Error()) {
			// Link up gotos
			for (compJump jump : m_jumps) {

				// Find instruction
				assert (jump.m_jumpInstruction < m_vm.InstructionCount());
				vmInstruction instr = m_vm.Instruction(jump.m_jumpInstruction);

				// Point token to goto instruction, so that it will be displayed
				// if there is an error.
				m_token.m_line = instr.mSourceLine;
				m_token.m_col = instr.mSourceChar;

				// Label must exist
				if (!LabelExists(jump.m_labelName)) {
					SetError((String) "Label: " + jump.m_labelName
							+ " does not exist");
					return;
				}

				// Patch in offset
				instr.mValue.setIntVal(Label(jump.m_labelName).m_offset);
			}

			// Link up resets
			for (compJump jump : m_resets) {

				// Find instruction
				assert (jump.m_jumpInstruction < m_vm.InstructionCount());
				vmInstruction instr = m_vm.Instruction(jump.m_jumpInstruction);

				// Point token to reset instruction, so that it will be
				// displayed
				// if there is an error.
				m_token.m_line = instr.mSourceLine;
				m_token.m_col = instr.mSourceChar;

				// Label must exist
				if (!LabelExists(jump.m_labelName)) {
					SetError((String) "Label: " + jump.m_labelName
							+ " does not exist");
					return;
				}

				// Patch in data offset
				instr.mValue
				.setIntVal(Label(jump.m_labelName).m_programDataOffset);
			}

			// Check for open function or flow control structures
			if (!CheckUnclosedUserFunction())
				return;

			if (!CheckUnclosedFlowControl())
				return;

			// Check for not implemented forward declared functions
			if (!CheckFwdDeclFunctions())
				return;
		}
	}

	boolean NeedAutoEndif() {

		// Look at the top-most control structure
		if (m_flowControl.isEmpty())
			return false;

		compFlowControl top = FlowControlTOS();

		// Auto endif required if top-most flow control is a non-block "if" or
		// "else"
		return (top.m_type == compFlowControlType.FCT_IF || top.m_type == compFlowControlType.FCT_ELSE)
				&& !top.m_blockIf;
	}

	public boolean IsFunction(String name) {
		// TODO Reimplement libraries
		return IsBuiltinFunction(name);// || m_plugins.IsPluginFunction(name);
	}

	public TomVM VM() {
		return m_vm;
	}

	public compParser Parser() {
		return m_parser;
	}

	public boolean CaseSensitive() {
		return m_caseSensitive;
	}

	// TODO Reimplement libraries
	// public PluginDLLManager Plugins() {
	// return m_plugins;
	// }

	// ////////////////////
	// Language extension

	// Constants
	public void AddConstant(String name, compConstant c) {
		m_constants.put(name.toLowerCase(), c);
	}

	public void AddConstant(String name, String s) {
		AddConstant(name, new compConstant(s));
	}

	public void AddConstant(String name, int i) {
		AddConstant(name, new compConstant(i));
	}

	public void AddConstant(String name, long i) {
		AddConstant(name, new compConstant(i));
	}

	public void AddConstant(String name, float r) {
		AddConstant(name, new compConstant(r));
	}

	public void AddConstant(String name, double r) {
		AddConstant(name, new compConstant(r));
	}

	public Map<String, compConstant> Constants() {
		return m_constants;
	}

	// Functions
	public boolean IsBuiltinFunction(String name) {
		return m_functionIndex.containsKey(name.toLowerCase());
		// Multimap<String,Integer>.iterator i = m_functionIndex.find
		// (LowerCase (name));
		// return i != m_functionIndex.lastElement() && i.first == LowerCase
		// (name);
	}

	public boolean IsUserFunction(String name) {
		return m_visibleUserFunctionIndex.containsKey(name.toLowerCase());
	}

	public boolean IsLocalUserFunction(String name) {
		return m_localUserFunctionIndex.containsKey(name.toLowerCase());
	}

	public boolean IsGlobalUserFunction(String name) {
		return m_globalUserFunctionIndex.containsKey(name.toLowerCase());
	}

	public boolean IsRuntimeFunction(String name) {
		return m_runtimeFunctionIndex.containsKey(name.toLowerCase());
	}

	public Vector<compFuncSpec> Functions() {
		return m_functions;
	}

	public Map<String, List<Integer>> FunctionIndex() {
		return m_functionIndex;
	}

	// Language extension
	public void AddUnOperExt(compUnOperExt e) {
		m_unOperExts.add(e);
	}

	public void AddBinOperExt(compBinOperExt e) {
		m_binOperExts.add(e);
	}

	// Language features (for context highlighting)
	public boolean IsReservedWord(String text) {
		return m_reservedWords.contains(text);
	}

	public boolean IsConstant(String text) {

		// Check built in constants
		if (m_constants.containsKey(text))
			return true;

		// Check DLL constants
		// TODO Reimplement libraries
		// compConstant compConst = new compConstant();
		// return (m_plugins.FindConstant(text, compConst));
		return false; // Remove line after libraries are reimplemented
	}

	public boolean IsBinaryOperator(String text) {
		return m_binaryOperators.containsKey(text);
	}

	public boolean IsUnaryOperator(String text) {
		return m_unaryOperators.containsKey(text);
	}

	public boolean IsOperator(String text) {
		return IsBinaryOperator(text) || IsUnaryOperator(text);
	}

	public long Line() {
		return m_token.m_line;
	}

	public long Col() {
		return m_token.m_col;
	}

	// //////////
	// Settings
	public compLanguageSyntax Syntax() {
		return m_syntax;
	}

	public String getSymbolPrefix() {
		return m_symbolPrefix;
	}

	public void setSymbolPrefix(String prefix) {
		m_symbolPrefix = prefix;
	}

	// Misc
	boolean LabelExists(String labelText) {
		return m_labels.containsKey(labelText);
	}

	compLabel Label(String labelText) {
		assert (LabelExists(labelText));
		return m_labels.get(labelText);
	}

	void AddLabel(String labelText, compLabel label) {
		assert (!LabelExists(labelText));
		m_labels.put(labelText, label);
		m_labelIndex.put(label.m_offset, labelText);
	}

	compFlowControl FlowControlTOS() {
		assert (!m_flowControl.isEmpty());
		return m_flowControl.get(m_flowControl.size() - 1);
	}

	boolean FlowControlTopIs(compFlowControlType type) {
		return !m_flowControl.isEmpty() && FlowControlTOS().m_type == type;
	}

	// TODO rename
	vmUserFunc _UserFunc() {
		// Return function currently being declared
		assert (m_vm.UserFunctions().size() > 0);
		assert (m_currentFunction >= 0);
		assert (m_currentFunction < m_vm.UserFunctions().size());
		return m_vm.UserFunctions().get(m_currentFunction);
	}

	vmUserFuncPrototype UserPrototype() {
		// Return prototype of function currently being declared
		assert (m_vm.UserFunctionPrototypes().size() > 0);
		assert (_UserFunc().prototypeIndex >= 0);
		assert (_UserFunc().prototypeIndex < m_vm.UserFunctionPrototypes()
				.size());
		return m_vm.UserFunctionPrototypes().get(_UserFunc().prototypeIndex);
	}

	boolean InternalCompileBindCode() {

		// Evaluate code handle
		if (!CompileExpression())
			return false;

		if (!CompileConvert(BasicValType.VTP_INT.getType()))
			return false;

		// Add bindcode op-code
		AddInstruction(OpCode.OP_BINDCODE, BasicValType.VTP_INT.getType(),
				new VMValue());

		return true;
	}

	boolean CompileBindCode() {

		// Skip "bindcode" keyword
		if (!GetToken())
			return false;

		// Combine bind code
		return InternalCompileBindCode();
	}

	boolean CompileExec() {

		// Skip "exec" keyword
		if (!GetToken())
			return false;

		// Check if explicit code block specified
		if (!AtSeparatorOrSpecial())
			if (!InternalCompileBindCode())
				return false;

		// Add exec op-code
		AddInstruction(OpCode.OP_EXEC, BasicValType.VTP_INT.getType(), new VMValue());

		return true;
	}

	compRollbackPoint GetRollbackPoint() {
		compRollbackPoint r = new compRollbackPoint();

		// Get virtual machine rollback info
		r.vmRollback = m_vm.GetRollbackPoint();

		// Get compiler rollback info
		r.runtimeFunctionCount = m_runtimeFunctions.size();

		return r;
	}

	void Rollback(compRollbackPoint rollbackPoint) {

		// Rollback virtual machine
		m_vm.Rollback(rollbackPoint.vmRollback);

		// Rollback compiler

		// Remove new labels
		// (We can detect these as any labels with an offset past the instruction
		// count stored in the rollback).
		for(Iterator<Map.Entry<String, compLabel>> it = m_labels.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, compLabel> entry = it.next();
			if (entry.getValue().m_offset >= rollbackPoint.vmRollback.instructionCount){
				it.remove();
			}
		}


		// Remove global function name->index records
		// (Can detect them as any global function with an invalid function index)
		for(Iterator<Map.Entry<String, Integer>> it = m_globalUserFunctionIndex.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Integer> entry = it.next();
			if (entry.getValue() >= rollbackPoint.vmRollback.functionCount){
				it.remove();
			}
		}


		// Remove function index->name records (used for debugging)
		for(Iterator<Map.Entry<Integer, String>> it = m_userFunctionReverseIndex.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Integer, String> entry = it.next();
			if (entry.getKey() >= rollbackPoint.vmRollback.functionCount){
				it.remove();
			}
		}

		// Remove runtime functions
		m_runtimeFunctions.setSize(rollbackPoint.runtimeFunctionCount);

		for(Iterator<Map.Entry<String, Integer>> it = m_runtimeFunctionIndex.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, Integer> entry = it.next();
			if (entry.getValue() >= rollbackPoint.runtimeFunctionCount){
				it.remove();
			}
		}
	}

	boolean CheckUnclosedFlowControl() {

		// Check for open flow control structures
		if (!m_flowControl.isEmpty()) {

			// Find topmost structure
			compFlowControl top = FlowControlTOS();

			// Point parser to it
			m_parser.SetPos(top.m_sourcePos.m_sourceLine,
					top.m_sourcePos.m_sourceCol);

			// Set error message
			switch (FlowControlTOS().m_type) {
			case FCT_IF:
				SetError("'if' without 'endif'");
				break;
			case FCT_ELSE:
				SetError("'else' without 'endif'");
				break;
			case FCT_FOR:
				SetError("'for' without 'next'");
				break;
			case FCT_WHILE:
				SetError("'while' without 'wend'");
				break;
			case FCT_DO_PRE:
			case FCT_DO_POST:
				SetError("'do' without 'loop'");
				break;
			default:
				SetError("Open flow control structure");
				break;
			}

			return false;
		} else
			return true;
	}

	boolean CheckUnclosedUserFunction() {
		if (m_inFunction) {

			// Point parser to function
			m_parser.SetPos(m_functionStart.m_sourceLine,
					m_functionStart.m_sourceCol);

			// Return error
			if (UserPrototype().hasReturnVal)
				SetError("'function' without 'endfunction'");
			else
				SetError("'sub' without 'endsub'");
			return false;
		} else
			return true;
	}

	boolean CheckFwdDeclFunctions() {

		// Look for function that is declared, but not yet implemented
		for (String key : m_localUserFunctionIndex.keySet()) {
			if (!m_vm.UserFunctions().get(m_localUserFunctionIndex.get(key)).implemented) {
				SetError((String) "Function/sub '" + key
						+ "' was DECLAREd, but not implemented");
				return false;
			}
		}

		return true;
	}

	// Compilation
	void AddInstruction(short opCode, int basictype, VMValue val) {

		// Add instruction, and include source code position audit
		int line = m_token.m_line, col = m_token.m_col;

		// Prevent line and col going backwards. (Debugging tools rely on
		// source-
		// offsets never decreasing as source code is traversed.)
		if (line < m_lastLine || (line == m_lastLine && col < m_lastCol)) {
			line = m_lastLine;
			col = m_lastCol;
		}
		m_vm.AddInstruction(new vmInstruction(opCode, (byte)basictype, val, line, col));
		m_lastLine = line;
		m_lastCol = col;
	}

	boolean CompileInstruction() {
		m_needColon = true; // Instructions by default must be separated by
		// colons

		// Is it a label?
		Token nextToken = m_parser.PeekToken(false, false);
		if (!CheckParser())
			return false;
		if (m_token.m_newLine && m_token.m_type == TokenType.CTT_TEXT
				&& nextToken.m_text.equals(":")) {

			// Labels cannot exist inside subs/functions
			if (m_inFunction) {
				SetError((String) "You cannot use a label inside a function or subroutine");
				return false;
			}

			// Label declaration
			String labelName = m_symbolPrefix + m_token.m_text;

			// Must not already exist
			if (LabelExists(labelName)) {
				SetError("Duplicate label name: " + labelName);
				return false;
			}

			// Create new label
			AddLabel(labelName, new compLabel(m_vm.InstructionCount(), m_vm
					.ProgramData().size()));

			// Skip label
			if (!GetToken())
				return false;
		}
		// Determine the type of instruction, based on the current token
		else if (m_token.m_text.equals("struc")
				|| m_token.m_text.equals("type")) {
			if (!CompileStructure())
				return false;
		} else if (m_token.m_text.equals("dim")) {
			if (!CompileDim(false, false))
				return false;
		} else if (m_token.m_text.equals("goto")) {
			if (!GetToken())
				return false;
			if (!CompileGoto(OpCode.OP_JUMP))
				return false;
		} else if (m_token.m_text.equals("gosub")) {
			if (!GetToken())
				return false;
			if (!CompileGoto(OpCode.OP_CALL))
				return false;
		} else if (m_token.m_text.equals("return")) {
			if (!CompileReturn())
				return false;
		} else if (m_token.m_text.equals("if")) {
			if (!CompileIf(false))
				return false;
		} else if (m_token.m_text.equals("elseif")) {
			if (!(CompileElse(true) && CompileIf(true)))
				return false;
		} else if (m_token.m_text.equals("else")) {
			if (!CompileElse(false))
				return false;
		} else if (m_token.m_text.equals("endif")) {
			if (!CompileEndIf(false))
				return false;
		} else if (m_token.m_text.equals("for")) {
			if (!CompileFor())
				return false;
		} else if (m_token.m_text.equals("next")) {
			if (!CompileNext())
				return false;
		} else if (m_token.m_text.equals("while")) {
			if (!CompileWhile())
				return false;
		} else if (m_token.m_text.equals("wend")) {
			if (!CompileWend())
				return false;
		} else if (m_token.m_text.equals("do")) {
			if (!CompileDo())
				return false;
		} else if (m_token.m_text.equals("loop")) {
			if (!CompileLoop())
				return false;
		} else if (m_token.m_text.equals("end")) {
			if (!GetToken())
				return false;

			// Special case! "End" immediately followed by "if" is syntactically
			// equivalent to "endif"
			if (m_token.m_text.equals("if")) {
				if (!CompileEndIf(false))
					return false;
			}
			// Special case! "End" immediately followed by "function" is
			// syntactically equivalent to "endfunction"
			else if (m_token.m_text.equals("function")) {
				if (!CompileEndUserFunction(true))
					return false;
			} else if (m_token.m_text.equals("sub")) {
				if (!CompileEndUserFunction(false))
					return false;
			} else
				// Otherwise is "End" program instruction
				AddInstruction(OpCode.OP_END, BasicValType.VTP_INT.getType(),
						new VMValue());
		} else if (m_token.m_text.equals("run")) {
			if (!GetToken())
				return false;
			AddInstruction(OpCode.OP_RUN, BasicValType.VTP_INT.getType(), new VMValue());
		} else if (m_token.m_text.equals("const")) {
			if (!CompileConstant())
				return false;
		} else if (m_token.m_text.equals("alloc")) {
			if (!CompileAlloc())
				return false;
		} else if (m_token.m_text.equals("data")) {
			if (!CompileData())
				return false;
		} else if (m_token.m_text.equals("read")) {
			if (!CompileDataRead())
				return false;
		} else if (m_token.m_text.equals("reset")) {
			if (!CompileDataReset())
				return false;
		//TODO Remove compile input and move to library
		} else if (m_token.m_text.equals("input")) {
			if (!CompileInput())
				return false;
		} else if (m_token.m_text.equals("language")) {
			if (!CompileLanguage())
				return false;
		} else if (m_token.m_text.equals("function")
				|| m_token.m_text.equals("sub")) {
			if (!CompileUserFunction(compUserFunctionType.UFT_IMPLEMENTATION))
				return false;
		} else if (m_token.m_text.equals("endfunction")) {
			if (!CompileEndUserFunction(true))
				return false;
		} else if (m_token.m_text.equals("endsub")) {
			if (!CompileEndUserFunction(false))
				return false;
		} else if (m_token.m_text.equals("declare")) {
			if (!CompileUserFunctionFwdDecl())
				return false;
		} else if (m_token.m_text.equals("runtime")) {
			if (!CompileUserFunctionRuntimeDecl())
				return false;
		} else if (m_token.m_text.equals("bindcode")) {
			if (!CompileBindCode())
				return false;
		} else if (m_token.m_text.equals("exec")) {
			if (!CompileExec())
				return false;

		} else if (m_token.m_type == TokenType.CTT_FUNCTION) {
			if (!CompileFunction())
				return false;
		} else if (m_token.m_type == TokenType.CTT_USER_FUNCTION) {
			if (!CompileUserFunctionCall(false, false))
				return false;
		} else if (m_token.m_type == TokenType.CTT_RUNTIME_FUNCTION) {
			if (!CompileUserFunctionCall(false, true))
				return false;
		} else if (!CompileAssignment())
			return false;

		// Free any temporary data (if necessary)
		if (!CompileFreeTempData())
			return false;

		// Skip separators (:, EOL)
		if (!SkipSeparators())
			return false;

		return true;
	}

	boolean AtSeparator() {
		return m_token.m_type == TokenType.CTT_EOL
				|| m_token.m_type == TokenType.CTT_EOF
				|| m_token.m_text.equals(":");
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
		return AtSeparator() || m_token.m_text.equals("endif")
				|| m_token.m_text.equals("else")
				|| m_token.m_text.equals("elseif");
	}

	boolean SkipSeparators() {

		// Expect separator. Either as an EOL or EOF or ':'
		if (m_needColon && !AtSeparatorOrSpecial()) {
			SetError("Expected ':'");
			return false;
		}

		// Skip separators
		while (m_token.m_type == TokenType.CTT_EOL
				|| m_token.m_type == TokenType.CTT_EOF
				|| m_token.m_text.equals(":")) {

			// If we reach the end of the line, insert any necessary implicit
			// "endifs"
			if (m_token.m_type == TokenType.CTT_EOL
					|| m_token.m_type == TokenType.CTT_EOF) {

				// Generate any automatic endifs for the previous line
				while (NeedAutoEndif())
					if (!CompileEndIf(true))
						return false;

				// Convert any remaining flow control structures to block
				for (compFlowControl flowControl : m_flowControl)
					flowControl.m_blockIf = true;
			}

			if (!GetToken(true, false))
				return false;
		}

		return true;
	}

	boolean CompileStructure() {

		// Skip STRUC
		String keyword = m_token.m_text; // Record whether "struc" or "type" was
		// used to declare type
		if (!GetToken())
			return false;

		// Check that we are not already inside a function
		if (m_inFunction) {
			SetError("Cannot define a structure inside a function or subroutine");
			return false;
		}

		// Check that we are not inside a control structure
		if (!CheckUnclosedFlowControl())
			return false;

		// Expect structure name
		if (m_token.m_type != TokenType.CTT_TEXT) {
			SetError("Expected structure name");
			return false;
		}
		String name = m_symbolPrefix + m_token.m_text;
		if (!CheckName(name))
			return false;
		if (m_vm.DataTypes().StrucStored(name)) { // Must be unused
			SetError("'" + name + "' has already been used as a structure name");
			return false;
		}
		if (!GetToken()) // Skip structure name
			return false;

		if (!SkipSeparators()) // Require : or new line
			return false;

		// Create structure
		m_vm.DataTypes().NewStruc(name);

		// Expect at least one field
		if (m_token.m_text.equals("endstruc") || m_token.m_text.equals("end")) {
			SetError("Expected DIM or field name");
			return false;
		}

		// Populate with fields
		while (!m_token.m_text.equals("endstruc")
				&& !m_token.m_text.equals("end")) {

			// dim statement is optional
			/*
			 * if (m_token.m_text != "dim") { SetError
			 * ("Expected 'dim' or 'endstruc'"); return false; }
			 */
			if (!CompileDim(true, false))
				return false;

			if (!SkipSeparators())
				return false;
		}

		if (m_token.m_text.equals("end")) {

			// Skip END
			if (!GetToken())
				return false;

			// Check END keyword matches declaration keyword
			if (!m_token.m_text.equals(keyword)) {
				SetError("Expected '" + keyword + "'");
				return false;
			}

			// Skip STRUC/TYPE
			if (!GetToken())
				return false;
		} else {

			// Make sure "struc" was declared
			if (!keyword.equals("struc")) {
				SetError("Expected 'END '" + keyword + "'");
				return false;
			}

			// Skip ENDSTRUC
			if (!GetToken())
				return false;
		}
		return true;
	}

	boolean CompileDim(boolean forStruc, boolean forFuncParam) {

		// Skip optional DIM
		if (m_token.m_text.equals("dim"))
			if (!GetToken())
				return false;

		// Expect at least one field in dim
		if (AtSeparatorOrSpecial()) {
			SetError("Expected variable declaration");
			return false;
		}

		// Parse fields in dim
		boolean needComma = false; // First element doesn't need a comma
		while (!AtSeparatorOrSpecial()
				&& (!forFuncParam || !m_token.m_text.equals(")"))) {

			// Handle commas
			if (needComma) {
				if (!m_token.m_text.equals(",")) {
					SetError("Expected ','");
					return false;
				}
				if (!GetToken())
					return false;
			}
			needComma = true; // Remaining elements do need commas

			// Extract field type
			String name = "";
			ValType type = new ValType();
			// Create wrappers to pass values by reference
			Mutable<String> nameRef = new Mutable<String>(name);
			Mutable<ValType> typeRef = new Mutable<ValType>(type);
			if (!CompileDimField(nameRef, typeRef, forStruc, forFuncParam))
				return false;
			// Update local values from wrappers
			name = nameRef.get();
			type = typeRef.get();

			if (!CheckName(name))
				return false;

			if (forStruc) {

				// Validate field name and type
				Structure struc = m_vm.DataTypes().CurrentStruc();
				if (m_vm.DataTypes().FieldStored(struc, name)) {
					SetError((String) "Field '" + name
							+ "' has already been DIMmed in structure '"
							+ struc.m_name + "'");
					return false;
				}
				if (type.m_pointerLevel == 0
						&& type.m_basicType == m_vm.DataTypes()
						.GetStruc(struc.m_name)) {
					SetError("Structure cannot contain an element of its own type");
					return false;
				}

				// Add field to structure
				m_vm.DataTypes().NewField(name, type);
			} else if (forFuncParam) {

				// Check parameter of the same name has not already been added
				int varIndex = m_userFuncPrototype.GetLocalVar(name);
				if (varIndex >= 0) {
					SetError("There is already a function parameter called '"
							+ name + "'");
					return false;
				}

				// Add parameter type to function definition
				m_userFuncPrototype.NewParam(name, type);

			} else {

				// Regular DIM.
				// Prefix DIMmed variable names
				name = m_symbolPrefix + name;

				if (m_inFunction) {

					// Local variable

					// Check if variable has already been DIMmed locally.
					// (This is allowed, but only if DIMmed to the same type.)
					int varIndex = UserPrototype().GetLocalVar(name);
					if (varIndex >= 0) {
						if (!(UserPrototype().localVarTypes.get(varIndex) == type)) {
							SetError((String) "Local variable '"
									+ name
									+ "' has already been allocated as a different type.");
							return false;
						}

						// Variable already created (earlier), so fall through
						// to
						// allocation code generation
					} else
						// Create new variable
						varIndex = UserPrototype().NewLocalVar(name, type);

					// Generate code to allocate local variable data
					// Note: Opcode contains the index of the variable. Variable
					// type and size data stored in the user function defn.
					AddInstruction(OpCode.OP_DECLARE_LOCAL,
							BasicValType.VTP_INT.getType(), new VMValue(varIndex));

					// Data containing strings will need to be "destroyed" when
					// the stack unwinds.
					if (m_vm.DataTypes().ContainsString(type))
						AddInstruction(OpCode.OP_REG_DESTRUCTOR,
								BasicValType.VTP_INT.getType(),
								new VMValue((int) m_vm.StoreType(type)));

					// Optional "= value"?
					if (m_token.m_text.equals("=")) {

						// Add opcode to load variable address
						AddInstruction(OpCode.OP_LOAD_LOCAL_VAR,
								BasicValType.VTP_INT.getType(), new VMValue(varIndex));

						// Set register type
						m_regType.Set(UserPrototype().localVarTypes
								.get(varIndex));
						m_regType.m_pointerLevel++;

						// Compile single deref.
						// Unlike standard variable assignment, we don't
						// automatically
						// deref pointers here. (Otherwise it would be
						// impossible to
						// set the pointer within the DIM statement).
						if (!CompileDeref())
							return false;

						// Compile the rest of the assignment
						if (!InternalCompileAssignment())
							return false;
					}

				} else {

					// Check if variable has already been DIMmed. (This is
					// allowed, but
					// only if DIMed to the same type.)
					int varIndex = m_vm.Variables().GetVar(name);
					if (varIndex >= 0) {
						if (!(m_vm.Variables().Variables().get(varIndex).m_type == type)) {
							SetError((String) "Variable '"
									+ name
									+ "' has already been allocated as a different type.");
							return false;
						}

						// Variable already created (earlier), so fall through
						// to
						// allocation code generation.
					} else
						// Create new variable
						varIndex = m_vm.Variables().NewVar(name, type);

					// Generate code to allocate variable data
					// Note: Opcode contains the index of the variable. Variable
					// type
					// and size data is stored in the variable entry.
					AddInstruction(OpCode.OP_DECLARE, BasicValType.VTP_INT.getType(),
							new VMValue(varIndex));

					// Optional "= value"?
					if (m_token.m_text.equals("=")) {

						// Add opcode to load variable address
						AddInstruction(OpCode.OP_LOAD_VAR,
								BasicValType.VTP_INT.getType(), new VMValue(varIndex));

						// Set register type
						m_regType.Set(m_vm.Variables().Variables()
								.get(varIndex).m_type);
						m_regType.m_pointerLevel++;

						// Compile single deref.
						// Unlike standard variable assignment, we don't
						// automatically
						// deref pointers here. (Otherwise it would be
						// impossible to
						// set the pointer within the DIM statement).
						if (!CompileDeref())
							return false;

						// Compile the rest of the assignment
						if (!InternalCompileAssignment())
							return false;
					}

				}

				// If this was an array and not a pointer, then its array
				// indices
				// will have been pushed to the stack.
				// The DECLARE operator automatically removes them however
				if (type.PhysicalPointerLevel() == 0)
					for (int i = 0; i < type.m_arrayLevel; i++)
						m_operandStack.remove(m_operandStack.size() - 1);
			}
		}
		return true;
	}

	boolean CompileTokenName(Mutable<String> name,
			Mutable<TokenType> tokenType, boolean allowSuffix) {
		tokenType.set(m_token.m_type);
		name.set(m_token.m_text);

		if (tokenType.get() != TokenType.CTT_TEXT
				&& tokenType.get() != TokenType.CTT_USER_FUNCTION
				&& tokenType.get() != TokenType.CTT_RUNTIME_FUNCTION) {
			SetError("Expected name");
			return false;
		}

		if (!allowSuffix) {
			char last = name.get().charAt(name.get().length() - 1);
			if (last == '#' || last == '%' || last == '$') {
				SetError("Subroutine names cannot end with: " + last);
				return false;
			}
		}

		if (!GetToken())
			return false;

		return true;
	}

	boolean CompileDataType(Mutable<String> name, Mutable<ValType> type,
			Mutable<TokenType> tokenType) {

		type.get().Set(BasicValType.VTP_UNDEFINED);
		name.set("");

		// Look for structure type
		if (m_token.m_type == TokenType.CTT_TEXT) {
			String structureName = m_symbolPrefix + m_token.m_text;
			int i = m_vm.DataTypes().GetStruc(structureName);
			if (i >= 0) {
				type.get().m_basicType = i;
				if (!GetToken()) // Skip token type keyword
					return false;
			}
		}

		// Look for preceeding & (indicates pointer)
		if (m_token.m_text.equals("&")) {
			type.get().m_pointerLevel++;
			if (!GetToken())
				return false;
		}

		// Look for variable name
		if (!CompileTokenName(name, tokenType, true))
			return false;

		// Determine variable type
		char last = '\0';
		if (name.get().length() > 0)
			last = name.get().charAt(name.get().length() - 1);
		if (type.get().m_basicType == BasicValType.VTP_UNDEFINED.getType()) {
			if (last == '$')
				type.get().m_basicType = BasicValType.VTP_STRING.getType();
			else if (last == '#')
				type.get().m_basicType = BasicValType.VTP_REAL.getType();
			else if (last == '%')
				type.get().m_basicType = BasicValType.VTP_INT.getType();
		} else {
			if (last == '$' || last == '#' || last == '%') {
				SetError((String) "\""
						+ name
						+ "\" is a structure variable, and cannot end with #, $ or %");
				return false;
			}
		}

		return true;
	}

	boolean CompileAs(Mutable<String> name, Mutable<ValType> type) {
		if (type.get().m_basicType != BasicValType.VTP_UNDEFINED.getType()) {
			SetError("'" + name
					+ "'s type has already been defined. Cannot use 'as' here.");
			return false;
		}

		// Skip "as"
		if (!GetToken())
			return false;

		// Expect "single", "double", "integer", "string" or a structure type
		if (m_token.m_type != TokenType.CTT_TEXT
				&& m_token.m_type != TokenType.CTT_KEYWORD) {
			SetError("Expected 'single', 'double', 'integer', 'string' or type name");
			return false;
		}
		if (m_token.m_text.equals("integer"))
			type.get().m_basicType = BasicValType.VTP_INT.getType();
		else if (m_token.m_text.equals("single")
				|| m_token.m_text.equals("double"))

			// Note: Basic4GL supports only one type of floating point number.
			// We will accept both keywords, but simply allocate a real (=
			// single
			// precision) floating point number each time.
			type.get().m_basicType = BasicValType.VTP_REAL.getType();
		else if (m_token.m_text.equals("string"))
			type.get().m_basicType = BasicValType.VTP_STRING.getType();
		else {

			// Look for recognised structure name
			String structureName = m_symbolPrefix + m_token.m_text;
			int i = m_vm.DataTypes().GetStruc(structureName);
			if (i < 0) {
				SetError("Expected 'single', 'double', 'integer', 'string' or type name");
				return false;
			}
			type.get().m_basicType = i;
		}

		// Skip type name
		if (!GetToken())
			return false;

		return true;
	}

	boolean CompileDimField(Mutable<String> name, Mutable<ValType> type,
			boolean forStruc, boolean forFuncParam) {

		// Compile data type
		TokenType tokenType = TokenType.CTT_CONSTANT;
		Mutable<TokenType> tokenTypeRef = new Mutable<TokenType>(tokenType);
		if (!CompileDataType(name, type, tokenTypeRef))
			return false;
		tokenType = tokenTypeRef.get(); // Update local value from reference

		// Name token must be text
		if (tokenType != TokenType.CTT_TEXT) {
			SetError("Expected variable name");
			return false;
		}

		// Look for array dimensions
		if (m_token.m_text.equals("(")) {

			boolean foundComma = false;
			while (m_token.m_text.equals("(") || foundComma) {

				// Room for one more dimension?
				if (type.get().m_arrayLevel >= Constants.VM_MAXDIMENSIONS) {
					SetError((String) "Arrays cannot have more than "
							+ String.valueOf(Constants.VM_MAXDIMENSIONS)
							+ " dimensions.");
					return false;
				}
				if (!GetToken()) // Skip "("
					return false;

				// Validate dimensions.
				// Firstly, pointers don't have dimensions declared with them.
				if (type.get().m_pointerLevel > 0) {
					if (!m_token.m_text.equals(")")) {
						SetError("Use '()' to declare a pointer to an array");
						return false;
					}
					type.get().m_arrayLevel++;
				}
				// Structure field types must have constant array size that we
				// can
				// evaluate at compile time (i.e right now).
				else if (forStruc) {

					// Evaluate constant expression
					Integer expressionType = BasicValType.VTP_INT.getType();
					VMValue value = new VMValue();
					String stringValue = "";

					Mutable<Integer> expressionTypeRef = new Mutable<Integer>(expressionType);
					Mutable<VMValue> valueRef = new Mutable<VMValue>(value);
					Mutable<String> stringValueRef = new Mutable<String>(
							stringValue);

					if (!EvaluateConstantExpression(expressionTypeRef,
							valueRef, stringValueRef))
						return false;

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
					if (!(CompileExpression()
							&& CompileConvert(BasicValType.VTP_INT.getType()) && CompilePush()))
						return false;
					type.get().m_arrayLevel++;
				}

				// Expect closing ')', or a separating comma
				foundComma = false;
				if (m_token.m_text.equals(")")) {
					if (!GetToken())
						return false;
				} else if (m_token.m_text.equals(","))
					foundComma = true;
				else {
					SetError("Expected ')' or ','");
					return false;
				}
			}
		}

		// "as" keyword (QBasic/FreeBasic compatibility)
		if (m_token.m_text.equals("as")) {
			if (!CompileAs(name, type))
				return false;
		}

		// If data type still not specified, default to integer
		if (type.get().m_basicType == BasicValType.VTP_UNDEFINED.getType())
			type.get().m_basicType = BasicValType.VTP_INT.getType();

		return true;
	}

	boolean CompileLoadVar() {

		// Look for "take address"
		boolean takeAddress = false;
		if (m_token.m_text.equals("&")) {
			takeAddress = true;
			if (!GetToken())
				return false;
		}

		// Look for variable name
		if (m_token.m_type != TokenType.CTT_TEXT) {
			SetError("Expected variable name");
			return false;
		}

		// Prefix variable names
		String varName = m_symbolPrefix + m_token.m_text;

		// Find variable
		boolean found = false;

		// Check local variable first
		if (m_inFunction) {

			// Look for variable
			int varIndex = UserPrototype().GetLocalVar(varName);

			// Set register type
			if (varIndex >= 0) {

				// Generate code to load variable
				AddInstruction(OpCode.OP_LOAD_LOCAL_VAR,
						BasicValType.VTP_INT.getType(), new VMValue(varIndex));

				// Set register type
				m_regType.Set(UserPrototype().localVarTypes.get(varIndex));
				m_regType.m_pointerLevel++;

				found = true;
			}
		}

		// Then try global
		if (!found) {

			// Look for variable
			int varIndex = m_vm.Variables().GetVar(varName);

			if (varIndex >= 0) {

				// Generate code to load variable
				AddInstruction(OpCode.OP_LOAD_VAR, BasicValType.VTP_INT.getType(),
						new VMValue(varIndex));

				// Set register type
				m_regType
				.Set(m_vm.Variables().Variables().get(varIndex).m_type);
				m_regType.m_pointerLevel++;

				found = true;
			}
		}

		if (!found) {
			SetError((String) "Unknown variable: " + m_token.m_text
					+ ". Variables must be declared first with DIM");
			return false;
		}

		// Skip past variable name
		if (!GetToken())
			return false;

		// Dereference to reach data
		if (!CompileDerefs())
			return false;

		// Compile data lookups (e.g. ".fieldname", array indices, take address
		// e.t.c)
		return CompileDataLookup(takeAddress);
	}

	boolean CompileDeref() {

		// Generate code to dereference pointer in reg. (i.e reg = [reg]).
		assert (m_vm.DataTypes().TypeValid(m_regType));

		// Not a pointer?
		if (m_regType.VirtualPointerLevel() <= 0) {
			assert (false); // This should never happen
			SetError("INTERNAL COMPILER ERROR: Attempted to dereference a non-pointer");
			return false;
		}

		// If reg is pointing to a structure or an array, we don't dereference
		// (as we can't fit an array or structure into a 4 byte register!).
		// Instead we leave it as a pointer, but update the type to indicate
		// to the compiler that we are using a pointer internally to represent
		// a variable.
		assert (!m_regType.m_byRef);
		if (m_regType.PhysicalPointerLevel() == 1 // Must be pointer to actual
				// data (not pointer to
				// pointer e.t.c)
				&& (m_regType.m_arrayLevel > 0 // Array
						|| m_regType.m_basicType >= 0)) { // or structure
			m_regType.m_byRef = true;
			return true;
		}

		// ///////////////////////
		// Generate dereference

		// Generate deref instruction
		m_regType.m_pointerLevel--;
		AddInstruction(OpCode.OP_DEREF, m_regType.StoredType(), new VMValue()); // Load
		// variable

		return true;
	}

	boolean CompileDerefs() {

		// Generate code to dereference pointer
		if (!CompileDeref())
			return false;

		// In Basic4GL syntax, pointers are implicitly dereferenced (similar to
		// C++'s
		// "reference" type.)
		if (m_regType.VirtualPointerLevel() > 0)
			if (!CompileDeref())
				return false;

		return true;
	}

	boolean CompileDataLookup(boolean takeAddress) {

		// Compile various data operations that can be performed on data object.
		// These operations include:
		// * Array indexing: data (index)
		// * Field lookup: data.field
		// * Taking address: &data
		// Or any combination of the above.

		boolean done = false;
		while (!done) {
			if (m_token.m_text.equals(".")) {

				// Lookup subfield
				// Register must contain a structure type
				if (m_regType.VirtualPointerLevel() != 0
						|| m_regType.m_arrayLevel != 0
						|| m_regType.m_basicType < 0) {
					SetError("Unexpected '.'");
					return false;
				}
				assert (m_vm.DataTypes().TypeValid(m_regType));

				// Skip "."
				if (!GetToken())
					return false;

				// Read field name
				if (m_token.m_type != TokenType.CTT_TEXT) {
					SetError("Expected field name");
					return false;
				}
				String fieldName = m_token.m_text;
				if (!GetToken())
					return false;

				// Validate field
				Structure s = m_vm.DataTypes().Structures()
						.get(m_regType.m_basicType);
				int fieldIndex = m_vm.DataTypes().GetField(s, fieldName);
				if (fieldIndex < 0) {
					SetError((String) "'" + fieldName
							+ "' is not a field of structure '" + s.m_name
							+ "'");
					return false;
				}

				// Generate code to calculate address of field
				// Reg is initially pointing to address of structure.
				StructureField field = m_vm.DataTypes().Fields()
						.get(fieldIndex);
				AddInstruction(OpCode.OP_ADD_CONST, BasicValType.VTP_INT.getType(),
						new VMValue(field.m_dataOffset));

				// Reg now contains pointer to field
				m_regType.Set(field.m_type);
				m_regType.m_pointerLevel++;

				// Dereference to reach data
				if (!CompileDerefs())
					return false;
			} else if (m_token.m_text.equals("(")) {

				// Register must contain an array
				if (m_regType.VirtualPointerLevel() != 0
						|| m_regType.m_arrayLevel == 0) {
					SetError("Unexpected '('");
					return false;
				}

				do {
					if (m_regType.m_arrayLevel == 0) {
						SetError("Unexpected ','");
						return false;
					}

					// Index into array
					if (!GetToken()) // Skip "(" or ","
						return false;

					// Generate code to push array address
					if (!CompilePush())
						return false;

					// Evaluate array index, and convert to an integer.
					if (!CompileExpression())
						return false;
					if (!CompileConvert(BasicValType.VTP_INT.getType())) {
						SetError("Array index must be a number. "
								+ m_vm.DataTypes().DescribeVariable("",
										m_regType) + " is not a number");
						return false;
					}

					// Generate code to pop array address into reg2
					if (!CompilePop())
						return false;

					// Generate code to index into array.
					// Input: reg = Array index
					// reg2 = Array address
					// Output: reg = Pointer to array element
					AddInstruction(OpCode.OP_ARRAY_INDEX,
							BasicValType.VTP_INT.getType(), new VMValue());

					// reg now points to an element
					m_regType.Set(m_reg2Type);
					m_regType.m_byRef = false;
					m_regType.m_pointerLevel = 1;
					m_regType.m_arrayLevel--;

					// Dereference to get to element
					if (!CompileDerefs())
						return false;

				} while (m_token.m_text.equals(","));

				// Expect closing bracket
				if (!m_token.m_text.equals(")")) {
					SetError("Expected ')'");
					return false;
				}
				if (!GetToken())
					return false;
			} else
				done = true;
		}

		// Compile take address (if necessary)
		if (takeAddress)
			if (!CompileTakeAddress())
				return false;

		return true;
	}

	boolean CompileExpression() {
		return CompileExpression(false);
	}

	boolean CompileExpression(boolean mustBeConstant) {

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
		m_operatorStack.add(new compStackedOperator(new compOperator(
				compOpType.OT_STOP, OpCode.OP_NOP, 0, -200000))); // Stop
		// evaluation
		// operator

		if (!CompileExpressionLoad(mustBeConstant))
			return false;

		compOperator o = null;
		while ((m_token.m_text.equals(")") && getOperatorTOS().mOper.mType != compOpType.OT_STOP)
				|| ((o = m_binaryOperators.get(m_token.m_text)) != null)) {

			// Special case, right bracket
			if (m_token.m_text.equals(")")) {

				// Evaluate all operators down to left bracket
				while (getOperatorTOS().mOper.mType != compOpType.OT_STOP
						&& getOperatorTOS().mOper.mType != compOpType.OT_LBRACKET)
					if (!CompileOperation())
						return false;

				// If operator stack is empty, then the expression terminates
				// before
				// the closing bracket
				if (getOperatorTOS().mOper.mType == compOpType.OT_STOP) {
					m_operatorStack.remove(m_operatorStack.size() - 1); // Remove
					// stopper
					return true;
				}

				// Remove left bracket
				m_operatorStack.remove(m_operatorStack.size() - 1);

				// Move on
				if (!GetToken())
					return false;

				// Result may be an array or a structure to which a data lookup
				// can
				// be applied.
				if (!CompileDataLookup(false))
					return false;
			}

			// Otherwise must be regular binary operator
			else {

				// Compare current operator with top of stack operator
				while (getOperatorTOS().mOper.mType != compOpType.OT_STOP
						&& getOperatorTOS().mOper.mBinding >= o.mBinding)
					if (!CompileOperation())
						return false;

				// 14-Apr-06: Lazy evaluation.
				// Add jumps around the second part of AND or OR operations
				int lazyJumpAddr = -1;
				if (o.mType == compOpType.OT_LAZYBOOLOPERATOR) {
					if (o.mOpCode == OpCode.OP_OP_AND) {
						lazyJumpAddr = m_vm.InstructionCount();
						AddInstruction(OpCode.OP_JUMP_FALSE,
								BasicValType.VTP_INT.getType(), new VMValue(0));
					} else if (o.mOpCode == OpCode.OP_OP_OR) {
						lazyJumpAddr = m_vm.InstructionCount();
						AddInstruction(OpCode.OP_JUMP_TRUE,
								BasicValType.VTP_INT.getType(), new VMValue(0));
					}
				}

				// Save operator to stack
				m_operatorStack.add(new compStackedOperator(o, lazyJumpAddr));

				// Push first operand
				if (!CompilePush())
					return false;

				// Load second operand
				if (!GetToken())
					return false;
				if (!CompileExpressionLoad(mustBeConstant))
					return false;
			}
		}

		// Perform remaining operations
		while (getOperatorTOS().mOper.mType != compOpType.OT_STOP)
			if (!CompileOperation())
				return false;

		// Remove stopper
		m_operatorStack.remove(m_operatorStack.size() - 1);

		return true;
	}

	boolean CompileOperation() {

		// Compile topmost operation on operator stack
		assert (!m_operatorStack.isEmpty());

		// Remove operator from stack
		compStackedOperator o = getOperatorTOS();
		m_operatorStack.remove(m_operatorStack.size() - 1);

		// Must not be a left bracket
		if (o.mOper.mType == compOpType.OT_LBRACKET) {
			SetError("Expected ')'");
			return false;
		}

		// Binary or unary operation?
		if (o.mOper.mParams == 1) {

			// Try plug in language extension first
			if (CompileExtendedUnOperation(o.mOper.mOpCode))
				return true;

			// Can only operate on basic types.
			// (This will change once vector and matrix routines have been
			// implemented).
			if (!m_regType.IsBasic()) {
				SetError("Operator cannot be applied to this data type");
				return false;
			}

			// Special case, boolean operator.
			// Must convert to boolean first
			if (o.mOper.mType == compOpType.OT_BOOLOPERATOR
					|| o.mOper.mType == compOpType.OT_LAZYBOOLOPERATOR)
				CompileConvert(BasicValType.VTP_INT.getType());

			// Perform unary operation
			AddInstruction(o.mOper.mOpCode, m_regType.m_basicType,
					new VMValue());

			// Special case, boolean operator
			// Result will be an integer
			if (o.mOper.mType == compOpType.OT_RETURNBOOLOPERATOR)
				m_regType.Set(BasicValType.VTP_INT);
		} else if (o.mOper.mParams == 2) {

			// Generate code to pop first operand from stack into Reg2
			if (!CompilePop())
				return false;

			// Try plug in language extension first
			if (CompileExtendedBinOperation(o.mOper.mOpCode))
				return true;

			// Ensure operands are equal type. Generate code to convert one if
			// necessary.

			int opCodeType; // Data type to store in OP_CODE
			if (m_regType.IsNull() || m_reg2Type.IsNull()) {

				// Can compare null to any pointer type. However, operator must
				// be '=' or '<>'
				if (o.mOper.mOpCode != OpCode.OP_OP_EQUAL
						&& o.mOper.mOpCode != OpCode.OP_OP_NOT_EQUAL) {
					SetError("Operator cannot be applied to this data type");
					return false;
				}

				// Convert null pointer type to non null pointer type
				// Note: If both pointers a null, CompileConvert will simply do
				// nothing
				if (m_regType.IsNull())
					if (!CompileConvert(m_reg2Type))
						return false;

				if (m_reg2Type.IsNull())
					if (!CompileConvert2(m_regType))
						return false;

				opCodeType = BasicValType.VTP_INT.getType(); // Integer comparison is
				// used internally
			} else if (m_regType.VirtualPointerLevel() > 0
					|| m_reg2Type.VirtualPointerLevel() > 0) {

				// Can compare 2 pointers. However operator must be '=' or '<>'
				// and
				// pointer types must be exactly the same
				if (o.mOper.mOpCode != OpCode.OP_OP_EQUAL
						&& o.mOper.mOpCode != OpCode.OP_OP_NOT_EQUAL) {
					SetError("Operator cannot be applied to this data type");
					return false;
				}
				if (!m_regType.ExactEquals(m_reg2Type)) {
					SetError("Cannot compare pointers to different types");
					return false;
				}

				opCodeType = BasicValType.VTP_INT.getType(); // Integer comparison is
				// used internally
			} else {

				// Otherwise all operators can be applied to basic data types
				if (!m_regType.IsBasic() || !m_reg2Type.IsBasic()) {
					SetError("Operator cannot be applied to this data type");
					return false;
				}

				// Convert operands to highest type
				int highest = m_regType.m_basicType;
				if (m_reg2Type.m_basicType > highest)
					highest = m_reg2Type.m_basicType;
				if (o.mOper.mType == compOpType.OT_BOOLOPERATOR
						|| o.mOper.mType == compOpType.OT_LAZYBOOLOPERATOR)
					highest = BasicValType.VTP_INT.getType();
				if (m_syntax == compLanguageSyntax.LS_TRADITIONAL
						&& o.mOper.mOpCode == OpCode.OP_OP_DIV)
					// 14-Aug-05 Tom: In traditional mode, division is always
					// between floating pt numbers
					highest = BasicValType.VTP_REAL.getType();

				if (!CompileConvert(highest))
					return false;
				if (!CompileConvert2(highest))
					return false;

				opCodeType = highest;
			}

			// Generate operation code
			AddInstruction(o.mOper.mOpCode, opCodeType, new VMValue());

			// Special case, boolean operator
			// Result will be an integer
			if (o.mOper.mType == compOpType.OT_RETURNBOOLOPERATOR)
				m_regType.Set(BasicValType.VTP_INT);
		} else
			assert (false);

		// Fix up lazy jumps
		if (o.mLazyJumpAddr >= 0)
			m_vm.Instruction(o.mLazyJumpAddr).mValue.setVal((int) m_vm
					.InstructionCount());

		return true;
	}

	boolean CompileLoad() {

		// Compile load var or constant, or function result
		if (m_token.m_type == TokenType.CTT_CONSTANT
				|| m_token.m_text.equals("null"))
			return CompileLoadConst();
		else if (m_token.m_type == TokenType.CTT_TEXT
				|| m_token.m_text.equals("&"))
			return CompileLoadVar();
		else if (m_token.m_type == TokenType.CTT_FUNCTION)
			return CompileFunction(true);
		else if (m_token.m_type == TokenType.CTT_USER_FUNCTION)
			return CompileUserFunctionCall(true, false);
		else if (m_token.m_type == TokenType.CTT_RUNTIME_FUNCTION)
			return CompileUserFunctionCall(true, true);

		SetError("Expected constant, variable or function");
		return false;
	}

	boolean CompileExpressionLoad() {
		return CompileExpressionLoad(false);
	}

	boolean CompileExpressionLoad(boolean mustBeConstant) {

		// Like CompileLoad, but will also accept and stack preceeding unary
		// operators

		// Push any unary operators found
		while (true) {

			// Special case, left bracket
			if (m_token.m_text.equals("("))
				m_operatorStack.add(new compStackedOperator(new compOperator(
						compOpType.OT_LBRACKET, OpCode.OP_NOP, 0, -10000))); // Brackets
			// bind
			// looser
			// than
			// anything else

			// Otherwise look for recognised unary operator
			else {
				compOperator o = m_unaryOperators.get(m_token.m_text);
				if (o != null) // Operator found
					m_operatorStack.add(new compStackedOperator(o)); // => Stack
				// it
				else { // Not an operator
					if (mustBeConstant)
						return CompileLoadConst();
					else
						return CompileLoad(); // => Proceed on to load
					// variable/constant
				}
			}

			if (!GetToken())
				return false;
		}
	}

	boolean CompileNull() {
		AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_INT.getType(),
				new VMValue(0)); // Load 0 into
		// register
		m_regType.Set(new ValType(BasicValType.VTP_NULL, (byte) 0, (byte) 1,
				false)); // Type
		// is
		// pointer
		// to
		// VTP_NULL
		return GetToken();
	}

	boolean CompileLoadConst() {

		// Special case, "null" reserved word
		if (m_token.m_text.equals("null"))
			return CompileNull();

		// Compile load constant
		if (m_token.m_type == TokenType.CTT_CONSTANT) {

			// Special case, string constants
			if (m_token.m_valType == BasicValType.VTP_STRING) {

				// Allocate new string constant
				String text;
				text = m_token.m_text.substring(1,	m_token.m_text.length()); // Remove S prefix
				int index = m_vm.StoreStringConstant(text);

				// store load instruction
				AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING.getType(),
						new VMValue(index));
				m_regType.Set(BasicValType.VTP_STRING);
			} else if (m_token.m_valType == BasicValType.VTP_REAL) {
				AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_REAL.getType(),
						new VMValue(Float.valueOf(m_token.m_text)));
				m_regType.Set(BasicValType.VTP_REAL);
			} else if (m_token.m_valType == BasicValType.VTP_INT) {
				AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_INT.getType(),
						new VMValue(Cast.StringToInt(m_token.m_text)));
				m_regType.Set(BasicValType.VTP_INT);
			} else {
				SetError("Unknown data type");
				return false;
			}

			return GetToken();
		}

		SetError("Expected constant");
		return false;
	}

	boolean CompilePush() {

		// Store pushed value type
		m_operandStack.add(new ValType(m_regType));

		// Generate push code
		AddInstruction(OpCode.OP_PUSH, m_regType.StoredType(), new VMValue());

		return true;
	}

	boolean CompilePop() {

		if (m_operandStack.isEmpty()) {
			SetError("Expression error");
			return false;
		}

		// Retrieve pushed value type
		m_reg2Type.Set(m_operandStack.lastElement());
		m_operandStack.remove(m_operandStack.size() - 1);

		// Generate pop code
		AddInstruction(OpCode.OP_POP, m_reg2Type.StoredType(), new VMValue());

		return true;
	}

	boolean CompileConvert(int basictype) {

		// Convert reg to given type
		if (m_regType.Equals(basictype)) // Already same type
			return true;

		// Determine opcode
		short code = OpCode.OP_NOP;
		if (m_regType.Equals(BasicValType.VTP_INT)) {
			if (basictype == BasicValType.VTP_REAL.getType())
				code = OpCode.OP_CONV_INT_REAL;
			else if (basictype == BasicValType.VTP_STRING.getType())
				code = OpCode.OP_CONV_INT_STRING;
		} else if (m_regType.equals(BasicValType.VTP_REAL)) {
			if (basictype == BasicValType.VTP_INT.getType())
				code = OpCode.OP_CONV_REAL_INT;
			else if (basictype == BasicValType.VTP_STRING.getType())
				code = OpCode.OP_CONV_REAL_STRING;
		}

		// Store instruction
		if (code != OpCode.OP_NOP) {
			AddInstruction(code, BasicValType.VTP_INT.getType(), new VMValue());
			m_regType.Set(basictype);
			return true;
		}

		SetError("Incorrect data type");
		return false;
	}

	boolean CompileConvert2(int type) {

		// Convert reg2 to given type
		if (m_reg2Type.Equals(type)) // Already same type
			return true;

		// Determine opcode
		short code = OpCode.OP_NOP;
		if (m_reg2Type.Equals(BasicValType.VTP_INT)) {
			if (type == BasicValType.VTP_REAL.getType())
				code = OpCode.OP_CONV_INT_REAL2;
			else if (type == BasicValType.VTP_STRING.getType())
				code = OpCode.OP_CONV_INT_STRING2;
		} else if (m_reg2Type.Equals(BasicValType.VTP_REAL)) {
			if (type == BasicValType.VTP_INT.getType())
				code = OpCode.OP_CONV_REAL_INT2;
			else if (type == BasicValType.VTP_STRING.getType())
				code = OpCode.OP_CONV_REAL_STRING2;
		}

		// Store instruction
		if (code != OpCode.OP_NOP) {
			AddInstruction(code, BasicValType.VTP_INT.getType(), new VMValue());
			m_reg2Type.Set(type);
			return true;
		}

		SetError("Incorrect data type");
		return false;
	}

	boolean CompileConvert(ValType type) {

		// Can convert null to a different pointer type
		if (m_regType.IsNull()) {
			if (type.VirtualPointerLevel() <= 0) {
				SetError("Cannot convert null to "
						+ m_vm.DataTypes().DescribeVariable("", type));
				return false;
			}

			// No generated code necessary, just substitute in type
			m_regType.Set(type);
			return true;
		}

		// Can convert values to references. (This is used when evaluating
		// function
		// parameters.)
		if (type.m_pointerLevel == 1 && type.m_byRef // type is a reference
				&& m_regType.m_pointerLevel == 0 // regType is a value
				&& m_regType.m_basicType == type.m_basicType // Same type of
				// data
				&& m_regType.m_arrayLevel == type.m_arrayLevel) {

			// Convert register to pointer
			if (CompileTakeAddress()) {

				// Convert pointer to reference
				m_regType.m_byRef = true;
				return true;
			} else
				return false;
		}

		// Can convert to basic types.
		// For non basic types, all we can do is verify that the register
		// contains
		// the type that we expect, and raise a compiler error otherwise.
		if (type.IsBasic())
			return CompileConvert(type.m_basicType);
		else if (m_regType.ExactEquals(type))
			return true; // Note: Exact equals is required as == will say that
		// pointers are equal to references.
		// (Internally this is true, but we want to enforce
		// that programs use the correct type.)

		SetError("Cannot convert to "
				+ m_vm.DataTypes().DescribeVariable("", type));
		return false;
	}

	boolean CompileConvert2(ValType type) {

		// Can convert null to a different pointer type
		if (m_reg2Type.IsNull()) {
			if (type.VirtualPointerLevel() <= 0) {
				SetError("Cannot convert null to "
						+ m_vm.DataTypes().DescribeVariable("", type));
				return false;
			}

			// No generated code necessary, just substitute in type
			m_reg2Type.Set(type);
			return true;
		}

		// Can convert to basic types.
		// For non basic types, all we can do is verify that the register
		// contains
		// the type that we expect, and raise a compiler error otherwise.
		if (type.IsBasic())
			return CompileConvert2(type.m_basicType);
		else if (m_reg2Type.ExactEquals(type))
			return true; // Note: Exact equals is required as == will say that
		// pointers are equal to references.
		// (Internally this is true, but we want to enforce
		// that programs use the correct type.)

		SetError("Cannot convert to "
				+ m_vm.DataTypes().DescribeVariable("", type));
		return false;
	}

	boolean CompileTakeAddress() {

		// Take address of data in reg.
		// We do this my moving the previously generate deref from the end of
		// the program.
		// (If the last instruction is not a deref, then there is a problem.)

		// Special case: Implicit pointer
		if (m_regType.m_byRef) {
			m_regType.m_byRef = false; // Convert to explicit pointer
			return true;
		}

		// Check last instruction was a deref
		if (m_vm.InstructionCount() <= 0
				|| m_vm.Instruction(m_vm.InstructionCount() - 1).mOpCode != OpCode.OP_DEREF) {
			SetError("Cannot take address of this data");
			return false;
		}

		// Remove it
		m_vm.RemoveLastInstruction();
		m_regType.m_pointerLevel++;

		return true;
	}

	boolean CompileAssignment() {

		// Generate code to load target variable
		if (!CompileLoadVar())
			return false;

		// Compile code to assign value to variable
		if (!InternalCompileAssignment())
			return false;

		return true;
	}

	boolean InternalCompileAssignment() {

		// Expect =
		if (!m_token.m_text.equals("=")) {
			SetError("Expected '='");
			return false;
		}

		// Convert load target variable into take address of target variable
		if (!CompileTakeAddress()) {
			SetError("Left side cannot be assigned to");
			return false;
		}

		// Skip =
		if (!GetToken())
			return false;

		// Push target address
		if (!CompilePush())
			return false;

		// Generate code to evaluate expression
		if (!CompileExpression())
			return false;

		// Pop target address into reg2
		if (!CompilePop())
			return false;

		// Simple type case: reg2 points to basic type
		if (m_reg2Type.m_pointerLevel == 1 && m_reg2Type.m_arrayLevel == 0
				&& m_reg2Type.m_basicType < 0) {

			// Attempt to convert value in reg to same type
			if (!CompileConvert(m_reg2Type.m_basicType)) {
				SetError("Types do not match");
				return false;
			}

			// Save reg into [reg2]
			AddInstruction(OpCode.OP_SAVE, m_reg2Type.m_basicType,
					new VMValue());
		}

		// Pointer case. m_reg2 must point to a pointer and m_reg1 point to a
		// value.
		else if (m_reg2Type.VirtualPointerLevel() == 2
				&& m_regType.VirtualPointerLevel() == 1) {

			// Must both point to same type, OR m_reg1 must point to null
			if (m_regType.IsNull()
					|| (m_regType.m_arrayLevel == m_reg2Type.m_arrayLevel && m_regType.m_basicType == m_reg2Type.m_basicType)) {

				// Validate pointer scope before saving to variable
				AddInstruction(OpCode.OP_CHECK_PTR, BasicValType.VTP_INT.getType(),
						new VMValue());

				// Save address to pointer
				AddInstruction(OpCode.OP_SAVE, BasicValType.VTP_INT.getType(),
						new VMValue());
			} else {
				SetError("Types do not match");
				return false;
			}
		}

		// Copy object case
		else if (m_reg2Type.VirtualPointerLevel() == 1
				&& m_regType.VirtualPointerLevel() == 0
				&& m_regType.PhysicalPointerLevel() == 1) {

			// Check that both are the same type
			if (m_regType.m_arrayLevel == m_reg2Type.m_arrayLevel
					&& m_regType.m_basicType == m_reg2Type.m_basicType) {

				// Add op-code to check pointers if necessary
				ValType dataType = new ValType(m_regType);
				dataType.m_pointerLevel--;
				dataType.m_byRef = false;
				if (m_vm.DataTypes().ContainsPointer(dataType))
					AddInstruction(OpCode.OP_CHECK_PTRS,
							BasicValType.VTP_INT.getType(),
							new VMValue((int) m_vm.StoreType(dataType)));

				AddInstruction(OpCode.OP_COPY, BasicValType.VTP_INT.getType(),
						new VMValue((int) m_vm.StoreType(m_regType)));
			} else {
				SetError("Types do not match");
				return false;
			}
		} else {
			SetError("Types do not match");
			return false;
		}

		return true;
	}

	boolean CompileGoto() {
		return CompileGoto(OpCode.OP_JUMP);
	}

	/**
	 *
	 * @param jumpType Flow control Op code
	 * @return
	 */
	boolean CompileGoto(short jumpType) {
		assert (jumpType == OpCode.OP_JUMP
				|| jumpType == OpCode.OP_JUMP_TRUE
				|| jumpType == OpCode.OP_JUMP_FALSE || jumpType == OpCode.OP_CALL);

		// Cannot use goto inside a function or sub (can use GOSUB though)
		if (m_inFunction && jumpType != OpCode.OP_CALL) {
			SetError("Cannot use 'goto' inside a function or subroutine");
			return false;
		}

		// Validate label
		if (m_token.m_type != TokenType.CTT_TEXT) {
			SetError("Expected label name");
			return false;
		}

		// Record jump, so that we can fix up the offset in the second compile
		// pass.
		String labelName = m_symbolPrefix + m_token.m_text;
		m_jumps.add(new compJump(m_vm.InstructionCount(), labelName));

		// Add jump instruction
		AddInstruction(jumpType, BasicValType.VTP_INT.getType(), new VMValue(0));

		// Move on
		return GetToken();
	}

	boolean CompileIf(boolean elseif) {

		// Skip "if"
		int line = m_parser.Line(), col = m_parser.Col();
		if (!GetToken())
			return false;

		// Generate code to evaluate expression
		if (!CompileExpression())
			return false;

		// Generate code to convert to integer
		if (!CompileConvert(BasicValType.VTP_INT.getType()))
			return false;

		// Free any temporary data expression may have created
		if (!CompileFreeTempData())
			return false;

		// Special case!
		// If next instruction is a "goto", then we can ommit the "then"
		if (!m_token.m_text.equals("goto")) {

			// Otherwise expect "then"
			if (!m_token.m_text.equals("then")) {
				SetError("Expected 'then'");
				return false;
			}
			if (!GetToken())
				return false;
		}

		// Determine whether this "if" has an automatic "endif" inserted at the
		// end of the line
		boolean autoEndif = (m_syntax == compLanguageSyntax.LS_TRADITIONAL) // Only
				// applies
				// to
				// traditional syntax
				&& !(m_token.m_type == TokenType.CTT_EOL || m_token.m_type == TokenType.CTT_EOF); // "then"
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
		m_flowControl.add(new compFlowControl(compFlowControlType.FCT_IF, m_vm
				.InstructionCount(), 0, line, col, elseif, "", !autoEndif));

		// Create conditional jump
		AddInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT.getType(),
				new VMValue(0));

		m_needColon = false; // Don't need colon between this and next
		// instruction
		return true;
	}

	boolean CompileElse(boolean elseif) {

		// Find "if" on top of flow control stack
		if (!FlowControlTopIs(compFlowControlType.FCT_IF)) {
			SetError("'else' without 'if'");
			return false;
		}
		compFlowControl top = FlowControlTOS();
		m_flowControl.remove(m_flowControl.size() - 1);

		// Skip "else"
		// (But not if it's really an "elseif". CompileIf will skip over it
		// then.)
		int line = m_parser.Line(), col = m_parser.Col();
		if (!elseif) {
			if (!GetToken())
				return false;
		}

		// Push else to flow control stack
		m_flowControl.add(new compFlowControl(compFlowControlType.FCT_ELSE,
				m_vm.InstructionCount(), 0, line, col, top.m_impliedEndif, "",
				top.m_blockIf));

		// Generate code to jump around else block
		AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT.getType(), new VMValue(0));

		// Fixup jump around IF block
		assert (top.m_jumpOut < m_vm.InstructionCount());
		m_vm.Instruction(top.m_jumpOut).mValue.setIntVal(m_vm
				.InstructionCount());

		m_needColon = false; // Don't need colon between this and next
		// instruction
		return true;
	}

	boolean CompileEndIf(boolean automatic) {

		// Find if or else on top of flow control stack
		if (!(FlowControlTopIs(compFlowControlType.FCT_IF) || FlowControlTopIs(compFlowControlType.FCT_ELSE))) {
			SetError("'endif' without 'if'");
			return false;
		}
		compFlowControl top = FlowControlTOS();
		m_flowControl.remove(m_flowControl.size() - 1);

		// Skip "endif"
		if (!top.m_impliedEndif && !automatic) {
			if (!GetToken())
				return false;
		}

		// Fixup jump around IF or ELSE block
		assert (top.m_jumpOut < m_vm.InstructionCount());
		m_vm.Instruction(top.m_jumpOut).mValue.setIntVal(m_vm
				.InstructionCount());

		// If there's an implied endif then add it
		if (top.m_impliedEndif)
			return CompileEndIf(automatic);
		else
			return true;
	}

	boolean CompileFor() {

		// Skip "for"
		int line = m_parser.Line(), col = m_parser.Col();
		if (!GetToken())
			return false;

		// Extract loop variable name
		Token nextToken = m_parser.PeekToken(false, false);
		if (!CheckParser())
			return false;
		if (nextToken.m_text.equals("(")) {
			SetError("Cannot use array variable in 'for' - 'next' structure");
			return false;
		}
		String loopVarUnprefixed = m_token.m_text;
		String loopVar = m_symbolPrefix + loopVarUnprefixed;

		// Verify variable is numeric
		boolean found = false;
		Integer loopVarType = BasicValType.VTP_INT.getType();

		// Check local variable first
		if (m_inFunction) {

			// Look for variable
			int varIndex = UserPrototype().GetLocalVar(loopVar);

			// Set register type
			if (varIndex >= 0) {
				found = true;

				// Check type is INT or REAL
				ValType type = UserPrototype().localVarTypes.get(varIndex);
				if (!(type.Equals(BasicValType.VTP_INT) || type
						.Equals(BasicValType.VTP_REAL))) {
					SetError("Loop variable must be an Integer or Real");
					return false;
				}
				loopVarType = type.m_basicType;
			}
		}

		// Check global variable
		if (!found) {
			int varIndex = m_vm.Variables().GetVar(loopVar);
			if (varIndex >= 0) {
				found = true;

				// Check type is INT or REAL
				ValType type = m_vm.Variables().Variables().get(varIndex).m_type;
				if (!(type.Equals(BasicValType.VTP_INT) || type
						.Equals(BasicValType.VTP_REAL))) {
					SetError("Loop variable must be an Integer or Real");
					return false;
				}
				loopVarType = type.m_basicType;
			}
		}
		if (!found) {
			SetError("Unknown variable: " + m_token.m_text
					+ ". Must be declared with DIM");
			return false;
		}

		// Compile assignment
		int varLine = m_parser.Line(), varCol = m_parser.Col();
		Token varToken = m_token;
		if (!CompileAssignment())
			return false;

		// Save loop back position
		int loopPos = m_vm.InstructionCount();

		// Expect "to"
		if (!m_token.m_text.equals("to")) {
			SetError("Expected 'to'");
			return false;
		}
		if (!GetToken())
			return false;

		// Compile load variable and push
		compParserPos savedPos = SavePos(); // Save parser position
		m_parser.SetPos(varLine, varCol); // Point to variable name
		m_token = varToken;

		if (!CompileLoadVar()) // Load variable
			return false;
		if (!CompilePush()) // And push
			return false;

		RestorePos(savedPos); // Restore parser position

		// Compile "to" expression
		if (!CompileExpression())
			return false;
		if (!CompileConvert(loopVarType))
			return false;

		// Evaluate step. (Must be a constant expression)
		Integer stepType = loopVarType;
		VMValue stepValue = new VMValue();

		if (m_token.m_text.equals("step")) {

			// Skip step instruction
			if (!GetToken())
				return false;

			// Compile step constant (expression)
			String stringValue = "";
			Mutable<Integer> stepTypeRef = new Mutable<Integer>(stepType);
			Mutable<VMValue> stepValueRef = new Mutable<VMValue>(stepValue);
			Mutable<String> stringValueRef = new Mutable<String>(stringValue);
			if (!EvaluateConstantExpression(stepTypeRef, stepValueRef,
					stringValueRef))
				return false;

			// Update local values from references
			stepType = stepTypeRef.get();
			stepValue = stepValueRef.get();
			stringValue = stringValueRef.get();

		} else {

			// No explicit step.
			// Use 1 as default
			if (stepType == BasicValType.VTP_INT.getType())
				stepValue = new VMValue(1);
			else
				stepValue = new VMValue(1.0f);
		}

		// Choose comparison operator (based on direction of step)
		compOperator comparison;
		if (stepType == BasicValType.VTP_INT.getType()) {
			if (stepValue.getIntVal() > 0)
				comparison = m_binaryOperators.get("<=");
			else if (stepValue.getIntVal() < 0)
				comparison = m_binaryOperators.get(">=");
			else
				comparison = m_binaryOperators.get("<>");
		} else {
			assert (stepType == BasicValType.VTP_REAL.getType());
			if (stepValue.getRealVal() > 0)
				comparison = m_binaryOperators.get("<=");
			else if (stepValue.getRealVal() < 0)
				comparison = m_binaryOperators.get(">=");
			else
				comparison = m_binaryOperators.get("<>");
		}

		// Compile comparison expression
		m_operatorStack.add(new compStackedOperator(comparison));
		if (!CompileOperation())
			return false;

		// Generate step expression
		String step = loopVarUnprefixed
				+ " = "
				+ loopVarUnprefixed
				+ " + "
				+ (stepType == BasicValType.VTP_INT.getType() ? String.valueOf(stepValue
						.getIntVal()) : String.valueOf(stepValue.getRealVal()));

		// Create flow control structure
		m_flowControl.add(new compFlowControl(compFlowControlType.FCT_FOR, m_vm
				.InstructionCount(), loopPos, line, col, false, step, false));

		// Create conditional jump
		AddInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT.getType(),
				new VMValue(0));

		return true;
	}

	boolean CompileNext() {

		// Find for on top of flow control stack
		if (!FlowControlTopIs(compFlowControlType.FCT_FOR)) {
			SetError("'next' without 'for'");
			return false;
		}
		compFlowControl top = FlowControlTOS();
		m_flowControl.remove(m_flowControl.size() - 1);

		// Skip "next"
		int nextLine = m_token.m_line, nextCol = m_token.m_col;
		if (!GetToken())
			return false;

		// Generate instruction to increment loop variable
		m_parser.SetSpecial(top.m_data, nextLine, nextCol); // Special mode.
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
		Token saveToken = m_token;
		if (!GetToken())
			return false;
		if (!CompileAssignment())
			return false;
		m_parser.SetNormal();
		m_token = saveToken;

		// Generate jump back instruction
		AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT.getType(), new VMValue(
				top.m_jumpLoop));

		// Fixup jump around FOR block
		assert (top.m_jumpOut < m_vm.InstructionCount());
		m_vm.Instruction(top.m_jumpOut).mValue.setIntVal(m_vm
				.InstructionCount());
		return true;
	}

	boolean CompileWhile() {

		// Save loop position
		int loopPos = m_vm.InstructionCount();

		// Skip "while"
		int line = m_parser.Line(), col = m_parser.Col();
		if (!GetToken())
			return false;

		// Generate code to evaluate expression
		if (!CompileExpression())
			return false;

		// Generate code to convert to integer
		if (!CompileConvert(BasicValType.VTP_INT.getType()))
			return false;

		// Free any temporary data expression may have created
		if (!CompileFreeTempData())
			return false;

		// Create flow control structure
		m_flowControl.add(new compFlowControl(compFlowControlType.FCT_WHILE,
				m_vm.InstructionCount(), loopPos, line, col));

		// Create conditional jump
		AddInstruction(OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT.getType(),
				new VMValue(0));
		return true;
	}

	boolean CompileWend() {

		// Find while on top of flow control stack
		if (!FlowControlTopIs(compFlowControlType.FCT_WHILE)) {
			SetError("'wend' without 'while'");
			return false;
		}
		compFlowControl top = FlowControlTOS();
		m_flowControl.remove(m_flowControl.size() - 1);

		// Skip "wend"
		if (!GetToken())
			return false;

		// Generate jump back
		AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT.getType(), new VMValue(
				top.m_jumpLoop));

		// Fixup jump around WHILE block
		assert (top.m_jumpOut < m_vm.InstructionCount());
		m_vm.Instruction(top.m_jumpOut).mValue.setIntVal(m_vm
				.InstructionCount());
		return true;
	}

	boolean CompileDo() {

		// Save loop position
		int loopPos = m_vm.InstructionCount();

		// Skip "do"
		int line = m_parser.Line(), col = m_parser.Col();
		if (!GetToken())
			return false;

		// Look for "while" or "until"
		if (m_token.m_text.equals("while") || m_token.m_text.equals("until")) {

			// Is this a negative condition?
			boolean negative = m_token.m_text.equals("until");

			// Skip "while" or "until"
			if (!GetToken())
				return false;

			// Generate code to evaluate expression
			if (!CompileExpression())
				return false;

			// Generate code to convert to integer
			if (!CompileConvert(BasicValType.VTP_INT.getType()))
				return false;

			// Free any temporary data expression may have created
			if (!CompileFreeTempData())
				return false;

			// Create flow control structure
			m_flowControl.add(new compFlowControl(
					compFlowControlType.FCT_DO_PRE, m_vm.InstructionCount(),
					loopPos, line, col));

			// Create conditional jump
			AddInstruction(negative ? OpCode.OP_JUMP_TRUE
					: OpCode.OP_JUMP_FALSE, BasicValType.VTP_INT.getType(),
					new VMValue(0));

			// Done
			return true;
		} else {

			// Post condition DO.
			// Create flow control structure
			m_flowControl.add(new compFlowControl(
					compFlowControlType.FCT_DO_POST, m_vm.InstructionCount(),
					loopPos, line, col));
			return true;
		}
	}

	boolean CompileLoop() {

		if (!(FlowControlTopIs(compFlowControlType.FCT_DO_PRE) || FlowControlTopIs(compFlowControlType.FCT_DO_POST))) {
			SetError("'loop' without 'do'");
			return false;
		}

		// Find DO details
		compFlowControl top = FlowControlTOS();
		m_flowControl.remove(m_flowControl.size() - 1);

		// Skip "DO"
		if (!GetToken())
			return false;

		// Look for "while" or "until"
		if (m_token.m_text.equals("while") || m_token.m_text.equals("until")) {

			// This must be a post condition "do"
			if (top.m_type != compFlowControlType.FCT_DO_POST) {
				SetError("'until' or 'while' condition has already been specified for this 'do'");
				return false;
			}

			// Is this a negative condition?
			boolean negative = m_token.m_text.equals("until");

			// Skip "while" or "until"
			if (!GetToken())
				return false;

			// Generate code to evaluate expression
			if (!CompileExpression())
				return false;

			// Generate code to convert to integer
			if (!CompileConvert(BasicValType.VTP_INT.getType()))
				return false;

			// Free any temporary data expression may have created
			if (!CompileFreeTempData())
				return false;

			// Create conditional jump back to "do"
			AddInstruction(negative ? OpCode.OP_JUMP_FALSE
					: OpCode.OP_JUMP_TRUE, BasicValType.VTP_INT.getType(), new VMValue(
							top.m_jumpLoop));

			// Done
			return true;
		} else {

			// Jump unconditionally back to "do"
			AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT.getType(), new VMValue(
					top.m_jumpLoop));

			// If this is a precondition "do", fixup the jump around the "do"
			// block
			if (top.m_type == compFlowControlType.FCT_DO_PRE) {
				assert (top.m_jumpOut < m_vm.InstructionCount());
				m_vm.Instruction(top.m_jumpOut).mValue.setIntVal(m_vm
						.InstructionCount());
			}

			// Done
			return true;
		}
	}

	boolean CheckName(String name) {

		// Check that name is a suitable variable, structure or structure field
		// name.
		if (m_constants.containsKey(name)
				|| m_programConstants.containsKey(name)) {
			SetError("'" + name + "' is a constant, and cannot be used here");
			return false;
		}
		if (m_reservedWords.contains(name)) {
			SetError("'" + name
					+ "' is a reserved word, and cannot be used here");
			return false;
		}
		return true;
	}
	public void AddConstants(Map<String, compConstant> constants){
		if (constants == null)
			return;
		//TODO Check if constant already exists before adding
		for (String key: constants.keySet())
			m_constants.put(key, constants.get(key));
	}
	public void AddFunctions(Map<String, List<Function>> functions,
			Map<String, List<compFuncSpec>> specs) {
		int specIndex;
		int vmIndex;
		int i;

		compFuncSpec spec;
		if(functions == null|| specs == null)
			return;
		for (String name : functions.keySet()) {
			i = 0;
			for (Function func : functions.get(name)) {
				// Register wrapper function to virtual machine
				vmIndex = m_vm.AddFunction(func);

				// Register function spec to compiler
				specIndex = m_functions.size();
				spec = specs.get(name).get(i);
				// TODO Add handling for if spec is null or i is out of bounds
				spec.setIndex(vmIndex);

				m_functions.add(spec);

				// Add function name . function spec mapping
				List<Integer> l = m_functionIndex.get(name.toLowerCase());
				if (l == null) {
					l = new ArrayList<Integer>();
					m_functionIndex.put(name.toLowerCase(), l);
				}
				l.add(specIndex);

				i++;
			}

		}

	}

	boolean CompileFunction() {
		return CompileFunction(false);
	}

	boolean CompileFunction(boolean needResult) {

		// Find function specifications.
		// (Note: There may be more than one with the same name.
		// We collect the possible candidates in an array, and prune out the
		// ones
		// whose paramater types are incompatible as we go..)
		compExtFuncSpec[] functions = new compExtFuncSpec[TC_MAXOVERLOADEDFUNCTIONS];
		int functionCount = 0;

		// Find builtin functions
		boolean found = false;
		for (Integer i : m_functionIndex.get(m_token.m_text)) {
			if (!(functionCount < TC_MAXOVERLOADEDFUNCTIONS))
				break;
			compFuncSpec spec = m_functions.get(i); // Get specification
			found = true;

			// Check whether function returns a value (if we need one)
			if (!needResult || spec.isFunction()) {
				if (functions[functionCount] == null)
					functions[functionCount] = new compExtFuncSpec();
				functions[functionCount].m_spec = spec;
				functions[functionCount].m_builtin = true;
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
				SetError(m_token.m_text + " does not return a value");
				return false;
			} else {
				SetError(m_token.m_text + " is not a recognised function name");
				return false;
			}
		}

		// Skip function name token
		if (!GetToken())
			return false;

		// Only the first instance will be checked to see whether we need
		// brackets.
		// (Therefore either all instances should have brackets, or all
		// instances
		// should have no brackets.)
		boolean brackets = functions[0].m_spec.hasBrackets();
		if (m_syntax == compLanguageSyntax.LS_TRADITIONAL && brackets) { // Special
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
			for (int i = 0; i < functionCount && !brackets; i++)
				brackets = functions[i].m_spec.isFunction();
			// && functions.get(i).m_paramTypes.Params().size() > 0; // Need to
			// rethink below loop before we can enable this
		}

		// Expect opening bracket
		if (brackets) {
			if (!m_token.m_text.equals("(")) {
				SetError("Expected '('");
				return false;
			}
			// Skip it
			if (!GetToken())
				return false;
		}

		// Generate code to push parameters
		boolean first = true;
		int count = 0;
		int pushCount = 0; // Usually pushCount = count (the parameter count).
		// However "any type" parameters also have their
		// data type pushed with them, in which case
		// pushCount > count.
		while (functionCount > 0 && !m_token.m_text.equals(")")
				&& !AtSeparatorOrSpecial()) {

			// Trim functions with less parameters than we have found
			int src, dst;
			dst = 0;
			for (src = 0; src < functionCount; src++)
				if (functions[src].m_spec.getParamTypes().Params().size() > count)
					functions[dst++] = functions[src];
			functionCount = dst;

			// None left?
			if (functionCount == 0) {
				if (brackets)
					SetError("Expected ')'");
				else
					SetError("Expected ':' or end of line");
				return false;
			}

			if (!first) {
				// Expect comma
				if (!m_token.m_text.equals(",")) {
					SetError("Expected ','");
					return false;
				}
				// Skip it
				if (!GetToken())
					return false;
			}
			first = false;

			// Generate code to evaluate parameter
			if (!CompileExpression())
				return false;

			// Find first valid function which matches at this parameter
			int matchIndex = -1;
			boolean isAnyType = false;
			int i;
			for (i = 0; i < functionCount && matchIndex < 0; i++) {
				ValType type = functions[i].m_spec.getParamTypes().Params()
						.get(count);

				// Check for undefined type parameter
				if (type.Equals(BasicValType.VTP_UNDEFINED)) {

					// Function definition indicates whether parameter type is
					// valid
					// via a compiler time callback function.
					if (functions[i].m_spec.getParamValidationCallback().run(
							count, m_regType)) {

						// Found "any type" match
						matchIndex = i;
						isAnyType = true;
					} else
						SetError("Incorrect data type");
				} else {

					// Specific type requested.
					// Check parameter can be converted to that type
					if (CompileConvert(type)) {

						// Found specific type match
						matchIndex = i;
						isAnyType = false;
					}
				}
			}

			if (matchIndex >= 0) {

				// Clear any errors that non-matching instances might have set.
				ClearError();

				ValType type = functions[matchIndex].m_spec.getParamTypes()
						.Params().get(count);

				// Filter out all functions whose "count" parameter doesn't
				// match "type".
				dst = 0;
				for (src = 0; src < functionCount; src++) {

					if (isAnyType) {
						// If the first function to match accepts an "any type"
						// parameter, then all other overloads must be an
						// "any type"
						// parameter.
						if (functions[src].m_spec.getParamValidationCallback()
								.run(count, m_regType))
							functions[dst++] = functions[src];
					} else {
						// Likewise if the first function to match requires a
						// specific
						// parameter type, then all other overloads must require
						// that
						// same type.
						if (functions[src].m_spec.getParamTypes().Params()
								.get(count) == type)
							functions[dst++] = functions[src];
					}
				}
				functionCount = dst;
				assert (functionCount > 0); // (Should at least have the
				// function that originally matched
				// the register)

				// Generate code to push parameter to stack
				CompilePush();
				pushCount++;

				// If parameter is an "any type" then generate code to push the
				// parameter type to the stack.
				if (isAnyType) {
					AddInstruction(OpCode.OP_LOAD_CONST,
							BasicValType.VTP_INT.getType(),
							new VMValue((int) m_vm.StoreType(m_regType)));
					m_regType.Set(BasicValType.VTP_INT);
					CompilePush();
					pushCount++;
				}
			} else
				return false; // No function matched. (Return last compile
			// convert error).

			count++; // Count parameters pushed
		}

		// Find the first function instance that accepts this number of
		// parameters
		int matchIndex = -1;
		int i;
		for (i = 0; i < functionCount && matchIndex < 0; i++)
			if (functions[i].m_spec.getParamTypes().Params().size() == count)
				matchIndex = i;
		if (matchIndex < 0) {
			if (count == 0)
				SetError("Expected function parameter");
			else
				SetError("Expected ','");
			return false;
		}
		compExtFuncSpec spec = functions[matchIndex];

		// Expect closing bracket
		if (brackets) {
			if (!m_token.m_text.equals(")")) {
				SetError("Expected ')'");
				return false;
			}
			// Skip it
			if (!GetToken())
				return false;
		}

		// Generate code to call function
		if (spec.m_builtin)

			// Builtin function
			AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT.getType(),
					new VMValue(spec.m_spec.getIndex()));
		else

			// DLL function
			// Note: The DLL index is encoded as the high byte.
			// The 3 low bytes are the function index within the DLL.
			// This may be revised later...
			AddInstruction(
					OpCode.OP_CALL_DLL,
					BasicValType.VTP_INT.getType(),
					new VMValue((spec.m_pluginIndex << 24)
							| (spec.m_spec.getIndex() & 0x00ffffff)));

		// If function has return type, it will have changed the type in the
		// register
		if (spec.m_spec.isFunction()) {
			m_regType.Set(spec.m_spec.getReturnType());

			// If data is too large to fit in the register, it will be returned
			// in the "temp" area. If the data contains strings, they will need
			// to
			// be "destroyed" when temp data is unwound.
			if (!m_regType.CanStoreInRegister()
					&& m_vm.DataTypes().ContainsString(m_regType))
				AddInstruction(OpCode.OP_REG_DESTRUCTOR,
						BasicValType.VTP_INT.getType(),
						new VMValue((int) m_vm.StoreType(m_regType)));

			if (!CompileDataLookup(false))
				return false;
		}

		// Note whether function has generated temporary data
		m_freeTempData = m_freeTempData | spec.m_spec.getFreeTempData();

		// Generate code to clean up stack
		for (int i2 = 0; i2 < pushCount; i2++)
			if (!CompilePop())
				return false;

		// Generate explicit timesharing break (if necessary)
		if (spec.m_spec.getTimeshare())
			AddInstruction(OpCode.OP_TIMESHARE, BasicValType.VTP_INT.getType(),
					new VMValue());

		return true;
	}

	boolean CompileConstant() {

		// Skip CONST
		if (!GetToken())
			return false;

		// Expect at least one field in dim
		if (AtSeparatorOrSpecial()) {
			SetError("Expected constant declaration");
			return false;
		}

		// Parse fields in dim
		boolean needComma = false; // First element doesn't need a comma
		while (!AtSeparatorOrSpecial()) {

			// Handle commas
			if (needComma) {
				if (!m_token.m_text.equals(",")) {
					SetError("Expected ','");
					return false;
				}
				if (!GetToken())
					return false;
			}
			needComma = true; // Remaining elements do need commas

			// Read constant name
			if (m_token.m_type != TokenType.CTT_TEXT) {
				SetError("Expected constant name");
				return false;
			}
			String name = m_token.m_text;
			if (m_programConstants.containsKey(name)) {
				SetError("'" + name
						+ "' has already been declared as a constant.");
				return false;
			}
			if (!CheckName(name))
				return false;
			if (!GetToken())
				return false;

			// Determine constant type from last character of constant name
			Integer type = BasicValType.VTP_UNDEFINED.getType();
			if (name.length() > 0) {
				char last = name.charAt(name.length() - 1);
				if (last == '$')
					type = BasicValType.VTP_STRING.getType();
				else if (last == '#')
					type = BasicValType.VTP_REAL.getType();
				else if (last == '%')
					type = BasicValType.VTP_INT.getType();
			}

			if (m_token.m_text.equals("as")) {
				if (type != BasicValType.VTP_UNDEFINED.getType()) {
					SetError("'"
							+ name
							+ "'s type has already been defined. Cannot use 'as' here.");
					return false;
				}
				if (!GetToken())
					return false;
				if (m_token.m_text.equals("integer"))
					type = BasicValType.VTP_INT.getType();
				else if (m_token.m_text.equals("single")
						|| m_token.m_text.equals("double"))

					// Note: Basic4GL supports only one type of floating point
					// number.
					// We will accept both keywords, but simply allocate a real
					// (= single
					// precision) floating point number each time.
					type = BasicValType.VTP_REAL.getType();
				else if (m_token.m_text.equals("string"))
					type = BasicValType.VTP_STRING.getType();
				else {
					SetError("Expected 'integer', 'single', 'double', 'string'");
					return false;
				}
				if (!GetToken())
					return false;
			}

			// Default type to integer if not defined
			if (type == BasicValType.VTP_UNDEFINED.getType())
				type = BasicValType.VTP_INT.getType();

			// Expect =
			if (!m_token.m_text.equals("=")) {
				SetError("Expected '='");
				return false;
			}
			if (!GetToken())
				return false;

			// Compile constant expression
			VMValue value = new VMValue();
			String stringValue = "";

			Mutable<Integer> typeRef = new Mutable<Integer>(type);
			Mutable<VMValue> valueRef = new Mutable<VMValue>(value);
			Mutable<String> stringValueRef = new Mutable<String>(stringValue);

			if (!EvaluateConstantExpression(typeRef, valueRef, stringValueRef))
				return false;

			// Update local values from references
			type = typeRef.get();
			value = valueRef.get();
			stringValue = stringValueRef.get();

			switch (BasicValType.getType(type)) {
			case VTP_INT:
				m_programConstants.put(name,
						new compConstant(value.getIntVal()));
				break;
			case VTP_REAL:
				m_programConstants.put(name,
						new compConstant(value.getRealVal()));
				break;
			case VTP_STRING:
				m_programConstants.put(name, new compConstant(
						(String) ("S" + stringValue)));
				break;
			default:
				break;
			}

		}
		return true;
	}

	boolean CompileFreeTempData() {

		// Add instruction to free temp data (if necessary)
		if (m_freeTempData)
			AddInstruction(OpCode.OP_FREE_TEMP, BasicValType.VTP_INT.getType(),
					new VMValue());
		m_freeTempData = false;

		return true;
	}

	boolean CompileExtendedUnOperation(short operOpCode) {

		Mutable<ValType> type = new Mutable<ValType>(new ValType());
		Mutable<Integer> opFunc = new Mutable<Integer>(-1);
		Mutable<Boolean> freeTempData = new Mutable<Boolean>(false);
		Mutable<ValType> resultType = new Mutable<ValType>(new ValType());
		boolean found = false;

		// Iterate through external operator extension functions until we find
		// one that can handle our data.
		for (int i = 0; i < m_unOperExts.size() && !found; i++) {

			// Setup input data
			type.get().Set(m_regType);
			opFunc.set(-1);
			freeTempData.set(false);
			resultType.set(new ValType());

			// Call function
			found = m_unOperExts.get(i).run(type, operOpCode, opFunc, resultType,
					freeTempData);
		}

		if (!found) // No handler found.
			return false; // This is not an error, but operation must be
		// passed through to default operator handling.

		// Generate code to convert operands as necessary
		boolean conv = CompileConvert(type.get());
		assert (conv);

		// Generate code to call external operator function
		assert (opFunc.get() >= 0);
		assert (opFunc.get() < m_vm.OperatorFunctionCount());
		AddInstruction(OpCode.OP_CALL_OPERATOR_FUNC, BasicValType.VTP_INT.getType(),
				new VMValue(opFunc.get()));

		// Set register to result type
		m_regType.Set(resultType.get());

		// Record whether we need to free temp data
		m_freeTempData = m_freeTempData || freeTempData.get();

		return true;
	}

	boolean CompileExtendedBinOperation(short operOpCode) {

		Mutable<ValType> type1 = new Mutable<ValType>(new ValType());
		Mutable<ValType> type2 = new Mutable<ValType>(new ValType());
		Mutable<Integer> opFunc = new Mutable<Integer>(-1);
		Mutable<Boolean> freeTempData = new Mutable<Boolean>(false);
		Mutable<ValType> resultType = new Mutable<ValType>(new ValType());
		boolean found = false;

		// Iterate through external operator extension functions until we find
		// one that can handle our data.
		for (int i = 0; i < m_binOperExts.size() && !found; i++) {

			// Setup input data
			type1.get().Set(m_regType);
			type2.get().Set(m_reg2Type);
			opFunc.set(-1);
			freeTempData.set(false);
			resultType.set(new ValType());

			// Call function
			found = m_binOperExts.get(i).run(type1, type2, operOpCode, opFunc,
					resultType, freeTempData);

		}

		if (!found) // No handler found.
			return false; // This is not an error, but operation must be
		// passed through to default operator handling.

		// Generate code to convert operands as necessary
		boolean conv1 = CompileConvert(type1.get());
		boolean conv2 = CompileConvert2(type2.get());
		assert (conv1);
		assert (conv2);

		// Generate code to call external operator function
		assert (opFunc.get() >= 0);
		assert (opFunc.get() < m_vm.OperatorFunctionCount());
		AddInstruction(OpCode.OP_CALL_OPERATOR_FUNC, BasicValType.VTP_INT.getType(),
				new VMValue(opFunc.get()));

		// Set register to result type
		m_regType.Set(resultType.get());

		// Record whether we need to free temp data
		m_freeTempData = m_freeTempData || freeTempData.get();

		return true;
	}

	public String FunctionName(int index) // Find function name for function #.
	// Used for debug reporting
	{
		for (String key : m_functionIndex.keySet()) {
			for (Integer i : m_functionIndex.get(key))
				if (i.equals(index))
					return key;
		}
		return "???";

	}

	boolean CompileAlloc() {

		// Skip "alloc"
		if (!GetToken())
			return false;

		// Expect &pointer variable
		if (m_token.m_text.equals("&")) {
			SetError("First argument must be a pointer");
			return false;
		}

		// Load pointer
		if (!(CompileLoadVar() && CompileTakeAddress()))
			return false;

		// Store pointer type
		ValType ptrType = new ValType(m_regType), dataType = new ValType(
				m_regType);
		dataType.m_byRef = false;
		dataType.m_pointerLevel--;

		// Get pointer address
		if (!CompileTakeAddress()) {
			SetError("First argument must be a pointer");
			return false;
		}

		// Push destination address to stack
		if (!CompilePush())
			return false;

		// Generate code to push array dimensions (if any) to the stack.
		int i;
		for (i = 0; i < dataType.m_arrayLevel; i++) {

			// Expect ,
			if (!m_token.m_text.equals(",")) {
				SetError("Expected ','");
				return false;
			}
			if (!GetToken())
				return false;

			// Generate code to evaluate array index, and convert to an integer.
			if (!CompileExpression())
				return false;
			if (!CompileConvert(BasicValType.VTP_INT.getType())) {
				SetError("Array index must be a number. "
						+ m_vm.DataTypes().DescribeVariable("", m_regType)
						+ " is not a number");
				return false;
			}

			// Push array index to stack
			if (!CompilePush())
				return false;
		}

		// Add alloc instruction
		AddInstruction(OpCode.OP_ALLOC, BasicValType.VTP_INT.getType(), new VMValue(
				(int) m_vm.StoreType(dataType)));

		// Instruction automatically removes all array indices that were pushed
		// to
		// the stack.
		for (i = 0; i < dataType.m_arrayLevel; i++)
			m_operandStack.remove(m_operandStack.size() - 1);

		// Instruction also automatically leaves the register pointing to the
		// new
		// data.
		m_regType.Set(ptrType);
		m_regType.m_byRef = false;

		// Generate code to pop destination address
		if (!CompilePop())
			return false;

		// Generate code to save address to pointer
		AddInstruction(OpCode.OP_SAVE, BasicValType.VTP_INT.getType(), new VMValue());

		return true;
	}

	compParserPos SavePos() {

		// Save the current parser position, so we can return to it later.
		compParserPos pos = new compParserPos();
		pos.m_line = m_parser.Line();
		pos.m_col = m_parser.Col();
		pos.m_token = m_token;
		return pos;
	}

	void RestorePos(compParserPos pos) {

		// Restore parser position
		m_parser.SetPos(pos.m_line, pos.m_col);
		m_token = pos.m_token;
	}

	// Debugging
	public String DescribeStackCall(int returnAddr) {

		// Return a string describing the gosub call
		if (returnAddr == 0 || returnAddr >= m_vm.InstructionCount())
			return "???";

		// Look at instruction immediately before return address.
		// This should be the gosub
		if (m_vm.Instruction(returnAddr - 1).mOpCode != OpCode.OP_CALL)
			return "???";

		// Get target address
		long target = m_vm.Instruction(returnAddr - 1).mValue.getIntVal();

		// Lookup label name
		String name = m_labelIndex.get(target);
		if (name == null)
			return "???";

		// Return label name
		return name;
	}

	public boolean TempCompileExpression(String expression, ValType valType,
			boolean inFunction, int currentFunction) {

		// Load expression into parser
		m_parser.SourceCode().clear();
		m_parser.SourceCode().add(expression);
		m_parser.Reset();
		m_lastLine = 0;
		m_lastCol = 0;

		// Reset compiler state
		ClearState();
		m_inFunction = inFunction;
		m_currentFunction = currentFunction;

		// Clear error state
		ClearError();
		m_parser.ClearError();

		// Read first token
		if (!GetToken(true, false))
			return false;

		// Compile code
		if (!CompileExpression())
			return false;

		if (m_token.m_type != TokenType.CTT_EOL) {
			SetError("Extra characters after expression");
			return false;
		}

		// Terminate program
		AddInstruction(OpCode.OP_END, BasicValType.VTP_INT.getType(), new VMValue());

		// Return expression result type
		valType.Set(m_regType);
		return true;
	}

	boolean CompileData() {

		// Skip "data"
		if (!GetToken(false, true)) // Use "DATA" mode read
			return false;

		// Compile data elements
		boolean needComma = false;
		do {

			// Handle commas
			if (needComma) {
				if (!m_token.m_text.equals(",")) {
					SetError("Expected ','");
					return false;
				}
				if (!GetToken(false, true))
					return false;
			}
			needComma = true; // Remaining elements do need commas

			// Consecutive commas?
			if (m_token.m_text.equals(",") || AtSeparatorOrSpecial()) {

				// Store a blank string
				m_vm.StoreProgramData(BasicValType.VTP_STRING, new VMValue(0));
			} else {

				// Extract value
				VMValue v = new VMValue();
				if (m_token.m_valType == BasicValType.VTP_STRING) {

					// Allocate new string constant
					String text = m_token.m_text.substring(1,
							m_token.m_text.length() - 1); // Remove S prefix
					v.setIntVal(m_vm.StoreStringConstant(text));
				} else if (m_token.m_valType == BasicValType.VTP_INT)
					v.setIntVal(Cast.StringToInt(m_token.m_text));
				else
					v.setRealVal(Float.valueOf(m_token.m_text));

				// Store data in VM
				m_vm.StoreProgramData(m_token.m_valType, v);

				// Next token
				if (!GetToken())
					return false;
			}
		} while (!AtSeparatorOrSpecial());
		return true;
	}

	boolean CompileDataRead() {

		// Skip "read"
		if (!GetToken())
			return false;

		// Expect at one variable name
		if (AtSeparatorOrSpecial()) {
			SetError("Expected variable name");
			return false;
		}

		// Parse fields in dim
		boolean needComma = false; // First element doesn't need a comma
		while (!AtSeparatorOrSpecial()) {

			// Handle commas
			if (needComma) {
				if (!m_token.m_text.equals(",")) {
					SetError("Expected ','");
					return false;
				}
				if (!GetToken())
					return false;
			}
			needComma = true; // Remaining elements do need commas

			// Generate code to load target variable address
			if (!CompileLoadVar())
				return false;

			// Must be a basic type.
			ValType type = new ValType(m_regType);
			if (!type.IsBasic()) {
				SetError("Can only READ built in types (int, real or string)");
				return false;
			}

			// Convert load target variable into take address of target variable
			if (!CompileTakeAddress()) {
				SetError("Value cannot be READ into");
				return false;
			}

			if (!CompilePush())
				return false;

			// Generate READ op-code
			AddInstruction(OpCode.OP_DATA_READ, type.m_basicType,
					new VMValue());

			if (!CompilePop())
				return false;

			// Save reg into [reg2]
			AddInstruction(OpCode.OP_SAVE, m_reg2Type.m_basicType,
					new VMValue());
		}
		return true;
	}

	boolean CompileDataReset() {

		// Skip "reset"
		if (!GetToken())
			return false;

		// If label specified, use offset stored in label
		if (!AtSeparatorOrSpecial()) {

			// Validate label
			if (m_token.m_type != TokenType.CTT_TEXT) {
				SetError("Expected label name");
				return false;
			}

			// Record reset, so that we can fix up the offset in the second
			// compile pass.
			String labelName = m_symbolPrefix + m_token.m_text;
			m_resets.add(new compJump(m_vm.InstructionCount(), labelName));

			// Skip label name
			if (!GetToken())
				return false;
		}

		// Add jump instruction
		AddInstruction(OpCode.OP_DATA_RESET, BasicValType.VTP_INT.getType(),
				new VMValue(0));

		return true;
	}

	boolean EvaluateConstantExpression(Mutable<Integer> basictype,
			Mutable<VMValue> result, Mutable<String> stringResult) {

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
		int expressionStart = m_vm.InstructionCount();

		// Compile expression, specifying that it must be constant
		if (!CompileExpression(true))
			return false;

		// Convert to required type
		if (basictype.get() != BasicValType.VTP_UNDEFINED.getType())
			if (!CompileConvert(basictype.get()))
				return false;

		// Add "end program" opcode, so we can safely evaluate it
		AddInstruction(OpCode.OP_END, BasicValType.VTP_INT.getType(), new VMValue());

		// Setup virtual machine to execute expression
		// Note: Expressions can't branch or loop, and it's very difficult to
		// write
		// one that evaluates to a large number of op-codes. Therefore we won't
		// worry
		// about processing windows messages or checking for pause state etc.
		m_vm.ClearError();
		m_vm.GotoInstruction(expressionStart);
		try {
			do {
				m_vm.Continue(1000);
			} while (!m_vm.Error() && !m_vm.Done());
		} catch (Exception e) {
			SetError("Error evaluating constant expression");
			return false;
		}
		if (m_vm.Error()) {
			SetError("Error evaluating constant expression");
			return false;
		}

		// Now we have the result type of the constant expression,
		// AND the virtual machine has its value stored in the register.

		// Roll back all the expression op-codes
		m_vm.GotoInstruction(0);
		m_vm.RollbackProgram(expressionStart);

		// Set return values
		basictype.set(m_regType.m_basicType);
		if (basictype.get() == BasicValType.VTP_STRING.getType())
			stringResult.set(m_vm.RegString());
		else
			result.set(m_vm.Reg());

		return true;
	}

	boolean CompileConstantExpression() {
		return CompileConstantExpression(BasicValType.VTP_UNDEFINED.getType());
	}

	boolean CompileConstantExpression(int basictype) {

		// Evaluate constant expression
		VMValue value = new VMValue();
		String stringValue = "";

		// Create wrappers to pass values by reference
		Mutable<Integer> typeRef = new Mutable<Integer>(basictype);
		Mutable<VMValue> valueRef = new Mutable<VMValue>(value);
		Mutable<String> stringValueRef = new Mutable<String>(stringValue);

		if (!EvaluateConstantExpression(typeRef, valueRef, stringValueRef))
			return false;

		// Update local values from wrappers
		basictype = typeRef.get();
		value = valueRef.get();
		stringValue = stringValueRef.get();

		// Generate "load constant" instruction
		if (basictype == BasicValType.VTP_STRING.getType()) {

			// Create string constant entry if necessary
			int index = m_vm.StoreStringConstant(stringValue);
			AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING.getType(),
					new VMValue(index));
		} else
			AddInstruction(OpCode.OP_LOAD_CONST, basictype, value);

		return true;
	}

	compFuncSpec FindFunction(String name, int paramCount) {

		// Search for function with matching name & param count
		List<Integer> l = m_functionIndex.get(name);
		if (l != null)
			for (Integer i : l) {
				compFuncSpec spec = m_functions.get(i);
				if (spec.getParamTypes().Params().size() == paramCount)
					return spec;
			}

		// None found
		SetError((String) "'" + name + "' function not found");
		return null;
	}

	/*
	boolean CompilePrint(boolean forceNewLine) {

		// The print function has a special syntax, and must be compiled
		// separtely

		// Skip "print"
		if (!GetToken())
			return false;

		boolean foundSemiColon = false;
		int operandCount = 0;
		while (!AtSeparatorOrSpecial()) {

			// Look for semicolon
			if (m_token.m_text.equals(";")) {

				// Record it, and move on to next
				foundSemiColon = true;
				if (!GetToken())
					return false;
			} else {
				foundSemiColon = false;

				// If this is not the first operand, then there will be a string
				// sitting in the register. Need to push it first.
				if (operandCount > 0)
					if (!CompilePush())
						return false;

				// Evaluate expression & convert it to string
				if (!(CompileExpression() && CompileConvert(BasicValType.VTP_STRING)))
					return false;

				operandCount++;
			}
		}

		// Add all operands together
		while (operandCount > 1) {
			if (!CompilePop())
				return false;
			AddInstruction(OpCode.OP_OP_PLUS, BasicValType.VTP_STRING,
					new VMValue());
			m_regType.Set(BasicValType.VTP_STRING);

			operandCount--;
		}

		// Push string as function parameter
		if (operandCount == 1)
			if (!CompilePush())
				return false;

		// Find print/printr function
		boolean newLine = forceNewLine
				|| ((m_syntax == compLanguageSyntax.LS_TRADITIONAL || m_syntax == compLanguageSyntax.LS_TRADITIONAL_PRINT) && !foundSemiColon);

		if (!newLine && operandCount == 0) // Nothing to print?
			return true; // Do nothing!

		compFuncSpec spec = FindFunction(newLine ? "printr" : "print",
				operandCount);
		if (spec == null)
			return false;

		// Generate code to call it
		AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT,
				new VMValue(spec.getIndex()));

		// Generate code to clean up stack
		if (operandCount == 1)
			if (!CompilePop())
				return false;

		return true;
	}*/

	boolean CompileInput() {

		// Input also has a special syntax.
		// This still isn't a complete input implementation, as it doesn't
		// support
		// inputting multiple values on one line.
		// But it's a lot better than before.

		// Skip "input"
		if (!GetToken())
			return false;

		// Check for prompt
		if (m_token.m_type == TokenType.CTT_CONSTANT
				&& m_token.m_valType == BasicValType.VTP_STRING) {

			// Allocate new string constant
			String text = m_token.m_text.substring(1,
					m_token.m_text.length() - 1); // Remove
			// S
			// prefix

			if (!GetToken())
				return false;

			// Expect , or ;
			if (m_token.m_text.equals(";"))
				text = text + "? ";
			else if (!m_token.m_text.equals(",")) {
				SetError("Expected ',' or ';'");
				return false;
			}
			if (!GetToken())
				return false;

			// Create new string constant
			int index = m_vm.StoreStringConstant(text);

			// Generate code to print it (load, push, call "print" function)
			AddInstruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING.getType(),
					new VMValue(index));
			m_regType.Set(BasicValType.VTP_STRING);
			if (!CompilePush())
				return false;

			// Generate code to call "print" function
			compFuncSpec printSpec = FindFunction("print", 1);
			if (printSpec == null)
				return false;
			AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT.getType(),
					new VMValue(printSpec.getIndex()));

			// Generate code to clean up stack
			if (!CompilePop())
				return false;
		}

		// Generate code to effectively perform
		// variable = Input$()
		// or
		// variable = val(Input$())
		//
		// (Depending on whether variable is a string)

		// Generate code to load target variable
		if (!CompileLoadVar())
			return false;

		// Must be a simple variable
		if (!m_regType.IsBasic()) {
			SetError("Input variable must be a basic string, integer or real type");
			return false;
		}
		Integer variableType = m_regType.m_basicType;

		// Generate code to push its address to stack
		if (!(CompileTakeAddress() && CompilePush()))
			return false;

		// Generate code to call "input$()" function
		compFuncSpec inputSpec = FindFunction("input$", 0);
		if (inputSpec == null)
			return false;
		AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT.getType(),
				new VMValue(inputSpec.getIndex()));
		AddInstruction(OpCode.OP_TIMESHARE, BasicValType.VTP_INT.getType(),
				new VMValue()); // Timesharing break
		// is necessary
		m_regType.Set(BasicValType.VTP_STRING);

		// If the variable is not a string, then we need to convert it to the
		// target
		// type. We do this by inserting an implicit call to the val() function.
		if (variableType != BasicValType.VTP_STRING.getType()) {

			// Push register back as input to val function
			if (!CompilePush())
				return false;

			// Generate code to call "val()" function
			compFuncSpec valSpec = FindFunction("val", 1);
			if (valSpec == null)
				return false;
			AddInstruction(OpCode.OP_CALL_FUNC, BasicValType.VTP_INT.getType(),
					new VMValue(valSpec.getIndex()));
			m_regType.Set(BasicValType.VTP_REAL);

			// Clean up stack
			if (!CompilePop())
				return false;
		}

		// Generate code to pop target address into reg2
		if (!CompilePop())
			return false;

		if (!CompileConvert(m_reg2Type.m_basicType)) {
			SetError("Types do not match"); // Technically this should never
			// actually happen
			return false;
		}

		// Generate code to save value
		AddInstruction(OpCode.OP_SAVE, m_reg2Type.m_basicType, new VMValue());

		return true;
	}

	boolean CompileLanguage() {

		// Compile language directive
		// Skip "language"
		if (!GetToken())
			return false;

		// Expect syntax type
		if (m_token.m_text.equals("traditional"))
			m_syntax = compLanguageSyntax.LS_TRADITIONAL;
		else if (m_token.m_text.equals("basic4gl"))
			m_syntax = compLanguageSyntax.LS_BASIC4GL;
		else if (m_token.m_text.equals("traditional_print"))
			m_syntax = compLanguageSyntax.LS_TRADITIONAL_PRINT;
		else {
			SetError("Expected 'traditional', 'basic4gl' or 'traditional_print'");
			return false;
		}

		// Skip syntax token
		if (!GetToken())
			return false;

		return true;
	}

	boolean CompileUserFunctionFwdDecl() {

		// Skip "declare"
		if (!GetToken())
			return false;

		// Look for "sub" or "function"
		return CompileUserFunction(compUserFunctionType.UFT_FWDDECLARATION);
	}

	boolean CompileUserFunctionRuntimeDecl() {
		// Skip "runtime"
		if (!GetToken())
			return false;

		return CompileUserFunction(compUserFunctionType.UFT_RUNTIMEDECLARATION);
	}

	boolean CompileUserFunction(compUserFunctionType funcType) {
		// Function or sub?
		boolean hasReturnVal;
		if (m_token.m_text == "function")
			hasReturnVal = true;
		else if (m_token.m_text == "sub")
			hasReturnVal = false;
		else {
			SetError("Expected 'sub' or 'function'");
			return false;
		}

		// Check that we are not already inside a function
		if (m_inFunction) {
			SetError("Cannot define a function or subroutine inside another function or subroutine");
			return false;
		}

		// Check that we are not inside a control structure
		if (!CheckUnclosedFlowControl())
			return false;

		// Mark start of function in source code
		m_functionStart.m_sourceLine = m_parser.Line();
		m_functionStart.m_sourceCol = m_parser.Col();

		// Skip "func"
		if (!GetToken())
			return false;

		// Compile data type
		TokenType tokenType = TokenType.CTT_CONSTANT;
		ValType type = new ValType(BasicValType.VTP_UNDEFINED);
		String name = "";
		Mutable<TokenType> tokenTypeRef = new Mutable<TokenType>(tokenType);
		Mutable<ValType> typeRef = new Mutable<ValType>(type);
		Mutable<String> nameRef = new Mutable<String>(name);

		if (hasReturnVal) {
			if (!CompileDataType(nameRef, typeRef, tokenTypeRef))
				return false;
			// Update local values from references
			tokenType = tokenTypeRef.get();
			type = typeRef.get();
			name = nameRef.get();

		} else {
			if (!CompileTokenName(nameRef, tokenTypeRef, false))
				return false;
			// Update local values from references
			type = typeRef.get();
			name = nameRef.get();
		}

		// Validate function name
		if (tokenType != TokenType.CTT_TEXT
				&& tokenType != TokenType.CTT_USER_FUNCTION
				&& tokenType != TokenType.CTT_RUNTIME_FUNCTION) {
			if (tokenType == TokenType.CTT_FUNCTION)
				SetError("'"
						+ name
						+ "' has already been used as a built-in function/subroutine name");
			else
				SetError("Expected a function/subroutine name");
			return false;
		}

		// Must not be a variable name
		if (m_vm.Variables().GetVar(name) >= 0) {
			SetError("'" + name + "' has already been used as a variable name");
			return false;
		}

		// Must not be a structure name
		if (m_vm.DataTypes().GetStruc(name) >= 0) {
			SetError("'" + name + "' has already been used as a structure name");
			return false;
		}

		// Allocate a new user function
		m_userFuncPrototype.Reset();

		// Expect "("
		if (!m_token.m_text.equals("(")) {
			SetError("Expected '('");
			return false;
		}
		if (!GetToken())
			return false;

		// Look for function parameters
		if (!m_token.m_text.equals(")")) {
			if (!CompileDim(false, true))
				return false;
		}

		// Expect ")"
		if (!m_token.m_text.equals(")")) {
			SetError("Expected ')'");
			return false;
		}
		if (!GetToken())
			return false;

		// Calculate final return value
		if (hasReturnVal) {

			// Any trailing () denote an array
			while (m_token.m_text.equals("(")) {

				// Room for one more dimension?
				if (type.m_arrayLevel >= Constants.VM_MAXDIMENSIONS) {
					SetError((String) "Arrays cannot have more than "
							+ String.valueOf(Constants.VM_MAXDIMENSIONS)
							+ " dimensions.");
					return false;
				}

				// Add dimension
				type.m_arrayLevel++;

				if (!GetToken()) // Skip "("
					return false;

				// Expect ")"
				if (!m_token.m_text.equals(")")) {
					SetError("')' expected");
					return false;
				}
				if (!GetToken())
					return false;
			}

			// "as" keyword (QBasic/FreeBasic compatibility)
			if (m_token.m_text.equals("as")) {

				typeRef.set(type);
				nameRef.set(name);

				if (!CompileAs(nameRef, typeRef))
					return false;

				// Update local values from references
				name = nameRef.get();
				type = typeRef.get();
			}

			// Default basic type to int if undefined
			if (type.m_basicType == BasicValType.VTP_UNDEFINED.getType())
				type.m_basicType = BasicValType.VTP_INT.getType();

			// Store function return value type
			m_userFuncPrototype.hasReturnVal = true;
			m_userFuncPrototype.returnValType = type;
		} else
			m_userFuncPrototype.hasReturnVal = false;

		// Store function, and get its index (in m_currentFunction)
		Vector<vmUserFunc> functions = m_vm.UserFunctions();
		Vector<vmUserFuncPrototype> prototypes = m_vm.UserFunctionPrototypes();

		if (funcType == compUserFunctionType.UFT_FWDDECLARATION) {
			// Forward declaration.

			// Function name must not already have been used
			if (IsLocalUserFunction(name)) {
				SetError("'"
						+ name
						+ "' has already been used as a function/subroutine name");
				return false;
			}

			// Function name must not have been used for a runtime function
			if (IsRuntimeFunction(name)) {
				SetError((String) "'"
						+ name
						+ "' has already been used as a runtime function/subroutine name");
				return false;
			}

			// Allocate new function
			prototypes.add(m_userFuncPrototype);
			functions.add(new vmUserFunc(prototypes.size() - 1, false));
			m_currentFunction = functions.size() - 1;

			// Map name to function
			m_localUserFunctionIndex.put(name, m_currentFunction);
			m_visibleUserFunctionIndex.put(name, m_currentFunction);
			if (!IsGlobalUserFunction(name))
				m_globalUserFunctionIndex.put(name, m_currentFunction);

			// Build reverse index (for debugger)
			m_userFunctionReverseIndex.put(m_currentFunction, name);
		} else if (funcType == compUserFunctionType.UFT_RUNTIMEDECLARATION) {

			// Function name must not already have been used
			if (IsLocalUserFunction(name)) {
				SetError("'"
						+ name
						+ "' has already been used as a function/subroutine name");
				return false;
			}

			// Function name must not have been used for a runtime function
			if (IsRuntimeFunction(name)) {
				SetError("'"
						+ name
						+ "' has already been used as a runtime function/subroutine name");
				return false;
			}

			// Store prototype
			prototypes.add(m_userFuncPrototype);

			// Store runtime function
			m_runtimeFunctions.add(new compRuntimeFunction(
					prototypes.size() - 1));

			// Map name to runtime function
			m_runtimeFunctionIndex.put(name, m_runtimeFunctions.size() - 1);
		} else if (funcType == compUserFunctionType.UFT_IMPLEMENTATION) {

			// Function implementation

			// Create jump-past-function op-code
			m_functionJumpOver = m_vm.InstructionCount();
			AddInstruction(OpCode.OP_JUMP, BasicValType.VTP_INT.getType(), new VMValue(
					0)); // Jump target will be fixed up when "endfunction" is
			// compiled

			if (IsRuntimeFunction(name)) {

				// Implementation of runtime function
				int index = m_runtimeFunctionIndex.get(name);
				vmRuntimeFunction runtimeFunction = m_vm.CurrentCodeBlock()
						.GetRuntimeFunction(index);

				// Check if already implemented
				if (runtimeFunction.functionIndex >= 0) {
					SetError("Runtime function/sub '" + name
							+ "' has already been implemented");
					return false;
				}

				// Function must match runtime prototype
				if (!m_userFuncPrototype.Matches(prototypes
						.get(m_runtimeFunctions.get(index).prototypeIndex))) {
					SetError("Function/sub does not match its RUNTIME declaration");
					return false;
				}

				// Allocate new function
				prototypes.add(m_userFuncPrototype);
				functions.add(new vmUserFunc(prototypes.size() - 1, true, m_vm
						.InstructionCount()));
				m_currentFunction = functions.size() - 1;

				// Map runtime function to implementation
				runtimeFunction.functionIndex = m_currentFunction;
			} else {
				if (IsLocalUserFunction(name)) {

					// Function already DECLAREd.
					m_currentFunction = m_localUserFunctionIndex.get(name);

					// Must not be already implemented
					if (functions.get(m_currentFunction).implemented) {
						SetError("'"
								+ name
								+ "' has already been used as a function/subroutine name");
						return false;
					}

					// Function prototypes must match
					if (!m_userFuncPrototype.Matches(prototypes.get(functions
							.get(m_currentFunction).prototypeIndex))) {
						SetError((String) "Function/subroutine does not match how it was DECLAREd");
						return false;
					}

					// Save updated function spec
					// Function starts at next offset
					functions.get(m_currentFunction).implemented = true;
					functions.get(m_currentFunction).programOffset = m_vm
							.InstructionCount();
				} else {

					// Completely new function

					// Allocate a new prototype
					prototypes.add(m_userFuncPrototype);

					// Allocate a new function
					functions.add(new vmUserFunc(prototypes.size() - 1, true,
							m_vm.InstructionCount()));
					m_currentFunction = functions.size() - 1;
				}

				// Map name to function
				m_localUserFunctionIndex.put(name, m_currentFunction);
				m_visibleUserFunctionIndex.put(name, m_currentFunction);
				if (!IsGlobalUserFunction(name))
					m_globalUserFunctionIndex.put(name, m_currentFunction);
			}

			// Build reverse index (for debugger)
			m_userFunctionReverseIndex.put(m_currentFunction, name);

			// Compile the body of the function
			m_inFunction = true;
		}

		return true;
	}

	boolean CompileEndUserFunction(boolean hasReturnVal) {

		// Must be inside a function
		if (!m_inFunction) {
			if (hasReturnVal)
				SetError("'endfunction' without 'function'");
			else
				SetError("'endsub' without 'sub'");
			return false;
		}

		// Match end sub/function against sub/function type
		if (UserPrototype().hasReturnVal != hasReturnVal) {
			if (hasReturnVal)
				SetError("'endfunction' without 'function'");
			else
				SetError("'endsub' without 'sub'");
			return false;
		}

		// Check for unclosed flow controls
		if (!CheckUnclosedFlowControl())
			return false;

		// Skip 'endfunction'
		if (!GetToken())
			return false;

		// If end of function is reached without a return value, need to trigger
		// a runtime error.
		if (UserPrototype().hasReturnVal)
			AddInstruction(OpCode.OP_NO_VALUE_RETURNED, BasicValType.VTP_INT.getType(),
					new VMValue(0));
		else
			// Add return-from-user-function instruction
			AddInstruction(OpCode.OP_RETURN_USER_FUNC, BasicValType.VTP_INT.getType(),
					new VMValue(0));

		// Fix up jump-past-function op-code
		assert (m_functionJumpOver < m_vm.InstructionCount());
		m_vm.Instruction(m_functionJumpOver).mValue.setIntVal(m_vm
				.InstructionCount());

		// Let compiler know we have left the function
		m_inFunction = false;

		// TODO: If function is supposed to return a value, add an op-code that
		// triggers
		// a run-time error (meaning that the end of the function was reached
		// without
		// finding a "return" command).

		return true;
	}

	boolean CompileUserFunctionCall(boolean mustReturnValue,
			boolean isRuntimeFunc) {
		assert ((!isRuntimeFunc && m_token.m_type == TokenType.CTT_USER_FUNCTION) || (isRuntimeFunc && m_token.m_type == TokenType.CTT_RUNTIME_FUNCTION));
		assert ((!isRuntimeFunc && IsUserFunction(m_token.m_text)) || (isRuntimeFunc && IsRuntimeFunction(m_token.m_text)));

		// Read function name
		String name = m_token.m_text;
		if (!GetToken())
			return false;

		// Lookup prototype
		int index;
		int prototypeIndex;
		if (isRuntimeFunc) {
			index = m_runtimeFunctionIndex.get(name);
			prototypeIndex = m_runtimeFunctions.get(index).prototypeIndex;
		} else {
			index = m_visibleUserFunctionIndex.get(name);
			prototypeIndex = m_vm.UserFunctions().get(index).prototypeIndex;
		}
		vmUserFuncPrototype prototype = m_vm.UserFunctionPrototypes().get(
				prototypeIndex);

		if (mustReturnValue && !prototype.hasReturnVal) {
			SetError((String) "'" + name + "' does not return a value");
			return false;
		}

		// Add op-code to prepare function stack frame.
		// Stack frame remains inactive while evaluating its parameters.
		if (isRuntimeFunc)
			AddInstruction(OpCode.OP_CREATE_RUNTIME_FRAME,
					BasicValType.VTP_INT.getType(), new VMValue(index));
		else
			AddInstruction(OpCode.OP_CREATE_USER_FRAME, BasicValType.VTP_INT.getType(),
					new VMValue(index));

		// Expect "("
		if (!m_token.m_text.equals("(")) {
			SetError("Expected '('");
			return false;
		}
		if (!GetToken())
			return false;

		// Evaluate function parameters
		boolean needComma = false;
		for (int i = 0; i < prototype.paramCount; i++) {
			if (needComma) {
				if (!m_token.m_text.equals(",")) {
					SetError("Expected ','");
					return false;
				}
				if (!GetToken())
					return false;
			}
			needComma = true;

			Mutable<vmUserFuncPrototype> funcRef = new Mutable<vmUserFuncPrototype>(
					prototype);
			if (!CompileUserFuncParam(funcRef, i))
				return false;
			// Update local value from reference
			prototype = funcRef.get();
		}

		// Expect ")"
		if (!m_token.m_text.equals(")")) {
			SetError("Expected ')'");
			return false;
		}
		if (!GetToken())
			return false;

		// Add op-code to call function.
		// Type: Unused.
		// Value: index of function specification.
		AddInstruction(OpCode.OP_CALL_USER_FUNC, BasicValType.VTP_INT.getType(),
				new VMValue());

		if (prototype.hasReturnVal) {

			// Data containing strings will need to be "destroyed" when the
			// stack unwinds.
			if (!prototype.returnValType.CanStoreInRegister()
					&& m_vm.DataTypes().ContainsString(prototype.returnValType))
				AddInstruction(
						OpCode.OP_REG_DESTRUCTOR,
						BasicValType.VTP_INT.getType(),
						new VMValue((int) m_vm
								.StoreType(prototype.returnValType)));

			// Set register type to value returned from function (if applies)
			m_regType.Set(prototype.returnValType.RegisterType());
			if (!CompileDataLookup(false))
				return false;

			// If function returns a value larger than the register, temp data
			// will
			// need to be freed.
			if (!prototype.returnValType.CanStoreInRegister()) {
				m_freeTempData = true;
			}
		}

		return true;
	}

	boolean CompileUserFuncParam(Mutable<vmUserFuncPrototype> prototype, int i) {

		// Generate code to store result as a function parameter
		ValType type = prototype.get().localVarTypes.get(i);

		// Basic type case
		if (type.IsBasic()) {

			// Generate code to compile function parameter
			if (!CompileExpression(false))
				return false;

			// Attempt to convert value in reg to same type
			if (!CompileConvert(type.m_basicType)) {
				SetError("Types do not match");
				return false;
			}

			// Save reg into parameter
			AddInstruction(OpCode.OP_SAVE_PARAM, type.m_basicType,
					new VMValue(i));
		}

		// Pointer case. Parameter must be a pointer and m_reg must point to a
		// value accessible through a variable.
		else if (type.VirtualPointerLevel() == 1) {

			// Special case: We accept "null" to pointer parameters
			if (m_token.m_text.equals("null")) {
				if (!CompileNull())
					return false;
				AddInstruction(OpCode.OP_SAVE_PARAM, BasicValType.VTP_INT.getType(),
						new VMValue(i));
			} else {

				// Otherwise we implicitly take the address of any variable
				// passed in
				if (!CompileLoadVar())
					return false;

				if (!CompileTakeAddress())
					return false;

				// Register should now match the expected type
				if (m_regType.m_pointerLevel == type.m_pointerLevel
						&& m_regType.m_arrayLevel == type.m_arrayLevel
						&& m_regType.m_basicType == type.m_basicType)
					AddInstruction(OpCode.OP_SAVE_PARAM,
							BasicValType.VTP_INT.getType(), new VMValue(i));

				else {
					SetError("Types do not match");
					return false;
				}
			}
		}

		// Not basic, and not a pointer.
		// Must be a large object (structure or array)
		else {
			assert (type.m_pointerLevel == 0);

			// Generate code to compile function parameter
			if (!CompileExpression(false))
				return false;

			if (m_regType.m_pointerLevel == 1 && m_regType.m_byRef
					&& m_regType.m_arrayLevel == type.m_arrayLevel
					&& m_regType.m_basicType == type.m_basicType) {
				AddInstruction(OpCode.OP_COPY_USER_STACK,
						BasicValType.VTP_INT.getType(),
						new VMValue((int) m_vm.StoreType(type)));
				AddInstruction(OpCode.OP_SAVE_PARAM_PTR,
						BasicValType.VTP_INT.getType(), new VMValue(i));
			} else {
				SetError("Types do not match");
				return false;
			}
		}

		// Data containing strings will need to be "destroyed" when the stack
		// unwinds.
		if (m_vm.DataTypes().ContainsString(type))
			AddInstruction(OpCode.OP_REG_DESTRUCTOR, BasicValType.VTP_INT.getType(),
					new VMValue((int) m_vm.StoreType(type)));

		return true;
	}

	boolean CompileReturn() {
		if (!GetToken())
			return false;

		if (m_inFunction) {
			if (UserPrototype().hasReturnVal) {
				ValType type = new ValType(UserPrototype().returnValType);

				// Generate code to compile and return value
				if (!CompileExpression())
					return false;
				if (!CompileConvert(type.RegisterType()))
					return false;

				// Basic values and pointers can be returned in the register
				if (!type.CanStoreInRegister()) {

					// Add instruction to move that data into temp data
					AddInstruction(OpCode.OP_MOVE_TEMP, BasicValType.VTP_INT.getType(),
							new VMValue((int) m_vm.StoreType(type)));

					// Add return-from-function OP-code
					// Note: The 0 in the instruction value indicates that temp
					// data should NOT be freed on return (as we have just moved
					// the return value there.)
					AddInstruction(OpCode.OP_RETURN_USER_FUNC,
							BasicValType.VTP_INT.getType(), new VMValue(0));
				} else
					// Add return-from-function OP-code
					// Note: The 1 in the instruction value indicates that temp
					// data should be freed on return.
					AddInstruction(OpCode.OP_RETURN_USER_FUNC,
							BasicValType.VTP_INT.getType(), new VMValue(1));
			} else
				AddInstruction(OpCode.OP_RETURN_USER_FUNC,
						BasicValType.VTP_INT.getType(), new VMValue(1));
		} else {
			// Add "return from Gosub" op-code
			AddInstruction(OpCode.OP_RETURN, BasicValType.VTP_INT.getType(),
					new VMValue());
		}

		return true;
	}

	public String GetUserFunctionName(int index) {
		String name = m_userFunctionReverseIndex.get(index);
		return name == null ? "???" : name;
	}

	// State streaming
	public void StreamOut(ByteBuffer buffer) {
		try {
			// Stream out VM state
			m_vm.StreamOut(buffer);

			// Stream out constants
			for (String key : m_programConstants.keySet()) {
				Streaming.WriteString(buffer, key);
				m_programConstants.get(key).StreamOut(buffer);
			}
			Streaming.WriteString(buffer, "");

			// Stream out labels
			for (String key : m_labels.keySet()) {
				Streaming.WriteString(buffer, key);
				m_labels.get(key).StreamOut(buffer);
			}
			Streaming.WriteString(buffer, "");

			// Stream out user function/subroutine names
			for (String key : m_globalUserFunctionIndex.keySet()) {
				Streaming.WriteString(buffer, key);
				Streaming.WriteLong(buffer, m_globalUserFunctionIndex.get(key));
			}
			Streaming.WriteString(buffer, "");

			// Stream out runtime functions
			// Note that strictly speaking these aren't "names", but because
			// they are
			// required when name information is present, and not required when
			// it is
			// absent, we are bundling them into the same #ifdef
			Streaming.WriteLong(buffer, m_runtimeFunctions.size());
			for (int i = 0; i < m_runtimeFunctions.size(); i++)
				m_runtimeFunctions.get(i).StreamOut(buffer);

			// Stream out runtime function names
			for (String key : m_runtimeFunctionIndex.keySet()) {
				Streaming.WriteString(buffer, key);
				Streaming.WriteLong(buffer, m_runtimeFunctionIndex.get(key));
			}
			Streaming.WriteString(buffer, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean StreamIn(ByteBuffer buffer) {
		try {

			// TODO Reimplement Libraries
			// Unload any plugins
			// m_plugins.Clear();

			// Clear current program (if any)
			New();

			// Stream in VM state
			if (!m_vm.StreamIn(buffer)) {
				SetError(m_vm.GetError());
				return false;
			}

			// Stream in constant names
			String name = Streaming.ReadString(buffer);
			while (!name.equals("")) {

				// Read constant details
				compConstant constant = new compConstant();
				constant.StreamIn(buffer);

				// Store constant
				m_programConstants.put(name, constant);

				// Next constant
				name = Streaming.ReadString(buffer);
			}

			// Stream in label names
			name = Streaming.ReadString(buffer);

			while (!name.equals("")) {

				// Read label details
				compLabel label = new compLabel();
				label.StreamIn(buffer);

				// Store label
				m_labels.put(name, label);
				m_labelIndex.put(label.m_offset, name);

				// Next label
				name = Streaming.ReadString(buffer);

			}

			// Stream in user function/subroutine names
			name = Streaming.ReadString(buffer);
			while (!name.equals("")) {

				// Read function details
				int index = (int) Streaming.ReadLong(buffer);

				// Store function index
				m_globalUserFunctionIndex.put(name, index);

				// Next function
				name = Streaming.ReadString(buffer);
			}
			// Stream in runtime functions
			// Note that strictly speaking these aren't "names", but because
			// they are
			// required when name information is present, and not required when
			// it is
			// absent, we are bundling them into the same #ifdef
			int count = (int) Streaming.ReadLong(buffer);
			m_runtimeFunctions.setSize(count);
			for (int i = 0; i < count; i++)
				m_runtimeFunctions.get(i).StreamIn(buffer);

			name = Streaming.ReadString(buffer);
			while (!name.equals("")) {

				// Read runtime function details
				int index = (int) Streaming.ReadLong(buffer);

				// Store runtime function index
				m_runtimeFunctionIndex.put(name, index);

				// Next function
				name = Streaming.ReadString(buffer);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}