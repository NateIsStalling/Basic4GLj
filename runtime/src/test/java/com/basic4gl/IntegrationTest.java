package com.basic4gl;

import java.util.Arrays;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.NullSourceFile;
import com.basic4gl.runtime.Instruction;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.plugin.PluginManager;
import com.basic4gl.runtime.plugin.NullPluginManager;
import com.basic4gl.runtime.util.IVMDebugger;
import com.basic4gl.runtime.util.NullDebugger;

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

	public IntegrationTest(boolean printDisassembly) {
		this();
		this.printDisassembly = printDisassembly;
	}

	@AfterEach
	void tearDown() {
		this.compiler.clearProgram();
	}

	@Test
	void canCreateCompiler() {
		// Assert that the required compiler object has been successfully initialized.
		assertNotNull(compiler, "TomBasicCompiler object should be initialized for testing.");
	}

	@Test
	void compilesEmptyProgram() {
		assertCodeCompiles("");
		assertTrue(this.vm.getInstructions().length == 1);
	}

	@Test
	void compilesDimStatement() {
		assertCodeCompiles("DIM var as string\n");
	}

	// Helper assertions

	void assertCodeCompiles(String source) {
		NullSourceFile sf = new NullSourceFile("");
		assertTrue(this.compiler.load(sf));
		assertTrue(this.compiler.compile());
		if (printDisassembly) {
			System.out.println("Main program disassembly: ");
			System.out.println(Instruction.disassemble(Arrays.asList(this.vm.getInstructions())));
		}
	}
}
