package com.basic4gl.lib.desktopgl;

import com.basic4gl.lib.util.FileOpener;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Vector;

/**
 * Created by Nate on 11/4/2015.
 */
public class LoadImage {
// File opener
    static FileOpener mFiles = null;


    static void init(FileOpener files) {
        assert (files != null);
        mFiles = files;
    }

    // Return the image format as an OpenGL constant
    static int ImageFormat (Image image) {
        assert (image != null);
        return (image.getBPP () & 0xffff) == 3 ? GL11.GL_RGB : GL11.GL_RGBA;//corona::PF_R8G8B8 ? GL_RGB : GL_RGBA;
    }

    static Image LoadImage (String filename) {
        //TODO implement file opener
        //assert (files != null);

        // Load and return an image.
        // Returns null if image fails to load.
        // The Corona image lib supports:
        //  BMP
        //  GIF
        //  JPEG
        //  PCX
        //  PNG
        //  TGA

        // We convert all images to either RGB (24 bit) or RGBA (32 bit)

        // Load image
        Image image = null;
        filename = mFiles.FilenameForRead (filename, false);
        File file;
        if (filename != null && !filename.equals("") && mFiles.getError().equals("") &&
                (file = new File(filename)).exists() && !file.isDirectory())
            image = new Image(filename);//files.FilenameForRead (filename, false));

        //TODO Check image format; Image constructor currently forces RGBA so this step may be unnecessary
            /*
            if (image != null) {

            // Flipping appears to be necessary..
            //corona::FlipImage (image, corona::CA_X);

            // Convert to either RGB or RGBA
            //int corona::PixelFormat format = (corona::PixelFormat) ((int) image.getFormat () & 0xffff);
            int format = image.getFormat () & 0xffff;
            int newFormat;
            switch (format) {
                case 3://corona::PF_R8G8B8:
                case 4://corona::PF_R8G8B8A8:
                    newFormat = 0;//corona::PF_DONTCARE;
                    break;
                case corona::PF_B8G8R8:
                case corona::PF_I8:
                    newFormat = 3;//corona::PF_R8G8B8;
                    break;
                case corona::PF_B8G8R8A8:
                    newFormat = 4;//corona::PF_R8G8B8A8;
                    break;
            }
            if (newFormat != corona::PF_DONTCARE)
                image = corona::ConvertImage (image, newFormat);        // (Note: Convert image destroys the old image...)
        }*/
        return image;
    }
/*
    corona::Image *ResizeImageForOpenGL (corona::Image *src) {

        // Find first power of 2 greater than or equal to the current width and height
        int swidth = src.getWidth (), sheight = src.getHeight ();
        char *spixels = (char *) src.getPixels ();
        int width = 1, height = 1;
        while (width < swidth)
            width <<= 1;
        while (height < sheight)
            height <<= 1;

        // If image already has useable dimensions, return it
        if (width == swidth && height == sheight)
            return src;
        int bpp = ImageFormat (src) == GL_RGB ? 3 : 4;        // (BYTES per pixel)

        // Would dimension/2 be closer to the original size?
        if (abs (width - swidth) > abs ((width >> 1) - swidth))
            width >>= 1;
        if (abs (height - sheight) > abs ((height >> 1) - sheight))
            height >>= 1;

        // Allocate destination pixels
        corona::Image *dst = corona::CreateImage (width, height, src.getFormat ());
        char *pixels = (char *) dst.getPixels ();

        #ifdef _CAN_INLINE_ASM
        int xdInt, xdFrac, ydInt, ydFrac;
        int xUnit, yUnit;
        __asm {

            pushad

            mov     eax,        [bpp]
            mov     [xUnit],    eax
            mov     eax,        [bpp]
            mul     [swidth]
            mov     [yUnit],    eax

            // Calculate xd
            xor     edx,        edx             // Calculate integer bit
            mov     eax,        [swidth]
            div     [width]
            push    edx                         // Save fraction
            mul     [xUnit]
            mov     [xdInt],    eax
            pop     edx

            xor     eax,        eax             // edx:eax = remainder x 10000h
            div     [width]						// Calculate fraction
            mov     [xdFrac],   eax

            // Calculate yd
            xor     edx,        edx             // Calculate integer bit
            mov     eax,        [sheight]
            div     [height]
            push    edx                         // Save fraction part
            mul     [yUnit]
            mov     [ydInt],    eax
            pop     edx

            xor     eax,        eax             // edx:eax = remainder x 10000h
            div     [height]					// Calculate fraction
            mov     [ydFrac],   eax

            // Setup registers for loop
            mov     esi,        [spixels]       // esi = source
            mov     edi,        [pixels]        // edi = dest
            xor     ebx,        ebx             // ebx = x fraction
            xor     edx,        edx             // edx = y fraction
            mov     ecx,        [height]        // ecx = loop counter
            jecxz   YLoopDone
            YLoop:

            // Loop through columns
            push    ecx
            push    esi

            mov     ecx,        [width]
            jecxz   XLoopDone

            // 2 separate inner loops for speed.
            // * 4 bpp version
            // * 3 bpp version
            mov     eax,        3
            cmp     eax,        [bpp]
            jne     XLoop4

            XLoop3:
            mov     ax,         [esi]           // Copy 3 bytes from source to dest
            mov     [edi],      ax
            mov     al,         [esi + 2]
            mov     [edi + 2],  al
            add     edi,        3

            // Move across
            add     ebx,        [xdFrac]
            jnc     NoXCarry3
            add     esi,        [xUnit]
            NoXCarry3:
            add     esi,        [xdInt]

            // Loop back
            dec     ecx
            jnz     XLoop3
            jmp     XLoopDone

            XLoop4:
            mov     eax,        [esi]           // Copy 4 bytes from source to dest
            mov     [edi],      eax
            add     edi,        4

            // Move across
            add     ebx,        [xdFrac]
            jnc     NoXCarry4
            add     esi,        [xUnit]
            NoXCarry4:
            add     esi,        [xdInt]

            // Loop back
            dec     ecx
            jnz     XLoop4

            XLoopDone:
            pop     esi
            pop     ecx

            // Move down
            add     edx,        [ydFrac]
            jnc     NoYCarry
            add     esi,        [yUnit]
            NoYCarry:
            add     esi,        [ydInt]

            // Loop back
            dec     ecx
            jnz     YLoop
            YLoopDone:

            popad
        }
        #else
        float sx, sy, xd, yd;
        int x, y, offset, soffset;
        xd = (float) swidth / width;
        yd = (float) sheight / height;

        sy = 0;
        offset = 0;
        for (y = 0; y < height; y++) {
            sx = 0;
            soffset = ((int) sy) * swidth * bpp;
            for (x = 0; x < width; x++) {
                int sofs = soffset + ((int) sx) * bpp;
                pixels [offset    ] = spixels [sofs    ];
                pixels [offset + 1] = spixels [sofs + 1];
                pixels [offset + 2] = spixels [sofs + 2];
                if (bpp == 4)
                    pixels [offset + 3] = spixels [soffset + 3];
                offset += bpp;
                sx += xd;
            }
            sy += yd;
        }
        #endif

        // Return new image
        delete src;
        return dst;
    }
*/
    static Vector<Image> SplitUpImageStrip(
            Image image,
            int frameWidth,
            int frameHeight) {
        assert(image != null);

        if (frameWidth <= 0 || frameHeight <= 0) {

            // Default to square image.
            if (image.getWidth() < image.getHeight())
                frameWidth = image.getWidth();
            else
                frameWidth = image.getHeight();
            frameHeight = frameWidth;
        }

        if (frameWidth > image.getWidth())
            frameWidth = image.getWidth();
        if (frameHeight > image.getHeight())
            frameHeight = image.getHeight();

        // Extract images
        Vector<Image> images = new Vector<Image>();
        int bpp = image.getBPP();
        for (int y = image.getHeight() - frameHeight; y >= 0; y -= frameHeight) {
            for (int x = 0; x + frameWidth <= image.getWidth(); x += frameWidth) {

                // Create frame image
                Image dst = new Image(frameWidth, frameHeight, image.getBPP());
                images.add(dst);

                // Copy pixels row by row
                ByteBuffer srcPixels = image.getPixels();
                ByteBuffer dstPixels = dst.getPixels();
                for (int dy = 0; dy < frameHeight; dy++){
                    dstPixels.position(dy * frameWidth * bpp);
                    srcPixels.position(((y + dy) * image.getWidth() + x) * bpp);
                    for (int dx = 0; dx < frameWidth * bpp; dx++);
                        dstPixels.put(srcPixels.get());

                }
            }
        }

        return images;
    }

    static boolean ImageIsBlank(Image image) {
        assert(image != null);

        // Format must include an alpha channel
        switch (image.getFormat() & 0xffff) {
            case GL11.GL_RGBA: //PF_R8G8B8A8:
            //case PF_B8G8R8A8:
                break;
            default:
                return false;
        }

        // Search for non-transparent pixel
        ByteBuffer pixels = image.getPixels();
        for (int i = 0; i < image.getWidth() * image.getHeight(); i++)
            if (pixels.get(i * 4 + 3) != 0)
                return false;

        return true;
    }

    static Image ApplyTransparentColour(Image image, long col) {
        assert(image != null);

        // Clone image and convert to RGBA
        Image dst = new Image(image, 4);//STBImage.STBI_rgb_alpha);
        if (dst.getPixels() == null)
            return image;           // Unable to convert. Just return original image

        // Convert pixels to transparent
        LongBuffer pixels = dst.getPixels().asLongBuffer();
        int count = dst.getWidth() * dst.getHeight();
        col = col & 0x00ffffff;                     // Mask out alpha channel
        for (int i = 0; i < count; i++)
            if ((pixels.get(i) & 0x00ffffff) == col)    // We mask out the alpha before comparing
                pixels.put(i, 0);

        return dst;
    }


}
