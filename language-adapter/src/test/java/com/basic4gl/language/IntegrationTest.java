package com.basic4gl.language;

import static org.junit.jupiter.api.Assertions.*;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.ISourceFile;
import com.basic4gl.language.core.runtime.Data;
import com.basic4gl.language.core.runtime.IVMDebugger;
import com.basic4gl.language.core.runtime.Instruction;
import com.basic4gl.language.core.runtime.Value;
import com.basic4gl.language.core.types.BasicValType;
import com.basic4gl.language.core.types.Constant;
import com.basic4gl.language.core.types.OpCode;
import com.basic4gl.language.core.types.VariableCollection;
import com.basic4gl.language.spi.PluginManager;
import com.basic4gl.runtime.TomVM;
import java.util.Arrays;
import org.junit.jupiter.api.*;

// Eventually we want to test libraries. Soon.
// import com.basic4gl.library.standard.Standard;

public class IntegrationTest {

    // Set to `true` within a test to dump the disassembly for the test.
    private boolean printDisassembly = false;

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
        this.printDisassembly = false;
        this.compiler.clearProgram();
        this.compiler.clearError();
        this.vm.clearProgram();
        this.vm.clearError();
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

        // The empty program should execute (and do nothing)
        assertCodeExecutes();

        // All things past the single quote are commented out.
        assertCodeCompiles("' this is a comment");

        // Comments are not carried over as instructions.
        assertBytecodeOutput(new Instruction[] {
            new Instruction(OpCode.OP_END, BasicValType.VTP_INT, new Value(0)),
        });
        assertCodeExecutes();
    }

    /*
     * Compiling DIM and CONST directives.
     */

    // Regular DIM invocations create a global variable.
    @Test
    void compilesDim() {

        String program = "dim var$\r\n";

        assertCodeCompiles(program);
        assertBytecodeOutput(new Instruction[] {
            // Global variables are declared at the beginning of the instruction stream.
            // TODO: why do we compile a declaration of a variable, when we already have it
            // done before we start running code?
            new Instruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(0)),
            new Instruction(OpCode.OP_END, BasicValType.VTP_INT, new Value(0)),
        });

        assertCodeExecutes();
        assertTrue(this.vm.getVariables().containsVariable("var$"), "Variable should be created in the VM.");
        // What does a string variable default to?
        // assertGlobalVariableEquals("var$", 0);
    }

    // DIM invocations can be immediately assigned.
    @Test
    void compilesDimWithInitialValue() {
        String program = "dim var$ = \"hello, world\"\r\n";
        assertCodeCompiles(program);
        assertBytecodeOutput(new Instruction[] {
            // Allocate new variable, named 0
            new Instruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(0)),
            // Load that variable into the first register
            new Instruction(OpCode.OP_LOAD_VAR),
            // Push that value onto the stack
            new Instruction(OpCode.OP_PUSH),
            // Load a predefined constant value (String index 0) into the first register.
            new Instruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING, new Value(0)),
            // Pop a value from the stack and place it into the second register.
            new Instruction(OpCode.OP_POP),
            // Save the value in the first register to the memory location in the second
            // register.
            new Instruction(OpCode.OP_SAVE, BasicValType.VTP_STRING, new Value(0)),
            // Finish the program.
            new Instruction(OpCode.OP_END),
        });
        assertCodeExecutes();
        assertTrue(this.vm.getVariables().containsVariable("var$"), "Variable should be created in the VM.");
        assertGlobalVariableEquals("var$", "hello, world");
    }

    // DIM can declare multiple variables at once.
    //
    // This also tests some basic value types.
    @Test
    void compilesDimWithMultipleVariables() {
        String program = "DIM a, b$, c#";
        assertCodeCompiles(program);
        assertTrue(this.vm.getVariables().containsVariable("a"), "Variable should be created in the VM.");
        assertTrue(this.vm.getVariables().containsVariable("b$"), "Variable should be created in the VM.");
        assertTrue(this.vm.getVariables().containsVariable("c#"), "Variable should be created in the VM.");
        assertBytecodeOutput(new Instruction[] {
            // Allocate the 3 variables
            new Instruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(0)),
            new Instruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(1)),
            new Instruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(2)),
            // Finish the program.
            new Instruction(OpCode.OP_END),
        });
        assertCodeExecutes();
    }

    // DIM can also set variables, all at once.
    @Test
    void compilesDimWithMultipleVariablesAndInitialValues() {
        String program = "DIM a = 1, b$ = \"strang\", c# = 3.14159";
        assertCodeCompiles(program);
        assertTrue(this.vm.getVariables().containsVariable("a"), "Variable should be created in the VM.");
        assertTrue(this.vm.getVariables().containsVariable("b$"), "Variable should be created in the VM.");
        assertTrue(this.vm.getVariables().containsVariable("c#"), "Variable should be created in the VM.");

        assertBytecodeOutput(new Instruction[] {
            // Allocate and set each variable.
            new Instruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(0)),
            // Load that variable into the first register
            new Instruction(OpCode.OP_LOAD_VAR),
            // Push that value onto the stack
            new Instruction(OpCode.OP_PUSH),
            // Load a predefined constant value into the first register.
            new Instruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_INT, new Value(1)),
            // Pop a value from the stack and place it into the second register.
            new Instruction(OpCode.OP_POP),
            // Save the value in the first register to the memory location in the second
            // register.
            new Instruction(OpCode.OP_SAVE, BasicValType.VTP_INT),
            new Instruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(1)),
            // Load that variable into the first register
            new Instruction(OpCode.OP_LOAD_VAR, BasicValType.VTP_INT, new Value(1)),
            // Push that value onto the stack
            new Instruction(OpCode.OP_PUSH),
            // Load a predefined constant value (String index 0) into the first register.
            new Instruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_STRING, new Value(0)),
            // Pop a value from the stack and place it into the second register.
            new Instruction(OpCode.OP_POP),
            // Save the value in the first register to the memory location in the second
            // register.
            new Instruction(OpCode.OP_SAVE, BasicValType.VTP_STRING),
            new Instruction(OpCode.OP_DECLARE, BasicValType.VTP_INT, new Value(2)),
            // Load that variable into the first register
            new Instruction(OpCode.OP_LOAD_VAR, BasicValType.VTP_INT, new Value(2)),
            // Push that value onto the stack
            new Instruction(OpCode.OP_PUSH),
            // Load a predefined constant value (String index 0) into the first register.
            new Instruction(OpCode.OP_LOAD_CONST, BasicValType.VTP_REAL, new Value((float) 3.14159)),
            // Pop a value from the stack and place it into the second register.
            new Instruction(OpCode.OP_POP),
            // Save the value in the first register to the memory location in the second
            // register.
            new Instruction(OpCode.OP_SAVE, BasicValType.VTP_REAL),

            // Finish the program.
            new Instruction(OpCode.OP_END),
        });

        assertCodeExecutes();
    }

    @Test
    void compilesConst() {
        printDisassembly = true;
        // Constants must be set to a value.
        String program = "const theAnswer = 42\r\n";
        assertCodeCompiles(program);
        assertCodeExecutes();
        assertConstantDefined("theAnswer");
        assertConstantEquals("theAnswer", 42);
    }

    /*
     * Compiling assignment.
     */

    @Test
    void compilesAssignment() {
        String program = "dim var$\n" + "var$ = \"Hello, world\"";
        assertCodeCompiles(program);
        assertCodeExecutes();
        assertGlobalVariableEquals("var$", "Hello, world");
    }

    @Test
    void executesGotoToLinkedLabel() {
        assertCodeCompiles(
                """
                dim result
                goto target
                result = 1
                target:
                result = 2
                """);

        assertCodeExecutes();
        assertGlobalVariableEquals("result", 2);
    }

    @Test
    void executesGosubToLinkedLabel() {
        assertCodeCompiles(
                """
                dim x
                x = 1
                gosub test
                end
                test:
                    x = 2
                    return
                """);

        assertCodeExecutes();
        assertGlobalVariableEquals("x", 2);
    }

    @Test
    void rejectsUnimplementedGlobalForwardDeclaredSub() {
        String program =
                """
                declare sub test()

                dim result
                result = 1
                """;

        assertCodeDoesNotCompile(program);
    }

    @Test
    void rejectsForwardDeclaredSubWithMismatchedImplementation() {
        String program =
                """
                declare sub test(x)

                sub test(x#)
                end sub
                """;

        assertCodeDoesNotCompile(program);
    }

    @Test
    void rejectsUnimplementedForwardDeclaredGlobalSub() {
        String program =
                """
                declare sub test()

                dim value
                value = 1
                """;

        assertCodeDoesNotCompile(program);
    }

    @Test
    void executesForwardDeclaredGlobalSub() {
        String program =
                """
                declare sub test()

                dim result
                result = 1

                test()

                sub test()
                    result = 2
                end sub
                """;

        assertCodeCompiles(program);

        // The forward declaration and implementation must resolve to the
        // same UserFunc entry.
        Integer functionIndex = compiler.getGlobalUserFunctionIndex().get("test");
        assertNotNull(functionIndex, "Forward-declared sub should remain globally indexed");

        assertEquals(
                1,
                vm.getUserFunctions().size(),
                "Forward declaration and implementation should not create separate UserFunc entries");

        var function = vm.getUserFunctions().get(functionIndex);

        assertTrue(function.implemented, "Forward-declared sub should be marked implemented");
        assertTrue(function.programOffset >= 0, "Forward-declared sub should have a valid implementation offset");

        // The call site must create a frame for that same UserFunc.
        Instruction createFrame = Arrays.stream(vm.getInstructions())
                .filter(i -> i.opCode == OpCode.OP_CREATE_USER_FRAME)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected OP_CREATE_USER_FRAME"));

        assertEquals(
                functionIndex.intValue(),
                createFrame.value.getIntVal(),
                "Call site should reference the implemented UserFunc");

        assertCodeExecutes();
        assertGlobalVariableEquals("result", 2);
    }

    @Test
    void nestedUserFunctionCallsPreserveCallerLocals() {
        assertCodeCompiles(
                """
                function inner(x)
                    return x + 1
                endfunction

                function outer(x)
                    dim before
                    before = x
                    return before + inner(10) + before
                endfunction

                dim result
                result = outer(5)
                """);

        assertCodeExecutes();

        assertGlobalVariableEquals("result", 21);
    }

    @Test
    void recursiveUserFunctionCallsPreserveFrameState() {
        assertCodeCompiles(
                """
                function countdown(n)
                    if n <= 0 then
                        return 0
                    endif

                    return n + countdown(n - 1)
                endfunction

                dim result
                result = countdown(5)
                """);

        assertCodeExecutes();

        assertGlobalVariableEquals("result", 15);
    }

    @Test
    void reusedUserFunctionFrameDoesNotRetainLocalValues() {
        assertCodeCompiles(
                """
                function choose(x)
                    dim local

                    if x = 1 then
                        local = 7
                    endif

                    return local
                endfunction

                dim first, second
                first = choose(1)
                second = choose(0)
                """);

        assertCodeExecutes();

        assertGlobalVariableEquals("first", 7);
        assertGlobalVariableEquals("second", 0);
    }

    @Test
    void repeatedUserFunctionCallsDoNotCorruptReturnValues() {
        assertCodeCompiles(
                """
                function addOne(x)
                    return x + 1
                endfunction

                dim first, second, third

                first = addOne(10)
                second = addOne(20)
                third = addOne(30)
                """);

        assertCodeExecutes();

        assertGlobalVariableEquals("first", 11);
        assertGlobalVariableEquals("second", 21);
        assertGlobalVariableEquals("third", 31);
    }

    // Helper assertions

    /** Asserts that the BASIC code given should correctly compile. */
    void assertCodeCompiles(String source) {
        assertCodeCompiles(source, "");
    }

    /** Asserts that the BASIC code given should correctly compile. */
    void assertCodeCompiles(String source, String message) {
        ISourceFile sf = new StringSourceFile(source);
        assertTrue(this.compiler.load(sf), message);
        this.compiler.compile();
        if (printDisassembly) {
            displayState(source);
        }
        assertFalse(this.compiler.hasError(), message + this.compiler.getError());
    }

    /** Asserts that the BASIC code given is erroneous and should not compile. */
    void assertCodeDoesNotCompile(String source) {
        ISourceFile sf = new StringSourceFile(source);
        assertTrue(this.compiler.load(sf), "Compiler did not catch the error");
        assertFalse(compiler.compile(), "Compiler did not catch the error");
        if (printDisassembly) {
            displayState(source);
        }
        assertTrue(this.compiler.hasError(), "Compiler did not catch the error");
    }

    void assertCodeExecutes() {
        this.vm.continueVM();
        assertFalse(this.vm.hasError(), this.vm.getError());
    }

    void assertGlobalVariableEquals(String globalName, int expected) {
        Value val = findGlobalVariable(globalName);
        assertNotNull(val);
        assertEquals(expected, val.getIntVal());
    }

    void assertGlobalVariableEquals(String globalName, String expected) {
        // This is a lot.
        Value val = findGlobalVariable(globalName);
        String vstr = this.vm.getString(val.getIntVal());
        assertNotNull(vstr);
        assertTrue(expected.equals(vstr), expected + "" + vstr);
    }

    Value findGlobalVariable(String globalName) {
        VariableCollection vars = this.vm.getVariables();

        assertTrue(
                vars.containsVariable(globalName),
                "Expected `" + globalName + "` to be a global variable, but it is not");

        // This is a lot.
        VariableCollection.Variable v = vars.getVariable(globalName);
        assertNotNull(v);

        Data d = this.vm.getData();
        assertNotNull(d);

        assertTrue(v.dataIndex <= d.data().size());

        return d.data().get(v.dataIndex);
    }

    void assertConstantDefined(String name) {
        assertNotNull(this.compiler.getUserDefinedConstant(name));
    }

    void assertConstantEquals(String name, int expected) {
        Constant c = compiler.getUserDefinedConstant(name);
        if (c == null) fail("Constant does not exist");

        assertEquals(expected, c.getIntValue());
    }

    /**
     * Asserts that the Virtual Machine's toplevel code should contain exactly the
     * expected instructions, in order.
     */
    void assertBytecodeOutput(Instruction[] expected) {
        Instruction[] actual = this.vm.getInstructions();
        int i = 0;
        for (; i < expected.length; i++) {
            assertEquals(
                    expected[i].opCode,
                    actual[i].opCode,
                    "Opcodes differ at index " + i + ", expected " + expected[i].opCode + " but got "
                            + actual[i].opCode);
            assertEquals(
                    expected[i].basicVarType,
                    actual[i].basicVarType,
                    "BasicVarType differ at index " + i + ", expected " + expected[i].basicVarType + " but got "
                            + actual[i].basicVarType);
            assertTrue(
                    expected[i].value.equals(actual[i].value),
                    "Values differ at index " + i + ", expected " + expected[i].value + " but got " + actual[i].value);
            // TODO: check source line and source char
        }
        assertTrue(i == actual.length);
    }

    // Helpers for displaying useful information while working on the tests.
    void displayState(String source) {
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
}
