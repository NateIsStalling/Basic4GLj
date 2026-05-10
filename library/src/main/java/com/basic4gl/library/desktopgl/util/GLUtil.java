package com.basic4gl.library.desktopgl.util;

import org.lwjgl.opengl.GL11;

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
}
