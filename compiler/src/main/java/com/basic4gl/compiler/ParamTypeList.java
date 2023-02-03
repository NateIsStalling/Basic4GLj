package com.basic4gl.compiler;

import java.util.*;

import com.basic4gl.runtime.types.ValType;

/**
 * ParamTypeList
 */
public class ParamTypeList {
    private Vector<ValType> mParams;

    public ParamTypeList() { mParams = new Vector<ValType>(); }
    public ParamTypeList(ParamTypeList list){
        for (ValType val: list.mParams) {
            mParams.add (new ValType(val));
        }
    }

    public ParamTypeList(ValType ... type){
        mParams = new Vector<ValType>(Arrays.asList(type));
    }
    public ParamTypeList(Integer ... type){
        mParams = new Vector<ValType>();
        for (int i = 0; i < type.length; i++) {
            mParams.add (new ValType(type[i]));
        }
    }

    public void addParam(ValType val) {
        mParams.add(val);
    }
    public Vector<ValType> getParams() { return mParams; }
}
