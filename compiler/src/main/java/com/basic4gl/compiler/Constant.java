package com.basic4gl.compiler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.basic4gl.runtime.util.Streamable;
import com.basic4gl.runtime.util.Streaming;
import com.basic4gl.runtime.types.ValType;

// Constant
//
// Recognised constants (e.g. "true", "false")

public class Constant implements Streamable{
	int mBasicType;          // Value type
	int     mIntVal;           // Value
	float	mRealVal;
	String  mStringVal;

	public Constant()
	{ mBasicType = ValType.VTP_STRING;
	mStringVal ="";
	mIntVal =0;
	mRealVal =0f; }
	public Constant(String s)
	{ mBasicType = ValType.VTP_STRING;
	mStringVal =s;
	mIntVal =0;
	mRealVal =0f; }
	public Constant(int i)
	{ mBasicType = ValType.VTP_INT;
	mIntVal =i;
	mStringVal ="";
	mRealVal =0f; }
	public Constant(float r)
	{ mBasicType = ValType.VTP_REAL;
	mRealVal =r;
	mStringVal ="";
	mIntVal =0; }
	public Constant(double r)
	{ 
		mBasicType = ValType.VTP_REAL;
		mRealVal = (float) r;
		mStringVal = "";
		mIntVal = 0; }
	public Constant(Constant c)
	{ mBasicType =c.mBasicType;
	mRealVal =c.mRealVal;
	mIntVal =c.mIntVal;
	mStringVal = c.mStringVal; }

	public int getType(){
		return mBasicType;
	}
	public String ToString() {
		switch(mBasicType) {
		case ValType.VTP_INT:    return String.valueOf(mIntVal);
		case ValType.VTP_REAL:   return String.valueOf(mRealVal);
		case ValType.VTP_STRING: return mStringVal;
		default:    return "???";
		}
	}
	@Override
	public void StreamOut(DataOutputStream stream) throws IOException
	{
			Streaming.WriteLong(stream, mBasicType);

			switch(mBasicType) {
			case ValType.VTP_INT:       Streaming.WriteLong(stream, mIntVal);        break;
			case ValType.VTP_REAL:      Streaming.WriteFloat(stream, mRealVal);      break;
			case ValType.VTP_STRING:    Streaming.WriteString(stream, mStringVal);   break;
			default:
				break;
			}
	}
	@Override
	public boolean StreamIn(DataInputStream stream) throws IOException
	{
			mBasicType = (int)Streaming.ReadLong(stream);
			switch(mBasicType) {
			case ValType.VTP_INT:       mIntVal    = (int) Streaming.ReadLong(stream);     break;
			case ValType.VTP_REAL:      mRealVal   = Streaming.ReadFloat(stream);    break;
			case ValType.VTP_STRING:    mStringVal = Streaming.ReadString(stream);   break;
			default:
				break;
			}
		return true;
	}
}
