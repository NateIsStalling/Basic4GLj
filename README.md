# Basic4GLj

_Basic4GL for Java - BASIC language compiler and IDE_

![heightmap-demo](https://github.com/NateIsStalling/Basic4GLj/assets/14190443/a9a0a90b-152d-4395-80f8-3f35690f100d)

---

This repository contains the Java port of Basic4GL, a BASIC programming language that features OpenGL functionality.

Basic4GLj features Windows and Mac OS support, requiring Java 21, and uses LWJGL to provide support for OpenGL functions.

Basic4GLj is licensed under a New BSD license - please see the LICENSES folder for more details and licenses for third-party libraries used.

## Getting Started

### Get the Latest Build

Check out the [Releases Page](https://github.com/NateIsStalling/Basic4GLj/releases) of this repo for the latest build.

A `/samples` folder with example programs is included with each release that can be run by opening the `.gb` file in the Basic4GL editor and clicking the Play button.

### Building the Application

This project requires Java 21 and uses Gradle for its builds.

> ./gradlew :app:build

_build artifacts can be found in `/app/build/distributions`_

### Debugging the Application

To debug the app IDE project along with its dependencies, use the following gradle task:
> ./gradlew :app:debugAll

_The application depends on the JAR output of its "library" and "debugServer" modules - the `:app:debugAll` task should generate these dependencies_

**TODO clean up gradle build scripts**


### Known Issues

- Exporting programs is currently disabled in the Editor.
- Debugger disconnects from Websocket connection timeout if idle at a breakpoint for too long.
- Some sample programs will not run, _see Compatibility Notes below_.


## Compatibility Notes

Basic4GL was originally developed for Windows with OpenGL 1.2, which is considered a legacy version of OpenGL and may not be fully supported by modern systems.

This Java port of Basic4GL attempts to support all functions provided by the original Windows version, but some functions have not been ported yet and some may not be possible with Basic4GLj's current OpenGL implementation

Additionally, recent work on Basic4GLj (as of 2022!) has been done on a Mac with Intel - I can't guarantee everything works as expected on Windows, and support for Apple Silicon based Macs is not currently planned but pull requests are always welcome.

Please report any compatibility or stability issues to the Issues page of this project.

### Sample Program Compatibility

Sample programs located in the `/samples` folder are copied from the Windows version of Basic4GL and are not guaranteed to work due to the in-progress nature of Basic4GLj and some Basic4GL features being otherwise unsupported.

Network and Sound functions are currently unsupported and some file formats like `.PCX` textures are not currently supported.

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

## Sound System

Basic4GLj depends on a fork of Paulscode-SoundSystem to support sound codecs for LWJGL 3 which can be found here:

https://github.com/NateIsStalling/Paulscode-SoundSystem/tree/lwjgl3

### Playing Sound Effects

The following file formats are currently supported for playing sound effects using the `loadsound` and `playsound` functions: 

| File Extension | Codec | 
|----------------| ------- |
| wav            | CodecWav |
| ogg            | CodecJOrbis |
| xm             | CodecIBXM |
| s3m            | CodecIBXM |
| mod            | CodecIBXM |

### Playing Music

Ogg Vorbis files are supported for playing music continuously using the `playmusic` function.

### Sound System Licenses

Licenses for Paulscode-SoundSystem and related sound Codecs can be found in the app module's `dist` directory or in release packages under `/LICENSES/sound system` 

## Basic4GL for Windows

Source of the original Basic4GL can be found here:

https://github.com/basic4gl-guy/basic4gl
