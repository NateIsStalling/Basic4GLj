package com.basic4gl.library.desktopgl.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.glMultMatrixf;
import static org.lwjgl.opengl.GL11.glTranslated;

public class GLUtil {
    public static void gluPerspective(float fovY, float aspect, float zNear, float zFar) {
        perspectiveGL(fovY, aspect, zNear, zFar);
    }

    /**
     * gluPerspective replacement
     * @param fovY
     * @param aspect
     * @param zNear
     * @param zFar
     */
    public static void perspectiveGL(double fovY, double aspect, double zNear, double zFar) {
        double fW, fH;

        // fH = tan( (fovY / 2) / 180 * pi ) * zNear;
        fH = Math.tan(fovY / 360.0 * Math.PI) * zNear;
        fW = fH * aspect;

        GL11.glFrustum(-fW, fW, -fH, fH, zNear, zFar);
    }

    public static boolean isExtensionSupported(String extension) {
        String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
        if (extensions == null) {
            return false;
        }
        // Search for extension in list.
        // Use basic trick of adding a space before and after each.
        extensions = " " + extensions + " ";
        extension = " " + extension + " ";
        return extensions.contains(extension);
    }

    public static void gluOrtho2D(int i, int i1, int i2, int i3) {
        // TODO review
        GL11.glOrtho(i, i1, i2, i3, -1, 1);
    }

    /**
     * gluLookAt replacement
     * @param eyeX
     * @param eyeY
     * @param eyeZ
     * @param centerX
     * @param centerY
     * @param centerZ
     * @param upX
     * @param upY
     * @param upZ
     */
    public static void lookAt(
            double eyeX, double eyeY, double eyeZ,
            double centerX, double centerY, double centerZ,
            double upX, double upY, double upZ) {

        // forward = normalize(center - eye)
        double fx = centerX - eyeX;
        double fy = centerY - eyeY;
        double fz = centerZ - eyeZ;
        double flen = Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (flen == 0.0) return;
        fx /= flen; fy /= flen; fz /= flen;

        // up = normalize(up)
        double ulen = Math.sqrt(upX * upX + upY * upY + upZ * upZ);
        if (ulen == 0.0) return;
        upX /= ulen; upY /= ulen; upZ /= ulen;

        // side = normalize(forward x up)
        double sx = fy * upZ - fz * upY;
        double sy = fz * upX - fx * upZ;
        double sz = fx * upY - fy * upX;
        double slen = Math.sqrt(sx * sx + sy * sy + sz * sz);
        if (slen == 0.0) return;
        sx /= slen; sy /= slen; sz /= slen;

        // recompute orthogonal up = side x forward
        double ux = sy * fz - sz * fy;
        double uy = sz * fx - sx * fz;
        double uz = sx * fy - sy * fx;

        // Column-major matrix for OpenGL
        FloatBuffer m = BufferUtils.createFloatBuffer(16);
        m.put((float) sx).put((float) ux).put((float) -fx).put(0.0f);
        m.put((float) sy).put((float) uy).put((float) -fy).put(0.0f);
        m.put((float) sz).put((float) uz).put((float) -fz).put(0.0f);
        m.put(0.0f).put(0.0f).put(0.0f).put(1.0f);
        m.flip();

        glMultMatrixf(m);
        glTranslated(-eyeX, -eyeY, -eyeZ);
    }
}
