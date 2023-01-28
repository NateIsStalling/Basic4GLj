package com.basic4gl.library.desktopgl;

import com.basic4gl.library.standard.Standard;
import com.basic4gl.library.standard.TrigBasicLib;
import com.basic4gl.lib.util.FileOpener;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL11.*;

/**
 * A GLTextGrid with added support for sprites
 */
public class GLSpriteEngine extends GLTextGrid{

    // Constants
    public static final byte DRAW_SPRITES_INFRONT = 2;
    public static final byte DRAW_SPRITES_BEHIND = 4;
    public static final byte DRAW_SPRITES = DRAW_SPRITES_INFRONT | DRAW_SPRITES_BEHIND;

////////////////////////////////////////////////////////////////////////////////
// glSpriteList
//
// A list of glBasicSprite objects, sorted by Z order

    public class GLSpriteList {

        public GLBasicSprite m_head;

        public GLSpriteList (){
            m_head = null;
        }
        protected void finalize(){
            while (m_head != null) {
                m_head.Remove ();
            }
        }
        //public ~GLSpriteList ();
    }

    public enum GLSpriteType {
        SPR_INVALID(0), SPR_SPRITE(1), SPR_TILEMAP(2);
        private int mType;
        GLSpriteType(int type) {
            mType = type;
        }
        public int getType() { return mType;}
    }


    ////////////////////////////////////////////////////////////////////////////////
// glSpriteEngine
//

    // Sprites
    GLSpriteList m_sprites = new GLSpriteList();

    // Camera settings
    float m_width, m_height, m_fov;

    // Working variables
    GLBasicSprite m_cursor;

    void DrawSprites (boolean inFront){

        // Setup OpenGL state.
        // Note: Most of the OpenGL state is already setup for us (in preparation for
        // drawing the text sprites.)
        // We only need to set up an appropriate projection and ensure the model view
        // matrix is set to our requirements.
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        //gluOrtho2D(0, 1, 1, 0);                    // Top left corner is (0, 0). Bottom right is (1, 1).
        //Replaced with glOrtho
        glOrtho(0, 1, 1, 0, -1, 1);
        glMatrixMode (GL_MODELVIEW);
        glLoadIdentity ();

        // Apply camera transformations
        float[] m1 = new float[16], m2 = new float[16];

//        System.out.println("width "+ m_width + ", " + m_height);
        // Scale in window dimensions
        glScalef (1.0f / m_width, 1.0f / m_height, 1);
        TrigBasicLib.Scale(m_width, m_height, 1);
        TrigBasicLib.CopyMatrix(m1, TrigBasicLib.getGlobalMatrix());

        // Camera scale and rotation
        glTranslatef (m_width / 2, m_height / 2, 0);
        TrigBasicLib.Translate(-m_width / 2, -m_height / 2, 0);
        TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m1, m2);

        glRotatef (-m_camAngle, 0, 0, 1);
        TrigBasicLib.RotateZ(m_camAngle);
        TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m2, m1);

        float[] camInv = new float[16];
        TrigBasicLib.CopyMatrix(camInv, m1);

        // Parallax settings
        float dist = (float) (m_height / (2f * Math.tan (m_fov * Standard.M_PI / 360f)));         // Effective distance of screen

        if (!inFront) {
            m_cursor = m_sprites.m_head;    // Reset cursor to start of list
        }

        // Render sprites.
        // In front:    Render all remaining sprites
        // Behind:      Render all ZOrder >= 0
        while (m_cursor != null && (inFront || m_cursor.ZOrder () >= 0)) {

            // Sprite must be visible.
            // Must also be in front of camera (if parallax mode).
            if (m_cursor.m_visible && (!m_cursor.m_parallax || m_cursor.ZOrder () >= m_camZ - dist + 0.0001)) {
                glPushMatrix ();

                // Build rest of camera matrix.
                if (m_cursor.m_parallax) {
                    float parallaxFactor = dist / ((m_cursor.ZOrder () - m_camZ) + dist);

                    // Update camera matrix
                    glScalef (parallaxFactor, parallaxFactor, 1);
                    TrigBasicLib.Scale(1.0f / parallaxFactor, 1.0f / parallaxFactor, 1);
                    TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), camInv, m1);

                    glTranslatef (-m_width / 2, -m_height / 2, 1);
                    TrigBasicLib.Translate(m_width / 2, m_height / 2, 1);
                    TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m1, m2);
                }
                else {
                    glTranslatef (-m_width / 2, -m_height / 2, 1);
                    TrigBasicLib.Translate(m_width / 2, m_height / 2, 1);
                    TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), camInv, m2);
                }

                glTranslatef (-m_camX, -m_camY, 0);
                TrigBasicLib.Translate(m_camX, m_camY, 0);
                TrigBasicLib.MatrixTimesMatrix(TrigBasicLib.getGlobalMatrix(), m2, m1);

                // Render sprite
                if (m_cursor.m_solid) {
                    glDisable (GL_BLEND);
                }
                else {
                    glEnable (GL_BLEND);
                    glBlendFunc(m_cursor.m_srcBlend, m_cursor.m_dstBlend);
                }
                m_cursor.Render (m1);
                glPopMatrix ();
            }

            // Move on to next
            m_cursor = m_cursor.Next ();
        }

        glPopAttrib ();
    }
    void InternalDraw(byte flags){

        // Draw sprites behind text
        if ((flags & DRAW_SPRITES_BEHIND) != 0) {
            DrawSprites (false);
        }

        // Draw text
        super.InternalDraw(flags);

        // Draw sprites in front of text
        if ((flags & DRAW_SPRITES_INFRONT) != 0) {
            DrawSprites (true);
        }
    }

    public float m_camX, m_camY, m_camZ;
    public float m_camAngle;

    public GLSpriteEngine (String texFile, FileOpener files, int rows, int cols, int texRows, int texCols)
    {
        super (texFile, files, rows, cols, texRows, texCols);
        SetDefaults ();
    }
    public void SetDefaults (){
        m_width     = 640;
        m_height    = 480;
        m_camX      = 0;
        m_camY      = 0;
        m_camAngle  = 0;
        m_fov       = 60;
        m_camZ      = 0;
    }

    public void AddSprite (GLBasicSprite sprite) {
        assertTrue(sprite != null);
        sprite.Insert (m_sprites);
    }

    public float Width ()                  { return m_width; }
    public float Height ()                 { return m_height; }
    public float FOV ()                    { return m_fov; }
    public void SetWidth  (float width)    { if (width != 0) {
        m_width  = width;
    }
    }
    public void SetHeight (float height)   { if (height != 0) {
        m_height = height;
    }
    }
    public void SetFOV (float fov)         { if (fov >= 1 && fov <= 175) {
        m_fov = fov;
    }
    }
    public void Animate () {
        for (GLBasicSprite s = m_sprites.m_head; s != null; s = s.Next ()) {
            s.Animate();
        }
    }
    public void AnimateFrames() {
        for (GLBasicSprite s = m_sprites.m_head; s != null; s = s.Next ()) {
            s.AnimateFrame();
        }
    }
}
