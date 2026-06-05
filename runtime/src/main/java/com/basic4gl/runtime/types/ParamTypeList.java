package com.basic4gl.runtime.types;

import java.util.*;

/**
 * ParamTypeList
 */
public class ParamTypeList {
    private final ArrayList<ValType> params;

    public ParamTypeList() {
        params = new ArrayList<>();
    }

    public ParamTypeList(ParamTypeList list) {
        params = new ArrayList<>();
        for (ValType val : list.params) {
            params.add(new ValType(val));
        }
    }

    public ParamTypeList(ValType... type) {
        params = new ArrayList<>(Arrays.asList(type));
    }

    public ParamTypeList(Integer... type) {
        params = new ArrayList<>();
        for (int i = 0; i < type.length; i++) {
            params.add(new ValType(type[i]));
        }
    }

    public void addParam(ValType val) {
        params.add(val);
    }

    public ArrayList<ValType> getParams() {
        return params;
    }
}
