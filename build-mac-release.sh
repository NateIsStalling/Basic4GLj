#!/bin/bash

set -e # die on error

# Usage: build-mac-release.sh [--entitlements-profile adhoc|app-store]
#
# adhoc (default)  - non-sandboxed entitlements for adhoc/notarized Developer ID distribution
#                     (adhoc.plist / adhoc-embedded-tool.plist)
# app-store        - sandboxed entitlements matching the (currently unused) Mac App Store path
#                     (sandbox.plist / embedded-tool.plist), kept for parity with
#                     build-mac-app-store-release.sh should that distribution channel return

ENV_FILE_PATH='./.env'
ENTITLEMENTS_PROFILE='adhoc'
SKIP_BUILD=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --entitlements-profile) ENTITLEMENTS_PROFILE="$2"; shift ;;
    --skip-build|--package-only) SKIP_BUILD=true ;;
    *) echo "Unknown parameter: $1"; exit 1 ;;
  esac
  shift
done

case "$ENTITLEMENTS_PROFILE" in
  adhoc)
    ENTITLEMENTS_FILE='adhoc.plist'
    INHERITED_ENTITLEMENTS_FILE='adhoc-embedded-tool.plist'
    ;;
  app-store)
    ENTITLEMENTS_FILE='sandbox.plist'
    INHERITED_ENTITLEMENTS_FILE='embedded-tool.plist'
    ;;
  *)
    echo "Unknown --entitlements-profile '$ENTITLEMENTS_PROFILE' (expected 'adhoc' or 'app-store')"
    exit 1
    ;;
esac
echo "Using entitlements profile '$ENTITLEMENTS_PROFILE' ($ENTITLEMENTS_FILE / $INHERITED_ENTITLEMENTS_FILE)"

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
echo "Create app-image Version '$APP_RELEASE_VERSION'"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image-mac.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --icon "icons/icon.icns" \
  --mac-entitlements "$ENTITLEMENTS_FILE" \
  --verbose

if [ ! -d ./build/distributions/Basic4GLj.app ]; then
  echo "Expected macOS app image not found: ./build/distributions/Basic4GLj.app"
  exit 1
fi

echo "Sign app-image"
cp "$MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH" ./build/distributions/Basic4GLj.app/Contents/embedded.provisionprofile
if [[  -z "$MAC_SIGNING_KEYCHAIN_PATH" ]]; then
  sh ./build-mac-sign.sh --app-location "./build/distributions/Basic4GLj.app" \
     --signing-identity "$MAC_SIGNING_KEY_USER_NAME" \
     --identifier-prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" \
     --entitlements "$ENTITLEMENTS_FILE" \
     --inherited-entitlements "$INHERITED_ENTITLEMENTS_FILE" \
     --mac-bundle-identifier "com.basic4glj.desktop" \
     --app-name "Basic4GLj"
 else
  sh ./build-mac-sign.sh --app-location "./build/distributions/Basic4GLj.app" \
     --signing-identity "$MAC_SIGNING_KEY_USER_NAME" \
     --signing-keychain "$MAC_SIGNING_KEYCHAIN_PATH" \
     --identifier-prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" \
     --entitlements "$ENTITLEMENTS_FILE" \
     --inherited-entitlements "$INHERITED_ENTITLEMENTS_FILE" \
     --mac-bundle-identifier "com.basic4glj.desktop" \
     --app-name "Basic4GLj"
fi


echo "Create native installer"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-mac.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --verbose

INSTALLER_PATH="./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}.dmg"
if [ ! -f "$INSTALLER_PATH" ]; then
  echo "Expected macOS installer not found: $INSTALLER_PATH"
  exit 1
fi

if [[  -z "$MAC_SIGNING_KEYCHAIN_PATH" ]]; then
  /usr/bin/codesign --force --timestamp \
      --options runtime \
      --sign "$MAC_SIGNING_KEY_USER_NAME" \
      --entitlements "$ENTITLEMENTS_FILE" \
      --prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}.dmg"
else
  /usr/bin/codesign --force --timestamp \
      --options runtime \
      --sign "$MAC_SIGNING_KEY_USER_NAME" \
      --keychain "$MAC_SIGNING_KEYCHAIN_PATH" \
      --entitlements "$ENTITLEMENTS_FILE" \
      --prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}.dmg"
fi
echo "Created macOS installer: $INSTALLER_PATH"