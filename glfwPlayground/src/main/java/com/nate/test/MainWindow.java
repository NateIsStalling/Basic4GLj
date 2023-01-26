package com.nate.test;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MainWindow {
    static class HelloWorld {
        // The window handle
        private long window;

        float width = 640f;
        float height = 320f;

        public void run() {
            System.out.println("Hello LWJGL " + Version.getVersion() + "!");

            init();
            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }

        private void init() {
            // Setup an error callback. The default implementation
            // will print the error message in System.err.
            GLFWErrorCallback.createPrint(System.err).set();

//            debugProc = GLUtil.setupDebugMessageCallback();
            // Initialize GLFW. Most GLFW functions will not work before doing this.
            if (!glfwInit()) {
                throw new IllegalStateException("Unable to initialize GLFW");
            }

            // Configure GLFW
            glfwDefaultWindowHints(); // optional, the current window hints are already the default
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

            // Create the window
            window = glfwCreateWindow((int)width, (int)height, "Hello World!", NULL, NULL);
            if (window == NULL) {
                throw new RuntimeException("Failed to create the GLFW window");
            }

            // Setup a key callback. It will be called every time a key is pressed, repeated or released.
            glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
                }
            });

            // Get the thread stack and push a new frame
            try (MemoryStack stack = stackPush()) {
                IntBuffer pWidth = stack.mallocInt(1); // int*
                IntBuffer pHeight = stack.mallocInt(1); // int*

                // Get the window size passed to glfwCreateWindow
                glfwGetWindowSize(window, pWidth, pHeight);

                // Get the resolution of the primary monitor
                GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

                // Center the window
                glfwSetWindowPos(
                        window,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2
                );
            } // the stack frame is popped automatically

            // Make the OpenGL context current
            glfwMakeContextCurrent(window);
            // Enable v-sync
            glfwSwapInterval(1);

            // Make the window visible
            glfwShowWindow(window);
        }

        private void loop() {
            // This line is critical for LWJGL's interoperation with GLFW's
            // OpenGL context, or any context that is managed externally.
            // LWJGL detects the context that is current in the current thread,
            // creates the GLCapabilities instance and makes the OpenGL
            // bindings available for use.
            GL.createCapabilities();

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            //Calculate the aspect ratio of the window
            perspectiveGL(90.0f,(float)width/(float)height,0.1f,100.0f);

            glShadeModel(GL_SMOOTH);
            glClearDepth(1.0f);                         // Depth Buffer Setup
            glEnable(GL_DEPTH_TEST);                        // Enables Depth Testing
            glDepthFunc(GL_LEQUAL);                         // The Type Of Depth Test To Do

            // Set the clear color
            glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);//	' Clear The Screen And The Depth Buffer
            glLoadIdentity();//							' Reset The View
//            glTranslatef(-1.5f,0.0f,-6.0f);//					' Move Left 1.5 Units And Into The Screen 6.0
//            glBegin(GL_TRIANGLES);//							' Drawing Using Triangles
//            glColor3f(1.0f,0.0f,0.0f)	;//				' Set The Color To Red
//            glVertex3f( 0.0f, 1.0f, 0.0f)	;//				' Top
//            glColor3f(0.0f,1.0f,0.0f);//					' Set The Color To Green
//            glVertex3f(-1.0f,-1.0f, 0.0f);//					' Bottom Left
//            glColor3f(0.0f,0.0f,1.0f);//					' Set The Color To Blue
//            glVertex3f( 1.0f,-1.0f, 0.0f);//					' Bottom Right
//            glEnd();//									' Finished Drawing The Triangle
//            glTranslatef(3.0f,0.0f,0.0f);//						' Move Right 3 Units
            glColor3f(0.5f,0.5f,1.0f);//						' Set The Color To Blue One Time Only
            glBegin(GL_QUADS);//							' Draw A Quad
            glVertex3f(-1.0f, 1.0f, 0.0f);//					' Top Left
            glVertex3f( 1.0f, 1.0f, 0.0f)	;//				' Top Right
            glVertex3f( 1.0f,-1.0f, 0.0f);//					' Bottom Right
            glVertex3f(-0.5f,-1.0f, 0.0f);//					' Bottom Left
            glEnd();
//            glfwSwapBuffers(window);

            // Run the rendering loop until the user has attempted to close
            // the window or has pressed the ESCAPE key.
            while (!glfwWindowShouldClose(window)) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

                glLoadIdentity();//							' Reset The View
//            glTranslatef(-1.5f,0.0f,-6.0f);//					' Move Left 1.5 Units And Into The Screen 6.0
//            glBegin(GL_TRIANGLES);//							' Drawing Using Triangles
//            glColor3f(1.0f,0.0f,0.0f)	;//				' Set The Color To Red
//            glVertex3f( 0.0f, 1.0f, 0.0f)	;//				' Top
//            glColor3f(0.0f,1.0f,0.0f);//					' Set The Color To Green
//            glVertex3f(-1.0f,-1.0f, 0.0f);//					' Bottom Left
//            glColor3f(0.0f,0.0f,1.0f);//					' Set The Color To Blue
//            glVertex3f( 1.0f,-1.0f, 0.0f);//					' Bottom Right
//            glEnd();//									' Finished Drawing The Triangle
//            glTranslatef(3.0f,0.0f,0.0f);//						' Move Right 3 Units
                glColor3f(0.5f,0.5f,1.0f);//						' Set The Color To Blue One Time Only
                glBegin(GL_QUADS);//							' Draw A Quad
                glVertex3f(-1.0f, 1.0f, 0.0f);//					' Top Left
                glVertex3f( 1.0f, 1.0f, 0.0f)	;//				' Top Right
                glVertex3f( 1.0f,-1.0f, 0.0f);//					' Bottom Right
                glVertex3f(-0.5f,-1.0f, 0.0f);//					' Bottom Left
                glEnd();
                glfwSwapBuffers(window); // swap the color buffers


                // Poll for window events. The key callback above will only be
                // invoked during this call.
                glfwPollEvents();
            }
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
            fH = Math.tan(fovY / 360.0 * Math.PI) * zNear;
            fW = fH * aspect;

            GL11.glFrustum(-fW, fW, -fH, fH, zNear, zFar);
        }

    }
    public static void main(String[] args) {
        new HelloWorld().run();
    }
}
