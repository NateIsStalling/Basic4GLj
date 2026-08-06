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
fi

echo "Create app-image Version '$APP_RELEASE_VERSION'"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --icon "icons/icon.ico" \
  ${RUNTIME_IMAGE:+--runtime-image "$RUNTIME_IMAGE"} \
  --verbose

echo "Create native installer"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-windows.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --win-upgrade-uuid "$WIN_UPGRADE_UUID" \
  --verbose

INSTALLER_PATH="./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}.msi"
if [ ! -f "$INSTALLER_PATH" ]; then
  echo "Expected Windows installer not found: $INSTALLER_PATH"
  exit 1
fi
echo "Created Windows installer: $INSTALLER_PATH"
