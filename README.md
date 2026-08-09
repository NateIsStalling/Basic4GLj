# Basic4GLj

_Basic4GL development tools for the JVM - a BASIC programming environment for 2D/3D game development_

![heightmap-demo](https://github.com/NateIsStalling/Basic4GLj/assets/14190443/a9a0a90b-152d-4395-80f8-3f35690f100d)

---

Basic4GLj is a cross-platform JVM implementation of Basic4GL, providing a development environment for writing and running BASIC programs with a built-in 2D sprite engine and OpenGL support for 3D graphics.

Basic4GLj runs on Windows, Linux, and macOS, and uses LWJGL to provide graphics and platform support.

## Getting Started

### Download the Latest Build

Check out [Basic4GLj on itch.io](https://nateisstalling.itch.io/basic4glj), or the [Releases Page](https://github.com/NateIsStalling/Basic4GLj/releases) of this repo for the latest build.

### Sample Programs

Sample programs can be found in the `/samples` [folder](https://github.com/NateIsStalling/Basic4GLj/tree/main/samples/Programs).

Programs can be run by opening a `.gb` file in the Basic4GLj editor and clicking the Play button.

### Documentation

Check out the [docs](./docs) or [wiki](https://github.com/NateIsStalling/Basic4GLj/wiki) for the Basic4GL language guide, sprite library guide for 2D game programming, and additional tutorials.

- [Language Syntax Guide](./docs/basic4gl/language-syntax-guide.md)
- [Text Output Guide](./docs/basic4gl/text-output-guide.md)
- [Sprite Library Guide](./docs/basic4gl/sprite-library-guide.md)
- [OpenGL Guide](./docs/basic4gl/opengl-guide.md)
- [Sound Guide](./docs/basic4gl/sound-guide.md)

## Building the Editor

This project requires Java 17 and uses Gradle for its builds.

```shell
./gradlew :app:build
```

_build artifacts can be found in `/app/build/distributions`_

### Debugging the Editor

To build the required dependencies and launch the editor for debugging:

```shell
./gradlew :app:debugAll
```

The application depends on the JAR output of the `app-runtime` and `debug-server` modules. 
The `:app:debugAll` task builds these dependencies before launching the application.

## Compatibility Notes

Basic4GL was originally developed for Windows with OpenGL 1.1, which is considered a legacy version of OpenGL and may not be fully supported by modern systems.
Recent versions of Basic4GL have been updated to use GLFW for its OpenGL context and window management.

Basic4GLj attempts to support all functions provided by GLFW versions of Basic4GL, but some GLU functionality and legacy keyboard constants are unsupported by GLFW and LWJGL.

Please report any compatibility or stability issues to the Issues page of this project.

Additional compatibility notes can be found on the project's wiki: [Compatibility Notes](https://github.com/NateIsStalling/Basic4GLj/wiki/Compatibility-Notes)

## License

Basic4GLj is licensed under a **BSD 3-Clause license** - please see the LICENSES folder for more details and licenses for third-party libraries used.

## Credits

Basic4GLj is based on the source of Basic4GL by Tom Mulgrew. The source of the original C++ implementation can be found here:

https://github.com/basic4gl-guy/basic4gl
