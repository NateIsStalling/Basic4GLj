# OpenGL Guide

This document will not teach you OpenGL!

Instead it is designed to give you the information you need to use OpenGL tutorials and example programs from other sources in Basic4GL.

> [!TIP]
>
> One such source is Neon Helium Productions OpenGL tutorials http://nehe.gamedev.net.
>
> Basic4GL conversions of tutorials 2 - 11 are available in the sample program directory (named nehe2, nehe3, e.t.c).
>
> The next important resource is an OpenGL function reference.
> You can find one for the LWJGL 3 OpenGL implementation at https://javadoc.lwjgl.org/org/lwjgl/opengl/GL11.html

> [!WARNING]
>
> _Nehe9.gb_ and _Nehe17.gb_ sample programs have known issues and may crash.
>
> Please report issues with OpenGL sample programs to https://github.com/NateIsStalling/Basic4GLj/issues

## OpenGL support

> [!NOTE]
>
> Basic4GLj uses LWJGL 3 for OpenGL v1.1 support

Basic4GL supports version OpenGL v1.1, and supports all functions of the Win32 OpenGL implementation, except for:

- glBitmap
- glDrawElements
- glDrawPixels
- glEdgeFlagPointer
- glGetMap- (range)
- glGetPixelMap- (range)
- glGetTexImage
- glIndexPointer
- glInterleavedArrays
- glMap- (range)
- glNormalPointer
- glPixelMap- (range)
- glReadPixels
- glTexCoordPointer
- glTexImage1d
- glTexSubImage1d
- glVertexPointer

> [!CAUTION]
>
> `glCallList` and `glCallLists` are not supported in the current Basic4GLj version
> and may result in compile or runtime errors.

### OpenGL Extension support:
Basic4GL also supports the following functions from the `GL_ARB_multitexture` extension:

- glMultiTexCoord2f
- glMultiTexCoord2d
- glActiveTexture

### OpenGL GLU function support:

> [!IMPORTANT]
>
> OpenGL GLU constants are unavailable, they are unsupported by the current version of LWJGL that is used by Basic4GLj.
>
> GLU functions available in previous versions of Basic4GL are available with modifications.

| Function        | Basic4GL for Windows | Basic4GLj          |
|-----------------|----------------------|--------------------|
| gluOrtho2d      | Supported            | Supported [^1]     |
| gluPerspective  | Supported            | Supported [^2]     |
| gluLookat       | Supported            | Not Supported [^3] |

[^1]: gluOrtho2D is mapped to glOrtho in Basic4GLj
[^2]: gluPerspective uses glFrustrum implementation in Basic4GLj
[^3]: gluLookAt will currently throw an UnsupportedOperationException if called in Basic4GLj

## Basic4GL OpenGL implementation
### Basic4GL OpenGL initialization
Firstly, Basic4GL creates a window for you and initializes it for OpenGL.
Therefore, Basic4GL programs skip the initialization stage and can start executing OpenGL commands straight away.

Example:
```
glTranslatef (0, 0, -4)
glBegin (GL_TRIANGLES)
glColor3f (1, 0, 0): glVertex2f ( 0, 1)
glColor3f (0, 1, 0): glVertex2f (-1,-1)
glColor3f (0, 0, 1): glVertex2f ( 1,-1)
glEnd ()
SwapBuffers ()
```

Basic4GL also performs the following OpenGL calls at the beginning of each program:
```
` Initialise the view port
glViewport (0, 0, WindowWidth(), WindowHeight())

` Create projection matrix, 60 degree field of view, near clip plane at 1, far clip plane at 1000
glMatrixMode (GL_PROJECTION)
glLoadIdentity ()
gluPerspective (60, (1.0*WindowWidth()) / WindowHeight(), 1, 1000)

` Initialise the model view matrix
glMatrixMode(GL_MODELVIEW)
glLoadIdentity()

` Enable depth testing
glEnable (GL_DEPTH_TEST)
glDepthFunc (GL_LEQUAL)
```

This is simply for convenience and saves typing - if you're happy with the default settings.
Otherwise, feel free to roll your own projection matrix e.t.c.
The `WindowWidth()` and `WindowHeight()` functions will return the width and height of the output window.
(Multiplying the `WindowWidth()` by `1.0` simply converts it to a real value instead of an integer, so that real division is used instead of integer).

> [!WARNING]
>
> Modern retina and high resolution displays may scale window dimensions to accommodate different display configurations.
>
> Basic4GLj attempts to scale output to accommodate modern, high resolution display settings,
> and `WindowWidth()` and `WindowHeight()` may not reflect the applied resolution scaling.
>
> If you experience issues with `WindowWidth()` and `WindowHeight()`,
> please report issues on the Basic4GLj project page on GitHub:
> https://github.com/NateIsStalling/Basic4GLj/issues

### Double buffered OpenGL window.
The OpenGL window is double buffered.

This means it has a back buffer, which is hidden away from the user, and a front buffer which corresponds to the visible image on the screen.

All OpenGL rendering occurs in the back buffer. Once the scene is complete and ready to be displayed it is "swapped" to the front buffer, which displays it on the screen.
In Basic4GL you do this with the `SwapBuffers()` command.

### SwapBuffers
`SwapBuffers()` will swap the completed scene from the back buffer.
This immediately displays the result of the image that has just been rendered.

`SwapBuffers()` is a crucial part of any Basic4GL OpenGL program.
Without it the user won't see anything rendered, because it will all be sitting in the back buffer,
which is not displayed.

A simple example:

```
while true
glClearColor (rnd()%100/100.0, rnd()%100/100.0, rnd()%100/100.0, rnd()%100/100.0)
glClear (GL_COLOR_BUFFER_BIT)
SwapBuffers ()
wend
```

Will repeatedly clear the OpenGL window to a random colour, and display it. The visual result is random flickering colours.
Without the `SwapBuffers()` call, the above program would not appear to do anything,
as nothing ever gets through to the front buffer.

> [!NOTE]
>
> `SwapBuffers()` will either copy the back buffer to the front buffer, or exchange the buffers.
> This appears to depend on the hardware, screen mode and OpenGL implementation.
> For example, my on NVidia GeForce2, `SwapBuffers` appears to exchange in fullscreen mode and copy in windowed mode.

### Image and texture loading

Basic4GLj supports image formats compatible with LWJGL 3 stb bindings,
including: _JPG_, _PNG_, _TGA_, _BMP_, _PSD_, _GIF_, _HDR_, _PIC_.

_PCX_ texture support is provided by Apache Commons Imaging library.

See _LICENSES_ directory in project's git repository and distributions
for license information about LWJGL 3, stb, and Apache Commons.

> [!IMPORTANT]
>
> Legacy Basic4GL for Windows versions use the Corona open-source image library to load image files,
> for use in OpenGL textures, including Windows Bitmap, JPEG, and other formats.
>
> If you experience issues loading any texture formats in Basic4GLj,
> please report any issues on the project's GitHub page at https://github.com/NateIsStalling/Basic4GLj/issues

### LoadTex
The easiest way to get a texture into Basic4GL is to use the `LoadTex()` functions.

Format:
```
LoadTex(filename)
```

Where filename is a string containing the filename of an image to load into an OpenGL texture.

This will allocate an OpenGL texture, load the image into the texture,
and return the OpenGL texture handle (a numeric handle known as the "texture name").
The function returns `0`, if for any reason it cannot load the image and store it in a texture.

> [!TIP]
>
> See the **Sprite Library Guide** for more information on loading textures.

### Multitexturing
Basic4GL uses OpenGL v1.1.

Multitexturing is not natively part of the v1.1, but is available through the OpenGL extensions mechanism.

Basic4GL automatically hooks into this extension and makes the associated functions and constants available to Basic4GL programs. (If the extension is not available, calling the functions will simply do nothing.)

### ExtensionSupported
The Basic4GL function `ExtensionSupported` is the easiest way to test whether the current hardware supports multitexturing, as for example:

```
if not ExtensionSupported ("GL_ARB_multitexture") then
Do something else...
```

(You can also check for other extensions, however this version of Basic4GL only supports `GL_ARB_multitexture`..)

This is exactly equivalent to calling `glGetString (GL_EXTENSIONS)` and testing the resulting string for the presence of `"GL_ARB_multitexture"`

### MaxTextureUnits
`MaxTextureUnits()` will return the number of available texturing units.

```
dim units
units = MaxTextureUnits ()
```

Note: This is exactly equivalent to:
```
dim units
glGetIntegerv (GL_MAX_TEXTURE_UNITS_ARB, units)
```
### glMultitexCoord2f, glMultitexCoord2d and glActiveTexture
These are the actual multitexturing functions that Basic4GL supports.

> [!TIP]
>
> See the _MultitextureDemo.gb_ example program for an example of multitexturing in action.

## Credits
Basic4GL, Copyright (C) 2003 Tom Mulgrew

26-Jul-2008
Tom Mulgrew

Documentation modified for Markdown formatting by Nathaniel Nielsen