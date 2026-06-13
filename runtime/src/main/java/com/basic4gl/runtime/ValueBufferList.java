package com.basic4gl.runtime;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.RandomAccess;

/**
 * A contiguous value store backed by a ByteBuffer.
 * Each Value occupies 4 bytes and can be interpreted as either int or float.
 */
public class ValueBufferList extends AbstractList<Value> implements RandomAccess {
    private static final int BYTES_PER_VALUE = Integer.BYTES;

    private ByteBuffer byteBuffer;
    private IntBuffer intBuffer;
    private FloatBuffer floatBuffer;
    private final ByteOrder byteOrder;

    private int size;
    private final ArrayList<ValueView> valueViews;

    public ValueBufferList() {
        this(0, ByteOrder.LITTLE_ENDIAN);
    }

    public ValueBufferList(int initialCapacity, ByteOrder byteOrder) {
        this.byteOrder = byteOrder == null ? ByteOrder.LITTLE_ENDIAN : byteOrder;
        this.byteBuffer = ByteBuffer.allocateDirect(Math.max(0, initialCapacity) * BYTES_PER_VALUE).order(this.byteOrder);
        this.intBuffer = this.byteBuffer.asIntBuffer();
        this.floatBuffer = this.byteBuffer.asFloatBuffer();
        this.valueViews = new ArrayList<>();
        this.size = 0;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public int getIntValue(int index) {
        checkElementIndex(index);
        return intBuffer.get(index);
    }

    public void setIntValue(int index, int value) {
        checkElementIndex(index);
        intBuffer.put(index, value);
    }

    public float getFloatValue(int index) {
        checkElementIndex(index);
        return floatBuffer.get(index);
    }

    public void setFloatValue(int index, float value) {
        checkElementIndex(index);
        floatBuffer.put(index, value);
    }

    public void fillInts(int startIndex, int length, int value) {
        checkRange(startIndex, length);
        for (int i = 0; i < length; i++) {
            intBuffer.put(startIndex + i, value);
        }
    }

    public void copyInts(int sourceIndex, int destIndex, int length) {
        checkRange(sourceIndex, length);
        checkRange(destIndex, length);
        if (length == 0 || sourceIndex == destIndex) {
            return;
        }

        // Use memmove-like behavior when source and destination overlap.
        if (destIndex > sourceIndex && destIndex < sourceIndex + length) {
            for (int i = length - 1; i >= 0; i--) {
                intBuffer.put(destIndex + i, intBuffer.get(sourceIndex + i));
            }
            return;
        }

        for (int i = 0; i < length; i++) {
            intBuffer.put(destIndex + i, intBuffer.get(sourceIndex + i));
        }
    }

    public void resize(int newSize) {
        if (newSize < 0) {
            throw new IllegalArgumentException("newSize must be >= 0");
        }

        ensureCapacity(newSize);

        if (newSize > size) {
            for (int i = size; i < newSize; i++) {
                intBuffer.put(i, 0);
            }
        }

        size = newSize;

        if (valueViews.size() > newSize) {
            valueViews.subList(newSize, valueViews.size()).clear();
        }
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity <= intBuffer.capacity()) {
            return;
        }

        int newCapacity = Math.max(minCapacity, Math.max(4, intBuffer.capacity() * 2));
        ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(newCapacity * BYTES_PER_VALUE).order(byteOrder);
        IntBuffer newIntBuffer = newByteBuffer.asIntBuffer();
        for (int i = 0; i < size; i++) {
            newIntBuffer.put(i, intBuffer.get(i));
        }

        byteBuffer = newByteBuffer;
        intBuffer = newIntBuffer;
        floatBuffer = newByteBuffer.asFloatBuffer();
    }

    @Override
    public Value get(int index) {
        checkElementIndex(index);
        return getOrCreateView(index);
    }

    @Override
    public Value set(int index, Value element) {
        checkElementIndex(index);
        Value previous = new Value(getIntValue(index));
        if (element == null) {
            setIntValue(index, 0);
        } else {
            setIntValue(index, element.getIntVal());
        }
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
            return intBuffer.get(index);
        }

        @Override
        public float getRealVal() {
            return floatBuffer.get(index);
        }

        @Override
        public void setIntVal(int val) {
            intBuffer.put(index, val);
        }

        @Override
        public void setRealVal(float val) {
            floatBuffer.put(index, val);
        }

        @Override
        public void setVal(Value val) {
            intBuffer.put(index, val == null ? 0 : val.getIntVal());
        }
    }
}

