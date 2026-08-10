package com.basic4gl.runtime.plugin;

import static com.basic4gl.language.core.extensions.Basic4GLExtendedTypeCode.PLUGIN_BASIC4GL_EXT_FLOAT;
import static com.basic4gl.language.core.extensions.Basic4GLExtendedTypeCode.PLUGIN_BASIC4GL_EXT_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.basic4gl.language.spi.PluginManager;
import com.basic4gl.runtime.TomVM;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TomVMPluginAdapterTests {

    private TomVM vm;
    private TomVMPluginAdapter adapter;

    @BeforeEach
    void setUp() {
        TestPluginManager pluginManager = new TestPluginManager();

        vm = new TomVM(pluginManager, null, 1024, 256);

        adapter = new TomVMPluginAdapter(vm, pluginManager.getStructureManager());
    }

    @Test
    void setParamSetsSimpleIntValue() {
        adapter.setType(PLUGIN_BASIC4GL_EXT_INT);

        // Parameter 1 is the top of the VM stack.
        vm.getStack().push(17);

        // Slot 17 deliberately contains unrelated data. This verifies that
        // the parameter value is not accidentally treated as a VM address.
        vm.getData().data().setIntValue(17, 1234);

        adapter.setParam(1, intBuffer(42));

        assertEquals(42, vm.getIntParam(1));
        assertEquals(1234, vm.getData().data().getIntValue(17));
    }

    @Test
    void setParamSetsSimpleFloatValue() {
        adapter.setType(PLUGIN_BASIC4GL_EXT_FLOAT);

        vm.getStack().push(Float.floatToRawIntBits(1.25f));

        adapter.setParam(1, floatBuffer(3.5f));

        assertEquals(3.5f, vm.getRealParam(1), 0.00001f);
    }

    @Test
    void setParamSetsSimpleStringValue() {
        int stringHandle = vm.allocString();
        vm.setString(stringHandle, "before");
        vm.getStack().push(stringHandle);

        adapter.setStringType(16);

        adapter.setParam(1, stringBuffer("after", 16));

        // Updating a string parameter should preserve its existing handle.
        assertEquals(stringHandle, vm.getIntParam(1));
        assertEquals("after", vm.getStringParam(1));
    }

    @Test
    void setParamAllocatesStringWhenParameterHasNoHandle() {
        vm.getStack().push(0);

        adapter.setStringType(16);

        adapter.setParam(1, stringBuffer("hello", 16));

        int stringHandle = vm.getIntParam(1);

        assertEquals("hello", vm.getString(stringHandle));
    }

    @Test
    void setParamWritesReferencedIntWithoutChangingReference() {
        adapter.setType(PLUGIN_BASIC4GL_EXT_INT);
        adapter.modTypeReference();

        int dataIndex = vm.getData().allocate(1);
        vm.getData().data().setIntValue(dataIndex, 10);

        // Reference parameters contain a VM data address.
        vm.getStack().push(dataIndex);

        adapter.setParam(1, intBuffer(99));

        assertEquals(99, vm.getData().data().getIntValue(dataIndex));

        // BasicDataFromCData advances its working data index internally.
        // That traversal must not replace the caller's original reference.
        assertEquals(dataIndex, vm.getIntParam(1));
    }

    @Test
    void getParamReadsSimpleIntValue() {
        adapter.setType(PLUGIN_BASIC4GL_EXT_INT);
        vm.getStack().push(42);

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);

        adapter.getParam(1, buffer);

        buffer.flip();

        assertEquals(42, buffer.getInt());
        assertEquals(42, vm.getIntParam(1));
    }

    @Test
    void getParamReadsReferencedIntWithoutChangingReference() {
        adapter.setType(PLUGIN_BASIC4GL_EXT_INT);
        adapter.modTypeReference();

        int dataIndex = vm.getData().allocate(1);
        vm.getData().data().setIntValue(dataIndex, 123);

        vm.getStack().push(dataIndex);

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);

        adapter.getParam(1, buffer);

        buffer.flip();

        assertEquals(123, buffer.getInt());
        assertEquals(dataIndex, vm.getIntParam(1));
    }

    private static ByteBuffer intBuffer(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        buffer.flip();
        return buffer;
    }

    private static ByteBuffer floatBuffer(float value) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(value);
        buffer.flip();
        return buffer;
    }

    private static ByteBuffer stringBuffer(String value, int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);

        byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
        int length = Math.min(bytes.length, Math.max(0, size - 1));

        buffer.put(bytes, 0, length);

        // The adapter reads the entire fixed-width C string buffer.
        while (buffer.position() < size) {
            buffer.put((byte) 0);
        }

        buffer.flip();
        return buffer;
    }

    private static class TestPluginManager extends PluginManager {

        TestPluginManager() {
            super(false);
        }

        @Override
        public boolean loadPlugin(String filename) {
            return false;
        }

        @Override
        public boolean unloadPlugin(String filename) {
            return false;
        }
    }
}
