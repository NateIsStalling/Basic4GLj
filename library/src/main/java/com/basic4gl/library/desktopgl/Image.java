package com.basic4gl.library.desktopgl;

import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.formats.pcx.PcxImageParser;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * Created by Nate on 11/4/2015.
 */
public class Image {
    private static final int PCX_BYTES_PER_PIXEL =
            3; // PCX image parser does not support transparency; see PCXImageParser.getImageInfo()

    private ByteBuffer mData;
    private int mWidth, mHeight;
    private int mFormat;
    private int mBPP;

    public Image(String filename) {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        String errorMessage = "";

        File file = new File(filename);

        if (file.exists()) {
            // special handling for image formats not supported by STB
            if (filename.endsWith(".pcx")) {
                try {
                    List<BufferedImage> imageList = new PcxImageParser().getAllBufferedImages(file);
                    if (!imageList.isEmpty()) {
                        BufferedImage image = imageList.get(0);

                        loadFromBufferedImage(image, PCX_BYTES_PER_PIXEL);
                    } else {
                        errorMessage = "Unknown error";
                    }
                } catch (ImageReadException e) {
                    errorMessage = e.getMessage();
                } catch (IOException e) {
                    errorMessage = e.getMessage();
                }
            } else {
                mData = stbi_load(filename, w, h, comp, 0);

                mWidth = w.get(0);
                mHeight = h.get(0);
                mBPP = comp.get(0);
                if (mData == null) {
                    errorMessage = stbi_failure_reason();
                }
            }

            mFormat = mBPP == 3 ? GL11.GL_RGB : GL11.GL_RGBA;

            if (mData == null) {
                System.out.println("Failed to load image: " + filename);
                System.out.println(errorMessage);
            } else {
                // There's an issue with loading images upside down
                // So here we change the order of the bytes to flip the image that we just loaded
                ByteBuffer temp = ByteBuffer.allocateDirect(mData.capacity());
                mData.rewind(); // copy from the beginning
                temp.rewind();
                for (int dy = 0; dy < mHeight; dy++) {
                    temp.position(dy * mWidth * mBPP);
                    mData.position((mHeight - dy - 1) * mWidth * mBPP);
                    for (int dx = 0; dx < mWidth * mBPP; dx++) {
                        temp.put(mData.get());
                    }
                }
                temp.rewind();
                mData = temp;
            }
        }
    }

    public Image(int width, int height, int bpp) {
        mWidth = width;
        mHeight = height;
        mBPP = bpp;
        mFormat = mBPP == 3 ? GL11.GL_RGB : GL11.GL_RGBA;
        mData = BufferUtils.createByteBuffer(width * height * bpp);
    }

    public Image(Image image) {
        mData = clone(image.mData);

        if (mData == null) {
            System.out.println("Failed to load image: " + stbi_failure_reason());
        }
        mWidth = image.mWidth;
        mHeight = image.mHeight;
        mBPP = image.mBPP;
        mFormat = mBPP == 3 ? GL11.GL_RGB : GL11.GL_RGBA;
    }

    public Image(Image image, int bpp) {
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
        if (mData != null) {
            mData.rewind();
        }
        return mData;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getBPP() {
        return mBPP;
    }

    public int getFormat() {
        return mFormat;
    }

    private static ByteBuffer clone(ByteBuffer original) {
        if (original == null) {
            return null;
        }
        ByteBuffer clone = ByteBuffer.allocateDirect(original.capacity());
        original.rewind(); // copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    /**
     * @param image
     * @param bpp Bytes per pixel - 4 for RGBA, 3 for RGB
     */
    private void loadFromBufferedImage(BufferedImage image, int bpp) {

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        mData = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * bpp);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                mData.put((byte) ((pixel >> 16) & 0xFF)); // Red component
                mData.put((byte) ((pixel >> 8) & 0xFF)); // Green component
                mData.put((byte) (pixel & 0xFF)); // Blue component

                if (bpp == 4) {
                    mData.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
                }
            }
        }

        mData.flip();

        mWidth = image.getWidth();
        mHeight = image.getHeight();
        mBPP = bpp;
    }
}
