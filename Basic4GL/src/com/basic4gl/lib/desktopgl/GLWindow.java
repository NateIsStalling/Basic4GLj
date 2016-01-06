package com.basic4gl.lib.desktopgl;

import com.basic4gl.compiler.Constant;
import com.basic4gl.lib.util.Configuration;
import com.basic4gl.lib.util.Target;
import com.basic4gl.lib.util.TaskCallback;
import com.basic4gl.util.FuncSpec;
import com.basic4gl.util.IVMDriver;
import com.basic4gl.vm.HasErrorState;
import com.basic4gl.vm.TomVM;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwInit;

import org.lwjgl.opengl.*;

import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;

/**
 * Created by Nate on 8/26/2015.
 */
/*#include <windows.h>
        #include <gl\gl.h>
        #include <gl\glu.h>
        #include <gl\glext.h>                           // If this line wont compile, see OpenGLHeaderFiles\Readme.txt
        #include <string>
        #include "HasErrorState.h"
        #include "Misc.h"*/
public abstract class GLWindow extends HasErrorState implements Target, IVMDriver {
    static final double M_PI = 3.1415926535897932384626433832795;
    static final int WM_MOUSEWHEEL = 0x020A;
    static final int WHEEL_DELTA = 120;
    static final int GLWINDOWKEYBUFFER = 16;    // X character key buffer

    @Override
    public abstract Configuration getSettings();

    @Override
    public abstract Configuration getConfiguration();

    @Override
    public abstract void setConfiguration(Configuration config);

    @Override
    public abstract void loadConfiguration(InputStream stream) throws Exception;

    @Override
    public abstract void saveConfiguration(OutputStream stream) throws Exception;

    @Override
    public abstract void saveState(OutputStream stream) throws Exception;

    @Override
    public abstract void loadState(InputStream stream) throws Exception;

    @Override
    public abstract String name();

    @Override
    public abstract String description();

    @Override
    public abstract void init(TomVM vm);

    @Override
    public abstract List<String> getDependencies();


    enum ResetGLModeType {
        RGM_RESETSTATE(0),         // Explicitly reset all the OpenGL state (quite rigourous. some drivers/OpenGL setups can't handle this.)
        RGM_RECREATECONTEXT(1),     // Reuse the window, but re-create the OpenGL context (some drivers don't like this)
        RGM_RECREATEWINDOW(2);      // Destroy and re-create the entire window.

        private int mode;

        ResetGLModeType(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }
        }
    ////////////////////////////////////////////////////////////////////////////////
// Multitexturing
//

    // IMPORTANT!:
// This code will not compile if you have an older version of glext.h (as included
// in some older compilers).
//
// The file is in:					Exception e\include\gl
// You can get a replacement from:	www.opengl.org/resources/faq/technical/extensions.htm
    //PFNGLMULTITEXCOORD2FARBPROC glMultiTexCoord2f = null;
    //PFNGLMULTITEXCOORD2DARBPROC glMultiTexCoord2d = null;
    //PFNGLACTIVETEXTUREARBPROC glActiveTexture = null;
    /*
    HDC			m_HDC;              // Device context
    HGLRC       m_HGLRC;            // OpenGL rendering context
    HWND		m_HWnd;             // Window handle
    HINSTANCE	m_HInstance;        // Instance handle
    */
    long m_window;

    //long    m_HWnd;             // Window handle
    boolean m_active;           // True if window is active
    boolean m_focused;          // True if window has focus
    boolean m_visible;          // True if window is visible
    boolean m_fullScreen;       // True if fullscreen mode
    boolean m_border;           // True if window has border (windowed mode only)
    boolean m_allowResizing;    // True if window can be dragged and resized
    boolean m_fitToWorkArea;    // True if window should be fitted to the desktop work area
    int m_width, m_height;
    int m_bpp;              // (Fullscreen mode only)
    boolean m_stencil;          // True to enable stencil buffer
    String m_title;            // Window title
    double m_fov, m_fovX,
            m_nearClip, m_farClip;
    //DEVMODE m_screenSettings;    // Screen settings for fullscreen mode
    boolean m_closing;
    //TODO Possibly use Map structure instead of array for m_keyDown if it'd use less memory
    //Character.MAX_VALUE is used for array size to support GLFW keycodes that are in the 256+ range
    byte[] m_keyDown = new byte[Character.MAX_VALUE];        // Tracks key states
    int[] m_keyBuffer = new int[GLWINDOWKEYBUFFER];        // Queues key presses
    int[] m_scanKeyBuffer = new int[GLWINDOWKEYBUFFER];    // Queuse scan keys
    int m_bufStart, m_bufEnd, m_scanBufStart, m_scanBufEnd;
    boolean m_dontPaint;        // If false (default), WM_PAINT messages will cause a SwapBuffers () call. Otherwise they wontException e
    boolean m_painting;
    boolean m_pausePressed;
    int m_mouseX, m_mouseY;
    boolean[] m_mouseButton = new boolean[3];  // 0 = Left, 1 = Right, 2 = Middle
    int m_mouseWheel, m_mouseWheelDelta;
    boolean m_mouseCentred;
    // Used to detect the first time someone uses the MouseXD() or MouseYD() methods.
    // These methods work by placing the mouse cursor in the middle of the
    // window and measuring the distance it moves from that point.
    // On the first call they simply place the cursor in the centre and
    // return 0.
    boolean m_showingCursor;
    ResetGLModeType m_resetGLMode;

    void IncStart() {
        m_bufStart = (++m_bufStart) % GLWINDOWKEYBUFFER;
    }

    void IncEnd() {
        m_bufEnd = (++m_bufEnd) % GLWINDOWKEYBUFFER;
    }

    void IncScanStart() {
        m_scanBufStart = (++m_scanBufStart) % GLWINDOWKEYBUFFER;
    }

    void IncScanEnd() {
        m_scanBufEnd = (++m_scanBufEnd) % GLWINDOWKEYBUFFER;
    }

    void PositionMouse(){
        try {
            new Robot().mouseMove(m_mouseX, m_mouseY);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    void BufferScanKey(int key){
        int end = m_scanBufEnd;
        IncScanEnd ();
        if (m_scanBufEnd != m_scanBufStart)
            m_scanKeyBuffer [end] = key;
        else
            m_scanBufEnd = end;
    }


    protected void ResizeGLScene(int width, int height) // Resize the scene in response to a window resize
    {

        // Sanity checks
        if (height < 10)
            height = 10;
        if (width < 10)
            width = 10;

        m_width = width;
        m_height = height;

        if (height == 0)                                        // Prevent A Divide By Zero By
        {
            height = 1;                                        // Making Height Equal One
        }

        // Calculate the field of view on the XZ plane
        m_fovX = (180 / M_PI) * 2 * Math.atan(((double) width / height) * Math.tan(m_fov / 2 * (M_PI / 180)));

/*    m_width = width;
    m_height = height;
	glViewport(0,0,m_width,m_height);  					// Reset The Current Viewport
	glMatrixMode(GL_PROJECTION);						// Select The Projection Matrix
	glLoadIdentity();									// Reset The Projection Matrix

	// Calculate The Aspect Ratio Of The Window
	gluPerspective(m_fov,(GLfloat)m_width/(GLfloat)m_height, m_nearClip, m_farClip);

	glMatrixMode(GL_MODELVIEW);							// Select The Modelview Matrix
	glLoadIdentity();									// Reset The Modelview Matrix*/
    }


    protected void KillWindow(){

        // Close the window
        Hide ();

        // Must destroy OpenGL context first
        KillGLContext();

        // Delete the window
        /*glWindows.erase (m_HWnd);           // Remove from window list
        if (m_HDC) {
            ReleaseDC(m_HWnd,m_HDC);
            m_HDC = 0;
        }*/
        if (m_window != 0) {
            glfwDestroyWindow(m_window);
            m_window = 0;
        }
    }

    protected void KillGLContext(){

        // Destroy the OpenGL context
        glfwTerminate(); //TODO confirm this destroys the context
        /*if (m_HGLRC)
        {
            wglMakeCurrent(NULL,NULL);
            wglDeleteContext(m_HGLRC);
            m_HGLRC = 0;
        }*/
    }

    protected void DoShowCursor() {
        if (!m_showingCursor) {
            //TODO Show cursor
            //ShowCursor(true);
            m_showingCursor = true;
        }
    }

    protected void DoHideCursor() {
        if (m_showingCursor && (m_fullScreen || m_mouseCentred)) {
            //TODO hide cursor
            //ShowCursor(false);
            m_showingCursor = false;
        }
    }

    public GLWindow(boolean fullScreen,
                    boolean border,
                    int width,          // Note: If width = 0, will use screen width
                    int height,
                    int bpp,
                    boolean stencil,
                    String title,
                    boolean allowResizing,
                    boolean fitToWorkArea){
        this(fullScreen,border,width,height, bpp, stencil,title,allowResizing,fitToWorkArea,ResetGLModeType.RGM_RECREATEWINDOW);
    }
    public GLWindow(boolean fullScreen,
                    boolean border,
                    int width,          // Note: If width = 0, will use screen width
                    int height,
                    int bpp,
                    boolean stencil,
                    String title,
                    boolean allowResizing,
                    boolean fitToWorkArea,
                    ResetGLModeType resetGLMode) {

        m_resetGLMode = resetGLMode;

        // Null default values
        // If constructor is aborted, then destructor wont try to deallocate garbage handles.
        m_active = false;
        m_focused = false;
        m_visible = false;
        m_closing = false;
        m_bufStart = 0;
        m_bufEnd = 0;
        m_scanBufStart = 0;
        m_scanBufEnd = 0;
        m_showingCursor = true;

        // Clear key buffers
        ClearKeyBuffers();

        // Defaults
        m_fov = 60;
        m_nearClip = 1;
        m_farClip = 1000;
        m_painting = false;
        m_dontPaint = false;
        m_pausePressed = false;
        m_mouseX = 0;
        m_mouseY = 0;
        m_mouseButton[0] = false;
        m_mouseButton[1] = false;
        m_mouseButton[2] = false;
        m_mouseWheel = 0;
        m_mouseWheelDelta = 0;
        m_mouseCentred = false;
/*
        m_HInstance = GetModuleHandle(NULL);                // Grab An Instance For Our Window

        // Create open GL window.
        // Note:    Caller should check error state after construction. If error set,
        //          then the window is probably unusable
        WNDCLASS wc;                        // Windows Class Structure
        wc.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;    // Redraw On Size, And Own DC For Window.
        wc.lpfnWndProc = (WNDPROC)::WndProc;                    // WndProc Handles Messages
        wc.cbClsExtra = 0;                                    // No Extra Window Data
        wc.cbWndExtra = 0;                                    // No Extra Window Data
        wc.hInstance = m_HInstance;                            // Set The Instance
        wc.hIcon = LoadIcon(NULL, IDI_WINLOGO);            // Load The Default Icon
        wc.hCursor = LoadCursor(NULL, IDC_ARROW);            // Load The Arrow Pointer
        wc.hbrBackground = NULL;                                    // No Background Required For GL
        wc.lpszMenuName = NULL;                                    // We Don't Want A Menu
        wc.lpszClassName = "gbOpenGL";                        // Set The Class Name

        if (!RegisterClass( & wc))                                    // Attempt To Register The Window Class
        {
            setError("Window class registration failed");
            return;
        }

        RecreateWindow(fullScreen, border, width, height, bpp, stencil, title, allowResizing, fitToWorkArea);
        */
    }

    public boolean ResetGL(){

        /*if (m_resetGLMode == ResetGLModeType.RGM_RECREATEWINDOW) {
            RecreateWindow(m_fullScreen, m_border, m_width, m_height, m_bpp, m_stencil, m_title, m_allowResizing, m_fitToWorkArea);
            return true;
        }
        else if (m_resetGLMode ==  ResetGLModeType.RGM_RECREATECONTEXT) {
            RecreateGLContext ();
            return true;
        }*/

        // Setup OpenGL defaults.
        // This should reset as much as possible back to the initial state of OpenGL.

        // Exceptions:
        //      * Projection matrix is initialised to a perspective transform
        int i;
        // End current gl block
        try{ GL11.glEnd ();                                                             } catch (Exception e) {
        e.printStackTrace();}
        m_dontPaint = false;

        // Intialise matrices
        try {
            GL11.glMatrixMode (GL11.GL_PROJECTION);   ClearGLMatrix ();                     } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glMatrixMode (GL11.GL_TEXTURE);      ClearGLMatrix ();                     } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glMatrixMode (GL11.GL_MODELVIEW);    ClearGLMatrix ();                     } catch (Exception e) {
            e.printStackTrace();}

        // Initialise state

        try {
            GL11.glColor4f (1f, 1f, 1f, 1f);
        } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glIndexi (1);                                                         } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glTexCoord4f (0f, 0f, 0f, 1f);                                            } catch (Exception e) {
            e.printStackTrace();}
        try { GL11.glNormal3f (0f, 0f, 1f);                                                 } catch (Exception e) {
            e.printStackTrace();}
//    try{ GL11.glRasterPos4f (0, 0, 0, 1);                                           } catch (Exception e) { ; }
        try {
            GL11.glEdgeFlag (true);                                                 } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_VERTEX_ARRAY);                                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_NORMAL_ARRAY);                                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_COLOR_ARRAY);                                           } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_INDEX_ARRAY);                                           } catch (Exception e) {
            e.printStackTrace(); }
        try {
            GL11.glDisable(GL11.GL_TEXTURE_COORD_ARRAY);                                   } catch (Exception e) {
        }
        try {
            GL11.glDisable (GL11.GL_EDGE_FLAG_ARRAY);                                       } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glDepthRange (0, 1);                                                  } catch (Exception e) {
            e.printStackTrace(); }
        try {
            GL11.glDisable (GL11.GL_NORMALIZE);                                             } catch (Exception e) {
            e.printStackTrace();}
        for (i = 0; i < GL11.GL_MAX_CLIP_PLANES; i++)
            try {
                GL11.glDisable (GL11.GL_CLIP_PLANE0 + i);                                   } catch (Exception e) {
                e.printStackTrace(); }
        FloatBuffer fog = BufferUtils.createFloatBuffer(4).put(new float[]{0f, 0f, 0f, 0f});
        fog.rewind();
        try {
            GL11.glFogfv (GL11.GL_FOG_COLOR, fog);                                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glFogi (GL11.GL_FOG_INDEX, 0);                                             } catch (Exception e) {
            e.printStackTrace(); }
        try {
            GL11.glFogf (GL11.GL_FOG_DENSITY, 1.0f);                                         } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glFogf (GL11.GL_FOG_START, 0.0f);                                           } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glFogf (GL11.GL_FOG_END, 1.0f);                                             } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);                                         } catch (Exception e) {
            e.printStackTrace();}
        try { GL11.glDisable (GL11.GL_FOG);                                                   } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glShadeModel (GL11.GL_SMOOTH);                                             } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable(GL11.GL_LIGHTING);                                              } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable(GL11.GL_COLOR_MATERIAL);                                        } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);          } catch (Exception e) {
            e.printStackTrace();}

        FloatBuffer ambient   = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.2f, 0.2f, 0.2f, 1.0f }),
                diffuse   = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.8f, 0.8f, 0.8f, 1.0f }),
                specular  = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.0f, 0.0f, 0.0f, 1.0f }),
                emission  = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.0f, 0.0f, 0.0f, 1.0f }),
                shininess = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.0f, 0.0f, 0.0f, 0.0f });
        ambient.rewind();
        diffuse.rewind();
        specular.rewind();
        emission.rewind();
        shininess.rewind();
        try {
            GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT,    ambient);             } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE,    diffuse);             } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR,  specular);             } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_EMISSION,   emission);            } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glMaterialfv(GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS,  shininess);           } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, ambient);                     } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glLightModeli(GL11.GL_LIGHT_MODEL_LOCAL_VIEWER, GL11.GL_FALSE);                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glLightModeli(GL11.GL_LIGHT_MODEL_TWO_SIDE, GL11.GL_FALSE);                    } catch (Exception e) {
            e.printStackTrace();}
        FloatBuffer lambient      = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.0f, 0.0f, 0.0f, 1.0f }),
                ldiffuse0     = BufferUtils.createFloatBuffer(4).put(new float[]{ 1.0f, 1.0f, 1.0f, 1.0f }),
                ldiffuse1     = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.0f, 0.0f, 0.0f, 1.0f }),
                lspecular0    = BufferUtils.createFloatBuffer(4).put(new float[]{ 1.0f, 1.0f, 1.0f, 1.0f }),
                lspecular1    = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.0f, 0.0f, 0.0f, 1.0f }),
                lposition     = BufferUtils.createFloatBuffer(4).put(new float[]{ 0.0f, 0.0f, 1.0f, 0.0f });
        lambient.rewind();
        ldiffuse0.rewind();
        ldiffuse1.rewind();
        lspecular0.rewind();
        lspecular1.rewind();
        lposition.rewind();
        for (i = 0; i < 8; i++) {
            try {
                GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_AMBIENT, lambient);                              } catch (Exception e) {
                e.printStackTrace();}
            try {
                GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, i == 0 ? ldiffuse0 : ldiffuse1);        } catch (Exception e) {
                e.printStackTrace();}
            try {
                GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, i == 0 ? lspecular0 : lspecular1);     } catch (Exception e) {
                e.printStackTrace();}
            try {
                GL11.glLightfv(GL11.GL_LIGHT0 + i, GL11.GL_POSITION, lposition);                            } catch (Exception e) {
                e.printStackTrace();}
            try {
                GL11.glLightf(GL11.GL_LIGHT0 + i, GL11.GL_SPOT_EXPONENT, 0.0f);                              } catch (Exception e) {
                e.printStackTrace();}
            try {
                GL11.glLightf(GL11.GL_LIGHT0 + i, GL11.GL_CONSTANT_ATTENUATION, 1.0f);                       } catch (Exception e) {
                e.printStackTrace();}
            try {
                GL11.glLightf(GL11.GL_LIGHT0 + i, GL11.GL_LINEAR_ATTENUATION, 0.0f);                         } catch (Exception e) {
                e.printStackTrace();}
            try {
                GL11.glLightf(GL11.GL_LIGHT0 + i, GL11.GL_QUADRATIC_ATTENUATION, 0.0f);                      } catch (Exception e) {
                e.printStackTrace();}
            try {
                GL11.glDisable (GL11.GL_LIGHT0 + i);                                                    } catch (Exception e) {
                e.printStackTrace();}
        }

        try{ GL11.glPointSize (1.0f);                                                    } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_POINT_SMOOTH);                                          } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glLineWidth (1.0f);                                                    } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_LINE_SMOOTH);                                           } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glLineStipple (1, (short)0xffff);                                            } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_LINE_STIPPLE);                                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_CULL_FACE);                                             } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glCullFace (GL11.GL_BACK);                                                 } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glFrontFace (GL11.GL_CCW);                                                 } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_POLYGON_SMOOTH);                                        } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPolygonMode (GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);                           } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_TEXTURE_1D);                                            } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_TEXTURE_2D);                                            } catch (Exception e) {
            e.printStackTrace();}

        try {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST); } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);                    } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);                    } catch (Exception e) {
            e.printStackTrace(); }
        FloatBuffer texBorder = BufferUtils.createFloatBuffer(4).put(new float[]{0f, 0f, 0f, 0f});
        texBorder.rewind();
        try {
            GL11.glTexParameterfv(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_BORDER_COLOR, texBorder);             } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_TEXTURE_GEN_T);                                                     } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_TEXTURE_GEN_S);                                                     } catch (Exception e) {
            e.printStackTrace(); }
        try {
            GL11.glDisable (GL11.GL_TEXTURE_GEN_R);                                                     } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_TEXTURE_GEN_Q);                                                     } catch (Exception e) {
            e.printStackTrace();}
        for (i = 0; i < 4; i++) {
            int coord = 0;
            switch (i) {
                case 0: coord = GL11.GL_T; break;
                case 1: coord = GL11.GL_S; break;
                case 2: coord = GL11.GL_R; break;
                case 3: coord = GL11.GL_Q; break;
            }
            try {
                GL11.glTexGeni(coord, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);            } catch (Exception e) {
                e.printStackTrace(); }
        }

        try {
            GL11.glDisable (GL11.GL_SCISSOR_TEST);                                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_ALPHA_TEST);                                            } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glAlphaFunc (GL11.GL_ALWAYS, 0);                                           } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glDisable (GL11.GL_STENCIL_TEST);                                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glStencilFunc (GL11.GL_ALWAYS, 0, 0xffffffff);                             } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glStencilOp (GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);                              } catch (Exception e) {
            e.printStackTrace(); }
        try {
            GL11.glDisable (GL11.GL_DEPTH_TEST);                                            } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glDepthFunc (GL11.GL_LESS);                                                } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glDisable (GL11.GL_BLEND);                                                 } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glBlendFunc (GL11.GL_ONE, GL11.GL_ZERO);                                        } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glDrawBuffer (GL11.GL_BACK);                                               } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glColorMask(true, true, true, true);                     } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glDepthMask (true);                                                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glClearAccum (0, 0, 0, 0);                                            } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glClearColor (0, 0, 0, 0);                                            } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glClearDepth (1);                                                     } catch (Exception e) {
            e.printStackTrace();}
        try{ GL11.glClearIndex (0);                                                     } catch (Exception e) {
            e.printStackTrace(); }
        try{ GL11.glClearStencil (0);                                                   } catch (Exception e) {
            e.printStackTrace();}

        try {
            GL11.glPixelStorei(GL11.GL_PACK_SWAP_BYTES, GL11.GL_FALSE);                         } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStorei(GL11.GL_PACK_LSB_FIRST, GL11.GL_FALSE);                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStoref (GL11.GL_PACK_ROW_LENGTH, 0);                                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStoref (GL11.GL_PACK_SKIP_PIXELS, 0);                               } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStorei (GL11.GL_PACK_ALIGNMENT, 4);                                 } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, GL11.GL_FALSE);                       } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, GL11.GL_FALSE);                        } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStoref (GL11.GL_UNPACK_ROW_LENGTH, 0);                              } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStoref (GL11.GL_UNPACK_SKIP_PIXELS, 0);                             } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelStorei (GL11.GL_UNPACK_ALIGNMENT, 4);                               } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferi (GL11.GL_MAP_COLOR, GL11.GL_FALSE);                            } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferi (GL11.GL_MAP_STENCIL, GL11.GL_FALSE);                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferi (GL11.GL_INDEX_SHIFT, 0);                                 } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferi (GL11.GL_INDEX_OFFSET, 0);                                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_RED_SCALE, 1.0f);                                 } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_GREEN_SCALE, 1.0f);                               } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_BLUE_SCALE, 1.0f);                                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_ALPHA_SCALE, 1.0f);                               } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_DEPTH_SCALE, 1.0f);                               } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_RED_BIAS, 0.0f);                                  } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_GREEN_BIAS, 0.0f);                                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_BLUE_BIAS, 0.0f);                                 } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_ALPHA_BIAS, 0.0f);                                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glPixelTransferf (GL11.GL_DEPTH_BIAS, 0.0f);                                } catch (Exception e) {
            e.printStackTrace();}

        try {
            GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_DONT_CARE);                } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_DONT_CARE);                          } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);                           } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE);                        } catch (Exception e) {
            e.printStackTrace();}
        try {
            GL11.glHint (GL11.GL_FOG_HINT, GL11.GL_DONT_CARE);                                   } catch (Exception e) {
            e.printStackTrace();}

        // Multitexturing
        if (ExtensionSupported ("GL_ARB_multitexture")) {

            // Disable texturing for all texture units
            IntBuffer units = BufferUtils.createIntBuffer(1);
            units.rewind();
            try {
                GL11.glGetIntegerv (ARBMultitexture.GL_MAX_TEXTURE_UNITS_ARB, units);                 } catch (Exception e) {
                e.printStackTrace();}
            int u = units.get();
            for (int k = 0; k < u; k++) {
                //TODO look into glActiveTexture
                GL13.glActiveTexture (ARBMultitexture.GL_TEXTURE0_ARB + i);

                try {
                    GL11.glDisable (GL11.GL_TEXTURE_2D);                                    } catch (Exception e) {
                    e.printStackTrace();}
                try {
                    GL11.glDisable (GL11.GL_TEXTURE_1D);                                    } catch (Exception e) {
                    e.printStackTrace();}
                if (k == 0) try {
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE); } catch (Exception e) {
                    e.printStackTrace();}
                else try {
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);    } catch (Exception e) {
                    e.printStackTrace();}
            }
            try {
            GL13.glActiveTexture(ARBMultitexture.GL_TEXTURE0_ARB);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // Setup OpenGL defaults
        OpenGLDefaults ();

        return true;									// Initialization Went OK
    }

    public void OpenGLDefaults(){

        // Setup some 'nice' defaults
        ResizeGLScene(m_width, m_height);                  // Projection matrix

        // Setup a default viewport
        GL11.glViewport(0, 0, m_width, m_height);  					// Reset The Current Viewport

        // Set some default OpenGL matrices. Basic 3D perspective projection
        GL11.glMatrixMode(GL11.GL_PROJECTION);						// Select The Projection Matrix
        GL11.glLoadIdentity();									// Reset The Projection Matrix
        perspectiveGL(m_fov, (float) m_width / (float) m_height, m_nearClip, m_farClip);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);							// Select The Modelview Matrix
        GL11.glLoadIdentity();									// Reset The Modelview Matrix
        try {
            GL11.glShadeModel(GL11.GL_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            GL11.glClearDepth(1.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            GL11.glDepthFunc(GL11.GL_LEQUAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
        } catch (Exception e) {
            e.printStackTrace();
        }

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        SwapBuffers ();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
    }
/*
    public boolean ProcessWindowsMessages(){
        MSG msg;								// Windows Message Structure
        while 	(PeekMessage (msg, null, 0, 0, PM_REMOVE)) {
            if (msg.message==WM_QUIT)				// Have We Received A Quit Message?
            {
                return true;
            }
            else									// If Not, Deal With Window Messages
            {
                TranslateMessage(msg);				// Translate The Message
                DispatchMessage(msg);				// Dispatch The Message
            }
        }
        return false;
    }*/


    public void RecreateWindow(boolean fullScreen, boolean border, int width, int height, int bpp, boolean stencil, String title, boolean allowResizing, boolean fitToWorkArea){

        // Save window settings
        m_fullScreen    = fullScreen;
        m_border        = border;
        m_width         = width;
        m_height        = height;
        m_bpp           = bpp;
        m_title         = title;
        m_allowResizing = allowResizing;
        m_fitToWorkArea = fitToWorkArea;
        m_stencil       = stencil;

        // Delete existing window
        KillWindow ();

        //TODO Create window here
        /*
        // Create new one
        GLuint		PixelFormat;			// Holds The Results After Searching For A Match
        DWORD		dwExStyle;				// Window Extended Style
        DWORD		dwStyle;				// Window Style
        RECT		WindowRect;				// Grabs Rectangle Upper Left / Lower Right Values
        WindowRect.left     =(long)0;			// Set Left Value To 0
        WindowRect.right    =(long)m_width;		// Set Right Value To Requested Width
        WindowRect.top      =(long)0;			// Set Top Value To 0
        WindowRect.bottom   =(long)m_height;	// Set Bottom Value To Requested Height

        if (m_fullScreen)												// Attempt Fullscreen Mode?
        {
            memset(&m_screenSettings,0,sizeof(m_screenSettings));	// Makes Sure Memory's Cleared
            m_screenSettings.dmSize=sizeof(m_screenSettings);		// Size Of The Devmode Structure
            m_screenSettings.dmPelsWidth	= m_width;				// Selected Screen Width
            m_screenSettings.dmPelsHeight	= m_height;				// Selected Screen Height
            m_screenSettings.dmBitsPerPel	= m_bpp;				// Selected Bits Per Pixel
            m_screenSettings.dmFields=DM_PELSWIDTH|DM_PELSHEIGHT;
            if (m_bpp)
                m_screenSettings.dmFields |= DM_BITSPERPEL;
        }

        if (m_fullScreen || !m_border)									// Are We Still In Fullscreen Mode?
        {
            // Borderless window
            dwExStyle=WS_EX_APPWINDOW;
            dwStyle=WS_POPUP;
        }
        else {
            // Standard window
            dwExStyle=WS_EX_APPWINDOW | WS_EX_WINDOWEDGE;			        // Window Extended Style
            if (allowResizing)
                dwStyle = WS_OVERLAPPEDWINDOW;
            else
                dwStyle = WS_OVERLAPPEDWINDOW & ~(WS_SIZEBOX | WS_MAXIMIZEBOX);	    // Windows Style
            if (m_fitToWorkArea)
                dwStyle |= WS_MAXIMIZE;
        }

        if (!m_fullScreen && !m_fitToWorkArea) {

            // Get the screen resolution
            HWND desktop = GetDesktopWindow ();
            RECT desktopRect;
            GetWindowRect (desktop, &desktopRect);
            int xoffs, yoffs;
            if (WindowRect.right > desktopRect.right)
                xoffs = -WindowRect.left;
            else
                xoffs = (desktopRect.right  - WindowRect.right)  / 2;
            if (WindowRect.bottom > desktopRect.bottom)
                yoffs = -WindowRect.top;
            else
                yoffs = (desktopRect.bottom - WindowRect.bottom) / 2;
            WindowRect.left     += xoffs;
            WindowRect.right    += xoffs;
            WindowRect.top      += yoffs;
            WindowRect.bottom   += yoffs;

            // Adjust window size to allow for border
            if (m_border)
                AdjustWindowRectEx(&WindowRect, dwStyle, false, dwExStyle);
        }

        // Fitting to work area?
        if (m_fitToWorkArea) {
            SystemParametersInfo(SPI_GETWORKAREA, 0, &WindowRect, 0);    // Gets the desktop work area
            m_width = WindowRect.right - WindowRect.left;
            m_height = WindowRect.bottom - WindowRect.top;
        }

        // Create The Window
        if (!(m_HWnd=CreateWindowEx(	dwExStyle,				    		// Extended Style For The Window
                "gbOpenGL",			    			// Class Name
                m_title.c_str (),   				// Window Title
                dwStyle |							// Defined Window Style
                        WS_CLIPSIBLINGS |					// Required Window Style
                        WS_CLIPCHILDREN,					// Required Window Style
                WindowRect.left, WindowRect.top,	// Window Position
                WindowRect.right-WindowRect.left,	// Calculate Window Width
                WindowRect.bottom-WindowRect.top,	// Calculate Window Height
                NULL,								// No Parent Window
                NULL,								// No Menu
                m_HInstance,						// Instance
                NULL)))								// Dont Pass Anything To WM_CREATE
        {
            setError ("Window creation error");
            return;
        }

        // Register window (for window procedure)
        glWindows [m_HWnd] = this;

        */
        //TODO enable stencil buffer
        /*
        // Stencil buffer depth
        int stencilBits = m_stencil ? 8 : 0;

        static	PIXELFORMATDESCRIPTOR pfd=				// pfd Tells Windows How We Want Things To Be
                {
                        sizeof(PIXELFORMATDESCRIPTOR),				// Size Of This Pixel Format Descriptor
                        1,											// Version Number
                        PFD_DRAW_TO_WINDOW |						// Format Must Support Window
                                PFD_SUPPORT_OPENGL |						// Format Must Support OpenGL
                                PFD_DOUBLEBUFFER,
                        PFD_TYPE_RGBA,								// Request An RGBA Format
                        m_bpp,										// Select Our Color Depth
                        0, 0, 0, 0, 0, 0,							// Color Bits Ignored
                        0,											// No Alpha Buffer
                        0,											// Shift Bit Ignored
                        0,											// No Accumulation Buffer
                        0, 0, 0, 0,									// Accumulation Bits Ignored
                        16,											// 16Bit Z-Buffer (Depth Buffer)
                        stencilBits, 								// No Stencil Buffer
                        0,											// No Auxiliary Buffer
                        PFD_MAIN_PLANE,								// Main Drawing Layer
                        0,											// Reserved
                        0, 0, 0										// Layer Masks Ignored
                };

        if (!(m_HDC=GetDC(m_HWnd)))					 	// Did We Get A Device Context?
        {
            SetError ("Failed to get window device context");
            return;
        }

        if (!(PixelFormat=ChoosePixelFormat(m_HDC,&pfd)))	// Did Windows Find A Matching Pixel Format?
        {
            SetError ("Could not find a suitable pixel format");
            return;
        }

        // Determine whether pixel format will be hardware accellerated.

        PIXELFORMATDESCRIPTOR pfd_new;
        DescribePixelFormat (m_HDC, PixelFormat, sizeof (PIXELFORMATDESCRIPTOR), &pfd_new);

        if ((pfd_new.dwFlags & PFD_GENERIC_FORMAT) != 0					// Generic
                &&  (pfd_new.dwFlags & PFD_GENERIC_ACCELERATED) == 0) {			// Non accellerated

            // Warn user that OpenGL will proceed in software mode!
            if (MessageBox(null, "Hardware 3D acceleration is not available for this display mode.\nProceed in software mode?",
                    "Warning",
                    MB_YESNO|MB_ICONEXCLAMATION) == IDNO) {
                setError ("Aborted");
                return;
            }
        }

        if(!SetPixelFormat(m_HDC,PixelFormat,&pfd))		// Are We Able To Set The Pixel Format?
        {
            setError ("Set pixel format failed");
            return;
        }*/

        // Setup OpenGL
        RecreateGLContext ();
    }

    //virtual
    public void RecreateGLContext(){

        // Delete existing context
        KillGLContext ();

        // Create main context

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (glfwInit() != GL11.GL_TRUE)
            throw new IllegalStateException("Unable to initialize GLFW");

        //TODO Make context current
        /*
        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        */

        // Multitexture extension pointers may not be valid anymore, so re-fetch them
        InitMultitexture ();

        // Setup OpenGL defaults
        OpenGLDefaults ();
    }

    // Settings
    public int Width() {
        return m_width;
    }

    public int Height() {
        return m_height;
    }

    public int Bpp() {
        return m_bpp;
    }

    public boolean FullScreen() {
        return m_fullScreen;
    }

    //return double&
    public double FOV() {
        return m_fov;
    }

    public double FOVX() {
        return m_fovX;
    }

    //return double&
    public double NearClip() {
        return m_nearClip;
    }

    //return double&
    public double FarClip() {
        return m_farClip;
    }

    public String Title() {
        return m_title;
    }

    public ResetGLModeType ResetGLMode() {
        return m_resetGLMode;
    }

    // Resources

    // (At least some of these handles are recreated when the OpenGL settings are
    // initialised. Calling code shouldn't rely on them staying constant.)
    /*HDC         GetHDC ()       { return m_HDC; }
    HGLRC       GetHGLRC ()     { return m_HGLRC; }
    HWND        GetHWND ()      { return m_HWnd; }
    HINSTANCE   GetHINSTANCE () { return m_HInstance; }*/

    // Active state
    public boolean Active() {
        return m_active;
    }
/*
    public void Activate(){
        if (!m_visible)
            Show ();
        else {
            SetForegroundWindow(m_HWnd);						// Slightly Higher Priority
            SetFocus(m_HWnd);									// Sets Keyboard Focus To The Window
        }
    }*/

    public boolean Focused() {
        return m_focused;
    }
/*
    public boolean Show()            // Show window. Will switch to window if in fullscreen mode
    {

        if (m_visible)
            return true;

        // Attempt to set fullscreen mode
        boolean result = true;

        if (m_fullScreen) {
            result = ChangeDisplaySettings(m_screenSettings,CDS_FULLSCREEN)==DISP_CHANGE_SUCCESSFUL;
            if (result)
                DoHideCursor ();
        }
        if (result) {
            ShowWindow(m_HWnd,SW_RESTORE);	    			    // Show the window
            SetForegroundWindow(m_HWnd);						// Slightly Higher Priority
            SetFocus(m_HWnd);									// Sets Keyboard Focus To The Window
            m_visible = true;
        }
        return result;
    }*/
    public void Hide()            // Hide window. Switch back from fullscreen (if fullscreen mode)
    {
        DoShowCursor ();
        if (!m_visible)
            return;
        glfwHideWindow(m_window); 		    			    // Hide the window
        //ProcessWindowsMessages ();
        m_visible = false;
        //TODO Exit fullscreen mode
        //if (m_fullScreen)									// Are We In Fullscreen Mode?
        //    glfwChangeDisplaySettings(null,0);					// If So Switch Back To The Desktop
    }

    public boolean Visible() {
        return m_visible;
    }

    public boolean Closing() {
        return m_closing;
    }

    public void SetClosing(boolean value) {
        m_closing = value;
    }


    // Keyboard handling
    public int getKey()          // Pulls a key from the keyboard buffer. Returns 0 if none waiting.
    {

        // Flush any keypress messages
        //ProcessWindowsMessages ();

        // Check for buffered keypress
        if (m_bufStart == m_bufEnd)
            return 0;

        // Extract and return it
        int result = m_keyBuffer [m_bufStart];
        IncStart ();
        return result;
    }
    public int getScanKey()
    {

        // Flush any keypress messages
        //ProcessWindowsMessages ();

        // Check for buffered keypress
        if (m_scanBufStart == m_scanBufEnd)
            return 0;

        // Extract and return it
        int result = m_scanKeyBuffer [m_scanBufStart];
        IncScanStart ();
        return result;
    }
    public void ClearKeyBuffers() {

        // Clear key states
        for (int i = 0; i < m_keyDown.length; i++)
            //TODO determine correct default value; 'false' was used in original source
            m_keyDown [i] = 0;

        // Clear key buffer
        m_bufStart  = 0;
        m_bufEnd    = 0;
        m_scanBufStart  = 0;
        m_scanBufEnd    = 0;
    }

    public boolean isKeyDown(char i) {
        //ProcessWindowsMessages();
        return m_keyDown[i] != 0;
    }

    public void fakeScanKey(int scanCode, int bitmask, boolean down) {
        assert (bitmask != 0);

        // Fake a key press/release
        //TODO confirm mask works properly
        boolean wasDown = (m_keyDown [scanCode]  & bitmask) != 0;

        // Add a keypress to buffer (if necessary)
        if (down && !wasDown)
            BufferScanKey (scanCode);

        // Toggle key bitmask
        if (down)
            m_keyDown [scanCode] |= bitmask;
        else
            m_keyDown [scanCode] &= ~bitmask;
    }

    // Display screen
    void        SwapBuffers ()  {
        glfwSwapBuffers(m_window); //DisplayManager
    }

    //virtual
    /*
    public LRESULT CALLBACK WndProc(HWND	hWnd,		    // Handle For This Window
                                    UINT	uMsg,			// Message For This Window
                                    WPARAM	wParam,			// Additional Message Information
                                    LPARAM	lParam)	{		// Additional Message Information
        // Find window object
        GLWindow win = glWindows [hWnd];

        // Pass call to window object
        if (win != NULL)
            return win.WndProc (hWnd, uMsg, wParam, lParam);
        else
            return DefWindowProc(hWnd,uMsg,wParam,lParam);   // No window object found. Use default handler.

    }*/

    // Multitexturing extension
    public boolean ExtensionSupported(String extension){
        //TODO determine what this function does; not sure the purpose of padding the strings with whitespace
        String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
        extensions = " " + extensions + " ";
        extension = " " + extension + " ";
        return extensions.contains (extension);
        /*
        //Original source for reference
            std::string extensions = (char *) glGetString (GL_EXTENSIONS);
            extensions = (std::string) " " + extensions + " ";
            extension = " " + extension + " ";
            return extensions.find (extension) != std::string::npos;
         */
    }

    public void InitMultitexture(){
        //TODO Look into extensions
        /*if (ExtensionSupported ("GL_ARB_multitexture")) {
            glMultiTexCoord2f = (PFNGLMULTITEXCOORD2FARBPROC) wglGetProcAddress ("glMultiTexCoord2fARB");
            glMultiTexCoord2d = (PFNGLMULTITEXCOORD2DARBPROC) wglGetProcAddress ("glMultiTexCoord2dARB");
            glActiveTexture = (PFNGLACTIVETEXTUREARBPROC) wglGetProcAddress ("glActiveTextureARB");
        }
        else {
            glMultiTexCoord2f = null;
            glMultiTexCoord2d = null;
            glActiveTexture = null;
        }*/
    }

    // Mouse
    public int MouseX() {
        //ProcessWindowsMessages();
        return m_mouseX;
    }

    public int MouseY() {
        //ProcessWindowsMessages();
        return m_mouseY;
    }

    public int MouseXD(){

        // Read any pending windows messages
        //ProcessWindowsMessages ();

        if (!m_focused)
            return 0;

        if (!m_mouseCentred) {
            m_mouseX = m_width / 2;
            m_mouseY = m_height / 2;
            PositionMouse ();
            m_mouseCentred = true;
            DoHideCursor ();
            return 0;
        }

        // Calculate how far mouse has moved from centre
        int centre = m_width / 2;
        int result = m_mouseX - centre;

        // Recentre cursor x
        m_mouseX = centre;
        PositionMouse();

        // Return result
        return result;
    }

    public int MouseYD(){

        // Read any pending windows messages
        //ProcessWindowsMessages ();

        if (!m_focused)
            return  0;

        if (!m_mouseCentred) {
            m_mouseX = m_width / 2;
            m_mouseY = m_height / 2;
            PositionMouse ();
            m_mouseCentred = true;
            DoHideCursor ();
            return 0;
        }

        // Calculate how far mouse has moved from centre
        int centre = m_height / 2;
        int result = m_mouseY - centre;

        // Recentre cursor y
        m_mouseY = centre;
        PositionMouse ();

        // Return result
        return result;
    }

    public boolean MouseButton(int index) {
        assert (index >= 0);
        assert (index < 3);
        //ProcessWindowsMessages();
        return m_mouseButton[index];
    }

    public int MouseWheel() {
        int result = m_mouseWheel;
        m_mouseWheel = 0;
        return result;
    }

    // Misc
    public boolean DontPaint() {
        return m_dontPaint;
    }

    public void SetDontPaint(boolean dontPaint) {
        m_dontPaint = dontPaint;
    }

    public boolean PausePressed() {
        boolean result = m_pausePressed;
        m_pausePressed = false;
        return result;
    }

    void ClearGLMatrix() {

        // Clear the matrix stack by popping a lot of matrices
        for (int i = 0; i < 256; i++)
            GL11.glPopMatrix();

        // Reset the matrix itself
        GL11.glLoadIdentity();
    }


    /**
     * gluPerspective replacement
     * @param fovY
     * @param aspect
     * @param zNear
     * @param zFar
     */
    void perspectiveGL( double fovY, double aspect, double zNear, double zFar )
    {
        double fW, fH;

        //fH = tan( (fovY / 2) / 180 * pi ) * zNear;
        fH = Math.tan(fovY / 360.0 * M_PI) * zNear;
        fW = fH * aspect;

        GL11.glFrustum(-fW, fW, -fH, fH, zNear, zFar);
    }
}

