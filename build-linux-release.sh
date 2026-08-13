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

if [ -n "$RUNTIME_IMAGE" ]; then
  echo "Bundling runtime image: $RUNTIME_IMAGE"
  if [ ! -d "$RUNTIME_IMAGE" ]; then
    echo "Runtime image directory not found: $RUNTIME_IMAGE"
    exit 1
  fi

  # GitHub-hosted JDK installs can contain symlinks back into the toolcache.
  # Materialize a private copy so the .deb never ships links to build-machine paths.
  RUNTIME_IMAGE_STAGING_DIR="$(mktemp -d -p ./build)/runtime-image"
  mkdir -p "$RUNTIME_IMAGE_STAGING_DIR"
  cp -aL "$RUNTIME_IMAGE/." "$RUNTIME_IMAGE_STAGING_DIR/"
  chmod -R u+rwX "$RUNTIME_IMAGE_STAGING_DIR"
  RUNTIME_IMAGE="$RUNTIME_IMAGE_STAGING_DIR"
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
if [ ! -x "$APP_RUNTIME_PATH/bin/java" ]; then
  echo "Expected bundled runtime java not found or not executable: $APP_RUNTIME_PATH/bin/java"
  exit 1
fi
if find "$APP_RUNTIME_PATH" -type l -print -quit | grep -q .; then
  echo "Bundled runtime image contains symlinks; refusing to build a non-self-contained .deb"
  find "$APP_RUNTIME_PATH" -type l -print
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
