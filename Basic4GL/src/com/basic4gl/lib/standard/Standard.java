package com.basic4gl.lib.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.ParamValidationCallback;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.lib.util.Library;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.types.ValType;
import com.basic4gl.vm.util.Function;

public class Standard implements FunctionLibrary {

	// Constants
	public static final int DEF_MAX_CATCHUP_TIME = 150; // .15 seconds
	public static final int RAND_MAX = 32767;
	public static final float M_PI = 3.1415926535897932384626433832795f;
	public static final float M_E = 2.7182818284590452353602874713526f;
	public static final float M_RAD2DEG = (180 / M_PI);
	public static final float M_DEG2RAD = (M_PI / 180);

	// extern int PerformanceCounter();

	// Globals
	static long lastTickCount = 0;
	static int maxCatchupTime = DEF_MAX_CATCHUP_TIME;
	static int catchupTime = maxCatchupTime - 1;
	static Random rnd;
	static Vector<String> mArguments;
	static long performanceFreq;

	@Override
	public String name(){return "Standard";}
	@Override
	public String description() {return "Standard Basic4GL library; contains math functions";}

	// //////////////////////////////////////////////////////////////////////////////
	// Pre-run initialisation
	@Override
	public void init(TomVM vm) {
		/////////////////////
		// Initialise state
		rnd = new Random();

		lastTickCount = 0;
		maxCatchupTime = DEF_MAX_CATCHUP_TIME;
		catchupTime = maxCatchupTime - 1;
	}
	@Override
	public void init(TomBasicCompiler comp){

	}
	///////////////////////////////////////////////////////////////////////////////////
	//Documentation
	@Override
	public HashMap<String, String> getTokenTips() {
		// TODO Add documentation
		return null;
	}
	// //////////////////////////////////////////////////////////////////////////////
	// Performance counter
	long PerformanceCounter() {
		if (performanceFreq == 0) // No performance counter?
			return System.currentTimeMillis(); // Degrade to tick counter
		else {
			return (System.nanoTime() * 1000) / performanceFreq;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////
	// Function wrappers

	public final class WrapAbs implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(Math.abs(vm.GetRealParam(1)));
		}
	}

	public final class WrapAsc implements Function {
		public void run(TomVM vm) {
			vm.Reg().setIntVal((int) vm.GetStringParam(1).charAt(0));
		}
	}

	public final class WrapAtn implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.atan(vm.GetRealParam(1)));
		}
	}

	public final class WrapChr implements Function {
		public void run(TomVM vm) {
			vm.setRegString(String.valueOf((char) vm.GetIntParam(1).intValue()));
		}
	}

	public final class WrapCos implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.cos(vm.GetRealParam(1)));
		}
	}

	public final class WrapExp implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.pow(2.7182818284590452353602874713526f,
							vm.GetRealParam(1)));
		}
	}

	public final class WrapInt implements Function {
		public void run(TomVM vm) {

			float realVal = vm.GetRealParam(1);
			int intVal = (int) realVal;
			if (realVal < 0 && realVal != intVal) // Special case, negative
				// numbers
				intVal--;
			vm.Reg().setIntVal(intVal);
		}
	}

	public final class WrapLeft implements Function {
		public void run(TomVM vm) {
			String s = vm.GetStringParam(2);
			int c = vm.GetIntParam(1);
			if (c <= 0)
				vm.setRegString("");
			else if (c >= s.length())
				vm.setRegString(s);
			else
				vm.setRegString(s.substring(0, c));
		}
	}

	public final class WrapLen implements Function {
		public void run(TomVM vm) {
			vm.Reg().setIntVal(vm.GetStringParam(1).length());
		}
	}

	public final class WrapLog implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.log(vm.GetRealParam(1)));
		}
	}

	public final class WrapMid implements Function {
		public void run(TomVM vm) {
			String s = vm.GetStringParam(3);
			int i = vm.GetIntParam(2) - 1, c = vm.GetIntParam(1);
			if (i < 0) {
				c += i;
				i = 0;
			}
			if (c > 0 && s.length() > 0 && i < s.length()) {
				if (i + c > s.length())
					c = s.length() - i;
				vm.setRegString(s.substring(i, i + c));
			} else
				vm.setRegString("");
		}
	}

	public final class WrapLCase implements Function {
		public void run(TomVM vm) {
			vm.setRegString(vm.GetStringParam(1).toLowerCase());
		}
	}

	public final class WrapUCase implements Function {
		public void run(TomVM vm) {
			vm.setRegString(vm.GetStringParam(1).toUpperCase());
		}
	}

	public final class WrapPow implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.pow(vm.GetRealParam(2), vm.GetRealParam(1)));
		}
	}

	public final class WrapRight implements Function {

		public void run(TomVM vm) {
			String s = vm.GetStringParam(2);
			int c = vm.GetIntParam(1);
			if (c <= 0)
				vm.setRegString("");
			else if (c >= s.length())
				vm.setRegString(s);
			else
				vm.setRegString(s.substring(s.length() - c, s.length()));
		}
	}

	public final class WrapRnd implements Function {
		public void run(TomVM vm) {
			vm.Reg().setIntVal(rnd.nextInt(RAND_MAX));
		}
	}

	public final class WrapRandomize implements Function {
		public void run(TomVM vm) {
			rnd.setSeed(vm.GetIntParam(1));
		}
	}

	public final class WrapRandomize_2 implements Function {
		public void run(TomVM vm) {
			rnd.setSeed(0);
		}
	}

	public final class WrapSgn implements Function {
		public void run(TomVM vm) {
			float i = vm.GetRealParam(1);
			if (i < 0)
				vm.Reg().setIntVal(-1);
			else if (i == 0)
				vm.Reg().setIntVal(0);
			else
				vm.Reg().setIntVal(1);
		}
	}

	public final class WrapSin implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.sin(vm.GetRealParam(1)));
		}
	}

	public final class WrapSqrt implements Function {
		public void run(TomVM vm) {
			float param = vm.GetRealParam(1);
			if (param < 0)
				vm.Reg().setRealVal((float) Math.sqrt(-param));
			else
				vm.Reg().setRealVal((float) Math.sqrt(param));
		}
	}

	public final class WrapSqr implements Function {
		public void run(TomVM vm) {
			float param = vm.GetRealParam(1);
			if (param < 0)
				vm.Reg().setRealVal((float) Math.sqrt(-param));
			else
				vm.Reg().setRealVal((float) Math.sqrt(param));
		}
	}

	public final class WrapStr implements Function {
		public void run(TomVM vm) {
			vm.setRegString(String.valueOf(vm.GetRealParam(1)));
		}
	}

	public final class WrapTan implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.tan(vm.GetRealParam(1)));
		}
	}

	public final class WrapTanh implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.tanh(vm.GetRealParam(1)));
		}
	}

	public final class WrapVal implements Function {
		public void run(TomVM vm) {
			try {
				vm.Reg().setRealVal(Float.valueOf(vm.GetStringParam(1)));
			} catch(NumberFormatException e) {
				vm.Reg().setRealVal(0f);
			}
		}
	}

	public final class WrapInitTimer implements Function {
		public void run(TomVM vm) {
			// lastTickCount = GetTickCount ();
			lastTickCount = PerformanceCounter();
		}
	}

	public final class WrapWaitTimer implements Function {
		public void run(TomVM vm) {
			// Fetch and validate delay
			int delay = vm.GetIntParam(1);
			if (delay < 0)
				delay = 0;
			if (delay > 5000)
				delay = 5000;

			// Find clock tick count to wait for
			lastTickCount += delay;
			// if (GetTickCount () > lastTickCount)
			// lastTickCount = GetTickCount ();
			long tickCount = PerformanceCounter();
			if (tickCount > lastTickCount)
				lastTickCount = tickCount;

			// Wait for tick count
			// int dif = lastTickCount - GetTickCount();
			long dif = lastTickCount - tickCount;

			try {
				while (dif > 0) {
					Thread.sleep(dif > 10 ? dif - 10 : 0);
					// dif = lastTickCount - GetTickCount();
					dif = lastTickCount - PerformanceCounter();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public final class WrapSyncTimerCatchup implements Function {
		public void run(TomVM vm) {
			maxCatchupTime = vm.GetIntParam(1);
			if (maxCatchupTime < 1)
				maxCatchupTime = 1;
		}
	}

	public final class WrapSyncTimer implements Function {
		public void run(TomVM vm) {
			// Fetch and validate delay
			int delay = vm.GetIntParam(1);
			if (delay < 0)
				delay = 0;
			if (delay > 5000)
				delay = 5000;

			// int tickCount = GetTickCount ();
			long tickCount = PerformanceCounter();
			long diff = tickCount - lastTickCount;
			if (diff < delay) {
				catchupTime = 0;
				vm.Reg().setIntVal(0);
			} else if (catchupTime >= maxCatchupTime) {
				lastTickCount = tickCount;
				catchupTime = 0;
				vm.Reg().setIntVal(0);
			} else {
				vm.Reg().setIntVal(-1);
				lastTickCount += delay;
				catchupTime += delay;
			}
		}
	}

	// Sin, cos and tan using degrees
	public final class WrapSinD implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.sin(vm.GetRealParam(1) * M_DEG2RAD));
		}
	}

	public final class WrapCosD implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.cos(vm.GetRealParam(1) * M_DEG2RAD));
		}
	}

	public final class WrapTanD implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.tan(vm.GetRealParam(1) * M_DEG2RAD));
		}
	}

	public final class WrapATanD implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.atan(vm.GetRealParam(1)) * M_RAD2DEG);
		}
	}

	public final class WrapDivByZero implements Function { // TESTING!!! REMOVE
		// LATER!!!
		public void run(TomVM vm) {
			vm.Reg().setRealVal(vm.GetRealParam(1) / 0.0f);
		}
	}

	public final class WrapArgCount implements Function {
		public void run(TomVM vm) {
			vm.Reg().setIntVal(mArguments.size());
		}
	}

	public final class WrapArg implements Function {
		public void run(TomVM vm) {
			int index = vm.GetIntParam(1);
			if (index >= 0 && index < mArguments.size())
				vm.setRegString(mArguments.get(index));
			else
				vm.setRegString("");
		}
	}

	public final class WrapATan2 implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.atan2(vm.GetRealParam(2), vm.GetRealParam(1)));
		}
	}

	public final class WrapATan2D implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.atan2(vm.GetRealParam(2), vm.GetRealParam(1))
					* M_RAD2DEG);
		}
	}

	public final class WrapArrayMax implements Function {
		public void run(TomVM vm) {
			// Param 1 is data type, which we can ignore.
			// Param 2 is the array (which will be by reference)
			int dataIndex = vm.GetIntParam(2);

			// Arrays are prefixed with their array size.
			// Subtract one to get the maximum accepted value.
			vm.Reg().setIntVal(vm.Data().Data().get(dataIndex).getIntVal() - 1);
		}
	}

	public final class ValidateArrayMaxParam implements ParamValidationCallback {
		public boolean run (int index, ValType type){
			// Type must be an array type. Must not be a pointer (but can be a
			// by-reference)
			return type.VirtualPointerLevel() == 0 && type.m_arrayLevel > 0;
		}
	}



	void SetProgramArguments(Vector<String> arguments) {
		mArguments = arguments;
	}

	Vector<String> GetProgramArguments() {
		return mArguments;
	}
	@Override
	public Map<String, Constant> constants() {
		Map<String, Constant> c = new HashMap<String, Constant>();
		// Regular constants
		c.put("true",	new Constant(-1));
		c.put("false",	new Constant(0));
		c.put("rnd_max",new Constant(RAND_MAX));

		// Mathematics constants
		c.put("m_pi",	new Constant(M_PI));
		c.put("m_e",	new Constant(M_E));
		return c;
	}
	@Override
	public Map<String, FuncSpec[]> specs() {
		Map<String, FuncSpec[]> s = new HashMap<String, FuncSpec[]>();

		//new FuncSpec(params, isFunction, brackets,	new ValType(returnType),
		//				timeshare, vmIndex, freeTempData, paramValidationCallback)

		//AddFunction(String name, Function func, ParamTypeList params, boolean brackets, boolean isFunction,
		//		ValType returnType, boolean timeshare, boolean freeTempData,
		//		ParamValidationCallback paramValidationCallback)
		///////////////////////
		// Register functions
		s.put ("abs", new FuncSpec[]{ new FuncSpec(  WrapAbs.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("asc", new FuncSpec[]{ new FuncSpec(  WrapAsc.class, new ParamTypeList ( ValType.VTP_STRING), true, true, ValType.VTP_INT, false, false, null)});
		s.put ("atn", new FuncSpec[]{ new FuncSpec(  WrapAtn.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("chr$", new FuncSpec[]{ new FuncSpec(  WrapChr.class, new ParamTypeList ( ValType.VTP_INT), true, true, ValType.VTP_STRING, false, false, null)});
		s.put ("cos", new FuncSpec[]{ new FuncSpec(  WrapCos.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("exp", new FuncSpec[]{ new FuncSpec(  WrapExp.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("int", new FuncSpec[]{ new FuncSpec(  WrapInt.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_INT, false, false, null)});
		s.put ("left$", new FuncSpec[]{ new FuncSpec(  WrapLeft.class, new ParamTypeList ( ValType.VTP_STRING,  ValType.VTP_INT), true, true, ValType.VTP_STRING, false, false, null)});
		s.put ("len", new FuncSpec[]{ new FuncSpec(  WrapLen.class, new ParamTypeList ( ValType.VTP_STRING), true, true, ValType.VTP_INT, false, false, null)});
		s.put ("log", new FuncSpec[]{ new FuncSpec(  WrapLog.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("mid$", new FuncSpec[]{ new FuncSpec(  WrapMid.class, new ParamTypeList ( ValType.VTP_STRING,  ValType.VTP_INT,  ValType.VTP_INT), true, true, ValType.VTP_STRING, false, false, null)});
		s.put ("pow", new FuncSpec[]{ new FuncSpec(  WrapPow.class, new ParamTypeList ( ValType.VTP_REAL,  ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("right$", new FuncSpec[]{ new FuncSpec(  WrapRight.class, new ParamTypeList ( ValType.VTP_STRING,  ValType.VTP_INT), true, true, ValType.VTP_STRING, false, false, null)});
		s.put ("rnd", new FuncSpec[]{ new FuncSpec(  WrapRnd.class, new ParamTypeList (), true, true, ValType.VTP_INT, false, false, null)});
		s.put ("sgn", new FuncSpec[]{ new FuncSpec(  WrapSgn.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_INT, false, false, null)});
		s.put ("sin", new FuncSpec[]{ new FuncSpec(  WrapSin.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("sqrt", new FuncSpec[]{ new FuncSpec(  WrapSqrt.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("sqr", new FuncSpec[]{ new FuncSpec(  WrapSqr.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)}); // sqr = Synonym for sqrt
		s.put ("str$", new FuncSpec[]{ new FuncSpec(  WrapStr.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_STRING, false, false, null)});
		s.put ("tan", new FuncSpec[]{ new FuncSpec(  WrapTan.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("tanh", new FuncSpec[]{ new FuncSpec(  WrapTanh.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("val", new FuncSpec[]{ new FuncSpec(  WrapVal.class, new ParamTypeList ( ValType.VTP_STRING), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("sind", new FuncSpec[]{ new FuncSpec(  WrapSinD.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("cosd", new FuncSpec[]{ new FuncSpec(  WrapCosD.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("tand", new FuncSpec[]{ new FuncSpec(  WrapTanD.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("atand", new FuncSpec[]{ new FuncSpec(  WrapATanD.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("atnd", new FuncSpec[]{ new FuncSpec(  WrapATanD.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("atn2", new FuncSpec[]{ new FuncSpec(  WrapATan2.class, new ParamTypeList ( ValType.VTP_REAL,  ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("atn2d", new FuncSpec[]{ new FuncSpec(  WrapATan2D.class, new ParamTypeList ( ValType.VTP_REAL,  ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});
		s.put ("lcase$", new FuncSpec[]{ new FuncSpec(  WrapLCase.class, new ParamTypeList ( ValType.VTP_STRING), true, true, ValType.VTP_STRING, false, false, null)});
		s.put ("ucase$", new FuncSpec[]{ new FuncSpec(  WrapUCase.class, new ParamTypeList ( ValType.VTP_STRING), true, true, ValType.VTP_STRING, false, false, null)});
		s.put ("randomize", new FuncSpec[]{
				new FuncSpec(  WrapRandomize.class, new ParamTypeList ( ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null),
				new FuncSpec(  WrapRandomize_2.class, new ParamTypeList (), true, false, ValType.VTP_INT, false, false, null)});
		s.put ("divbyzero", new FuncSpec[]{ new FuncSpec(  WrapDivByZero.class, new ParamTypeList ( ValType.VTP_REAL), true, true, ValType.VTP_REAL, false, false, null)});

		// Timer
		s.put ("inittimer", new FuncSpec[]{ new FuncSpec(  WrapInitTimer.class, new ParamTypeList (), true, false, ValType.VTP_INT, false, false, null)});
		s.put ("waittimer", new FuncSpec[]{ new FuncSpec(  WrapWaitTimer.class, new ParamTypeList ( ValType.VTP_INT), true, false, ValType.VTP_INT, true, false, null)});
		s.put ("synctimer", new FuncSpec[]{ new FuncSpec(  WrapSyncTimer.class, new ParamTypeList ( ValType.VTP_INT), true, true, ValType.VTP_INT, false, false, null)});
		s.put ("synctimercatchup", new FuncSpec[]{ new FuncSpec(  WrapSyncTimerCatchup.class, new ParamTypeList ( ValType.VTP_INT), true, false, ValType.VTP_INT, false, false, null)});

		// Program arguments
		s.put("argcount", new FuncSpec[]{ new FuncSpec(  WrapArgCount.class, new ParamTypeList(), true, true, ValType.VTP_INT, false, false, null)});
		s.put("arg", new FuncSpec[]{ new FuncSpec(  WrapArg.class, new ParamTypeList( ValType.VTP_INT), true, true, ValType.VTP_STRING, false, false, null)});

		// Array size
		s.put("arraymax", new FuncSpec[]{ new FuncSpec(  WrapArrayMax.class, new ParamTypeList( ValType.VTP_UNDEFINED), true, true, ValType.VTP_INT, false, false, new ValidateArrayMaxParam())});
		return s;
	}
	@Override
	public List<String> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getClassPathObjects() {
		return null;
	}

}
