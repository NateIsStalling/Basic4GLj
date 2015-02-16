package com.basic4gl.lib.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nate on 2/15/2015.
 */
public class Configuration {
    public static final int PARAM_STRING = 0;
    public static final int PARAM_INT = 1;
    public static final int PARAM_BOOL = 2;
    public static final int PARAM_CHOICE = 3;

    private List<String[]> mFields = new ArrayList<String[]>();
    private List<Integer> mParameters = new ArrayList<Integer>();
    private List<String> mValues = new ArrayList<String>();

    public Configuration(){}
    public Configuration(Configuration config){
        mFields = new ArrayList<String[]>(config.mFields.size());
        for(String[] item: config.mFields) mFields.add(item.clone());
        mParameters = new ArrayList<Integer>(config.mParameters.size());
        for(Integer item: config.mParameters) mParameters.add(item);
        mValues = new ArrayList<String>(config.mValues.size());
        for(String item: config.mValues) mValues.add(item);

    }
    public int getSettingCount(){return mFields.size();}
    public void addSetting(String[] field, int param, String val){
        mFields.add(field);
        mParameters.add(param);
        mValues.add(val);
    }

    public String[] getField(int index){return mFields.get(index);}
    public int getParamType(int index){return mParameters.get(index);}
    public String getValue(int index){ return mValues.get(index);}

    public void setValue(int index, String val){
        mValues.set(index, val);
    }
}
