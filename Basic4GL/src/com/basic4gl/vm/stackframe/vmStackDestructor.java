package com.basic4gl.vm.stackframe;

////////////////////////////////////////////////////////////////////////////////
//vmStackDestructor
//
/// Indicates data in the function or temp stack area that needs to be destroyed
/// when the stack unwinds.
/// Currently our only "destruction" logic is for deallocating strings
/// referenced by the data.
public class vmStackDestructor {

	/// Address of data on stack or in temp space
	public int addr;

	/// Index of data type
	public int dataTypeIndex;

	public vmStackDestructor(int _addr, int _dataTypeIndex)  
	{
		addr = _addr; 
		dataTypeIndex = _dataTypeIndex;
	}
}