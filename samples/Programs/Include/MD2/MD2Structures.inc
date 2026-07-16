struc MD2_header
    
    ' File information
    dim ident
    dim version
    dim skinwidth                   ' Width of skin texture (pixels)
    dim skinheight                  ' Height of skin texture (pixels)
    dim framesize                   
    
    ' Number of different components
    dim num_skins                   ' Skins
    dim num_xyz                     ' Vertices (x, y, z)
    dim num_st                      ' Texture coordinates (s, t)
    dim num_tris                    ' Triangles
    dim num_glcmds                  ' GL Commands (which we aren't going to use)
    dim num_frames                  ' Frames
    
    ' File offset of different components
    dim ofs_skins                   ' Skins 
    dim ofs_st                      ' Texture coordinates (s,t)
    dim ofs_tris                    ' Triangles
    dim ofs_frames                  ' Frames
    dim ofs_glcmds                  ' GL Commands (which we aren't going to use)
    dim ofs_end                     ' End of file
endstruc

struc MD2_vertex
    dim v#(2)                                   ' Vertex
    dim normal                                  ' Index of normal (into some lookup table. I can't find any info on this table, so normals and lighting aren't currently implemented.)
endstruc

struc MD2_triangle
    dim index_xyz(2)                            ' Index of triangle vertices
    dim index_st(2)                             ' Index of texture coordinates
endstruc

struc MD2_frame
    dim scale#(2), translate#(2)                ' Scaling and translation
    dim name$                                   ' Display name
    dim MD2_vertex &verts()                     ' Vertices
endstruc

struc MD2_model
    dim MD2_header header                       ' Header data
    dim &skins ()                               ' Skin textures
    dim &st# ()()                               ' Texture coordinates (compressed)
    dim MD2_triangle &tris ()                   ' Triangles
    dim MD2_frame &frames()                     ' Frames
endstruc
            
