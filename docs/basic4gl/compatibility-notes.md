
Basic4GL was originally developed for Windows with OpenGL 1.1, which is considered a legacy version of OpenGL and may not be fully supported by modern systems.

This Java port of Basic4GL attempts to support all functions provided by the original Windows version, but some functions have not been ported yet and some may not be possible with Basic4GLj's current OpenGL implementation

Additionally, recent work on Basic4GLj (as of 2022!) has been done on a Mac with Intel - I can't guarantee everything works as expected on Windows, and support for Apple Silicon based Macs is experimental.

Please report any compatibility or stability issues to the Issues page of this project.

### Sample Program Compatibility

Sample programs located in the `/samples` folder are copied from the Windows version of Basic4GL and are not guaranteed to work due to the in-progress nature of Basic4GLj and some Basic4GL features being otherwise unsupported.

Network functions are currently unsupported are not currently supported.

Some sample programs may require `glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND)` to be added their original source inorder to display output; this is considered a bug since it inverts texture colors and will hopefully become unecessary in future versions of the Basic4GLj IDE - the applications will function normally with or without the added `glTexEnvi` in earlier versions of the Windows Basic4GL.

### Keyboard Input Compatibility

The following virtualkey constants available in the Windows version of Basic4GL are not currently supported:

    VK_LBUTTON, VK_RBUTTON, VK_CANCEL, VK_MBUTTON, VK_CLEAR, VK_RETURN, VK_SHIFT, VK_CONTROL, VK_MENU, VK_PAUSE, 
    VK_KANA, VK_HANGEUL, VK_HANGUL, VK_JUNJA, VK_FINAL, VK_HANJA, VK_KANJI, VK_CONVERT, VK_NONCONVERT, VK_ACCEPT, 
    VK_MODECHANGE, VK_SELECT, VK_PRINT, VK_EXECUTE, VK_SNAPSHOT, VK_HELP, VK_LWIN, VK_RWIN, VK_APPS, VK_SEPARATOR, 
    VK_LMENU, VK_RMENU, VK_PROCESSKEY, VK_ATTN, VK_CRSEL, VK_EXSEL, VK_EREOF, VK_PLAY, VK_ZOOM

_Integer values of the virtualkey constants may differ from the Windows version; the supported virtualkeys have been mapped to the key constants provided by GLFW where possible. In addition, all documented GLFW key constants are available for use in Basic4GLj.
see: http://www.glfw.org/docs/latest/group__keys.html_

`VK_RETURN` has not been mapped to a GLFW constant because GLFW has separate constants for the enter key and the keypad/numpad enter key, where `VK_RETURN` would recognize either.
`GLFW_KEY_ENTER` and `GLFW_KEY_KP_ENTER` are available for usage in place of `VK_RETURN`.

### OpenGL GLU Compatibility

OpenGL GLU constants are unavailable, they are unsupported by the current version of LWJGL that is used by Basic4GLj. GLU functions available in previous versions of Basic4GL are available with modifications.
- `gluOrtho2D` is mapped to `glOrtho`
- `gluPerspective` uses `glFrustrum` implementation
- `gluLookAt` will currently throw an `UnsupportedOperationException` if called


### Misc.

- `glColor3ub` and `glColor4ub` are mapped to `glColor3ubv` and `glColor4ubv` to resolve crash on macOS