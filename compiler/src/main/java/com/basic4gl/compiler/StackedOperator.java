package com.basic4gl.compiler;

class StackedOperator {
    private Operator operator;

    private int lazyJumpAddress;

    StackedOperator(Operator o) {
        operator = o;
        lazyJumpAddress = -1;
    }

    StackedOperator(Operator o, int lazyJumpAddr) {
        operator = o;
        lazyJumpAddress = lazyJumpAddr;
    }

    /**
     * Stacked operator
     */
    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * Address of lazy jump op code (for "and" and "or" operations)
     */
    public int getLazyJumpAddress() {
        return lazyJumpAddress;
    }

    public void setLazyJumpAddress(int lazyJumpAddress) {
        this.lazyJumpAddress = lazyJumpAddress;
    }
}
