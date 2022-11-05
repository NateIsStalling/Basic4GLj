package com.basic4gl.library.desktopgl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

/**
 * Created by Nate on 11/4/2015.
 */
public class Image {
    private ByteBuffer mData;
    private int mWidth, mHeight;
    private int mFormat;
    private int mBPP;

    public Image(String filename){
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        File file = new File(filename);
        if (file.exists()) {
            mData = stbi_load(filename, w, h, comp, 0);

            mWidth = w.get(0);
            mHeight = h.get(0);
            mBPP = comp.get(0);
            mFormat = mBPP == 3 ? GL11.GL_RGB : GL11.GL_RGBA;

            if (mData == null) {
                System.out.println("Failed to load image: " + filename);
                System.out.println(stbi_failure_reason());
            } else {
                //There's an issue with stbi_load loading images upside down
                //So here we change the order of the bytes to flip the image that we just loaded
                ByteBuffer temp = ByteBuffer.allocateDirect(mData.capacity());
                mData.rewind();//copy from the beginning
                temp.rewind();
                for (int dy = 0; dy < mHeight; dy++) {
                    temp.position(dy * mWidth * mBPP);
                    mData.position((mHeight - dy - 1) * mWidth * mBPP);
                    for (int dx = 0; dx < mWidth * mBPP; dx++)
                        temp.put(mData.get());
                }
                temp.rewind();
                mData = temp;
            }

        }
    }

    public Image(int width, int height, int bpp){
        mWidth = width;
        mHeight = height;
        mBPP = bpp;
        mFormat = mBPP == 3 ? GL11.GL_RGB : GL11.GL_RGBA;
        mData = BufferUtils.createByteBuffer(width * height * bpp);
    }

    public Image(Image image){
        mData = clone(image.mData);

        if (mData == null) {
            System.out.println("Failed to load image: " + stbi_failure_reason());
        }
        mWidth = image.mWidth;
        mHeight = image.mHeight;
        mBPP = image.mBPP;
        mFormat = mBPP == 3 ? GL11.GL_RGB : GL11.GL_RGBA;
    }

    public Image(Image image, int bpp){
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        image.mData.rewind();
        mData = stbi_load_from_memory(image.mData, w, h, comp, bpp);

        if (mData == null) {
            System.out.println("Failed to load image: " + stbi_failure_reason());
        }
        mWidth = w.get(0);
        mHeight = h.get(0);
        mBPP = comp.get(0);
        mFormat = mBPP == 3 ? GL11.GL_RGB : GL11.GL_RGBA;
    }

    public ByteBuffer getPixels() {
        if (mData != null)
            mData.rewind();
        return mData;}
    public int getWidth() { return mWidth;}
    public int getHeight() { return mHeight;}
    public int getBPP() { return mBPP;}
    public int getFormat() { return mFormat;}

    private static ByteBuffer clone(ByteBuffer original) {
        if (original == null)
            return null;
        ByteBuffer clone = ByteBuffer.allocateDirect(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }
}
