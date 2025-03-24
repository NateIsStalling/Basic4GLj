package com.basic4gl.runtime.stackframe;

/**
 * Indicates a range of data on the stack protected from destruction.
 * (Used when collapsing temporary data, to destroy everything except the data
 * to be returned).
 */
public class ProtectedStackRange {
    private final int startAddress, endAddress;

    public ProtectedStackRange() {
        startAddress = 0;
        endAddress = 0;
    }

    public ProtectedStackRange(int startAddress, int endAddress) {
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    public boolean containsAddress(int address) {
        return address >= startAddress && address < endAddress;
    }

    public boolean containsRange(int start, int end) {
        return start >= startAddress && end <= endAddress;
    }
}
