package com.basic4gl.lib.targets.desktopgl;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.ParamTypeList;
import com.basic4gl.lib.util.Configuration;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.lib.util.Library;
import com.basic4gl.lib.util.Target;
import com.basic4gl.lib.util.TaskCallback;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.types.ValType;
import com.basic4gl.vm.util.Function;

public class DesktopGL implements Library, Target{

	private static DesktopGL instance;

	private TomVM 	mVm;
	private VmWorker mWorker;

	private Frame mFrame;
	private GLCanvas mCanvas;

	private TaskCallback mCallbacks;
	private boolean mClosing;


	private Configuration mConfiguration;
	private static final int SETTING_TITLE 			= 0; //Index of window title setting in config
	private static final int SETTING_WIDTH 			= 1; //Index of window width setting in config
	private static final int SETTING_HEIGHT 		= 2; //Index of window height setting in config
	private static final int SETTING_RESIZABLE 		= 3; //Index of window resizable setting in config
	private static final int SETTING_SCREEN_MODE	= 4; //Index of screen mode setting in config

	private static final int MODE_WINDOWED = 0;
	private static final int MODE_FULLSCREEN = 1;

	public static void main(String[] args) {
		instance = new DesktopGL(new TomVM(null));
		instance.activate();
		instance.reset();
		instance.show(null);
	}
	public DesktopGL(TomVM vm){
		mVm = vm;
	}
	

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
		// TODO Add print functions
		Map<String, List<Function>> f = new HashMap<String, List<Function>>();
		f.put("print", new ArrayList<Function>());
		f.get("print").add(new WrapPrint());
		return f;
	}

	@Override
	public Map<String, List<FuncSpec>> specs() {
		// TODO Add print functions
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
		// TODO Auto-generated method stub
		if (mWorker != null)
			mWorker.cancel(true);
		mWorker = new VmWorker();
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
				if (mCallbacks != null)
					mCallbacks.complete(true, "Program completed");
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

	private class VmWorker extends SwingWorker{

		@Override
		protected Object doInBackground() throws Exception {
			boolean noError;
			if (mVm == null)
				return null;	//TODO Throw exception
			
			while (!this.isCancelled() && !mVm.Done()){
				if (isClosing())
					break;
				
				driveVm();
			
			}
			if (mCallbacks != null) {
				noError = !mVm.Error();
				mCallbacks.complete(noError, noError ? "Program completed" : mVm.GetError());
			}
			return null;
		}

		private void driveVm() {

			// Drive the virtual machine

			// Execute a number of VM steps
			try {
				mVm.Continue(TomVM.VM_STEPS);
			} catch (Exception e) {
				mVm.MiscError("An exception occured!");
			}

			// Check for error
			if (mVm.Error() || mVm.Done() || isClosing()) {
				if (isClosing() || isFullscreen())
					hide();	//Stop program and close window
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
	public OutputStream getState() throws IOException{
		return null;
	}

	@Override
	public void loadState(InputStream stream) throws IOException{

	}

	@Override
	public Configuration getSettings() {
		Configuration settings = new Configuration();

		settings.addSetting(new String[]{"Window Title"}, Configuration.PARAM_STRING, "My Application");
		settings.addSetting(new String[]{"Window Width"}, Configuration.PARAM_INT, "640");
		settings.addSetting(new String[]{"Window Height"}, Configuration.PARAM_INT, "480");
		settings.addSetting(new String[]{"Resizable"}, Configuration.PARAM_BOOL, "false");
		settings.addSetting(new String[]{"Screen Mode",
											"Windowed",
											"Fullscreen"},
											Configuration.PARAM_CHOICE, "0");

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
	public OutputStream export() throws IOException{
		return new JarOutputStream(null);
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
		// TODO Confirm files needed		
		List<String> list = new ArrayList<String>();
		//list.add("jogl-all.jar");
		//list.add("gluegen.jar");
		//list.add("gluegen-rt.jar");
		
		// TODO Return different dependencies depending on settings
		//Native libraries
		list.add("jogl/android-armv6/libgluegen-rt.so");
		list.add("jogl/android-armv6/libjoal.so");
		list.add("jogl/android-armv6/libjocl.so");
		list.add("jogl/android-armv6/libjogl_mobile.so");
		list.add("jogl/android-armv6/libnewt.so");
		list.add("jogl/android-armv6/libopenal.so");

		list.add("jogl/linux-amd64/libgluegen-rt.so");
		list.add("jogl/linux-amd64/libjoal.so");
		list.add("jogl/linux-amd64/libjocl.so");
		list.add("jogl/linux-amd64/libjogl_cg.so");
		list.add("jogl/linux-amd64/libjogl_desktop.so");
		list.add("jogl/linux-amd64/libjogl_mobile.so");
		list.add("jogl/linux-amd64/libnativewindow_awt.so");
		list.add("jogl/linux-amd64/libnativewindow_x11.so");
		list.add("jogl/linux-amd64/libnewt.so");
		list.add("jogl/linux-amd64/libopenal.so");

		list.add("jogl/linux-armv6/libgluegen-rt.so");
		list.add("jogl/linux-armv6/libjoal.so");
		list.add("jogl/linux-armv6/libjocl.so");
		list.add("jogl/linux-armv6/libjogl_cg.so");
		list.add("jogl/linux-armv6/libjogl_desktop.so");
		list.add("jogl/linux-armv6/libjogl_mobile.so");
		list.add("jogl/linux-armv6/libnativewindow_awt.so");
		list.add("jogl/linux-armv6/libnativewindow_x11.so");
		list.add("jogl/linux-armv6/libnewt.so");
		list.add("jogl/linux-armv6/libopenal.so");

		list.add("jogl/linux-armv6hf/libgluegen-rt.so");
		list.add("jogl/linux-armv6hf/libjoal.so");
		list.add("jogl/linux-armv6hf/libjocl.so");
		list.add("jogl/linux-armv6hf/libjogl_cg.so");
		list.add("jogl/linux-armv6hf/libjogl_desktop.so");
		list.add("jogl/linux-armv6hf/libjogl_mobile.so");
		list.add("jogl/linux-armv6hf/libnativewindow_awt.so");
		list.add("jogl/linux-armv6hf/libnativewindow_x11.so");
		list.add("jogl/linux-armv6hf/libnewt.so");
		list.add("jogl/linux-armv6hf/libopenal.so");

		list.add("jogl/linux-i586/libgluegen-rt.so");
		list.add("jogl/linux-i586/libjoal.so");
		list.add("jogl/linux-i586/libjocl.so");
		list.add("jogl/linux-i586/libjogl_cg.so");
		list.add("jogl/linux-i586/libjogl_desktop.so");
		list.add("jogl/linux-i586/libjogl_mobile.so");
		list.add("jogl/linux-i586/libnativewindow_awt.so");
		list.add("jogl/linux-i586/libnativewindow_x11.so");
		list.add("jogl/linux-i586/libnewt.so");
		list.add("jogl/linux-i586/libopenal.so");

		list.add("jogl/macosx-universal/libgluegen-rt.jnilib");
		list.add("jogl/macosx-universal/libjoal.jnilib");
		list.add("jogl/macosx-universal/libjocl.jnilib");
		list.add("jogl/macosx-universal/libjogl_cg.jnilib");
		list.add("jogl/macosx-universal/libjogl_desktop.jnilib");
		list.add("jogl/macosx-universal/libjogl_mobile.jnilib");
		list.add("jogl/macosx-universal/libnativewindow_awt.jnilib");
		list.add("jogl/macosx-universal/libnativewindow_macosx.jnilib");
		list.add("jogl/macosx-universal/libnewt.jnilib");
		list.add("jogl/macosx-universal/libopenal.1.15.1.dylib");
		list.add("jogl/macosx-universal/libopenal.1.dylib");
		list.add("jogl/macosx-universal/libopenal.dylib");
		
		list.add("jogl/solaris-amd64/libgluegen-rt.so");
		list.add("jogl/solaris-amd64/libjoal.so");
		list.add("jogl/solaris-amd64/libjocl.so");
		list.add("jogl/solaris-amd64/libjogl_cg.so");
		list.add("jogl/solaris-amd64/libjogl_desktop.so");
		list.add("jogl/solaris-amd64/libjogl_mobile.so");
		list.add("jogl/solaris-amd64/libnativewindow_awt.so");
		list.add("jogl/solaris-amd64/libnativewindow_x11.so");
		list.add("jogl/solaris-amd64/libnewt.so");
		
		list.add("jogl/solaris-i586/libgluegen-rt.so");
		list.add("jogl/solaris-i586/libjoal.so");
		list.add("jogl/solaris-i586/libjocl.so");
		list.add("jogl/solaris-i586/libjogl_cg.so");
		list.add("jogl/solaris-i586/libjogl_desktop.so");
		list.add("jogl/solaris-i586/libjogl_mobile.so");
		list.add("jogl/solaris-i586/libnativewindow_awt.so");
		list.add("jogl/solaris-i586/libnativewindow_x11.so");
		list.add("jogl/solaris-i586/libnewt.so");

		list.add("jogl/windows-amd64/gluegen-rt.dll");
		list.add("jogl/windows-amd64/joal.dll");
		list.add("jogl/windows-amd64/jocl.dll");
		list.add("jogl/windows-amd64/jogl_cg.dll");
		list.add("jogl/windows-amd64/jogl_desktop.dll");
		list.add("jogl/windows-amd64/jogl_mobile.dll");
		list.add("jogl/windows-amd64/nativewindow_awt.dll");
		list.add("jogl/windows-amd64/nativewindow_win32.dll");
		list.add("jogl/windows-amd64/newt.dll");
		list.add("jogl/windows-amd64/soft_oal.dll");

		list.add("jogl/windows-i586/gluegen-rt.dll");
		list.add("jogl/windows-i586/joal.dll");
		list.add("jogl/windows-i586/jocl.dll");
		list.add("jogl/windows-i586/jogl_cg.dll");
		list.add("jogl/windows-i586/jogl_desktop.dll");
		list.add("jogl/windows-i586/jogl_mobile.dll");
		list.add("jogl/windows-i586/nativewindow_awt.dll");
		list.add("jogl/windows-i586/nativewindow_win32.dll");
		list.add("jogl/windows-i586/newt.dll");
		list.add("jogl/windows-i586/soft_oal.dll");
		
		list.add("jogl/gluegen-java-src.zip");
		list.add("jogl/gluegen-rt.jar");
		list.add("jogl/jogl-all.jar");
		list.add("jogl/jogl-java-src.zip");

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
		list.add("gluegen-rt-natives-linux-amd64.jar");
		list.add("gluegen-rt-natives-linux-armv6.jar");
		list.add("gluegen-rt-natives-linux-armv6hf.jar");
		list.add("gluegen-rt-natives-linux-i586.jar");
		list.add("gluegen-rt-natives-macosx-universal.jar");
		list.add("gluegen-rt-natives-solaris-amd64.jar");
		list.add("gluegen-rt-natives-solaris-i586.jar");
		list.add("gluegen-rt-natives-windows-amd64.jar");
		list.add("gluegen-rt-natives-windows-i586.jar");
		list.add("gluegen-rt.jar");
		list.add("gluegen.jar");
		
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
		list.add("jogl-all-natives-linux-amd64.jar");
		list.add("jogl-all-natives-linux-armv6.jar");
		list.add("jogl-all-natives-linux-armv6hf.jar");
		list.add("jogl-all-natives-linux-i586.jar");
		list.add("jogl-all-natives-macosx-universal.jar");
		list.add("jogl-all-natives-solaris-amd64.jar");
		list.add("jogl-all-natives-solaris-i586.jar");
		list.add("jogl-all-natives-windows-amd64.jar");
		list.add("jogl-all-natives-windows-i586.jar");
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
		list.add("jogl-all.jar");
		
		
		return list;
	}
	@Override
	public List<String> getDependenciesForClassPath() {
		List<String> list = new ArrayList<String>();
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
		list.add("gluegen-rt-natives-linux-amd64.jar");
		list.add("gluegen-rt-natives-linux-armv6.jar");
		list.add("gluegen-rt-natives-linux-armv6hf.jar");
		list.add("gluegen-rt-natives-linux-i586.jar");
		list.add("gluegen-rt-natives-macosx-universal.jar");
		list.add("gluegen-rt-natives-solaris-amd64.jar");
		list.add("gluegen-rt-natives-solaris-i586.jar");
		list.add("gluegen-rt-natives-windows-amd64.jar");
		list.add("gluegen-rt-natives-windows-i586.jar");
		list.add("gluegen-rt.jar");
		list.add("gluegen.jar");
		
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
		list.add("jogl-all-natives-linux-amd64.jar");
		list.add("jogl-all-natives-linux-armv6.jar");
		list.add("jogl-all-natives-linux-armv6hf.jar");
		list.add("jogl-all-natives-linux-i586.jar");
		list.add("jogl-all-natives-macosx-universal.jar");
		list.add("jogl-all-natives-solaris-amd64.jar");
		list.add("jogl-all-natives-solaris-i586.jar");
		list.add("jogl-all-natives-windows-amd64.jar");
		list.add("jogl-all-natives-windows-i586.jar");
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
		list.add("jogl-all.jar");

		return list;
	}


}
