package com.basic4gl.runtime;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Used to stack values for reverse-Polish expression evaluation, or as
 * function parameters.
 */
public class ValueStack {
    private int[] data;
    private int size;
    private final Store<String> strings;

    public ValueStack(Store<String> strings) {
        this.strings = strings;
        data = new int[10000000];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void push(int v) { // Push v as NON string
        data[size++] = v;
    }

    public void pushString(String str) {
        int index = strings.alloc(); // Allocate string
        strings.setValue(index, str); // Copy value
        push(index); // Create stack index
    }

    public int tos() {
        assertTrue(!isEmpty());
        return data[size - 1];
    }

    public int pop() {
        assertTrue(!isEmpty());
        return data[--size];
    }

    public String popString() {
        assertTrue(!isEmpty());
        String str;
        // Copy string value from stack
        int index = tos();
        assertTrue(strings.isIndexValid(index));
        str = strings.getValueAt(index);
        // Deallocate stacked string
        strings.freeAtIndex(index);

        // Remove stack element
        pop();
        return str;
    }

    public void clear() {
        size = 0;
    }

    public int size() {
        return size;
    }

    public int get(int index) {
        assertTrue(index >= 0 && index < size && index < data.length);
        return data[index];
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) {
            return;
        }

        int newCapacity = data.length;

        while (newCapacity < minCapacity) {
            newCapacity *= 2;
        }

        data = Arrays.copyOf(data, newCapacity);
    }
    public void resize(int size) {
        int oldSize = this.size;

        if (size > data.length) {
            ensureCapacity(size);
        }

        if (size > oldSize) {
            Arrays.fill(data, oldSize, size, 0);
        }

        this.size = size;
    }
}
