package com.basic4gl;

import java.util.Arrays;

import org.junit.jupiter.api.*;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.StringSourceFile;
import com.basic4gl.compiler.util.ISourceFile;

import com.basic4gl.runtime.Data;
import com.basic4gl.runtime.Instruction;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.Value;
import com.basic4gl.runtime.plugin.NullPluginManager;
import com.basic4gl.runtime.plugin.PluginManager;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.OpCode;
import com.basic4gl.runtime.util.IVMDebugger;
import com.basic4gl.runtime.util.NullDebugger;
import com.basic4gl.runtime.VariableCollection.Variable;

// Eventually we want to test libraries. Soon.
// import com.basic4gl.library.standard.Standard;

public class IntegrationTest {

	// Set to `true` to dump the disassembly for all programs in these tests to
	// STDOUT.
	private boolean printDisassembly = true;

	private TomBasicCompiler compiler;
	private IVMDebugger debugger;
	private PluginManager pm;
	private TomVM vm;

	public IntegrationTest() {
		debugger = new NullDebugger();
		pm = new NullPluginManager();
		vm = new TomVM(pm, debugger);
		this.compiler = new TomBasicCompiler(vm, pm);
	}

	@AfterEach
	void tearDown() {
		this.compiler.clearProgram();
	}

	// this was vibecoded lol
	@Test
	void canCreateCompiler() {
		// Assert that the required compiler object has been successfully initialized.
		assertNotNull(compiler, "TomBasicCompiler object should be initialized for testing.");
	}

	/**
	 * Compile the trivial program and the trivial program, with comments.
	 *
	 * Tests weird edge cases that should obviously work.
	 */
	@Test
	void compilesEmptyPrograms() {
		// All strings are programs.
		assertCodeCompiles("");

		// All programs compile to at least an END instruction.
		assertTrue(this.vm.getInstructions().length == 1);
		Instruction end = vm.getInstructions()[0];
		assertTrue(end.opCode == OpCode.OP_END);

		// All things past the single quote are commented out.
		assertCodeCompiles("' this is a comment");

		// Comments are not carried over as instructions.
		assertBytecodeOutput(new Instruction[] {
				new Instruction(OpCode.OP_END, BasicValType.VTP_INT, new Value(0)),
		});
	}

	/*
	 * Compiling DIM and CONST directives.
	 */

	@Test
	void compilesDim() {
		// Regular DIM invocations create a global variable.
		String program = "dim var$\r\n";
		assertCodeCompiles(program);
		assertTrue(this.vm.getVariables().containsVariable("var$"),
				"Variable should be created in the VM.");

		// DIM invocations can be immediately assigned.
		program = "dim var$ = \"hello, world\"\r\n";
		assertCodeCompiles(program);
		assertTrue(this.vm.getVariables().containsVariable("var$"),
				"Variable should be created in the VM.");
		// This is a lot.
		Variable v = this.vm.getVariables().getVariable("var$");
		Data d = this.vm.getData();
		Value val = d.data().get(v.dataIndex);
		String vstr = this.vm.getStringConstants().get(val.getIntVal());
		assertEquals("hello, world", vstr);
	}

	@Test
	void compilesConst() {
		// Constants must be set to a value.
		String program = "const var$ = \"hello world\"\r\n";
		assertCodeCompiles(program);
	}

	/*
	 * Compiling assignment.
	 */

	@Test
	void compilesAssignment() {
		String program = "dim var$\n" + "var$ = \"Hello, world\"";
		assertCodeCompiles(program);
	}

	// Helper assertions

	void assertCodeCompiles(String source) {
		assertCodeCompiles(source, "");
	}

	void assertCodeCompiles(String source, String message) {
		ISourceFile sf = new StringSourceFile(source);
		assertTrue(this.compiler.load(sf), message);
		this.compiler.compile();
		if (printDisassembly) {
			System.out.println("---------------------------");
			System.out.println("Input program: ");
			System.out.println("```");
			System.out.print(source);
			System.out.println("```");

			System.out.println("Output: \n");
			System.out.println("Variables: ");
			System.out.println(vm.getVariables());
			System.out.println("String constants: ");
			System.out.println(vm.getStringConstants());

			System.out.println("Disassembly: ");
			System.out.println(Instruction.disassemble(Arrays.asList(this.vm.getInstructions())));
		}

		assertFalse(this.compiler.hasError(), message + this.compiler.getError());
	}

	void assertCodeDoesNotCompile(String source) {
		ISourceFile sf = new StringSourceFile(source);
		assertFalse(this.compiler.load(sf), "Compiler did not catch the error");
		this.compiler.compile();
		assertTrue(this.compiler.hasError(), "Compiler did not catch the error");
	}

	void assertBytecodeOutput(Instruction[] expected) {
		Instruction[] actual = this.vm.getInstructions();
		int i = 0;
		for (; i < expected.length; i++) {
			assertEquals(expected[i].opCode, actual[i].opCode, "Opcodes differ at index " + i + ", expected "
					+ expected[i].opCode + " but got " + actual[i].opCode);
			assertTrue(expected[i].value.equals(actual[i].value), "Values differ at index " + i + ", expected "
					+ expected[i].value + " but got " + actual[i].value);
			// TODO: check source line and source char
		}
		assertTrue(i == actual.length);
	}
}
