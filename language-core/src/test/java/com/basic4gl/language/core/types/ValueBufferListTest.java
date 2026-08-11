package com.basic4gl.language.core.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.basic4gl.language.core.runtime.Value;
import org.junit.jupiter.api.Test;

class ValueBufferListTest {

    @Test
    void valueViewWriteIsVisibleThroughPrimitiveAccess() {
        ValueBufferList list = new ValueBufferList();
        list.resize(1);

        Value view = list.get(0);
        view.setIntVal(123);

        assertEquals(123, list.getIntValue(0));
    }

    @Test
    void primitiveWriteIsVisibleThroughExistingValueView() {
        ValueBufferList list = new ValueBufferList();
        list.resize(1);

        Value view = list.get(0);

        list.setIntValue(0, 456);

        assertEquals(456, view.getIntVal());
    }

    @Test
    void floatViewAndPrimitiveAccessUseSameBackingBits() {
        ValueBufferList list = new ValueBufferList();
        list.resize(1);

        Value view = list.get(0);

        float first = -0.0f;
        list.setFloatValue(0, first);

        assertEquals(Float.floatToRawIntBits(first), Float.floatToRawIntBits(view.getRealVal()));

        float second = 3.25f;
        view.setRealVal(second);

        assertEquals(Float.floatToRawIntBits(second), list.getIntValue(0));
        assertEquals(second, list.getFloatValue(0));
    }

    @Test
    void valueViewRemainsValidAfterBufferGrowth() {
        ValueBufferList list = new ValueBufferList(1);
        list.resize(1);
        list.setIntValue(0, 123);

        Value view = list.get(0);

        // Forces replacement of the original backing int[].
        list.resize(1024);

        assertEquals(123, view.getIntVal());

        list.setIntValue(0, 456);
        assertEquals(456, view.getIntVal());

        view.setIntVal(789);
        assertEquals(789, list.getIntValue(0));
    }

    @Test
    void newlyExposedValuesAreZeroInitialized() {
        ValueBufferList list = new ValueBufferList();

        list.resize(8);

        for (int i = 0; i < 8; i++) {
            assertEquals(0, list.getIntValue(i));
        }
    }

    @Test
    void shrinkingAndRegrowingClearsReusedValues() {
        ValueBufferList list = new ValueBufferList();
        list.resize(8);

        list.setIntValue(6, 123);
        assertEquals(123, list.getIntValue(6));

        list.resize(4);
        list.resize(8);

        assertEquals(0, list.getIntValue(6));
    }

    @Test
    void copyIntsHandlesForwardOverlap() {
        ValueBufferList list = values(1, 2, 3, 4, 5);

        list.copyInts(0, 1, 4);

        assertValues(list, 1, 1, 2, 3, 4);
    }

    @Test
    void copyIntsHandlesBackwardOverlap() {
        ValueBufferList list = values(1, 2, 3, 4, 5);

        list.copyInts(1, 0, 4);

        assertValues(list, 2, 3, 4, 5, 5);
    }

    private static ValueBufferList values(int... values) {
        ValueBufferList list = new ValueBufferList(values.length);
        list.resize(values.length);

        for (int i = 0; i < values.length; i++) {
            list.setIntValue(i, values[i]);
        }

        return list;
    }

    private static void assertValues(ValueBufferList list, int... expected) {
        assertEquals(expected.length, list.size());

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], list.getIntValue(i), "Unexpected value at index " + i);
        }
    }
}
