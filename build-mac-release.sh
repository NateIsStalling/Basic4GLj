#!/bin/bash

set -e # die on error

ENV_FILE_PATH='./.env'

MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH='embedded.provisionprofile'
MAC_SIGNING_KEY_USER_NAME='Configure CI/CD Variable'
MAC_SIGNING_PACKAGE_SIGNING_PREFIX='com.basic4glj.desktop.'

java --version

# Load variables from local
if [ -e "$ENV_FILE_PATH" ]; then
  echo 'Using local .env file'
  set -a
  . "$ENV_FILE_PATH"
  set +a
else
  echo 'Local .env file not found'
fi

./gradlew -v
./gradlew clean build copyJarsForJPackage

# TODO having trouble with signing..
echo "Create app-image Version '$APP_RELEASE_VERSION'"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image-mac.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --icon "icons/icon.icns" \
  --verbose

echo "Sign app-image"
cp "$MAC_SIGNING_EMBEDDED_PROVISIONPROFILE_FILE_PATH" ./build/distributions/Basic4GLj.app/Contents/embedded.provisionprofile
if [[  -z "$MAC_SIGNING_KEYCHAIN_PATH" ]]; then
  sh ./build-mac-sign.sh --app-location "./build/distributions/Basic4GLj.app" \
     --signing-identity "$MAC_SIGNING_KEY_USER_NAME" \
     --identifier-prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" \
     --entitlements "sandbox.plist" \
     --inherited-entitlements "embedded-tool.plist" \
     --mac-bundle-identifier "com.basic4glj.desktop" \
     --app-name "Basic4GLj"
 else
  sh ./build-mac-sign.sh --app-location "./build/distributions/Basic4GLj.app" \
     --signing-identity "$MAC_SIGNING_KEY_USER_NAME" \
     --signing-keychain "$MAC_SIGNING_KEYCHAIN_PATH" \
     --identifier-prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" \
     --entitlements "sandbox.plist" \
     --inherited-entitlements "embedded-tool.plist" \
     --mac-bundle-identifier "com.basic4glj.desktop" \
     --app-name "Basic4GLj"
fi


echo "Create native installer"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-mac.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --verbose

if [[  -z "$MAC_SIGNING_KEYCHAIN_PATH" ]]; then
  /usr/bin/codesign --force --timestamp \
      --options runtime \
      --sign "$MAC_SIGNING_KEY_USER_NAME" \
      --entitlements "sandbox.plist" \
      --prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}.dmg"
else
  /usr/bin/codesign --force --timestamp \
      --options runtime \
      --sign "$MAC_SIGNING_KEY_USER_NAME" \
      --keychain "$MAC_SIGNING_KEYCHAIN_PATH" \
      --entitlements "sandbox.plist" \
      --prefix "$MAC_SIGNING_PACKAGE_SIGNING_PREFIX" "./build/distributions/Basic4GLj-${APP_RELEASE_VERSION}.dmg"
fi