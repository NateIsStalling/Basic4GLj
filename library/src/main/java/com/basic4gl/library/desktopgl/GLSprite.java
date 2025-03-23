package com.basic4gl.library.desktopgl;

import static com.basic4gl.runtime.util.Assert.assertTrue;
import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;
import org.lwjgl.BufferUtils;

/**
 * An animated sprite
 */
public class GLSprite extends GLBasicSprite {

  private float frame;

  private void SetDefaults() {
    frame = 0;
    xd = 0;
    yd = 0;
    angled = 0;
    framed = 0;
    animLoop = true;
  }

  protected void internalCopy(GLBasicSprite s) {
    super.internalCopy(s);
    GLSprite spr = (GLSprite) s;
    frame = spr.frame;
    xd = spr.xd;
    yd = spr.yd;
    angled = spr.angled;
    framed = spr.framed;
    animLoop = spr.animLoop;
  }

  protected void checkFrame() {

    // An obscure bug which I haven't tracked down yet occasionally causes
    // m_frame to go to +INF, which causes an infinite loop.
    // Workaround for now is to reset it to 0 when goes out (sane) range.
    if (frame < -10000 || frame > 10000) {
      frame = 0;
    }

    if (!textures.isEmpty()) {
      if (animLoop) {

        // Looped animation.
        while (frame < 0) {
          frame += textures.size();
        }
        while (frame >= textures.size()) {
          frame -= textures.size();
        }
      } else {

        // Clamped animation
        if (frame < 0) {
          frame = 0;
        }
        if (frame >= textures.size()) {
          frame = (float) textures.size() - 0.001f;
        }
      }
    }
  }

  public float xd, yd, angled, framed;
  public boolean animLoop;

  public GLSprite() {
    super();
    SetDefaults();
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
  public GLSpriteEngine.GLSpriteType getGLSpriteType() {
    return GLSpriteEngine.GLSpriteType.SPR_SPRITE;
  }

  // Getters and setters.
  public int getFrameCount() {
    return textures.size();
  }

  public float getFrame() {
    return frame;
  }

  public void setFrame(float f) {
    frame = f;
    checkFrame();
  }

  // All other fields can be accessed directly.

  // Misc
  public boolean isAnimationDone() {
    return (!animLoop) && frame >= textures.size() - 0.002;
  }

  // Rendering
  public void render(float[] camInv) {

    // Render sprite using OpenGL commands.

    // Assumes that the appropriate projection/translation matrices have been setup,
    // and other OpenGL state (such as texturing & transparency) has been setup
    // accordingly.

    // Sprite must be visible
    if (!visible || getFrameCount() == 0) {
      return;
    }

    // Setup texture and colour
    int frame = (int) this.frame;
    assertTrue(frame >= 0);
    assertTrue(frame < getFrameCount());
    glBindTexture(GL_TEXTURE_2D, textures.get(frame));

    ByteBuffer byteBuf = BufferUtils.createByteBuffer(color.length * 4); // 4 bytes per float
    FloatBuffer buffer = byteBuf.asFloatBuffer();
    buffer.put(color);
    buffer.position(0);
    glColor4fv(buffer);
    buffer.rewind();
    buffer.get(color);

    // Translation, rotation & scaling
    glTranslatef(positionX, positionY, 0);
    if (angle != 0) {
      glRotatef(angle, 0, 0, 1);
    }
    glScalef(sizeX * scale, sizeY * scale, 1);
    if (centerX != 0 || centerY != 0) {
      glTranslatef(-centerX, -centerY, 0);
    }

    float x1 = 0, x2 = 1, y1 = 1, y2 = 0;
    if (flipX) {
      x1 = 1;
      x2 = 0;
    }
    if (flipY) {
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
  public void animate() {
    positionX += xd; // Simple animation
    positionY += yd;
    angle += angled;
    animateFrame();
  }

  public void animateFrame() {
    frame += framed;
    checkFrame();
  }
}
