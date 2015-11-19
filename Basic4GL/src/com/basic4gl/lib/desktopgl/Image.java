package com.basic4gl.lib.desktopgl;

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

            if (mData == null) {
                System.out.println("Failed to load image: " + filename);
                System.out.println(stbi_failure_reason());
            }
            mWidth = w.get(0);
            mHeight = h.get(0);
            mBPP = comp.get(0);
            mFormat = mBPP == 3 ? GL11.GL_RGB : GL11.GL_RGBA;

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
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        image.mData.rewind();
        mData = stbi_load_from_memory(image.mData, w, h, comp, 0);

        if (mData == null) {
            System.out.println("Failed to load image: " + stbi_failure_reason());
        }
        mWidth = w.get(0);
        mHeight = h.get(0);
        mBPP = comp.get(0);
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
        mData.rewind();
        return mData;}
    public int getWidth() { return mWidth;}
    public int getHeight() { return mHeight;}
    public int getBPP() { return mBPP;}
    public int getFormat() { return mFormat;}

}
