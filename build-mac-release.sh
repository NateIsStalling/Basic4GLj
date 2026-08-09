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
RUNTIME_IMAGE=''

while [[ $# -gt 0 ]]; do
  case "$1" in
    --entitlements-profile) ENTITLEMENTS_PROFILE="$2"; shift ;;
    --skip-build|--package-only) SKIP_BUILD=true ;;
    --runtime-image) RUNTIME_IMAGE="$2"; shift ;;
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

if [ -n "$RUNTIME_IMAGE" ]; then
  echo "Bundling runtime image: $RUNTIME_IMAGE"
  if [ ! -d "$RUNTIME_IMAGE" ]; then
    echo "Runtime image directory not found: $RUNTIME_IMAGE"
    exit 1
  fi
fi

echo "Create app-image Version '$APP_RELEASE_VERSION'"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image-mac.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --icon "icons/icon.icns" \
  --mac-entitlements "$ENTITLEMENTS_FILE" \
  ${RUNTIME_IMAGE:+--runtime-image "$RUNTIME_IMAGE"} \
  --verbose

if [ ! -d ./build/distributions/Basic4GLj.app ]; then
  echo "Expected macOS app image not found: ./build/distributions/Basic4GLj.app"
  exit 1
fi

echo "Sign app-image"
cp "$MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH" ./build/distributions/Basic4GLj.app/Contents/embedded.provisionprofile
if [[  -z "$MAC_SIGNING_KEYCHAIN_PATH" ]]; then
  bash ./build-mac-sign.sh --app-location "./build/distributions/Basic4GLj.app" \
     --signing-identity "$MAC_SIGNING_KEY_USER_NAME" \
     --identifier-prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" \
     --entitlements "$ENTITLEMENTS_FILE" \
     --inherited-entitlements "$INHERITED_ENTITLEMENTS_FILE" \
     --mac-bundle-identifier "com.basic4glj.desktop" \
     --app-name "Basic4GLj"
 else
  bash ./build-mac-sign.sh --app-location "./build/distributions/Basic4GLj.app" \
     --signing-identity "$MAC_SIGNING_KEY_USER_NAME" \
     --signing-keychain "$MAC_SIGNING_KEYCHAIN_PATH" \
     --identifier-prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" \
     --entitlements "$ENTITLEMENTS_FILE" \
     --inherited-entitlements "$INHERITED_ENTITLEMENTS_FILE" \
     --mac-bundle-identifier "com.basic4glj.desktop" \
     --app-name "Basic4GLj"
fi

echo "Verify app-image signature"
/usr/bin/codesign --verify --deep --strict --verbose=4 ./build/distributions/Basic4GLj.app


echo "Create native installer"
# jpackage's --type dmg builder copies --app-image into its own temp working
# directory and re-signs it there before sealing the dmg, regardless of
# whether --mac-sign is given and regardless of JDK 25 vs 26 - that re-sign
# has repeatedly clobbered the correct signature build-mac-sign.sh just
# applied (missing secure timestamps / wrong identity on embedded runtime
# dylibs). Build the dmg ourselves from the already-verified .app instead, so
# nothing re-signs it after this point.
DMG_VOLNAME="Basic4GLj"
DMG_STAGING_DIR="$(mktemp -d)"
cp -R "./build/distributions/Basic4GLj.app" "$DMG_STAGING_DIR/"
ln -s /Applications "$DMG_STAGING_DIR/Applications"

INSTALLER_PATH="./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}.dmg"
DMG_WORKDIR="$(mktemp -d)"
PROTO_DMG="$DMG_WORKDIR/proto.dmg"

# Build as UDRW first so the volume icon can be set on the mounted image,
# then convert to the compressed UDZO format for the final artifact -
# mirrors what jpackage's own (now-bypassed) dmg builder did.
hdiutil create -volname "$DMG_VOLNAME" -srcfolder "$DMG_STAGING_DIR" -ov -fs HFS+ -format UDRW "$PROTO_DMG"

DMG_MOUNT_POINT="$(mktemp -d)"
hdiutil attach "$PROTO_DMG" -mountpoint "$DMG_MOUNT_POINT" -nobrowse -quiet -owners on

cp "icons/icon.icns" "$DMG_MOUNT_POINT/.VolumeIcon.icns"
SetFile -c icnC "$DMG_MOUNT_POINT/.VolumeIcon.icns"
SetFile -a V "$DMG_MOUNT_POINT/.VolumeIcon.icns"
SetFile -a C "$DMG_MOUNT_POINT"

hdiutil detach "$DMG_MOUNT_POINT" -quiet
rmdir "$DMG_MOUNT_POINT"

rm -f "$INSTALLER_PATH"
hdiutil convert "$PROTO_DMG" -format UDZO -o "$INSTALLER_PATH"
rm -rf "$DMG_WORKDIR" "$DMG_STAGING_DIR"

if [ ! -f "$INSTALLER_PATH" ]; then
  echo "Expected macOS installer not found: $INSTALLER_PATH"
  exit 1
fi

echo "Verify signature of app bundle sealed inside the dmg"
DMG_MOUNT_POINT="$(mktemp -d)"
hdiutil attach "$INSTALLER_PATH" -mountpoint "$DMG_MOUNT_POINT" -nobrowse -quiet
/usr/bin/codesign --verify --deep --strict --verbose=4 "$DMG_MOUNT_POINT/Basic4GLj.app"
DMG_APP_VERIFY_STATUS=$?
hdiutil detach "$DMG_MOUNT_POINT" -quiet
rmdir "$DMG_MOUNT_POINT"
if [ "$DMG_APP_VERIFY_STATUS" -ne 0 ]; then
  echo "App bundle sealed inside $INSTALLER_PATH failed signature verification"
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
