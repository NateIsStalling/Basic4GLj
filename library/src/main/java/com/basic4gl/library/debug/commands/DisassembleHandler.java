package com.basic4gl.library.debug.commands;

import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.debug.protocol.callbacks.DisassembleCallback;
import com.basic4gl.debug.protocol.commands.DisassembleCommand;
import com.basic4gl.debug.protocol.types.DisassembledInstruction;
import com.basic4gl.debug.protocol.types.DisassembleArguments;
import com.basic4gl.debug.protocol.types.Source;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.Instruction;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.OpCode;
import com.basic4gl.runtime.util.ILineNumberMapping;
import com.basic4gl.runtime.util.Mutable;
import com.google.gson.Gson;

import javax.websocket.Session;
import java.util.stream.IntStream;

import static com.basic4gl.runtime.types.OpCode.*;

public class DisassembleHandler {
    private final Debugger debugger;
    private final TomBasicCompiler compiler;
    private final TomVM vm;
    private final Gson gson;

    private final ILineNumberMapping lineNumberMapping;

    public DisassembleHandler(Debugger debugger, TomBasicCompiler compiler, TomVM vm, Gson gson) {
        this.debugger = debugger;
        this.compiler = compiler;
        this.vm = vm;
        this.lineNumberMapping = debugger.getLineNumberMapping();
        this.gson = gson;
    }

    public void handle(DisassembleCommand command, int requestId, Session session) {
        Instruction[] vmInstructions = this.vm.getInstructions();
        DisassembleArguments arguments = command != null ? command.arguments : null;
        int start = getStartInstruction(arguments);
        int instructionCount = getInstructionCount(arguments);
        boolean resolveSymbols = arguments == null || arguments.resolveSymbols == null || arguments.resolveSymbols;

        DisassembledInstruction[] instructions = IntStream.range(0, instructionCount)
                .mapToObj(offset -> {
                    int ip = start + offset;
                    if (ip < 0 || ip >= vmInstructions.length) {
                        return buildInvalidInstruction(ip);
                    }
                    return buildInstruction(vmInstructions[ip], ip, resolveSymbols);
                })
                .toArray(DisassembledInstruction[]::new);

        DisassembleCallback callback = new DisassembleCallback();
        callback.setRequestId(requestId);
        callback.setInstructions(instructions);

        String json = gson.toJson(callback);
        message(session, json);
    }

    private int getStartInstruction(DisassembleArguments arguments) {
        int base = parseMemoryReference(arguments != null ? arguments.memoryReference : null);
        int byteOffset = arguments != null && arguments.offset != null ? arguments.offset : 0;
        int instructionOffset = arguments != null && arguments.instructionOffset != null ? arguments.instructionOffset : 0;
        return base + byteOffset + instructionOffset;
    }

    private int getInstructionCount(DisassembleArguments arguments) {
        int requested = (arguments != null && arguments.instructionCount > 0) ? arguments.instructionCount : 200;
        return Math.min(requested, 1000);
    }

    private int parseMemoryReference(String memoryReference) {
        if (memoryReference == null || memoryReference.trim().isEmpty()) {
            return 0;
        }
        String trimmed = memoryReference.trim();
        try {
            if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
                return Integer.parseUnsignedInt(trimmed.substring(2), 16);
            }
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private DisassembledInstruction buildInstruction(Instruction data, int ip, boolean resolveSymbols) {
        DisassembledInstruction result = new DisassembledInstruction();
        result.instruction = OpCode.vmOpCodeName(data.opCode);
        result.symbol = resolveSymbols ? getOpCodeData(data) : "";
        result.instructionBytes = Integer.toHexString(data.opCode);
        result.address = String.valueOf(ip);
        result.line = data.sourceLine;
        result.column = data.sourceChar;
        result.presentationHint = "normal";

        if (lineNumberMapping != null) {
            Mutable<String> filename = new Mutable<>("");
            Mutable<Integer> fileRow = new Mutable<>(0);
            lineNumberMapping.getSourceFromMain(filename, fileRow, data.sourceLine);
            result.location = new Source();
            result.location.name = filename.get();
            result.location.path = filename.get();
            result.line = fileRow.get();
        }
        return result;
    }

    private DisassembledInstruction buildInvalidInstruction(int ip) {
        DisassembledInstruction result = new DisassembledInstruction();
        result.address = String.valueOf(ip);
        result.instruction = "<invalid>";
        result.symbol = "";
        result.instructionBytes = "";
        result.presentationHint = "invalid";
        return result;
    }

    private void message(Session session, String json) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getOpCodeData(Instruction i) {

        // Display op-code data
        // For some op-codes the data type corresponds to the index of a function
        // or variable etc. To make the code easier to interpret we try to display
        // these instead, where we can.
        switch (i.opCode) {
            case OP_DECLARE:
            case OP_LOAD_VAR: {
                int index = i.value.getIntVal();
                if (vm.getVariables().isIndexValid(index))
                    return vm.getVariables().getVariables().get(index).name;
                else
                    return "???";
            }
            case OP_CALL_FUNC: {
                return this.compiler.getFunctionNameAt(i.value.getIntVal()) + "()";
            }
            case OP_CALL_DLL: {
                int index = i.value.getIntVal();
                return vm.getPlugins().getFunctionName(index >> 24, index & 0x00ffffff) + "()";
            }
            case OP_COPY:
            case OP_ALLOC: {
                return vm.getDataTypes().describeVariable("", vm.getStoredType(i.value.getIntVal()));
            }
            case OP_CREATE_USER_FRAME: {
                return compiler.getUserFunctionName(i.value.getIntVal()) + "()";
            }
            case OP_LOAD_CONST: {
                if (i.basicVarType == BasicValType.VTP_FUNC_PTR)
                {
                    if (i.value.getIntVal() == 0) return "[NULL]";
                    return compiler.getUserFunctionName(i.value.getIntVal() - 1) + "()";
                }
            }
        }
        return vm.basicValToString(i.value, i.basicVarType, true);
    }
}
