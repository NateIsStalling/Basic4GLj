# Basic4GLj

_Basic4GL for Java - BASIC language compiler and IDE_

![heightmap-demo](https://github.com/NateIsStalling/Basic4GLj/assets/14190443/a9a0a90b-152d-4395-80f8-3f35690f100d)

---

This repository contains the Java port of Basic4GL, a BASIC programming language that features OpenGL functionality.

Basic4GLj features Windows and Mac OS support, requiring Java 17, and uses LWJGL to provide support for OpenGL functions.

Basic4GLj is licensed under a New BSD license - please see the LICENSES folder for more details and licenses for third-party libraries used.

## Getting Started

### Get the Latest Build

Check out the [Releases Page](https://github.com/NateIsStalling/Basic4GLj/releases) of this repo for the latest build.

### Documentation

Check out the [wiki](https://github.com/NateIsStalling/Basic4GLj/wiki) of this repo for the Basic4GL language guide, sprite library guide for 2D game programming, and additional tutorials.

- [Language Syntax Guide](https://github.com/NateIsStalling/Basic4GLj/wiki/Language-Syntax-Guide)
- [Text Output Guide](https://github.com/NateIsStalling/Basic4GLj/wiki/Text-Output-Guide)
- [Sprite Library Guide](https://github.com/NateIsStalling/Basic4GLj/wiki/Sprite-Library-Guide)
- [OpenGL Guide](https://github.com/NateIsStalling/Basic4GLj/wiki/OpenGL-Guide)
- [Sound Guide](https://github.com/NateIsStalling/Basic4GLj/wiki/Sound-Guide) 

(more documentation coming soon!)

### Sample Programs

A `/samples` folder with example programs is included with each release that can be run by opening the `.gb` file in the Basic4GL editor and clicking the Play button.

Sample programs can also be found here in the repo:
https://github.com/NateIsStalling/Basic4GLj/tree/main/app/src/main/dist/samples/Programs


## Building the Editor

This project requires Java 17 and uses Gradle for its builds.

> ./gradlew :app:build

_build artifacts can be found in `/app/build/distributions`_

### Debugging the Editor

To debug the app IDE project along with its dependencies, use the following gradle task:
> ./gradlew :app:debugAll

_The application depends on the JAR output of its "library" and "debugServer" modules - the `:app:debugAll` task should generate these dependencies_

## Sound System

[Sound Guide](https://github.com/NateIsStalling/Basic4GLj/wiki/Sound-Guide) tutorial is available on the project's wiki.

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

Basic4GLj depends on a fork of Paulscode-SoundSystem to support sound codecs for LWJGL 3 which can be found here:

https://github.com/NateIsStalling/Paulscode-SoundSystem/tree/lwjgl3

Licenses for Paulscode-SoundSystem and related sound Codecs can be found in Basic4GLj's app module `dist` directory or in release packages under `/LICENSES/sound system` 

## Compatibility Notes

Basic4GL was originally developed for Windows with OpenGL 1.1, which is considered a legacy version of OpenGL and may not be fully supported by modern systems.

This Java port of Basic4GL attempts to support all functions provided by the original Windows version, but some functions have not been ported yet and some may not be possible with Basic4GLj's current OpenGL implementation

Additionally, recent work on Basic4GLj (as of 2022!) has been done on a Mac with Intel - I can't guarantee everything works as expected on Windows, and support for Apple Silicon based Macs is experimental.

Please report any compatibility or stability issues to the Issues page of this project.

Additional compatibility notes can be found on the project's wiki: https://github.com/NateIsStalling/Basic4GLj/wiki/Compatibility-Notes

## Basic4GL for Windows

Source of the original Basic4GL can be found here:

https://github.com/basic4gl-guy/basic4gl
