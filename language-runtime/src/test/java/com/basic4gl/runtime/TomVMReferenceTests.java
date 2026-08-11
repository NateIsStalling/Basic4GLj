package com.basic4gl.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.basic4gl.language.core.runtime.Value;
import com.basic4gl.language.spi.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TomVMReferenceTests {

    private TomVM vm;

    @BeforeEach
    void setUp() {
        vm = new TomVM(new TestPluginManager(), null, 1024, 256);
    }

    @Test
    void refParameterWriteUpdatesVmMemory() {
        int dataIndex = vm.getData().allocate(1);
        vm.getData().data().setIntValue(dataIndex, 10);

        // Reference parameters are VM data addresses stored on the parameter stack.
        vm.getStack().push(dataIndex);

        Value ref = vm.getRefParam(1);
        ref.setIntVal(20);

        assertEquals(20, vm.getData().data().getIntValue(dataIndex));
    }

    @Test
    void vmMemoryWriteIsVisibleThroughExistingRefParameter() {
        int dataIndex = vm.getData().allocate(1);
        vm.getData().data().setIntValue(dataIndex, 10);

        vm.getStack().push(dataIndex);

        Value ref = vm.getRefParam(1);

        vm.getData().data().setIntValue(dataIndex, 30);

        assertEquals(30, ref.getIntVal());
    }

    @Test
    void refParameterRemainsValidAfterDataBufferGrowth() {
        int dataIndex = vm.getData().allocate(1);
        vm.getData().data().setIntValue(dataIndex, 10);

        vm.getStack().push(dataIndex);
        Value ref = vm.getRefParam(1);

        // Force the ValueBufferList backing array to grow after the view exists.
        vm.getData().allocate(600);

        ref.setIntVal(40);

        assertEquals(40, vm.getData().data().getIntValue(dataIndex));

        vm.getData().data().setIntValue(dataIndex, 50);
        assertEquals(50, ref.getIntVal());
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
