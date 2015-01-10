package com.basic4gl.lib.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import com.basic4gl.compiler.compConstant;
import com.basic4gl.compiler.compParamTypeList;
import com.basic4gl.compiler.util.compParamValidationCallback;
import com.basic4gl.util.compFuncSpec;
import com.basic4gl.lib.util.Library;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.types.ValType;
import com.basic4gl.vm.util.Function;

public class Standard implements Library{

	// Constants
	static final int DEF_MAX_CATCHUP_TIME = 150; // .15 seconds
	static final int RAND_MAX = 32767;
	static final float M_PI = 3.1415926535897932384626433832795f;
	static final float M_E = 2.7182818284590452353602874713526f;
	static final float M_RAD2DEG = (180 / M_PI);
	static final float M_DEG2RAD = (M_PI / 180);

	// extern int PerformanceCounter();

	// Globals
	static long lastTickCount = 0;
	static int maxCatchupTime = DEF_MAX_CATCHUP_TIME;
	static int catchupTime = maxCatchupTime - 1;
	static Random rnd;
	static Vector<String> mArguments;
	static long performanceFreq;


	@Override
	public boolean isTarget() { return false;}	//Library is not a build target
	@Override
	public String name(){return "Standard";}
	@Override
	public String version() {return "1";}
	@Override
	public String description() {return "Standard Basic4GL math library";}
	@Override
	public String author() {return "";}
	@Override
	public String contact() {return "N/A";}
	@Override
	public String id() {return "standard";}
	@Override
	public String[] compat() {return new String[]{"desktopgl"};}

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

	final protected class WrapAbs implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(Math.abs(vm.GetRealParam(1)));
		}
	}

	final class WrapAsc implements Function {
		public void run(TomVM vm) {
			vm.Reg().setIntVal((int) vm.GetStringParam(1).charAt(0));
		}
	}

	final class WrapAtn implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.atan(vm.GetRealParam(1)));
		}
	}

	final class WrapChr implements Function {
		public void run(TomVM vm) {
			vm.setRegString(String.valueOf((char) vm.GetIntParam(1).intValue()));
		}
	}

	final class WrapCos implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.cos(vm.GetRealParam(1)));
		}
	}

	final class WrapExp implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.pow(2.7182818284590452353602874713526f,
							vm.GetRealParam(1)));
		}
	}

	final class WrapInt implements Function {
		public void run(TomVM vm) {

			float realVal = vm.GetRealParam(1);
			int intVal = (int) realVal;
			if (realVal < 0 && realVal != intVal) // Special case, negative
				// numbers
				intVal--;
			vm.Reg().setIntVal(intVal);
		}
	}

	final class WrapLeft implements Function {
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

	final class WrapLen implements Function {
		public void run(TomVM vm) {
			vm.Reg().setIntVal(vm.GetStringParam(1).length());
		}
	}

	final class WrapLog implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.log(vm.GetRealParam(1)));
		}
	}

	final class WrapMid implements Function {
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
				vm.setRegString(s.substring(i, c));
			} else
				vm.setRegString("");
		}
	}

	final class WrapLCase implements Function {
		public void run(TomVM vm) {
			vm.setRegString(vm.GetStringParam(1).toLowerCase());
		}
	}

	final class WrapUCase implements Function {
		public void run(TomVM vm) {
			vm.setRegString(vm.GetStringParam(1).toUpperCase());
		}
	}

	final class WrapPow implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.pow(vm.GetRealParam(2), vm.GetRealParam(1)));
		}
	}

	final class WrapRight implements Function {

		public void run(TomVM vm) {
			String s = vm.GetStringParam(2);
			int c = vm.GetIntParam(1);
			if (c <= 0)
				vm.setRegString("");
			else if (c >= s.length())
				vm.setRegString(s);
			else
				vm.setRegString(s.substring(s.length() - c, c));
		}
	}

	final class WrapRnd implements Function {
		public void run(TomVM vm) {
			vm.Reg().setIntVal(rnd.nextInt(RAND_MAX));
		}
	}

	final class WrapRandomize implements Function {
		public void run(TomVM vm) {
			rnd.setSeed(vm.GetIntParam(1));
		}
	}

	final class WrapRandomize_2 implements Function {
		public void run(TomVM vm) {
			rnd.setSeed(0);
		}
	}

	final class WrapSgn implements Function {
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

	final class WrapSin implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.sin(vm.GetRealParam(1)));
		}
	}

	final class WrapSqrt implements Function {
		public void run(TomVM vm) {
			float param = vm.GetRealParam(1);
			if (param < 0)
				vm.Reg().setRealVal((float) Math.sqrt(-param));
			else
				vm.Reg().setRealVal((float) Math.sqrt(param));
		}
	}

	final class WrapSqr implements Function {
		public void run(TomVM vm) {
			float param = vm.GetRealParam(1);
			if (param < 0)
				vm.Reg().setRealVal((float) Math.sqrt(-param));
			else
				vm.Reg().setRealVal((float) Math.sqrt(param));
		}
	}

	final class WrapStr implements Function {
		public void run(TomVM vm) {
			vm.setRegString(String.valueOf(vm.GetRealParam(1)));
		}
	}

	final class WrapTan implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.tan(vm.GetRealParam(1)));
		}
	}

	final class WrapTanh implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal((float) Math.tanh(vm.GetRealParam(1)));
		}
	}

	final class WrapVal implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(Float.valueOf(vm.GetStringParam(1)));
		}
	}

	final class WrapInitTimer implements Function {
		public void run(TomVM vm) {
			// lastTickCount = GetTickCount ();
			lastTickCount = PerformanceCounter();
		}
	}

	final class WrapWaitTimer implements Function {
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

	final class WrapSyncTimerCatchup implements Function {
		public void run(TomVM vm) {
			maxCatchupTime = vm.GetIntParam(1);
			if (maxCatchupTime < 1)
				maxCatchupTime = 1;
		}
	}

	final class WrapSyncTimer implements Function {
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
	final class WrapSinD implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.sin(vm.GetRealParam(1) * M_DEG2RAD));
		}
	}

	final class WrapCosD implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.cos(vm.GetRealParam(1) * M_DEG2RAD));
		}
	}

	final class WrapTanD implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.tan(vm.GetRealParam(1) * M_DEG2RAD));
		}
	}

	final class WrapATanD implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.atan(vm.GetRealParam(1)) * M_RAD2DEG);
		}
	}

	final class WrapDivByZero implements Function { // TESTING!!! REMOVE
		// LATER!!!
		public void run(TomVM vm) {
			vm.Reg().setRealVal(vm.GetRealParam(1) / 0.0f);
		}
	}

	final class WrapArgCount implements Function {
		public void run(TomVM vm) {
			vm.Reg().setIntVal(mArguments.size());
		}
	}

	final class WrapArg implements Function {
		public void run(TomVM vm) {
			int index = vm.GetIntParam(1);
			if (index >= 0 && index < mArguments.size())
				vm.setRegString(mArguments.get(index));
			else
				vm.setRegString("");
		}
	}

	final class WrapATan2 implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.atan2(vm.GetRealParam(2), vm.GetRealParam(1)));
		}
	}

	final class WrapATan2D implements Function {
		public void run(TomVM vm) {
			vm.Reg().setRealVal(
					(float) Math.atan2(vm.GetRealParam(2), vm.GetRealParam(1))
					* M_RAD2DEG);
		}
	}

	final class WrapArrayMax implements Function {
		public void run(TomVM vm) {
			// Param 1 is data type, which we can ignore.
			// Param 2 is the array (which will be by reference)
			int dataIndex = vm.GetIntParam(2);

			// Arrays are prefixed with their array size.
			// Subtract one to get the maximum accepted value.
			vm.Reg().setIntVal(vm.Data().Data().get(dataIndex).getIntVal() - 1);
		}
	}

	final class ValidateArrayMaxParam implements compParamValidationCallback{
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
	public Map<String, compConstant> constants() {
		Map<String, compConstant> c = new HashMap<String, compConstant>();
		// Regular constants
		c.put("true",	new compConstant(-1));
		c.put("false",	new compConstant(0));
		c.put("rnd_max",new compConstant(RAND_MAX));

		// Mathematics constants
		c.put("m_pi",	new compConstant(M_PI));
		c.put("m_e",	new compConstant(M_E));
		return c;
	}
	@Override
	public Map<String, List<Function>> functions() {
		Map<String, List<Function>> f = new HashMap<String, List<Function>>();

		//Prototypes
		f.put("abs",        new ArrayList<Function>());
		f.put("asc",        new ArrayList<Function>());
		f.put("atn",        new ArrayList<Function>());
		f.put("chr$",       new ArrayList<Function>());
		f.put("cos",        new ArrayList<Function>());
		f.put("exp",        new ArrayList<Function>());
		f.put("int",        new ArrayList<Function>());
		f.put("left$",      new ArrayList<Function>());
		f.put("len",        new ArrayList<Function>());
		f.put("log",        new ArrayList<Function>());
		f.put("mid$",       new ArrayList<Function>());
		f.put("pow",        new ArrayList<Function>());
		f.put("right$",     new ArrayList<Function>());
		f.put("rnd",        new ArrayList<Function>());
		f.put("sgn",        new ArrayList<Function>());
		f.put("sin",        new ArrayList<Function>());
		f.put("sqrt",       new ArrayList<Function>());
		f.put("sqr",        new ArrayList<Function>());      // sqr = Synonym for sqrt
		f.put("str$",       new ArrayList<Function>());
		f.put("tan",        new ArrayList<Function>());
		f.put("tanh",       new ArrayList<Function>());
		f.put("val",        new ArrayList<Function>());
		f.put("sind",       new ArrayList<Function>());
		f.put("cosd",       new ArrayList<Function>());
		f.put("tand",       new ArrayList<Function>());
		f.put("atand",      new ArrayList<Function>());
		f.put("atnd",       new ArrayList<Function>());
		f.put("atn2",       new ArrayList<Function>());
		f.put("atn2d",      new ArrayList<Function>());
		f.put("lcase$",     new ArrayList<Function>());
		f.put("ucase$",     new ArrayList<Function>());
		f.put("randomize",  new ArrayList<Function>());
		f.put("divbyzero",  new ArrayList<Function>());

		// Timer
		f.put("inittimer",  new ArrayList<Function>());
		f.put("waittimer",  new ArrayList<Function>());
		f.put("synctimer",  new ArrayList<Function>());
		f.put("synctimercatchup",  new ArrayList<Function>());

		// Program arguments
		f.put("argcount",    new ArrayList<Function>());
		f.put("arg",         new ArrayList<Function>());

		// Array size
		f.put("arraymax",    new ArrayList<Function>());

		///////////////////////
		// Register functions
		f.get("abs").add(        new WrapAbs());
		f.get("asc").add(        new WrapAsc());
		f.get("atn").add(        new WrapAtn());
		f.get("chr$").add(       new WrapChr());
		f.get("cos").add(        new WrapCos());
		f.get("exp").add(        new WrapExp());
		f.get("int").add(        new WrapInt());
		f.get("left$").add(      new WrapLeft());
		f.get("len").add(        new WrapLen());
		f.get("log").add(        new WrapLog());
		f.get("mid$").add(       new WrapMid());
		f.get("pow").add(        new WrapPow());
		f.get("right$").add(     new WrapRight());
		f.get("rnd").add(        new WrapRnd());
		f.get("sgn").add(        new WrapSgn());
		f.get("sin").add(        new WrapSin());
		f.get("sqrt").add(       new WrapSqrt());
		f.get("sqr").add(        new WrapSqrt());      // sqr = Synonym for sqrt
		f.get("str$").add(       new WrapStr());
		f.get("tan").add(        new WrapTan());
		f.get("tanh").add(       new WrapTanh());
		f.get("val").add(        new WrapVal());
		f.get("sind").add(       new WrapSinD());
		f.get("cosd").add(       new WrapCosD());
		f.get("tand").add(       new WrapTanD());
		f.get("atand").add(      new WrapATanD());
		f.get("atnd").add(       new WrapATanD());
		f.get("atn2").add(       new WrapATan2());
		f.get("atn2d").add(      new WrapATan2D());
		f.get("lcase$").add(     new WrapLCase());
		f.get("ucase$").add(     new WrapUCase());
		f.get("randomize").add(  new WrapRandomize());
		f.get("randomize").add(  new WrapRandomize_2());
		f.get("divbyzero").add(  new WrapDivByZero());

		// Timer
		f.get("inittimer").add(  new WrapInitTimer());
		f.get("waittimer").add(  new WrapWaitTimer());
		f.get("synctimer").add(  new WrapSyncTimer());
		f.get("synctimercatchup").add(  new WrapSyncTimerCatchup());

		// Program arguments
		f.get("argcount").add(    new WrapArgCount());
		f.get("arg").add(         new WrapArg());

		// Array size
		f.get("arraymax").add(    new WrapArrayMax());

		return f;
	}
	@Override
	public Map<String, List<compFuncSpec>> specs() {
		Map<String, List<compFuncSpec>> s = new HashMap<String, List<compFuncSpec>>();

		//Prototypes
		s.put("abs",        new ArrayList<compFuncSpec>());
		s.put("asc",        new ArrayList<compFuncSpec>());
		s.put("atn",        new ArrayList<compFuncSpec>());
		s.put("chr$",       new ArrayList<compFuncSpec>());
		s.put("cos",        new ArrayList<compFuncSpec>());
		s.put("exp",        new ArrayList<compFuncSpec>());
		s.put("int",        new ArrayList<compFuncSpec>());
		s.put("left$",      new ArrayList<compFuncSpec>());
		s.put("len",        new ArrayList<compFuncSpec>());
		s.put("log",        new ArrayList<compFuncSpec>());
		s.put("mid$",       new ArrayList<compFuncSpec>());
		s.put("pow",        new ArrayList<compFuncSpec>());
		s.put("right$",     new ArrayList<compFuncSpec>());
		s.put("rnd",        new ArrayList<compFuncSpec>());
		s.put("sgn",        new ArrayList<compFuncSpec>());
		s.put("sin",        new ArrayList<compFuncSpec>());
		s.put("sqrt",       new ArrayList<compFuncSpec>());
		s.put("sqr",        new ArrayList<compFuncSpec>());      // sqr = Synonym for sqrt
		s.put("str$",       new ArrayList<compFuncSpec>());
		s.put("tan",        new ArrayList<compFuncSpec>());
		s.put("tanh",       new ArrayList<compFuncSpec>());
		s.put("val",        new ArrayList<compFuncSpec>());
		s.put("sind",       new ArrayList<compFuncSpec>());
		s.put("cosd",       new ArrayList<compFuncSpec>());
		s.put("tand",       new ArrayList<compFuncSpec>());
		s.put("atand",      new ArrayList<compFuncSpec>());
		s.put("atnd",       new ArrayList<compFuncSpec>());
		s.put("atn2",       new ArrayList<compFuncSpec>());
		s.put("atn2d",      new ArrayList<compFuncSpec>());
		s.put("lcase$",     new ArrayList<compFuncSpec>());
		s.put("ucase$",     new ArrayList<compFuncSpec>());
		s.put("randomize",  new ArrayList<compFuncSpec>());
		s.put("divbyzero",  new ArrayList<compFuncSpec>());

		// Timer
		s.put("inittimer",  new ArrayList<compFuncSpec>());
		s.put("waittimer",  new ArrayList<compFuncSpec>());
		s.put("synctimer",  new ArrayList<compFuncSpec>());
		s.put("synctimercatchup",  new ArrayList<compFuncSpec>());

		// Program arguments
		s.put("argcount",    new ArrayList<compFuncSpec>());
		s.put("arg",         new ArrayList<compFuncSpec>());

		// Array size
		s.put("arraymax",    new ArrayList<compFuncSpec>());

		//new compFuncSpec(params, isFunction, brackets,	new ValType(returnType), 
		//				timeshare, vmIndex, freeTempData, paramValidationCallback)

		//AddFunction(String name, Function func, compParamTypeList params, boolean brackets, boolean isFunction,
		//		ValType returnType, boolean timeshare, boolean freeTempData,
		//		compParamValidationCallback paramValidationCallback)
		///////////////////////
		// Register functions

		s.get("abs").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("asc").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_STRING }), true, true, ValType.VTP_INT, false, false, null));
		s.get("atn").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("chr$").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_INT }), true, true, ValType.VTP_STRING, false, false, null));
		s.get("cos").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("exp").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("int").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_INT, false, false, null));
		s.get("left$").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_STRING, ValType.VTP_INT }), true, true, ValType.VTP_STRING, false, false, null));
		s.get("len").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_STRING }), true, true, ValType.VTP_INT, false, false, null));
		s.get("log").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("mid$").add( new compFuncSpec(new compParamTypeList(new Integer[] {	ValType.VTP_STRING, ValType.VTP_INT,	ValType.VTP_INT }), true, true, ValType.VTP_STRING, false, false, null));
		s.get("pow").add( new compFuncSpec(new compParamTypeList(new Integer[] {	ValType.VTP_REAL, ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("right$").add( new compFuncSpec(new compParamTypeList(new Integer[] {ValType.VTP_STRING, ValType.VTP_INT }), true, true, ValType.VTP_STRING, false, false, null));
		s.get("rnd").add( new compFuncSpec(new compParamTypeList(), true, true, ValType.VTP_INT, false, false, null));
		s.get("sgn").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_INT, false, false, null));
		s.get("sin").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("sqrt").add( new compFuncSpec(new compParamTypeList( new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("sqr").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null)); // sqr = Synonym for sqrt
		s.get("str$").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true,	ValType.VTP_STRING, false, false, null));
		s.get("tan").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true,	ValType.VTP_REAL, false, false, null));
		s.get("tanh").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("val").add( new compFuncSpec(new compParamTypeList( new Integer[] { ValType.VTP_STRING }), true, true, ValType.VTP_REAL, false, false, null));
		s.get("sind").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true,	ValType.VTP_REAL, false, false, null));
		s.get("cosd").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true,	ValType.VTP_REAL, false, false, null));
		s.get("tand").add( new compFuncSpec(new compParamTypeList(	new Integer[] { ValType.VTP_REAL }), true, true,ValType.VTP_REAL, false, false, null));
		s.get("atand").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_REAL }), true, true,ValType.VTP_REAL, false, false, null));
		s.get("atnd").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_REAL }), true, true,ValType.VTP_REAL, false, false, null));
		s.get("atn2").add( new compFuncSpec(new compParamTypeList(new Integer[] {	ValType.VTP_REAL, ValType.VTP_REAL }), true, true,ValType.VTP_REAL, false, false, null));
		s.get("atn2d").add( new compFuncSpec(new compParamTypeList(new Integer[] {ValType.VTP_REAL, ValType.VTP_REAL }), true, true,ValType.VTP_REAL, false, false, null));
		s.get("lcase$").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_STRING }), true, true, ValType.VTP_STRING, false, false, null));
		s.get("ucase$").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_STRING }), true, true, ValType.VTP_STRING, false, false, null));
		s.get("randomize").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_INT }), false, true, ValType.VTP_INT, false, false, null));
		s.get("randomize").add( new compFuncSpec(new compParamTypeList(), false, true,	ValType.VTP_INT, false, false, null));
		s.get("divbyzero").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_REAL }), true, true, ValType.VTP_REAL, false, false, null));

		// Timer
		s.get("inittimer").add( new compFuncSpec(new compParamTypeList(), false, true,	ValType.VTP_INT, false, false, null));
		s.get("waittimer").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_INT }), false, true, ValType.VTP_INT, true, false, null));
		s.get("synctimer").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_INT }), true, true, ValType.VTP_INT, false, false, null));
		s.get("synctimercatchup").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_INT }), false, true, ValType.VTP_INT, false, false, null));

		// Program arguments
		s.get("argcount").add( new compFuncSpec(new compParamTypeList(), true, true, ValType.VTP_INT, false, false, null));
		s.get("arg").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_INT }), true, true,	ValType.VTP_STRING, false, false, null));

		// Array size
		s.get("arraymax").add( new compFuncSpec(new compParamTypeList(new Integer[] { ValType.VTP_UNDEFINED }), true, true, ValType.VTP_INT, false, false, new ValidateArrayMaxParam()));

		return s;
	}
	@Override
	public List<String> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<String> getDependenciesForClassPath() {
		// TODO Auto-generated method stub
		return null;
	}
}
