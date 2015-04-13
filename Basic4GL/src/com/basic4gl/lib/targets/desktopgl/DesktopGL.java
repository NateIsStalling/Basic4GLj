package com.basic4gl.lib.targets.desktopgl;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.*;
import com.basic4gl.util.Exporter;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.types.ValType;
import com.basic4gl.vm.util.Function;

public class DesktopGL implements Target{

	//Libraries
	private java.util.List<Library> mLibraries;
	private static DesktopGL instance;

	private TomBasicCompiler mComp;
	private TomVM mVM;
	private VmWorker mWorker;

	private Frame mFrame;
	private GLCanvas mCanvas;

	private TaskCallback mCallbacks;
	private CallbackMessage mMessage;
	private boolean mClosing;

	private Configuration mConfiguration;

	private static final int SETTING_TITLE 				= 1; //Index of window title setting in config
	private static final int SETTING_WIDTH 				= 2; //Index of window width setting in config
	private static final int SETTING_HEIGHT 			= 3; //Index of window height setting in config
	private static final int SETTING_RESIZABLE 			= 4; //Index of window resizable setting in config
	private static final int SETTING_SCREEN_MODE		= 5; //Index of screen mode setting in config
	private static final int SETTING_SUPPORT_WINDOWS	= 8; //Index of Windows support setting in config
	private static final int SETTING_SUPPORT_MAC		= 9; //Index of Mac support setting in config
	private static final int SETTING_SUPPORT_LINUX		= 10; //Index of Linux support setting in config
	private static final int SETTING_SUPPORT_SOLARIS	= 11; //Index of Solaris support setting in config

	private static final int SUPPORT_WINDOWS_32_64		= 0;
	private static final int SUPPORT_WINDOWS_32			= 1;
	private static final int SUPPORT_WINDOWS_64			= 2;
	private static final int SUPPORT_WINDOWS_NO			= 3;

	private static final int SUPPORT_MAC_32_64			= 0;
	private static final int SUPPORT_MAC_NO				= 1;

	private static final int SUPPORT_LINUX_32_64		= 0;
	private static final int SUPPORT_LINUX_32			= 1;
	private static final int SUPPORT_LINUX_64			= 2;
	private static final int SUPPORT_LINUX_ARMV6		= 3;
	private static final int SUPPORT_LINUX_ARMV6HF		= 4;

	private static final int SUPPORT_SOLARIS_32_64		= 0;
	private static final int SUPPORT_SOLARIS_32			= 1;
	private static final int SUPPORT_SOLARIS_64			= 2;
	private static final int SUPPORT_SOLARIS_NO			= 3;

	private static final int MODE_WINDOWED = 0;
	private static final int MODE_FULLSCREEN = 1;

	private static final String CONFIG_FILE			= "config.ser";	//Filename for configuration file
	private static final String STATE_FILE			= "state.bin";	//Filename for stored VM state

	public static void main(String[] args) {
		//Debug only, makes it easier to attach a remote debugger
		//JOptionPane.showMessageDialog(null, "Waiting...");

		instance = new DesktopGL(new TomBasicCompiler(new TomVM(null)));
		instance.mLibraries = new ArrayList<Library>();
		//TODO Load libraries dynamically
		//TODO Save/Load list of libraries in order they should be added
		instance.mLibraries.add(new com.basic4gl.lib.standard.Standard());
		// Register library functions
		for (Library lib : instance.mLibraries) {
			instance.mComp.AddConstants(lib.constants());
			instance.mComp.AddFunctions(lib.functions(), lib.specs());
		}
		// Register DesktopGL's functions
		instance.mComp.AddConstants(instance.constants());
		instance.mComp.AddFunctions(instance.functions(), instance.specs());

		//Load VM's state from file
		try {
			instance.loadState(instance.getClass().getResourceAsStream("/" + STATE_FILE));
		} catch (Exception ex){
			ex.printStackTrace();
			System.err.println("VM state could not be loaded");
		}
		//Load window configuration
		try {
			instance.loadConfiguration(instance.getClass().getResourceAsStream("/" + CONFIG_FILE));
		} catch (Exception ex){
			ex.printStackTrace();
			System.err.println("Configuration file could not be loaded");
		}

		//Initialize window and setup VM
		instance.activate();
		instance.reset();
		instance.show(null);
	}
	public DesktopGL(TomBasicCompiler compiler){
		mComp = compiler;
		mVM = mComp.VM();
	}
	
	@Override
	public String getFileDescription() { return "Java Application (*.jar)";}

	@Override
	public String getFileExtension() { return "jar";}

	@Override
	public boolean isTarget() { return true;}	//Library is a build target

	@Override
	public  boolean isRunnable() { return true;} //Build target can be run as an application

	@Override
	public String name() { return "OpenGL Window";}

	@Override
	public String version() { return "0.1";}

	@Override
	public String description() { return "Desktop application with OpenGL capabilities.";}

	@Override
	public String author() { return "Nathaniel Nielsen";}

	@Override
	public String contact() { return "support@crazynatestudios.com";}

	@Override
	public String id() { return "desktopgl";}

	@Override
	public String[] compat() { return null;}

	@Override
	public void init(TomVM vm) {
		// TODO Auto-generated method stub

	}

	@Override
	public HashMap<String, String> getTokenTips() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, Constant> constants() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<Function>> functions() {
		//TODO move functions to separate library
		Map<String, List<Function>> f = new HashMap<String, List<Function>>();
		f.put("print", new ArrayList<Function>());
		f.get("print").add(new WrapPrint());
		return f;
	}

	@Override
	public Map<String, List<FuncSpec>> specs() {
		//TODO move functions to separate library
		Map<String, List<FuncSpec>> s = new HashMap<String, List<FuncSpec>>();
		s.put("print", new ArrayList<FuncSpec>());
		s.get("print").add( new FuncSpec(new ParamTypeList(	new Integer[] { ValType.VTP_STRING }), false, false, ValType.VTP_INT, false, false, null));
		return s;
	}
	@Override
	public boolean isVisible() {
		return mFrame == null ? false : mFrame.isVisible();
	}
	
	@Override
	public void reset() {
		if (mWorker != null)
			mWorker.cancel(true);
		mWorker = new VmWorker();
	}

	public void pause(){
		mVM.Pause();
	}


	@Override
	public void activate() {
		mClosing = false;

		//Get settings
		if (mConfiguration == null)
			mConfiguration = getSettings();
		//TODO load config from file

		String title = mConfiguration.getValue(SETTING_TITLE);
		int width = Integer.valueOf(mConfiguration.getValue(SETTING_WIDTH));
		int height = Integer.valueOf(mConfiguration.getValue(SETTING_HEIGHT));
		boolean resizable = Boolean.valueOf(mConfiguration.getValue(SETTING_RESIZABLE));
		int mode = Integer.valueOf(mConfiguration.getValue(SETTING_SCREEN_MODE));


		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		GLCanvas mCanvas = new GLCanvas(caps);

		// m_glWin = null;
		// m_glText = null;

		// Default settings
		// boolean fullScreen = false, border = true;
		// int width = 640, height = 480, bpp = 0;
		// ResetGLModeType resetGLMode = RGM_RESETSTATE;

		// Create window
		/*
		 * m_glWin = new glTextGridWindow ( fullScreen, border, width, height,
		 * bpp, "Basic4GL", resetGLMode);
		 *
		 * // Check for errors if (m_glWin.Error ()) { MessageDlg ( (AnsiString)
		 * m_glWin.GetError().c_str(), mtError, TMsgDlgButtons() << mbOK, 0);
		 * Application.Terminate (); return; } m_glWin.Hide ();
		 *
		 * // Create OpenGL text grid m_glText = new glSpriteEngine (
		 * (ExtractFilePath (Application.ExeName) + "charset.png").c_str (),
		 * &m_files, 25, 40, 16, 16);
		 *
		 * // Check for errors if (m_glText.Error ()) { MessageDlg (
		 * (AnsiString) + m_glText.GetError ().c_str (), mtError,
		 * TMsgDlgButtons() << mbOK, 0); Application.Terminate (); return; }
		 * m_glWin.SetTextGrid (m_glText);
		 */

		mFrame = new Frame(title);
		mFrame.setSize(width, height);
		mFrame.setResizable(resizable);
		if (mode == MODE_FULLSCREEN) {
			mFrame.setUndecorated(true);
			mFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			mFrame.setUndecorated(false);
			mFrame.setExtendedState(JFrame.NORMAL);
		}
		mFrame.add(mCanvas);


		// by default, an AWT Frame doesn't do anything when you click
		// the close button; this bit of code will terminate the program when
		// the window is asked to close
		mFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				mWorker.cancel(true);
				mClosing = true;

				//Free VM thread from breakpoint
				if (mMessage != null)
					synchronized (mMessage) {
						mMessage.notify();
					}

				//Perform callbacks
				if (mCallbacks != null) {
					//mFrame.setVisible(false);
					mCallbacks.message(new CallbackMessage(CallbackMessage.SUCCESS, "Program completed"));
				}
				else
					System.exit(0);
			}
		});
		mFrame.setLocationRelativeTo(null);
	}

	@Override
	public void show(TaskCallback callbacks) {
		mCallbacks = callbacks;
		mFrame.setVisible(true);
		mWorker.execute();
	}

	@Override
	public void hide() {

		mWorker.cancel(true);
		mFrame.setVisible(false);

	}
	@Override
	public void stop() {

		mWorker.cancel(true);

	}

	@Override
	public boolean isFullscreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClosing() {
		// TODO Auto-generated method stub
		return mClosing;
	}

	private class VmWorker extends SwingWorker<Object, CallbackMessage>{

		@Override
		protected void process(List<CallbackMessage> chunks) {
			super.process(chunks);
			for (CallbackMessage message : chunks) {
				mCallbacks.message(message);
			}
		}
		@Override
		protected Object doInBackground() throws Exception {
			boolean noError;
			System.out.println("Running...");
			if (mVM == null)
				return null;	//TODO Throw exception

			//TODO Implement debugging

			if (mVM.getDebugger() == null) {
				//Debugger is not attached
				while (!this.isCancelled() && !mVM.Done() && !isClosing()) {
					//Continue to next OpCode
					driveVm(TomVM.VM_STEPS);

				}
			} else {
				//Debugger is attached
				while (!this.isCancelled() && !mVM.Done() && !isClosing()) {

					// Kick the virtual machine over the next op-code before patching in the breakpoints.
					// otherwise we would never get past a breakpoint once we hit it, because we would
					// keep on hitting it immediately and returning.
					driveVm(1);

					// Run the virtual machine for a certain number of steps
					mVM.PatchIn ();

					if (mVM.Paused()) {
						//Breakpoint reached or paused by debugger
						System.out.println("VM paused");
						mMessage = new CallbackMessage(CallbackMessage.PAUSED, "Reached breakpoint");
						publish(mMessage);

						synchronized (mMessage) {
							mMessage.wait();
						}
						//Check if program was stopped while paused
						if (this.isCancelled() || mVM.Done() || isClosing())
							break;
					}

					//Continue to next OpCode
					driveVm(TomVM.VM_STEPS);

				}
			}
			return null;
		}

		private void driveVm(int steps) {

			// Drive the virtual machine

			// Execute a number of VM steps
			try {
				mVM.Continue(steps);

			} catch (Exception e) {
				//TODO get error type
				// Need to screen out numeric errors, as these can be generated by some
				// OpenGL implementations...
				/*switch (GetExceptionCode()) {

                // Skip mathematics errors (overflows, divide by 0 etc).
                // This is quite important!, as some OpenGL drivers will trigger
                // divide-by-zero and other conditions if geometry happens to
                // be aligned in certain ways. The appropriate behaviour is to
                // ignore these errors, and keep running, and NOT to stop the
                // program!
                case EXCEPTION_FLT_DENORMAL_OPERAND:
                case EXCEPTION_FLT_DIVIDE_BY_ZERO:
                case EXCEPTION_FLT_INEXACT_RESULT:
                case EXCEPTION_FLT_INVALID_OPERATION:
                case EXCEPTION_FLT_OVERFLOW:
                case EXCEPTION_FLT_STACK_CHECK:
                case EXCEPTION_FLT_UNDERFLOW:
                case EXCEPTION_INT_DIVIDE_BY_ZERO:
                case EXCEPTION_INT_OVERFLOW:
                    mVM.SkipInstruction();*/
                    /*break;

                // All other exceptions will stop the program.
                default:*/
				mVM.MiscError("An exception occured!");
			}

			// Check for error
			if (mVM.hasError() || mVM.Done() || isClosing()) {
				int success;
				if (mCallbacks != null) {
					try {
						success = !mVM.hasError()
								? CallbackMessage.SUCCESS
								: CallbackMessage.FAILED;
						publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
								? "Program completed"
								: mVM.getError()));

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				if (isClosing() || isFullscreen())
					hide();    //Stop program and close window
				else
					stop(); //Just stop the worker thread;
			}
		}


	}

	@Override
	public Object getContext() {
		// TODO Auto-generated method stub
		return mFrame;
	}
	@Override
	public Object getContextGL() {
		// TODO Auto-generated method stub
		return mCanvas;
	}

	@Override
	public Configuration getSettings() {
		Configuration settings = new Configuration();
		settings.addSetting(new String[]{"Window Config"}, Configuration.PARAM_HEADING, "");
		settings.addSetting(new String[]{"Window Title"}, Configuration.PARAM_STRING, "My Application");
		settings.addSetting(new String[]{"Window Width"}, Configuration.PARAM_INT, "640");
		settings.addSetting(new String[]{"Window Height"}, Configuration.PARAM_INT, "480");
		settings.addSetting(new String[]{"Resizable Window"}, Configuration.PARAM_BOOL, "false");
		settings.addSetting(new String[]{"Screen Mode",
											"Windowed",
											"Fullscreen"},
											Configuration.PARAM_CHOICE, "0");
		settings.addSetting(new String[]{}, Configuration.PARAM_DIVIDER, "");
		settings.addSetting(new String[]{"Platforms"}, Configuration.PARAM_HEADING, "");
		settings.addSetting(new String[]{"Windows Support",
						"32/64-bit",
						"32-bit",
						"64-bit",
						"Do not support"},
				Configuration.PARAM_CHOICE, "0");
		settings.addSetting(new String[]{"Mac Support",
						"32/64-bit",
						"Do not support"},
				Configuration.PARAM_CHOICE, "0");
		settings.addSetting(new String[]{"Linux Support",
						"32/64-bit",
						"32-bit",
						"64-bit",
						"ARMv6",
						"ARMv6hf",
						"Do not support"},
				Configuration.PARAM_CHOICE, "0");
		settings.addSetting(new String[]{"Solaris Support",
						"32/64-bit",
						"32-bit",
						"64-bit",
						"Do not support"},
				Configuration.PARAM_CHOICE, "3");

		return settings;
	}

	@Override
	public Configuration getConfiguration() {
		if (mConfiguration == null)
			return getSettings();
		return mConfiguration;
	}

	@Override
	public void setConfiguration(Configuration config) {
		mConfiguration = config;
	}

	@Override
	public void loadConfiguration(InputStream stream) throws Exception{
		InputStream buffer = new BufferedInputStream(stream);
		ObjectInput input = new ObjectInputStream (buffer);
		mConfiguration = (Configuration)input.readObject();
		input.close();
		buffer.close();
	}

	@Override
	public void saveConfiguration(OutputStream stream) throws Exception{
		//Serialize configuration
		ObjectOutput output = new ObjectOutputStream(stream);
		output.writeObject(mConfiguration);
	}

	@Override
	public void saveState(OutputStream stream) throws IOException{
		DataOutputStream output = new DataOutputStream(stream);
		mComp.StreamOut(output);
	}

	@Override
	public void loadState(InputStream stream) throws IOException{
		DataInputStream input = new DataInputStream(stream);
		mComp.StreamIn(input);
	}

	@Override
	public boolean export(OutputStream stream, TaskCallback callback) throws Exception{
		int i;
		String path;
		//TODO set this as a parameter or global constant
		String libRoot = "jar/"; //External folder where dependencies should be located
		JarEntry entry;

		//TODO Add build option for single Jar
		boolean singleJar = true;	//Should be exported using JarInJar

		ClassLoader loader = getClass().getClassLoader();
		List<String> dependencies;

		//Create application's manifest
		Manifest manifest = new Manifest();

		path = singleJar ? "./" : "";
		//Generate class path
		dependencies = getDependencies();
		i = 0;
		if (dependencies != null)
			for (String dependency: dependencies) {
				path += ((i != 0 || singleJar) ? " " : "") + (singleJar ? "" : libRoot) + dependency;
				i++;
			}
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		if (singleJar) {
			manifest.getMainAttributes().put(new Attributes.Name("Rsrc-Class-Path"), path);
			manifest.getMainAttributes().put(new Attributes.Name("Rsrc-Main-Class"), getClass().getName());
			manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, ".");
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
					org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader.class.getName());
		} else {
			manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, path);
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
					getClass().getName());
		}
		JarOutputStream target = new JarOutputStream(stream, manifest);

		//Add Basic4GLj classes to new Jar
		System.out.println("Adding source files");
		List<String> files = new ArrayList<String>();
		files.add("com/basic4gl/compiler");
		files.add("com/basic4gl/lib");
		files.add("com/basic4gl/util");
		files.add("com/basic4gl/vm");
		if (singleJar)
			files.add("org/eclipse/jdt/internal/jarinjarloader");
		Exporter.addSource(files,target);

		//Save VM's initial state to Jar
		mVM.Reset();
		entry = new JarEntry(STATE_FILE);
		target.putNextEntry(entry);
		saveState(target);
		target.closeEntry();

		//Serialize the build configuration and add to Jar
		entry = new JarEntry(CONFIG_FILE);
		target.putNextEntry(entry);
		saveConfiguration(target);
		target.closeEntry();

		//Add external libraries
		if (singleJar){
			System.out.println("Adding dependencies");
			if (dependencies != null)
				for (String file: dependencies) {
					File source = new File(libRoot + file);
					if (!source.exists())
						continue;
					FileInputStream input = new FileInputStream(source);

					entry = new JarEntry(file);
					entry.setTime(source.lastModified());

					target.putNextEntry(entry);
					for (int c = input.read(); c != -1; c = input.read()) {
						target.write(c);
					}
					target.closeEntry();
				}
		}

		target.close();
		return true;
	}

	public final class WrapPrint implements Function{

		@Override
		public void run(TomVM vm) {
			System.out.print("debug: ");
			System.out.println(vm.GetStringParam(1));

			JOptionPane.showMessageDialog(null, vm.GetStringParam(1));
		}
		
	}

	@Override
	public List<String> getDependencies() {

		//Get settings
		if (mConfiguration == null)
			mConfiguration = getSettings();

		List<String> list = new ArrayList<String>();

		//Get supported platforms
		int windows = Integer.valueOf(mConfiguration.getValue(SETTING_SUPPORT_WINDOWS));
		int mac 	= Integer.valueOf(mConfiguration.getValue(SETTING_SUPPORT_MAC));
		int linux 	= Integer.valueOf(mConfiguration.getValue(SETTING_SUPPORT_LINUX));
		int solaris = Integer.valueOf(mConfiguration.getValue(SETTING_SUPPORT_SOLARIS));

		//Common
		list.add("gluegen-rt.jar");
		list.add("jogl-all.jar");

		//Source; Not sure if needed and they significantly increase output file size
		//list.add("gluegen-java-src.zip");
		//list.add("jogl-java-src.zip");

		//Windows
		if (windows == SUPPORT_WINDOWS_32_64 || windows == SUPPORT_WINDOWS_64) {
			//64-bit JOGL Windows libraries
			list.add("gluegen-rt-natives-windows-amd64.jar");
			list.add("jogl-all-natives-windows-amd64.jar");
		}
		if (windows == SUPPORT_WINDOWS_32_64 || windows == SUPPORT_WINDOWS_32) {
			//32-bit JOGL Windows libraries
			list.add("gluegen-rt-natives-windows-i586.jar");
			list.add("jogl-all-natives-windows-i586.jar");
		}
		//Mac
		if (mac == SUPPORT_MAC_32_64) {
			//Universal JOGL Mac libraries
			list.add("gluegen-rt-natives-macosx-universal.jar");
			list.add("jogl-all-natives-macosx-universal.jar");
		}
		//Linux
		if (linux == SUPPORT_LINUX_32_64 || linux == SUPPORT_LINUX_64) {
			//64-bit JOGL Linux libraries
			list.add("gluegen-rt-natives-linux-amd64.jar");
			list.add("jogl-all-natives-linux-amd64.jar");
		}
		if (linux == SUPPORT_LINUX_32_64 || linux == SUPPORT_LINUX_32) {
			//32-bit JOGL Linux libraries
			list.add("gluegen-rt-natives-linux-i586.jar");
			list.add("jogl-all-natives-linux-i586.jar");
		}
		if (linux == SUPPORT_LINUX_ARMV6) {
			//ARMv6 JOGL Linux libraries
			list.add("gluegen-rt-natives-linux-armv6.jar");
			list.add("jogl-all-natives-linux-armv6.jar");
		}
		if (linux == SUPPORT_LINUX_ARMV6HF) {
			//ARMv6hf JOGL Linux libraries
			list.add("gluegen-rt-natives-linux-armv6hf.jar");
			list.add("jogl-all-natives-linux-armv6hf.jar");
		}

		//Solaris
		if (solaris == SUPPORT_SOLARIS_32_64 || solaris == SUPPORT_SOLARIS_64) {
			//64-bit JOGL Solaris libraries
			list.add("gluegen-rt-natives-solaris-amd64.jar");
			list.add("jogl-all-natives-solaris-amd64.jar");
		}
		if (solaris == SUPPORT_SOLARIS_32_64 || solaris == SUPPORT_SOLARIS_64) {
			//32-bit JOGL Solaris libraries
			list.add("gluegen-rt-natives-solaris-i586.jar");
			list.add("jogl-all-natives-solaris-i586.jar");
		}
		/*
		//Possibly unnecessary files
		list.add("gluegen.jar");

		list.add("gluegen-rt-android-natives-android-armv6.jar");
		list.add("gluegen-rt-android-natives-linux-amd64.jar");
		list.add("gluegen-rt-android-natives-linux-armv6.jar");
		list.add("gluegen-rt-android-natives-linux-armv6hf.jar");
		list.add("gluegen-rt-android-natives-linux-i586.jar");
		list.add("gluegen-rt-android-natives-macosx-universal.jar");
		list.add("gluegen-rt-android-natives-solaris-amd64.jar");
		list.add("gluegen-rt-android-natives-solaris-i586.jar");
		list.add("gluegen-rt-android-natives-windows-amd64.jar");
		list.add("gluegen-rt-android-natives-windows-i586.jar");
		list.add("gluegen-rt-android.jar");
		list.add("gluegen-rt-natives-android-armv6.jar");

		list.add("jogl-all-android-natives-android-armv6.jar");
		list.add("jogl-all-android-natives-linux-amd64.jar");
		list.add("jogl-all-android-natives-linux-armv6.jar");
		list.add("jogl-all-android-natives-linux-armv6hf.jar");
		list.add("jogl-all-android-natives-linux-i586.jar");
		list.add("jogl-all-android-natives-macosx-universal.jar");
		list.add("jogl-all-android-natives-solaris-amd64.jar");
		list.add("jogl-all-android-natives-solaris-i586.jar");
		list.add("jogl-all-android-natives-windows-amd64.jar");
		list.add("jogl-all-android-natives-windows-i586.jar");
		list.add("jogl-all-android.jar");
		list.add("jogl-all-mobile-natives-android-armv6.jar");
		list.add("jogl-all-mobile-natives-linux-amd64.jar");
		list.add("jogl-all-mobile-natives-linux-armv6.jar");
		list.add("jogl-all-mobile-natives-linux-armv6hf.jar");
		list.add("jogl-all-mobile-natives-linux-i586.jar");
		list.add("jogl-all-mobile-natives-macosx-universal.jar");
		list.add("jogl-all-mobile-natives-solaris-amd64.jar");
		list.add("jogl-all-mobile-natives-solaris-i586.jar");
		list.add("jogl-all-mobile-natives-windows-amd64.jar");
		list.add("jogl-all-mobile-natives-windows-i586.jar");
		list.add("jogl-all-mobile.jar");

		list.add("jogl-all-natives-android-armv6.jar");

		list.add("jogl-all-noawt-natives-android-armv6.jar");
		list.add("jogl-all-noawt-natives-linux-amd64.jar");
		list.add("jogl-all-noawt-natives-linux-armv6.jar");
		list.add("jogl-all-noawt-natives-linux-armv6hf.jar");
		list.add("jogl-all-noawt-natives-linux-i586.jar");
		list.add("jogl-all-noawt-natives-macosx-universal.jar");
		list.add("jogl-all-noawt-natives-solaris-amd64.jar");
		list.add("jogl-all-noawt-natives-solaris-i586.jar");
		list.add("jogl-all-noawt-natives-windows-amd64.jar");
		list.add("jogl-all-noawt-natives-windows-i586.jar");
		list.add("jogl-all-noawt.jar");
		*/

		return list;
	}


}
