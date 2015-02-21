package com.basic4gl.compiler;

import java.util.*;

import com.basic4gl.vm.types.ValType;

////////////////////////////////////////////////////////////////////////////////
//  Compile time parameter validation callback.
//  Called at compile time to validate any parameters whose types are specified
//  as VTP_UNDEFINED.
//  Note: Index is 0 for the leftmost parameter and so on (unlike at run time)
//  Callback should return true if parameter type is valid.


////////////////////////////////////////////////////////////////////////////////
// ParamTypeList


public class ParamTypeList {
    private Vector<ValType> mParams;

    public ParamTypeList() { mParams = new Vector<ValType>(); }
    public ParamTypeList(ParamTypeList list){
        for (ValType val: list.mParams)
            mParams.add (new ValType(val));
    }
    public ParamTypeList(ValType[] list){
        mParams = new Vector<ValType>(Arrays.asList(list));
    }
    public ParamTypeList(Integer[] list){
        mParams = new Vector<ValType>();
        List<Integer> l = Arrays.asList(list);
        for (Integer type : l)
            mParams.add (new ValType(type));

    }
    public void addParam(ValType val) {
        mParams.add(val);
    }
    public Vector<ValType> getParams() { return mParams; }
}
