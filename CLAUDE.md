# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Basic4GLj is a Java port of Basic4GL, a BASIC-dialect language/IDE with OpenGL support (via LWJGL). It's a
multi-module Gradle project targeting Java 17.

## Commands

```
./gradlew build                 # build everything (spotless auto-format runs first, see below)
./gradlew :app:build             # build just the desktop IDE
./gradlew :app:debugAll          # build app-runtime + debug-server jars the app depends on at runtime
```

Run the IDE from source: `./gradlew :app:run` or use `:app:debugAll` then run the `Basic4GLj` distribution
under `app/build/install`. Build artifacts land in `app/build/distributions`.

### Tests

Only `app` and `language-adapter` currently have tests (JUnit 5 / Jupiter, except `app` which mixes JUnit 4/5).

```
./gradlew :language-adapter:test
./gradlew :language-adapter:test --tests "com.basic4gl.language.adapter.Basic4GLLanguageSupportCompletionTest"
./gradlew :app:test --tests "com.basic4gl.desktop.debugger.VmWorkerTest"
```

### Formatting

Spotless (Palantir Java Format) is wired into every module and runs automatically before `build`
(`build.dependsOn spotlessApply`) — you generally don't need to invoke it manually. If needed directly:
`./gradlew spotlessApply` / `./gradlew spotlessCheck`.

### Platform release builds

`build-mac-release.sh`, `build-windows-release.sh`, `build-linux-release.sh`, `build-jar-release.sh` and the
`build-mac-*.sh` signing/notarization scripts drive jpackage-based installers per OS; see
`MAC-SIGNING-NOTES.md` for the macOS signing/notarization flow. These are release-engineering scripts, not
part of the normal edit/build/test loop.

## Architecture

The repo is mid-migration from a monolithic Basic4GL-specific editor toward a generic IDE shell driven by
SPI contracts, with Basic4GL as the (currently only) language plugin implementing those contracts. This
split explains why code that looks Basic4GL-specific often has a generic counterpart one module over.

### Module layering

```
language-core    – Basic4GL language/runtime primitives & extension interfaces (Data, Function, VM driver
                    interfaces, IAppSettings, opengl/standard extension interfaces). No deps.
language-spi     – Basic4GL runtime *plugin* system (PluginManager, PluginLibrary, PluginStructure*) —
                    i.e. loadable .plugin/.jar libraries that add BASIC functions to the VM. Depends on
                    language-core.
debug-protocol   – Wire types/commands/callbacks for the debugger websocket protocol. No internal deps.
language-runtime – TomVM (the bytecode VM) and Debugger. Depends on language-core, language-spi.
compiler         – TomBasicCompiler, Parser, Preprocessor (BASIC -> bytecode). Depends on language-core,
                    language-spi, language-runtime.
library          – Concrete Basic4GL standard library: OpenGL/GLFW bindings, sprites, sound, text grid,
                    file I/O, and the debug-command handlers that adapt TomVM/Debugger to debug-protocol.
                    Depends on language-core, language-spi, debug-protocol.
debug-server     – Standalone process (own main class) hosting the debug websocket server; the IDE talks
                    to it over debug-protocol rather than in-process. Depends on debug-protocol.

app-spi          – Generic desktop-IDE plugin contracts: EditorPlugin, LanguageSupport (tokenizing/
                    highlighting/symbol-extraction, deliberately free of any UI-toolkit types),
                    CompilerService, DebugService, ProjectSettingsPage, etc. Depends on debug-protocol.
app-runtime      – Generic desktop app runtime: GLFW window/input glue, standalone app settings/CLI parsing.
                    Depends on compiler, language-core, language-spi, language-runtime, library,
                    debug-protocol (i.e. it currently still knows about Basic4GL concretely — not yet fully
                    generic).
language-adapter – The Basic4GL implementation of the app-spi contracts: Basic4GLLanguageSupport,
                    Basic4GLCompilerService/DebugService/PreprocessorService, Basic4GLEditorPluginAdapter.
                    Also owns the ANTLR grammar (src/main/antlr/Basic4GL.g4) used for the newer
                    tokenizer/completion path. Depends on nearly everything: app-runtime, app-spi,
                    language-core/spi/runtime, library, compiler.
app              – The desktop IDE itself (MainWindow, BasicEditor, FileEditor, editor/debugger UI). Talks
                    to the language mainly through app-spi types, instantiating Basic4GLLanguageSupport /
                    Basic4GLEditorPluginAdapter from language-adapter as the concrete plugin.
```

Two unrelated things are both called "plugin" — don't conflate them:
- **Runtime plugins** (`language-spi.PluginManager`/`PluginLibrary`, `library.plugin.PluginJAR*`): loadable
  libraries that extend the BASIC language with new functions (e.g. OpenGL, sprites) at VM level.
- **Editor plugins** (`app-spi.EditorPlugin`): IDE-level language integrations. Today there is exactly one,
  `Basic4GLEditorPluginAdapter`, which wires together a `TomVM`/`TomBasicCompiler`/`Preprocessor`/`Debugger`
  and exposes them through the app-spi service interfaces.

### Debugging flow

The IDE doesn't debug in-process: `debug-server` runs as a separate JVM process exposing a javax.websocket
endpoint; `app`/`library` speak to it using the shared message/command/callback types in `debug-protocol`.
`library`'s `debug.commands.*Handler` classes are the server-side command handlers that drive the actual
`TomVM`/`Debugger`.

### Repo housekeeping

Top-level `src/`, `runtime/`, and `debugProtocol/` directories are leftover empty scaffolding from an
earlier module restructuring (not referenced by `settings.gradle`, not git-tracked). Ignore them — the real
modules are the ones listed in `settings.gradle`.
