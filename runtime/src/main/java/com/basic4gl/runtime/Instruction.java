package com.basic4gl.runtime;

import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.OpCode;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * A single operation for the virtual machine to perform.
 *
 * Consists of an opcode to specify what to do, a small type annotation for
 * expressing type-specific operations, an optional operand, and the source line
 * and column of the original source code.
 */
public class Instruction implements Streamable {
    // The action to perform.
    // TODO: this should be an enumeration.
    public short opCode = OpCode.OP_NOP;

    // An extra type annotation. Used in some operations to specify type-specific
    // operations inside the VM.
    // TODO: Which operations use this? Should it be constrained to the BasicVarType
    // stuff?
    public int basicVarType;

    /**
     * An optional operand. Jump addresses, variable load indices, and other stuff
     * like that ends up here.
     */
    public Value value;

    /**
     * For debugging
     */
    public int sourceLine = 0;

    public int sourceChar = 0;

    public Instruction() {
        opCode = OpCode.OP_NOP;
        basicVarType = BasicValType.VTP_INT;
        sourceLine = 0;
        sourceChar = 0;
        value = new Value();
    }

    public Instruction(Instruction i) {
        opCode = i.opCode;
        basicVarType = i.basicVarType;
        sourceChar = i.sourceChar;
        sourceLine = i.sourceLine;
        value = i.value;
    }

    public Instruction(short opCode) {
        this();
        this.opCode = opCode;
    }

    public Instruction(short opCode, int type) {
        this();
        this.opCode = opCode;
        this.basicVarType = type;
    }

    public Instruction(short opCode, int type, Value val) {
        this(opCode, type, val, 0, 0);
    }

    public Instruction(short opCode, int type, Value val, int sourceLine, int sourceChar) {
        this.opCode = opCode;
        basicVarType = type;
        value = val;
        this.sourceLine = sourceLine;
        this.sourceChar = sourceChar;
    }

    public String toString() {
        return "<Instruction " + OpCode.vmOpCodeName(this.opCode) + " " + this.value + ">";
    }

    // Given a list of instructions, construct a string with the disassembled code.
    public static String disassemble(List<Instruction> instructions) {
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < instructions.size(); idx++) {
            final Instruction i = instructions.get(idx);
            sb.append(idx + ":");
            sb.append(OpCode.vmOpCodeName(i.opCode));
            sb.append(" ");
            sb.append(BasicValType.getName(i.basicVarType));
            sb.append(" ");
            sb.append(i.value);
            sb.append("\n");
        }
        return sb.toString();
    }

    // Streaming
    // #ifdef VM_STATE_STREAMING
    public void streamOut(DataOutputStream stream) throws IOException {
        Streaming.writeShort(stream, opCode);
        Streaming.writeLong(stream, basicVarType);
        value.streamOut(stream);

        Streaming.writeLong(stream, sourceLine);
        Streaming.writeLong(stream, sourceChar);
    }

    public boolean streamIn(DataInputStream stream) throws IOException {
        opCode = Streaming.readShort(stream);
        basicVarType = (int) Streaming.readLong(stream);
        value.streamIn(stream);

        sourceLine = (int) Streaming.readLong(stream);
        sourceChar = (int) Streaming.readLong(stream);
        return true;
    }
}
