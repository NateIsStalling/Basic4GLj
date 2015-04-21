package com.basic4gl.util;

import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.util.ParamValidationCallback;
import com.basic4gl.vm.types.ValType;

////////////////////////////////////////////////////////////////////////////////
//FuncSpec
//
//Specifies a function and parameters
public class FuncSpec {
	Class<?> mFunctionClass;	//Canonical name of function wrapper class
	ParamTypeList mParamTypes;
	boolean mBrackets, 		// True if function requires brackets around parameters
			mIsFunction; 	// True if function returns a value
	ValType mReturnType;
	boolean mTimeshare; 	// True if virtual machine should perform a timesharing
							// break immediately after returning
	int mIndex; 			// Index in Virtual Machine's "functions" array
	boolean mFreeTempData; 	// True if function allocates temporary data that
							// should be freed before the next instruction
	ParamValidationCallback mParamValidationCallback;

	public Class<?> getFunctionClass() {return mFunctionClass;}
	public ParamTypeList getParamTypes() {
		return mParamTypes;
	}

	public void setParamTypes(ParamTypeList paramTypes) {
		mParamTypes = paramTypes;
	}

	public boolean hasBrackets() {
		return mBrackets;
	}

	public boolean isFunction() {
		return mIsFunction;
	}

	public void setBrackets(boolean hasBrackets) {
		mBrackets = hasBrackets;
	}

	public void setFunction(boolean isFunction) {
		mIsFunction = isFunction;
	}

	public ValType getReturnType() {
		return mReturnType;
	}

	public void setReturnType(ValType type) {
		mReturnType = type;
	}

	public boolean getTimeshare() {
		return mTimeshare;
	}

	public void setTimeshare(boolean timeshare) {
		mTimeshare = timeshare;
	}

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int index) {
		mIndex = index;
	}

	public boolean getFreeTempData() {
		return mFreeTempData;
	}

	public void setFreeTempData(boolean freeTempData) {
		mFreeTempData = freeTempData;
	}

	public ParamValidationCallback getParamValidationCallback() {
		return mParamValidationCallback;
	}

	public void setParamValidationCallback(
			ParamValidationCallback paramValidationCallback) {
		mParamValidationCallback = paramValidationCallback;
	}

	public FuncSpec() {
		mParamTypes = new ParamTypeList();
		mReturnType = new ValType();
		mParamValidationCallback = null;
	}
	
	public FuncSpec(Class functionClass,
					ParamTypeList paramTypes, boolean brackets,
					boolean isFunction, int returnType, boolean timeshare, boolean freeTempData,
					ParamValidationCallback paramValidationCallback) {
		mFunctionClass = functionClass;
		mParamTypes = paramTypes;
		mBrackets = brackets;
		mIsFunction = isFunction;
		mReturnType = new ValType(returnType);
		mTimeshare = timeshare;
		mFreeTempData = freeTempData;
		mParamValidationCallback = paramValidationCallback;
	}
	public FuncSpec(Class functionClass,
					ParamTypeList paramTypes, boolean brackets,
					boolean isFunction, ValType returnType, boolean timeshare, boolean freeTempData,
					ParamValidationCallback paramValidationCallback) {
		mFunctionClass = functionClass;
		mParamTypes = paramTypes;
		mBrackets = brackets;
		mIsFunction = isFunction;
		mReturnType = returnType;
		mTimeshare = timeshare;
		mFreeTempData = freeTempData;
		mParamValidationCallback = paramValidationCallback;
	}

	public FuncSpec(FuncSpec spec) {
		mIsFunction = spec.mIsFunction;
		mBrackets = spec.mBrackets;
		mReturnType = spec.mReturnType;
		mTimeshare = spec.mTimeshare;
		mIndex = spec.mIndex;
		mFreeTempData = spec.mFreeTempData;
		mParamValidationCallback = spec.mParamValidationCallback;
		mParamTypes = spec.mParamTypes;
	}
}