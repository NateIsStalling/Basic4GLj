package com.basic4gl.lib.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nate on 2/15/2015.
 */
public class Configuration implements Serializable {
    public static final int PARAM_HEADING = -1;
    public static final int PARAM_DIVIDER = 0;
    public static final int PARAM_STRING = 1;
    public static final int PARAM_INT = 2;
    public static final int PARAM_BOOL = 3;
    public static final int PARAM_CHOICE = 4;

    private List<String[]> fieldNames = new ArrayList<String[]>();
    private List<Integer> parameterTypes = new ArrayList<Integer>();
    private List<String> values = new ArrayList<String>();

    public Configuration(){}
    public Configuration(Configuration config){
        fieldNames = new ArrayList<String[]>(config.fieldNames.size());
        for(String[] item: config.fieldNames) {
            fieldNames.add(item.clone());
        }
        parameterTypes = new ArrayList<Integer>(config.parameterTypes.size());
        for(Integer item: config.parameterTypes) {
            parameterTypes.add(item);
        }
        values = new ArrayList<String>(config.values.size());
        for(String item: config.values) {
            values.add(item);
        }

    }
    public int getSettingCount(){return fieldNames.size();}
    public void addSetting(String[] field, int param, String val){
        fieldNames.add(field);
        parameterTypes.add(param);
        values.add(val);
    }

    public String[] getField(int index){return fieldNames.get(index);}
    public int getParamType(int index){return parameterTypes.get(index);}
    public String getValue(int index){ return values.get(index);}

    public void setValue(int index, String val){
        values.set(index, val);
    }
}
