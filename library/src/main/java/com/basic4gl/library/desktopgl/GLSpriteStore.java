package com.basic4gl.library.desktopgl;

import com.basic4gl.runtime.util.ResourceStore;

/**
 * A store of glSprites
 */
class GLSpriteStore extends ResourceStore<GLBasicSprite> {
  protected void deleteElement(int index) {
    setValue(index, null);
  }

  public GLSpriteStore() {
    super(null);
  }
}
