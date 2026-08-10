package com.basic4gl.language.core.runtime;

import static com.basic4gl.language.core.internal.Assert.assertTrue;
import static java.lang.Float.floatToRawIntBits;

import java.util.Arrays;

/**
 * Used to stack values for reverse-Polish expression evaluation, or as
 * function parameters.
 */
public class ValueStack {
    private int[] data;
    private int size;
    private final int limit;
    private final Store<String> strings;

    public ValueStack(int limit, Store<String> strings) {
        this.strings = strings;
        this.limit = limit;
        data = new int[limit];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void push(int v) { // Push v as NON string
        ensureCapacity(size + 1);
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

    public void set(int index, int value) {
        assertTrue(index >= 0 && index < size && index < data.length);
        data[index] = value;
    }

    public void set(int index, float value) {
        assertTrue(index >= 0 && index < size && index < data.length);
        data[index] = floatToRawIntBits(value);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) {
            return;
        }

        int newCapacity = data.length;

        assertTrue(minCapacity <= limit, "Stack overflow");

        while (newCapacity < minCapacity) {
            newCapacity *= 2;
        }

        newCapacity = Math.min(newCapacity, limit);

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
