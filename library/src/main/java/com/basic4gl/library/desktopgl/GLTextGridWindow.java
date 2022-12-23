package com.basic4gl.library.desktopgl;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import com.basic4gl.compiler.LineNumberMapping;
import com.basic4gl.compiler.TomBasicCompiler;
import com.basic4gl.lib.util.*;
import com.basic4gl.compiler.util.IVMDriverAccess;
import com.basic4gl.library.debug.DebuggerCallbacksAdapter;
import com.basic4gl.runtime.Debugger;
import com.basic4gl.runtime.TomVM;
import com.basic4gl.lib.util.FunctionLibrary;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import javax.management.RuntimeMBeanException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;


public class GLTextGridWindow extends GLWindow implements IFileAccess {

	//Libraries
	private java.util.List<Library> mLibraries;
	private static GLTextGridWindow instance;

	private FileOpener mFiles;

	private TomBasicCompiler mComp;
	private TomVM mVM;
	private Thread mThread;	//Standalone

	private DebuggerCallbacks mDebugger;
	private DebuggerCallbacksAdapter debuggerCallback;

	private CountDownLatch completionLatch;
//	private TaskCallback mCallbacks;
	private CallbackMessage mMessage;
	private CallbackMessage mUpdates;

	// We need to strongly reference callback instances.
	private GLFWErrorCallback errorCallback;
	private GLFWKeyCallback keyCallback;
	private GLFWCharCallback charCallback;

	private boolean mClosing;


	GLTextGrid  mTextGrid;

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

	static final int MODE_WINDOWED = 0;
	static final int MODE_FULLSCREEN = 1;

	static final String CONFIG_FILE			= "config.ser";	//Filename for configuration file
	static final String STATE_FILE			= "state.bin";	//Filename for stored VM state

	private String mCharset = "charset.png"; //Default charset texture

	public void setCharsetPath(String path){ mCharset = path;}
	public String getCharsetPath(){ return mCharset;}
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

		//Load VM's state from file
//		String stateFile = "/Users/nate/Downloads/git/Basic4GL/javaDesktop/basicvm-nehe2";//args[0];// "/" + STATE_FILE
		String stateFile = args[0];// "/" + STATE_FILE
		String configFile = args[1];//"/" + CONFIG_FILE;
		String mappingFile = args[2];
		String currentDirectory = args[3];

		//JOptionPane.showMessageDialog(null, "Waiting...");
		instance = new GLTextGridWindow();

		LineNumberMapping lineNumberMapping = null;
		try (
				FileInputStream streamIn = new FileInputStream(mappingFile);
				ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
		) {
			lineNumberMapping = (LineNumberMapping) objectinputstream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Debugger debugger = new Debugger(lineNumberMapping);

//		instance.mFiles = new FileOpener(); //TODO load embedded files
		instance.mFiles = new FileOpener(""); //TODO load embedded files
		instance.mFiles.setParentDirectory("/Users/nate/Downloads/git/Basic4GL/javaDesktop/distribution");
		instance.mComp = new TomBasicCompiler(new TomVM(debugger));
		instance.mVM = instance.mComp.VM();
		instance.mLibraries = new ArrayList<>();
		//TODO Load libraries dynamically
		//TODO Save/Load list of libraries in order they should be added
		instance.mLibraries.add(new com.basic4gl.library.standard.Standard());
		instance.mLibraries.add(new com.basic4gl.library.standard.TrigBasicLib());
		instance.mLibraries.add(new com.basic4gl.library.standard.FileIOBasicLib());
		instance.mLibraries.add(new com.basic4gl.library.standard.WindowsBasicLib());
		instance.mLibraries.add(new com.basic4gl.library.desktopgl.JoystickBasicLib());
		instance.mLibraries.add(new com.basic4gl.library.desktopgl.TextBasicLib());
		instance.mLibraries.add(new com.basic4gl.library.desktopgl.OpenGLBasicLib());
		instance.mLibraries.add(new com.basic4gl.library.desktopgl.GLUBasicLib());
		instance.mLibraries.add(new com.basic4gl.library.desktopgl.GLBasicLib_gl());
		instance.mLibraries.add(new com.basic4gl.library.desktopgl.TomCompilerBasicLib());
//		instance.mLibraries.add(new com.basic4gl.library.desktopgl.SoundBasicLib());


		// Register library functions
		for (Library lib : instance.mLibraries) {
			//instance.mComp.AddConstants(lib.constants());
			if (lib instanceof FunctionLibrary)
				instance.mComp.AddFunctions(lib, ((FunctionLibrary) lib).specs());
		}

		System.out.println("par: " + currentDirectory);
		instance.mFiles.setParentDirectory(currentDirectory);

		try {
			System.out.println(stateFile);
			instance.loadState(new DataInputStream(new FileInputStream(stateFile)));
//			instance.loadState(instance.getClass().getResourceAsStream(stateFile));
		} catch (Exception ex){
			ex.printStackTrace();
			System.err.println("VM state could not be loaded");
		}
		//Load window configuration
		try {
		System.out.println(configFile);
			instance.loadConfiguration(new FileInputStream(configFile));
//			instance.loadConfiguration(instance.getClass().getResourceAsStream(configFile));
		} catch (Exception ex){
			ex.printStackTrace();
			System.err.println("Configuration file could not be loaded");
		}


		//Initialize file opener
		instance.mFiles = new FileOpener("");
		instance.mFiles = new FileOpener(currentDirectory);
		//Initialize window and setup VM
		instance.mVM.Pause();
		instance.mVM.Reset();
		instance.activate();


		instance.mMessage = new CallbackMessage();
		instance.debuggerCallback = new DebuggerCallbacksAdapter(
				instance.mMessage,
				debugger, // TODO add User Breakpoints to params
				instance,
				instance.mComp,
				instance.mVM);
		instance.debuggerCallback.connect();

		instance.mDebugger = new DebuggerCallbacks(instance.debuggerCallback, instance.mMessage, instance) {
			@Override
			public void onPreLoad() {

			}

			@Override
			public void onPostLoad() {

			}
		};
		instance.start(null);
	}

	@Override
	public String name() { return "GLFW Window";}

	@Override
	public String description() { return "Desktop application with OpenGL capabilities.";}

	@Override
	public void init(TomVM vm) {
		// TODO Auto-generated method stub

	}
	@Override
	public void init(TomBasicCompiler comp){

	}

	@Override
	public void cleanup() {
		if (debuggerCallback != null) {
			debuggerCallback.stop();
		}
	}

	@Override
	public void init(FileOpener files){
		mFiles = files;
	}


	public void pause(){
		mVM.Pause();
	}


	@Override
	public void reset() {

	}

	@Override
	public void activate() {
		mClosing = false;

		//Get settings
		if (mConfiguration == null)
			mConfiguration = getSettings();


	}

	@Override
	public void start(DebuggerCallbacks _) {
		mThread = new Thread(new VMThread(mDebugger));
		// TODO thread.start() has issues with initializing GL stuff off the main thread
		mThread.run();
	}

	@Override
	public void hide() {

	}

	@Override
	public void stop() {

	}


	@Override
	public boolean isFullscreen() {
		// TODO Auto-generated method stub
		return false;
	}



	private class VMThread implements Runnable {
		private final DebuggerCallbacks mDebugger;
		VMThread(DebuggerCallbacks debugger){
			mDebugger = debugger;
		}
		@Override
		public void run() {
			System.out.println("Running...");
			if (mVM == null) {
				return;    //TODO Throw exception
			}
			try {
				if (mDebugger != null) {
					mDebugger.onPreLoad();
				}
				mCharset = mFiles.FilenameForRead("charset.png", false);
				if (mDebugger != null) {
					mDebugger.onPostLoad();
				}
				onPreExecute();
				//Initialize libraries
				for (Library lib : mComp.getLibraries()) {
					initLibrary(lib);
				}
					//Debugger is not attached
				if (mDebugger == null) {
					while (!Thread.currentThread().isInterrupted() && !mVM.hasError() && !mVM.Done() && !isClosing()) {
						//Continue to next OpCode
						driveVM(TomVM.VM_STEPS);

						// Poll for window events. The key callback above will only be
						// invoked during this call.
						handleEvents();
					}   //Program completed
				}
				else	//Debugger is attached
				{
					while (!Thread.currentThread().isInterrupted() && !mVM.hasError() && !mVM.Done() && !isClosing()) {
						// Run the virtual machine for a certain number of steps
						mVM.PatchIn();

						if (mVM.Paused()) {
							//Breakpoint reached or paused by debugger
							System.out.println("VM paused");

							mDebugger.pause("Reached breakpoint");

							//Resume running
							if (mDebugger.getMessage().status == CallbackMessage.WORKING) {
								// Kick the virtual machine over the next op-code before patching in the breakpoints.
								// otherwise we would never get past a breakpoint once we hit it, because we would
								// keep on hitting it immediately and returning.
								mDebugger.message(driveVM(1));

								// Run the virtual machine for a certain number of steps
								mVM.PatchIn();
							}
							//Check if program was stopped while paused
							if (Thread.currentThread().isInterrupted() || mVM.hasError() || mVM.Done() || isClosing())
								break;
						}

						//Continue to next OpCode
						mDebugger.message(driveVM(TomVM.VM_STEPS));

						// Poll for window events. The key callback above will only be
						// invoked during this call.
						handleEvents();
					}   //Program completed
				}


				//Perform debugger callbacks
				int success;
				if (mDebugger != null) {
					success = !mVM.hasError()
							? CallbackMessage.SUCCESS
							: CallbackMessage.FAILED;
					mDebugger.message(new CallbackMessage(success, success == CallbackMessage.SUCCESS
							? "Program completed"
							: mVM.getError()));
				}
//
//				glMatrixMode(GL_MODELVIEW);
//				glLoadIdentity();
//
//				//Calculate the aspect ratio of the window
//				perspectiveGL(90.0f,(float)m_width/(float)m_height,0.1f,100.0f);
//
//				glShadeModel(GL_SMOOTH);
//				glClearDepth(1.0f);                         // Depth Buffer Setup
//				glEnable(GL_DEPTH_TEST);                        // Enables Depth Testing
//				glDepthFunc(GL_LEQUAL);                         // The Type Of Depth Test To Do
				//Keep window responsive until closed
				while (!Thread.currentThread().isInterrupted() && m_window != 0 && !isClosing()) {
					System.out.println("idle");
					try {
						//Go easy on the processor
						Thread.sleep(10);


//						glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
//
//						glLoadIdentity();//							' Reset The View
//
//						glColor3f(0.5f,0.5f,1.0f);//						' Set The Color To Blue One Time Only
//						glBegin(GL_QUADS);//							' Draw A Quad
//						glVertex3f(-1.0f, 1.0f, 0.0f);//					' Top Left
//						glVertex3f( 1.0f, 1.0f, 0.0f)	;//				' Top Right
//						glVertex3f( 1.0f,-1.0f, 0.0f);//					' Bottom Right
//						glVertex3f(-0.5f,-1.0f, 0.0f);//					' Bottom Left
//						glEnd();
//						glfwSwapBuffers(m_window);

						// Poll for window events. The key callback above will only be
						// invoked during this call.
						handleEvents();
					} catch (InterruptedException consumed){ break;}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				onFinally();
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
						"Windowed"},
						//"Fullscreen"}, temporarily disabled
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
		return Arrays.asList(
		        "jar/lwjgl.jar",
                "jar/lwjgl-opengl.jar",
                "jar/lwjgl-stb.jar",
                "jar/lwjgl-openal.jar",
                "jar/lwjgl-glfw.jar",
                "jar/lwjgl-assimp.jar",
                //Sound engine
                "jar/SoundSystem.jar",
                "jar/LibraryLWJGL3OpenAL.jar",
                "jar/CodecIBXM.jar",
                "jar/CodecJOrbis.jar",
                "jar/CodecWav.jar");
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
//		list.add("jar/lwjgl.jar");

        list.add("jar/lwjgl-assimp-natives-macos.jar");
        list.add("jar/lwjgl-assimp.jar");
        list.add("jar/lwjgl-glfw-natives-macos.jar");
        list.add("jar/lwjgl-glfw.jar");
        list.add("jar/lwjgl-natives-macos.jar");
        list.add("jar/lwjgl-openal-natives-macos.jar");
        list.add("jar/lwjgl-openal.jar");
        list.add("jar/lwjgl-opengl-natives-macos.jar");
        list.add("jar/lwjgl-opengl.jar");
        list.add("jar/lwjgl-stb-natives-macos.jar");
        list.add("jar/lwjgl-stb.jar");
        list.add("jar/lwjgl.jar");

		//Sound engine
		list.add("jar/SoundSystem.jar");
		list.add("jar/LibraryLWJGLOpenAL.jar");
		list.add("jar/CodecIBXM.jar");
		list.add("jar/CodecJOrbis.jar");
		list.add("jar/CodecWav.jar");

		//Windows
//		if (windows == GLTextGridWindow.SUPPORT_WINDOWS_32_64 || windows == GLTextGridWindow.SUPPORT_WINDOWS_64) {
//			//64-bit JOGL Windows libraries
//			list.add("native/lwjgl.dll");
//			list.add("native/OpenAL.dll");
//			list.add("native/jemalloc.dll");
//			list.add("native/glfw.dll");
//		}
//		if (windows == GLTextGridWindow.SUPPORT_WINDOWS_32_64 || windows == GLTextGridWindow.SUPPORT_WINDOWS_32) {
//			//32-bit lwjgl Windows libraries
//			list.add("native/lwjgl32.dll");
//			list.add("native/OpenAL32.dll");
//			list.add("native/jemalloc32.dll");
//			list.add("native/glfw32.dll");
//		}
//		//Mac
//		if (mac == GLTextGridWindow.SUPPORT_MAC_32_64) {
//			//Universal lwjgl Mac libraries
//			list.add("native/liblwjgl.dylib");
//			list.add("native/libopenal.dylib");
//			list.add("native/libjemalloc.dylib");
//			list.add("native/libglfw.dylib");
//		}
//		//Linux
//		if (linux == GLTextGridWindow.SUPPORT_LINUX_32_64) {
//			//lwjgl Linux libraries
//			list.add("native/liblwjgl.so");
//			list.add("native/libopenal.so");
//			list.add("native/libjemalloc.so");
//			list.add("native/libglfw.so");
//		}

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


	GLTextGrid getTextGrid ()                 { return mTextGrid; }
	void setTextGrid (GLTextGrid grid)    { mTextGrid = grid; }



	public CallbackMessage driveVM(int steps) {

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
			if (mDebugger != null) {
				success = !mVM.hasError()
						? CallbackMessage.SUCCESS
						: CallbackMessage.FAILED;
				return new CallbackMessage(success, success == CallbackMessage.SUCCESS
						? "Program completed"
						: mVM.getError());
			}

			if (isClosing() || isFullscreen()) {
				hide();    //Stop program and close window
			}
			else {
				//TODO handle program completion options
				//stop(); //Just stop the worker thread;
			}

		}
		return null;
	}
	public void initLibrary(Library lib){
		if (lib instanceof IVMDriverAccess){
			//Allows libraries to access VM driver/keep it responsive
			((IVMDriverAccess) lib).init(this);
		}
		if (lib instanceof IFileAccess){
			((IFileAccess) lib).init(mFiles);
		}
		if (lib instanceof IGLRenderer) {
			((IGLRenderer) lib).setTextGrid(mTextGrid);
			((IGLRenderer) lib).setWindow(GLTextGridWindow.this);
		}

		lib.init(mVM);
	}
	public boolean handleEvents(){

		//Keep window responsive during loops
		glfwPollEvents();
		return true; //all went well
	}
	public void onPreExecute(){
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
		mTextGrid = new GLSpriteEngine(mCharset, mFiles, 25, 40, 16, 16);
		if (mTextGrid.hasError())
			mVM.setError(mTextGrid.getError());
	}
	public void onPostExecute(){
//		glfwSwapBuffers(m_window); // swap the color buffers
		//Keep window responsive until closed
		while (!Thread.currentThread().isInterrupted() && m_window != 0 && !isClosing()) {
			try {
				//Go easy on the processor
				Thread.sleep(10);

			} catch (InterruptedException e){}

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

	public void onFinally(){
		synchronized (GLTextGridWindow.this) {
			//Do any library cleanup
			for(Library lib: mLibraries) {
				System.out.println("cleanup " + lib.name());
				lib.cleanup();
			}
			System.out.println("cleanup " + name());
			cleanup();

			//Free text grid image
			System.out.println("destroy textgrid");
			mTextGrid.destroy();

			// Release window and window callbacks
			System.out.println("destroy window");
			glfwDestroyWindow(m_window);

			System.out.println("destroy callbacks");
			keyCallback.free();
			charCallback.free();
			ClearKeyBuffers();

			// Terminate GLFW and release the GLFWerrorfun
			System.out.println("glfwTerminate");
			glfwTerminate();
			GLFWErrorCallback callback = glfwSetErrorCallback(null);
			if (callback != null) {
				callback.free();
			}
			errorCallback = null;//.release();
			//Clear pointer to window
			//An access violation will occur next time this window is launched if this isn't cleared
			m_window = 0;
		}
		System.out.println("exit");
	}


	public void    RecreateGLContext (){
		super.RecreateGLContext();

		if (mTextGrid != null)
			mTextGrid.UploadCharsetTexture();
	}

	public boolean isClosing(){
		try {
			synchronized (this) {
				return mClosing || (m_window != 0 && glfwWindowShouldClose(m_window));
			}
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public boolean isVisible() {
		try {
			synchronized (this) {
				return m_window != 0 && !glfwWindowShouldClose(m_window);
			}
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public void swapBuffers(){
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
			glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

			// Initialize GLFW. Most GLFW functions will not work before doing this.
			if (!glfwInit())
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
						glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
					}
					if (key < 0)
						return;
					if (action == GLFW_PRESS){
						if (key == GLFW_KEY_PAUSE)
							m_pausePressed = true;
						else {
							m_keyDown [key] |= 1;
							BufferScanKey ((char) key );
						}
					} else if (action == GLFW_RELEASE){
						m_keyDown [key] &= ~1;
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
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			// Center our window
			glfwSetWindowPos(
					m_window,
					(vidmode.width() - m_width) / 2,
					(vidmode.height() - m_height) / 2
			);
			// Get the thread stack and push a new frame
//			try ( MemoryStack stack = stackPush() ) {
//				IntBuffer pWidth = stack.mallocInt(1); // int*
//				IntBuffer pHeight = stack.mallocInt(1); // int*
//
//				// Get the window size passed to glfwCreateWindow
//				glfwGetWindowSize(m_window, pWidth, pHeight);
//
//				// Get the resolution of the primary monitor
//				GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//
//				// Center the window
//				glfwSetWindowPos(
//						m_window,
//						(vidmode.width() - pWidth.get(0)) / 2,
//						(vidmode.height() - pHeight.get(0)) / 2
//				);
//			} // the stack frame is popped automatically


			// Make the OpenGL context current
			glfwMakeContextCurrent(m_window);
			// Enable v-sync
			glfwSwapInterval(1);

			// Make the window visible
			glfwShowWindow(m_window);
//			int err = glGetError();
//			System.out.println(err);
		}
	}

}
