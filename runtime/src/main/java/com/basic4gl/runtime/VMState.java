package com.basic4gl.runtime;

/**
 * Used to save virtual machine state.
 * (Debugger uses this to run small sections of code, without interrupting the
 * main program.)
 */
public class VMState {

// Instruction pointer
private int ip;

// Registers
private Value reg;
private Value reg2;
private String regString;
private String reg2String;

// Stacks
private int stackTop;
private int userFuncStackTop;
private int currentUserFrame;

// Top of program
private int codeSize;
private int codeBlockCount;

// Var data
private int stackDataTop;
private int tempDataLock;

// Error state
private boolean error;
private String errorString;

// Other state
private boolean paused;

public int getIp() {
	return ip;
}

public void setIp(int ip) {
	this.ip = ip;
}

public Value getReg() {
	return reg;
}

public void setReg(Value reg) {
	this.reg = reg;
}

public Value getReg2() {
	return reg2;
}

public void setReg2(Value reg2) {
	this.reg2 = reg2;
}

public String getRegString() {
	return regString;
}

public void setRegString(String regString) {
	this.regString = regString;
}

public String getReg2String() {
	return reg2String;
}

public void setReg2String(String reg2String) {
	this.reg2String = reg2String;
}

public int getStackTop() {
	return stackTop;
}

public void setStackTop(int stackTop) {
	this.stackTop = stackTop;
}

public int getUserFuncStackTop() {
	return userFuncStackTop;
}

public void setUserFuncStackTop(int userFuncStackTop) {
	this.userFuncStackTop = userFuncStackTop;
}

public int getCurrentUserFrame() {
	return currentUserFrame;
}

public void setCurrentUserFrame(int currentUserFrame) {
	this.currentUserFrame = currentUserFrame;
}

public int getCodeSize() {
	return codeSize;
}

public void setCodeSize(int codeSize) {
	this.codeSize = codeSize;
}

public int getCodeBlockCount() {
	return codeBlockCount;
}

public void setCodeBlockCount(int codeBlockCount) {
	this.codeBlockCount = codeBlockCount;
}

public int getStackDataTop() {
	return stackDataTop;
}

public void setStackDataTop(int stackDataTop) {
	this.stackDataTop = stackDataTop;
}

public int getTempDataLock() {
	return tempDataLock;
}

public void setTempDataLock(int tempDataLock) {
	this.tempDataLock = tempDataLock;
}

public boolean isError() {
	return error;
}

public void setError(boolean error) {
	this.error = error;
}

public String getErrorString() {
	return errorString;
}

public void setErrorString(String errorString) {
	this.errorString = errorString;
}

public boolean isPaused() {
	return paused;
}

public void setPaused(boolean paused) {
	this.paused = paused;
}
}
