package com.basic4gl.runtime;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.RandomAccess;

import static java.lang.Float.floatToRawIntBits;
import static java.lang.Float.intBitsToFloat;

public class ValueBufferList extends AbstractList<Value> implements RandomAccess {
    private int[] values;
    private int size;
    private final ArrayList<ValueView> valueViews;

    public ValueBufferList() {
        this(0);
    }

    public ValueBufferList(int initialCapacity) {
        values = new int[Math.max(0, initialCapacity)];
        valueViews = new ArrayList<>();
        size = 0;
    }

    public int getIntValue(int index) {
        checkElementIndex(index);
        return values[index];
    }

    public void setIntValue(int index, int value) {
        checkElementIndex(index);
        values[index] = value;
    }

    public float getFloatValue(int index) {
        checkElementIndex(index);
        return intBitsToFloat(values[index]);
    }

    public void setFloatValue(int index, float value) {
        checkElementIndex(index);
        values[index] = floatToRawIntBits(value);
    }

    // Use only inside VM/runtime paths that already validated the address.
    public int getIntValueFast(int index) {
        return values[index];
    }

    public void setIntValueFast(int index, int value) {
        values[index] = value;
    }

    public float getFloatValueFast(int index) {
        return intBitsToFloat(values[index]);
    }

    public void setFloatValueFast(int index, float value) {
        values[index] = floatToRawIntBits(value);
    }

    public int[] rawArray() {
        return values;
    }

    public void fillInts(int startIndex, int length, int value) {
        checkRange(startIndex, length);
        Arrays.fill(values, startIndex, startIndex + length, value);
    }

    public void fillIntsFast(int startIndex, int length, int value) {
        Arrays.fill(values, startIndex, startIndex + length, value);
    }

    public void copyInts(int sourceIndex, int destIndex, int length) {
        checkRange(sourceIndex, length);
        checkRange(destIndex, length);
        System.arraycopy(values, sourceIndex, values, destIndex, length);
    }

    public void copyIntsFast(int sourceIndex, int destIndex, int length) {
        System.arraycopy(values, sourceIndex, values, destIndex, length);
    }

    public void resize(int newSize) {
        if (newSize < 0) {
            throw new IllegalArgumentException("newSize must be >= 0");
        }

        ensureCapacity(newSize);

        if (newSize > size) {
            Arrays.fill(values, size, newSize, 0);
        }

        size = newSize;

        if (valueViews.size() > newSize) {
            valueViews.subList(newSize, valueViews.size()).clear();
        }
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity <= values.length) {
            return;
        }

        int newCapacity = Math.max(minCapacity, Math.max(4, values.length * 2));
        values = Arrays.copyOf(values, newCapacity);
    }

    @Override
    public Value get(int index) {
        checkElementIndex(index);
        return getOrCreateView(index);
    }

    @Override
    public Value set(int index, Value element) {
        checkElementIndex(index);

        Value previous = new Value(values[index]);
        values[index] = element == null ? 0 : element.getIntVal();

        return previous;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        size = 0;
        valueViews.clear();
    }

    private Value getOrCreateView(int index) {
        while (valueViews.size() <= index) {
            valueViews.add(null);
        }

        ValueView view = valueViews.get(index);
        if (view == null) {
            view = new ValueView(index);
            valueViews.set(index, view);
        }

        return view;
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", size: " + size);
        }
    }

    private void checkRange(int startIndex, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }

        if (startIndex < 0 || startIndex + length > size) {
            throw new IndexOutOfBoundsException(
                    "startIndex: " + startIndex + ", length: " + length + ", size: " + size);
        }
    }

    private final class ValueView extends Value {
        private final int index;

        private ValueView(int index) {
            this.index = index;
        }

        @Override
        public int getIntVal() {
            return values[index];
        }

        @Override
        public float getRealVal() {
            return intBitsToFloat(values[index]);
        }

        @Override
        public void setIntVal(int val) {
            values[index] = val;
        }

        @Override
        public void setRealVal(float val) {
            values[index] = floatToRawIntBits(val);
        }

        @Override
        public void setVal(Value val) {
            values[index] = val == null ? 0 : val.getIntVal();
        }
    }
}