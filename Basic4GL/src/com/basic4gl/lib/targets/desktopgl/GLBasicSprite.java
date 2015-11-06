package com.basic4gl.lib.targets.desktopgl;

import org.lwjgl.opengl.GL11;

import java.util.Vector;

/**
 * Created by Nate on 11/2/2015.
 */
////////////////////////////////////////////////////////////////////////////////
// glBasicSprite
//
// Abstract base type for 2D sprite and sprite like objects.
// (Currently this includes sprites and tile map layers.)
public abstract class GLBasicSprite {
    private GLSpriteEngine.GLSpriteList m_list;
    private GLBasicSprite m_prev, m_next;
    private float m_zOrder;

    private void InternalRemove() {
        assert (m_list != null);

        // Link prev item to next
        if (m_prev != null)
            m_prev.m_next = m_next;
        else
            m_list.m_head = m_next;

        // Link next item to prev
        if (m_next != null)
            m_next.m_prev = m_prev;

        // Unlink this item
        m_prev = null;
        m_next = null;
    }

    private void InternalReorder(GLBasicSprite prev, GLBasicSprite next) {

        // Note: List is sorted in DESCENDING zOrder
        boolean done = false;
        while (!done) {

            // Need to move forward?
            if (prev != null && prev.m_zOrder < m_zOrder) {
                next = prev;
                prev = prev.m_prev;
            }

            // Need to move backward?
            else if (next != null && next.m_zOrder > m_zOrder) {
                prev = next;
                next = next.m_next;
            }

            // Otherwise we're done
            else
                done = true;
        }

        // Insert sprite between prev and next
        assert (m_list != null);

        // Link prev to us
        if (prev != null)
            prev.m_next = this;
        else
            m_list.m_head = this;

        // Link next to us
        if (next != null)
            next.m_prev = this;

        // Link us to next & prev
        m_prev = prev;
        m_next = next;
    }

    private void Reorder() {

        // Remove from linked list
        if (m_list != null) {

            // Save original links
            GLBasicSprite prev = m_prev, next = m_next;
            InternalRemove();

            // Move to correct position
            InternalReorder(prev, next);
        }
    }

    private void SetDefaults() {
        m_zOrder = 0;
        m_list = null;
        m_prev = null;
        m_next = null;
        m_x = 0;
        m_y = 0;
        m_xSize = 32;
        m_ySize = 32;
        m_scale = 1;
        m_xCentre = .5f;
        m_yCentre = .5f;
        m_xFlip = false;
        m_yFlip = false;
        m_visible = true;
        m_angle = 0;
        m_colour[0] = 1;
        m_colour[1] = 1;
        m_colour[2] = 1;
        m_colour[3] = 1;
        m_parallax = false;
        m_solid = false;
        m_srcBlend = GL11.GL_SRC_ALPHA;
        m_dstBlend = GL11.GL_ONE_MINUS_SRC_ALPHA;
    }

    protected Vector<Integer> m_textures;

    protected void InternalCopy(GLBasicSprite s) {
        SetTextures(s.m_textures);
        m_x = s.m_x;
        m_y = s.m_y;
        m_xSize = s.m_xSize;
        m_ySize = s.m_ySize;
        m_scale = s.m_scale;
        m_xCentre = s.m_xCentre;
        m_yCentre = s.m_yCentre;
        m_xFlip = s.m_xFlip;
        m_yFlip = s.m_yFlip;
        m_visible = s.m_visible;
        m_angle = s.m_angle;
        m_colour[0] = s.m_colour[0];
        m_colour[1] = s.m_colour[1];
        m_colour[2] = s.m_colour[2];
        m_colour[3] = s.m_colour[3];
        m_zOrder = s.m_zOrder;
        m_srcBlend = s.m_srcBlend;
        m_dstBlend = s.m_dstBlend;
    }

    protected void CheckFrame() {
        // Default = do nothing.
    }

    // Basic fields.
    // I can't be bothered to write getters/setters for these. Just write in the
    // values, and call Render().
    public float m_x, m_y, m_xSize, m_ySize, m_scale;
    public float m_xCentre, m_yCentre;
    public boolean m_xFlip, m_yFlip, m_visible, m_parallax, m_solid;
    public float m_angle;
    public float m_colour[] = new float[4];
    public int m_srcBlend, m_dstBlend;

    // Construction/destruction
    public GLBasicSprite() {
        SetDefaults();
    }

    public GLBasicSprite(int tex) {
        SetDefaults();
        SetTexture(tex);
    }

    public GLBasicSprite(Vector<Integer> tex) {
        SetDefaults();
        SetTextures(tex);
    }
    /*public ~GLBasicSprite (){
        Remove ();
    }*/

    // Class type identification
    public abstract GLSpriteEngine.GLSpriteType Type();

    // ZOrder and list functions
    public void Insert(GLSpriteEngine.GLSpriteList list) {
        assert (list != null);

        // Remove from previous list (if any)
        Remove();

        // Insert into new list
        m_list = list;

        // At correct position
        InternalReorder(null, list.m_head);
    }

    public void Remove() {
        if (m_list != null) {
            InternalRemove();
            m_list = null;
        }
    }

    public float ZOrder() {
        return m_zOrder;
    }

    public void SetZOrder(float zOrder) {
        m_zOrder = zOrder;
        Reorder();
    }

    public GLBasicSprite Prev() {
        return m_prev;
    }

    public GLBasicSprite Next() {
        return m_next;
    }

    // Texture handle storage
    public void AddTexture(int t) {
        m_textures.add(t);
        CheckFrame();
    }

    public void AddTextures(Vector<Integer> t) {
        m_textures.addAll(t);            // Append textures to end
        CheckFrame();
    }

    public void SetTexture(int t) {
        m_textures.clear();
        AddTexture(t);
    }

    public void SetTextures(Vector<Integer> t) {
        m_textures.clear();
        AddTextures(t);
    }

    // Rendering/animation
    public abstract void Render(float[] camInv);           // camInv is the inverted camera matrix

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
        return s.Type() == Type();
    }

    public void Copy(GLBasicSprite s) {
        assert (SameTypeAs(s));
        InternalCopy(s);
        Reorder();             // (As ZOrder may have changed)
    }

    /*
    public GLBasicSprite& operator= (glBasicSprite& s) {
        Copy (s);
        return *this;
    }*/
}
