package com.basic4gl.compiler.util;

import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.runtime.types.ValType;

/**
 * Specifies a function and parameters
 */
public class FunctionSpecification {
	private Class<?> functionClass;
	private ParamTypeList paramTypes;
	private boolean hasBrackets;
	private boolean isFunction;
	private ValType returnType;
	private boolean timeshare;
	private int index;
	private boolean freeTempData;
	private ParamValidationCallback paramValidationCallback;


	public FunctionSpecification() {
		paramTypes = new ParamTypeList();
		returnType = new ValType();
		paramValidationCallback = null;
	}

	public FunctionSpecification(Class functionClass,
								 ParamTypeList paramTypes, boolean brackets,
								 boolean isFunction, int returnType, boolean timeshare, boolean freeTempData,
								 ParamValidationCallback paramValidationCallback) {
		this.functionClass = functionClass;
		this.paramTypes = paramTypes;
		hasBrackets = brackets;
		this.isFunction = isFunction;
		this.returnType = new ValType(returnType);
		this.timeshare = timeshare;
		this.freeTempData = freeTempData;
		this.paramValidationCallback = paramValidationCallback;
	}
	public FunctionSpecification(Class functionClass,
								 ParamTypeList paramTypes, boolean brackets,
								 boolean isFunction, ValType returnType, boolean timeshare, boolean freeTempData,
								 ParamValidationCallback paramValidationCallback) {
		this.functionClass = functionClass;
		this.paramTypes = paramTypes;
		hasBrackets = brackets;
		this.isFunction = isFunction;
		this.returnType = returnType;
		this.timeshare = timeshare;
		this.freeTempData = freeTempData;
		this.paramValidationCallback = paramValidationCallback;
	}

	public FunctionSpecification(FunctionSpecification spec) {
		functionClass = spec.functionClass;
		isFunction = spec.isFunction;
		hasBrackets = spec.hasBrackets;
		returnType = spec.returnType;
		timeshare = spec.timeshare;
		index = spec.index;
		freeTempData = spec.freeTempData;
		paramValidationCallback = spec.paramValidationCallback;
		paramTypes = spec.paramTypes;
	}

	/**
	 * @return Canonical name of function wrapper class
	 */
	public Class<?> getFunctionClass() {return functionClass;}
	public ParamTypeList getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(ParamTypeList paramTypes) {
		this.paramTypes = paramTypes;
	}

	/**
	 * @return True if function requires brackets around parameters
	 */
	public boolean hasBrackets() {
		return hasBrackets;
	}

	/**
	 * @return True if function returns a value
	 */
	public boolean isFunction() {
		return isFunction;
	}

	public void setBrackets(boolean hasBrackets) {
		this.hasBrackets = hasBrackets;
	}

	/**
	 * @param isFunction True if function returns a value
	 */
	public void setFunction(boolean isFunction) {
		this.isFunction = isFunction;
	}

	public ValType getReturnType() {
		return returnType;
	}

	public void setReturnType(ValType type) {
		returnType = type;
	}

	/**
	 * True if virtual machine should perform a timesharing
	 * break immediately after returning
	 */
	public boolean getTimeshare() {
		return timeshare;
	}

	public void setTimeshare(boolean timeshare) {
		this.timeshare = timeshare;
	}

	/**
	 * @return Index in Virtual Machine's "functions" array
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index Index in Virtual Machine's "functions" array
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return True if function allocates temporary data that
	 * should be freed before the next instruction
	 */
	public boolean getFreeTempData() {
		return freeTempData;
	}

	public void setFreeTempData(boolean freeTempData) {
		this.freeTempData = freeTempData;
	}

	public ParamValidationCallback getParamValidationCallback() {
		return paramValidationCallback;
	}

	public void setParamValidationCallback(
			ParamValidationCallback paramValidationCallback) {
		this.paramValidationCallback = paramValidationCallback;
	}
}