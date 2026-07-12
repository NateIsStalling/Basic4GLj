package com.basic4gl.language.adapter.util;

import com.basic4gl.desktop.spi.language.TypeDefinition;
import com.basic4gl.language.core.types.BasicValType;
import com.basic4gl.language.core.types.ValType;

public final class LanguageUtil {
    private LanguageUtil() {}

    //    public static VariableDefinition toVariableDefinition() {
    //        return new VariableDefinition()
    //    }
    //
    //
    //    public static VariableDefinition buildVariableDefinition(String ) {
    //
    //    }

    public static TypeDefinition toTypeDefinition(ValType type) {
        String name = getTypeString(type);
        return new TypeDefinition(name, "", "", "");
    }

    public static TypeDefinition toTypeDefinition(int type) {
        String name = getTypeString(type);
        return new TypeDefinition(name, "", "", "");
    }

    public static String getTypeString(ValType type) {
        if (type == null) {
            return "???";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < type.getVirtualPointerLevel(); i++) {
            result.append('&');
        }
        result.append(getTypeString(type.basicType));
        for (int i = 0; i < type.arrayLevel; i++) {
            result.append("()");
        }
        return result.toString();
    }

    public static String getTypeString(int type) {
        switch (type) {
            case BasicValType.VTP_INT:
                return "int";
            case BasicValType.VTP_REAL:
                return "real";
            case BasicValType.VTP_STRING:
                return "string";
            default:
                return "???";
        }
    }
}
