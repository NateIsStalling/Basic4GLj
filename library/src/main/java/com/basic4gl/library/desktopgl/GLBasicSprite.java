package com.basic4gl.library.desktopgl;

import org.lwjgl.opengl.GL11;

import java.util.Vector;

import static com.basic4gl.runtime.util.Assert.assertTrue;

/**
 * Abstract base type for 2D sprite and sprite like objects.
 * (Currently this includes sprites and tile map layers.)
 */
public abstract class GLBasicSprite {
    private GLSpriteEngine.GLSpriteList list;
    private GLBasicSprite prev, next;
    private float zOrder;

    private void InternalRemove() {
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

    private void InternalReorder(GLBasicSprite prev, GLBasicSprite next) {

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

    private void Reorder() {

        // Remove from linked list
        if (list != null) {

            // Save original links
            GLBasicSprite prev = this.prev, next = this.next;
            InternalRemove();

            // Move to correct position
            InternalReorder(prev, next);
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
        SetTextures(s.textures);
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

    protected void CheckFrame() {
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

    protected Vector<Integer> textures = new Vector<Integer>();

    // Construction/destruction
    public GLBasicSprite() {
        setDefaults();
    }

    public GLBasicSprite(int tex) {
        setDefaults();
        SetTexture(tex);
    }

    public GLBasicSprite(Vector<Integer> tex) {
        setDefaults();
        SetTextures(tex);
    }
    /*public ~GLBasicSprite (){
        Remove ();
    }*/

    // Class type identification
    public abstract GLSpriteEngine.GLSpriteType getGLSpriteType();

    // ZOrder and list functions
    public void Insert(GLSpriteEngine.GLSpriteList list) {
        assertTrue(list != null);

        // Remove from previous list (if any)
        Remove();

        // Insert into new list
        this.list = list;

        // At correct position
        InternalReorder(null, list.head);
    }

    public void Remove() {
        if (list != null) {
            InternalRemove();
            list = null;
        }
    }

    public float ZOrder() {
        return zOrder;
    }

    public void SetZOrder(float zOrder) {
        this.zOrder = zOrder;
        Reorder();
    }

    public GLBasicSprite Prev() {
        return prev;
    }

    public GLBasicSprite Next() {
        return next;
    }

    // Texture handle storage
    public void AddTexture(int t) {
        textures.add(t);
        CheckFrame();
    }

    public void AddTextures(Vector<Integer> t) {
        textures.addAll(t);            // Append textures to end
        CheckFrame();
    }

    public void SetTexture(int t) {
        textures.clear();
        AddTexture(t);
    }

    public void SetTextures(Vector<Integer> t) {
        textures.clear();
        AddTextures(t);
    }

    // Rendering/animation
    public abstract void render(float[] camInv);           // camInv is the inverted camera matrix

    public void Animate() {
        // By default Animate does nothing. Override for types to which it is
        // relevant.
    }

    public void AnimateFrame() {
        // Restricted animate. Animate frames only. Do not move sprites.
        // (Again by default does nothing).
    }

    // Copying/assignment
    public boolean SameTypeAs(GLBasicSprite s) {
        return s.getGLSpriteType() == getGLSpriteType();
    }

    public void Copy(GLBasicSprite s) {
        assertTrue(SameTypeAs(s));
        internalCopy(s);
        Reorder();             // (As ZOrder may have changed)
    }

    /*
    public GLBasicSprite& operator= (glBasicSprite& s) {
        Copy (s);
        return *this;
    }*/
}
