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

MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH='embedded.provisionprofile'
MAC_SIGNING_KEY_USER_NAME='Configure CI/CD Variable'
MAC_SIGNING_PACKAGE_SIGNING_PREFIX='com.basic4glj.desktop.'

# Load variables from local
if [ -e "$ENV_FILE_PATH" ]; then
  echo 'Using local .env file'
  set -a
  . "$ENV_FILE_PATH"
  set +a
else
  echo 'Local .env file not found'
fi

if [ -n "MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_BASE64" ]; then
  echo 'Using embedded.provisionprofile from base64 ENV variable...'
  echo MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_BASE64 | base64 --decode > "$MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH"
else
  echo "Using embedded.provisionprofile from $MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH..."
fi

if [ ! -e "$MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH" ]; then
  echo "MacOS Provisioning Profile Not Found; please add '$MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH' or configure ENV variable '\$EMBEDDED_PROVISIONPROFILE_BASE64'"
  exit 1
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

# TODO having trouble with signing..
echo "Create app-image"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image-mac.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --app-content embedded.provisionprofile \
  --icon "icons/icon.icns" \
  --mac-sign \
  --mac-app-store \
  --mac-package-signing-prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" \
  --mac-signing-key-user-name "$MAC_SIGNING_KEY_USER_NAME" \
  --mac-entitlements sandbox.plist \
  --verbose

echo "Create native installer"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-mac-app-store.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --mac-sign \
  --mac-app-store \
  --mac-package-signing-prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" \
  --mac-signing-key-user-name "$MAC_SIGNING_KEY_USER_NAME" \
  --mac-entitlements sandbox.plist \
  --verbose