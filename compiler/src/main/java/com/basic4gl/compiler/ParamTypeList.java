package com.basic4gl.compiler;

import com.basic4gl.runtime.types.ValType;
import java.util.*;

/**
 * ParamTypeList
 */
public class ParamTypeList {
    private final Vector<ValType> params;

    public ParamTypeList() {
        params = new Vector<>();
    }

    public ParamTypeList(ParamTypeList list) {
        params = new Vector<>();
        for (ValType val : list.params) {
            params.add(new ValType(val));
        }
    }

    public ParamTypeList(ValType... type) {
        params = new Vector<>(Arrays.asList(type));
    }

    public ParamTypeList(Integer... type) {
        params = new Vector<>();
        for (int i = 0; i < type.length; i++) {
            params.add(new ValType(type[i]));
        }
    }

    public void addParam(ValType val) {
        params.add(val);
    }

    public Vector<ValType> getParams() {
        return params;
    }
}
