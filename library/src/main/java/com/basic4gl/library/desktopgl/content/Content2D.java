package com.basic4gl.library.desktopgl.content;

/**
 * Copyright (C) Tom Mulgrew, 2016 (tmulgrew@slingshot.co.nz)
 *
 * 2D content to draw
 */
public class Content2D {

    float z;
    int bitFlag;
    Content2DDrawHandler drawCallback;

    Content2D(float z, int bitFlag, Content2DDrawHandler drawCallback)
    {
        this.z = z;
        this.bitFlag = bitFlag;
        this.drawCallback = drawCallback;
    }
}
