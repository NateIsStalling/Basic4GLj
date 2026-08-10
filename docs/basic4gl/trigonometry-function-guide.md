# Programmer's Guide: Trigonometry Functions

> [!TIP]
>
> For additional math routines like `cos`, `sin`, `tan` and `log`, see the [Standard Function Guide
](./standard-function-guide.md)

## Vector and Matrix routines
Basic4GL contains built in support for matrix and vector arithmetic, through a library of trigonometry functions, and also through extensions to standard mathematical operators (+, -, * e.t.c) to work with vector and matrix types.

### Vector storage format
Vectors are stored as an array of reals. For example:
```
dim vec#(3)
vec# = vec4 (1, 2, 3, 1) ' Create a vector and assign it to vec#
```

To be eligible for use with the built-in trigonometry functions, the array must have 2, 3 or 4 elements. (Remember that declaring an array as size 3 actually results in 4 elements, 0 through 3 inclusive).

Element `0` stores the `x` component, element `1` stores `y` component, `2` stores `z` and `3` stores `w`.

Certain trigonometry functions that operate on 4 component vectors will automatically substitute `z = 0` and/or `w = 1` when short version vectors are passed in.

### Matrix storage format
A matrix is a 4 x 4 array of reals, and must always be "DIM"med as:
```
matrixname#(3)(3)
```

Example:
```
dim matrix#(3)(3)
matrix# = IdentityMatrix () ' Assign a matrix to matrix#
```
The first array dimension corresponds to the x coordinate of the matrix, and the second to the y.

Basic4GL vector and matrix storage format and operations are designed to mirror those of OpenGL.
As such vectors are multiplied as column vectors on the right hand side of matrices. Matrices are stored as an array of column vectors.

## Creating vectors
Vectors are just arrays, so you can read from and write to them like any other array.
```
dim v#(3), i
for i = 0 to 3: v#(i) = i: next ' Create a (0 1 2 3) vector
dim v1#(3), v2#(3), dotProd#
dotProd# = v1#(0)*v2#(0) + v1#(1)*v2#(1) + v1#(2)*v2#(2)
' Calculate the vector dot product
' (Note: we could also have said dotProd# = v1# * v2#)
```

However, there are a set of routines for creating vectors quickly and simply:

### vec4, vec3 and vec2
`vec4(x, y, z, w)` returns a 4 component vector with `x`, `y`, `z` and `w` components initialised accordingly.

`vec3(x, y, z)` returns a 3 component vector with `x`, `y` and `z` components initialised accordingly.

`vec2(x, y)` returns a 2 component vector with `x` and `y` components initialised accordingly.

Examples:

```
dim lightsource#(3)
lightsource# = vec4(0, 100, 0, 1) ' Lightsource at (0 100 0)
```

This is exactly equivalent to:
```
dim lightsource#(3)
lightsource#(0) = 0
lightsource#(1) = 100
lightsource#(2) = 0
lightsource#(3) = 1
```
The first version is simply a more compact alternative.

## Extended mathematics operators
Certain mathematics operators have been extended to accept vectors and or matrices as input, and (where appropriate) return a vector or a matrix as a result.

- _vec_ = A vector
- _matrix_ = A matrix
- _real_ = A real value

| Expression        | Result                                                                                                     |
|-------------------|------------------------------------------------------------------------------------------------------------|
| -vec              | Returns vec negated. That is vec scaled by -1                                                              |
| -matrix           | Returns matrix negated. I.e matrix scaled by -1                                                            |
| vec * real        | Returns vector scaled by real                                                                              |
| real * vec        | Returns vector scaled by real                                                                              |
| matrix * real     | Returns matrix scaled by real                                                                              |
| real * matrix     | Returns matrix scaled by real                                                                              |
| matrix * vec      | Returns vec multiplied as a column vector on the right hand side of matrix. The result is another vector.  |
| matrix1 * matrix2 | Returns matrix2 multiplied on the right hand side of matrix1. The result is another matrix.                |
| vec1 * vec2       | Returns the dot product of vec1 and vec2, as a real value.                                                 |
| vec / real        | Returns vec scaled by 1 / real                                                                             |
| matrix / real     | Returns matrix scaled by 1 / real                                                                          |
| vec1 + vec2       | Returns vec2 added to vec1 as a vector                                                                     |
| matrix1 + matrix2 | Returns matrix2 added to matrix1 as matrix                                                                 |
| vec1 - vec2       | Returns vec2 subtracted from vec1 as a vector                                                              |
| matrix1 - matrix2 | Returns matrix2 subtracted from matrix1 as a matrix                                                        |

## Matrix creation functions
These are based on the OpenGL matrix functions (`glTranslate-`, `glRotate-`, e.t.c).

### MatrixZero
`MatrixZero ()` returns a matrix where every element is zero.

```
dim m#(3)(3)
m# = MatrixZero ()
```

### MatrixIdentity
`MatrixIdentity ()` returns the identity matrix.

### MatrixScale
`MatrixScale (scale)` returns a scale matrix

### MatrixTranslate
`MatrixTranslate (x, y, z)` returns a translation matrix.

### MatrixRotateX, MatrixRotateY and MatrixRotateZ
`MatrixRotateX (angle)` returns a matrix that rotates anticlockwise around the positive X axis by `angle` degrees.

Likewise, `MatrixRotateY (angle)` and `MatrixRotateZ (angle)` return matrices that rotate around their respective axes.

There is no function for creating a rotation matrix around an arbitrary axis (like `glRotate-` in OpenGL) because I'm not smart enough! :-) (If anyone wants to send me the maths, I'll add one...)

### MatrixBasis
`MatrixBasis (vecx, vecy, vecz)` creates a matrix from 3 basis vectors.

### MatrixCrossProduct
`MatrixCrossProduct (vec)` creates a cross product matrix for `vec`.

This matrix has the property that when multiplied with a vector `v`, the result is `vec x v`.

That is the cross product of `vec` and `v`.

## Using Matrices with OpenGL

### glLoadMatrixf, glMultMatrixf
You can copy a standard matrix into OpenGL, replacing the perspective, model-view or texture matrix (whatever was last selected by `glMatrixMode ()`).
You can also multiply the current OpenGL matrix with a standard matrix.
The new matrix will transform vertices passed to OpenGL (or texture coordinates for the texture matrix), just as if you had built the matrix with `glRotate-`, `glTranslate-`, `glScale-`,... commands.

`glLoadMatrixf (matrix)` will replace the current OpenGL matrix with matrix.

`glMultMatrixf (matrix)` will multiply the current OpenGL matrix by matrix. The resulting matrix replaces the previous OpenGL matrix.

(Note: `glLoadMatrixd` and `glMultMatrixd` also work. However, as Basic4GL works with floats internally rather than doubles, there is no particular advantage in using these functions.)

#### Examples:
The following examples all draw a square 10 units "into the screen", rotated anticlockwise by 20 degrees.

##### Example 1.

```
' Standard OpenGL matrix routines
glLoadIdentity ()
glTranslatef (0, 0, -10)
glRotatef (20, 0, 0, 1)
glBegin (GL_QUADS)
glVertex2f (-1, 1): glVertex2f (-1, -1): glVertex2f (1, -1): glVertex2f (1, 1)
glEnd ()
```

##### Example 2.

```
' Using glMultMatrixf to multiply in basic matrices
glLoadMatrixf (MatrixIdentity ())
glMultMatrixf (MatrixTranslate (0, 0, -10))
glMultMatrixf (MatrixRotateZ (20))
glBegin (GL_QUADS)
glVertex2f (-1, 1): glVertex2f (-1, -1): glVertex2f (1, -1): glVertex2f (1, 1)
glEnd ()
```

##### Example 3.

```
' Build a complete matrix and load into OpenGL in one go
glLoadMatrixf (MatrixTranslate (0, 0, -10) * MatrixRotateZ (20))
glBegin (GL_QUADS)
glVertex2f (-1, 1): glVertex2f (-1, -1): glVertex2f (1, -1): glVertex2f (1, 1)
glEnd ()
```

##### Example 4.

```
' Matrix stored in a variable
dim m#(3)(3)
m# = MatrixTranslate (0, 0, -10) * MatrixRotateZ (20)
glLoadMatrixf (m#)
glBegin (GL_QUADS)
glVertex2f (-1, 1): glVertex2f (-1, -1): glVertex2f (1, -1): glVertex2f (1, 1)
glEnd ()
Alternatively we could simply transform the vertices before passing them to OpenGL

dim m#(3)(3)
m# = MatrixTranslate (0, 0, -10) * MatrixRotateZ (20)
glBegin (GL_QUADS)
glVertex3fv (m# * vec3(-1, 1, 0))
glVertex3fv (m# * vec3(-1, -1, 0))
glVertex3fv (m# * vec3(1, -1, 0))
glVertex3fv (m# * vec3(1, 1, 0))
glEnd ()
```

Which works just as well.
However, keep in mind that if we perform the transformations ourselves we deny OpenGL the opportunity to perform the transformations, and make use of any optimisations such as hardware transformations supported on modern 3D graphics cards.

## Other trigonometry functions

### CrossProduct
`CrossProduct (vec1, vec2)` returns the vector cross product of `vec1` and `vec2`.
The result is a vector.

### Length
`Length (vec)` returns the length of `vec`.
This is equivalent to `sqr(vec*vec)`

### Normalize
`Normalize (vec)` returns `vec` scaled to length `1`.
This is equivalent to `vec / Length(vec)`

### Determinant
`Determinant (matrix)` returns the matrix determinant of `matrix`. The result is a real value.

### Transpose
`Transpose (matrix)` returns `matrix` transposed. (That is matrix mirrored about the diagonal.)

### RTInvert
`RTInvert (matrix)` returns `matrix` inverted, for any matrix containing only rotations and translations.
If matrix contains any other transformations apart from rotations and translations then the result is undefined, and will not be the inverse of `matrix`.

### Orthonormalize
`Orthonormalize (matrix)` returns an orthonormal matrix by performing a series of normalizations and cross products on the basis vectors of `matrix`.

This is useful for matrices that are nearly orthonormal. For example to ensure a matrix (that should be orthonormal) hasn't accumulated rounding errors after a large number of transformations.

## Handling the w coordinate
Some of the above functions (such as `CrossProduct`) and operators (such as `+`) take two vectors and return a single result vector.

Basic4GL sets the `w` coordinate of the resulting vector as follows:

- If `w = 0` for both input vectors, then `w = 0` for the resulting vector
- Otherwise `w` is set to `1`

If this is not the behaviour that you want, you will have to set the `w` coordinate manually.

There is no special treatment of `w` when multiplying a vector by a matrix, `w` is calculated like any other component.

You will need to divide through by `w` manually if this is the behaviour you require.
```
dim vec#(3), matrix#(3)(3)
...
vec# = matrix# * vec#		' Multiply vector by matrix
vec# = vec# / vec#(3)		' Divide through by w
```


## Credits
Basic4GL, Copyright (C) 2003-2007 Tom Mulgrew

_Programmer's guide_

26-Jul-2008
Tom Mulgrew

Documentation modified for Markdown formatting by Nathaniel Nielsen