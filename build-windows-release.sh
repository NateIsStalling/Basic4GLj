#!/bin/bash

set -e # die on error

ENV_FILE_PATH='./.env'
SKIP_BUILD=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-build|--package-only) SKIP_BUILD=true ;;
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
  echo "Skipping Gradle build; using existing jpackage input in ./build/libs"
fi

if [ ! -d ./build/libs ]; then
  echo "jpackage input directory './build/libs' not found"
  exit 1
fi

echo "jpackage JDK"
java --version

echo "Create app-image Version '$APP_RELEASE_VERSION'"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --icon "icons/icon.ico" \
  --verbose

echo "Create native installer"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-windows.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --win-upgrade-uuid "$WIN_UPGRADE_UUID" \
  --verbose