package com.basic4gl.library.standard;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.compiler.util.FunctionSpecification;
import com.basic4gl.compiler.util.ParamValidationCallback;
import com.basic4gl.lib.util.FunctionLibrary;
import com.basic4gl.lib.util.IAppSettings;
import com.basic4gl.lib.util.IServiceCollection;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.runtime.types.BasicValType;
import com.basic4gl.runtime.types.ValType;
import com.basic4gl.runtime.util.Function;
import java.util.*;

public class Standard implements FunctionLibrary {

// Constants
public static final int DEF_MAX_CATCHUP_TIME = 150; // .15 seconds
public static final int RAND_MAX = 32767;
public static final float M_PI = 3.1415926535897932384626433832795f;
public static final float M_E = 2.7182818284590452353602874713526f;
public static final float M_RAD2DEG = (180 / M_PI);
public static final float M_DEG2RAD = (M_PI / 180);

// Globals
private static long lastTickCount = 0;
private static int maxCatchupTime = DEF_MAX_CATCHUP_TIME;
private static int catchupTime = maxCatchupTime - 1;
private static Random rnd;
private static Vector<String> mArguments;
private static long performanceFreq;

@Override
public String name() {
	return "Standard";
}

@Override
public String description() {
	return "Standard Basic4GL library; contains math functions";
}

// //////////////////////////////////////////////////////////////////////////////
// Pre-run initialisation
@Override
public void init(TomVM vm, IServiceCollection services, IAppSettings settings, String[] args) {
	/////////////////////
	// Initialise state
	rnd = new Random();

	lastTickCount = 0;
	maxCatchupTime = DEF_MAX_CATCHUP_TIME;
	catchupTime = maxCatchupTime - 1;

	mArguments = new Vector<>();
	if (args != null) {
	mArguments.addAll(Arrays.asList(args));
	}
}

@Override
public void init(TomBasicCompiler comp, IServiceCollection services) {}

@Override
public void cleanup() {
	// Do nothing
}

///////////////////////////////////////////////////////////////////////////////////
// Documentation
@Override
public HashMap<String, String> getTokenTips() {
	// TODO Add documentation
	return null;
}

/**
* Performance counter
* @return
*/
long getPerformanceCounter() {
	if (performanceFreq == 0) // No performance counter?
	{
	return System.currentTimeMillis(); // Degrade to tick counter
	} else {
	return (System.nanoTime() * 1000) / performanceFreq;
	}
}

public static final class WrapAbs implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal(Math.abs(vm.getRealParam(1)));
	}
}

public static final class WrapAsc implements Function {
	public void run(TomVM vm) {
	vm.getReg().setIntVal((int) vm.getStringParam(1).charAt(0));
	}
}

public static final class WrapAtn implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.atan(vm.getRealParam(1)));
	}
}

public static final class WrapChr implements Function {
	public void run(TomVM vm) {
	vm.setRegString(String.valueOf((char) vm.getIntParam(1).intValue()));
	}
}

public static final class WrapCos implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.cos(vm.getRealParam(1)));
	}
}

public static final class WrapExp implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.pow(M_E, vm.getRealParam(1)));
	}
}

public static final class WrapInt implements Function {
	public void run(TomVM vm) {

	float realVal = vm.getRealParam(1);
	int intVal = (int) realVal;
	if (realVal < 0 && realVal != intVal) // Special case, negative
	// numbers
	{
		intVal--;
	}
	vm.getReg().setIntVal(intVal);
	}
}

public static final class WrapLeft implements Function {
	public void run(TomVM vm) {
	String s = vm.getStringParam(2);
	int c = vm.getIntParam(1);
	if (c <= 0) {
		vm.setRegString("");
	} else if (c >= s.length()) {
		vm.setRegString(s);
	} else {
		vm.setRegString(s.substring(0, c));
	}
	}
}

public static final class WrapLen implements Function {
	public void run(TomVM vm) {
	vm.getReg().setIntVal(vm.getStringParam(1).length());
	}
}

public static final class WrapLog implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.log(vm.getRealParam(1)));
	}
}

public static final class WrapMid implements Function {
	public void run(TomVM vm) {
	String s = vm.getStringParam(3);
	int i = vm.getIntParam(2) - 1, c = vm.getIntParam(1);
	if (i < 0) {
		c += i;
		i = 0;
	}
	if (c > 0 && s.length() > 0 && i < s.length()) {
		if (i + c > s.length()) {
		c = s.length() - i;
		}
		vm.setRegString(s.substring(i, i + c));
	} else {
		vm.setRegString("");
	}
	}
}

public static final class WrapLCase implements Function {
	public void run(TomVM vm) {
	vm.setRegString(vm.getStringParam(1).toLowerCase());
	}
}

public static final class WrapUCase implements Function {
	public void run(TomVM vm) {
	vm.setRegString(vm.getStringParam(1).toUpperCase());
	}
}

public static final class WrapPow implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.pow(vm.getRealParam(2), vm.getRealParam(1)));
	}
}

public static final class WrapRight implements Function {

	public void run(TomVM vm) {
	String s = vm.getStringParam(2);
	int c = vm.getIntParam(1);
	if (c <= 0) {
		vm.setRegString("");
	} else if (c >= s.length()) {
		vm.setRegString(s);
	} else {
		vm.setRegString(s.substring(s.length() - c, s.length()));
	}
	}
}

public static final class WrapRnd implements Function {
	public void run(TomVM vm) {
	vm.getReg().setIntVal(rnd.nextInt(RAND_MAX));
	}
}

public static final class WrapRandomize implements Function {
	public void run(TomVM vm) {
	rnd.setSeed(vm.getIntParam(1));
	}
}

public static final class WrapRandomize_2 implements Function {
	public void run(TomVM vm) {
	rnd.setSeed(0);
	}
}

public static final class WrapSgn implements Function {
	public void run(TomVM vm) {
	float i = vm.getRealParam(1);
	if (i < 0) {
		vm.getReg().setIntVal(-1);
	} else if (i == 0) {
		vm.getReg().setIntVal(0);
	} else {
		vm.getReg().setIntVal(1);
	}
	}
}

public static final class WrapSin implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.sin(vm.getRealParam(1)));
	}
}

public static final class WrapSqrt implements Function {
	public void run(TomVM vm) {
	float param = vm.getRealParam(1);
	if (param < 0) {
		vm.getReg().setRealVal((float) Math.sqrt(-param));
	} else {
		vm.getReg().setRealVal((float) Math.sqrt(param));
	}
	}
}

public static final class WrapSqr implements Function {
	public void run(TomVM vm) {
	float param = vm.getRealParam(1);
	if (param < 0) {
		vm.getReg().setRealVal((float) Math.sqrt(-param));
	} else {
		vm.getReg().setRealVal((float) Math.sqrt(param));
	}
	}
}

public static final class WrapStr implements Function {
	public void run(TomVM vm) {
	vm.setRegString(String.valueOf(vm.getRealParam(1)));
	}
}

public static final class WrapTan implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.tan(vm.getRealParam(1)));
	}
}

public static final class WrapTanh implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.tanh(vm.getRealParam(1)));
	}
}

public static final class WrapVal implements Function {
	public void run(TomVM vm) {
	try {
		vm.getReg().setRealVal(Float.valueOf(vm.getStringParam(1)));
	} catch (NumberFormatException e) {
		vm.getReg().setRealVal(0f);
	}
	}
}

public final class WrapInitTimer implements Function {
	public void run(TomVM vm) {
	// lastTickCount = GetTickCount ();
	lastTickCount = getPerformanceCounter();
	}
}

public final class WrapWaitTimer implements Function {
	public void run(TomVM vm) {
	// Fetch and validate delay
	int delay = vm.getIntParam(1);
	if (delay < 0) {
		delay = 0;
	}
	if (delay > 5000) {
		delay = 5000;
	}

	// Find clock tick count to wait for
	lastTickCount += delay;
	// if (GetTickCount () > lastTickCount)
	// lastTickCount = GetTickCount ();
	long tickCount = getPerformanceCounter();
	if (tickCount > lastTickCount) {
		lastTickCount = tickCount;
	}

	// Wait for tick count
	// int dif = lastTickCount - GetTickCount();
	long dif = lastTickCount - tickCount;

	try {
		while (dif > 0) {
		Thread.sleep(dif > 10 ? dif - 10 : 0);
		// dif = lastTickCount - GetTickCount();
		dif = lastTickCount - getPerformanceCounter();
		}
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
}

public static final class WrapSyncTimerCatchup implements Function {
	public void run(TomVM vm) {
	maxCatchupTime = vm.getIntParam(1);
	if (maxCatchupTime < 1) {
		maxCatchupTime = 1;
	}
	}
}

public final class WrapSyncTimer implements Function {
	public void run(TomVM vm) {
	// Fetch and validate delay
	int delay = vm.getIntParam(1);
	if (delay < 0) {
		delay = 0;
	}
	if (delay > 5000) {
		delay = 5000;
	}

	// int tickCount = GetTickCount ();
	long tickCount = getPerformanceCounter();
	long diff = tickCount - lastTickCount;
	if (diff < delay) {
		catchupTime = 0;
		vm.getReg().setIntVal(0);
	} else if (catchupTime >= maxCatchupTime) {
		lastTickCount = tickCount;
		catchupTime = 0;
		vm.getReg().setIntVal(0);
	} else {
		vm.getReg().setIntVal(-1);
		lastTickCount += delay;
		catchupTime += delay;
	}
	}
}

// Sin, cos and tan using degrees
public static final class WrapSinD implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.sin(vm.getRealParam(1) * M_DEG2RAD));
	}
}

public static final class WrapCosD implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.cos(vm.getRealParam(1) * M_DEG2RAD));
	}
}

public static final class WrapTanD implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.tan(vm.getRealParam(1) * M_DEG2RAD));
	}
}

public static final class WrapATanD implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.atan(vm.getRealParam(1)) * M_RAD2DEG);
	}
}

public static final class WrapDivByZero implements Function { // TESTING!!! REMOVE
	// LATER!!!
	public void run(TomVM vm) {
	vm.getReg().setRealVal(vm.getRealParam(1) / 0.0f);
	}
}

public static final class WrapArgCount implements Function {
	public void run(TomVM vm) {
	vm.getReg().setIntVal(mArguments.size());
	}
}

public static final class WrapArg implements Function {
	public void run(TomVM vm) {
	int index = vm.getIntParam(1);
	if (index >= 0 && index < mArguments.size()) {
		vm.setRegString(mArguments.get(index));
	} else {
		vm.setRegString("");
	}
	}
}

public static final class WrapATan2 implements Function {
	public void run(TomVM vm) {
	vm.getReg().setRealVal((float) Math.atan2(vm.getRealParam(2), vm.getRealParam(1)));
	}
}

public static final class WrapATan2D implements Function {
	public void run(TomVM vm) {
	vm.getReg()
		.setRealVal((float) Math.atan2(vm.getRealParam(2), vm.getRealParam(1)) * M_RAD2DEG);
	}
}

public static final class WrapArrayMax implements Function {
	public void run(TomVM vm) {
	// Param 1 is data type, which we can ignore.
	// Param 2 is the array (which will be by reference)
	int dataIndex = vm.getIntParam(2);

	// Arrays are prefixed with their array size.
	// Subtract one to get the maximum accepted value.
	vm.getReg().setIntVal(vm.getData().data().get(dataIndex).getIntVal() - 1);
	}
}

public static final class ValidateArrayMaxParam implements ParamValidationCallback {
	public boolean run(int index, ValType type) {
	// Type must be an array type. Must not be a pointer (but can be a
	// by-reference)
	return type.getVirtualPointerLevel() == 0 && type.arrayLevel > 0;
	}
}

void setProgramArguments(Vector<String> arguments) {
	mArguments = arguments;
}

Vector<String> getProgramArguments() {
	return mArguments;
}

@Override
public Map<String, Constant> constants() {
	Map<String, Constant> c = new HashMap<>();
	// Regular constants
	c.put("true", new Constant(-1));
	c.put("false", new Constant(0));
	c.put("rnd_max", new Constant(RAND_MAX));

	// Mathematics constants
	c.put("m_pi", new Constant(M_PI));
	c.put("m_e", new Constant(M_E));
	return c;
}

@Override
public Map<String, FunctionSpecification[]> specs() {
	Map<String, FunctionSpecification[]> s = new HashMap<>();

	// new FuncSpec(params, isFunction, brackets,	new ValType(returnType),
	//				timeshare, vmIndex, freeTempData, paramValidationCallback)

	// AddFunction(String name, Function func, ParamTypeList params, boolean brackets, boolean
	// isFunction,
	//		ValType returnType, boolean timeshare, boolean freeTempData,
	//		ParamValidationCallback paramValidationCallback)
	///////////////////////
	// Register functions
	s.put(
		"abs",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapAbs.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"asc",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapAsc.class,
			new ParamTypeList(BasicValType.VTP_STRING),
			true,
			true,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"atn",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapAtn.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"chr$",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapChr.class,
			new ParamTypeList(BasicValType.VTP_INT),
			true,
			true,
			BasicValType.VTP_STRING,
			false,
			false,
			null)
		});
	s.put(
		"cos",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapCos.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"exp",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapExp.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"int",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapInt.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"left$",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapLeft.class,
			new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT),
			true,
			true,
			BasicValType.VTP_STRING,
			false,
			false,
			null)
		});
	s.put(
		"len",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapLen.class,
			new ParamTypeList(BasicValType.VTP_STRING),
			true,
			true,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"log",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapLog.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"mid$",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapMid.class,
			new ParamTypeList(
				BasicValType.VTP_STRING, BasicValType.VTP_INT, BasicValType.VTP_INT),
			true,
			true,
			BasicValType.VTP_STRING,
			false,
			false,
			null)
		});
	s.put(
		"pow",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapPow.class,
			new ParamTypeList(BasicValType.VTP_REAL, BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"right$",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapRight.class,
			new ParamTypeList(BasicValType.VTP_STRING, BasicValType.VTP_INT),
			true,
			true,
			BasicValType.VTP_STRING,
			false,
			false,
			null)
		});
	s.put(
		"rnd",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapRnd.class,
			new ParamTypeList(),
			true,
			true,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"sgn",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapSgn.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"sin",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapSin.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"sqrt",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapSqrt.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"sqr",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapSqr.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		}); // sqr = Synonym for sqrt
	s.put(
		"str$",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapStr.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_STRING,
			false,
			false,
			null)
		});
	s.put(
		"tan",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapTan.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"tanh",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapTanh.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"val",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapVal.class,
			new ParamTypeList(BasicValType.VTP_STRING),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"sind",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapSinD.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"cosd",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapCosD.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"tand",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapTanD.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"atand",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapATanD.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"atnd",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapATanD.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"atn2",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapATan2.class,
			new ParamTypeList(BasicValType.VTP_REAL, BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"atn2d",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapATan2D.class,
			new ParamTypeList(BasicValType.VTP_REAL, BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});
	s.put(
		"lcase$",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapLCase.class,
			new ParamTypeList(BasicValType.VTP_STRING),
			true,
			true,
			BasicValType.VTP_STRING,
			false,
			false,
			null)
		});
	s.put(
		"ucase$",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapUCase.class,
			new ParamTypeList(BasicValType.VTP_STRING),
			true,
			true,
			BasicValType.VTP_STRING,
			false,
			false,
			null)
		});
	s.put(
		"randomize",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapRandomize.class,
			new ParamTypeList(BasicValType.VTP_INT),
			true,
			false,
			BasicValType.VTP_INT,
			false,
			false,
			null),
		new FunctionSpecification(
			WrapRandomize_2.class,
			new ParamTypeList(),
			true,
			false,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"divbyzero",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapDivByZero.class,
			new ParamTypeList(BasicValType.VTP_REAL),
			true,
			true,
			BasicValType.VTP_REAL,
			false,
			false,
			null)
		});

	// Timer
	s.put(
		"inittimer",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapInitTimer.class,
			new ParamTypeList(),
			true,
			false,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"waittimer",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapWaitTimer.class,
			new ParamTypeList(BasicValType.VTP_INT),
			true,
			false,
			BasicValType.VTP_INT,
			true,
			false,
			null)
		});
	s.put(
		"synctimer",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapSyncTimer.class,
			new ParamTypeList(BasicValType.VTP_INT),
			true,
			true,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"synctimercatchup",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapSyncTimerCatchup.class,
			new ParamTypeList(BasicValType.VTP_INT),
			true,
			false,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});

	// Program arguments
	s.put(
		"argcount",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapArgCount.class,
			new ParamTypeList(),
			true,
			true,
			BasicValType.VTP_INT,
			false,
			false,
			null)
		});
	s.put(
		"arg",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapArg.class,
			new ParamTypeList(BasicValType.VTP_INT),
			true,
			true,
			BasicValType.VTP_STRING,
			false,
			false,
			null)
		});

	// Array size
	s.put(
		"arraymax",
		new FunctionSpecification[] {
		new FunctionSpecification(
			WrapArrayMax.class,
			new ParamTypeList(BasicValType.VTP_UNDEFINED),
			true,
			true,
			BasicValType.VTP_INT,
			false,
			false,
			new ValidateArrayMaxParam())
		});
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
