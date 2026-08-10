function ReadString$(file, numChars)
    dim i, tempStr$
    for i = 1 to numChars: tempStr$ = tempStr$ + ReadChar (file): next
    while Len(tempStr$) > 0 and Right$(tempStr$, 1) <= " "            
        tempStr$ = Left$(tempStr$, Len(tempStr$) - 1)
    wend
    return tempStr$
end function

sub LoadMD2Header(MD2_header &theHeader, theFile)
    theHeader.ident       = ReadInt (theFile)
    theHeader.version     = ReadInt (theFile)
    theHeader.skinwidth   = ReadInt (theFile)
    theHeader.skinheight  = ReadInt (theFile)
    theHeader.framesize   = ReadInt (theFile)
    theHeader.num_skins   = ReadInt (theFile)
    theHeader.num_xyz     = ReadInt (theFile)
    theHeader.num_st      = ReadInt (theFile)
    theHeader.num_tris    = ReadInt (theFile)
    theHeader.num_glcmds  = ReadInt (theFile)
    theHeader.num_frames  = ReadInt (theFile)
    theHeader.ofs_skins   = ReadInt (theFile)
    theHeader.ofs_st      = ReadInt (theFile)
    theHeader.ofs_tris    = ReadInt (theFile)
    theHeader.ofs_frames  = ReadInt (theFile)
    theHeader.ofs_glcmds  = ReadInt (theFile)
    theHeader.ofs_end     = ReadInt (theFile)
end sub     

sub LoadMD2Skins(MD2_model &theModel, theFile, theFileDir$)  
    ' Seek to skin texture names
    Seek (theFile, theModel.header.ofs_skins)

    ' Allocate skin texture handles
    alloc theModel.skins, theModel.header.num_skins - 1
    
    ' Load textures
    dim i, skinName$
    for i = 0 to theModel.header.num_skins - 1
    
        ' Read texture name
        skinName$ = ReadString$(theFile, 64)

        ' Load texture
        theModel.skins (i) = LoadMipmapTexture (theFileDir$ + skinName$)
        if theModel.skins (i) <= 0 then 
            print "Failed to load texture: " + skinName$
            end
        endif  
    next               
end sub       
   
sub LoadMD2ST(MD2_Model &theModel, theFile)
    ' Allocate texture coordinates    
    alloc theModel.st#, theModel.header.num_st - 1, 1
    
    ' Seek to texture coordinates
    Seek (theFile, theModel.header.ofs_st)

    ' Load texture coordinates
    dim i
    for i = 0 to theModel.header.num_st - 1
        theModel.st# (i)(0) = ReadWord (theFile)
        theModel.st# (i)(1) = ReadWord (theFile)
    next    
end sub

sub LoadMD2Tris(MD2_Model &theModel, theFile)
    ' Allocate triangles
    alloc theModel.tris, theModel.header.num_tris - 1
    
    ' Seek to triangles
    Seek (theFile, theModel.header.ofs_tris)
    
    ' Load triangles      
    dim i, i2
    for i = 0 to theModel.header.num_tris - 1
        for i2 = 0 to 2
            theModel.tris (i).index_xyz (i2) = ReadWord (theFile)
        next
        for i2 = 0 to 2
            theModel.tris (i).index_st (i2) = ReadWord (theFile)
        next
    next
end sub   

sub LoadMD2Frames(MD2_Model &theModel, theFile)

    ' Allocate frames
    alloc theModel.frames, theModel.header.num_frames - 1
    
    ' Seek to frames
    Seek (theFile, theModel.header.ofs_frames)
    
    ' Load frames  
    dim i, i2, i3, MD2_Frame &theFrame
    for i = 0 to theModel.header.num_frames - 1 
        &theFrame = &theModel.frames (i)
        
        for i2 = 0 to 2
            theFrame.scale# (i2) = ReadFloat (theFile)
        next
        for i2 = 0 to 2
            theFrame.translate# (i2) = ReadFloat (theFile)
        next
        theFrame.name$ = ReadString$(theFile, 16)
        
        ' Allocate vertices
        alloc theFrame.verts, theModel.header.num_xyz
        
        ' Read vertices
        for i2 = 0 to theModel.header.num_xyz - 1
            for i3 = 0 to 2
                theFrame.verts (i2).v# (i3) = ReadByte (theFile)
            next
            theFrame.verts (i2).normal = ReadByte (theFile)
        next    
    next
end sub
