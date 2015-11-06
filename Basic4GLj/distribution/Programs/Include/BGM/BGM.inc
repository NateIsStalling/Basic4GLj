' BGM loader.
'
' "Basic4GL Model format". A simple, easy model format I created for Basic4GL.
 
struc BGM_Vertex
    dim v# (2)              ' Vertex position
    dim t# (1)              ' Texture coordinates
endstruc
 
struc BGM_PolyVertex
    dim vertexIndex, normIndex
endstruc
 
struc BGM_Poly
    dim firstVertex, vertexCount
endstruc
 
struc BGM_Model
    dim vertexCount,        BGM_Vertex &vertex()
    dim normCount,          &norm#()()
    dim polyVertexCount,    BGM_PolyVertex  &polyVertex()
    dim polyCount,          BGM_Poly        &poly()
    dim scale#
    dim displayList
endstruc

function BGM_Model& LoadBGM(dir$, filename$)

    ' Working variables
    dim i, i2, file, BGM_Model& model
    
    ' Open file
    file = OpenFileRead (dir$ + fileName$)
    if FileError () <> "" then print FileError (): end: endif
         
    ' Allocate model
    alloc model
    model.scale# = 1
 
    ' Vertices
    model.vertexCount = ReadInt (file)
    alloc model.vertex, model.vertexCount
    for i = 0 to model.vertexCount - 1
        for i2 = 0 to 2: model.vertex (i).v#(i2) = ReadFloat (file): next
        for i2 = 0 to 1: model.vertex (i).t#(i2) = ReadFloat (file): next
    next
    
    ' Normals
    model.normCount = ReadInt (file)
    alloc model.norm#, model.normCount, 2
    for i = 0 to model.normCount - 1
        for i2 = 0 to 2: model.norm# (i)(i2) = ReadFloat (file): next
    next
    
    ' Poly vertices
    model.polyVertexCount = ReadInt (file)
    alloc model.polyVertex, model.polyVertexCount
    for i = 0 to model.polyVertexCount - 1
        model.polyVertex (i).vertexIndex = ReadInt (file)
        model.polyVertex (i).normIndex = ReadInt (file)
    next
    
    ' Polygons
    model.polyCount = ReadInt (file)
    alloc model.poly, model.polyCount
    for i = 0 to model.polyCount - 1
        model.poly (i).firstVertex = ReadInt (file)
        model.poly (i).vertexCount = ReadInt (file)
    next
    CloseFile (file)
    
    return &model
end function
    
sub DrawBGM(BGM_Model& model)

    dim i, i2, BGM_Poly& poly, BGM_PolyVertex& polyVertex, BGM_Vertex& vertex
    
    ' Draw model       
    glEnable(GL_CULL_FACE)
    glCullFace(GL_BACK)
    glPushMatrix ()
    glScalef (model.scale#, model.scale#, model.scale#)
    if model.displayList <> 0 then
        ' If a display list exists, use it
        glCallList (model.displayList)
    else        
    
        ' Otherwise, create a display list, and render the model into it
        model.displayList = glGenLists (1)
        glNewList (model.displayList, GL_COMPILE_AND_EXECUTE)
 
        for i = 0 to model.polyCount - 1
            &poly = &model.poly (i)
            glBegin (GL_TRIANGLE_FAN)
            for i2 = 0 to poly.vertexCount - 1
                &polyVertex = &model.polyVertex (poly.firstVertex + i2)
                &vertex = &model.vertex (polyVertex.vertexIndex)
                glNormal3fv (model.norm# (polyVertex.normIndex))
                glVertex3fv (vertex.v#)
            next
            glEnd ()
        next
        glEndList ()
    endif
 
    glPopMatrix ()
end sub