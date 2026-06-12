package com.basic4gl.compiler;

/**
 * Used to track program jumps. Actual addresses are patched into jump
 * instructions after the main compilation pass has completed. (Thus forward
 * jumps are possible.)
 */
class Jump {
    private int jumpInstruction;

    private String labelName;

    Jump(int instruction, String labelName) {
        jumpInstruction = instruction;
        this.labelName = labelName;
    }

    Jump() {
        jumpInstruction = 0;
        labelName = "";
    }

    /**
     * Instruction containing jump instruction
     */
    public int getJumpInstruction() {
        return jumpInstruction;
    }

    public void setJumpInstruction(int jumpInstruction) {
        this.jumpInstruction = jumpInstruction;
    }

    /**
     * Label to which we are jumping
     */
    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }
}
