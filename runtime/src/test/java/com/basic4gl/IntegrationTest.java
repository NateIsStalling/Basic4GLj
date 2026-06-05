package com.basic4gl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.plugin.PluginManager;
import com.basic4gl.runtime.plugin.NullPluginManager;
import com.basic4gl.runtime.util.IVMDebugger;
import com.basic4gl.runtime.util.NullDebugger;

public class IntegrationTest {
	private TomBasicCompiler compiler;

	@BeforeEach
	void setUp() {
		IVMDebugger dbg = new NullDebugger();
		PluginManager pm = new NullPluginManager();
		// Initialize core runtime services needed by the compiler.
		TomVM vm = new TomVM(pm, dbg);
		// Use the required constructor parameters for integration testing.
		this.compiler = new TomBasicCompiler(vm, pm);
	}

	@Test
	void canTestCompilerIntegration() {
		// Assert that the required compiler object has been successfully initialized.
		assertNotNull(compiler, "TomBasicCompiler object should be initialized for testing.");
	}
}
