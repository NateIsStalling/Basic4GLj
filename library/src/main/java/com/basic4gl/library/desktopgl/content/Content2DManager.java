package com.basic4gl.library.desktopgl.content;

import static org.lwjgl.opengl.GL11.*;

import com.basic4gl.library.desktopgl.window.OpenGLWindowManager;
import com.basic4gl.runtime.core.opengl.IB4GLOpenGLText;
import java.util.ArrayList;

/**
 * Copyright (C) Tom Mulgrew, 2016 (tmulgrew@slingshot.co.nz)
 *
 * 	Orchestrates drawing 2D content, such as text and sprites.
 */
public class Content2DManager {

    private OpenGLWindowManager windowManager;
    private IB4GLOpenGLText.TextMode drawMode;
    private ArrayList<Content2D> content = new ArrayList<>();

    private void SaveGLState() {
        // Preserve OpenGL state
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        // Setup state for 2D rendering
        glDisable(GL_FOG);
        glDisable(GL_BLEND);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glDisable(GL_SCISSOR_TEST);
        glDisable(GL_STENCIL_TEST);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void restoreGLState() {
        // Restore preserved OpenGL state
        glMatrixMode(GL_TEXTURE);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glPopAttrib();
    }

    private void internalDraw(int bitFlags) {
        // Draw content
        SaveGLState();
        for (Content2D c : content) {
            if ((c.bitFlag & bitFlags) != 0) {
                c.drawCallback.onDrawContent2D(c);
            }
        }
        restoreGLState();
    }

    public Content2DManager(OpenGLWindowManager windowManager) {
        this.windowManager = windowManager;
        reset();
    }

    public void dispose() {}

    /**
     * Reset to defaults
     */
    public void reset() {
        // Set default
        drawMode = IB4GLOpenGLText.TextMode.TEXT_SIMPLE;
    }

    /**
     * Register content to draw
     * @param z
     * @param bitFlag
     */
    public void addContent(float z, int bitFlag, Content2DDrawHandler drawCallback) {
        // Find sorted insert position
        int i = 0;
        while (i < content.size() && content.get(i).z < z) {
            i++;
        }

        // Insert content at the correct index
        content.add(i, new Content2D(z, bitFlag, drawCallback));
    }

    // Properties
    public IB4GLOpenGLText.TextMode getDrawMode() {
        return drawMode;
    }

    public void setDrawMode(IB4GLOpenGLText.TextMode value) {
        drawMode = value;
    }

    public OpenGLWindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Explicitly redraw
     */
    public void draw() {
        draw(0xffffffff);
    }
    /**
     * Explicitly redraw
     * @param bitFlags
     */
    public void draw(int bitFlags) {
        if (drawMode == IB4GLOpenGLText.TextMode.TEXT_SIMPLE || drawMode == IB4GLOpenGLText.TextMode.TEXT_BUFFERED) {
            fullRedraw(bitFlags);
        } else if (drawMode == IB4GLOpenGLText.TextMode.TEXT_OVERLAID) {
            internalDraw(bitFlags);
        }
    }

    public void fullRedraw() {
        fullRedraw(0xffffffff);
    }

    public void fullRedraw(int bitFlags) {
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        internalDraw(bitFlags);
        windowManager.swapBuffers();
    }

    /**
     * Notify of 2D change made
     */
    public void changeMade() {
        // Redraw automatically if in simple mode
        if (drawMode == IB4GLOpenGLText.TextMode.TEXT_SIMPLE) {
            this.draw();
        }
    }
}
