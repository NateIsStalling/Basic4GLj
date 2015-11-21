package com.basic4gl.lib.desktopgl;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Nate on 11/2/2015.
 */
////////////////////////////////////////////////////////////////////////////////
// GLSprite
//
// An animated sprite
public class GLSprite extends GLBasicSprite {

    private float m_frame;

    private void SetDefaults() {
        m_frame = 0;
        m_xd = 0;
        m_yd = 0;
        m_angled = 0;
        m_framed = 0;
        m_animLoop = true;
    }


    protected void InternalCopy(GLBasicSprite s) {
        super.InternalCopy(s);
        GLSprite spr = (GLSprite) s;
        m_frame = spr.m_frame;
        m_xd = spr.m_xd;
        m_yd = spr.m_yd;
        m_angled = spr.m_angled;
        m_framed = spr.m_framed;
        m_animLoop = spr.m_animLoop;
    }

    protected void CheckFrame() {

        // An obscure bug which I haven't tracked down yet occasionally causes
        // m_frame to go to +INF, which causes an infinite loop.
        // Workaround for now is to reset it to 0 when goes out (sane) range.
        if (m_frame < -10000 || m_frame > 10000)
            m_frame = 0;

        if (!m_textures.isEmpty()) {
            if (m_animLoop) {

                // Looped animation.
                while (m_frame < 0)
                    m_frame += m_textures.size();
                while (m_frame >= m_textures.size())
                    m_frame -= m_textures.size();
            } else {

                // Clamped animation
                if (m_frame < 0)
                    m_frame = 0;
                if (m_frame >= m_textures.size())
                    m_frame = (float) m_textures.size() - 0.001f;
            }
        }
    }


    public float m_xd, m_yd, m_angled, m_framed;
    public boolean m_animLoop;

    public GLSprite() {
        super();
        SetDefaults ();
    }

    public GLSprite(int tex) {
        super(tex);
        SetDefaults();
    }

    public GLSprite(Vector<Integer> tex) {
        super(tex);
        SetDefaults();
    }

    // Class type identification
    public GLSpriteEngine.GLSpriteType Type() {
        return GLSpriteEngine.GLSpriteType.SPR_SPRITE;
    }

    // Getters and setters.
    public int FrameCount() {
        return m_textures.size();
    }

    public float Frame() {
        return m_frame;
    }

    public void SetFrame(float f) {
        m_frame = f;
        CheckFrame();
    }
    // All other fields can be accessed directly.

    // Misc
    public boolean AnimDone() {
        return (!m_animLoop) && m_frame >= m_textures.size() - 0.002;
    }

    // Rendering
    public void Render(float[] camInv) {

        // Render sprite using OpenGL commands.

        // Assumes that the appropriate projection/translation matrices have been setup,
        // and other OpenGL state (such as texturing & transparency) has been setup
        // accordingly.

        // Sprite must be visible
        if (!m_visible || FrameCount() == 0)
            return;

        // Setup texture and colour
        int frame = (int) m_frame;
        assert (frame >= 0);
        assert (frame < FrameCount());
        glBindTexture(GL_TEXTURE_2D, m_textures.get(frame));

        ByteBuffer byteBuf = BufferUtils.createByteBuffer(m_colour.length * 4); //4 bytes per float
        FloatBuffer buffer = byteBuf.asFloatBuffer();
        buffer.put(m_colour);
        buffer.position(0);
        glColor4fv(buffer);
        buffer.rewind();
        buffer.get(m_colour);

        // Translation, rotation & scaling
        glTranslatef(m_x, m_y, 0);
        if (m_angle != 0)
            glRotatef(m_angle, 0, 0, 1);
        glScalef(m_xSize * m_scale,
                m_ySize * m_scale,
                1);
        if (m_xCentre != 0 || m_yCentre != 0)
            glTranslatef(-m_xCentre, -m_yCentre, 0);

        float x1 = 0, x2 = 1,
                y1 = 1, y2 = 0;
        if (m_xFlip) {
            x1 = 1;
            x2 = 0;
        }
        if (m_yFlip) {
            y1 = 0;
            y2 = 1;
        }

        // Draw sprite
        glBegin(GL_QUADS);
        glTexCoord2f(x1, y1);
        glVertex2f(0, 0);
        glTexCoord2f(x2, y1);
        glVertex2f(1, 0);
        glTexCoord2f(x2, y2);
        glVertex2f(1, 1);
        glTexCoord2f(x1, y2);
        glVertex2f(0, 1);
        glEnd();
    }

    // Animation
    public void Animate() {
        m_x += m_xd;            // Simple animation
        m_y += m_yd;
        m_angle += m_angled;
        AnimateFrame();
    }

    public void AnimateFrame() {
        m_frame += m_framed;
        CheckFrame();
    }
}
