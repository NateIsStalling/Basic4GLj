package com.basic4gl.library.desktopgl;

import com.basic4gl.runtime.util.IntHandleResources;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.glDeleteTextures;

/**
 * Stores OpenGL texture handles
 */
public class TextureResourceStore extends IntHandleResources {
    protected void deleteHandle(int handle) {
        int texture = handle;//(GLuint) handle;
        ByteBuffer buffer = BufferUtils.createByteBuffer(Integer.SIZE / Byte.SIZE);
        buffer.asIntBuffer().put(texture);
        buffer.rewind();
        glDeleteTextures(buffer.asIntBuffer());
    }
}
