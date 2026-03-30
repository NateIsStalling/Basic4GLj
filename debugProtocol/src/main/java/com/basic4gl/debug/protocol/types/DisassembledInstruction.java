package com.basic4gl.debug.protocol.types;

public class DisassembledInstruction {
    /**
     * The address of the instruction. Treated as a hex value if prefixed with
     * `0x`, or as a decimal value otherwise.
     */
    public String address;

    /**
     * Raw bytes representing the instruction and its operands, in an
     * implementation-defined format.
     */
    public String instructionBytes;

    /**
     * Text representing the instruction and its operands, in an
     * implementation-defined format.
     */
    public String instruction;

    /**
     * Name of the symbol that corresponds with the location of this instruction,
     * if any.
     */
    public String symbol;

    /**
     * Source location that corresponds to this instruction, if any.
     * Should always be set (if available) on the first instruction returned,
     * but can be omitted afterwards if this instruction maps to the same source
     * file as the previous instruction.
     */
    public Source location;

    /**
     * The line within the source location that corresponds to this instruction,
     * if any.
     */
    public Integer line;

    /**
     * The column within the line that corresponds to this instruction, if any.
     */
    public Integer column;

    /**
     * The end line of the range that corresponds to this instruction, if any.
     */
    public Integer endLine;

    /**
     * The end column of the range that corresponds to this instruction, if any.
     */
    public Integer endColumn;

    /**
     * A hint for how to present the instruction in the UI.
     *
     * A value of `invalid` may be used to indicate this instruction is 'filler'
     * and cannot be reached by the program. For example, unreadable memory
     * addresses may be presented is 'invalid.'
     * Values: 'normal', 'invalid'
     */
    public String presentationHint;
}
