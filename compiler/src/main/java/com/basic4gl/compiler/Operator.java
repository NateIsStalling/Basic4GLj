package com.basic4gl.compiler;


import com.basic4gl.compiler.types.OperType;
import com.basic4gl.runtime.types.OpCode;

/**
 * Used for tracking which operators are about to be applied to operands.
 * Basic4GL converts infix expressions into reverse polish using an operator
 * stack and an operand stack.
 */
class Operator {
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