package com.basic4gl.library.desktopgl.content;

import com.basic4gl.runtime.util.ResourceStore;

/**
 * A store of glSprites
 */
public class GLSpriteStore extends ResourceStore<GLBasicSprite> {
    protected void deleteElement(int index) {
        GLBasicSprite sprite = getValueAt(index);
        if (sprite != null) {
            sprite.remove();
        }
        setValue(index, null);
    }

    public GLSpriteStore() {
        super(null);
    }
}
