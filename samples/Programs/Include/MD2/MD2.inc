include Include/MD2/md2structures.inc
include Include/MD2/md2routines.inc

function MD2_model &LoadMD2(theFileDir$, theFilename$)
       
    ' Open file
    dim theFile
    theFile = OpenFileRead (theFileDir$ + theFileName$)
    if FileError() <> "" then 
        printr "Failed to open " + theFileName$
        printr "Reason: " + FileError ()
        end
    endif
    
    ' Allocate model
    dim MD2_model &theModel
    alloc theModel

    ' Read header
    dim MD2_header &theHeader
    &theHeader = &theModel.header
    LoadMD2Header(theHeader, theFile)
    
    ' Read data    
    LoadMD2Skins(theModel, theFile, theFileDir$)
    LoadMD2ST(theModel, theFile)
    LoadMD2Tris(theModel, theFile)
    LoadMD2Frames(theModel, theFile)

    ' Close file and return
    CloseFile (theFile)   
    
    return &theModel
    
end function   

sub DrawMD2(MD2_Model &theModel, theFrameNum, theFrameNum2, theFrameFactor#, theSkin)

    dim MD2_header &theHeader, MD2_frame &theFrame, MD2_frame &theFrame2
    &theHeader = &theModel.header
    &theFrame  = &theModel.frames (theFrameNum)
    &theFrame2 = &theModel.frames (theFrameNum2)

    ' Bind texture    
    glEnable (GL_CULL_FACE)
    glCullFace (GL_FRONT)
    glEnable (GL_TEXTURE_2D)
    glBindTexture (GL_TEXTURE_2D, theModel.skins (theSkin))
    
    ' Setup texture scaling
    glMatrixMode (GL_TEXTURE)
    glPushMatrix ()
    glScalef (1.0 / theHeader.skinWidth, -1.0 / theHeader.skinHeight, 1)
    glMatrixMode (GL_MODELVIEW)
    
    ' Apply model scaling  
    dim tempVec#(2)
    glPushMatrix ()
    glRotatef( -90, 1, 0, 0 )
    glRotatef( -90, 0, 0, 1 )
    tempVec# = theFrame.translate# * (1 - theFrameFactor#) + theFrame2.translate# * theFrameFactor#
    glTranslatef (tempVec# (0), tempVec# (1), tempVec# (2))
    tempVec# = theFrame.scale# * (1 - theFrameFactor#) + theFrame2.scale# * theFrameFactor#
    glScalef     (tempVec# (0), tempVec# (1), tempVec# (2))
    
    ' Render each triangle  
    dim i, i2, MD2_Triangle &theTriangle
    glBegin (GL_TRIANGLES)    
        for i = 0 to theHeader.num_tris - 1
            &theTriangle = &theModel.tris (i)
            for i2 = 0 to 2
                glTexCoord2fv (theModel.st# (theTriangle.index_st (i2)))
                glVertex3fv (theFrame.verts (theTriangle.index_xyz (i2)).v# * (1 - theFrameFactor#) + theFrame2.verts (theTriangle.index_xyz (i2)).v# * theFrameFactor#)
            next
        next
    glEnd ()
    
    ' Restore modelview matrix
    glPopMatrix ()
    
    ' Restore texture matrix
    glMatrixMode (GL_TEXTURE)
    glPopMatrix ()
    glMatrixMode (GL_MODELVIEW)
end sub