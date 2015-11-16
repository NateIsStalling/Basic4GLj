package com.basic4gl.lib.targets.desktopgl;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import javax.swing.*;

import com.basic4gl.compiler.Constant;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.*;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.vm.TomVM;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;


public class GLTextGridWindow extends GLWindow {

	//Libraries
	private java.util.List<Library> mLibraries;
	private static GLTextGridWindow instance;

	private TomBasicCompiler mComp;
	private TomVM mVM;
	private VmWorker mWorker;	//Debugging
	private Thread mThread;	//Standalone


	private CountDownLatch completionLatch;
	private TaskCallback mCallbacks;
	private CallbackMessage mMessage;
	private CallbackMessage mUpdates;
	private boolean mClosing;

	private Configuration mConfiguration;

	static final int SETTING_TITLE 				= 1; //Index of window title setting in config
	static final int SETTING_WIDTH 				= 2; //Index of window width setting in config
	static final int SETTING_HEIGHT 			= 3; //Index of window height setting in config
	static final int SETTING_RESIZABLE 			= 4; //Index of window resizable setting in config
	static final int SETTING_SCREEN_MODE		= 5; //Index of screen mode setting in config
	static final int SETTING_SUPPORT_WINDOWS	= 8; //Index of Windows support setting in config
	static final int SETTING_SUPPORT_MAC		= 9; //Index of Mac support setting in config
	static final int SETTING_SUPPORT_LINUX		= 10; //Index of Linux support setting in config

	static final int SUPPORT_WINDOWS_32_64		= 0;
	static final int SUPPORT_WINDOWS_32			= 1;
	static final int SUPPORT_WINDOWS_64			= 2;
	static final int SUPPORT_WINDOWS_NO			= 3;

	static final int SUPPORT_MAC_32_64			= 0;
	static final int SUPPORT_MAC_NO				= 1;

	static final int SUPPORT_LINUX_32_64		= 0;
	static final int SUPPORT_LINUX_32			= 1;
	static final int SUPPORT_LINUX_64			= 2;

	static final int MODE_WINDOWED = 0;
	static final int MODE_FULLSCREEN = 1;

	static final String CONFIG_FILE			= "config.ser";	//Filename for configuration file
	static final String STATE_FILE			= "state.bin";	//Filename for stored VM state

	public GLTextGridWindow(){
		super(false,true,640,          // Note: If width = 0, will use screen width
		480,
		0,		//Color depth - 0 (use desktop), 16, or 32
		true,
		"Basic4GLj",
		false,
		false);
	}
	public static Library getInstance(TomBasicCompiler compiler){
		GLTextGridWindow instance = new GLTextGridWindow();
		instance.mComp = compiler;
		instance.mVM = compiler.VM();
		return instance;
	}
	/*
	//Constructor from C++ source for reference
	glTextGridWindow (  bool fullScreen,
						bool border,
						int width,          // Note: If width = 0, will use screen width
						int height,
						int bpp,
						bool stencil,
						std::string title,
						bool allowResizing,
						bool fitToWorkArea,
						ResetGLModeType resetGLMode);*/

	public static void main(String[] args) {
		//Debug only, makes it easier to attach a remote debugger
		//JOptionPane.showMessageDialog(null, "Waiting...");

		instance = new GLTextGridWindow();
		instance.mComp = new TomBasicCompiler(new TomVM(null));
		instance.mVM = instance.mComp.VM();
		instance.mLibraries = new ArrayList<Library>();
		//TODO Load libraries dynamically
		//TODO Save/Load list of libraries in order they should be added
		instance.mLibraries.add(new com.basic4gl.lib.standard.Standard());
		instance.mLibraries.add(new com.basic4gl.lib.standard.TrigBasicLib());
		instance.mLibraries.add(new com.basic4gl.lib.targets.desktopgl.TextBasicLib());
		instance.mLibraries.add(new com.basic4gl.lib.targets.desktopgl.OpenGLBasicLib());
		instance.mLibraries.add(new com.basic4gl.lib.targets.desktopgl.GLBasicLib_gl());

		// Register library functions
		for (Library lib : instance.mLibraries) {
			//instance.mComp.AddConstants(lib.constants());
			instance.mComp.AddFunctions(lib, lib.specs());
		}
		// Register DesktopGL's functions
		instance.mComp.AddConstants(instance.constants());
		instance.mComp.AddFunctions(instance, instance.specs());

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
		instance.mVM.Pause();
		instance.mVM.Reset();
		instance.resetThread(); //todo cleanup threading code
		instance.activate();
		instance.mThread.start();
	}

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
	public void init(TomBasicCompiler comp){

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
	public Map<String, FuncSpec[]> specs() {
		return null;
	}
	@Override
	public boolean isVisible() {
		return mWorker != null && mWorker.isVisible();
	}
	//Temporary; needed to show standalone thread
	private void resetThread(){
		mThread = new Thread(new VMThread());
	}
	@Override
	public void reset() {
		mVM.Pause();
		if (mWorker != null){
			mWorker.cancel(true);
			//TODO confirm there is no overlap with this thread stopping and starting a new one to avoid GL errors
			try {
				completionLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mWorker = new VmWorker();
		mWorker.setCompletionLatch(completionLatch = new CountDownLatch(1));
		mVM.Reset();
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
	}



	@Override
	public void show(TaskCallback callbacks) {
		mCallbacks = callbacks;
		mWorker.execute();
	}

	@Override
	public void hide() {
		mWorker.cancel(true);
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
		return mClosing || (mWorker != null && mWorker.isClosing());
	}

	void swapBuffers(){
		if (isVisible())
			mWorker.swapBuffers();
	}

	private class VMThread implements Runnable {
		// We need to strongly reference callback instances.
		private GLFWErrorCallback errorCallback;
		private GLFWKeyCallback keyCallback;
		private GLFWCharCallback charCallback;
		GLTextGrid  mTextGrid;
		GLTextGrid getTextGrid ()                 { return mTextGrid; }
		void setTextGrid (GLTextGrid grid)    { mTextGrid = grid; }

		void    RecreateGLContext (){
			GLTextGridWindow.super.RecreateGLContext();

			if (mTextGrid != null)
				mTextGrid.UploadCharsetTexture();
		}
		boolean isClosing(){
			try {
				synchronized (this) {
					return m_window != 0 && glfwWindowShouldClose(m_window) == GL_TRUE;
				}
			} catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}
		boolean isVisible() {
			try {
				synchronized (this) {
					return m_window != 0 && glfwWindowShouldClose(m_window) == GL_FALSE;
				}
			} catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}
		void swapBuffers(){
			try {
				synchronized (this) {
					glfwSwapBuffers(m_window);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		private void init() {
			synchronized (GLTextGridWindow.this) {
				//C++ source code for reference
				// m_glWin = null;
				// m_glText = null;

				// Default settings
				// boolean fullScreen = false, border = true;
				// int width = 640, height = 480, bpp = 0;
				// ResetGLModeType resetGLMode = RGM_RESETSTATE;

				// Create window
				/*
		 		m_glWin = new glTextGridWindow ( fullScreen, border, width, height,
		 		bpp, "Basic4GL", resetGLMode);

		 		// Check for errors if (m_glWin.Error ()) { MessageDlg ( (AnsiString)
		 		m_glWin.GetError().c_str(), mtError, TMsgDlgButtons() << mbOK, 0);
		 		Application.Terminate (); return; } m_glWin.Hide ();

		 		// Create OpenGL text grid m_glText = new glSpriteEngine (
		 		(ExtractFilePath (Application.ExeName) + "charset.png").c_str (),
		 		&m_files, 25, 40, 16, 16);
		 		// Check for errors if (m_glText.Error ()) { MessageDlg (
		 		(AnsiString) + m_glText.GetError ().c_str (), mtError,
		 		TMsgDlgButtons() << mbOK, 0); Application.Terminate (); return; }
		 		m_glWin.SetTextGrid (m_glText);
		 		*/
				String title = mConfiguration.getValue(SETTING_TITLE);
				m_width = Integer.valueOf(mConfiguration.getValue(SETTING_WIDTH));
				m_height = Integer.valueOf(mConfiguration.getValue(SETTING_HEIGHT));

				boolean resizable = Boolean.valueOf(mConfiguration.getValue(SETTING_RESIZABLE));
				int mode = Integer.valueOf(mConfiguration.getValue(SETTING_SCREEN_MODE));


				// Setup an error callback. The default implementation
				// will print the error message in System.err.
				glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

				// Initialize GLFW. Most GLFW functions will not work before doing this.
				if (glfwInit() != GL11.GL_TRUE) {
					throw new IllegalStateException("Unable to initialize GLFW");
				}

				// Configure our window
				glfwDefaultWindowHints(); // optional, the current window hints are already the default
				glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
				glfwWindowHint(GLFW_RESIZABLE, resizable ? GL_TRUE : GL_FALSE); // the window will be resizable


				// Create the window
				m_window = glfwCreateWindow(m_width, m_height, title, NULL, NULL);
				if (m_window == NULL) {
					throw new RuntimeException("Failed to create the GLFW window");
				}

				//TODO implement window icons

				/* //TODO Implement fullscreen and windowless mode
				//Scrap code from previous swing implementation
				if (mode == MODE_FULLSCREEN) {
					mFrame.setUndecorated(true);
					mFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				} else {
					mFrame.setUndecorated(false);
					mFrame.setExtendedState(JFrame.NORMAL);
				}
				mFrame.add(mCanvas);*/

				// Setup a key callback. It will be called every time a key is pressed, repeated or released.
				glfwSetKeyCallback(m_window, keyCallback = new GLFWKeyCallback() {
					@Override
					public void invoke(long window, int key, int scancode, int action, int mods) {
						if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
							glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop
						}

						if (action == GLFW_PRESS){
							if (key == GLFW_KEY_PAUSE)
								m_pausePressed = true;
							else {
								m_keyDown [key & 0xffff] |= 1;
								BufferScanKey ((char) (key & 0xffff));
							}
						} else if (action == GLFW_RELEASE){
							m_keyDown [key & 0xffff] &= ~1;
						}
					}
				});
				// Setup a character key callback
				glfwSetCharCallback(m_window, charCallback = new GLFWCharCallback() {
					@Override
					public void invoke(long window, int codepoint) {

						if (codepoint == 27)               // Esc closes window
							m_closing = true;

						int end = m_bufEnd;
						IncEnd();                    // Check for room in buffer
						if (m_bufEnd != m_bufStart)
							m_keyBuffer[end] = (char)codepoint;
						else
							m_bufEnd = end;           // No room. Restore buffer pointers


					}
				});

				// Get the resolution of the primary monitor
				ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
				// Center our window
				glfwSetWindowPos(
						m_window,
						(GLFWvidmode.width(vidmode) - m_width) / 2,
						(GLFWvidmode.height(vidmode) - m_height) / 2
				);

				// Make the OpenGL context current
				glfwMakeContextCurrent(m_window);
				// Enable v-sync
				glfwSwapInterval(1);

				// Make the window visible
				glfwShowWindow(m_window);
			}
		}
		@Override
		public void run() {
			System.out.println("Running...");
			if (mVM == null) {
				return;    //TODO Throw exception
			}
			try {
				//Create window
				init();
				// This line is critical for LWJGL's interoperation with GLFW's
				// OpenGL context, or any context that is managed externally.
				// LWJGL detects the context that is current in the current thread,
				// creates the ContextCapabilities instance and makes the OpenGL
				// bindings available for use.
				GL.createCapabilities();

				ResetGL();
				// Initialize Sprite Engine
				mTextGrid = new GLSpriteEngine("charset.png", null, 25, 40, 16, 16);
				if (mTextGrid.hasError())
					mVM.setError(mTextGrid.getError());

				//Initialize libraries
				for (Library lib : mComp.getLibraries()) {
					if (lib instanceof IGLRenderer) {
						((IGLRenderer) lib).setTextGrid(mTextGrid);
						((IGLRenderer) lib).setWindow(GLTextGridWindow.this);
					}
					lib.init(mVM);
				}
				if (mVM.getDebugger() == null) {
					//Debugger is not attached
					while (!Thread.currentThread().isInterrupted() && !mVM.hasError() && !mVM.Done() && !isClosing()) {
						//Continue to next OpCode
						driveVm(TomVM.VM_STEPS);

						// Poll for window events. The key callback above will only be
						// invoked during this call.
						glfwPollEvents();

					}
				}    //Program completed

				//Perform debugger callbacks
				//TODO implement callbacks
				/*
				if (mCallbacks != null) {
					int success;
					success = !mVM.hasError()
							? CallbackMessage.SUCCESS
							: CallbackMessage.FAILED;
					publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
							? "Program completed"
							: mVM.getError()));
				}*/

				//glfwSwapBuffers(m_window); // swap the color buffers
				//Keep window responsive until closed
				while (!Thread.currentThread().isInterrupted() && m_window != 0 && !isClosing()) {
					try {
						//Go easy on the processor
						Thread.sleep(10);
					} catch (InterruptedException e){ break;}

					// Poll for window events. The key callback above will only be
					// invoked during this call.
					glfwPollEvents();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				synchronized (GLTextGridWindow.this) {

					//Free text grid image
					mTextGrid.destroy();

					// Release window and window callbacks
					glfwDestroyWindow(m_window);
					keyCallback.release();
					charCallback.release();
					ClearKeyBuffers();

					// Terminate GLFW and release the GLFWerrorfun
					glfwTerminate();
					errorCallback.release();
					//Clear pointer to window
					//An access violation will occur next time this window is launched if this isn't cleared
					m_window = 0;
				}
			}
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
				e.printStackTrace();
				mVM.MiscError("An exception occured!");
			}

			// Check for error
			if (mVM.hasError() || mVM.Done() || isClosing()) {
				int success;
				//TODO implement callbacks
				/*if (mCallbacks != null) {
					success = !mVM.hasError()
							? CallbackMessage.SUCCESS
							: CallbackMessage.FAILED;
					publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
							? "Program completed"
							: mVM.getError()));
				}*/

				if (isClosing() || isFullscreen()) {
					hide();    //Stop program and close window
				}
				else {
					//TODO handle program completion options
					//stop(); //Just stop the worker thread;
				}

			}
		}
	}
	private class VmWorker extends SwingWorker<Object, CallbackMessage>{
		// We need to strongly reference callback instances.
		private GLFWErrorCallback errorCallback;
		private GLFWKeyCallback keyCallback;
		private GLFWCharCallback charCallback;

		//Prevents multiple VmWorker threads being executed at the same time;
		//window would become unresponsive if multiple threads were created
		private CountDownLatch mCompletionLatch;

		GLTextGrid  mTextGrid;
		GLTextGrid getTextGrid ()                 { return mTextGrid; }
		void setTextGrid (GLTextGrid grid)    { mTextGrid = grid; }

		void    RecreateGLContext (){
			GLTextGridWindow.super.RecreateGLContext();

			if (mTextGrid != null)
				mTextGrid.UploadCharsetTexture();
		}
		void setCompletionLatch(CountDownLatch latch){ mCompletionLatch = latch;}

		boolean isClosing(){
			try {
				synchronized (this) {
					return m_window != 0 && glfwWindowShouldClose(m_window) == GL_TRUE;
				}
			} catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}
		boolean isVisible() {
			try {
				synchronized (this) {
					return m_window != 0 && glfwWindowShouldClose(m_window) == GL_FALSE;
				}
			} catch (Exception e){
				e.printStackTrace();
				return false;
			}
		}
		void swapBuffers(){
			try {
				synchronized (this) {
					glfwSwapBuffers(m_window);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		private void init() {
			synchronized (GLTextGridWindow.this) {
				//C++ source code for reference
				// m_glWin = null;
				// m_glText = null;

				// Default settings
				// boolean fullScreen = false, border = true;
				// int width = 640, height = 480, bpp = 0;
				// ResetGLModeType resetGLMode = RGM_RESETSTATE;

				// Create window
				/*
		 		m_glWin = new glTextGridWindow ( fullScreen, border, width, height,
		 		bpp, "Basic4GL", resetGLMode);

		 		// Check for errors if (m_glWin.Error ()) { MessageDlg ( (AnsiString)
		 		m_glWin.GetError().c_str(), mtError, TMsgDlgButtons() << mbOK, 0);
		 		Application.Terminate (); return; } m_glWin.Hide ();

		 		// Create OpenGL text grid m_glText = new glSpriteEngine (
		 		(ExtractFilePath (Application.ExeName) + "charset.png").c_str (),
		 		&m_files, 25, 40, 16, 16);
		 		// Check for errors if (m_glText.Error ()) { MessageDlg (
		 		(AnsiString) + m_glText.GetError ().c_str (), mtError,
		 		TMsgDlgButtons() << mbOK, 0); Application.Terminate (); return; }
		 		m_glWin.SetTextGrid (m_glText);
		 		*/
				String title = mConfiguration.getValue(SETTING_TITLE);
				m_width = Integer.valueOf(mConfiguration.getValue(SETTING_WIDTH));
				m_height = Integer.valueOf(mConfiguration.getValue(SETTING_HEIGHT));

				boolean resizable = Boolean.valueOf(mConfiguration.getValue(SETTING_RESIZABLE));
				int mode = Integer.valueOf(mConfiguration.getValue(SETTING_SCREEN_MODE));


				// Setup an error callback. The default implementation
				// will print the error message in System.err.
				glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

				// Initialize GLFW. Most GLFW functions will not work before doing this.
				if (glfwInit() != GL11.GL_TRUE)
					throw new IllegalStateException("Unable to initialize GLFW");

				// Configure our window
				glfwDefaultWindowHints(); // optional, the current window hints are already the default
				glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
				glfwWindowHint(GLFW_RESIZABLE, resizable ? GL_TRUE : GL_FALSE); // the window will be resizable

				// Create the window
				m_window = glfwCreateWindow(m_width, m_height, title, NULL, NULL);
				if (m_window == NULL)
					throw new RuntimeException("Failed to create the GLFW window");

				//TODO implement window icons

				/* //TODO Implement fullscreen and windowless mode
				//Scrap code from previous swing implementation
				if (mode == MODE_FULLSCREEN) {
					mFrame.setUndecorated(true);
					mFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				} else {
					mFrame.setUndecorated(false);
					mFrame.setExtendedState(JFrame.NORMAL);
				}
				mFrame.add(mCanvas);*/

				// Setup a key callback. It will be called every time a key is pressed, repeated or released.
				glfwSetKeyCallback(m_window, keyCallback = new GLFWKeyCallback() {
					@Override
					public void invoke(long window, int key, int scancode, int action, int mods) {
						if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
							glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop
						}

						if (action == GLFW_PRESS){
							if (key == GLFW_KEY_PAUSE)
								m_pausePressed = true;
							else {
								m_keyDown [key & 0xffff] |= 1;
								BufferScanKey ((char) (key & 0xffff));
							}
						} else if (action == GLFW_RELEASE){
							m_keyDown [key & 0xffff] &= ~1;
						}
					}
				});
				// Setup a character key callback
				glfwSetCharCallback(m_window, charCallback = new GLFWCharCallback() {
					@Override
					public void invoke(long window, int codepoint) {

						if (codepoint == 27)               // Esc closes window
							m_closing = true;

						int end = m_bufEnd;
						IncEnd();                    // Check for room in buffer
						if (m_bufEnd != m_bufStart)
							m_keyBuffer[end] = (char)codepoint;
						else
							m_bufEnd = end;           // No room. Restore buffer pointers


				}
			});

			// Get the resolution of the primary monitor
				ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
				// Center our window
				glfwSetWindowPos(
						m_window,
						(GLFWvidmode.width(vidmode) - m_width) / 2,
						(GLFWvidmode.height(vidmode) - m_height) / 2
				);


				// Make the OpenGL context current
				glfwMakeContextCurrent(m_window);
				// Enable v-sync
				glfwSwapInterval(1);

				// Make the window visible
				glfwShowWindow(m_window);
			}
		}
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
				return null;    //TODO Throw exception
			try {

				//Create window
				init();

				// This line is critical for LWJGL's interoperation with GLFW's
				// OpenGL context, or any context that is managed externally.
				// LWJGL detects the context that is current in the current thread,
				// creates the ContextCapabilities instance and makes the OpenGL
				// bindings available for use.
				GL.createCapabilities();

				ResetGL();

				// Initialize Sprite Engine
				mTextGrid = new GLSpriteEngine("charset.png", null, 25, 40, 16, 16);
				if (mTextGrid.hasError())
					mVM.setError(mTextGrid.getError());

				//Initialize libraries
				for (Library lib : mComp.getLibraries()) {
					if (lib instanceof IGLRenderer) {
						((IGLRenderer) lib).setTextGrid(mTextGrid);
						((IGLRenderer) lib).setWindow(GLTextGridWindow.this);
					}
					lib.init(mVM);
				}
				if (mVM.getDebugger() == null) {
					//Debugger is not attached
					while (!this.isCancelled() && !mVM.hasError() && !mVM.Done() && !isClosing()) {
						//Continue to next OpCode
						driveVm(TomVM.VM_STEPS);

						// Poll for window events. The key callback above will only be
						// invoked during this call.
						glfwPollEvents();

					}
				} else {
					//Debugger is attached
					while (!this.isCancelled() && !mVM.hasError() && !mVM.Done() && !isClosing()) {
						// Run the virtual machine for a certain number of steps
						mVM.PatchIn();

						if (mVM.Paused()) {
							//Breakpoint reached or paused by debugger
							System.out.println("VM paused");
							mMessage = new CallbackMessage(CallbackMessage.PAUSED, "Reached breakpoint");
							publish(mMessage);

							//Wait for IDE to unpause the application
							synchronized (mMessage) {
								while (mMessage.status == CallbackMessage.PAUSED) {
									//Go easy on the processor
									try{
										Thread.sleep(10);
									} catch (InterruptedException e){}
									// Keep OpenGL window responsive while paused
									glfwPollEvents();
									mMessage.wait(100);
								}
							}
							//Resume running
							if (mMessage.status == CallbackMessage.WORKING) {
								// Kick the virtual machine over the next op-code before patching in the breakpoints.
								// otherwise we would never get past a breakpoint once we hit it, because we would
								// keep on hitting it immediately and returning.
								driveVm(1);

								// Run the virtual machine for a certain number of steps
								mVM.PatchIn();
							}
							//Check if program was stopped while paused
							if (this.isCancelled() || mVM.hasError() || mVM.Done() || isClosing())
								break;
						}

						//Continue to next OpCode
						driveVm(TomVM.VM_STEPS);

						// Poll for window events. The key callback above will only be
						// invoked during this call.
						glfwPollEvents();
					}
				}    //Program completed

				//Perform debugger callbacks
				if (mCallbacks != null) {
					int success;
					success = !mVM.hasError()
							? CallbackMessage.SUCCESS
							: CallbackMessage.FAILED;
					publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
							? "Program completed"
							: mVM.getError()));
				}

				//glfwSwapBuffers(m_window); // swap the color buffers
				//Keep window responsive until closed
				while (!this.isCancelled() && m_window != 0 && !isClosing()) {
					try {
						//Go easy on the processor
						Thread.sleep(10);
					} catch (InterruptedException e){}

					// Poll for window events. The key callback above will only be
					// invoked during this call.
					glfwPollEvents();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				synchronized (GLTextGridWindow.this) {

					//Free text grid image
					mTextGrid.destroy();

					// Release window and window callbacks
					glfwDestroyWindow(m_window);
					keyCallback.release();
					charCallback.release();
					ClearKeyBuffers();

					// Terminate GLFW and release the GLFWerrorfun
					glfwTerminate();
					errorCallback.release();
					//Clear pointer to window
					//An access violation will occur next time this window is launched if this isn't cleared
					m_window = 0;

					//Confirm this thread has completed before a new one can be executed
					if (mCompletionLatch != null)
						mCompletionLatch.countDown();
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
				e.printStackTrace();
				mVM.MiscError("An exception occured!");
			}

			// Check for error
			if (mVM.hasError() || mVM.Done() || isClosing()) {
				int success;
				if (mCallbacks != null) {
					success = !mVM.hasError()
							? CallbackMessage.SUCCESS
							: CallbackMessage.FAILED;
					publish(new CallbackMessage(success, success == CallbackMessage.SUCCESS
							? "Program completed"
							: mVM.getError()));
				}

				if (isClosing() || isFullscreen()) {
					hide();    //Stop program and close window
				}
				else {
					//TODO handle program completion options
					//stop(); //Just stop the worker thread;
				}

			}
		}



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
						"Do not support"},
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
	public List<String> getClassPathObjects(){
		return Arrays.asList("jar/lwjgl.jar");
	}
	@Override
	public List<String> getDependencies() {

		//Get settings
		Configuration config = getConfiguration();

		List<String> list = new ArrayList<String>();

		//Get supported platforms
		int windows = Integer.valueOf(config.getValue(GLTextGridWindow.SETTING_SUPPORT_WINDOWS));
		int mac 	= Integer.valueOf(config.getValue(GLTextGridWindow.SETTING_SUPPORT_MAC));
		int linux 	= Integer.valueOf(config.getValue(GLTextGridWindow.SETTING_SUPPORT_LINUX));

		//Common
		list.add("charset.png");
		list.add("jar/lwjgl.jar");

		//Windows
		if (windows == GLTextGridWindow.SUPPORT_WINDOWS_32_64 || windows == GLTextGridWindow.SUPPORT_WINDOWS_64) {
			//64-bit JOGL Windows libraries
			list.add("native/lwjgl.dll");
			list.add("native/OpenAL.dll");
			list.add("native/jemalloc.dll");
			list.add("native/glfw.dll");
		}
		if (windows == GLTextGridWindow.SUPPORT_WINDOWS_32_64 || windows == GLTextGridWindow.SUPPORT_WINDOWS_32) {
			//32-bit JOGL Windows libraries
			list.add("native/lwjgl32.dll");
			list.add("native/OpenAL32.dll");
			list.add("native/jemalloc32.dll");
			list.add("native/glfw32.dll");
		}
		//Mac
		if (mac == GLTextGridWindow.SUPPORT_MAC_32_64) {
			//Universal JOGL Mac libraries
			list.add("native/liblwjgl.dylib");
			list.add("native/libopenal.dylib");
			list.add("native/libjemalloc.dylib");
			list.add("native/libglfw.dylib");
		}
		//Linux
		if (linux == GLTextGridWindow.SUPPORT_LINUX_32_64 || linux == GLTextGridWindow.SUPPORT_LINUX_64) {
			//64-bit JOGL Linux libraries
			list.add("native/liblwjgl.so");
			list.add("native/libopenal.so");
			list.add("native/libjemalloc.so");
			list.add("native/libglfw.so");
		}
		if (linux == GLTextGridWindow.SUPPORT_LINUX_32_64 || linux == GLTextGridWindow.SUPPORT_LINUX_32) {
			//32-bit JOGL Linux libraries
			list.add("native/liblwjgl32.so");
			list.add("native/libopenal32.so");
			list.add("native/libjemalloc32.so");
			list.add("native/libglfw32.so");
		}

		return list;
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


}
