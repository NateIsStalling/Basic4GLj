package com.basic4gl.library.debug.commands;

import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.debug.protocol.commands.VariablesCommand;
import com.basic4gl.debug.protocol.types.Variable;
import com.basic4gl.debug.protocol.types.VariablePresentationHint;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.Value;
import com.basic4gl.runtime.VariableCollection;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Mutable;
import com.google.gson.Gson;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

public class VariablesHandler {
    private static final int MAX_MEMORY_ROWS = 256;
    private static final int MAX_VARIABLE_TEXT_CHARS = 500;
    private static final String TRUNCATED_SUFFIX = "... [truncated]";
    private static final String REFERENCE_VALUE = "[REFERENCE]";
    private static final String UNALLOCATED_VALUE = "[UNALLOCATED]";
    private static final String ERROR_VALUE = "[ERROR]";

    private final TomVM vm;
    private final Gson gson;

    public VariablesHandler(TomVM vm, Gson gson) {
        this.vm = vm;
        this.gson = gson;
    }

    public void handle(VariablesCommand variablesCommand, int requestId, Session session) {
        int reference = VariablesCommand.REF_GLOBALS;
        Integer start = null;
        Integer count = null;
        if (variablesCommand != null && variablesCommand.arguments != null) {
            reference = variablesCommand.arguments.variablesReference;
            start = variablesCommand.arguments.start;
            count = variablesCommand.arguments.count;
        }

        List<Variable> mapped = switch (reference) {
            case VariablesCommand.REF_REGISTERS -> buildRegisterVariables();
            case VariablesCommand.REF_HEAP -> buildHeapVariables(start, count);
            case VariablesCommand.REF_STACK -> buildStackVariables(start, count);
            case VariablesCommand.REF_GLOBALS -> buildGlobalVariables(start, count);
            default -> new ArrayList<>();
        };

        VariablesCallback callback = new VariablesCallback();
        callback.setRequestId(requestId);
        callback.setVariables(mapped.toArray(new Variable[0]));

        message(session, gson.toJson(callback));
    }

    private List<Variable> buildGlobalVariables(Integer start, Integer count) {
        int begin = Math.max(0, start != null ? start : 0);
        int maxRows = count != null ? Math.max(0, count) : MAX_MEMORY_ROWS;
        List<VariableCollection.Variable> globals = vm.getVariables().getVariables();
        int end = Math.min(globals.size(), begin + maxRows);

        List<Variable> mapped = new ArrayList<>();
        for (int i = begin; i < end; i++) {
            VariableCollection.Variable vmVariable = globals.get(i);
            Variable variable = new Variable();
            variable.name = vmVariable.name;
            variable.type = vm.getDataTypes().describeVariable("", vmVariable.type).trim();
            variable.value = getValueString(vmVariable);
            variable.evaluateName = vmVariable.name;
            variable.variablesReference = 0;
            applyLazyHintForLargePayload(variable, vmVariable);
            mapped.add(variable);
        }
        return mapped;
    }

    private List<Variable> buildRegisterVariables() {
        List<Variable> mapped = new ArrayList<>();
        Variable register1 = toVmRow("Register 1", vm.getReg());
        register1.evaluateName = vm.getRegString();
        mapped.add(register1);

        Variable register2 = toVmRow("Register 2", vm.getReg2());
        register2.evaluateName = vm.getReg2String();
        mapped.add(register2);

        return mapped;
    }

    private List<Variable> buildHeapVariables(Integer start, Integer count) {
        int begin = Math.max(0, start != null ? start : 0);
        int maxRows = count != null ? Math.max(0, count) : MAX_MEMORY_ROWS;
        int end = Math.min(vm.getData().size(), begin + maxRows);

        List<Variable> mapped = new ArrayList<>();
        for (int i = begin; i < end; i++) {
            mapped.add(toVmRow(Integer.toString(i), vm.getData().data().get(i)));
        }
        return mapped;
    }

    private List<Variable> buildStackVariables(Integer start, Integer count) {
        int stackSize = vm.getStack().size();
        int begin = Math.max(0, start != null ? start : 0);
        int maxRows = count != null ? Math.max(0, count) : MAX_MEMORY_ROWS;
        int end = Math.min(stackSize, begin + maxRows);

        List<Variable> mapped = new ArrayList<>();
        for (int offset = begin; offset < end; offset++) {
            int stackIndex = stackSize - 1 - offset;
            mapped.add(toVmRow(Integer.toString(stackIndex), vm.getStack().get(stackIndex)));
        }
        return mapped;
    }

    private Variable toVmRow(String name, Value value) {
        Variable variable = new Variable();
        variable.name = name;
        variable.value = Integer.toString(value.getIntVal());
        variable.type = Float.toString(value.getRealVal());
        variable.evaluateName = lookupString(value.getIntVal());
        variable.variablesReference = 0;
        applyLazyHintForLargePayload(variable, null);
        return variable;
    }

    private void applyLazyHintForLargePayload(Variable variable, VariableCollection.Variable vmVariable) {
        boolean truncatedValue = false;
        boolean truncatedEvaluateName = false;

        if (vmVariable != null && vmVariable.allocated()) {
             ValType valueType = new ValType(vmVariable.type);
            // Apply lazy loading hint for structured variables, as their value string can be expensive to compute
            if (!valueType.isBasicType() && valueType.pointerLevel <= 0) {
                // TODO Needing to review why valToString is slow for structured variables. Defer to lazy loading for now
                truncatedEvaluateName = true;
            }
        }

        if (variable.value != null && variable.value.length() > MAX_VARIABLE_TEXT_CHARS) {
            variable.value = abbreviate(variable.value);
            truncatedValue = true;
        }
        if (variable.evaluateName != null && variable.evaluateName.length() > MAX_VARIABLE_TEXT_CHARS) {
            variable.evaluateName = abbreviate(variable.evaluateName);
            truncatedEvaluateName = true;
        }

        if (truncatedValue || truncatedEvaluateName) {
            if (variable.presentationHint == null) {
                variable.presentationHint = new VariablePresentationHint();
            }
            variable.presentationHint.lazy = true;
        }
    }

    private String abbreviate(String text) {
        return text.substring(0, MAX_VARIABLE_TEXT_CHARS) + TRUNCATED_SUFFIX;
    }

    private String lookupString(int index) {
        if (!vm.getStringStore().isIndexStored(index)) {
            return "";
        }
        String str = vm.getString(index);
        return str != null ? str : "";
    }

    private String getValueString(VariableCollection.Variable vmVariable) {
        if (!vmVariable.allocated()) {
            return UNALLOCATED_VALUE;
        }

        ValType valueType = new ValType(vmVariable.type);
        Value value;

        if (valueType.isBasicType() || valueType.pointerLevel > 0) {
            value = vm.getData().data().get(vmVariable.dataIndex);
        } else {
            value = new Value(vmVariable.dataIndex);
            valueType.pointerLevel = 1;
            valueType.isByRef = true;
            // TODO: Needing to review why valToString is slow for structured variables. Defer to lazy loading for now
            return REFERENCE_VALUE;
        }

        try {
            Mutable<Integer> maxChars = new Mutable<>(TomVM.DATA_TO_STRING_MAX_CHARS);
            return vm.valToString(value, valueType, maxChars);
        } catch (Exception ex) {
            return ERROR_VALUE;
        }
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
}
