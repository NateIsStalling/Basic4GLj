package com.basic4gl.compiler;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.basic4gl.util.Streaming;
import com.basic4gl.vm.types.ValType;

// compConstant
//
// Recognised constants (e.g. "true", "false")

public class compConstant {
	int mBasicType;          // Value type
	int     mIntVal;           // Value
	float	mRealVal;
	String  mStringVal;

	public compConstant ()
	{ mBasicType = ValType.VTP_STRING;
	mStringVal ="";
	mIntVal =0;
	mRealVal =0f; }
	public compConstant (String s)
	{ mBasicType = ValType.VTP_STRING;
	mStringVal =s;
	mIntVal =0;
	mRealVal =0f; }
	public compConstant (int i)
	{ mBasicType = ValType.VTP_INT;
	mIntVal =i;
	mStringVal ="";
	mRealVal =0f; }
	public compConstant (float r)
	{ mBasicType = ValType.VTP_REAL;
	mRealVal =r;
	mStringVal ="";
	mIntVal =0; }
	public compConstant (double r)
	{ 
		mBasicType = ValType.VTP_REAL;
		mRealVal = (float) r;
		mStringVal = "";
		mIntVal = 0; }
	public compConstant (compConstant c)
	{ mBasicType =c.mBasicType;
	mRealVal =c.mRealVal;
	mIntVal =c.mIntVal;
	mStringVal = c.mStringVal; }

	public String ToString() {
		switch(mBasicType) {
		case ValType.VTP_INT:    return String.valueOf(mIntVal);
		case ValType.VTP_REAL:   return String.valueOf(mRealVal);
		case ValType.VTP_STRING: return mStringVal;
		default:    return "???";
		}
	}

	void StreamOut(ByteBuffer buffer)
	{
		try {
			Streaming.WriteLong(buffer, mBasicType);

			switch(mBasicType) {
			case ValType.VTP_INT:       Streaming.WriteLong(buffer, mIntVal);        break;
			case ValType.VTP_REAL:      Streaming.WriteFloat(buffer, mRealVal);      break;
			case ValType.VTP_STRING:    Streaming.WriteString(buffer, mStringVal);   break;
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
			mBasicType = (int)Streaming.ReadLong(buffer);
			switch(mBasicType) {
			case ValType.VTP_INT:       mIntVal    = (int) Streaming.ReadLong(buffer);     break;
			case ValType.VTP_REAL:      mRealVal   = Streaming.ReadFloat(buffer);    break;
			case ValType.VTP_STRING:    mStringVal = Streaming.ReadString(buffer);   break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
