package com.basic4gl.runtime;

import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.OpCode;
import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Instruction
 *
 * Note: Instruction size = 12 bytes.
 * Ordering of member fields is important, as the two 4 byte members
 * are first (and hence aligned to a 4 byte boundary).
 * sourceChar is next, and aligned to a 2 byte boundary.
 * Single byte fields are last, as their alignment is unimportant.
 *
 * porting note: contained directive `#pragma pack (push, 1)`
 */
public class Instruction implements Streamable {

  /**
   * Instruction value
   */
  public Value value;

  /**
   * For debugging
   */
  public int sourceLine;

  public int sourceChar;
  public short opCode; // (vmOpCode)
  public int basicVarType; // (vmBasicVarType)

  public Instruction() {
    opCode = OpCode.OP_NOP;
    // TODO Original source initializes with 0 instead of -1
    basicVarType = BasicValType.VTP_UNDEFINED;
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
