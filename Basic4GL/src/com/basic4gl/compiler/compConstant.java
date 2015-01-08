package com.basic4gl.compiler;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.ValType.BasicValType;

// compConstant
//
// Recognised constants (e.g. "true", "false")

public class compConstant {
	BasicValType  mValType;          // Value type
	int     mIntVal;           // Value
	float	mRealVal;
	String  mStringVal;

	public compConstant ()
	{ mValType =BasicValType.VTP_STRING;
	mStringVal ="";
	mIntVal =0;
	mRealVal =0f; }
	public compConstant (String s)
	{ mValType =BasicValType.VTP_STRING;
	mStringVal =s;
	mIntVal =0;
	mRealVal =0f; }
	public compConstant (int i)
	{ mValType =BasicValType.VTP_INT;
	mIntVal =i;
	mStringVal ="";
	mRealVal =0f; }
	public compConstant (float r)
	{ mValType=BasicValType.VTP_REAL;
	mRealVal =r;
	mStringVal ="";
	mIntVal =0; }
	public compConstant (double r)
	{ 
		mValType = BasicValType.VTP_REAL;
		mRealVal = (float) r;
		mStringVal = "";
		mIntVal = 0; }
	public compConstant (compConstant c)
	{ mValType =c.mValType;
	mRealVal =c.mRealVal;
	mIntVal =c.mIntVal;
	mStringVal = c.mStringVal; }

	public String ToString() {
		switch(mValType) {
		case VTP_INT:    return String.valueOf(mIntVal);
		case VTP_REAL:   return String.valueOf(mRealVal);
		case VTP_STRING: return mStringVal;
		default:    return "???";
		}
	}

	void StreamOut(ByteBuffer buffer)
	{
		try {
			Streaming.WriteLong(buffer,  mValType.getType());

			switch(mValType) {
			case VTP_INT:       Streaming.WriteLong(buffer, mIntVal);        break;
			case VTP_REAL:      Streaming.WriteFloat(buffer, mRealVal);      break;
			case VTP_STRING:    Streaming.WriteString(buffer, mStringVal);   break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	void StreamIn(ByteBuffer buffer)
	{
		try {
			mValType = BasicValType.getType((int) Streaming.ReadLong(buffer));
			switch(mValType) {
			case VTP_INT:       mIntVal    = (int) Streaming.ReadLong(buffer);     break;
			case VTP_REAL:      mRealVal   = Streaming.ReadFloat(buffer);    break;
			case VTP_STRING:    mStringVal = Streaming.ReadString(buffer);   break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
