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
// compParamTypeList


public class compParamTypeList {
    Vector<ValType> m_params;

    public compParamTypeList () { ; }
    public compParamTypeList (compParamTypeList list){
    	for (ValType val: list.m_params)
    		m_params.add (new ValType(val));
    }
    public compParamTypeList (ValType[] list){
    	m_params = new Vector<ValType>(Arrays.asList(list));
    }
    public compParamTypeList (Integer[] list){
    	m_params = new Vector<ValType>();
    	List<Integer> l = Arrays.asList(list);
    	for (Integer type : l)
    		m_params.add (new ValType(type));
    	
    }
    /*
    public compParamTypeList operator<< (VmValType val) {
        mParams.add (val);
        return this;
    }*/
public void addParam(ValType val) {
    m_params.add (val);
}
    public Vector<ValType> Params() { return m_params; }
}
