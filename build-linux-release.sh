#!/bin/bash

set -e # die on error

ENV_FILE_PATH='./.env'
SKIP_BUILD=false
RUNTIME_IMAGE=''

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-build|--package-only) SKIP_BUILD=true ;;
    --runtime-image) RUNTIME_IMAGE="$2"; shift ;;
    *) echo "Unknown parameter: $1"; exit 1 ;;
  esac
  shift
done

# Load variables from local
if [ -e "$ENV_FILE_PATH" ]; then
  echo 'Using local .env file'
  set -a
  . "$ENV_FILE_PATH"
  set +a
else
  echo 'Local .env file not found'
fi

if [[ "$SKIP_BUILD" == false ]]; then
  ./gradlew -v
  ./gradlew clean build copyJarsForJPackage
else
  echo "Package-only mode enabled; skipping Gradle build and using existing jpackage input in ./build/libs"
fi

if [ ! -d ./build/libs ]; then
  echo "jpackage input directory './build/libs' not found"
  exit 1
fi

if [ -z "$APP_RELEASE_VERSION" ]; then
  echo "APP_RELEASE_VERSION is required"
  exit 1
fi

echo "jpackage JDK"
java --version

# Platform modules required by the app + its runtime plugins (LWJGL, Jetty
# websocket client/server, Gson, FlatLaf, RSyntaxTextArea). Verified with a
# class-load smoke test across app.jar, app-runtime and debug-server.
JLINK_MODULES="java.base,java.desktop,java.datatransfer,java.logging,java.management,java.naming,java.sql,java.xml,java.prefs,java.scripting,java.security.jgss,java.net.http,jdk.unsupported,jdk.crypto.ec"

if [ -n "$RUNTIME_IMAGE" ]; then
  if [ ! -d "$RUNTIME_IMAGE" ]; then
    echo "Runtime image directory not found: $RUNTIME_IMAGE"
    exit 1
  fi

  if [ -d "$RUNTIME_IMAGE/jmods" ] && [ -x "$RUNTIME_IMAGE/bin/jlink" ]; then
    # A full JDK was supplied (e.g. $JAVA_HOME_17_X64). Build a slim, target-JDK
    # runtime with *that JDK's own* jlink so the app ships a JDK 17 runtime even
    # though jpackage itself runs on JDK 25. jpackage only copies the image given
    # to --runtime-image; it never re-runs jlink on it, so the runtime stays 17.
    echo "Linking slim runtime from JDK: $RUNTIME_IMAGE"
    RUNTIME_IMAGE_DIR="./build/jpackage-runtime"
    rm -rf "$RUNTIME_IMAGE_DIR"
    "$RUNTIME_IMAGE/bin/jlink" \
      --add-modules "$JLINK_MODULES" \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --output "$RUNTIME_IMAGE_DIR"
    RUNTIME_IMAGE="$RUNTIME_IMAGE_DIR"
  else
    echo "Using prebuilt runtime image: $RUNTIME_IMAGE"
  fi

  # A jlink image is self-contained; any absolute symlink would point back at a
  # build-machine path and break on the user's system. Fail fast if present.
  if find "$RUNTIME_IMAGE" -type l -lname '/*' -print -quit | grep -q .; then
    echo "Runtime image contains absolute symlinks; refusing to build a non-portable .deb"
    find "$RUNTIME_IMAGE" -type l -lname '/*' -print
    exit 1
  fi

  echo "Runtime image ready: $RUNTIME_IMAGE ($(du -sh "$RUNTIME_IMAGE" | cut -f1))"
  "$RUNTIME_IMAGE/bin/java" -version || true
fi

JPACKAGE_RUNTIME_ARGS=()
if [ -n "$RUNTIME_IMAGE" ]; then
  JPACKAGE_RUNTIME_ARGS=(--runtime-image "$RUNTIME_IMAGE")
fi

echo "Create app-image Version '$APP_RELEASE_VERSION'"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --icon "icons/icon.png" \
  "${JPACKAGE_RUNTIME_ARGS[@]}" \
  --verbose

APP_IMAGE_PATH="./build/distributions/Basic4GLj"
APP_RUNTIME_PATH="$APP_IMAGE_PATH/lib/runtime"

if [ ! -x "$APP_IMAGE_PATH/bin/Basic4GLj" ]; then
  echo "Expected app launcher not found or not executable: $APP_IMAGE_PATH/bin/Basic4GLj"
  exit 1
fi
if [ ! -x "$APP_IMAGE_PATH/bin/Basic4GLjDebugServer" ]; then
  echo "Expected debug server launcher not found or not executable: $APP_IMAGE_PATH/bin/Basic4GLjDebugServer"
  exit 1
fi
if [ ! -d "$APP_RUNTIME_PATH/lib" ]; then
  echo "Bundled runtime image missing from app image: $APP_RUNTIME_PATH"
  exit 1
fi
if find "$APP_RUNTIME_PATH" -type l -lname '/*' -print -quit | grep -q .; then
  echo "Bundled runtime contains absolute symlinks; refusing to build a non-portable .deb"
  find "$APP_RUNTIME_PATH" -type l -lname '/*' -print
  exit 1
fi

echo "Create native installer"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-linux.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --verbose

INSTALLER_PATH="./build/distributions/basic4glj_${APP_RELEASE_VERSION}_amd64.deb"

if [ ! -f "$INSTALLER_PATH" ]; then
  echo "Expected Linux installer not found: $INSTALLER_PATH"
  exit 1
fi
echo "Created Linux installer: $INSTALLER_PATH"
