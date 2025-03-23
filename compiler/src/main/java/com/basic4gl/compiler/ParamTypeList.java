package com.basic4gl.compiler;

import com.basic4gl.runtime.types.ValType;
import java.util.*;

/**
 * ParamTypeList
 */
public class ParamTypeList {
    private Vector<ValType> mParams;

    public ParamTypeList() {
        mParams = new Vector<>();
    }

    public ParamTypeList(ParamTypeList list) {
        mParams = new Vector<>();
        for (ValType val : list.mParams) {
            mParams.add(new ValType(val));
        }
    }

    public ParamTypeList(ValType... type) {
        mParams = new Vector<>(Arrays.asList(type));
    }

    public ParamTypeList(Integer... type) {
        mParams = new Vector<>();
        for (int i = 0; i < type.length; i++) {
            mParams.add(new ValType(type[i]));
        }
    }

    public void addParam(ValType val) {
        mParams.add(val);
    }

    public Vector<ValType> getParams() {
        return mParams;
    }
}
