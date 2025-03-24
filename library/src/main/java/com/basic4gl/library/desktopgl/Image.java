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
    /**
     * PCX image parser does not support transparency; see PCXImageParser.getImageInfo()
     */
    private static final int PCX_BYTES_PER_PIXEL = 3;

    private ByteBuffer data;
    private int width, height;
    private int format;
    private int bpp;

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
                data = stbi_load(filename, w, h, comp, 0);

                width = w.get(0);
                height = h.get(0);
                bpp = comp.get(0);
                if (data == null) {
                    errorMessage = stbi_failure_reason();
                }
            }

            format = bpp == 3 ? GL11.GL_RGB : GL11.GL_RGBA;

            if (data == null) {
                System.out.println("Failed to load image: " + filename);
                System.out.println(errorMessage);
            } else {
                // There's an issue with loading images upside down
                // So here we change the order of the bytes to flip the image that we just loaded
                ByteBuffer temp = ByteBuffer.allocateDirect(data.capacity());
                data.rewind(); // copy from the beginning
                temp.rewind();
                for (int dy = 0; dy < height; dy++) {
                    temp.position(dy * width * bpp);
                    data.position((height - dy - 1) * width * bpp);
                    for (int dx = 0; dx < width * bpp; dx++) {
                        temp.put(data.get());
                    }
                }
                temp.rewind();
                data = temp;
            }
        }
    }

    public Image(int width, int height, int bpp) {
        this.width = width;
        this.height = height;
        this.bpp = bpp;
        format = this.bpp == 3 ? GL11.GL_RGB : GL11.GL_RGBA;
        data = BufferUtils.createByteBuffer(width * height * bpp);
    }

    public Image(Image image) {
        data = clone(image.data);

        if (data == null) {
            System.out.println("Failed to load image: " + stbi_failure_reason());
        }
        width = image.width;
        height = image.height;
        bpp = image.bpp;
        format = bpp == 3 ? GL11.GL_RGB : GL11.GL_RGBA;
    }

    public Image(Image image, int bpp) {
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        image.data.rewind();
        data = stbi_load_from_memory(image.data, w, h, comp, bpp);

        if (data == null) {
            System.out.println("Failed to load image: " + stbi_failure_reason());
        }
        width = w.get(0);
        height = h.get(0);
        this.bpp = comp.get(0);
        format = this.bpp == 3 ? GL11.GL_RGB : GL11.GL_RGBA;
    }

    public ByteBuffer getPixels() {
        if (data != null) {
            data.rewind();
        }
        return data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBPP() {
        return bpp;
    }

    public int getFormat() {
        return format;
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

        data = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * bpp);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                data.put((byte) ((pixel >> 16) & 0xFF)); // Red component
                data.put((byte) ((pixel >> 8) & 0xFF)); // Green component
                data.put((byte) (pixel & 0xFF)); // Blue component

                if (bpp == 4) {
                    data.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
                }
            }
        }

        data.flip();

        width = image.getWidth();
        height = image.getHeight();
        this.bpp = bpp;
    }
}
