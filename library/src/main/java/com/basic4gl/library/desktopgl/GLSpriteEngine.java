package com.basic4gl.library.desktopgl;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL11.*;

import com.basic4gl.lib.util.FileOpener;
import com.basic4gl.library.standard.Standard;
import com.basic4gl.library.standard.TrigBasicLib;

/**
 * A GLTextGrid with added support for sprites
 */
public class GLSpriteEngine extends GLTextGrid {

    // Constants
    public static final byte DRAW_SPRITES_INFRONT = 2;
    public static final byte DRAW_SPRITES_BEHIND = 4;
    public static final byte DRAW_SPRITES = DRAW_SPRITES_INFRONT | DRAW_SPRITES_BEHIND;

    /**
     * A list of glBasicSprite objects, sorted by Z order
     */
    public static class GLSpriteList {

        public GLBasicSprite head;

        public GLSpriteList() {
            head = null;
        }

        protected void finalize() {
            while (head != null) {
                head.remove();
            }
        }
        // public ~GLSpriteList ();
    }

    public enum GLSpriteType {
        SPR_INVALID(0),
        SPR_SPRITE(1),
        SPR_TILEMAP(2);
        private final int type;

        GLSpriteType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    // Sprites
    private final GLSpriteList sprites = new GLSpriteList();

    // Camera settings
    private float width, height, fov;

    // Working variables
    private GLBasicSprite cursor;

    void drawSprites(boolean inFront) {

        // Setup OpenGL state.
        // Note: Most of the OpenGL state is already setup for us (in preparation for
        // drawing the text sprites.)
        // We only need to set up an appropriate projection and ensure the model view
        // matrix is set to our requirements.
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // gluOrtho2D(0, 1, 1, 0);                    // Top left corner is (0, 0). Bottom right is (1,
        // 1).
        // Replaced with glOrtho
        glOrtho(0, 1, 1, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // Apply camera transformations
        float[] m1 = new float[16], m2 = new float[16];

        //        System.out.println("width "+ this.width + ", " + this.height);
        // Scale in window dimensions
        glScalef(1.0f / width, 1.0f / height, 1);
        TrigBasicLib.scale(width, height, 1);
        TrigBasicLib.copyMatrix(m1, TrigBasicLib.getGlobalMatrix());

        // Camera scale and rotation
        glTranslatef(width / 2, height / 2, 0);
        TrigBasicLib.translate(-width / 2, -height / 2, 0);
        TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m1, m2);

        glRotatef(-camAngle, 0, 0, 1);
        TrigBasicLib.rotateZ(camAngle);
        TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m2, m1);

        float[] camInv = new float[16];
        TrigBasicLib.copyMatrix(camInv, m1);

        // Parallax settings
        float dist = (float) (height / (2f * Math.tan(fov * Standard.M_PI / 360f))); // Effective distance of screen

        if (!inFront) {
            cursor = sprites.head; // Reset cursor to start of list
        }

        // Render sprites.
        // In front:    Render all remaining sprites
        // Behind:      Render all ZOrder >= 0
        while (cursor != null && (inFront || cursor.getZOrder() >= 0)) {

            // Sprite must be visible.
            // Must also be in front of camera (if parallax mode).
            if (cursor.visible && (!cursor.parallax || cursor.getZOrder() >= camZ - dist + 0.0001)) {
                glPushMatrix();

                // Build rest of camera matrix.
                if (cursor.parallax) {
                    float parallaxFactor = dist / ((cursor.getZOrder() - camZ) + dist);

                    // Update camera matrix
                    glScalef(parallaxFactor, parallaxFactor, 1);
                    TrigBasicLib.scale(1.0f / parallaxFactor, 1.0f / parallaxFactor, 1);
                    TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), camInv, m1);

                    glTranslatef(-width / 2, -height / 2, 1);
                    TrigBasicLib.translate(width / 2, height / 2, 1);
                    TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m1, m2);
                } else {
                    glTranslatef(-width / 2, -height / 2, 1);
                    TrigBasicLib.translate(width / 2, height / 2, 1);
                    TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), camInv, m2);
                }

                glTranslatef(-camX, -camY, 0);
                TrigBasicLib.translate(camX, camY, 0);
                TrigBasicLib.matrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m2, m1);

                // Render sprite
                if (cursor.solid) {
                    glDisable(GL_BLEND);
                } else {
                    glEnable(GL_BLEND);
                    glBlendFunc(cursor.srcBlend, cursor.dstBlend);
                }
                cursor.render(m1);
                glPopMatrix();
            }

            // Move on to next
            cursor = cursor.next();
        }

        glPopAttrib();
    }

    void internalDraw(byte flags) {

        // Draw sprites behind text
        if ((flags & DRAW_SPRITES_BEHIND) != 0) {
            drawSprites(false);
        }

        // Draw text
        super.internalDraw(flags);

        // Draw sprites in front of text
        if ((flags & DRAW_SPRITES_INFRONT) != 0) {
            drawSprites(true);
        }
    }

    public float camX, camY, camZ;
    public float camAngle;

    public GLSpriteEngine(String texFile, FileOpener files, int rows, int cols, int texRows, int texCols) {
        super(texFile, files, rows, cols, texRows, texCols);
        setDefaults();
    }

    public void setDefaults() {
        width = 640;
        height = 480;
        camX = 0;
        camY = 0;
        camAngle = 0;
        fov = 60;
        camZ = 0;
    }

    public void addSprite(GLBasicSprite sprite) {
        assertTrue(sprite != null);
        sprite.insert(sprites);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getFOV() {
        return fov;
    }

    public void setWidth(float width) {
        if (width != 0) {
            this.width = width;
        }
    }

    public void setHeight(float height) {
        if (height != 0) {
            this.height = height;
        }
    }

    public void setFOV(float fov) {
        if (fov >= 1 && fov <= 175) {
            this.fov = fov;
        }
    }

    public void animate() {
        for (GLBasicSprite s = sprites.head; s != null; s = s.next()) {
            s.animate();
        }
    }

    public void animateFrames() {
        for (GLBasicSprite s = sprites.head; s != null; s = s.next()) {
            s.animateFrame();
        }
    }
}
