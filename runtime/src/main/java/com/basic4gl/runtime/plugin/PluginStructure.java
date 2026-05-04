package com.basic4gl.runtime.plugin;

import com.basic4gl.runtime.types.TypeLibrary;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Assert;

import java.util.Locale;
import java.util.Vector;

import static com.basic4gl.runtime.plugin.Basic4GLExtendedTypeCode.PLUGIN_BASIC4GL_EXT_PADDING;
import static com.basic4gl.runtime.util.Assert.assertTrue;

public class PluginStructure {

    // Structure definition
    private final String name;
    private final int versionMajor, versionMinor;
    private final Vector<PluginStructureField> fields = new Vector<>();

    // Index of corresponding Basic4GL structure type in compiler/virtual machine.
    // Note: This is valid only while compiling, or while the program is running,
    // as Basic4GL structures are only created from PluginStructures at the
    // beginning of a compilation.
    private int vmStructureIndex;

    // Owning object
    private final Object owner;

    // Handle passed to plugins
    private int handle;

    public PluginStructure() {
        name = "";
        versionMajor = 0;
        versionMinor = 0;
        vmStructureIndex = 0;
        owner = null;
        handle = 0;
    }
    public PluginStructure(Object owner, String name, int versionMajor, int versionMinor) {
        this.owner = owner;
        this.name = name.toLowerCase(Locale.getDefault());
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        vmStructureIndex = 0;
        handle = 0;
    }

    // Structure building
    public void addField(PluginStructureField field) {
        fields.add(field);
    }

    // Member access
    public Object getOwner()      { return owner; }
    public String getName() { return name; }
    public int getVersionMajor() { return versionMajor; }
    public int getVersionMinor() { return versionMinor; }

    // Field access
    public int getFieldCount() { return fields.size(); }
    public PluginStructureField getField(int index) {
        Assert.assertTrue(index >= 0);
        Assert.assertTrue(index < getFieldCount());
        return fields.get(index);
    }

    public int getVMStructureIndex()           { return vmStructureIndex; }
    public void setVMStructureIndex(int value) { vmStructureIndex = value; }
    public int getHandle()                     { return handle; }
    public void setHandle(int value)           { handle = value; }

    public void addToVM(TypeLibrary typeLib, PluginStructureManager manager){

        // Create corresponding structure in type library

        // Create new structure
        vmStructureIndex = typeLib.getStructures().size();
        typeLib.createStruc(name);

        // Add and convert fields
        for (PluginStructureField field: fields) {

            // Find structure data type
            PluginDataType type = field.getDataType();

            // Ignore padding fields
            if (type.getBaseType() != PLUGIN_BASIC4GL_EXT_PADDING) {

                // Calculate vm data type
                ValType vmType = manager.vmTypeFromPluginType(type);

                // Add field
                typeLib.createField(field.getFieldName(), vmType);
            }
        }
    }
}
