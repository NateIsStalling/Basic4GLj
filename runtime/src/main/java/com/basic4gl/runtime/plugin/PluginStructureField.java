package com.basic4gl.compiler.plugin;

import java.util.Locale;

public class PluginStructureField {
    private String fieldName;
    private PluginDataType dataType;

    // Constructor
    PluginStructureField(String fieldName, PluginDataType dataType) {
        this.fieldName = fieldName.toLowerCase(Locale.getDefault());
        this.dataType = dataType;
    }

    // Copy constructor
    PluginStructureField(PluginStructureField f) {
        fieldName = f.fieldName;
        dataType = f.dataType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public PluginDataType getDataType() {
        return dataType;
    }

    public void setDataType(PluginDataType dataType) {
        this.dataType = dataType;
    }
}
