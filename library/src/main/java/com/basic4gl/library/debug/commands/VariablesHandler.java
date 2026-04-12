package com.basic4gl.library.debug.commands;

import com.basic4gl.debug.protocol.callbacks.VariablesCallback;
import com.basic4gl.debug.protocol.commands.VariablesCommand;
import com.basic4gl.debug.protocol.types.Variable;
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
            case VariablesCommand.REF_GLOBALS -> buildGlobalVariables();
            default -> new ArrayList<>();
        };

        VariablesCallback callback = new VariablesCallback();
        callback.setRequestId(requestId);
        callback.setVariables(mapped.toArray(new Variable[0]));

        message(session, gson.toJson(callback));
    }

    private List<Variable> buildGlobalVariables() {
        List<Variable> mapped = new ArrayList<>();
        for (VariableCollection.Variable vmVariable : vm.getVariables().getVariables()) {
            Variable variable = new Variable();
            variable.name = vmVariable.name;
            variable.type = vm.getDataTypes().describeVariable("", vmVariable.type).trim();
            variable.value = getValueString(vmVariable);
            variable.variablesReference = 0;
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
        return variable;
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
            return "[UNALLOCATED]";
        }

        ValType valueType = new ValType(vmVariable.type);
        Value value;

        if (valueType.isBasicType() || valueType.pointerLevel > 0) {
            value = vm.getData().data().get(vmVariable.dataIndex);
        } else {
            value = new Value(vmVariable.dataIndex);
            valueType.pointerLevel = 1;
            valueType.isByRef = true;
        }

        try {
            Mutable<Integer> maxChars = new Mutable<>(TomVM.DATA_TO_STRING_MAX_CHARS);
            return vm.valToString(value, valueType, maxChars);
        } catch (Exception ex) {
            return "[ERROR]";
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
