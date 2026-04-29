package com.basic4gl.runtime.plugin;

import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.TypeLibrary;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Assert;
import com.basic4gl.runtime.util.PointerResourceStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import static com.basic4gl.runtime.plugin.Basic4GLExtendedTypeCode.*;
import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Stores and manages structures defined by plugins.
 */
public class PluginStructureManager {
    /**
     * Structure handle store
     */
    private PointerResourceStore<PluginStructure> structureStore = new PointerResourceStore<>();

    /**
     * Structures in the order they were created
     */
    private final ArrayList<PluginStructure> structures = new ArrayList<>();

    /**
     * Name->structure index lookup
     */
    private final HashMap<String,Integer> nameIndex = new HashMap<>();

    public PluginStructureManager() {
        clear();
    }

    /**
     * Create a new structure and return its handle.
     * <p>Note: Structure manager takes responsibility for deleting the structure.
     * @param structure
     * @return
     */
    public int add(PluginStructure structure){
        Assert.assertTrue(structure != null);

        // Structure must not already be stored
        if (findStructure(structure.getName()) != 0)
            return 0;

        // Add to ordered list
        structures.add(structure);

        // Store and allocate a handle
        structure.setHandle(structureStore.alloc(structure));

        // Map name to handle
        nameIndex.put(structure.getName(), structure.getHandle());

        return structure.getHandle();
    }

    /**
     *
     * @param handle
     * @return PluginStructure or null if handle invalid
     */
    public PluginStructure getStructure(int handle)
    {
        return structureStore.getValueAt(handle);
    }

    public void clear(){
        structureStore.clear();
        structures.clear();
        nameIndex.clear();

        // Add a blank element to the store. It will be assigned the handle 0.
        // Valid elements are not allocated handle 0.
        // (We are using 0 as a "no handle" value).
        // TODO is this handled by blankElement already (Mar 31, 2025)?       structureStore.alloc(null);
    }


    /**
     * Find structure by name.
     * @return Returns its handle, or 0 if not found.
     */
    public int findStructure(String name){
        String key = name.toLowerCase();
        if (nameIndex.containsKey(key)) {
            return nameIndex.get(key);
        }
        return 0;
    }

    /**
     * Remove all structures owned by a particular object
     * @param owner
     */
    public void removeOwnedStructures(Object owner){

        // Iterate over structures
        Iterator<PluginStructure> i = structures.iterator();

        while (i.hasNext()) {
            PluginStructure structure = i.next();

            // Find structures owned by given owner
            if (Objects.equals(structure.getOwner(), owner)) {

                // Remove from name index, structure store and structure list
                nameIndex.remove(structure.getName());
                structureStore.remove(structure.getHandle());
                i.remove();
            }
        }
    }

    /**
     * Return list of structures owned by owner
     * @param owner
     * @return
     */
    public String describeOwnedStructures(Object owner){
        String result = "";
        for (PluginStructure i : structures) {
            if (i.getOwner() == owner) {
                if (!result.isEmpty()) {
                    result += "\r\n";
                }
                PluginStructure structure = i;
                result += "struc " + structure.getName();
                for (int j = 0; j < structure.getFieldCount(); j++) {
                    PluginStructureField field = structure.getField(j);
                    result += "\r\n  dim ";
                    if (field.getDataType().getBaseType() >= 0) {
                        // Find referred to structure
                        PluginStructure fieldStructure = structureStore.getValueAt(field.getDataType().getBaseType());
                        if (fieldStructure != null) {
                            result += fieldStructure.getName() + " ";
                        } else {
                            result += "??? ";
                        }
                    }
                    for (int k = 0; k < field.getDataType().getPointerLevel(); k++) {
                        result += "&";
                    }
                    result += field.getFieldName();
                    for (int k = 0; k < field.getDataType().getArrayLevel(); k++) {
                        int value = field.getDataType().getArrayDims()[field.getDataType().getArrayLevel() - k - 1] - 1;
                        result += "(" + value + ")";
                    }

                    String fieldName = field.getFieldName();
                    char lastChar = fieldName.isEmpty() ? 0 : fieldName.charAt(fieldName.length() - 1);
                    switch (field.getDataType().getBaseType()) {
                        case PLUGIN_BASIC4GL_EXT_FLOAT:
                        case PLUGIN_BASIC4GL_EXT_DOUBLE:
                            if (lastChar != '#') {
                                result += " as single";
                            }
                            break;
                        case PLUGIN_BASIC4GL_EXT_STRING:
                            if (lastChar != '$') {
                                result += " as string";
                            }
                            break;
                    }
                }
                result += "\r\nendstruc";
            }
        }

        return result;
    }

    /**
     * Add structures to virtual machine
     * @param typeLib
     */
    public void addVMStructures(TypeLibrary typeLib) {

        // Convert structures to virtual machine
        // Iterate structures in the order that they were defined
        for (PluginStructure i : structures) {
            i.addToVM(typeLib, this);
        }
    }

    /**
     * Find the VM datatype that corresponds to plugin data type.
     * @param pluginType
     * @return
     */
    public ValType vmTypeFromPluginType(PluginDataType pluginType){

        ValType vmType = new ValType();

        // Structure?
        if (pluginType.getBaseType() > 0) {

            // Find structure
            PluginStructure structure = structureStore.getValueAt(pluginType.getBaseType());

            // Set virtual machine structure type
            vmType.setType(structure.getVMStructureIndex());
        }
        else {

            // Basic type.
            // Find closest corresponding Basic4GL type
            switch(pluginType.getBaseType()) {
                case PLUGIN_BASIC4GL_EXT_BYTE:
                case PLUGIN_BASIC4GL_EXT_WORD:
                case PLUGIN_BASIC4GL_EXT_INT:
                case PLUGIN_BASIC4GL_EXT_INT64:
                    vmType.basicType = BasicValType.VTP_INT;
                    break;

                case PLUGIN_BASIC4GL_EXT_FLOAT:
                case PLUGIN_BASIC4GL_EXT_DOUBLE:
                    vmType.basicType = BasicValType.VTP_REAL;
                    break;

                case PLUGIN_BASIC4GL_EXT_STRING:
                    vmType.setType(BasicValType.VTP_STRING);
                    break;
            }
        }

        // Other type properties
        vmType.pointerLevel = pluginType.getPointerLevel();
        vmType.arrayLevel = pluginType.getArrayLevel();

        // Copy array dimensions
        for (int i = 0; i < vmType.arrayLevel; i++) {
            vmType.arrayDimensions[i] = pluginType.getArrayDims()[i];
        }

        return vmType;
    }

    /**
     * Get virtual machine equivalent type from plugin type.
     * (Will replace plugin structure indices with corresponding virtual machine indices).
     * @param src plugin type
     * @return virtual machine equivalent type
     */
    public ValType getVMType(ValType src){

        // Copy value type
        ValType dst = new ValType(src);

        // Check if is a structure type
        if (dst.basicType > 0) {

            // Find corresponding structure
            PluginStructure structure = getStructure(dst.basicType);
            Assert.assertTrue(structure != null);

            // Replace with virtual machine structure index
            dst.basicType = structure.getVMStructureIndex();
        }

        return dst;
    }
}
