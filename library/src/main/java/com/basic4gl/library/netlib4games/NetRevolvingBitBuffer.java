package com.basic4gl.library.netlib4games;

import com.basic4gl.library.netlib4games.internal.Assert;

/**
 * Used to track which packets have already been received by their ID.
 * Current implementation as an array of booleans.
 * Could be replaced by a proper bit array as an optimisation.
 */
public class NetRevolvingBitBuffer {
    private final int size;
    private final boolean[] data;

    // Position pointers
    private int hiPosition;
    private int hiRevolving;
    private int lowPosition;
    private int lowRevolving;

    public NetRevolvingBitBuffer(int size, int initialTop) {
        this.size = size;
        hiPosition = initialTop;
        lowPosition = initialTop;
        hiRevolving = 0;
        lowRevolving = 0;

        Assert.assertTrue(this.size > 0);
        data = new boolean[this.size];
    }

    public void dispose() {}

    int getHiPosition() {
        return hiPosition;
    }

    int getLowPosition() {
        return lowPosition;
    }

    boolean isInRange(long index) {
        return (index >= lowPosition && index < hiPosition);
    }

    int getRevolvingIndex(int index) {
        Assert.assertTrue(isInRange(index));
        return (lowRevolving + index - lowPosition) % size;
    }

    boolean getValueAt(int index) {
        return data[getRevolvingIndex(index)];
    }

    void set(int index, boolean value) {
        data[getRevolvingIndex(index)] = value;
    }

    /**
     * @return true if falses removed
     */
    boolean setTop(int index, boolean initialValue) {
        boolean truesRemoved = false;
        boolean falsesRemoved = false;

        // Remove low array elements
        if (index > size) {
            int lowIndex = index - size;
            while (this.lowPosition < lowIndex) {

                // Calculate value removed.
                boolean value = this.lowPosition < hiPosition ? data[lowRevolving] : initialValue;

                // Update flags accordingly
                truesRemoved = truesRemoved || value;
                falsesRemoved = falsesRemoved || !value;

                // Update low poiners
                if (++lowRevolving >= size) {
                    lowRevolving = 0;
                }
                this.lowPosition++;
            }
        }

        // Initialise high array elements
        while (hiPosition < index) {

            // Initialise data
            if (hiPosition >= lowPosition) {
                data[hiRevolving] = initialValue;
            }

            // Update high pointers
            if (++hiRevolving >= size) {
                hiRevolving = 0;
            }
            hiPosition++;
        }

        return falsesRemoved;
    }
}
