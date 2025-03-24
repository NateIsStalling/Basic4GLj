package com.basic4gl.runtime;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Abstract template class for allocating and referencing a specific type of
 * object.
 * Used for strings, pointers, handles e.t.c.
 * The virtual machine stores only the array index, and thus VM programs avoid
 * having to see and manipulate pointers, handles e.t.c.
 */
public class Store<T> {

    private final Vector<T> array;
    private final Vector<Boolean> valAllocated;

    /**
     * List of free indices
     */
    private final ArrayList<Integer> freeList;

    /**
     * New elements are initialised to this
     */
    private final T blankElement;

    /**
     * @param blankElement New elements are initialised to this
     */
    public Store(T blankElement) {
        this.blankElement = blankElement;

        array = new Vector<>();
        valAllocated = new Vector<>();

        freeList = new ArrayList<>();
    }

    public boolean isIndexValid(int index) { // Return true if index is a valid
        // allocated index
        return index >= 0 && index < array.size() && valAllocated.get(index);
    }

    public boolean isIndexStored(int index) {
        return index != 0 && isIndexValid(index);
    }

    public T getValueAt(int index) {
        assertTrue(isIndexValid(index));
        return array.get(index);
    }

    public void setValue(int index, T val) {
        assertTrue(isIndexValid(index));
        array.set(index, (T) val);
    }

    public int alloc() {
        int index;
        if (freeList.isEmpty()) {

            // Extend array by a single item, and return index of that item
            index = array.size();
            array.add(blankElement);
            valAllocated.add(true); // Mark element as in use
        } else {

            // Reuse previously freed index
            index = freeList.get(0);
            freeList.remove(0);
            // index = 0; possible porting mistake, my bad.. the index = this.freeList.get(0) should be
            // correct

            // Initialise element
            array.set(index, blankElement);
            valAllocated.set(index, true);
        }
        return index;
    }

    public void freeAtIndex(int index) {

        // Deallocate index and return to array
        assertTrue(isIndexValid(index));
        valAllocated.set(index, false);
        freeList.add(0, index);
    }

    public void clear() {

        // Clear allocated values
        freeList.clear();
        array.clear();
        valAllocated.clear();

        // Allocate a "blank" value for the 0th element.
        // Basic4GL uses 0 to indicate that data hasn't been allocated yet.
        alloc();
    }

    public int getStoredElements() {
        return array.size() - freeList.size();
    }

    public Vector<T> getArray() {
        return array;
    }

    public Vector<Boolean> getValAllocated() {
        return valAllocated;
    }

    public T getBlankElement() {
        return blankElement;
    }
}
