#!/bin/bash

set -e # die on error

ENV_FILE_PATH='./.env'

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

./gradlew -v
./gradlew clean build copyJarsForJPackage

# TODO having trouble with signing..
echo "Create app-image Version '$APP_RELEASE_VERSION'"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-app-image-mac.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --icon "icons/icon.icns" \
  --verbose

echo "Create native installer"
jpackage "@jpackage/jpackage.cfg" \
  "@jpackage/jpackage-mac.cfg" \
  --app-version "$APP_RELEASE_VERSION" \
  --verbose