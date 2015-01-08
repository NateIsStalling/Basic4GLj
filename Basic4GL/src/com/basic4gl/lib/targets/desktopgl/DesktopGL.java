package com.basic4gl.lib.targets.desktopgl;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.basic4gl.compiler.compConstant;
import com.basic4gl.compiler.compParamTypeList;
import com.basic4gl.util.compFuncSpec;
import com.basic4gl.lib.util.Library;
import com.basic4gl.lib.util.Target;
import com.basic4gl.lib.util.TaskCallback;
import com.basic4gl.vm.TomVM;
import com.basic4gl.vm.types.ValType.BasicValType;
import com.basic4gl.vm.util.Function;

public class DesktopGL implements Library, Target{

	private static DesktopGL instance;

	private TomVM 	mVm;
	private VmWorker mWorker;

	private Frame mFrame;
	private GLCanvas mCanvas;

	private TaskCallback mCallbacks;
	private boolean mClosing;

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
	public String name() { return "OpenGL Window";}

	@Override
	public String version() { return "0.1";}

	@Override
	public String description() { return "Uses jogl";}

	@Override
	public String author() { return "Nathaniel Nielsen";}

	@Override
	public String contact() { return "Twitter: @crazynate_";}

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
	public Map<String, compConstant> constants() {
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
	public Map<String, List<compFuncSpec>> specs() {
		// TODO Add print functions
		Map<String, List<compFuncSpec>> s = new HashMap<String, List<compFuncSpec>>();
		s.put("print", new ArrayList<compFuncSpec>());
		s.get("print").add( new compFuncSpec(new compParamTypeList(	new BasicValType[] { BasicValType.VTP_STRING }), false, false, BasicValType.VTP_INT, false, false, null));
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

		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		GLCanvas mCanvas = new GLCanvas(caps);

		mFrame = new Frame("AWT Window Test");
		mFrame.setSize(300, 300);
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
			if (mVm == null)
				return null;	//TODO Throw exception
			
			while (!this.isCancelled() && !mVm.Done()){
				if (isClosing())
					break;
				
				driveVm();
			
			}
			mCallbacks.complete(true, "Program completed");
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
			if (mVm.Error()) {


			} else if (mVm.Done()) {
				if (mCallbacks != null)
					mCallbacks.complete(true, "Program completed");
				if (isClosing() || isFullscreen())
					hide();
			}
			// TODO Implement OpenGL

			else if (mVm.Done() || isClosing()) { 
				if (mCallbacks != null)
					mCallbacks.complete(true, "Program completed");
				if (isClosing() || isFullscreen())
					hide();
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
