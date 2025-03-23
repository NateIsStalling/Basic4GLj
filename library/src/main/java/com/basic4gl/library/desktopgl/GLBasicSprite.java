package com.basic4gl.library.desktopgl;

import static com.basic4gl.runtime.util.Assert.assertTrue;

import java.util.Vector;
import org.lwjgl.opengl.GL11;

/**
 * Abstract base type for 2D sprite and sprite like objects.
 * (Currently this includes sprites and tile map layers.)
 */
public abstract class GLBasicSprite {
  private GLSpriteEngine.GLSpriteList list;
  private GLBasicSprite prev, next;
  private float zOrder;

  // Construction/destruction
  public GLBasicSprite() {
    setDefaults();
  }

  public GLBasicSprite(int tex) {
    setDefaults();
    setTexture(tex);
  }

  public GLBasicSprite(Vector<Integer> tex) {
    setDefaults();
    setTextures(tex);
  }

  /*public ~GLBasicSprite (){
      Remove ();
  }*/

  private void internalRemove() {
    assertTrue(list != null);

    // Link prev item to next
    if (prev != null) {
      prev.next = next;
    } else {
      list.head = next;
    }

    // Link next item to prev
    if (next != null) {
      next.prev = prev;
    }

    // Unlink this item
    prev = null;
    next = null;
  }

  private void internalReorder(GLBasicSprite prev, GLBasicSprite next) {

    // Note: List is sorted in DESCENDING zOrder
    boolean done = false;
    while (!done) {

      // Need to move forward?
      if (prev != null && prev.zOrder < zOrder) {
        next = prev;
        prev = prev.prev;
      }

      // Need to move backward?
      else if (next != null && next.zOrder > zOrder) {
        prev = next;
        next = next.next;
      }

      // Otherwise we're done
      else {
        done = true;
      }
    }

    // Insert sprite between prev and next
    assertTrue(list != null);

    // Link prev to us
    if (prev != null) {
      prev.next = this;
    } else {
      list.head = this;
    }

    // Link next to us
    if (next != null) {
      next.prev = this;
    }

    // Link us to next & prev
    this.prev = prev;
    this.next = next;
  }

  private void reorder() {

    // Remove from linked list
    if (list != null) {

      // Save original links
      GLBasicSprite prev = this.prev, next = this.next;
      internalRemove();

      // Move to correct position
      internalReorder(prev, next);
    }
  }

  private void setDefaults() {
    zOrder = 0;
    list = null;
    prev = null;
    next = null;
    positionX = 0;
    positionY = 0;
    sizeX = 32;
    sizeY = 32;
    scale = 1;
    centerX = .5f;
    centerY = .5f;
    flipX = false;
    flipY = false;
    visible = true;
    angle = 0;
    color[0] = 1;
    color[1] = 1;
    color[2] = 1;
    color[3] = 1;
    parallax = false;
    solid = false;
    srcBlend = GL11.GL_SRC_ALPHA;
    dstBlend = GL11.GL_ONE_MINUS_SRC_ALPHA;
  }

  protected void internalCopy(GLBasicSprite s) {
    setTextures(s.textures);
    positionX = s.positionX;
    positionY = s.positionY;
    sizeX = s.sizeX;
    sizeY = s.sizeY;
    scale = s.scale;
    centerX = s.centerX;
    centerY = s.centerY;
    flipX = s.flipX;
    flipY = s.flipY;
    visible = s.visible;
    angle = s.angle;
    color[0] = s.color[0];
    color[1] = s.color[1];
    color[2] = s.color[2];
    color[3] = s.color[3];
    zOrder = s.zOrder;
    srcBlend = s.srcBlend;
    dstBlend = s.dstBlend;
  }

  protected void checkFrame() {
    // Default = do nothing.
  }

  // Basic fields.
  // I can't be bothered to write getters/setters for these. Just write in the
  // values, and call Render().
  public float positionX, positionY, sizeX, sizeY, scale;
  public float centerX, centerY;
  public boolean flipX, flipY, visible, parallax, solid;
  public float angle;
  public float[] color = new float[4];
  public int srcBlend, dstBlend;

  protected Vector<Integer> textures = new Vector<>();

  // Class type identification
  public abstract GLSpriteEngine.GLSpriteType getGLSpriteType();

  // ZOrder and list functions
  public void insert(GLSpriteEngine.GLSpriteList list) {
    assertTrue(list != null);

    // Remove from previous list (if any)
    remove();

    // Insert into new list
    this.list = list;

    // At correct position
    internalReorder(null, list.head);
  }

  public void remove() {
    if (list != null) {
      internalRemove();
      list = null;
    }
  }

  public float getZOrder() {
    return zOrder;
  }

  public void setZOrder(float zOrder) {
    this.zOrder = zOrder;
    reorder();
  }

  public GLBasicSprite prev() {
    return prev;
  }

  public GLBasicSprite next() {
    return next;
  }

  // Texture handle storage
  public void addTexture(int t) {
    textures.add(t);
    checkFrame();
  }

  public void addTextures(Vector<Integer> t) {
    textures.addAll(t); // Append textures to end
    checkFrame();
  }

  public void setTexture(int t) {
    textures.clear();
    addTexture(t);
  }

  public void setTextures(Vector<Integer> t) {
    textures.clear();
    addTextures(t);
  }

  // Rendering/animation
  public abstract void render(float[] camInv); // camInv is the inverted camera matrix

  public void animate() {
    // By default Animate does nothing. Override for types to which it is
    // relevant.
  }

  public void animateFrame() {
    // Restricted animate. Animate frames only. Do not move sprites.
    // (Again by default does nothing).
  }

  // Copying/assignment
  public boolean isSameTypeAs(GLBasicSprite s) {
    return s.getGLSpriteType() == getGLSpriteType();
  }

  public void copy(GLBasicSprite s) {
    assertTrue(isSameTypeAs(s));
    internalCopy(s);
    reorder(); // (As ZOrder may have changed)
  }

  /*
  public GLBasicSprite& operator= (glBasicSprite& s) {
      Copy (s);
      return *this;
  }*/
}
